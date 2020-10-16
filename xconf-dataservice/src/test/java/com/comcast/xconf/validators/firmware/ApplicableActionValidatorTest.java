/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Stanislav Menshykov
 * Created: 2/10/16  3:41 PM
 */
package com.comcast.xconf.validators.firmware;


import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static com.comcast.xconf.firmware.DefinePropertiesTemplateAction.PropertyValue;
import static com.comcast.xconf.firmware.DefinePropertiesTemplateAction.ValidationType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ApplicableActionValidatorTest {

    private final String defaultConfig = "defaultConfigId";

    private ApplicableActionValidator applicableActionValidator = new ApplicableActionValidator();

    @Before
    public void setUp() throws Exception {
        setupFirmwareConfigDao(createFirmwareConfig(defaultConfig));
    }

    @Test
    public void validateRuleAction_Less100PercentageIsValid() throws Exception {
        applicableActionValidator.validate(
                createFirmwareRule(entryFrom("distributionID1", 30), entryFrom("distributionID2", 40)));
    }

    @Test
    public void validateRuleAction_MoreThan100PercentageIsNotAllowed() throws Exception {
        FirmwareRule rule = createFirmwareRule(entryFrom("distributionID1", 30), entryFrom("distributionID2", 80));
        assertValidateThrowsException(rule, "Total percent sum should not be bigger than 100");
    }

    @Test
    public void validateRuleAction_NegativePercentageIsNotAllowed() throws Exception {
        FirmwareRule rule = createFirmwareRule(entryFrom("distributionID1", -30), entryFrom("distributionID2", 80));
        assertValidateThrowsException(rule, "Percentage could not be negative or 0 or bigger that 100");
    }

    @Test
    public void validateRuleAction_DuplicatesAreNotAllowed() throws Exception {
        String distributionID = "distributionID";
        FirmwareRule rule = createFirmwareRule(entryFrom(distributionID, 30), entryFrom(distributionID, 40));
        assertValidateThrowsException(rule, "Distribution contains duplicate firmware configs");
    }

    @Test
    public void validatePropertyType_AnyStringIsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.STRING);

        applicableActionValidator.validatePropertyType("abcd", validationTypes);
        applicableActionValidator.validatePropertyType("1", validationTypes);
        applicableActionValidator.validatePropertyType("-10", validationTypes);
        applicableActionValidator.validatePropertyType("1.1.1.1", validationTypes);
        applicableActionValidator.validatePropertyType("1::1", validationTypes);
    }

    @Test
    public void validatePropertyType_NumberFrom0to100IsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.PERCENT);

        applicableActionValidator.validatePropertyType("1", validationTypes);
        applicableActionValidator.validatePropertyType("100", validationTypes);
        assertValidatePropertyTypeThrowsException("-1", validationTypes);
        assertValidatePropertyTypeThrowsException("101", validationTypes);
        assertValidatePropertyTypeThrowsException("1.1.1.1", validationTypes);
        assertValidatePropertyTypeThrowsException("1::1", validationTypes);
        assertValidatePropertyTypeThrowsException("http://comcast.com", validationTypes);
    }

    @Test
    public void validatePropertyType_NumberFrom1To65535IsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.PORT);

        applicableActionValidator.validatePropertyType("1", validationTypes);
        applicableActionValidator.validatePropertyType("65535", validationTypes);
        assertValidatePropertyTypeThrowsException("0", validationTypes);
        assertValidatePropertyTypeThrowsException("65536", validationTypes);
        assertValidatePropertyTypeThrowsException("1.1.1.1", validationTypes);
        assertValidatePropertyTypeThrowsException("1::1", validationTypes);
        assertValidatePropertyTypeThrowsException("http://comcast.com", validationTypes);
    }

    @Test
    public void validatePropertyType_AnyUrlIsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.URL);

        applicableActionValidator.validatePropertyType("http://comcast.com", validationTypes);
        assertValidatePropertyTypeThrowsException("http://comcast.", validationTypes);
        assertValidatePropertyTypeThrowsException("comcast.com", validationTypes);
        assertValidatePropertyTypeThrowsException("string", validationTypes);
        assertValidatePropertyTypeThrowsException("1.1.1.1", validationTypes);
        assertValidatePropertyTypeThrowsException("1::1", validationTypes);
    }

    @Test
    public void validatePropertyType_Ipv4IsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.IPV4);

        applicableActionValidator.validatePropertyType("1.1.1.1", validationTypes);
        applicableActionValidator.validatePropertyType("1.1.1.1/31", validationTypes);
        assertValidatePropertyTypeThrowsException("1.1.1.555", validationTypes);
        assertValidatePropertyTypeThrowsException("1::1", validationTypes);
        assertValidatePropertyTypeThrowsException("http://comcast.com", validationTypes);
    }

    @Test
    public void validatePropertyType_Ipv6IsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.IPV6);

        applicableActionValidator.validatePropertyType("1::1", validationTypes);
        assertValidatePropertyTypeThrowsException("0", validationTypes);
        assertValidatePropertyTypeThrowsException("1.1.1.1", validationTypes);
        assertValidatePropertyTypeThrowsException("http://comcast.com", validationTypes);
    }

    @Test
    public void validatePropertyType_Ipv4OrUrlIsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.IPV4, ValidationType.URL);

        applicableActionValidator.validatePropertyType("1.1.1.1", validationTypes);
        applicableActionValidator.validatePropertyType("http://comcast.com", validationTypes);
        assertValidatePropertyTypeThrowsException("0", validationTypes);
        assertValidatePropertyTypeThrowsException("1::1", validationTypes);
    }

    @Test
    public void validatePropertyType_AnyNumberIsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.PERCENT, ValidationType.NUMBER);

        applicableActionValidator.validatePropertyType("999", validationTypes);
        applicableActionValidator.validatePropertyType("-666", validationTypes);
        applicableActionValidator.validatePropertyType("42", validationTypes);
        assertValidatePropertyTypeThrowsException("string", validationTypes);
    }

    @Test
    public void validatePropertyType_BooleanIsValid() throws Exception {
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.PERCENT, ValidationType.BOOLEAN);

        applicableActionValidator.validatePropertyType("true", validationTypes);
        applicableActionValidator.validatePropertyType("FALSE", validationTypes);
        applicableActionValidator.validatePropertyType("42", validationTypes);
        assertValidatePropertyTypeThrowsException("string", validationTypes);
    }

    @Test
    public void failsValidateCorrespondentPropertyFromRule_PropertyIsRequiredButEmpty() throws Exception {
        final String propertyKey = "propertyKey";
        final PropertyValue propertyValue = createPropertyValue(false);
        final String message = "Property " + propertyKey + " is required";
        Map<String, String> propertiesWithEmptyStringValue = new HashMap<String, String>(){{put(propertyKey, "");}};
        Map<String, String> propertiesWithNullValue = new HashMap<String, String>(){{put(propertyKey, null);}};

        assertValidateCorrespondentPropertyFromRuleThrowsException(propertyKey, propertyValue, propertiesWithEmptyStringValue, message);
        assertValidateCorrespondentPropertyFromRuleThrowsException(propertyKey, propertyValue, propertiesWithNullValue, message);
    }

    @Test
    public void passesValidateCorrespondentPropertyFromRule_PropertyIsNotRequiredAndNull() throws Exception {
        final String propertyKey = "propertyKey";
        final PropertyValue propertyValue = createPropertyValue(true);
        Map<String, String> validPropertiesToValidate = new HashMap<String, String>(){{put(propertyKey, null);}};

        applicableActionValidator.validateCorrespondentPropertyFromRule(propertyKey, propertyValue, validPropertiesToValidate);
    }

    @Test
    public void passesValidateCorrespondentPropertyFromRule_PropertyIsValid() throws Exception {
        final String propertyKey = "propertyKey";
        final PropertyValue propertyValue = createPropertyValue(true);
        final String validStringProperty = "validStringProperty";
        Map<String, String> validPropertiesToValidate = new HashMap<String, String>(){{put(propertyKey, validStringProperty);}};

        applicableActionValidator.validateCorrespondentPropertyFromRule(propertyKey, propertyValue, validPropertiesToValidate);
    }

    @Test
    public void failsValidateCorrespondentPropertyFromRule_PropertyIsInvalid() throws Exception {
        final String propertyKey = "propertyKey";
        final PropertyValue propertyValueWithPortValidationType = createPropertyValue(true);
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.PORT);
        propertyValueWithPortValidationType.setValidationTypes(validationTypes);
        final String notAPortProperty = "invalidProperty";
        Map<String, String> invalidPropertiesToValidate = new HashMap<String, String>(){{put(propertyKey, notAPortProperty);}};

        assertValidateCorrespondentPropertyFromRuleThrowsException(propertyKey, propertyValueWithPortValidationType,
                                    invalidPropertiesToValidate, "Property must be one of the following types: " + validationTypes);
    }

    @Test
    public void passesValidateApplicableActionPropertiesGeneric_TemplateDoesNotExist() throws Exception {
        String templateId = "MAC_RULE";
        setOne(templateId, null);
        Map<String, String> propertiesToValidate = null;

        applicableActionValidator.validateApplicableActionPropertiesGeneric(templateId, propertiesToValidate);
    }

    @Test
    public void passesValidateApplicableActionPropertiesGeneric_TemplateActionIsNotDefineProperties() throws Exception {
        String templateId = "MAC_RULE";
        ApplicableAction notDefinePropertiesAction = new BlockingFilterAction();
        createAndSaveTemplate(templateId, notDefinePropertiesAction);
        Map<String, String> propertiesToValidate = null;

        applicableActionValidator.validateApplicableActionPropertiesGeneric(templateId, propertiesToValidate);
    }

    @Test
    public void passesValidateApplicableActionPropertiesGeneric_TemplateActionsPropertiesIsNull() throws Exception {
        String templateId = "MAC_RULE";
        DefinePropertiesTemplateAction applicableAction = new DefinePropertiesTemplateAction();
        applicableAction.setProperties(null);
        createAndSaveTemplate(templateId, applicableAction);
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{put("key", "value");}};

        applicableActionValidator.validateApplicableActionPropertiesGeneric(templateId, propertiesToValidate);
    }

    @Test
    public void failsValidateApplicableActionPropertiesGeneric_PropertyOfInvalidType() throws Exception {
        String templateId = "MAC_RULE";
        final String propertyKey = "propertyKey";
        List<ValidationType> validationTypes = Arrays.asList(ValidationType.IPV4);
        createAndSaveTemplate(templateId, propertyKey, validationTypes, true);
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{put(propertyKey, "invalidValue");}};

        assertValidateApplicableActionPropertiesGenericThrowsException(templateId, propertiesToValidate,
                                                    "Property must be one of the following types: " + validationTypes);
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_DownloadLocationFilterFirmwareDownloadProtocolIsEmpty() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "");}};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate,
                                                                                        "FirmwareDownloadProtocol must be 'http' or 'tftp'");
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_DownloadLocationFilterTFTPFirmwareLocationIsNotIpv4() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "tftp");
            put(ConfigNames.FIRMWARE_LOCATION, null);
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate,
                                                                                            "FirmwareLocation must be valid ipv4 address");
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_DownloadLocationFilterTFTPIpv6FirmwareLocationIsInvalid() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "tftp");
            put(ConfigNames.FIRMWARE_LOCATION, "1.1.1.1");
            put(ConfigNames.IPV6_FIRMWARE_LOCATION, "notIpv6");
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate,
                                                                                        "Ipv6FirmwareLocation must be valid ipv6 address");
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_DownloadLocationFilterHTTPFirmwareLocationIsEmpty() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "http");
            put(ConfigNames.FIRMWARE_LOCATION, "");
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate,
                                                                                                    "FirmwareLocation must not be empty");
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_DownloadLocationFilterHTTPIpv6FirmwareLocationIsNotEmpty() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "http");
            put(ConfigNames.FIRMWARE_LOCATION, "notEmpty");
            put(ConfigNames.IPV6_FIRMWARE_LOCATION, "notEmpty");
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate,
                                                                                                    "Ipv6FirmwareLocation must be empty");
    }

    @Test
    public void passesValidateApplicableActionPropertiesSpecific_DownloadLocationFilterTFTP() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "tftp");
            put(ConfigNames.FIRMWARE_LOCATION, "1.1.1.1");
            put(ConfigNames.IPV6_FIRMWARE_LOCATION, "1::1");
        }};

        applicableActionValidator.validateApplicableActionPropertiesSpecific(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate);
    }

    @Test
    public void passesValidateApplicableActionPropertiesSpecific_DownloadLocationFilterHTTP() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, "http");
            put(ConfigNames.FIRMWARE_LOCATION, "someString");
        }};

        applicableActionValidator.validateApplicableActionPropertiesSpecific(TemplateNames.DOWNLOAD_LOCATION_FILTER, propertiesToValidate);
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_RebootImmediatelyFilter() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.REBOOT_IMMEDIATELY, "invalid");
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.REBOOT_IMMEDIATELY_FILTER, propertiesToValidate,
                                                                                                    "Reboot immediately must be boolean");
    }

    @Test
    public void passesValidateApplicableActionPropertiesSpecific_RebootImmediatelyFilter() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.REBOOT_IMMEDIATELY, "false");
        }};

        applicableActionValidator.validateApplicableActionPropertiesSpecific(TemplateNames.REBOOT_IMMEDIATELY_FILTER, propertiesToValidate);
    }

    @Test
    public void failsValidateApplicableActionPropertiesSpecific_MinVersionCheck() throws Exception {
        Map<String, String> propertiesToValidate = new HashMap<String, String>(){{
            put(ConfigNames.REBOOT_IMMEDIATELY, "invalid");
        }};

        assertValidateApplicableActionPropertiesSpecificThrowsException(TemplateNames.MIN_CHECK_RI, propertiesToValidate,
                "Reboot immediately must be boolean");
    }

    @Test
    public void passesValidateApplicableActionPropertiesSpecific_MinVersionCheck_RebootImmediatelyIsOptional() throws Exception {
        applicableActionValidator.validateApplicableActionPropertiesSpecific(TemplateNames.MIN_CHECK_RI, Collections.<String, String>emptyMap());
    }

    @Test
    public void passesValidateApplicableActionPropertiesSpecific_EmptyTemplateType() throws Exception {
        applicableActionValidator.validateApplicableActionPropertiesSpecific("", new HashMap<String, String>());
    }

    @Test
    public void passesValidateDefinePropertiesApplicableAction_NullTemplateType() throws Exception {
        applicableActionValidator.validateDefinePropertiesApplicableAction(new DefinePropertiesAction(), null);
    }

    @Test
    public void passesValidateDefinePropertiesApplicableAction_RebootImmediatelyFilterPropertiesIsValid() throws Exception {
        DefinePropertiesAction action = createRebootImmediatelyDefinePropertiesAction("true");

        applicableActionValidator.validateDefinePropertiesApplicableAction(action, TemplateNames.REBOOT_IMMEDIATELY_FILTER);
    }

    @Test
    public void failsGenericValidationInValidateDefinePropertiesApplicableAction_RebootImmediatelyFilterIsEmpty() throws Exception {
        assertFailsValidateDefinePropertiesApplicableAction("", "Property rebootImmediately is required");
    }

    @Test
    public void failsSpecificValidationInValidateDefinePropertiesApplicableAction_RebootImmediatelyFilterIsInvalid() throws Exception {
        assertFailsValidateDefinePropertiesApplicableAction("notBoolean", "Reboot immediately must be boolean");
    }

    @Test
    public void failsValidate_ApplicableActionIsNull() throws Exception {
        FirmwareRule rule = new FirmwareRule();

        assertValidateThrowsException(rule, "Applicable action must not be null");
    }

    @Test
    public void failsValidate_ApplicableActionTypeIsNull() throws Exception {
        FirmwareRule rule = new FirmwareRule();
        rule.setApplicableAction(new ApplicableAction());

        assertValidateThrowsException(rule, "Applicable action must not be null");
    }

    @Test
    public void passesValidate_PropertiesIsValid() throws Exception {
        String validRebootImmediatelyPropertyValue = "false";

        applicableActionValidator.validate(createRebootImmediatelyFirmwareRule(validRebootImmediatelyPropertyValue));
    }

    @Test
    public void failsValidate_PropertiesIsInValid() throws Exception {
        String invalidRebootImmediatelyPropertyValue = "invalidBoolean";

        assertValidateThrowsException(createRebootImmediatelyFirmwareRule(invalidRebootImmediatelyPropertyValue),
                                                                                    "Reboot immediately must be boolean");
    }

    private FirmwareRule createRebootImmediatelyFirmwareRule(String propertyValue) {
        FirmwareRule rule = new FirmwareRule();
        rule.setType(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
        rule.setApplicableAction(createRebootImmediatelyDefinePropertiesAction(propertyValue));

        return rule;
    }

    private DefinePropertiesAction createRebootImmediatelyDefinePropertiesAction(final String propertyValue) {
        DefinePropertiesAction action = new DefinePropertiesAction();
        action.setProperties(new HashMap<String, String>() {{
            put(ConfigNames.REBOOT_IMMEDIATELY, propertyValue);
        }});
        createAndSaveTemplate(TemplateNames.REBOOT_IMMEDIATELY_FILTER, ConfigNames.REBOOT_IMMEDIATELY, Arrays.asList(ValidationType.STRING), false);

        return action;
    }

    private void assertFailsValidateDefinePropertiesApplicableAction(final String propertyValue, String message) {
        DefinePropertiesAction action = createRebootImmediatelyDefinePropertiesAction(propertyValue);

        assertValidateDefinePropertiesApplicableActionThrowsException(action, TemplateNames.REBOOT_IMMEDIATELY_FILTER, message);
    }

    private FirmwareRuleTemplate createAndSaveTemplate(final String templateId,
                                                       final String propertyKey,
                                                       final List<ValidationType> validationTypes,
                                                       boolean isOptional) {
        final PropertyValue templatePropertyValue = new PropertyValue();
        templatePropertyValue.setValidationTypes(validationTypes);
        templatePropertyValue.setOptional(isOptional);
        DefinePropertiesTemplateAction applicableAction = new DefinePropertiesTemplateAction();
        applicableAction.setProperties(new HashMap<String, PropertyValue>(){{put(propertyKey, templatePropertyValue);}});

        return createAndSaveTemplate(templateId, applicableAction);
    }

    private FirmwareRuleTemplate createAndSaveTemplate(String id, ApplicableAction applicableAction) {
        FirmwareRuleTemplate template = new FirmwareRuleTemplate();
        template.setId(id);
        template.setApplicableAction(applicableAction);
        setOne(id, template);

        return template;
    }

    private FirmwareRule createFirmwareRule(RuleAction.ConfigEntry... entries) {
        FirmwareRule firmwareRule = new FirmwareRule();
        RuleAction ruleAction = new RuleAction();
        ruleAction.setConfigId(defaultConfig);
        ruleAction.setConfigEntries(Arrays.asList(entries));
        firmwareRule.setApplicableAction(ruleAction);
        return firmwareRule;
    }

    private FirmwareConfig createFirmwareConfig(String id) {
        FirmwareConfig config = new FirmwareConfig();
        config.setId(id);
        config.setFirmwareFilename("firmwareFilename");
        config.setFirmwareLocation("firmwareLocation");
        return config;
    }

    private RuleAction.ConfigEntry entryFrom(String id, double percent) {
        return new RuleAction.ConfigEntry(id, 0.0, percent);
    }

    private void setOne(String id, FirmwareRuleTemplate value) {
        setOne("firmwareRuleTemplateDao", id, value);
    }

    private <T extends IPersistable> void setOne(String fieldName, String id, T value) {
        Field field = ReflectionUtils.findField(ApplicableActionValidator.class, fieldName);
        field.setAccessible(true);

        CachedSimpleDao<String, T> dao = mock(CachedSimpleDao.class);
        when(dao.getOne(id)).thenReturn(value);
        ReflectionUtils.setField(field, applicableActionValidator, dao);
    }

    private void setupFirmwareConfigDao(FirmwareConfig firmwareConfig) {
        Field field = ReflectionUtils.findField(ApplicableActionValidator.class, "firmwareConfigDAO");
        field.setAccessible(true);

        CachedSimpleDao<String, FirmwareConfig> dao = mock(CachedSimpleDao.class);
        when(dao.getOne(anyString())).thenReturn(firmwareConfig);
        ReflectionUtils.setField(field, applicableActionValidator, dao);
    }

    private PropertyValue createPropertyValue(boolean isOptional) {
        final PropertyValue result = new PropertyValue();
        result.setOptional(isOptional);
        result.setValidationTypes(Arrays.asList(ValidationType.STRING));

        return result;
    }

    private void assertValidatePropertyTypeThrowsException(String property, List<ValidationType> validationTypes) {
        String message = "Property must be one of the following types: " + validationTypes;
        try {
            applicableActionValidator.validatePropertyType(property, validationTypes);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertValidateCorrespondentPropertyFromRuleThrowsException(String propertyKey,
                                                                            PropertyValue propertyValue,
                                                                            Map<String, String> properties,
                                                                            String message) {
        try {
            applicableActionValidator.validateCorrespondentPropertyFromRule(propertyKey, propertyValue, properties);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertValidateApplicableActionPropertiesGenericThrowsException(String templateId, Map<String, String> properties, String message) {
        try {
            applicableActionValidator.validateApplicableActionPropertiesGeneric(templateId, properties);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertValidateApplicableActionPropertiesSpecificThrowsException(String templateId, Map<String, String> properties, String message) {
        try {
            applicableActionValidator.validateApplicableActionPropertiesSpecific(templateId, properties);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertValidateDefinePropertiesApplicableActionThrowsException(DefinePropertiesAction definePropertiesAction, String templateId, String message) {
        try {
            applicableActionValidator.validateDefinePropertiesApplicableAction(definePropertiesAction, templateId);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }

    private void assertValidateThrowsException(FirmwareRule rule, String message) {
        try {
            applicableActionValidator.validate(rule);
            fail("Expected exception to be thrown ValidationRuntimeException: " + message);
        } catch (ValidationRuntimeException e) {
            assertEquals(message, e.getMessage());
        }
    }
}

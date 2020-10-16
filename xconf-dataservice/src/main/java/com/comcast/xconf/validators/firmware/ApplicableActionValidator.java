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
 * Created: 2/10/16  11:20 AM
 */
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.dcm.core.Utils;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.util.IpAddressUtils;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.comcast.xconf.firmware.DefinePropertiesTemplateAction.*;

@Component
public class ApplicableActionValidator {

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    private final String TFTP = "tftp";
    private final String HTTP = "http";

    public void validate(FirmwareRule rule) {
        ApplicableAction action = rule.getApplicableAction();
        if (action == null || action.getActionType() == null) {
            throw new ValidationRuntimeException("Applicable action must not be null");
        }

        if (action.getActionType() == Type.RULE) {
            if (!(action instanceof RuleAction)) {
                throw new ValidationRuntimeException("ApplicableAction type is not valid");
            }
            validateRuleAction(rule, (RuleAction) action);

        } else if (action.getActionType() == Type.DEFINE_PROPERTIES) {
            if (!(action instanceof DefinePropertiesAction)) {
                throw new ValidationRuntimeException("ApplicableAction type is not valid");
            }
            validateDefinePropertiesApplicableAction((DefinePropertiesAction) action, rule.getType());
        }
    }

    private void validateRuleAction(FirmwareRule firmwareRule, RuleAction action) {
        if (StringUtils.isBlank(action.getConfigId())) {
            return; // noop rule
        }

        validateConfigId(firmwareRule, action.getConfigId());

        List<RuleAction.ConfigEntry> configEntries = action.getConfigEntries();
        List<String> configList = new ArrayList<>();
        if (configEntries != null) {
            int totalPercentage = 0;
            for (RuleAction.ConfigEntry entry : configEntries) {

                String configId = entry.getConfigId();
                validateConfigId(firmwareRule, configId);

                if (configList.contains(configId)) {
                    throw new ValidationRuntimeException("Distribution contains duplicate firmware configs");
                }
                configList.add(configId);

                Double percentage = entry.getPercentage();
                validatePercentageRange(percentage, "Percentage");
                totalPercentage += percentage;

                Double startPercentRange = entry.getStartPercentRange();
                validatePercentageRange(startPercentRange, "StartPercentRange");
                Double endPercentRange = entry.getEndPercentRange();
                validatePercentageRange(endPercentRange, "EndPercentRange");
            }
            if (totalPercentage > 100) {
                throw new ValidationRuntimeException("Total percent sum should not be bigger than 100");
            }
        }

    }

    private void validatePercentageRange(Double value, String name) {
        if (value == null) {
            throw new ValidationRuntimeException(name + " is not defined for distribution config");
        }
        if (value < 0 || value > 100 ) {
            throw new ValidationRuntimeException(name + " could not be negative or 0 or bigger that 100");
        }
    }

    private void validateConfigId(FirmwareRule firmwareRule, String configId) {
        if (StringUtils.isBlank(configId)) {
            throw new ValidationRuntimeException("ConfigId could not be blank");
        }
        FirmwareConfig firmwareConfig = firmwareConfigDAO.getOne(configId);
        if (firmwareConfig == null) {
            throw new ValidationRuntimeException("FirmwareConfig with ID '" + configId + "' doesn't exist");
        }

        if (!ApplicationType.equals(firmwareRule.getApplicationType(), firmwareConfig.getApplicationType())) {
            throw new ValidationRuntimeException("Application types of FirmwareConfig " + firmwareConfig.getDescription() + " and FirmwareRule " + firmwareRule.getName() + " do not match");

        }
    }

    @VisibleForTesting
    void validateDefinePropertiesApplicableAction(DefinePropertiesAction action, String templateType) {
        if (templateType != null) {
            Map<String, String> properties = action.getProperties() != null ? action.getProperties() : new HashMap<String, String>();
            validateApplicableActionPropertiesGeneric(templateType, properties);
            validateApplicableActionPropertiesSpecific(templateType, properties);
        }
    }

    @VisibleForTesting
    void validateApplicableActionPropertiesGeneric(String templateType, Map<String, String> propertiesFromRule) {
        FirmwareRuleTemplate template = firmwareRuleTemplateDao.getOne(templateType);
        if (template != null) {
            ApplicableAction templateAction = template.getApplicableAction();
            if (templateAction instanceof DefinePropertiesTemplateAction) {
                Map<String, PropertyValue> templateProperties =
                                                    ((DefinePropertiesTemplateAction) templateAction).getProperties();
                if (templateProperties != null) {
                    for (Map.Entry<String, PropertyValue> propertyEntry : templateProperties.entrySet()) {
                        String templatePropertyKey = propertyEntry.getKey();
                        PropertyValue templatePropertyValue = propertyEntry.getValue();
                        if (templatePropertyKey != null && templatePropertyValue != null) {
                            validateCorrespondentPropertyFromRule(templatePropertyKey, templatePropertyValue, propertiesFromRule);
                        }
                    }
                }
            }
        }
    }

    @VisibleForTesting
    void validateCorrespondentPropertyFromRule(String templatePropertyKey,
                                                       PropertyValue templatePropertyValue,
                                                       Map<String, String> propertiesFromRule) {
        String correspondentPropertyFromRule = propertiesFromRule.get(templatePropertyKey);
        if (StringUtils.isBlank(correspondentPropertyFromRule) && !templatePropertyValue.isOptional()) {
            throw new ValidationRuntimeException("Property " + templatePropertyKey + " is required");
        } else if (!StringUtils.isBlank(correspondentPropertyFromRule)) {
            List<ValidationType> validationTypes = templatePropertyValue.getValidationTypes();
            if (CollectionUtils.isNotEmpty(validationTypes)) {
                validatePropertyType(correspondentPropertyFromRule, validationTypes);
            }
        }
    }

    @VisibleForTesting
    void validatePropertyType(String propertyToValidate, List<ValidationType> validationTypes) {
        if (!validationTypes.contains(ValidationType.STRING)) {
            if (!(canBeNumber(validationTypes) && isNumber(propertyToValidate)) &&
                    !(canBeBoolean(validationTypes) && isBoolean(propertyToValidate)) &&
                    !(canBePercent(validationTypes) && isPercent(propertyToValidate)) &&
                    !(canBePort(validationTypes) && isPort(propertyToValidate)) &&
                    !(canBeUrl(validationTypes) && isUrl(propertyToValidate)) &&
                    !(canBeIpv4(validationTypes) && isIpv4(propertyToValidate)) &&
                    !(canBeIpv6(validationTypes) && isIpv6(propertyToValidate))) {
                throw new ValidationRuntimeException("Property must be one of the following types: " + validationTypes);
            }
        }
    }

    @VisibleForTesting
    void validateApplicableActionPropertiesSpecific(String templateType, Map<String, String> properties) {
        switch (templateType) {
            case TemplateNames.DOWNLOAD_LOCATION_FILTER:
                validateDownloadLocationFilterApplicableActionProperties(properties);
                break;
            case TemplateNames.REBOOT_IMMEDIATELY_FILTER:
                validateRebootImmediatelyFilterApplicableActionProperties(properties);
                break;
            case TemplateNames.MIN_CHECK_RI:
                validateMinVersionCheckApplicableActionProperties(properties);
                break;
            default:
                //do nothing;
        }
    }

    private boolean canBeNumber(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.NUMBER);
    }

    private boolean canBeBoolean(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.BOOLEAN);
    }

    private boolean canBePercent(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.PERCENT);
    }

    private boolean canBePort(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.PORT);
    }

    private boolean canBeUrl(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.URL);
    }

    private boolean canBeIpv4(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.IPV4);
    }

    private boolean canBeIpv6(List<ValidationType> validationTypes) {
        return validationTypes.contains(ValidationType.IPV6);
    }

    private boolean isNumber(String value) {
        return NumberUtils.isNumber(value);
    }

    private boolean isBoolean(String value) {
        return Boolean.TRUE.toString().equalsIgnoreCase(value) || Boolean.FALSE.toString().equalsIgnoreCase(value);
    }

    private boolean isPercent(String value) {
        try {
            Float parsedValue = Float.parseFloat(value);
            return parsedValue >= 0 && parsedValue <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isPort(String value) {
        try{
            Integer parsedValue = Integer.parseInt(value);
            return  parsedValue >= 1 && parsedValue <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isUrl(String value) {
        return Utils.isValidUrl(value);
    }

    private boolean isIpv4(String value) {
        return IpAddressUtils.isValidIpv4Address(value);
    }

    private boolean isIpv6(String value) {
        return IpAddressUtils.isValidIpv6Address(value);
    }

    private void validateDownloadLocationFilterApplicableActionProperties(Map<String, String> properties) {
        String firmwareDownloadProtocol = properties.get(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL);
        String firmwareLocation = properties.get(ConfigNames.FIRMWARE_LOCATION);
        String ipv6FirmwareLocation = properties.get(ConfigNames.IPV6_FIRMWARE_LOCATION);

        validateFirmwareDowloadProtocol(firmwareDownloadProtocol);

        if (TFTP.equals(firmwareDownloadProtocol)) {
            validateTftpLocation(firmwareLocation, ipv6FirmwareLocation);
        } else if (HTTP.equals(firmwareDownloadProtocol)) {
            validateHttpLocation(firmwareLocation, ipv6FirmwareLocation);
        }
    }

    private void validateMinVersionCheckApplicableActionProperties(Map<String, String> properties) {
        String rebootImmediately = properties.get(ConfigNames.REBOOT_IMMEDIATELY);
        if (!StringUtils.isBlank(rebootImmediately)) {
            validateRebootImmediately(rebootImmediately);
        }
    }

    private void validateRebootImmediatelyFilterApplicableActionProperties(Map<String, String> properties) {
        validateRebootImmediately(properties.get(ConfigNames.REBOOT_IMMEDIATELY));
    }

    private void validateRebootImmediately(String rebootImmediately) {
        if (!isBoolean(rebootImmediately)) {
            throw new ValidationRuntimeException("Reboot immediately must be boolean");
        }
    }

    private void validateFirmwareDowloadProtocol(String firmwareDownloadProtocol) {
        if (!TFTP.equals(firmwareDownloadProtocol) && !HTTP.equals(firmwareDownloadProtocol)) {
            throw new ValidationRuntimeException("FirmwareDownloadProtocol must be 'http' or 'tftp'");
        }
    }

    private void validateTftpLocation(String firmwareLocation, String ipv6FirmwareLocation) {
        if (!IpAddressUtils.isValidIpv4Address(firmwareLocation)) {
            throw new ValidationRuntimeException("FirmwareLocation must be valid ipv4 address");
        }
        if (!StringUtils.isEmpty(ipv6FirmwareLocation) && !IpAddressUtils.isValidIpv6Address(ipv6FirmwareLocation)) {
            throw new ValidationRuntimeException("Ipv6FirmwareLocation must be valid ipv6 address");
        }
    }

    private void validateHttpLocation(String firmwareLocation, String ipv6FirmwareLocation) {
        if (StringUtils.isEmpty(firmwareLocation)) {
            throw new ValidationRuntimeException("FirmwareLocation must not be empty");
        }
        if (!StringUtils.isEmpty(ipv6FirmwareLocation)) {
            throw new ValidationRuntimeException("Ipv6FirmwareLocation must be empty");
        }
    }
}

/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.service.firmware;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.estbfirmware.PercentageBeanQueriesService;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.converter.PercentageBeanConverter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.firmware.FirmwareRuleDataServiceValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.comcast.xconf.util.ImportHelper.*;

@Service
public class FirmwareRuleDataService extends FirmwareRuleService {

    @Autowired
    private FirmwareRuleDataServiceValidator validator;

    @Autowired
    private PercentageBeanQueriesService percentageBeanQueriesService;

    @Autowired
    protected PercentageBeanConverter converter;

    public final static String APPLICATION_TYPE = "applicationType";

    private static final Logger logger = LoggerFactory.getLogger(FirmwareRuleDataService.class);

    @Override
    public IValidator<FirmwareRule> getValidator() {
        return validator;
    }

    @Override
    public void validateOnSave(FirmwareRule firmwareRule) {
        getValidator().validate(firmwareRule);
        getValidator().validateAll(firmwareRule, filterByApplicationType(getAll(), firmwareRule.getApplicationType()));
    }

    @Override
    public List<FirmwareRule> getAll() {
        return getEntityDAO().getAll();
    }

    @Override
    protected String getEntityName(FirmwareRule firmwareRule) {
        return firmwareRule != null ? firmwareRule.getName() : null;
    }

    @Override
    public Map<String, List<String>> importOrUpdateAll(List<FirmwareRule> firmwareRules) {
        Map<String, List<String>> importResult = buildImportResultMap();
        for (FirmwareRule entity : firmwareRules) {
            if (entity != null) {
                try {
                    beforeImport(entity);
                    if (StringUtils.isNotBlank(entity.getId()) && getEntityDAO().getOne(entity.getId()) != null) {
                        checkRuleTypeAndUpdate(entity);
                    } else {
                        checkRuleTypeAndCreate(entity);
                    }
                    importResult.get(IMPORTED).add(getEntityName(entity));
                } catch (Exception e) {
                    logger.error("Exception: " + JsonUtil.toJson(entity), e);
                    importResult.get(NOT_IMPORTED).add(getEntityName(entity));
                }
            }
        }
        return importResult;
    }

    private void checkRuleTypeAndUpdate(FirmwareRule firmwareRule) {
        if (TemplateNames.ENV_MODEL_RULE.equals(firmwareRule.getType())) {
            percentageBeanQueriesService.update(converter.convertIntoBean(firmwareRule));
        } else {
            update(firmwareRule);
        }
    }

    private void checkRuleTypeAndCreate(FirmwareRule firmwareRule) {
        if (TemplateNames.ENV_MODEL_RULE.equals(firmwareRule.getType())) {
            percentageBeanQueriesService.create(converter.convertIntoBean(firmwareRule));
        } else {
            create(firmwareRule);
        }
    }

    public Map<String, String> toNormalized(Map<String, String> context) {
        Map<String, String> normalizedMap = new HashMap<>();
        for (Map.Entry<String, String> customEntry : getCustomContextKeys().entrySet()) {
            if(StringUtils.isNotBlank(context.get(customEntry.getKey()))) {
                normalizedMap.put(customEntry.getValue(), context.get(customEntry.getKey()));
            }
        }
        return normalizedMap;
    }

    @Override
    protected String getWriteApplicationType(FirmwareRule firmwareRule) {
        return firmwareRule.getApplicationType();
    }

    private Map<String, String> getCustomContextKeys() {
        Map<String, String> contextKeys = new HashMap<>();
        contextKeys.put("applicationType", SearchFields.APPLICATION_TYPE);
        contextKeys.put("name", SearchFields.NAME);
        contextKeys.put("key", SearchFields.FREE_ARG);
        contextKeys.put("value", SearchFields.FIXED_ARG);
        contextKeys.put("firmwareVersion", SearchFields.FIRMWARE_VERSION);
        contextKeys.put("templateId", SearchFields.TEMPLATE_ID);
        return contextKeys;
    }
}
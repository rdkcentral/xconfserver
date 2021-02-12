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
 * Author: ikostrov
 * Created: 13.08.15 19:59
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.EnvModelRuleConverter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.ENV_MODEL_RULE;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class EnvModelRuleService {

    private static final Logger log = LoggerFactory.getLogger(EnvModelRuleService.class);

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public Set<EnvModelRuleBean> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(ENV_MODEL_RULE))
                .filter(byApplication(applicationType))
                .map(this::convertFirmwareRuleToEnvModelRuleBean)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public List<EnvModelRuleBean> getAll() {
        return firmwareRuleDao.getAll()
                .stream().filter(firmwareRulePredicates.byTemplate(ENV_MODEL_RULE))
                .map(this::convertFirmwareRuleToEnvModelRuleBean)
                .collect(Collectors.toList());
    }

    public void save(EnvModelRuleBean bean, String applicationType) {
        if (bean.getId() == null) {
            String id = UUID.randomUUID().toString();
            bean.setId(id);
        }
        FirmwareRule envModelRule = convertModelRuleBeanToFirmwareRule(bean);
        if (StringUtils.isNotBlank(applicationType)) {
            envModelRule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(envModelRule.getId(), envModelRule);
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public EnvModelRuleBean convertFirmwareRuleToEnvModelRuleBean(FirmwareRule firmwareRule) {
        EnvModelRuleBean bean = EnvModelRuleConverter.convertFirmwareRuleToEnvModelRuleBean(firmwareRule);
        RuleAction action = (RuleAction) firmwareRule.getApplicableAction();
        if (StringUtils.isNotBlank(action.getConfigId())) {
            bean.setFirmwareConfig(firmwareConfigDAO.getOne(action.getConfigId()));
        }

        return bean;
    }

    public FirmwareRule convertModelRuleBeanToFirmwareRule(EnvModelRuleBean bean) {
        return EnvModelRuleConverter.convertModelRuleBeanToFirmwareRule(bean);
    }

    public EnvModelRuleBean getOne(String id) {
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if (fr != null) {
            return convertFirmwareRuleToEnvModelRuleBean(fr);
        }
        return null;
    }

    public EnvModelRuleBean getOneByName(String name, String applicationType) {
        for (EnvModelRuleBean envModelRule : getByApplicationType(applicationType)) {
            if (envModelRule.getName().equalsIgnoreCase(name)) {
                return envModelRule;
            }
        }
        return null;
    }

    public EnvModelRuleBean getOneByEnvModel(String model, String environment, String applicationType) {
        for (EnvModelRuleBean envModelRule : getByApplicationType(applicationType)) {
            if (StringUtils.equalsIgnoreCase(envModelRule.getEnvironmentId(), environment)
                    && StringUtils.equalsIgnoreCase(envModelRule.getModelId(), model)) {
                return envModelRule;
            }
        }
        return null;
    }

    public FirmwareConfig nullifyUnwantedFields(FirmwareConfig config) {
        if (config != null) {
            config.setUpdated(null);
            config.setFirmwareDownloadProtocol(null);
            config.setRebootImmediately(null);
        }

        return config;
    }

    public boolean isExistEnvModelRule(EnvModelRuleBean envModelRule, String applicationType) {
        if (envModelRule != null && envModelRule.getEnvironmentId() != null && envModelRule.getModelId() != null) {
            EnvModelRuleBean one = getOneByEnvModel(envModelRule.getModelId(), envModelRule.getEnvironmentId(), applicationType);
            return one != null;
        }
        return false;
    }

}

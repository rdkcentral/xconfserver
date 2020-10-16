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
 * Created: 31.08.15 21:34
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.Environment;
import com.comcast.xconf.XRule;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import com.comcast.xconf.validators.common.EnvironmentValidator;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class EnvironmentQueriesService {

    @Autowired
    private CachedSimpleDao<String, Environment> environmentDAO;

    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    protected CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Autowired
    protected CachedSimpleDao<String, SettingRule> settingRuleDAO;

    @Autowired
    private EnvironmentValidator validator;

    public String checkUsage(String id) {
        List<? extends CachedSimpleDao<String, ? extends IPersistable>> daoList = Arrays.asList(
                dcmRuleDAO, firmwareRuleDao, firmwareRuleTemplateDao, telemetryRuleDAO, featureRuleDAO, settingRuleDAO
        );
        for (CachedSimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable persistable : Optional.presentInstances(dao.asLoadingCache().asMap().values())) {
                XRule xRule = (XRule) persistable;
                if (RuleUtil.isExistConditionByFreeArgAndFixedArg(xRule.getRule(), RuleFactory.ENV.getName(), id)) {
                    return String.format("ruleType=%s name=%s", xRule.getRuleType(), xRule.getName());
                }
            }
        }
        return null;
    }

    public boolean isExistEnvironment(String envId) {
        if (StringUtils.isNotBlank(envId)) {
            return environmentDAO.getOne(envId) != null;
        }
        return false;
    }

    public IValidator getValidator() {
        return validator;
    }
}

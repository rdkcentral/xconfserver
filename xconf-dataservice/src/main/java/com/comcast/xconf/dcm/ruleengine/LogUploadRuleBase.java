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
 * Author: slavrenyuk
 * Created: 6/20/14
 */
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.main.api.IRuleProcessor;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.SortingManager;
import com.comcast.xconf.evaluators.RuleProcessorFactory;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.logupload.Settings;
import com.comcast.xconf.logupload.SettingsUtil;
import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogUploadRuleBase {

    private SettingsUtil settingsUtil = new SettingsUtil();

    @Autowired
    private SettingsDAO settingsDAO;

    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    private RuleProcessorFactory ruleProcessorFactory;

    public Settings eval(LogUploaderContext context) {
        Settings settings = new Settings();

        List<DCMGenericRule> rules = SortingManager.sortRulesByPriorityAsc(Optional.presentInstances(dcmRuleDAO.asLoadingCache().asMap().values()));
        for (DCMGenericRule rule : rules) {
            IRuleProcessor<Condition, Rule> processor = ruleProcessorFactory.get();

            if (ApplicationType.equals(rule.getApplicationType(), context.getApplication()) && processor.evaluate(rule, context.getProperties())) {
                settingsUtil.copySettings(settings, settingsDAO.get(rule.getId()), rule, context.getEstbMacAddress(), context.getTimeZone());
            }

            if (settings.areFull()) {
                return settings;
            }
        }

        if (settings.getGroupName() != null || settings.getVodSettingsName() != null) {
            return settings;
        }

        return null;
    }
}

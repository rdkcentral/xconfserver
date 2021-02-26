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
 * Author: Igor Kostrov
 * Created: 5/13/2016
*/
package com.comcast.xconf.estbfirmware.migration;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.migration.MigrationController;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MigrationControllerTest extends BaseQueriesControllerTest {

    @Autowired
    private MigrationController migrationController;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    /**
     * First create all templates, try to delete one template.
     * During startup this template should not be recreated if any other templates are exist.
     */
    @Test
    public void initializeTemplatesDuringStartup() {
        migrationController.initializeTemplates();
        firmwareRuleTemplateDao.deleteOne(TemplateNames.IP_RULE);

        migrationController.initializeTemplates();

        FirmwareRuleTemplate ipRuleTemplate = firmwareRuleTemplateDao.getOne(TemplateNames.IP_RULE);

        Assert.assertNull(ipRuleTemplate);
        Assert.assertEquals(4, getRuleTemplatesCount());
    }

    private int getRuleTemplatesCount() {
        Iterable<FirmwareRuleTemplate> all = Optional.presentInstances(firmwareRuleTemplateDao.asLoadingCache().asMap().values());
        return Lists.newArrayList(Iterables.filter(all, input -> input.getApplicableAction() != null
                && input.getApplicableAction().getActionType() == ApplicableAction.Type.RULE_TEMPLATE)).size();
    }
}

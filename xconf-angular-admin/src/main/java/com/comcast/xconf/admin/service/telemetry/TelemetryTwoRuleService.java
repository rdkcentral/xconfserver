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
 * Author: Jeyabala Murugan
 * Created: 05/08/2020
 */
package com.comcast.xconf.admin.service.telemetry;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.validator.telemetry.TelemetryTwoRuleValidator;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.permissions.PermissionService;
import com.comcast.xconf.permissions.TelemetryPermissionService;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.telemetry.TelemetryTwoRulePredicates;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.comcast.xconf.util.RuleUtil;
import com.comcast.xconf.validators.IValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class TelemetryTwoRuleService extends AbstractApplicationTypeAwareService<TelemetryTwoRule> {

    @Autowired
    private CachedSimpleDao<String, TelemetryTwoRule> telemetryTwoRuleDAO;

    @Autowired
    private TelemetryTwoRuleValidator telemetryTwoRuleValidator;

    @Autowired
    private TelemetryTwoRulePredicates telemetryTwoRulePredicates;

    @Autowired
    private TelemetryPermissionService permissionService;

    @Override
    public PermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public CachedSimpleDao<String, TelemetryTwoRule> getEntityDAO() {
        return telemetryTwoRuleDAO;
    }

    @Override
    public IValidator<TelemetryTwoRule> getValidator() {
        return telemetryTwoRuleValidator;
    }

    @Override
    protected void normalizeOnSave(TelemetryTwoRule entity) {
        RuleUtil.normalizeConditions(entity);
    }

    @Override
    protected List<Predicate<TelemetryTwoRule>> getPredicatesByContext(Map<String, String> context) {
        ContextOptional contextOptional = new ContextOptional(context);
        contextOptional.setApplicationTypeIfNotPresent(permissionService.getReadApplication());
        return telemetryTwoRulePredicates.getPredicates(contextOptional);
    }
}
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
 * Created: 7/8/2016
*/
package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;

@Component
public class TemplateConsistencyValidator {

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    public void validate(FirmwareRule rule) {
        String templateId = rule.getTemplateId();
        FirmwareRuleTemplate template = firmwareRuleTemplateDao.getOne(templateId, false);
        if (template == null) {
            throw new ValidationRuntimeException("Can't create rule from non existing template: " + templateId);
        }
        validate(rule.getRule(), template.getRule());
    }

    @VisibleForTesting
    void validate(Rule rule, Rule template) {
        List<FreeArg> ruleFreeArgs = getFreeArgList(RuleUtil.toConditions(rule));
        List<FreeArg> templateFreeArgs = getFreeArgList(RuleUtil.toConditions(template));

        ruleFreeArgs.removeAll(templateFreeArgs);

        if (ruleFreeArgs.size() != 0) {
            throw new ValidationRuntimeException("FreeArg(s) are not present in template: " + getFreeArgNames(ruleFreeArgs));
        }
    }

    private List<FreeArg> getFreeArgList(Iterable<Condition> conditions) {
        return Lists.newArrayList(Iterables.transform(conditions, new Function<Condition, FreeArg>() {
            @Nullable
            @Override
            public FreeArg apply(Condition input) {
                return input.getFreeArg();
            }
        }));
    }

    private List<String> getFreeArgNames(Iterable<FreeArg> freeArgs) {
        return Lists.newArrayList(Iterables.transform(freeArgs, new Function<FreeArg, String>() {
            @Nullable
            @Override
            public String apply(FreeArg input) {
                return input.getName();
            }
        }));
    }
}

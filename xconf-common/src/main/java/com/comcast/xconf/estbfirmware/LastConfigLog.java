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
 * Author: Alexander Binkovsky
 * Created: 7/7/14  7:52 AM
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.estbfirmware.util.LogsCompatibilityUtils;
import com.comcast.xconf.firmware.BlockingFilterAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@ListingCF(cfName = CfNames.Common.LOGS,
        key2FieldName = "column1",
        ttl = 90*24*60*60,
        compress = true)
public class LastConfigLog extends XMLPersistable {
    public static final String LAST_CONFIG_LOG_ID = "0";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getId(){
        return this.id;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date getUpdated(){
        return this.updated;
    }

    EstbFirmwareContext.Converted input;
    RuleInfo rule;

    List<RuleInfo> filters = new ArrayList<>();
    String explanation;
    FirmwareConfigFacade config;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean hasMinimumFirmware;

    public LastConfigLog(EstbFirmwareContext context,
                         String explanation,
                         FirmwareConfigFacade config,
                         Iterable<Object> appliedFilters,
                         FirmwareRule evaluatedRule
                     ) {
        this.input = context.convert();
        this.explanation = explanation;
        this.config = config;
        if (evaluatedRule != null) {
            rule = new RuleInfo(evaluatedRule);
        }

        for (Object f : appliedFilters) {
            filters.add(new RuleInfo(f));
        }

        this.id = LAST_CONFIG_LOG_ID;
    }

    public LastConfigLog() {
        id = LAST_CONFIG_LOG_ID;
    }

    public EstbFirmwareContext.Converted getInput() {
        return input;
    }

    public void setInput(EstbFirmwareContext.Converted input) {
        this.input = input;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public FirmwareConfigFacade getConfig() {
        return config;
    }

    public void setConfig(FirmwareConfigFacade config) {
        this.config = config;
    }

    public RuleInfo getRule() {
        return rule;
    }

    public void setRule(RuleInfo rule) {
        this.rule = rule;
    }

    public List<RuleInfo> getFilters() {
        return filters;
    }

    public void setFilters(List<RuleInfo> filters) {
        this.filters = filters;
    }

    public Boolean isHasMinimumFirmware() {
        return hasMinimumFirmware;
    }

    public void setHasMinimumFirmware(Boolean hasMinimumFirmware) {
        this.hasMinimumFirmware = hasMinimumFirmware;
    }

    @JsonIgnore
    public boolean isBlocked() {
        for (RuleInfo f : filters) {
            if (f.isBlocking()) {
                return true;
            }
        }
        return false;
    }

    public static class RuleInfo {

        public RuleInfo(Object ruleOrFilter) {
            if (ruleOrFilter instanceof FirmwareRule) {
                FirmwareRule firmwareRule = (FirmwareRule)ruleOrFilter;
                type = firmwareRule.getType();
                name = firmwareRule.getName();
                noop = firmwareRule.isNoop();
                blocking = firmwareRule.getApplicableAction() instanceof BlockingFilterAction;
            } else if (ruleOrFilter instanceof SingletonFilterValue) {
                type = LogsCompatibilityUtils.getRuleTypeInfo(ruleOrFilter);
                name = ((SingletonFilterValue) ruleOrFilter).getId();
                noop = true;
            } else if (ruleOrFilter instanceof RuleAction) {
                type = "DistributionPercentInRuleAction";
                name = "DistributionPercentInRuleAction";
                noop = false;
            } else if (ruleOrFilter instanceof PercentageBean) {
                type = "PercentageBean";
                name = ((PercentageBean) ruleOrFilter).getName();
                noop = false;
            }
            id = LogsCompatibilityUtils.getRuleIdInfo(ruleOrFilter);
        }

        public RuleInfo() {
        }

        String type;
        String id;
        String name;
        boolean noop;
        boolean blocking = false;

        @JsonIgnore
        public boolean isBlocking() {
            return blocking;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isNoop() {
            return noop;
        }

        public void setNoop(boolean noop) {
            this.noop = noop;
        }
    }
}

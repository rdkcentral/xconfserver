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
 * Created: 2/15/2016
*/
package com.comcast.xconf.estbfirmware.evaluation;

import com.comcast.xconf.estbfirmware.FirmwareConfigFacade;
import com.comcast.xconf.firmware.FirmwareRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EvaluationResult {
    private FirmwareRule matchedRule;
    private List<Object> appliedFilters = new ArrayList<>();
    private FirmwareConfigFacade firmwareConfig;
    private String description;
    private boolean blocked;
    private Map<String, String> appliedVersionInfo = new HashMap<>();

    public FirmwareRule getMatchedRule() {
        return matchedRule;
    }

    public void setMatchedRule(FirmwareRule matchedRule) {
        this.matchedRule = matchedRule;
    }

    public List<Object> getAppliedFilters() {
        return appliedFilters;
    }

    public void setAppliedFilters(List<Object> appliedFilters) {
        this.appliedFilters = appliedFilters;
    }

    public FirmwareConfigFacade getFirmwareConfig() {
        return firmwareConfig;
    }

    public void setFirmwareConfig(FirmwareConfigFacade firmwareConfig) {
        this.firmwareConfig = firmwareConfig;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Map<String, String> getAppliedVersionInfo() {
        return appliedVersionInfo;
    }

    public void setAppliedVersionInfo(Map<String, String> appliedVersionInfo) {
        this.appliedVersionInfo = appliedVersionInfo;
    }


    public enum DefaultValue {
        BLOCKED,
        NOMATCH,
        NORULETYPE
    }
}

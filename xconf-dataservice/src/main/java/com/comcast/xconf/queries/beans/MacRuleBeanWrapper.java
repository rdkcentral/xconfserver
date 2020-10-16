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
 * Created: 20.08.15 21:03
*/
package com.comcast.xconf.queries.beans;

import com.comcast.xconf.estbfirmware.MacRuleBean;

import java.util.HashSet;
import java.util.Set;

public class MacRuleBeanWrapper extends MacRuleBean {

    private Set<String> macList = new HashSet<>();

    public MacRuleBeanWrapper() {}

    public MacRuleBeanWrapper(MacRuleBean bean, Set<String> macList) {
        super(bean);
        this.setTargetedModelIds(bean.getTargetedModelIds());
        this.setFirmwareConfig(bean.getFirmwareConfig());
        this.macList = macList;
    }

    public Set<String> getMacList() {
        return macList;
    }

    public void setMacList(Set<String> macList) {
        this.macList = macList;
    }
}

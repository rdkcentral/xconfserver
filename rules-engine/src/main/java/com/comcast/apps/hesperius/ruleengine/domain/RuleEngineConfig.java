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
 * Author: pbura
 * Created: 12/06/2014  15:24
 */
package com.comcast.apps.hesperius.ruleengine.domain;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RuleEngineConfig {
    private List<String> evaluatorClasses = new ArrayList<String>();
    private boolean allowDefaultOperationsOverrides = true;

    public List<String> getEvaluatorClasses() {
        return evaluatorClasses;
    }

    public void setEvaluatorClasses(List<String> evaluatorClasses) {
        this.evaluatorClasses = evaluatorClasses;
    }

    public boolean isAllowDefaultOperationsOverrides() {
        return allowDefaultOperationsOverrides;
    }

    public void setAllowDefaultOperationsOverrides(boolean allowDefaultOperationsOverrides) {
        this.allowDefaultOperationsOverrides = allowDefaultOperationsOverrides;
    }

    public static enum Provider {
        INSTANCE();

        private final RuleEngineConfig config;

        public RuleEngineConfig getConfig() {
            return config;
        }

        private Provider() {
            final InputStream configInput = Provider.class.getClassLoader().getResourceAsStream(RuleUtils.EngineConstants.CONFIG_FILE_NAME);
            if (configInput != null)
                config = RuleUtils.fromJSON(RuleEngineConfig.class, configInput);
            else config = new RuleEngineConfig();
        }
    }
}

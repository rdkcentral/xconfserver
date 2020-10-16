/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

/*
 * Author: Igor Kostrov
 * Created: 11/23/2018
*/
package com.comcast.xconf.admin.controller.rfc;

import com.comcast.xconf.featurecontrol.FeatureControlRuleBase;
import com.comcast.xconf.rfc.FeatureControl;
import com.comcast.xconf.rfc.FeatureRule;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RfcTestPageService {

    @Autowired
    private FeatureControlRuleBase featureControlRuleBase;

    public HashMap<String, Object> processFeatureRules(@RequestBody Map<String, String> context, String application) {
        HashMap<String, Object> result = new HashMap<>();
        List<FeatureRule> matchedRules = featureControlRuleBase.processFeatureRules(context, application);
        result.put("result", CollectionUtils.isNotEmpty(matchedRules) ? Collections.singletonMap("", matchedRules) : null);
        FeatureControl featureControl = featureControlRuleBase.eval(context, application);
        postProcessFeatureControl(featureControl, context);
        result.put("featureControl", featureControl);
        result.put("context", context);
        return result;
    }

    protected void normalizeContext(Map<String, String> context) {
        featureControlRuleBase.normalizeContext(context);
    }

    protected void postProcessFeatureControl(FeatureControl featureControl, Map<String, String> context) {
        // Do nothing.
    }

}

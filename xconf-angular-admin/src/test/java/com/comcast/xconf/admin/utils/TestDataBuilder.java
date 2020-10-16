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
 * Author: Yury Stagit
 * Created: 12/14/16  12:00 PM
 */
package com.comcast.xconf.admin.utils;

import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestDataBuilder {

    public static Feature createFeature() {
        String id = UUID.randomUUID().toString();
        Feature feature = new Feature();
        feature.setId(id);
        modifyFeature(feature, id);
        return feature;
    }

    public static Feature modifyFeature(Feature feature, String id) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(id + "-key", id + "-value");

        feature.setName(id + "-name");
        feature.setFeatureName(id + "-featureName");
        feature.setEffectiveImmediate(false);
        feature.setEnable(false);
        feature.setConfigData(properties);
        return feature;
    }

    public static FeatureRule createFeatureRule(List<String> featureIds, Rule rule) {
        String id = UUID.randomUUID().toString();
        FeatureRule featureRule = new FeatureRule();
        featureRule.setId(id);
        return modifyFeatureRule(featureRule, id, featureIds, rule);
    }

    public static FeatureRule modifyFeatureRule(FeatureRule featureRule, String id, List<String> featureIds, Rule rule) {
        featureRule.setName(id + "-name");
        featureRule.setFeatureIds(featureIds);
        featureRule.setRule(rule);
        return featureRule;
    }

    public static Rule createRule(Condition condition) {
        Rule rule = new Rule();
        rule.setCondition(condition);
        return rule;
    }

    public static Condition createCondition(FreeArg freeArg, Operation operation, String value) {
        return new Condition(freeArg, operation, FixedArg.from(value));
    }

}

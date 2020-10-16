/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.thucydides.util.rfc;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.thucydides.util.GenericTestUtils;
import com.comcast.xconf.thucydides.util.HttpClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FeatureRuleUtils {
    public static final String RFC_FEATURE_RULE = "rfc/featurerule";
    public static FeatureRule createFeatureRule(String ruleName, String featureName) throws IOException {
        Feature feature = FeatureUtils.createAndSaveFeature(featureName);
        final FeatureRule featureRule = new FeatureRule();
        featureRule.setId(UUID.randomUUID().toString());
        featureRule.setName(ruleName);
        featureRule.setFeatureIds(Collections.singletonList(feature.getId()));
        featureRule.setPriority(1);
        featureRule.setRule(createRule(ruleName));
        return featureRule;
    }

    public static FeatureRule createAndSaveFeatureRule(String ruleName, String featureName) throws IOException {
        final FeatureRule featureRule = createFeatureRule(ruleName,featureName);
        HttpClient.post(GenericTestUtils.buildFullUrl(RFC_FEATURE_RULE), featureRule);
        return featureRule;
    }

    public static FeatureRule createAndSaveFeatureRule(String ruleName, List<String> features,  Condition condition) throws IOException {
        final FeatureRule featureRule = new FeatureRule();
        featureRule.setName(ruleName);
        featureRule.setFeatureIds(features);
        featureRule.setRule(createRule(ruleName, condition));
        HttpClient.post(GenericTestUtils.buildFullUrl(RFC_FEATURE_RULE), featureRule);
        return featureRule;
    }

    private static Rule createRule(String name) {
        return Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING,name), StandardOperation.IS, FixedArg.from(name))).build();
    }

    private static Rule createRule(String name, Condition condition) {
        return Rule.Builder.of(condition).build();
    }

}

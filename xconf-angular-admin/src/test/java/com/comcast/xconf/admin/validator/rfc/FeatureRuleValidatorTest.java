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

package com.comcast.xconf.admin.validator.rfc;

import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.RuleValidationException;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.validators.rfc.FeatureRuleValidator;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.comcast.xconf.firmware.ApplicationType.STB;

public class FeatureRuleValidatorTest extends BaseControllerTest {

    @Autowired
    private FeatureRuleValidator featureRuleValidator;

    @org.junit.Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void validateStartRangeLessThan0() throws Exception {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Percent range -10-10 is not valid");

        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), createRangeRule("-10-10"));
        featureRuleValidator.validate(featureRule);
    }

    @Test
    public void validateEndRangeIsGreaterThen100() throws Exception {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("End range 105.0 is not valid");

        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), createRangeRule("10-105"));
        featureRuleValidator.validate(featureRule);
    }

    @Test
    public void validateRangeContainsNotNumericSymbol() throws Exception {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Percent range 1a0-50 is not valid");

        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), createRangeRule("1a0-50"));
        featureRuleValidator.validate(featureRule);
    }

    @Test
    public void validateRangeSeparatedByWrongSymbol() throws Exception {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Range format exception 10:50, format pattern is: startRange-endRange");

        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), createRangeRule("10:50"));
        featureRuleValidator.validate(featureRule);
    }

    @Test
    public void validateRangesAreOverlappedByEachOther() throws Exception {
        exceptionRule.expect(RuleValidationException.class);
        exceptionRule.expectMessage("Ranges overlap each other");

        Feature feature = createAndSaveFeature();
        FeatureRule featureRule = createFeatureRule(Collections.singletonList(feature.getId()), createMultipleRangesRule("10-50", "30-70"));
        featureRuleValidator.validate(featureRule);
    }

    private FeatureRule createFeatureRule(List<String> featureIds, Rule rule) {
        FeatureRule featureRule = new FeatureRule();
        featureRule.setId(UUID.randomUUID().toString());
        featureRule.setName("featureRuleName");
        featureRule.setPriority(1);
        featureRule.setRule(rule);
        featureRule.setApplicationType(STB);
        featureRule.setFeatureIds(featureIds);
        return featureRule;
    }

    private Feature createAndSaveFeature() throws Exception {
        Feature feature = new Feature();
        feature.setId(UUID.randomUUID().toString());
        feature.setName("featureName");
        feature.setFeatureName("featureName");
        feature.setWhitelisted(false);
        feature.setConfigData(Collections.singletonMap("key", "value"));
        feature.setEnable(true);
        feature.setApplicationType(STB);
        featureDAO.setOne(feature.getId(), feature);
        return feature;
    }

    private Rule createRangeRule(String range) {
        return Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_MAC), RuleFactory.RANGE, FixedArg.from(range))).build();
    }

    private Rule createMultipleRangesRule(String range1, String range2) {
        return Rule.Builder.of(new Condition(new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_MAC), RuleFactory.RANGE, FixedArg.from(range1)))
                .and(new Condition(new FreeArg(StandardFreeArgType.STRING, LogUploaderContext.ESTB_MAC), RuleFactory.RANGE, FixedArg.from(range2))).build();
    }
}

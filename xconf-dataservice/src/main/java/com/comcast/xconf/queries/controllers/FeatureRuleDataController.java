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

package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.service.rfc.FeatureRuleDataService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.comcast.xconf.queries.controllers.FeatureRuleDataController.API_URL;

@RestController
@RequestMapping(API_URL)
public class FeatureRuleDataController extends BaseQueriesController {

    public static final String API_URL = "/featurerule";

    @Autowired
    private FeatureRuleDataService featureRuleService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity getAll() {
        return new ResponseEntity<>(featureRuleService.getAll(), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/filtered")
    public ResponseEntity getFiltered(@RequestParam Map<String, String> context) {
        List<FeatureRule> featureRules = featureRuleService.findByContext(context);
        return new ResponseEntity<>(featureRules, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importAll")
    public ResponseEntity importAll(@RequestBody List<FeatureRule> featureRules) {
        Map<String, List<String>> importResult = featureRuleService.importOrUpdateAll(featureRules);
        return new ResponseEntity<>(importResult, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}")
    public FeatureRule getOne(@PathVariable String id) {
        validateId(id);
        return featureRuleService.getOne(id);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteOne(@PathVariable String id) {
        validateId(id);
        featureRuleService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody FeatureRule featureRule) {
        featureRuleService.create(featureRule);
        return new ResponseEntity<>(featureRule, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public FeatureRule update(@RequestBody FeatureRule featureRule) {
        featureRuleService.update(featureRule);
        return featureRule;
    }

    private void validateId(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ValidationRuntimeException("Id is blank");
        }
    }
}

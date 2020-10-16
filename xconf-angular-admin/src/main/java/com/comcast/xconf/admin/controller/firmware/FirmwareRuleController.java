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
 *  Author: mdolina
 *  Created: 11/20/15 7:03 PM
 */
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.firmware.FirmwareRuleService;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.utils.PageUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.comcast.xconf.search.SearchUtils.excludeFields;

@RestController
@RequestMapping(value = FirmwareRuleController.URL_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
public class FirmwareRuleController extends AbstractController<FirmwareRule> {

    public static final String URL_MAPPING = "api/firmwarerule";

    @Autowired
    private FirmwareRuleService firmwareRuleService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FIRMWARE_RULE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FIRMWARE_RULES.getName();
    }

    @Override
    public AbstractService<FirmwareRule> getService() {
        return firmwareRuleService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/filtered")
    @Override
    public ResponseEntity getFiltered(
            @RequestBody(required = false) Map<String, String> context,
            @RequestParam final Integer pageSize,
            @RequestParam final Integer pageNumber) {
        Map<String, String> simplifiedContext = excludeFields(context, SearchFields.APPLICABLE_ACTION_TYPE);
        List<FirmwareRule> firmwareRules = firmwareRuleService.findByContext(simplifiedContext);
        HttpHeaders headers = new HttpHeaders();
        putSizesOfRulesByTypeIntoHeaders(headers, firmwareRules);
        if (MapUtils.isNotEmpty(context) && StringUtils.isNotBlank(context.get(SearchFields.APPLICABLE_ACTION_TYPE))) {
            ApplicableAction.Type type = ApplicableAction.Type.valueOf(context.get(SearchFields.APPLICABLE_ACTION_TYPE));
            firmwareRules = firmwareRuleService.filterByActionType(firmwareRules, type);
        }

        Collections.sort(firmwareRules);
        return new ResponseEntity<>(PageUtils.getPage(firmwareRules, pageNumber, pageSize), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/names", method = RequestMethod.GET)
    public Map<String, String> getIdToNamesMapByType(@PathVariable final String type) {
        return firmwareRuleService.getIdToNameMap(type);
    }

    @RequestMapping(value = "/byTemplate/{templateId}/names", method = RequestMethod.GET)
    public ResponseEntity getFirmwareRuleNamesByType(@PathVariable String templateId) {
        return new ResponseEntity<>(firmwareRuleService.getFirmwareRuleNames(templateId), HttpStatus.OK);
    }

    @RequestMapping(value="/export/byType", params = "exportAll", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAllByType(@RequestParam String type) {
        ApplicableAction.Type actionType = ApplicableAction.Type.valueOf(type);
        String currentApplicationType = firmwareRuleService.getPermissionService().getReadApplication();
        List<FirmwareRule> entities = firmwareRuleService.filterByActionAndApplicationTypes(actionType, currentApplicationType);

        if (ApplicableAction.Type.RULE.equals(actionType)) {
            type = type + "_ACTION";
        }

        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + "_" + type + "_" + currentApplicationType);
        return new ResponseEntity<>(entities, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity exportOne(@PathVariable String id) {
        FirmwareRule firmwareRule = getService().getOne(id);
        String applicationType = firmwareRuleService.getPermissionService().getReadApplication();
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + firmwareRule.getId() + "_" + applicationType);
        return new ResponseEntity<>(Collections.singleton(firmwareRule), headers, HttpStatus.OK);
    }

    @RequestMapping(value="/export/allTypes", method = RequestMethod.GET, params = "exportAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAll(@RequestParam(required = false) String applicationType) {
        List<FirmwareRule> entities = firmwareRuleService.getAll();
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + "_" + firmwareRuleService.getPermissionService().getReadApplication());
        return new ResponseEntity<>(entities, headers, HttpStatus.OK);
    }

    private HttpHeaders putSizesOfRulesByTypeIntoHeaders(final HttpHeaders headers, final List<FirmwareRule> allRules) {
        List<FirmwareRule> filteredByRuleType = firmwareRuleService.filterByActionType(allRules, ApplicableAction.Type.RULE);
        List<FirmwareRule> filteredByDefinePropertiesType = firmwareRuleService.filterByActionType(allRules, ApplicableAction.Type.DEFINE_PROPERTIES);
        List<FirmwareRule> filteredByBlockingFilterType = firmwareRuleService.filterByActionType(allRules, ApplicableAction.Type.BLOCKING_FILTER);

        headers.add(ApplicableAction.Type.RULE.toString(), Integer.toString(filteredByRuleType.size()));
        headers.add(ApplicableAction.Type.DEFINE_PROPERTIES.toString(), Integer.toString(filteredByDefinePropertiesType.size()));
        headers.add(ApplicableAction.Type.BLOCKING_FILTER.toString(), Integer.toString(filteredByBlockingFilterType.size()));

        return headers;
    }
}

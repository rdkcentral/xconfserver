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
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.priority.PriorityComparator;
import com.comcast.xconf.search.SearchFields;
import com.comcast.xconf.service.firmware.FirmwareRuleTemplateService;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.shared.utils.PageUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = FirmwareRuleTemplateController.URL_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
public class FirmwareRuleTemplateController extends AbstractController<FirmwareRuleTemplate> {

    public static final String URL_MAPPING = "api/firmwareruletemplate";

    @Autowired
    private FirmwareRuleTemplateService firmwareRuleTemplateService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FIRMWARE_RULE_TEMPLATE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FIRMWARE_RULE_TEMPLATES.getName();
    }

    @Override
    public AbstractService<FirmwareRuleTemplate> getService() {
        return firmwareRuleTemplateService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/filtered")
    @Override
    public ResponseEntity getFiltered(
            @RequestBody(required = false) Map<String, String> searchContext,
            @RequestParam final Integer pageSize,
            @RequestParam final Integer pageNumber) {
        List<FirmwareRuleTemplate> firmwareRuleTemplates = firmwareRuleTemplateService.findByContext(searchContext);
        if (MapUtils.isEmpty(searchContext) || StringUtils.isBlank(SearchFields.APPLICABLE_ACTION_TYPE)) {
            return new ResponseEntity<>(PageUtils.getPage(firmwareRuleTemplates, pageNumber, pageSize, new PriorityComparator<FirmwareRuleTemplate>()), HttpStatus.OK);
        }

        HttpHeaders headers = new HttpHeaders();
        putSizesOfTemplatesByTypeIntoHeaders(headers, firmwareRuleTemplates);
        ApplicableAction.Type type = ApplicableAction.Type.valueOf(searchContext.get(SearchFields.APPLICABLE_ACTION_TYPE));
        headers.add("templateSizeByType", Integer.toString(getTemplatesByType(type, firmwareRuleTemplateService.getAll()).size()));
        firmwareRuleTemplates = getTemplatesByType(type, firmwareRuleTemplates);

        return new ResponseEntity<>(PageUtils.getPage(firmwareRuleTemplates, pageNumber, pageSize, new PriorityComparator<FirmwareRuleTemplate>()), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all/{type}")
    public ResponseEntity getByType(
            @PathVariable final ApplicableAction.Type type) {

        List<FirmwareRuleTemplate> firmwareRuleTemplates = getTemplatesByType(type, firmwareRuleTemplateService.getAll());
        return new ResponseEntity<>(firmwareRuleTemplates, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/import")
    public ResponseEntity importTemplates(@RequestBody List<OverwriteWrapper<FirmwareRuleTemplate>> wrappedFirmwareRuleTemplates) {
        return new ResponseEntity<>(firmwareRuleTemplateService.importTemplates(wrappedFirmwareRuleTemplates), HttpStatus.OK);
    }

    @RequestMapping(value = "/ids", method = RequestMethod.GET)
    public List<String> getFirmwareRuleTemplateIds(@RequestParam(required = false) final ApplicableAction.Type type) {
        return firmwareRuleTemplateService.getTemplateIds(type);
    }

    @RequestMapping(value="/{id}/priority/{newPriority}", method = RequestMethod.POST)
    public ResponseEntity changePriorities(@PathVariable final String id, @PathVariable final Integer newPriority) {
        List<FirmwareRuleTemplate> reorganizedTemplates = firmwareRuleTemplateService.changePriorities(id, newPriority);
        return new ResponseEntity<>(reorganizedTemplates, HttpStatus.OK);
    }

    @RequestMapping(value="/export", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAllByType(@RequestParam String type) {
        List<FirmwareRuleTemplate> entities = getTemplatesByType(ApplicableAction.Type.valueOf(type), getService().getAll());

        if (ApplicableAction.Type.valueOf(type) == ApplicableAction.Type.RULE_TEMPLATE) {
            type = type.replace("_TEMPLATE", "") + "_ACTION_TEMPLATE";
        }

        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + "_" + type);
        return new ResponseEntity<>(entities, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{type}/{isEditable}", method = RequestMethod.GET)
    public ResponseEntity getTemplatesByTypeAndEditableOption(@PathVariable ApplicableAction.Type type, @PathVariable boolean isEditable) {
        List<FirmwareRuleTemplate> templates = firmwareRuleTemplateService.getByTypeAndEditableOption(type, isEditable);
        return new ResponseEntity<>(templates, HttpStatus.OK);
    }

    private List<FirmwareRuleTemplate> getTemplatesByType(final ApplicableAction.Type type, List<FirmwareRuleTemplate> templates) {
        return Lists.newArrayList(Iterables.filter(templates, new Predicate<FirmwareRuleTemplate>() {
            @Override
            public boolean apply(FirmwareRuleTemplate input) {
                return input.getApplicableAction() != null && input.getApplicableAction().getActionType() == type;
            }
        }));
    }

    private HttpHeaders putSizesOfTemplatesByTypeIntoHeaders(final HttpHeaders headers, final List<FirmwareRuleTemplate> allTemplates) {
        List<FirmwareRuleTemplate> filteredByRuleTemplateType = getTemplatesByType(ApplicableAction.Type.RULE_TEMPLATE, allTemplates);
        List<FirmwareRuleTemplate> filteredByDefinePropertiesTemplateType = getTemplatesByType(ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE, allTemplates);
        List<FirmwareRuleTemplate> filteredByBlockingFilterTemplateType = getTemplatesByType(ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE, allTemplates);

        headers.add(ApplicableAction.Type.RULE_TEMPLATE.toString(), Integer.toString(filteredByRuleTemplateType.size()));
        headers.add(ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE.toString(), Integer.toString(filteredByDefinePropertiesTemplateType.size()));
        headers.add(ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE.toString(), Integer.toString(filteredByBlockingFilterTemplateType.size()));

        return headers;
    }
}

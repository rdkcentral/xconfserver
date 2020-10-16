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
package com.comcast.xconf.admin.controller.dcm;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.dcm.FormulaService;
import com.comcast.xconf.importing.OverwriteWrapper;
import com.comcast.xconf.logupload.*;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller to process REST queries for admin UI.
 * Created by ikostrov on 10/16/15.
 */
@RestController
@RequestMapping(value = FormulaQueryController.URL_MAPPING)
public class FormulaQueryController extends AbstractController<DCMGenericRule> {

    public static final String URL_MAPPING = "api/dcm/formula";

    @Autowired
    private CachedSimpleDao<String, VodSettings> vodSettingsDAO;

    @Autowired
    private CachedSimpleDao<String, DeviceSettings> deviceSettingsDAO;

    @Autowired
    private CachedSimpleDao<String, LogUploadSettings> logUploadSettingsDAO;

    @Autowired
    private FormulaService formulaService;

    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public Map<String, Object> createFormulaList(@RequestBody List<DCMRuleWithSettings> dcmRuleWithSettings) {
        return formulaService.importFormulas(dcmRuleWithSettings, false);
    }

    @RequestMapping(value = "/list", method = RequestMethod.PUT)
    public Map<String, Object> updateFormulaList(@RequestBody List<DCMRuleWithSettings> dcmRuleWithSettings) {
        return formulaService.importFormulas(dcmRuleWithSettings, true);
    }

    @RequestMapping(value = "/import/{overwrite}", method = RequestMethod.POST)
    public ResponseEntity importFormula(@RequestBody DCMRuleWithSettings formulaWithSettings, @PathVariable final Boolean overwrite) {
        return new ResponseEntity<>(formulaService.importFormula(formulaWithSettings, overwrite), HttpStatus.OK);
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public ResponseEntity importFormulas(@RequestBody List<OverwriteWrapper<DCMRuleWithSettings>> wrappedFormulasWithSettings) {
        return new ResponseEntity<>(formulaService.importFormulas(wrappedFormulasWithSettings), HttpStatus.OK);
    }

    @RequestMapping(value="/{id}/priority/{newPriority}", method = RequestMethod.POST)
    public ResponseEntity changePriorities(@PathVariable final String id, @PathVariable final Integer newPriority) {
        return new ResponseEntity<>(formulaService.changePriorities(id, newPriority), HttpStatus.OK);
    }

    @RequestMapping(value = "/settingsAvailability", method = RequestMethod.POST)
    public ResponseEntity getSettingsAvailability(@RequestBody final List<String> ids) {
        final Map<String, Map<String, Boolean>> result = new HashMap<>();
        for (final String id : ids) {
            result.put(id, new HashMap<String, Boolean>() {{
                put("deviceSettings", deviceSettingsDAO.getOne(id) != null);
                put("vodSettings", vodSettingsDAO.getOne(id) != null);
                put("logUploadSettings", logUploadSettingsDAO.getOne(id) != null);
            }});
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFormulasSize() {
        return Integer.toString(getService().getAll().size());
    }

    @RequestMapping(value = "/names", method = RequestMethod.GET)
    public ResponseEntity getFormulasNames() {
        return new ResponseEntity<>(formulaService.getFormulasNames(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity exportOne(@PathVariable final String id) {
        DCMRuleWithSettings result = formulaService.getFormulaForExport(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + result.getFormula().getId() + formulaService.getApplicationTypeSuffix());
        return new ResponseEntity<>(Lists.newArrayList(result), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity exportAll() {
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + formulaService.getApplicationTypeSuffix());
        return new ResponseEntity<>(formulaService.getAllFormulasForExport(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/formulasAvailability", method = RequestMethod.POST)
    public ResponseEntity getFormulasAvailability(@RequestBody List<String> settingsIds) {
        Map<String, Boolean> formulasAvailability = new HashedMap();
        for (String settingsId : settingsIds) {
            formulasAvailability.put(settingsId, getService().getEntityDAO().getOne(settingsId) != null);
        }
        return new ResponseEntity<>(formulasAvailability, HttpStatus.OK);
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FORMULA.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FORMULAS.getName();
    }

    @Override
    public AbstractService<DCMGenericRule> getService() {
        return formulaService;
    }
}

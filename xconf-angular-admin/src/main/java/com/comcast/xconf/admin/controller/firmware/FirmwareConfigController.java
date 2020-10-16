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
package com.comcast.xconf.admin.controller.firmware;

import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.core.Utils;
import com.comcast.xconf.admin.service.firmware.FirmwareConfigService;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.FirmwareConfigData;
import com.comcast.xconf.shared.controller.AbstractController;
import com.comcast.xconf.shared.service.AbstractService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(FirmwareConfigController.URL_MAPPING)
public class FirmwareConfigController extends AbstractController<FirmwareConfig> {
    public static final String URL_MAPPING = "api/firmwareconfig";

    @Autowired
    private FirmwareConfigService firmwareConfigService;

    @RequestMapping(method = RequestMethod.GET, value = "/model/{modelId}")
    @ResponseBody
    public ResponseEntity getFirmwareConfigsByModel(@PathVariable String modelId) {
        return new ResponseEntity<>(firmwareConfigService.getFirmwareConfigsByModel(modelId), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/bySupportedModels")
    public ResponseEntity getFirmwareConfigsBySupportedModels(@RequestBody Set<String> modelIds) {
        return new ResponseEntity<>(firmwareConfigService.getFirmwareConfigsBySupportedModels(modelIds), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/supportedConfigsByEnvModelRuleName/{ruleName}")
    public ResponseEntity getSupportedVersionsByEnvModelRuleName(@PathVariable String ruleName) {
        return new ResponseEntity<>(firmwareConfigService.getSupportedConfigsByEnvModelRuleName(ruleName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getSortedFirmwareVersionsIfExistOrNot")
    public ResponseEntity getSortedFirmwareVersionsIfDoesExistOrNot(@RequestBody FirmwareConfigData firmwareConfigData) {
        return new ResponseEntity<>(firmwareConfigService.getSortedFirmwareVersionsIfDoesExistOrNot(firmwareConfigData), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/byEnvModelRuleName/{ruleName}")
    public ResponseEntity getFirmwareConfigByEnvModelRuleName(@PathVariable String ruleName) {
        return new ResponseEntity<>(firmwareConfigService.getFirmwareConfigByEnvModelRuleName(ruleName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/firmwareConfigMap")
    public ResponseEntity getFirmwareConfigMap() {
        return new ResponseEntity<>(firmwareConfigService.getFirmwareConfigMap(), HttpStatus.OK);
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, value = "/{id}", params = "export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportOne(@PathVariable String id) {
        FirmwareConfig config = firmwareConfigService.getOne(id);
        HttpHeaders headers = Utils.createContentDispositionHeader(getOneEntityExportName() + config.getDescription() + firmwareConfigService.getApplicationTypeSuffix());

        return new ResponseEntity<>(Lists.newArrayList(Utils.nullifyUnwantedFields(config)), headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, params = "exportAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity exportAll() {
        HttpHeaders headers = Utils.createContentDispositionHeader(getAllEntitiesExportName() + firmwareConfigService.getApplicationTypeSuffix());

        List<FirmwareConfig> all = firmwareConfigService.getAll();
        for (FirmwareConfig config : all) {
            Utils.nullifyUnwantedFields(config);
        }
        return new ResponseEntity<>(all, headers, HttpStatus.OK);
    }

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.FIRMWARE_CONFIG.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_FIRMWARE_CONFIGS.getName();
    }

    @Override
    public AbstractService<FirmwareConfig> getService() {
        return firmwareConfigService;
    }
}

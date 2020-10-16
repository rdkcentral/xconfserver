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
package com.comcast.xconf.admin.controller.telemetry;

import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.admin.controller.ExportFileNames;
import com.comcast.xconf.admin.service.telemetry.PermanentTelemetryProfileService;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.shared.controller.ApplicationTypeAwayController;
import com.comcast.xconf.shared.service.AbstractApplicationTypeAwareService;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(PermanentProfileController.URL_MAPPING)
public class PermanentProfileController extends ApplicationTypeAwayController<PermanentTelemetryProfile> {

    public static final String URL_MAPPING = "api/telemetry/profile";

    private static final Logger logger = LoggerFactory.getLogger(PermanentProfileController.class);

    @Autowired
    private PermanentTelemetryProfileService permanentProfileService;

    @Override
    public String getOneEntityExportName() {
        return ExportFileNames.PERMANENT_PROFILE.getName();
    }

    @Override
    public String getAllEntitiesExportName() {
        return ExportFileNames.ALL_PERMANENT_PROFILES.getName();
    }

    @Override
    public AbstractApplicationTypeAwareService<PermanentTelemetryProfile> getService() {
        return permanentProfileService;
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody PermanentTelemetryProfile entity) {
        permanentProfileService.writeCreateChange(entity);
        logger.info("Successfully saved create change of TelemetryProfile: {}", JsonUtil.toJson(entity));
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    @Override
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity update(@RequestBody PermanentTelemetryProfile entity) {
        boolean addedToPending = permanentProfileService.writeUpdateChangeOrSave(entity);
        if (addedToPending) {
            logger.info("Successfully saved update change of TelemetryProfile: {}", JsonUtil.toJson(entity));
        } else {
            logger.info("Successfully updated: {}", JsonUtil.toJson(entity));
        }
        return ResponseEntity.ok(addedToPending);
    }

    @Override
    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public ResponseEntity deleteOne(@PathVariable String id) {
        PermanentTelemetryProfile entity = permanentProfileService.delete(id);
        logger.info("Successfully deleted TelemetryProfile: {}", JsonUtil.toJson(entity));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/migrate/createTelemetryId")
    public ResponseEntity createTelemetryIds() {
        List<String> migratedProfileNames = new ArrayList<>();
        for (Optional<PermanentTelemetryProfile> optionalProfile : permanentProfileService.getEntityDAO().asLoadingCache().asMap().values()) {
            try {
                if (optionalProfile.isPresent()) {
                    PermanentTelemetryProfile profile = optionalProfile.get();
                    permanentProfileService.normalizeOnSaveAfterApproving(profile);
                    permanentProfileService.getEntityDAO().setOne(profile.getId(), profile);
                    migratedProfileNames.add(profile.getName());
                }
            } catch (Exception e) {
                logger.error("TelemetryEntryIdException: ", e);
            }
        }

        return new ResponseEntity<>(migratedProfileNames, HttpStatus.OK);
    }
}

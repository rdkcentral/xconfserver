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

package com.comcast.xconf.validators.firmware;

import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.ActivationVersion;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static com.comcast.xconf.firmware.ApplicationType.STB;
import static org.mockito.Mockito.when;

public class ActivationVersionValidatorTest extends BaseQueriesControllerTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Autowired
    private ActivationVersionValidator activationVersionValidator;

    @Before
    public void initPermissionService() {
        when(firmwarePermissionService.getReadApplication()).thenReturn(STB);
        when(firmwarePermissionService.getWriteApplication()).thenReturn(STB);
        when(firmwarePermissionService.canWrite()).thenReturn(true);
    }

    @Test
    public void createWithWrongModel() {
        exceptionRule.expect(ValidationRuntimeException.class);
        exceptionRule.expectMessage("Model is required");
        ActivationVersion activationVersion = createActivationMinimumVersion(null, defaultPartnerId, "FirmwareVersion");

        activationVersionValidator.validate(activationVersion);
    }

    @Test
    public void createWithNonExistingModel() {
        exceptionRule.expect(EntityNotFoundException.class);
        exceptionRule.expectMessage("Model with id " + defaultModelId + " does not exist");
        ActivationVersion activationVersion = createActivationMinimumVersion(defaultModelId, defaultPartnerId, "someFirmwareVersion");

        activationVersionValidator.validate(activationVersion);
    }

    @Test
    public void createWithUsedDescription() throws Exception {
        exceptionRule.expect(EntityConflictException.class);
        Model model = createAndSaveModel(defaultModelId);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion1 = createAndSaveActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());
        FirmwareConfig firmwareConfig2 = createAndSaveFirmwareConfig("CHANGED_FIRMWARE_VERSION", model.getId(), FirmwareConfig.DownloadProtocol.http);
        ActivationVersion activationVersion2 = createActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig2.getFirmwareVersion());
        activationVersion2.setDescription(activationVersion1.getDescription());

        exceptionRule.expectMessage("Activation firmware versions with description " + activationVersion1.getDescription() + " already exists");
        activationVersionValidator.validateAll(activationVersion2, activationVersionService.getAll());
    }

    @Test
    public void createWithoutVersionsAndRegularExpression() throws Exception {
        exceptionRule.expect(ValidationRuntimeException.class);
        exceptionRule.expectMessage("FirmwareVersion or regular expression should be specified");
        Model model = createAndSaveModel(defaultModelId);
        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, null);
        activationVersion.setFirmwareVersions(new HashSet<String>());
        activationVersion.setRegularExpressions(new HashSet<String>());

        activationVersionValidator.validate(activationVersion);
    }

    @Test
    public void validateDuplicatesWithPartnerId() throws Exception {
        exceptionRule.expect(EntityConflictException.class);
        exceptionRule.expectMessage("ActivationVersion with the following model/partnerId already exists");

        Model model = createModel(defaultModelId);
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSaveActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());
        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), defaultPartnerId, firmwareConfig.getFirmwareVersion());

        activationVersionValidator.validateAll(activationVersion, activationVersionService.getAll());
    }

    @Test
    public void validateDuplicatesWithoutPartnerId() throws Exception {
        exceptionRule.expect(EntityConflictException.class);
        exceptionRule.expectMessage("ActivationVersion with the following model/partnerId already exists");

        Model model = createModel(defaultModelId);
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId(), FirmwareConfig.DownloadProtocol.http);

        createAndSaveActivationMinimumVersion(model.getId(), null, firmwareConfig.getFirmwareVersion());
        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), null, firmwareConfig.getFirmwareVersion());

        activationVersionValidator.validateAll(activationVersion, activationVersionService.getAll());
    }

    @Test
    public void validateDuplicatesWithEmptyStringPartnerId() throws Exception {
        exceptionRule.expect(EntityConflictException.class);
        exceptionRule.expectMessage("ActivationVersion with the following model/partnerId already exists");

        Model model = createModel(defaultModelId);
        modelDAO.setOne(model.getId(), model);
        FirmwareConfig firmwareConfig = createAndSaveFirmwareConfig(defaultFirmwareVersion, model.getId().toUpperCase(), FirmwareConfig.DownloadProtocol.http);

        createAndSaveActivationMinimumVersion(model.getId(), null, firmwareConfig.getFirmwareVersion());
        ActivationVersion activationVersion = createActivationMinimumVersion(model.getId(), null, firmwareConfig.getFirmwareVersion());

        activationVersionValidator.validateAll(activationVersion, activationVersionService.getAll());
    }
}

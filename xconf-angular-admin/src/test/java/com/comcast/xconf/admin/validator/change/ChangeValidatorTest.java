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

package com.comcast.xconf.admin.validator.change;

import com.comcast.apps.dataaccess.util.CloneUtil;
import com.comcast.xconf.admin.controller.BaseControllerTest;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.validators.change.ChangeValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ChangeValidatorTest extends BaseControllerTest {

    @Autowired
    private ChangeValidator validator;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void userIsNotAbleToCreateDuplicateChanges() {
        exceptionRule.expect(EntityExistsException.class);
        exceptionRule.expectMessage("The same change already exists");

        PermanentTelemetryProfile profile = createTelemetryProfile();
        saveTelemetryProfile(profile);
        Map<String, List<Change<PermanentTelemetryProfile>>> changes = createProfileChangesForTheSameId(profile.getId(), 1);
        Change<PermanentTelemetryProfile> change = changes.get(profile.getId()).get(0);
        Change<PermanentTelemetryProfile> newChange = CloneUtil.clone(change);
        newChange.setId(UUID.randomUUID().toString());

        validator.validateAll(newChange, changes.get(profile.getId()));
    }
}
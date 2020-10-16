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
 *  Created: 4:15 PM
 */
package com.comcast.xconf.admin.controller.migration;

import com.comcast.xconf.utils.annotation.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Use annotation for migration method to show in UI amount of new/old entities
 * @Migration(oldKey = String.class, newKey = String.class, oldEntity = FeatureLegacy.class, newEntity = Feature.class, migrationURL = "/feature")
 */
@RestController
@RequestMapping(MigrationUIController.URL_MAPPING)
@Migration
public class MigrationUIController {

    public static final String URL_MAPPING = "api/migration";

    private static final Logger log = LoggerFactory.getLogger(MigrationUIController.class);

}

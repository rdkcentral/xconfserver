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
 * Author: Igor Kostrov
 * Created: 12/8/2015
*/
package com.comcast.xconf;

import java.util.Arrays;
import java.util.List;

public interface ConfigNames {

    String ID = "id";
    String UPDATED = "updated";
    String DESCRIPTION = "description";
    String SUPPORTED_MODEL_IDS = "supportedModelIds";
    String FIRMWARE_DOWNLOAD_PROTOCOL = "firmwareDownloadProtocol";
    String FIRMWARE_FILENAME = "firmwareFilename";
    String FIRMWARE_LOCATION = "firmwareLocation";
    String FIRMWARE_VERSION = "firmwareVersion";
    String IPV6_FIRMWARE_LOCATION = "ipv6FirmwareLocation";
    String UPGRADE_DELAY = "upgradeDelay";
    String REBOOT_IMMEDIATELY = "rebootImmediately";
    String APPLICATION_TYPE = "applicationType";

    List<String> BASE_PROPERTIES = Arrays.asList(
            ID, UPDATED, DESCRIPTION, SUPPORTED_MODEL_IDS, FIRMWARE_DOWNLOAD_PROTOCOL,
            FIRMWARE_DOWNLOAD_PROTOCOL, FIRMWARE_FILENAME, FIRMWARE_LOCATION, FIRMWARE_VERSION,
            IPV6_FIRMWARE_LOCATION, UPGRADE_DELAY, REBOOT_IMMEDIATELY, APPLICATION_TYPE
    );
}

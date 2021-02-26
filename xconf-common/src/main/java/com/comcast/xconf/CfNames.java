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
 * Author: slavrenyuk
 * Created: 4/29/14
 */
package com.comcast.xconf;

public interface CfNames {
    interface LogUpload {
        String DCM_RULE = "DcmRule";
        String UPLOAD_REPOSITORY = "UploadRepository";
        String LOG_UPLOAD_SETTINGS = "LogUploadSettings2";
        String CONFIGURATION_SERVICE_URLs = "ConfigurationServiceURLs";
        String LOG_FILE = "LogFile";
        String LOG_FILE_LIST = "LogFileList";
        String INDEXED_LOG_FILES = "IndexedLogFiles";
        String LOG_FILES_GROUPS = "LogFilesGroups";
        String DEVICE_SETTINGS = "DeviceSettings2";
        String VOD_SETTINGS = "VodSettings2";
    }
    interface Firmware {
        String FIRMWARE_RULE = "FirmwareRule4";
        String FIRMWARE_CONFIG = "FirmwareConfig";
        String SINGLETON_FILTER_VALUE = "SingletonFilterValue";
        String MODEL = "Model";
    }
    interface Common {
        String ENVIRONMENT = "Environment";
        String IP_ADDRESS_GROUP = "IpAddressGroupExtended";
        String LOGS = "Logs2";
        String NS_LIST = "XconfNamedList";
        String GENERIC_NS_LIST = "GenericXconfNamedList";
    }
}

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
package com.comcast.xconf.admin.core;

import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.logupload.*;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.http.HttpHeaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final UrlValidator urlValidator;
    private static final String NUMBER_OF_ITEMS = "numberOfItems";

    static {
        List<String> schemas = new ArrayList<String>();
        for (UploadProtocol protocol : UploadProtocol.values()) {
            schemas.add(protocol.name().toLowerCase());
        }
        urlValidator = new UrlValidator(schemas.toArray(new String[schemas.size()]));
    }

    public static boolean isValidUrl(String url) {
        return urlValidator.isValid(url);
    }

    public static boolean isValidUrl(UploadProtocol protocol, String host) {
        String url = (host.contains("://") || protocol == null) ? host : (protocol.toString().toLowerCase() + "://" + host);
        return isValidUrl(url);
    }

    public static LogFile nullifyUnwantedFields(LogFile logFile) {
        logFile.setUpdated(null);
        return logFile;
    }

    public static LogFilesGroup nullifyUnwantedFields(LogFilesGroup logFilesGroup) {
        logFilesGroup.setUpdated(null);
        return logFilesGroup;
    }

    public static DCMRuleWithSettings nullifyUnwantedFields(DCMRuleWithSettings dcmRuleWithSettings) {
        DCMGenericRule dcmGenericRule = dcmRuleWithSettings.getFormula();
        if (dcmGenericRule != null) {
            dcmGenericRule.setUpdated(null);
        }

        DeviceSettings deviceSettings = dcmRuleWithSettings.getDeviceSettings();
        if (deviceSettings != null) {
            deviceSettings.setConfigurationServiceURL(null);
            deviceSettings.setUpdated(null);
            Schedule schedule = deviceSettings.getSchedule();
            if (schedule != null) {
                schedule.setStartDate(null);
                schedule.setEndDate(null);
            }
        }

        LogUploadSettings logUploadSettings = dcmRuleWithSettings.getLogUploadSettings();
        if (logUploadSettings != null) {
            logUploadSettings.setModeToGetLogFiles(null);
            logUploadSettings.setFromDateTime(null);
            logUploadSettings.setToDateTime(null);
            logUploadSettings.setLogFileIds(null);
            logUploadSettings.setLogFilesGroupId(null);
            logUploadSettings.setActiveDateTimeRange(null);
            logUploadSettings.setUpdated(null);
            Schedule schedule = logUploadSettings.getSchedule();
            if (schedule != null) {
                schedule.setStartDate(null);
                schedule.setEndDate(null);
            }
        }

        VodSettings vodSettings = dcmRuleWithSettings.getVodSettings();
        if (vodSettings != null) {
            vodSettings.setUpdated(null);
        }

        return dcmRuleWithSettings;
    }

    public static FirmwareConfig nullifyUnwantedFields(FirmwareConfig config) {
        if (config != null) {
            config.setFirmwareDownloadProtocol(null);
            config.setRebootImmediately(null);
        }
        return config;
    }

    public static HttpHeaders createContentDispositionHeader(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + org.apache.commons.lang.StringEscapeUtils.escapeXml(fileName + ".json"));
        return headers;
    }

    public static <T> HttpHeaders createNumberOfItemsHttpHeaders(List<T> entities) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(NUMBER_OF_ITEMS, Integer.toString(entities.size()));
        return headers;
    }

    public static <T> HttpHeaders createNumberOfItemsHttpHeaders(Integer size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(NUMBER_OF_ITEMS, Integer.toString(size));
        return headers;
    }

}

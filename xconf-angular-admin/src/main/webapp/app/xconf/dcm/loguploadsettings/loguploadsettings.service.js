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
 * Author: Stanislav Menshykov
 * Created: 20.10.15  17:47
 */
(function() {
    'use strict';

    angular
        .module('app.loguploadsettings')
        .factory('logUploadSettingsService', service);

    service.$inject=['$http'];

    function service($http) {
        var urlMapping = 'api/dcm/logUploadSettings/';

        return {
            getLogUploadSettings: getLogUploadSettings,
            getAllLogUploadSettings: getAllLogUploadSettings,
            createLogUploadSettings: createLogUploadSettings,
            updateLogUploadSettings: updateLogUploadSettings,
            deleteLogUploadSettings: deleteLogUploadSettings,
            getLogUploadSettingsNames: getLogUploadSettingsNames,
            getSizeOfLogUploadSettings: getSizeOfLogUploadSettings,
            getLogUploadSettingsPage: getLogUploadSettingsPage
        };

        function getLogUploadSettings(id) {
            return $http.get(urlMapping + id);
        }

        function getAllLogUploadSettings() {
            return $http.get(urlMapping);
        }

        function getLogUploadSettingsPage(pageNumber, pageSize, searchParam) {
            return $http.post(urlMapping + 'filtered' + '?pageNumber=' + pageNumber + '&pageSize=' + pageSize, searchParam);
        }

        function createLogUploadSettings(logUploadSettings) {
            return $http.post(urlMapping, logUploadSettings);
        }

        function updateLogUploadSettings(logUploadSettings) {
            return $http.put(urlMapping, logUploadSettings);
        }

        function deleteLogUploadSettings(id) {
            return $http.delete(urlMapping + id);
        }

        function getLogUploadSettingsNames() {
            return $http.get(urlMapping + 'names');
        }

        function getSizeOfLogUploadSettings() {
            return $http.get(urlMapping + "size/");
        }
    }
})();

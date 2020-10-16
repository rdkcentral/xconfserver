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
/**
 * Created by izluben on 17.10.15.
 */
(function() {
    'use strict';

    angular
        .module('app.devicesettings')
        .factory('deviceSettingsService', service);

    service.$inject=['$http', '$q'];

    function service($http, $q) {
        var urlMapping = 'api/dcm/deviceSettings/';

        return {
            getAllDeviceSettings: getAllDeviceSettings,
            getDeviceSettings: getDeviceSettings,
            createDeviceSettings: createDeviceSettings,
            updateDeviceSettings: updateDeviceSettings,
            deleteDeviceSettings: deleteDeviceSettings,
            getDeviceSettingsNames: getDeviceSettingsNames,
            convertDateToString: convertDateToString,
            convertStringToDate: convertStringToDate,
            getSizeOfDeviceSettings: getSizeOfDeviceSettings,
            getDeviceSettingsPage: getDeviceSettingsPage
        };

        function getAllDeviceSettings() {
            return $http.get(urlMapping);
        }

        function getDeviceSettingsPage(pageNumber, pageSize, searchParam) {
            return $http.post(urlMapping + 'filtered' + '?pageNumber=' + pageNumber + '&pageSize=' + pageSize, searchParam);
        }

        function getDeviceSettings(id) {
            return $http.get(urlMapping + id);
        }

        function updateDeviceSettings(deviceSettings) {
            return $http.put(urlMapping, deviceSettings);
        }

        function createDeviceSettings(deviceSettings) {
            return $http.post(urlMapping, deviceSettings);
        }

        function deleteDeviceSettings(id) {
            return $http.delete(urlMapping + id);
        }

        function getDeviceSettingsNames() {
            return $http.get(urlMapping + 'names');
        }

        function convertDateToString(date) {
            return date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + date.getDate() + ' '
                + date.getHours() + ':' + date.getMinutes() + ':' + date.getSeconds();
        }

        function convertStringToDate(string) {
            var dateAndTime = string.split(' ');
            var date = dateAndTime[0].split('-');
            var time = dateAndTime[1].split(':');

            return new Date(date[0], date[1] - 1, date[2], time[0], time[1], time[2]);
        }

        function getSizeOfDeviceSettings() {
            return $http.get(urlMapping + "size/");
        }
    }
})();

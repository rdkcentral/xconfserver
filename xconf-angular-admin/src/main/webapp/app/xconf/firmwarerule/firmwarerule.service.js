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
(function() {
    'use strict';

    angular
        .module('app.firmwarerule')
        .factory('firmwareRuleService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var API_URL = 'api/firmwarerule/';

        return {
            getFirmwareRule: getFirmwareRule,
            getAllFirmwareRules: getAllFirmwareRules,
            getFirmwareRulesByActionType: getFirmwareRulesByActionType,
            createFirmwareRule: createFirmwareRule,
            updateFirmwareRule: updateFirmwareRule,
            deleteFirmwareRule: deleteFirmwareRule,
            exportFirmwareRule: exportFirmwareRule,
            exportAllFirmwareRules: exportAllFirmwareRules,
            exportAllFirmwareRulesByType: exportAllFirmwareRulesByType,
            getFirmwareRuleNamesByTemplate: getFirmwareRuleNamesByTemplate,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            getMacRulesNames: getMacRulesNames
        };

        function getFirmwareRule(id) {
            return $http.get(API_URL + id);
        }

        function getAllFirmwareRules() {
            return $http.get(API_URL);
        }

        function createFirmwareRule(firmwareRule) {
            return $http.post(API_URL, firmwareRule);
        }

        function updateFirmwareRule(firmwareRule) {
            return $http.put(API_URL, firmwareRule);
        }

        function deleteFirmwareRule(id) {
            return $http.delete(API_URL + id);
        }

        function getFirmwareRulesByActionType(pageNumber, pageSize, searchParam) {
            var url = API_URL + 'filtered?pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.post(url, searchParam);
        }

        function getFirmwareRuleNamesByTemplate(templateId) {
            return $http.get(API_URL + 'byTemplate/' + templateId + '/names');
        }

        function exportFirmwareRule(id) {
            window.open(API_URL + id + '/?export');
        }

        function exportAllFirmwareRulesByType(type) {
            window.open(API_URL + 'export/byType?exportAll&type=' + type);
        }

        function exportAllFirmwareRules() {
            window.open(API_URL + 'export/allTypes?exportAll');
        }

        function updateSyncEntities(firmwareRules) {
            var requests = utilsService.generateRequestList(firmwareRules, {url: API_URL + 'entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(firmwareRules) {
            var requests = utilsService.generateRequestList(firmwareRules, {url: API_URL + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function getMacRulesNames() {
            return $http.get(API_URL + 'MAC_RULE/names');
        }
    }
})();

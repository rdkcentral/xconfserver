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
        .module('app.firmwareruletemplate')
        .factory('firmwareRuleTemplateService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var API_URL = 'api/firmwareruletemplate/';

        return {
            getFirmwareRuleTemplate: getFirmwareRuleTemplate,
            getAllFirmwareRuleTemplates: getAllFirmwareRuleTemplates,
            getFirmwareRuleTemplatesByActionType: getFirmwareRuleTemplatesByActionType,
            getFirmwareRuleTemplatesByActionTypePage: getFirmwareRuleTemplatesByActionTypePage,
            getFirmwareRuleTemplateIdsByActionType: getFirmwareRuleTemplateIdsByActionType,
            createFirmwareRuleTemplate: createFirmwareRuleTemplate,
            updateFirmwareRuleTemplate: updateFirmwareRuleTemplate,
            importFirmwareRuleTemplates: importFirmwareRuleTemplates,
            deleteFirmwareRuleTemplate: deleteFirmwareRuleTemplate,
            exportFirmwareRuleTemplate: exportFirmwareRuleTemplate,
            exportAllFirmwareRuleTemplates: exportAllFirmwareRuleTemplates,
            exportAllFirmwareRuleTemplatesByType: exportAllFirmwareRuleTemplatesByType,
            changePriorities: changePriorities,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            getByTypeAndEditableOption: getByTypeAndEditableOption
        };

        function getFirmwareRuleTemplate(id) {
            return $http.get(API_URL + id);
        }

        function getAllFirmwareRuleTemplates() {
            return $http.get(API_URL + 'all');
        }

        function createFirmwareRuleTemplate(firmwareRuleTemplate) {
            return $http.post(API_URL, firmwareRuleTemplate);
        }

        function updateFirmwareRuleTemplate(firmwareRuleTemplate) {
            return $http.put(API_URL, firmwareRuleTemplate);
        }

        function importFirmwareRuleTemplates(firmwareRuleTemplates) {
            return $http.post(API_URL + 'import/', firmwareRuleTemplates);
        }

        function deleteFirmwareRuleTemplate(id) {
            return $http.delete(API_URL + id);
        }

        function getFirmwareRuleTemplatesByActionTypePage(pageNumber, pageSize, searchContext) {
            var url = API_URL + 'filtered?pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.post(url, searchContext);
        }

        function getFirmwareRuleTemplatesByActionType(type) {
            var url = API_URL + 'all/' + type;
            return $http.get(url);
        }

        function getFirmwareRuleTemplateIdsByActionType(actionType) {
            var url = API_URL + "ids";
            if (actionType) {
                url += "?type=" + actionType;
            }
            return $http.get(url);
        }

        function exportFirmwareRuleTemplate(id) {
            window.open(API_URL + id + '/?export');
        }

        function exportAllFirmwareRuleTemplates() {
            window.open(API_URL + '/?export');
        }

        function exportAllFirmwareRuleTemplatesByType(type) {
            window.open(API_URL + '/export/?type=' + type);
        }

        function changePriorities(id, newPriority) {
            return $http.post(API_URL + id + '/priority/' + newPriority);
        }

        function updateSyncEntities(firmwareRuleTemplates) {
            var requests = utilsService.generateRequestList(firmwareRuleTemplates, {url: API_URL + 'entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(firmwareRuleTemplates) {
            var requests = utilsService.generateRequestList(firmwareRuleTemplates, {url: API_URL + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function getByTypeAndEditableOption(type, isEditable) {
            return $http.get(API_URL + type + '/' + isEditable);
        }
    }
})();
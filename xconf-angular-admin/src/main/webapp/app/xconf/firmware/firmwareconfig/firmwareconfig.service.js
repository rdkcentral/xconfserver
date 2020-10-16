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
        .module('app.firmwareconfig', ['ngResource'])
        .factory('firmwareConfigService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var URL = 'api/firmwareconfig';

        return {
            getPage: getPage,
            getAll: getAll,
            getById: getById,
            getByModelId: getByModelId,
            getBySupportedModels: getBySupportedModels,
            getByEnvModelRuleName: getByEnvModelRuleName,
            update: update,
            create: create,
            deleteById: deleteById,
            getSortedFirmwareVersionsIfDoesExistOrNot: getSortedFirmwareVersionsIfDoesExistOrNot,
            exportById: exportById,
            exportAll: exportAll,
            searchByContext: searchByContext,
            getSupportedConfigsByEnvModelRuleName: getSupportedConfigsByEnvModelRuleName,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            getFirmwareConfigMap: getFirmwareConfigMap,
            buildFirmwareConfigData: buildFirmwareConfigData
        };

        function getPage(pageSize, pageNumber) {
            return $http.get(URL + '/page?pageSize=' + pageSize + '&pageNumber=' + pageNumber);
        }

        function getAll() {
            return $http.get(URL);
        }

        function getById(id) {
            return $http.get(URL + '/' + id);
        }

        function getByModelId(modelId) {
            return $http.get(URL + '/model/' + modelId);
        }

        function getBySupportedModels(modelIds) {
            return $http.post(URL + '/bySupportedModels', modelIds);
        }

        function getSupportedConfigsByEnvModelRuleName(envModelRuleName) {
            return $http.get(URL + '/supportedConfigsByEnvModelRuleName/' + envModelRuleName);
        }

        function getByEnvModelRuleName(envModelRuleName) {
            return $http.get(URL + '/byEnvModelRuleName/' + envModelRuleName);
        }

        function update(firmwareConfig) {
            return $http.put(URL, firmwareConfig);
        }

        function create(firmwareConfig) {
            return $http.post(URL, firmwareConfig);
        }

        function deleteById(id) {
            return $http.delete(URL + '/' + id);
        }

        function getSortedFirmwareVersionsIfDoesExistOrNot(models, firmwareVersions) {
            return $http.post(URL + '/getSortedFirmwareVersionsIfExistOrNot', buildFirmwareConfigData(models, firmwareVersions));
        }

        function exportById(id) {
            window.open(URL + '/' + id + '?export');
        }

        function exportAll() {
            window.open(URL + '?exportAll');
        }

        function searchByContext(pageSize, pageNumber, searchContext) {
            return $http.post(URL + '/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchContext);
        }

        function updateSyncEntities(firmwareConfigs) {
            var requests = utilsService.generateRequestList(firmwareConfigs, {url: URL + '/entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(firmwareConfigs) {
            var requests = utilsService.generateRequestList(firmwareConfigs, {url: URL + '/entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function getFirmwareConfigMap() {
            return $http.get(URL + '/firmwareConfigMap');
        }

        function buildFirmwareConfigData(models, firmwareVersions) {
            return {
                models: models,
                firmwareVersions: firmwareVersions
            }
        }
    }
})();
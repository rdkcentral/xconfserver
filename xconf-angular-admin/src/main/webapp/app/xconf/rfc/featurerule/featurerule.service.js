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
    angular
        .module('app.featurerule')
        .factory('featureRuleService', service);

    service.$inject = ['$http', 'syncHttpService', 'utilsService'];

    function service($http, syncHttpService, utilsService) {
        var URL = "api/rfc/featurerule/";

        return {
            getAll: getAll,
            getFeatureRules: getFeatureRules,
            getFeatureRule: getFeatureRule,
            createFeatureRule: createFeatureRule,
            updateFeatureRule: updateFeatureRule,
            deleteFeatureRule: deleteFeatureRule,
            exportAllFeatureRules: exportAllFeatureRules,
            exportFeatureRule: exportFeatureRule,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            changePriorities: changePriorities,
            getFeatureRulesSize: getFeatureRulesSize,
            getAllowedNumberOfFeatures: getAllowedNumberOfFeatures
        };

        function getAll() {
            return $http.get(URL);
        }

        function getFeatureRules(pageNumber, pageSize, searchParam) {
            var url = URL + 'filtered?pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.post(url, searchParam);
        }

        function getFeatureRule(id) {
            return $http.get(URL + id);
        }

        function createFeatureRule(feature) {
            return $http.post(URL, feature);
        }

        function updateFeatureRule(feature) {
            return $http.put(URL, feature);
        }

        function deleteFeatureRule(id) {
            return $http.delete(URL + id);
        }

        function exportAllFeatureRules() {
            window.open(URL + "?export");
        }

        function exportFeatureRule(id) {
            window.open(URL + id + "?export");
        }

        function updateSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: URL + 'entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: URL + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function changePriorities(id, priority) {
            return $http.post(URL + id + '/priority/' + priority);
        }

        function getFeatureRulesSize() {
            return $http.get(URL + 'size');
        }

        function getAllowedNumberOfFeatures() {
            return $http.get(URL + 'allowedNumberOfFeatures');
        }
    }
})();
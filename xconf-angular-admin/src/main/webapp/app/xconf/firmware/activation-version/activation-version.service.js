/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

(function() {
    'use strict';

    angular
        .module('app.activation-version', [])
        .factory('activationVersionService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var URL = 'api/activationMinimumVersion';

        return {
            getAll: getAll,
            getById: getById,
            update: update,
            create: create,
            deleteById: deleteById,
            exportOne: exportOne,
            exportAll: exportAll,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            getPage: getPage
        };

        function getPage(pageSize, pageNumber, context) {
            return $http.post(URL + '/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, context);
        }

        function getAll() {
            return $http.get(URL);
        }

        function getById(id) {
            return $http.get(URL + '/' + id);
        }

        function update(activationVersion) {
            return $http.put(URL, activationVersion);
        }

        function create(activationVersion) {
            return $http.post(URL, activationVersion);
        }

        function deleteById(id) {
            return $http.delete(URL + '/' + id);
        }

        function exportOne(id) {
            window.open(URL + '/' + id + '?export');
        }

        function exportAll() {
            window.open(URL + '?exportAll');
        }

        function updateSyncEntities(activationVersions) {
            var requests = utilsService.generateRequestList(activationVersions, {url: URL + '/entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(activationVersions) {
            var requests = utilsService.generateRequestList(activationVersions, {url: URL + '/entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }
    }
})();
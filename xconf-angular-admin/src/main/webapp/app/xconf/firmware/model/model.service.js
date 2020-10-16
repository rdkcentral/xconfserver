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
        .module('app.model', [])
        .factory('modelService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {
        var URL = 'api/model';

        return {
            getPage: getPage,
            getAll: getAll,
            getById: getById,
            update: update,
            create: create,
            deleteById: deleteById,
            exportOne: exportOne,
            exportAll: exportAll,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities
        };

        function getAll() {
            return $http.get(URL);
        }

        function getById(id) {
            return $http.get(URL + '/' + id);
        }

        function update(model) {
            return $http.put(URL, model);
        }

        function create(model) {
            return $http.post(URL, model);
        }

        function deleteById(id) {
            return $http.delete(URL + '/' + id);
        }

        function exportOne(id) {
            window.open(URL + '/' + id + '?export');
        }

        function exportAll() {
            window.open(URL + '?export');
        }

        function getPage(pageNumber, pageSize, searchParam) {
            return $http.post(URL + '/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchParam);
        }

        function updateSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: URL + '/entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: URL + '/entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }
    }
})();
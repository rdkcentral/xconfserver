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
        .module('app.formula')
        .factory('formulaService', service);

    service.$inject = ['utilsService', '$http', 'syncHttpService']

    function service(utilsService, $http, syncHttpService) {
        var urlMapping = 'api/dcm/formula/';

        return {
            getAll: getAll,
            getPage: getPage,
            getSizeOfFormulas: getSizeOfFormulas,
            getById: getById,
            getUsedNames: getUsedName,
            create: create,
            update: update,
            deleteFormula: deleteFormula,
            importFormula: importFormula,
            getSettingsAvailability: getSettigsAvailability,
            changePriorities: changePriorities,
            getFormulasAvailability: getFormulasAvailability,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            exportFormula: exportFormula,
            exportAllFormulas: exportAllFormulas
        };

        function getAll() {
            return $http.get(urlMapping);
        }

        function getPage(pageSize, pageNumber, searchContext) {
            return $http.post(urlMapping + 'filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchContext);
        }

        function getSizeOfFormulas() {
            return $http.get(urlMapping + 'size');
        }

        function getById(id) {
            return $http.get(urlMapping + id);
        }

        function getUsedName() {
            return $http.get(urlMapping + 'names');
        }

        function create(formula) {
            return $http.post(urlMapping, formula);
        }

        function update(formula) {
            return $http.put(urlMapping, formula);
        }

        function deleteFormula(id) {
            return $http.delete(urlMapping + id);
        }

        function importFormula(formulaWithSettings, overwrite) {
            if (overwrite === false || overwrite === true) {
                return $http.post(urlMapping + '/import/' + overwrite, formulaWithSettings);
            }
            return $http.post(urlMapping + '/import', formulaWithSettings);
        }



        function getSettigsAvailability(settings) {
            return $http.post(urlMapping + 'settingsAvailability', settings);
        }

        function changePriorities(id, priority) {
            return $http.post(urlMapping + id + '/priority/' + priority);
        }

        function getFormulasAvailability(settings) {
            var settingsIds = _.pluck(settings, 'id');
            return $http.post(urlMapping + 'formulasAvailability', settingsIds);
        }

        function updateSyncEntities(formulas) {
            var requests = utilsService.generateRequestList(formulas, {url: urlMapping + 'list', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(formulas) {
            var requests = utilsService.generateRequestList(formulas, {url: urlMapping + 'list', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function exportFormula(id) {
            window.open(urlMapping + id + '/?export');
        }

        function exportAllFormulas() {
            window.open(urlMapping + '?export');
        }
    }
})();
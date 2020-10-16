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
 *  Author: mdolina
 *  Created: 2/1/16 4:51 PM
 */

(function() {
    'use strict';

    angular
        .module('app.roundrobinfilter')
        .controller('RoundRobinFilterImportController', controller);

    controller.$inject=['$rootScope', '$log', '$state', 'alertsService', 'importService', 'roundRobinFilterService'];

    function controller($rootScope, $log, $state, alertsService, importService, roundRobinFilterService) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importFilter = importFilter;
        vm.filter = null;

        async function retrieveFile(fileName) {
            vm.filter = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.filter = importService.getEntitiesFromFile(file);
            } catch(e) {
                alertsService.showError({message: e});
            }
        }

        function importFilter(filter) {
            if (!filter.applicationType) {
                filter.applicationType = $rootScope.applicationType;
            }
            roundRobinFilterService.saveFilter(filter).then(function () {
                alertsService.successfullySaved('Download Location Filter');
                $state.go('roundrobinfilter');
            }, function (reason) {
                alertsService.showError({title: 'Error', message: reason.data.message});
            });
        }
    }
})();
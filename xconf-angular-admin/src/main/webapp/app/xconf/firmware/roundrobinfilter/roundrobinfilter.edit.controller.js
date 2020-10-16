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
 *  Created: 11/9/15 6:34 PM
 */

(function() {
    'use strict';

    angular
        .module('app.roundrobinfilter')
        .controller('RoundRobinFilterEditController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'roundRobinFilterService', 'alertsService', 'utilsService', 'roundRobinFilterValidationService', '$state'];

    function controller($rootScope, $scope, $controller, roundRobinFilterService, alertsService, utilsService, roundRobinFilterValidationService, $state) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'roundrobinfilter',
            stateParameters: null
        }));

        vm.filter = null;
        vm.validator = roundRobinFilterValidationService;

        vm.addLocation = addLocation;
        vm.removeLocation = removeLocation;
        vm.hasValue = hasValue;
        vm.save = save;

        init();

        $scope.$on('applicationType:changed', function(event, data) {
            init();
        });

        function init() {
            roundRobinFilterService.getFilter($rootScope.applicationType).then(function(filterResp) {
                vm.filter = filterResp.data;
            }, alertsService.errorHandler);

            vm.validator.cleanErrors();
        }

        function addLocation(locations) {
            var newLocation = {
                locationIp: '',
                percentage: ''
            };

            locations.push(newLocation);
        }

        function removeLocation(locations, item) {
            utilsService.removeItemFromArray(locations, item);
        }

        function save() {
            if (!vm.filter.applicationType) {
                vm.filter.applicationType = $rootScope.applicationType;
            }
            roundRobinFilterService.saveFilter(vm.filter).then(function(resp) {
                alertsService.successfullySaved('Download Location Filter');
                $state.go('roundrobinfilter');
            }, alertsService.errorHandler);
        }

        function hasValue(error) {
            var result = angular.isDefined(error) && !$.isEmptyObject(error);
            return result;
        }
    }
})();
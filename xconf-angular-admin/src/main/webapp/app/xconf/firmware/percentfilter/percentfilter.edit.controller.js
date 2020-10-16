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
        .module('app.percentfilter')
        .controller('PercentFilterEditController', controller);

    controller.$inject = ['$state', '$scope', '$controller', 'percentFilterService', 'namespacedListService', 'alertsService', 'percentFilterValidationService', 'utilsService', 'NAMESPACED_LIST_TYPE'];

    function controller($state, $scope, $controller, percentFilterService, namespacedListService, alertsService, percentFilterValidationService, utilsService, NAMESPACED_LIST_TYPE) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'percentfilter',
            stateParameters: null
        }));

        vm.filter = null;
        vm.ipListIds = [];
        vm.validator = percentFilterValidationService;
        vm.hasValue = utilsService.hasValue;

        vm.save = save;

        init();

        function init() {
            vm.validator.cleanErrors();
            percentFilterService.getFilter().then(function(percentResp) {
                vm.filter = percentResp.data;
            }, function(error) {
                errorHandler(error.data.message);
            });

            namespacedListService.getNamespacedListIdsByType(NAMESPACED_LIST_TYPE.IP_LIST).then(function(resp) {
                vm.ipListIds = resp.data;
            }, function(error) {
                errorHandler(error.data.message);
            });
        }

        function save(filter) {
            if (vm.validator.validatePercentFilter(filter)) {
                percentFilterService.saveFilter(filter).then(function (resp) {
                    alertsService.successfullySaved('PercentFilter');
                    $state.go('percentfilter');
                }, alertsService.errorHandler);
            }
        }

        function errorHandler(errorMessage) {
            alertsService.showError({title: 'Error', message: errorMessage});
        }
    }
})();
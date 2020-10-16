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
        .module('app.firmwareTestPage')
        .controller('FirmwareTestPageController', controller);

    controller.$inject = ['$rootScope', 'firmwareTestPageService', 'alertsService', 'utilsService', 'FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE', 'CAPABILITIES', 'testPageUtils'];

    function controller($rootScope, firmwareTestPageService, alertsService, utilsService, FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE, CAPABILITIES, testPageUtils) {
        var vm = this;

        vm.parameters = [{key: '', value: ''}];
        vm.autoCompleteValues = FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.capabilities = CAPABILITIES;
        vm.quickAddValues = [];
        vm.rebootDecoupled = false;
        vm.rcdl = false;
        vm.supportsFullHttpUrl = false;
        vm.result = null;
        vm.context = null;

        vm.matchRules = matchRules;
        vm.printFilterName = printFilterName;

        init();

        function init() {
            angular.forEach(CAPABILITIES, function(capability) {
                vm.quickAddValues.push({display: capability, key: 'capabilities', value: capability});
            });
        }

        function printFilterName(filter) {
            return filter.name ? filter.name : filter.id;
        }

        function matchRules() {
            if (testPageUtils.validateInput(vm.parameters)) {
                var params = testPageUtils.getParametersAsString(vm.parameters);
                if ($rootScope.applicationType) {
                   params = setApplicationType(params, $rootScope.applicationType);
                }
                firmwareTestPageService.getMatchedRules(params).then(function (resp) {
                    vm.result = resp.data.result;
                    vm.context = resp.data.context;
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function setApplicationType(params, applicationType) {
            if (params !== '') {
                params += 'applicationType=' + applicationType;
            } else {
                params += '?applicationType=' + applicationType;
            }
            return params;
        }
    }
})();
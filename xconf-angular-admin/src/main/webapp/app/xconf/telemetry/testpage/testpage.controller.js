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
        .module('app.telemetryTestPage')
        .controller('TelemetryTestPageController', controller);

    controller.$inject = ['telemetryTestPageService', 'alertsService', 'permanentProfileService', 'LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', 'testPageUtils'];

    function controller(telemetryTestPageService, alertsService, permanentProfileService, LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE, testPageUtils) {
        var vm = this;
        vm.matchedRules = null;
        vm.profiles = [];
        vm.parameters = [{key: '', value: ''}];
        vm.autoCompleteValues = LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.context = null;

        vm.matchRules = matchRules;

        init();

        function init() {
            permanentProfileService.getAll()
                .then(function(resp) {
                    vm.profiles = resp.data;
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
        }

        function matchRules() {
            if (testPageUtils.validateInput(vm.parameters)) {
                var objParams = testPageUtils.getParametersAsObject(vm.parameters);
                telemetryTestPageService.getMatchedRules(objParams).then(function (resp) {
                    vm.matchedRules = resp.data.result;
                    vm.context = resp.data.context;
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }
    }
})();
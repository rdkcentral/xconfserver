/*******************************************************************************
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
        .module('app.telemetrytwotargetingrule')
        .controller('TelemetryTwoTargetingRuleViewController', controller);

    controller.$inject=['$rootScope', '$scope', '$controller', '$state', '$stateParams', 'telemetryRule', 'alertsService', 'ruleHelperService', 'telemetryTwoTargetingRuleService', 'telemetryTwoProfileService', '$uibModalInstance'];

    function controller($rootScope, $scope, $controller, $state, $stateParams, telemetryRule, alertsService, ruleHelperService, telemetryTwoTargetingRuleService, telemetryTwoProfileService, $uibModalInstance) {
        var vm = this;

        vm.telemetryRule = telemetryRule;
        vm.ruleProfiles = [];

        vm.dismiss = dismiss;

        init();

        function init() {
            if (vm.telemetryRule && vm.telemetryRule.boundTelemetryIds) {
                telemetryTwoProfileService.getTelemetryTwoProfilesByIdList(vm.telemetryRule.boundTelemetryIds).then(function(resp) {
                    vm.ruleProfiles = resp.data;
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function dismiss() {
            $uibModalInstance.dismiss('close');
        }

    }
})();
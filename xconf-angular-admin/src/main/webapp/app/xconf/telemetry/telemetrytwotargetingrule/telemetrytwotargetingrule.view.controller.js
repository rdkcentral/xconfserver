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
        .module('app.telemetrytwotargetingrule')
        .controller('TelemetryTwoTargetingRuleViewController', controller);

    controller.$inject=['$rootScope', '$scope', '$controller', '$state', '$stateParams', 'alertsService', 'ruleHelperService', 'telemetryTwoTargetingRuleService', 'telemetryTwoProfileService', 'namespacedListService', 'TARGETING_RULE_OPERATION_ARRAY', 'LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', 'FREE_ARG_NAME', 'ruleValidationService'];

    function controller($rootScope, $scope, $controller, $state, $stateParams, alertsService, ruleHelperService, telemetryTwoTargetingRuleService, telemetryTwoProfileService, namespacedListService, TARGETING_RULE_OPERATION_ARRAY, LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE, FREE_ARG_NAME, ruleValidationService) {
        var vm = this;

        vm.targetingRule = {
            "rule": {
                applicationType: $rootScope.applicationType,
                name:'',
                boundTelemetryIds: ''
            }
        };
        vm.namespacedListIds = [];
        vm.profiles = [];
        vm.namespacedListData = ruleHelperService.buildNamespacedListData();
        vm.operations = {general: TARGETING_RULE_OPERATION_ARRAY};
        vm.freeArgAutocompleteValues = LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.validationFunction = ruleValidationService.validate;
        vm.percentFreeArgName = FREE_ARG_NAME.ESTB_MAC_ADDRESS;
        vm.representation = ruleHelperService.buildRepresentation();
        init();

        function init() {
            namespacedListService.getNamespacedListIds().then(function(resp) {
                vm.namespacedListIds = resp;
            }, alertsService.errorHandler);

            if ($stateParams.ruleId) {
                telemetryTwoTargetingRuleService.getById($stateParams.ruleId).then(function(resp) {
                    vm.targetingRule.rule = resp.data;
                    getTelemetryTwoProfilesByIdList(vm.targetingRule.rule.boundTelemetryIds);
                }, alertsService.errorHandler);
            }

            $scope.$root.$on("rule::remove", function(e, obj) {
                var rule = vm.targetingRule.rule;
                if (ruleHelperService.isCompound(rule)) {
                    var compoundParts = rule.compoundParts || [];
                    for (var i = 0; i < compoundParts.length; i++) {
                        var currentRule = compoundParts[i];
                        if (currentRule === obj.rule) {
                            if (i === 0) {
                                $.extend(rule, ruleHelperService.createEmptyRule());
                            } else {
                                compoundParts.splice(i, 1);
                                if (compoundParts.length === 1) {
                                    var clonedFeatureRule = angular.copy(rule);
                                    $.extend(clonedFeatureRule, compoundParts[0]);
                                    vm.targetingRule.rule = clonedFeatureRule;
                                }
                            }
                            if (rule.compoundParts.length === 0 && !rule.condition) {
                                vm.isValidCondition = false;
                            }
                            return;
                        }
                    }
                } else {
                    if (obj.rule === rule) {
                        $.extend(rule, ruleHelperService.createEmptyRule());
                        vm.isValidCondition = false;
                    }
                }
            });

        }
        function getTelemetryTwoProfilesByIdList(boundTelemetryIds) {
            telemetryTwoProfileService.getTelemetryTwoProfilesByIdList(boundTelemetryIds)
                .then(function(resp) {
                    vm.profiles = resp.data;
            }, alertsService.errorHandler);
        }

    }
})();
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
        .module('app.firmwareruletemplate')
        .controller('FirmwareRuleTemplateEditController', controller);

    controller.$inject=['$rootScope', '$scope', '$state', 'alertsService', '$controller', '$stateParams', 'firmwareRuleTemplateService', 'firmwareConfigService', 'APPLICABLE_ACTION_TYPE', 'utilsService', 'FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE', 'TIME_FREE_ARG_OPERATION_ARRAY', 'ruleHelperService', 'FREE_ARG_NAME', 'ruleValidationService'];

    function controller($rootScope, $scope, $state, alertsService, $controller, $stateParams, firmwareRuleTemplateService, firmwareConfigService, APPLICABLE_ACTION_TYPE, utilsService, FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE, TIME_FREE_ARG_OPERATION_ARRAY, ruleHelperService, FREE_ARG_NAME, ruleValidationService) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'firmwareruletemplates',
            stateParameters: null
        }));

        // variables
        vm.isValidCondition = true;
        vm.namespacedListData = ruleHelperService.buildNamespacedListData();
        vm.data = {
            "name": "",
            "rule": {},
            "applicableAction" : null
        };
        vm.disableValidation = true;
        vm.isNewEntity = $state.current.name === 'firmwareruletemplate-add';
        vm.editedData = [];
        vm.freeArgAutocompleteValues = FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.blockingFilterTemplates = [];
        vm.operations = {time: TIME_FREE_ARG_OPERATION_ARRAY};
        vm.selectedActionType = null;
        vm.percentFreeArgName = FREE_ARG_NAME.ESTB_MAC;

        // rule action
        vm.ruleAction = {};
        vm.ruleAction.data = {
            "type": APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.class,
            "actionType": APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name,
            "configId": null
        };
        vm.fiwmareConfigs = [];

        // define properties action
        vm.definePropertiesAction = {};
        vm.definePropertiesAction.data = {
            "type": APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.class,
            "actionType": APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name,
            "properties": {

            },
            "byPassFilters": []
        };

        // blocking filter action
        vm.blockingFilterAction = {};
        vm.blockingFilterAction.data = {
            "type": APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.class,
            "actionType": APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name
        };

        // constants
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;

        // functions
        vm.saveFirmwareRuleTemplate = saveFirmwareRuleTemplate;
        vm.representation = ruleHelperService.buildRepresentation();
        vm.validationFunction = ruleValidationService.validate;

        init();

        function init() {
            if (vm.isNewEntity) {
                vm.selectedActionType = APPLICABLE_ACTION_TYPE.getActionTypeByName($stateParams.actionType);
                switch(vm.selectedActionType) {
                    case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE:
                        vm.data.applicableAction = vm.ruleAction.data;
                        getFirmwareConfigsAndBuildItBySupportedModels();
                        break;
                    case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE:
                        vm.data.applicableAction = vm.definePropertiesAction.data;
                        getBlockingFilterTemplates();
                        break;
                    case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE:
                        vm.data.applicableAction = vm.blockingFilterAction.data;
                        break;
                }
            } else {
                getFirmwareRuleTemplate($stateParams.id);
            }

            setAvailablePriorities($stateParams.templatesSize);
            if (vm.isNewEntity) {
                vm.data.priority = vm.availablePriorities[vm.availablePriorities.length - 1];
            }

            _.each(['rule::created', 'rule::updated'], function(eventType) {
                $scope.$root.$on(eventType, function(e, obj) {
                    console.log("Event: " + eventType);
                    vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(obj.data, vm.allFirmwareConfigs);
                    vm.representation.firmwareVersion = [];
                    _.each(vm.firmwareConfigs, function(firmwareConfig) {vm.representation.firmwareVersion.push(firmwareConfig.firmwareVersion)});
                });
            });

            $scope.$root.$on("rule::remove", function(e, obj) {
                var watchResult = ruleHelperService.watchRuleRemoveOperation(vm.isValidCondition, vm.data.rule, obj);
                vm.data.rule = watchResult.rule;
                vm.isValidCondition = watchResult.isValidCondition;
                vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(vm.data.rule, vm.allFirmwareConfigs);
                vm.representation.firmwareVersion = [];
                _.each(vm.firmwareConfigs, function(firmwareConfig) {vm.representation.firmwareVersion.push(firmwareConfig.firmwareVersion)});
            });
        }

        $scope.$on('applicationType:changed', function(event, data) {
            $state.go('firmwareruletemplates', {actionType: vm.data.applicableAction.actionType});
        });

        function getFirmwareRuleTemplate(id) {
            firmwareRuleTemplateService.getFirmwareRuleTemplate(id)
                .then(function (result) {
                    vm.data = result.data;

                    vm.selectedActionType = APPLICABLE_ACTION_TYPE.getActionTypeByName(vm.data.applicableAction.actionType);
                    switch(vm.selectedActionType) {
                        case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE:
                            vm.ruleAction.data = vm.data.applicableAction;
                            getFirmwareConfigsAndBuildItBySupportedModels(vm.data);
                            break;
                        case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE:
                            vm.definePropertiesAction.data = vm.data.applicableAction;
                            getBlockingFilterTemplates();
                            angular.copy(
                                utilsService.convertObjectToArray(vm.definePropertiesAction.data.properties),
                                vm.editedData
                            );
                            break;
                        case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE:
                            vm.blockingFilterAction.data = vm.data.applicableAction;
                            break;
                    }
                }, alertsService.errorHandler);
        }

        function getFirmwareConfigsAndBuildItBySupportedModels(ruleData) {
            firmwareConfigService.getAll($rootScope.applicationType).then(function(response) {
                vm.allFirmwareConfigs = response.data;
                if (ruleData && ruleData.rule) {
                    vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(ruleData.rule, vm.firmwareConfigs);
                }
            }, function(error) {
                alertsService.showError({title: 'Error', message: 'Error by loading FirmwareConfig'});
            });
        }

        function saveFirmwareRuleTemplate() {
            if (vm.selectedActionType.name === APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name) {
                vm.definePropertiesAction.data.properties = utilsService.convertArrayToObject(vm.editedData);
            }
            var promise = vm.isNewEntity
                ? firmwareRuleTemplateService.createFirmwareRuleTemplate(vm.data)
                : firmwareRuleTemplateService.updateFirmwareRuleTemplate(vm.data);

            promise.then(function () {
                alertsService.successfullySaved(vm.data.id);
                $state.go('firmwareruletemplates', {actionType: vm.data.applicableAction.actionType});
            }, alertsService.errorHandler);
        }

        function setAvailablePriorities(size) {
            if (!vm.isNewEntity) {
                size = parseInt(size);
            }

            if (vm.isNewEntity) {
                size = parseInt(size) + 1;
            }

            vm.availablePriorities = [];
            for (var i = 1; i < size + 1; i++) {
                vm.availablePriorities.push(i);
            }
        }

        function getBlockingFilterTemplates() {
            firmwareRuleTemplateService.getFirmwareRuleTemplatesByActionType(APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name).then(
                function (result) {
                    vm.blockingFilterTemplates = result.data;
                    removeUnavailableBlockingFilterTemplatesIdsFromModel(result.data);
                }, alertsService.errorHandler);
        }

        function removeUnavailableBlockingFilterTemplatesIdsFromModel(availableBlockingFilterTemplates) {
            var availableIds = _.pluck(availableBlockingFilterTemplates, 'id');
            vm.data.applicableAction.byPassFilters = _.intersection(vm.data.applicableAction.byPassFilters, availableIds);
        }
    }
})();
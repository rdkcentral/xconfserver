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
        .module('app.firmwarerule')
        .controller('FirmwareRuleEditController', controller);

    controller.$inject=['$rootScope','$scope', '$state', '$controller', 'alertsService', '$stateParams', 'firmwareRuleTemplateService', 'firmwareRuleService', 'firmwareConfigService', 'APPLICABLE_ACTION_TYPE', 'utilsService', 'firmwareRuleValidationService', 'ruleHelperService', 'FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE', 'TIME_FREE_ARG_OPERATION_ARRAY', 'FREE_ARG_NAME', 'ruleValidationService', 'FIRMWARE_RULE_OPERATION_ARRAY', 'FIRMWARE_RULE_TYPE', '$q', 'authUtilsService', 'PERMISSION', 'FIRMWARE_RULE_CONNECTION_TYPE'];
    function controller($rootScope, $scope, $state, $controller, alertsService, $stateParams, firmwareRuleTemplateService, firmwareRuleService, firmwareConfigService, APPLICABLE_ACTION_TYPE, utilsService, firmwareRuleValidationService, ruleHelperService, FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE, TIME_FREE_ARG_OPERATION_ARRAY, FREE_ARG_NAME, ruleValidationService, FIRMWARE_RULE_OPERATION_ARRAY, FIRMWARE_RULE_TYPE, $q, authUtils, PERMISSION, FIRMWARE_RULE_CONNECTION_TYPE) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'firmwarerules',
            stateParameters: null
        }));

        vm.isValidCondition = true;
        vm.namespacedListData = ruleHelperService.buildNamespacedListData();
        vm.data = {
            "name": "",
            "rule": {},
            "applicableAction" : null,
            "applicationType": $rootScope.applicationType,
            "connectionType": FIRMWARE_RULE_CONNECTION_TYPE.ANY.value
        };
        vm.disableValidation = true;
        vm.isNewEntity = $state.current.name === 'firmwarerule-add';
        vm.firmwareRuleTemplates = [];
        vm.usedFirmwareRuleTemplate = null;
        vm.selectedFirmwareRuleTemplate = null;
        vm.editedData = [

        ];
        vm.freeArgAutocompleteValues = FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.blockingFilterTemplates = [];
        vm.previousFirmwareConfigId = '';
        vm.operations = {time: TIME_FREE_ARG_OPERATION_ARRAY, general: FIRMWARE_RULE_OPERATION_ARRAY};
        vm.isNoopRule = false;
        vm.selectedActionType = null;
        vm.firmwareConfigs = [];
        vm.percentFreeArgName = FREE_ARG_NAME.ESTB_MAC;

        // rule action
        vm.ruleAction = {};
        vm.ruleAction.data = {
            "type": APPLICABLE_ACTION_TYPE.RULE.class,
            "actionType": APPLICABLE_ACTION_TYPE.RULE.name,
            "configId": null,
            "configEntries":[]
        };

        vm.allFirmwareConfigs = [];
        vm.firmwareConfigMap = {};
        // define properties action
        vm.definePropertiesAction = {};
        vm.definePropertiesAction.data = {
            "type": APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.class,
            "actionType": APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name,
            "properties": {

            },
            "byPassFilters": []
        };

        // blocking filter action
        vm.blockingFilterAction = {};
        vm.blockingFilterAction.data = {
            "type": APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.class,
            "actionType": APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name
        };

        vm.representation = ruleHelperService.buildRepresentation();

        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.FIRMWARE_RULE_TYPE = FIRMWARE_RULE_TYPE;
        vm.PERMISSION = PERMISSION;
        vm.FIRMWARE_RULE_CONNECTION_TYPE = FIRMWARE_RULE_CONNECTION_TYPE;

        vm.validationFunction = ruleValidationService.validate;
        vm.actionValidator = firmwareRuleValidationService;
        vm.authUtils = authUtils;
        vm.saveFirmwareRule = saveFirmwareRule;
        vm.addDistribution = addDistribution;
        vm.removeDistribution = removeDistribution;
        vm.selectTemplate = selectTemplate;
        vm.hasError = hasError;
        vm.noopHasChanged = noopHasChanged;
        vm.cancel = cancel;

        init();

        function init() {
            if (vm.isNewEntity) {
                vm.selectedActionType = APPLICABLE_ACTION_TYPE.getActionTypeByName($stateParams.actionType);
                switch(vm.selectedActionType) {
                    case APPLICABLE_ACTION_TYPE.RULE:
                        vm.data.applicableAction = vm.ruleAction.data;
                        getAllFirmwareConfigs();
                        break;
                    case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES:
                        vm.data.applicableAction = vm.definePropertiesAction.data;
                        getBlockingFilterTemplates();
                        break;
                    case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER:
                        vm.data.applicableAction = vm.blockingFilterAction.data;
                        break;
                }
                getFirmwareRuleTemplates().then(function(resp) {
                    if ($stateParams.templateId) {
                        selectTemplate($stateParams.templateId);
                    }
                });
            } else {
                getFirmwareRule($stateParams.id);
            }
            _.each(['rule::created', 'rule::updated'], function(eventType) {
                $scope.$root.$on(eventType, function(e, obj) {
                    console.log("Event: " + eventType);
                    vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(obj.data, vm.allFirmwareConfigs);
                    reloadConfigId();
                    vm.representation.firmwareVersion = [];
                    _.each(vm.firmwareConfigs, function(firmwareConfig) {vm.representation.firmwareVersion.push(firmwareConfig.firmwareVersion)});
                });
            });
        }

        $scope.$on('applicationType:changed', function(event, data) {
            $state.go('firmwarerules', {actionType: vm.data.applicableAction.actionType});
        });

        function selectTemplate(templateId) {
            var template = utilsService.getItemFromListById(templateId, vm.firmwareRuleTemplates);
            vm.selectedFirmwareRuleTemplate = template;
            vm.data.type = templateId;
            vm.data.rule = template.rule;
            switch(vm.selectedActionType) {
                case APPLICABLE_ACTION_TYPE.RULE:
                    vm.ruleAction.data.configId = template.applicableAction.configId;
                    getAllFirmwareConfigs();
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES:
                    vm.definePropertiesAction.data.properties = convertTemplateProperties(template.applicableAction.properties);
                    vm.definePropertiesAction.data.byPassFilters = template.applicableAction.byPassFilters || [];
                    angular.copy(
                        utilsService.convertObjectToArray(vm.definePropertiesAction.data.properties),
                        vm.editedData
                    );
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER:
                    break;
            }
        }

        function convertTemplateProperties(templateProperties) {
            var result = {};
            _.each(templateProperties, function(value, key) {
                result[key] = value.value;
            });
            return result;
        }

        function getFirmwareRuleTemplates() {
            var deferred = $q.defer();
            firmwareRuleTemplateService.getByTypeAndEditableOption(vm.selectedActionType.name + "_TEMPLATE", true)
                .then(function(response) {
                    vm.firmwareRuleTemplates = response.data;
                    deferred.resolve(response)
                }, function(error) {
                    alertsService.showError({title: 'Error', message: 'Error occurred while loading Firmware Rule Templates'});
                    deferred.reject(error);
                });
            return deferred.promise;
        }

        function getFirmwareRuleTemplate(id) {
            firmwareRuleTemplateService.getFirmwareRuleTemplate(id)
                .then(function(response) {
                    vm.selectedFirmwareRuleTemplate = response.data;
                }, function(error) {
                    alertsService.showError({title: 'Error', message: 'Error occurred while loading Firmware Rule Templates'});
                });
        }

        function getFirmwareRule(id) {
            firmwareRuleService.getFirmwareRule(id)
                .then(function (result) {
                    vm.data = result.data;
                    getFirmwareRuleTemplate(vm.data.type);
                    vm.selectedActionType = APPLICABLE_ACTION_TYPE.getActionTypeByName(vm.data.applicableAction.actionType);
                    switch(vm.selectedActionType) {
                        case APPLICABLE_ACTION_TYPE.RULE:
                            vm.ruleAction.data = vm.data.applicableAction;
                            if (!vm.ruleAction.data.configId) {
                                vm.isNoopRule = true;
                            }
                            if (!vm.ruleAction.data.configEntries) {
                                vm.ruleAction.data.configEntries = [];
                            }
                            getAllFirmwareConfigs();
                            break;
                        case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES:
                            vm.definePropertiesAction.data = vm.data.applicableAction;
                            getBlockingFilterTemplates();
                            angular.copy(
                                utilsService.convertObjectToArray(vm.definePropertiesAction.data.properties),
                                vm.editedData
                            );
                            break;
                        case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER:
                            vm.blockingFilterAction.data = vm.data.applicableAction;
                            break;
                    }
                    vm.data.applicationType = $rootScope.applicationType ? $rootScope.applicationType : result.data.applicationType;
                }, function (reason) {
                    alertsService.showError({message: reason.data.message});
                });
        }

        function getAllFirmwareConfigs() {
            firmwareConfigService.getAll().then(function(response) {
                vm.allFirmwareConfigs = response.data;
                vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(vm.data.rule, vm.allFirmwareConfigs);
                vm.representation.firmwareVersion = [];
                _.each(vm.firmwareConfigs, function(firmwareConfig) {vm.representation.firmwareVersion.push(firmwareConfig.firmwareVersion)});
                if (vm.isNewEntity && vm.representation.firmwareVersion.length > 0) {
                    vm.ruleAction.data.configId = vm.allFirmwareConfigs[0].id;
                }
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });

            firmwareConfigService.getFirmwareConfigMap().then(function(resp) {
                vm.firmwareConfigMap = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function saveFirmwareRule() {
            if (vm.selectedActionType.name === APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name) {
                vm.definePropertiesAction.data.properties = utilsService.convertArrayToObject(vm.editedData);
            }
            if (vm.isNewEntity) {
                firmwareRuleService.createFirmwareRule(vm.data)
                    .then(function () {
                        alertsService.successfullySaved(vm.data.name);
                        $state.go('firmwarerules', {actionType: vm.data.applicableAction.actionType});
                    }, function (reason) {
                        alertsService.showError({message: reason.data.message});
                    });
            } else {
                firmwareRuleService.updateFirmwareRule(vm.data)
                    .then(function () {
                        alertsService.successfullySaved(vm.data.name);
                        $state.go('firmwarerules', {actionType: vm.data.applicableAction.actionType});
                    }, function (reason) {
                        alertsService.showError({message: reason.data.message});
                    });
            }
        }

        function hasError() {
            switch(vm.selectedActionType) {
                case APPLICABLE_ACTION_TYPE.RULE:
                    return vm.actionValidator.validateDistributionPercentages(vm.ruleAction.data.configEntries) !== '';
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES:
                    var count = 0;
                    var templateProperties = vm.selectedFirmwareRuleTemplate.applicableAction.properties;
                    _.each(vm.editedData, function(item) {
                        if (!firmwareRuleValidationService.validatePropertyValue(templateProperties[item.key], item.value)) {
                            count++;
                        }
                    });
                    if (count > 0) {
                        return true;
                    }
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER:
                    //
                    break;
            }
            return false;
        }

        function getBlockingFilterTemplates() {
            firmwareRuleTemplateService.getFirmwareRuleTemplatesByActionType(APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name).then(
                function (result) {
                    vm.blockingFilterTemplates = result.data;
                    removeUnavailableBlockingFilterTemplatesIdsFromModel(result.data);
                }, function (reason) {
                    alertsService.showError({message: reason.data.message});
                });
        }

        function removeUnavailableBlockingFilterTemplatesIdsFromModel(availableBlockingFilterTemplates) {
            var availableIds = _.pluck(availableBlockingFilterTemplates, 'id');
            vm.data.applicableAction.byPassFilters = _.intersection(vm.data.applicableAction.byPassFilters, availableIds);
        }

        function addDistribution(configs) {
            var newConfigEntry = {
                configId: '',
                percentage: ''
            };
            configs.push(newConfigEntry);
        }

        function removeDistribution(configs, item) {
            utilsService.removeItemFromArray(configs, item);
        }

        function noopHasChanged() {
            if (vm.isNoopRule) {
                vm.previousFirmwareConfigId = vm.ruleAction.data.configId;
                vm.ruleAction.data.configId = '';
                vm.ruleAction.data.configEntries.length = 0;
            } else {
                reloadConfigId(vm.previousFirmwareConfigId);
            }
        }

        function reloadConfigId(previousConfigId) {
            if (vm.isNoopRule) {
                return;
            }
            if (utilsService.isNullOrUndefinedOrEmpty(vm.firmwareConfigs)) {
                vm.ruleAction.data.configId = '';
            } else {
                var configIds = _.pluck(vm.firmwareConfigs, 'id');
                if (!previousConfigId || configIds.indexOf(previousConfigId) < 0) {
                    vm.ruleAction.data.configId = vm.firmwareConfigs[0].id;
                } else {
                    vm.ruleAction.data.configId = previousConfigId;
                }
            }
            vm.ruleAction.data.configEntries.length = 0;
        }

        $scope.$root.$on("rule::remove", function(e, obj) {
            var watchResult = ruleHelperService.watchRuleRemoveOperation(vm.isValidCondition, vm.data.rule, obj);
            vm.data.rule = watchResult.rule;
            vm.isValidCondition = watchResult.isValidCondition;
            vm.firmwareConfigs = ruleHelperService.buildFirmwareConfigsBySupportedModels(vm.data.rule, vm.allFirmwareConfigs);
            reloadConfigId();
            vm.representation.firmwareVersion = [];
            _.each(vm.firmwareConfigs, function(firmwareConfig) {vm.representation.firmwareVersion.push(firmwareConfig.firmwareVersion)});
        });

        function cancel() {
            if ($stateParams.templateId) {
                $state.go('firmwareruletemplates');
            } else {
                $state.go('firmwarerules', {actionType: vm.data.applicableAction.actionType});
            }
        }
    }
})();
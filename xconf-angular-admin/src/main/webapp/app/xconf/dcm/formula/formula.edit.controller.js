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
        .controller('FormulaEditController', controller);

    controller.$inject=['$rootScope', '$log', '$scope', '$state', '$stateParams', '$controller', 'alertsService', 'utilsService', 'ruleHelperService', 'formulaService', 'vodSettingsService', '$uibModal', 'deviceSettingsService', '$q', 'logUploadSettingsService', 'SETTINGS_AVAILABILITY_KEYS', 'formulaValidationService', 'EDIT_MODE', 'LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', 'FREE_ARG_NAME', 'ruleValidationService', 'firmwareConfigService'];

    function controller($rootScope, $log, $scope, $state, $stateParams, $controller, alertsService, utilsService, ruleHelperService, formulaService, vodSettingsService, $modal, deviceSettingsService, $q, logUploadSettingsService, SETTINGS_AVAILABILITY_KEYS, formulaValidationService, EDIT_MODE, LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE, FREE_ARG_NAME, ruleValidationService, firmwareConfigService) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'formulas',
            stateParameters: null
        }));

        vm.formula = {
            "rule": {
                applicationType: $rootScope.applicationType,
                name:''
            }
        };
        vm.SETTINGS_AVAILABILITY_KEYS = SETTINGS_AVAILABILITY_KEYS;
        vm.namespacedListData = ruleHelperService.buildNamespacedListData();
        vm.namespacedListIds = [];
        vm.usedNames = [];
        vm.formulaValidationService = formulaValidationService;
        vm.EDIT_MODE = EDIT_MODE;
        vm.currentEditMode = $stateParams.ruleId ? EDIT_MODE.UPDATE : EDIT_MODE.CREATE;
        vm.formulasSize = $stateParams.formulasSize ? parseInt($stateParams.formulasSize) : 0;
        vm.availablePriorities = [];
        vm.isSettingsAvailable = {};
        vm.freeArgAutocompleteValues = LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.validationFunction = ruleValidationService.validate;
        vm.percentFreeArgName = FREE_ARG_NAME.ESTB_MAC_ADDRESS;
        vm.allFirmwareConfigs = [];
        vm.representation = ruleHelperService.buildRepresentation();

        vm.saveFormula = saveFormula;
        vm.editVodSettings = editVodSettings;
        vm.createVodSettings = createVodSettings;
        vm.editDeviceSettings = editDeviceSettings;
        vm.createDeviceSettings = createDeviceSettings;
        vm.editLogUploadSettings = editLogUploadSettings;
        vm.createLogUploadSettings = createLogUploadSettings;

        init();

        function init() {
            if (vm.currentEditMode === EDIT_MODE.UPDATE) {
                formulaService.getById($stateParams.ruleId).then(function(resp) {
                    vm.formula.rule = resp.data;
                    getUsedNames();
                }, alertsService.errorHandler);

            }

            if (vm.currentEditMode === EDIT_MODE.CREATE) {
                vm.formula.rule.percentage = 100;
                vm.formula.rule.percentageL1 = 0;
                vm.formula.rule.percentageL2 = 0;
                vm.formula.rule.percentageL3 = 0;
                getUsedNames();
            }

            firmwareConfigService.getAll($rootScope.applicationType).then(function(resp) {
                vm.allFirmwareConfigs = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
            _.each(['rule::created', 'rule::updated'], function(eventType) {
                $scope.$root.$on(eventType, function(e, obj) {
                    console.log("Event: " + eventType);
                    vm.representation.firmwareVersion = ruleHelperService.buildFirmwareConfigs(obj.data, vm.allFirmwareConfigs);
                });
            });

            $scope.$root.$on("rule::remove", function(e, obj) {
                var watchResult = ruleHelperService.watchRuleRemoveOperation(vm.isValidCondition, vm.formula.rule, obj);
                vm.formula.rule = watchResult.rule;
                vm.isValidCondition = watchResult.isValidCondition;
                vm.representation.firmwareVersion = ruleHelperService.buildFirmwareConfigs(vm.formula.rule, vm.allFirmwareConfigs);
            });

            if (vm.currentEditMode === EDIT_MODE.UPDATE) {
                formulaService.getSettingsAvailability([$stateParams.ruleId]).then(function(resp) {
                    vm.isSettingsAvailable = resp.data;
                });
            }

            setAvailablePriorities(vm.formulasSize);
            if (vm.currentEditMode === EDIT_MODE.CREATE) {
                vm.formula.rule.priority = vm.availablePriorities[vm.availablePriorities.length - 1];
            }
        }

        function saveFormula() {
            if (vm.currentEditMode === vm.EDIT_MODE.UPDATE) {
                formulaService.update(vm.formula.rule).then(function (resp) {
                    alertsService.successfullySaved(vm.formula.rule.name);
                    $state.go('formula-edit', {ruleId: resp.data.id});
                }, alertsService.errorHandler);
            }

            if (vm.currentEditMode === vm.EDIT_MODE.CREATE) {
                formulaService.create(vm.formula.rule).then(function (resp) {
                    alertsService.successfullySaved(vm.formula.rule.name);
                    $state.go('formula-edit', {ruleId: resp.data.id});
                }, alertsService.errorHandler);
            }
        }
        
        function createVodSettings(ruleId) {
            openEditVodSettingsModal(ruleId);
        }

        function editVodSettings(ruleId) {
            vodSettingsService.getById(ruleId).then(function(resp) {
                openEditVodSettingsModal(ruleId, resp.data);
            }, function(error) {
                $log.error(error.data.message);
            });
        }

        function openEditVodSettingsModal(id, vodSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/vodsettings/vodsettings.modal.edit.html',
                controller: 'VodSettingsModalEditController as vm',
                size: 'md',
                resolve : {
                    vodSettings: function() {
                        return vodSettings;
                    },
                    formulaId: function() {
                        return id;
                    }
                }
            }).result.then(
                function() {
                    init();
                }
            );
        }

        function createDeviceSettings(ruleId) {
            openEditDeviceSettingsModal(ruleId);
        }

        function editDeviceSettings(ruleId) {
            deviceSettingsService.getDeviceSettings(ruleId).then(
                function(result) {
                    openEditDeviceSettingsModal(ruleId, result.data);
                },
                alertsService.errorHandler
            );
        }

        function openEditDeviceSettingsModal(id, deviceSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/devicesettings/devicesettings.edit.html',
                size: 'lg',
                controller: 'DeviceSettingsEditController as vm',
                resolve: {
                    deviceSettings: function () {
                        return deviceSettings;
                    },
                    id: function () {
                        return id;
                    }
                }
            }).result.then(
                function() {
                    init();
                }
            );
        }

        function createLogUploadSettings(ruleId) {
            openLogUploadSettingsModal(ruleId);
        }

        function editLogUploadSettings(ruleId) {
            logUploadSettingsService.getLogUploadSettings(ruleId).then(
                function(result) {
                    openLogUploadSettingsModal(ruleId, result.data);
                },
                function(error) {
                    alertsService.showError({message: error.data.message, title: 'Service error'});
                }
            );
        }

        function openLogUploadSettingsModal(id, logUploadSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/loguploadsettings/loguploadsettings.edit.html',
                size: 'lg',
                controller: 'LogUploadSettingsEditController as vm',
                resolve: {
                    logUploadSettings: function () {
                        return logUploadSettings;
                    },
                    id: function () {
                        return id;
                    }
                }
            }).result.then(
                function() {
                    init();
                }
            );
        }

        function getUsedNames() {
            var initialName = angular.copy(vm.formula.rule.name);
            formulaService.getUsedNames().then(function(resp) {
                vm.usedNames = resp.data;
                utilsService.removeItemFromArray(vm.usedNames, initialName);
            });
        }

        function setAvailablePriorities(size) {
            if (vm.currentEditMode === EDIT_MODE.UPDATE) {
                size = parseInt(size);
            }

            if (vm.currentEditMode === EDIT_MODE.CREATE) {
                size = parseInt(size) + 1;
            }

            vm.availablePriorities = [];
            for (var i = 1; i < size + 1; i++) {
                vm.availablePriorities.push(i);
            }
        }
    }
})();
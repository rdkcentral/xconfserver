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
        .controller('PercentageBeanEditController', controller);

    controller.$inject = ['$rootScope', '$state', '$stateParams', 'percentFilterService', 'namespacedListService', 'alertsService', 'percentFilterValidationService', 'utilsService', 'firmwareConfigService', '$scope', 'modelService', 'environmentService', 'percentageBeanService', 'NAMESPACED_LIST_TYPE', 'ruleHelperService', 'ruleValidationService', 'TIME_FREE_ARG_OPERATION_ARRAY', 'PERCENTAGE_BEAN_OPERATION_ARRAY', 'FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE', 'FREE_ARG_NAME'];

    function controller($rootScope, $state, $stateParams, percentFilterService, namespacedListService, alertsService, percentFilterValidationService, utilsService, firmwareConfigService, $scope, modelService, environmentService, percentageBeanService, NAMESPACED_LIST_TYPE, ruleHelperService, ruleValidationService, TIME_FREE_ARG_OPERATION_ARRAY, PERCENTAGE_BEAN_OPERATION_ARRAY, FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE, FREE_ARG_NAME) {
        var vm = this;

        vm.percentageBean = {
            applicationType: $rootScope.applicationType,
            optionalConditions: {},
            distributions: [],
            firmwareVersions: [],
            firmwareCheckRequired: false,
            active: false,
            rebootImmediately: false,
            useAccountIdPercentage: false
        };
        vm.firmwareVersionSelectObjects = [];
        vm.firmwareConfigs = [];
        vm.missingFirmwareVersions = [];
        vm.firmwareConfigsBySupportedModels = [];
        vm.validator = percentFilterValidationService;
        vm.models = [];
        vm.environments = [];
        vm.hasValue = utilsService.hasValue;
        vm.whitelists = [];
        vm.noop = false;

        vm.disableValidation = true;
        vm.namespacedListData = ruleHelperService.buildNamespacedListData();
        vm.operations = {time: TIME_FREE_ARG_OPERATION_ARRAY, general: PERCENTAGE_BEAN_OPERATION_ARRAY};
        vm.representation = ruleHelperService.buildRepresentation();
        vm.freeArgAutocompleteValues = FIRMWARE_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.validationFunction = ruleValidationService.validate;
        vm.percentFreeArgName = FREE_ARG_NAME.ESTB_MAC;

        vm.save = save;
        vm.selectFirmwareConfig = selectFirmwareConfig;
        vm.getSelectedFirmwareVersions = getSelectedFirmwareVersions;
        vm.addDistribution = addDistribution;
        vm.getTotalDistributionPercentage = percentageBeanService.getTotalDistributionPercentage;
        vm.setNoop = setNoop;
        vm.isNoop = isNoop;
        vm.reloadFirmwareConfigsByModelChanging = reloadFirmwareConfigsByModelChanging;
        init();

        function init() {

            firmwareConfigService.getAll().then(function(resp) {
                vm.allFirmwareConfigs = resp.data;
                initPercentageBean();
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message})
            });

            $scope.$root.$on("rule::remove", function(e, obj) {
                var watchResult = ruleHelperService.watchRuleRemoveOperation(vm.isValidCondition, vm.percentageBean.optionalConditions, obj);
                vm.percentageBean.optionalConditions = watchResult.rule;
                vm.isValidCondition = watchResult.isValidCondition;
            });

            modelService.getAll().then(function(resp) {
                vm.models = resp.data;
            }, alertsService.errorHandler);

            environmentService.getAll().then(function(resp) {
                vm.environments = resp.data;
            }, alertsService.errorHandler);

            vm.validator.cleanErrors();
        }

        function initPercentageBean() {
            if ($stateParams.id) {
                percentageBeanService.getById($stateParams.id).then(function (resp) {
                    vm.percentageBean = resp.data;
                    if (!vm.percentageBean.optionalConditions) {
                        vm.percentageBean.optionalConditions = {};
                    }
                    reloadFirmwareConfigsByModelChanging(vm.percentageBean.model);
                    percentageBeanService.sortPercentageBeanFirmwareVersionsIfExistOrNot(vm.percentageBean).then(function (missingFirmwareVersions) {
                        vm.missingFirmwareVersions = missingFirmwareVersions;
                    }, alertsService.errorHandler);
                    vm.noop = isNoop();

                }, alertsService.errorHandler);
            }

            namespacedListService.getNamespacedListIdsByType(NAMESPACED_LIST_TYPE.IP_LIST).then(function(resp) {
                vm.whitelists = resp.data;
            }, alertsService.errorHandler);
        }

        $rootScope.$on('applicationType:changed', function(event, data) {
            $state.go('percentfilter');
        });

        function save(percentageBean) {
            percentageBean.firmwareVersions = getSelectedFirmwareVersions(vm.firmwareVersionSelectObjects);
            if (!percentageBean.firmwareCheckRequired) {
                percentageBean.rebootImmediately = false;
                percentageBean.firmwareVersions = [];
            }

            if (vm.validator.validatePercentageBean(percentageBean, getSelectedFirmwareVersions(vm.firmwareVersionSelectObjects), vm.firmwareConfigsBySupportedModels)) {
                percentageBean.firmwareVersions = getSelectedFirmwareVersions(vm.firmwareVersionSelectObjects);
                if (!percentageBean.firmwareCheckRequired) {
                    percentageBean.rebootImmediately = false;
                    percentageBean.firmwareVersions = [];
                }
                if ($stateParams.id) {
                    percentageBeanService.update(percentageBean).then(function (resp) {
                        alertsService.successfullySaved(percentageBean.name);
                        $state.go('percentfilter');
                    }, alertsService.errorHandler);
                } else {
                    percentageBeanService.create(percentageBean).then(function (resp) {
                        alertsService.successfullySaved(percentageBean.name);
                        $state.go('percentfilter');
                    }, alertsService.errorHandler);
                }
            }
        }

        function selectFirmwareConfig(firmwareConfigSelectObject) {
            firmwareConfigSelectObject.selected = !firmwareConfigSelectObject.selected;
        }

        function getSelectedFirmwareVersions(firmwareConfigSelectEntities) {
            var selectedVersions = [];
            angular.forEach(firmwareConfigSelectEntities, function (val, key) {
                if (val.selected === true) {
                    selectedVersions.push(val.config.firmwareVersion);
                }
            });
            return selectedVersions;
        }

        $scope.$watch('vm.percentageBean.lastKnownGood', function(newLkgConfigId, oldLkgConfigId) {
            vm.firmwareVersionSelectObjects.forEach(function (firmwareVersionSelectObject) {
                if (vm.percentageBean && vm.percentageBean.firmwareCheckRequired) {
                    var oldLkgConfig = utilsService.getItemFromListById(oldLkgConfigId, vm.firmwareConfigsBySupportedModels);
                    if (firmwareVersionSelectObject.config.id === newLkgConfigId) {
                        firmwareVersionSelectObject.selected = true;
                    } else if (firmwareVersionSelectObject.config.id == oldLkgConfigId
                        && vm.percentageBean.firmwareVersions.indexOf(oldLkgConfig.firmwareVersion) === -1) {
                        firmwareVersionSelectObject.selected = false;
                    }
                }
            });
            vm.noop = isNoop();
        });

        $scope.$watch('vm.percentageBean.intermediateVersion', function() {
            vm.noop = isNoop();
        });

        $scope.$watch('vm.percentageBean.distributions', function() {
            if (Math.floor(vm.getTotalDistributionPercentage(vm.percentageBean)) === 100) {
                vm.percentageBean.lastKnownGood = null;
            }
            vm.noop = isNoop();
        }, true);

        function addDistribution(percentageBean) {
            var distribution = {
                configId: '',
                percentage: '',
                startPercentRange: '',
                endPercentRange: ''
            };
            percentageBean.distributions.push(distribution);
            percentageBean.firmwareCheckRequired = true;
        }

        function setNoop() {
            if (vm.percentageBean.distributions.length) {
                vm.percentageBean.distributions.length = 0;
            }
            if (vm.percentageBean.lastKnownGood) {
                vm.percentageBean.lastKnownGood = '';
            }
            if (vm.percentageBean.intermediateVersion) {
                vm.percentageBean.intermediateVersion  = '';
            }
        }

        function isNoop() {
            var configPresent = vm.percentageBean.distributions.length || vm.percentageBean.lastKnownGood || vm.percentageBean.intermediateVersion;
            return !configPresent;
        }

        function reloadFirmwareConfigsByModelChanging(modelId) {
            vm.firmwareVersionSelectObjects = [];
            firmwareConfigService.getByModelId(modelId).then(function(firmwareConfigResp) {
                vm.firmwareConfigsBySupportedModels = firmwareConfigResp.data;
                angular.forEach(firmwareConfigResp.data, function (val, key) {
                    var selectObject = {
                        config: val,
                        selected: false
                    };
                    if (vm.percentageBean.firmwareVersions.indexOf(val.firmwareVersion) !== -1) {
                        selectObject.selected = true;
                    }
                    vm.firmwareVersionSelectObjects.push(selectObject);
                });
            }, alertsService.errorHandler);
        }
    }
})();
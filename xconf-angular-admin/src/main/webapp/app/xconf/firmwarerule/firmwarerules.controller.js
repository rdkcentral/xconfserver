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
        .controller('FirmwareRulesController', controller);

    controller.$inject=['$rootScope', '$q', '$scope', '$uibModal', '$location','$stateParams', '$controller', 'dialogs', 'alertsService', 'utilsService', 'firmwareRuleService', 'firmwareRuleTemplateService', 'APPLICABLE_ACTION_TYPE', 'firmwareConfigService', 'paginationService', 'RULE_SEARCH_OPTIONS', 'SEARCH_OPTIONS'];

    function controller($rootScope, $q, $scope, $modal, $location, $stateParams, $controller, dialogs, alertsService, utilsService, firmwareRuleService, firmwareRuleTemplateService, APPLICABLE_ACTION_TYPE, firmwareConfigService, paginationService, RULE_SEARCH_OPTIONS, SEARCH_OPTIONS) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        var promises = [];

        vm.data = [];
        vm.firmwareRuleTemplateId = $location.search().filterByTemplate ? $location.search().filterByTemplate : ' ';
        vm.firmwareRuleTemplateIds = [];
        vm.allFirmwareConfigs = [];
        vm.viewerPanelControl = {};

        vm.paginationStorageKey = 'firmwareRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.utilsService = utilsService;
        vm.selectedActionType = $stateParams.actionType
            ? APPLICABLE_ACTION_TYPE.getActionTypeByName($stateParams.actionType)
            : APPLICABLE_ACTION_TYPE.RULE;
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.sizeOfEachType = {};
        vm.searchParam = {};
        vm.searchOptions = null;

        vm.getFirmwareRules = getFirmwareRules;
        vm.deleteFirmwareRule = deleteFirmwareRule;
        vm.viewFirmwareRule = viewFirmwareRule;
        vm.exportFirmwareRule = exportFirmwareRule;
        vm.exportAllFirmwareRulesByType = exportAllFirmwareRulesByType;
        vm.exportAllFirmwareRules = exportAllFirmwareRules;
        vm.changeFirmwareRuleTemplateId = changeFirmwareRuleTemplateId;
        vm.getSizeByType = getSizeByType;
        vm.reloadPageByActionType = reloadPageByActionType;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.getSizeOfAllTypes = getSizeOfAllTypes;
        vm.getConfigById = getConfigById;
        vm.isMac = isMac;
        vm.isFirmwareRuleTemplate = isFirmwareRuleTemplate;

        init();

        function init() {
            if ($rootScope.applicationType) {
                vm.searchParam.APPLICATION_TYPE = $rootScope.applicationType;
            }
            reloadPageByActionType(vm.selectedActionType);
        }

        $scope.$on('applicationType:changed', function(event, data) {
            vm.searchParam.APPLICATION_TYPE = data.applicationType;
            getFirmwareRules();
        });

        function setSearchOptions(actionType) {
            vm.searchOptions = angular.copy(RULE_SEARCH_OPTIONS);
            if (actionType === vm.APPLICABLE_ACTION_TYPE.RULE) {
                var firmwareConfigSearchObject = {
                        "name": {
                            friendlyName: "FirmwareConfig",
                            apiArgs: ["FIRMWARE_VERSION"]
                        }
                    };
                vm.searchOptions.data.push(firmwareConfigSearchObject);
            }

            vm.searchParam[SEARCH_OPTIONS.TEMPLATE_ID] = vm.firmwareRuleTemplateId.trim();
            vm.searchParam[SEARCH_OPTIONS.APPLICABLE_ACTION_TYPE] = actionType.name;
        }

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            vm.searchParam[SEARCH_OPTIONS.TEMPLATE_ID] = vm.firmwareRuleTemplateId.trim();
            vm.searchParam[SEARCH_OPTIONS.APPLICABLE_ACTION_TYPE] = vm.selectedActionType.name;
            getFirmwareRules();
        });

        function reloadPageByActionType(selectedActionType) {
            promises = [];
            vm.firmwareRuleTemplateId = '';
            vm.selectedActionType = selectedActionType;
            getAllFirmwareConfigs();
            getFirmwareRuleTemplateIds();
            $q.all(promises).then(function() {
                getFirmwareRules();
            });
            setSearchOptions(vm.selectedActionType);
        }

        function getAllFirmwareConfigs() {
            vm.allFirmwareConfigs = [];
            if (vm.selectedActionType.name === APPLICABLE_ACTION_TYPE.RULE.name) {
                var firmwareConfigRequest = firmwareConfigService.getAll();
                firmwareConfigRequest.then(function(result) {
                    vm.allFirmwareConfigs = result.data;
                });
                promises.push(firmwareConfigRequest);
            }
        }

        function getConfigById(configId) {
            if (configId) {
                for (var key in vm.allFirmwareConfigs) {
                    if (vm.allFirmwareConfigs[key].id === configId) {
                        return vm.allFirmwareConfigs[key];
                    }
                }
            }
            return null;
        }

        function isMac(rule) {
            var isMac = false;
            if (rule.compoundParts && rule.compoundParts.length) {
                for (var key in rule.compoundParts) {
                    isMac = rule.compoundParts[key].condition.freeArg.name.includes('Mac');
                }
            } else {
                isMac = rule.condition.freeArg.name.includes('Mac');
            }

            return isMac;
        }

        function getFirmwareRules() {
            vm.searchParam = utilsService.removeEmptyStringParams(vm.searchParam);
            firmwareRuleService.getFirmwareRulesByActionType(vm.pageNumber, vm.pageSize, vm.searchParam)
                .then(function (result) {
                    vm.data = result.data;
                    setSizeOfEachType(result);
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                }, function (reason) {
                    alertsService.showError({message: reason.data.message, title: reason.data.type});
                });
        }

        function isFirmwareRuleTemplate(id) {
            var isFirmwareRuleTemplate = false;
            for (var key in vm.firmwareRuleTemplateIds) {
                if (vm.firmwareRuleTemplateIds[key] === id) {
                    isFirmwareRuleTemplate = true;
                    break;
                }
            }
            return isFirmwareRuleTemplate;
        }

        function setSizeOfEachType(result) {
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.RULE.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.RULE.name);
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name);
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name);
        }

        function getSizeByType(type) {
            var size = vm.sizeOfEachType[type];
            return size ? size : 0;
        }

        function getSizeOfAllTypes() {
            var size = 0;
            for (var key in vm.APPLICABLE_ACTION_TYPE) {
                size += parseInt(getSizeByType(vm.APPLICABLE_ACTION_TYPE[key].name));
            }
            return size;
        }

        function getFirmwareRuleTemplateIds() {
            var firmwareRuleTemplateRequest = firmwareRuleTemplateService.getFirmwareRuleTemplateIdsByActionType(vm.selectedActionType.name + "_TEMPLATE");
            firmwareRuleTemplateRequest.then(function (result) {
                vm.firmwareRuleTemplateIds.length = 0;
                _.each(result.data, function(value) {
                    if (!vm.firmwareRuleTemplateIds[value]) {
                        vm.firmwareRuleTemplateIds.push(value);
                    }
                });
            }, function (reason) {
                alertsService.showError({message: reason.data.message, title: reason.data.type});
            });
            promises.push(firmwareRuleTemplateRequest);
        }

        function viewFirmwareRule(id) {
            firmwareRuleService.getFirmwareRule(id).then(function (result) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/xconf/firmwarerule/firmwarerule-view.html',
                    size: 'md',
                    controller: 'FirmwareRuleViewController as vm',
                    resolve: {
                        obj: function () {
                            return result.data;
                        }
                    }
                });
            }, function(reason) {
                alertsService.showError({message: reason.data.message, title: reason.data.type});
            });
        }

        function changeFirmwareRuleTemplateId() {
            vm.searchParam[SEARCH_OPTIONS.TEMPLATE_ID] = vm.firmwareRuleTemplateId.trim();
            getFirmwareRules();
            if (vm.firmwareRuleTemplateId && vm.firmwareRuleTemplateId.trim()) {
                $location.search('filterByTemplate', vm.firmwareRuleTemplateId);
            } else {
                $location.search('filterByTemplate', null);
            }
        }

        function deleteFirmwareRule(firmwareRule) {
            dialogs
                .confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Firmware Rule ' + firmwareRule.name + '? </span>')
                .result.then().then(function (btn) {
                    firmwareRuleService.deleteFirmwareRule(firmwareRule.id)
                        .then(function (result) {
                            alertsService.successfullyDeleted(firmwareRule.name);
                            shiftItems();
                        }, function (reason) {
                            alertsService.showError({message: reason.data.message, title: reason.data.type});
                        });
                }, function (btn) {
                    //click cancel
                });
        }

        function exportFirmwareRule(id) {
            firmwareRuleService.exportFirmwareRule(id);
        }

        function exportAllFirmwareRulesByType(type) {
            firmwareRuleService.exportAllFirmwareRulesByType(type);
        }

        function exportAllFirmwareRules() {
            firmwareRuleService.exportAllFirmwareRules();
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getSizeByType(vm.selectedActionType.name) - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            reloadPageByActionType(vm.selectedActionType);
        }

        function startParse() {
            return !vm.utilsService.isMapEmpty(vm.sizeOfEachType);
        }

        function getGeneralItemsNumber() {
            return getSizeByType(vm.selectedActionType.name);
        }
    }
})();
/* 
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
 *
 * Author: Stanislav Menshykov
 * Created: 11/27/15  12:34 PM
 */

(function() {
    'use strict';

    angular
        .module('app.firmwarerule')
        .controller('FirmwareRuleImportController', controller);

    controller.$inject=['$rootScope', '$scope', '$log', '$uibModal', '$location', 'alertsService', 'firmwareRuleService', 'importService', 'utilsService', 'APPLICABLE_ACTION_TYPE', 'paginationService', 'authUtilsService', 'PERMISSION'];

    function controller($rootScope, $scope, $log, $modal, $location, alertsService, firmwareRuleService, importService, utilsService, APPLICABLE_ACTION_TYPE, paginationService, authUtils, PERMISSION) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importFirmwareRule = importFirmwareRule;
        vm.importAllFirmwareRules = importAllFirmwareRules;
        vm.getWrappedFirmwareRulesByType = getWrappedFirmwareRulesByType;
        vm.separateWrappedFirmwareRulesByType = separateWrappedFirmwareRulesByType;
        vm.overwriteAll = overwriteAll;
        vm.getSizeByType = getSizeByType;
        vm.authUtils = authUtils;
        vm.paginationStorageKey = 'firmwareRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;

        vm.viewFirmwareRule = viewFirmwareRule;
        vm.wrappedFirmwareRules = null;
        vm.isOverwritten = false;
        vm.selectedActionType = APPLICABLE_ACTION_TYPE.RULE;
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.ruleTypeFirmwareRules = [];
        vm.definePropertiesTypeFirmwareRules = [];
        vm.blockingFilterTypeFirmwareRules = [];
        vm.currentFirmwareRules = [];
        vm.PERMISSION = PERMISSION;
        vm.deviceType = null;
        vm.progressBarControl = importService.progressBarControl;


        async function retrieveFile(fileName) {
            clean();
            try {
                let file = await importService.openFile(fileName, null, this);
                init(file);
            } catch(e) {
                alertsService.showError({message: e});
            }
        }

        function init(file) {
            let firmwareRules = importService.getEntitiesFromFile(file);
            utilsService.sortObjectsById(firmwareRules);
            vm.wrappedFirmwareRules = importService.wrapToImport(firmwareRules);
            separateWrappedFirmwareRulesByType(vm.wrappedFirmwareRules);
            getWrappedFirmwareRulesByType(vm.selectedActionType);
            vm.isOverwritten = false;
            overwriteAll();
            selectPage();
        }

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        function viewFirmwareRule(firmwareRule) {
            $modal.open({
                templateUrl: 'app/xconf/firmwarerule/firmwarerule-view.html',
                size: 'lg',
                controller: 'FirmwareRuleViewController as vm',
                resolve: {
                    obj: function () {
                        return firmwareRule;
                    }
                }
            });
        }

        function importFirmwareRule(wrappedFirmwareRule) {
            if (!wrappedFirmwareRule.entity.applicationType) {
                wrappedFirmwareRule.entity.applicationType = $rootScope.applicationType;
            }
            if (wrappedFirmwareRule.overwrite) {
                handleSavePromise(wrappedFirmwareRule, firmwareRuleService.updateFirmwareRule(wrappedFirmwareRule.entity));
            } else {
                handleSavePromise(wrappedFirmwareRule, firmwareRuleService.createFirmwareRule(wrappedFirmwareRule.entity));
            }
        }

        function handleSavePromise(wrappedFirmwareRule, promise) {
            promise.then(
                function () {
                    alertsService.successfullySaved(wrappedFirmwareRule.entity.name);
                    removeImportedItemFromListsById(wrappedFirmwareRule.entity.id);
                }, function (reason) {
                    var data = reason.data;
                    alertsService.showError({title: data.type, message: data.message});
                }
            );
        }

        function removeImportedItemFromListsById(id) {
            _.each([vm.wrappedFirmwareRules, vm.ruleTypeFirmwareRules,
                vm.definePropertiesTypeFirmwareRules, vm.blockingFilterTypeFirmwareRules], function(key) {
                utilsService.removeSelectedItemFromListById(key, id);
            });
        }

        function importAllFirmwareRules() {
            importService.importAllEntities(firmwareRuleService, vm.wrappedFirmwareRules, function(id) {
                removeImportedItemFromListsById(id);
            });
        }

        function getWrappedFirmwareRulesByType(type) {
            vm.selectedActionType = type;
            switch(type) {
                case APPLICABLE_ACTION_TYPE.RULE:
                    vm.currentFirmwareRules = vm.ruleTypeFirmwareRules;
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES:
                    vm.currentFirmwareRules = vm.definePropertiesTypeFirmwareRules;
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER:
                    vm.currentFirmwareRules = vm.blockingFilterTypeFirmwareRules;
                    break;
            }
        }

        function separateWrappedFirmwareRulesByType(wrappedFirmwareRules) {
            angular.forEach(wrappedFirmwareRules, function(value) {
                switch(value.entity.applicableAction.actionType) {
                    case APPLICABLE_ACTION_TYPE.RULE.name:
                        vm.ruleTypeFirmwareRules.push(value);
                        break;
                    case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name:
                        vm.definePropertiesTypeFirmwareRules.push(value);
                        break;
                    case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name:
                        vm.blockingFilterTypeFirmwareRules.push(value);
                        break;
                }
            });
        }

        function getSizeByType(type) {
            switch(type) {
                case APPLICABLE_ACTION_TYPE.RULE.name:
                    return vm.ruleTypeFirmwareRules.length;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name:
                    return vm.definePropertiesTypeFirmwareRules.length;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name:
                    return vm.blockingFilterTypeFirmwareRules.length;
            }
        }

        function clean() {
            vm.wrappedFirmwareRules = [];
            vm.currentFirmwareRules = [];
            vm.ruleTypeFirmwareRules = [];
            vm.definePropertiesTypeFirmwareRules = [];
            vm.blockingFilterTypeFirmwareRules = [];
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedFirmwareRules, function (val, key) {
                val.overwrite = vm.isOverwritten;
            });
        }

        function selectPage() {
            paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            computeStartAndEndIndex();
        }

        function computeStartAndEndIndex() {
            vm.startIndex = (vm.pageNumber - 1) * vm.pageSize;
            vm.endIndex = vm.pageNumber * vm.pageSize;
        }

        function getGeneralItemsNumber() {
            return getSizeByType(vm.selectedActionType.name);
        }
    }
})();
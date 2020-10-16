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
 *  Author: mdolina
 *  Created: 01/18/16 11:35 AM
 */

(function() {
    'use strict';

    angular
        .module('app.firmwareconfig')
        .controller('FirmwareConfigImportController', controller);

    controller.$inject=['$rootScope', '$scope', '$log', 'alertsService', 'utilsService', 'importService', 'firmwareConfigService', '$uibModal', 'paginationService', 'authUtilsService', 'PERMISSION'];

    function controller($rootScope, $scope, $log, alertsService, utilsService, importService, firmwareConfigService, $modal, paginationService, authUtils, PERMISSION) {
        var vm = this;

        vm.firmwareConfigs = null;
        vm.wrappedFirmwareConfigs = null;
        vm.isOverwritten = false;
        vm.paginationStorageKey = 'firmwareRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.PERMISSION = PERMISSION;
        vm.retrieveFile = retrieveFile;
        vm.importFirmwareConfig = importFirmwareConfig;
        vm.importAllFirmwareConfigs = importAllFirmwareConfigs;
        vm.overwriteAll = overwriteAll;
        vm.viewFirmwareConfig = viewFirmwareConfig;
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.progressBarControl = importService.progressBarControl;
        vm.authUtils = authUtils;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        async function retrieveFile(fileName) {
            vm.firmwareConfigs = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedFirmwareConfigs = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importFirmwareConfig(wrappedFirmwareConfig) {
            if (!wrappedFirmwareConfig.entity.applicationType) {
                wrappedFirmwareConfig.entity.applicationType = $rootScope.applicationType;
            }
            if (wrappedFirmwareConfig.overwrite) {
                firmwareConfigService.update(wrappedFirmwareConfig.entity).then(function () {
                    alertsService.successfullySaved(wrappedFirmwareConfig.entity.description);
                    utilsService.removeSelectedItemFromListById(vm.wrappedFirmwareConfigs, wrappedFirmwareConfig.entity.id);
                }, function (error) {
                    alertsService.showError({message: error.data.message, title: 'Exception'});
                });
            } else {
                firmwareConfigService.create(wrappedFirmwareConfig.entity).then(function () {
                    alertsService.successfullySaved(wrappedFirmwareConfig.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.wrappedFirmwareConfigs, wrappedFirmwareConfig.entity.id);
                }, function (error) {
                    alertsService.showError({message: error.data.message, title: 'Exception'});
                });
            }
        }

        function importAllFirmwareConfigs() {
            importService.importAllEntities(firmwareConfigService, vm.wrappedFirmwareConfigs);
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedFirmwareConfigs, function (val) {
                val.overwrite = vm.isOverwritten;
            });
        }

        function viewFirmwareConfig(firmwareConfig) {
            $modal.open({
                templateUrl: 'app/xconf/firmware/firmwareconfig/firmwareconfig-view.html',
                controller: 'FirmwareConfigViewController as vm',
                size: 'md',
                resolve : {
                    firmwareConfig: function() {
                        return firmwareConfig;
                    }
                }
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
            return vm.wrappedFirmwareConfigs ? vm.wrappedFirmwareConfigs.length : 0;
        }
    }
})();
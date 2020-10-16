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
        .module('app.firmwareconfig')
        .controller('FirmwareConfigController', controller);

    controller.$inject = ['$rootScope', '$scope', 'firmwareConfigService', 'alertsService', '$uibModal', 'dialogs', 'paginationService', '$controller'];

    function controller($rootScope, $scope, firmwareConfigService, alertsService, $modal, dialogs, paginationService, $controller) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.firmwares = [];
        vm.paginationStorageKey = 'firmwareConfigPageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Model",
                        apiArgs: ["MODEL"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Version",
                        apiArgs: ["FIRMWARE_VERSION"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Description",
                        apiArgs: ["DESCRIPTION"]
                    }
                }
            ]
        };

        vm.deleteFirmwareConfig = deleteFirmwareConfig;
        vm.viewFirmwareConfig = viewFirmwareConfig;
        vm.exportFirmwareConfig = exportById;
        vm.exportAll = exportAll;
        vm.getFirmwareConfigs = getFirmwareConfigs;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.shiftItems = shiftItems;

        init();

        function init() {
            getFirmwareConfigs();
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
            getFirmwareConfigs();
        });

        function getFirmwareConfigs() {
            firmwareConfigService.searchByContext(vm.pageSize, vm.pageNumber, vm.searchParam).then(function(result) {
                vm.firmwares = result.data;
                vm.generalItemsNumber = result.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            },
            function(reason) {
                alertsService.failedToLoadData('firmwareConfigs', reason.data.message);
            });
        }

        function deleteFirmwareConfig(firmwareConfig) {
            var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete FirmwareConfig ' + firmwareConfig.description + ' ? </span>');
            dialog.result.then(function (btn) {
                firmwareConfigService.deleteById(firmwareConfig.id).then(function(resp) {
                    alertsService.successfullyDeleted(firmwareConfig.description);
                    for (var i=0; i < vm.firmwares.length; i++) {
                        if (firmwareConfig.id === vm.firmwares[i].id) {
                            vm.firmwares.splice(i, 1);
                        }
                    }
                    shiftItems();
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
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

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getFirmwareConfigs();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }

        function exportAll() {
            firmwareConfigService.exportAll();
        }

        function exportById(id) {
            firmwareConfigService.exportById(id);
        }
    }
})();
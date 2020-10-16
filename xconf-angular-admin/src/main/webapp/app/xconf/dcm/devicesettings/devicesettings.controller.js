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
/**
 * Created by izluben on 17.10.15.
 */
(function() {
    'use strict';

    angular
        .module('app.devicesettings')
        .controller('DeviceSettingsController', controller);

    controller.$inject = ['$scope', '$uibModal', '$controller', 'deviceSettingsService', 'alertsService', 'dialogs', 'formulaService', 'paginationService', 'deviceSettingsSize'];

    function controller($scope, $modal, $controller, deviceSettingsService, alertsService, dialogs, formulaService, paginationService, deviceSettingsSize) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.deviceSettingsPage = [];
        vm.isSettingsAvailable = {};
        vm.paginationStorageKey = 'deviceSettingsPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.deviceSettingsSize = parseInt(deviceSettingsSize);
        vm.isFormulasAvailable = null;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Name",
                        apiArgs: ['NAME']
                    }
                }
            ]
        };

        vm.viewDeviceSettings = viewDeviceSettings;
        vm.editDeviceSettings = editDeviceSettings;
        vm.deleteDeviceSettings = deleteDeviceSettings;
        vm.viewFormula = viewFormula;
        vm.changePageSize = changePageSize;
        vm.getSize = getSize;
        vm.getDeviceSettingsPage = getDeviceSettingsPage;
        vm.startParse = startParse;

        init();

        function init() {
            getDeviceSettingsPage(vm.pageNumber, vm.pageSize);
        }

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageSizeModel = vm.pageSize;
                paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            getDeviceSettingsPage(vm.pageNumber, vm.pageSize, vm.searchParam);
        });

        function getDeviceSettingsPage(pageNumber, pageSize) {
            deviceSettingsService.getDeviceSettingsPage(pageNumber, pageSize, vm.searchParam).then(
                function(result) {
                    vm.deviceSettingsPage = result.data;
                    vm.deviceSettingsSize = result.headers('numberOfItems');
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                    formulaService.getFormulasAvailability(vm.deviceSettingsPage).then(function(resp) {
                        vm.isFormulasAvailable = resp.data;
                    });
                },
                function(reason) {
                    alertsService.failedToLoadData('deviceSettings', reason.data.message);
                }
            );
        }

        function viewDeviceSettings(id) {
            deviceSettingsService.getDeviceSettings(id).then(
                function(result) {
                    if (result.data) {
                        $modal.open({
                            templateUrl: 'app/xconf/dcm/devicesettings/devicesettings.view.html',
                            size: 'lg',
                            controller: 'DeviceSettingsViewController as vm',
                            resolve: {
                                deviceSettings: function () {
                                    return result.data;
                                }
                            }
                        });
                    }
                },
                function(reason) {
                    alertsService.showError({message: reason.data.message, title: 'Service error'});
                }
            );
        }

        function editDeviceSettings(id) {
            deviceSettingsService.getDeviceSettings(id).then(
                function(result) {
                    $modal.open({
                        templateUrl: 'app/xconf/dcm/devicesettings/devicesettings.edit.html',
                        size: 'lg',
                        controller: 'DeviceSettingsEditController as vm',
                        resolve: {
                            deviceSettings: function () {
                                return result.data;
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
                },
                function(reason) {
                    alertsService.showError({message: reason.data.message, title: 'Service error'});
                }
            );
        }

        function deleteDeviceSettings(deviceSettings) {
            var dlg = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Device Settings ' + deviceSettings.name + "? </span>");
            dlg.result.then(function (btn) {
                deviceSettingsService.deleteDeviceSettings(deviceSettings.id)
                    .then(function (result) {
                        alertsService.showSuccessMessage({message: "Deleted " + deviceSettings.name});
                        shiftItems();
                    }, function (reason) {
                        alertsService.showError({message: reason.data.message, title: "Unable to delete"});
                    });
            });
        }

        function viewFormula(id) {
            formulaService.getById(id).then(function(resp) {
                    $modal.open({
                        templateUrl: 'app/xconf/dcm/formula/formula.modal.view.html',
                        controller: 'FormulaModalViewController as vm',
                        size: 'md',
                        resolve : {
                            formula: function() {
                                return resp.data;
                            }
                        }
                    });
                },
                function(reason) {
                    alertsService.showError({title: 'Error', message: reason.message});
                }
            );
        }

        function shiftItems() {
            vm.deviceSettingsSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.deviceSettingsSize) / vm.pageSize);
            var pageNumber = vm.pageNumber > numberOfPagesAfterDeletion ? numberOfPagesAfterDeletion : vm.pageNumber;
            getDeviceSettingsPage(pageNumber, vm.pageSize);
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getDeviceSettingsPage(vm.pageNumber, vm.pageSize);
        }

        function getSize() {
            return vm.deviceSettingsSize;
        }

        function startParse() {
            return getSize() > 0;
        }
    }
})();
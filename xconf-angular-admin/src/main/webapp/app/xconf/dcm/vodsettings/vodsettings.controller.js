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
        .module('app.vodsettings')
        .controller('VodSettingsController', controller);

    controller.$inject = ['$scope', '$controller', 'vodSettingsService', 'alertsService', 'utilsService', 'dialogs', 'formulaService', '$uibModal', 'vodSettingsSize', 'paginationService'];

    function controller($scope, $controller, vodSettingsService, alertsService, utilsService, dialogs, formulaService, $modal, vodSettingsSize, paginationService) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.vodSettingsPage = [];
        vm.paginationStorageKey = 'vodSettingsPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.vodSettingsSize = parseInt(vodSettingsSize);
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

        vm.getSize = getSize;
        vm.startParse = startParse;
        vm.deleteVodSettings = deleteVodSettings;
        vm.viewFormula = viewFormula;
        vm.changePageSize = changePageSize;
        vm.getVodSettingsPage = getVodSettingsPage;
        vm.editVodSettings = editVodSettings;
        vm.viewVodSettings = viewVodSettings;


        init();

        function init() {
            getVodSettingsPage(vm.pageNumber, vm.pageSize, vm.searchParam);
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
            getVodSettingsPage(vm.pageNumber, vm.pageSize);
        });

        function getVodSettingsPage(pageNumber, pageSize) {
            vodSettingsService.getPage(pageSize, pageNumber, vm.searchParam).then(function(result) {
                vm.vodSettingsPage = result.data;
                vm.vodSettingsSize = result.headers('numberofitems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                formulaService.getFormulasAvailability(vm.vodSettingsPage).then(function(resp) {
                    vm.isFormulasAvailable = resp.data;
                });
            }, function(error) {
                alertsService.showError({title: 'Error', message: 'Error by loading VodSettings'});
            });
        }

        function editVodSettings(vodSettingsId) {
            vodSettingsService.getById(vodSettingsId).then(function(resp) {
                openEditVodSettingsModal(vodSettingsId, resp.data);
            }, function(error) {
                $log.error(error.data);
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

        function deleteVodSettings(vodSettings) {
            if (!vodSettings) {
                alertsService.showError({title: 'Error', message: 'Vod Settings is not present'});
                return;
            }
            var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Vod Settings ' + vodSettings.name + ' ? </span>');
            dialog.result.then(function (btn) {
                vodSettingsService.deleteById(vodSettings.id).then(function(resp) {
                    utilsService.removeItemFromArray(vm.vodSettingsPage, vodSettings);
                    shiftItems();
                    alertsService.successfullyDeleted(vodSettings.name);
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.messsage});
                });
            });
        }

        function viewFormula(id) {
            formulaService.getById(id).then(function(result) {
                if (result) {
                    $modal.open({
                        templateUrl: 'app/xconf/dcm/formula/formula.modal.view.html',
                        controller: 'FormulaModalViewController as vm',
                        size: 'md',
                        resolve : {
                            formula: function() {
                                return result.data;
                            }
                        }
                    });
                } else {
                    alertsService.showError({title: 'Error', message: 'Formula id is not present'});
                }
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.message});
            });
        }

        function viewVodSettings(vodSettingsId) {
            vodSettingsService.getById(vodSettingsId).then(function(result) {
                $modal.open({
                    templateUrl: 'app/xconf/dcm/vodsettings/vodsettings.modal.view.html',
                    controller: 'VodSettingsModalViewController as vm',
                    size: 'md',
                    resolve : {
                        vodSettings: function() {
                            return result.data;
                        }
                    }
                });
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function shiftItems() {
            vm.vodSettingsSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.vodSettingsSize) / vm.pageSize);
            var pageNumber = vm.pageNumber > numberOfPagesAfterDeletion ? numberOfPagesAfterDeletion : vm.pageNumber;
            getVodSettingsPage(pageNumber, vm.pageSize);
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getVodSettingsPage(vm.pageNumber, vm.pageSize);
        }

        function getSize() {
            return vm.vodSettingsSize;
        }

        function startParse() {
            return getSize() > 0;
        }
    }
})();
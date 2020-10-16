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
 * Created: 23.10.15  11:19
 */
(function() {
    'use strict';

    angular
        .module('app.loguploadsettings')
        .controller('LogUploadSettingsController', controller);

    controller.$inject = ['$scope', '$uibModal', '$controller', 'alertsService', 'dialogs', 'utilsService', 'formulaService', 'logUploadSettingsService', 'logUploadSettingsSize', 'paginationService'];

    function controller( $scope, $modal, $controller, alertsService, dialogs, utilsService, formulaService, logUploadSettingsService, logUploadSettingsSize, paginationService) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.logUploadSettingsPage = [];
        vm.paginationStorageKey = 'logUploadSettingsPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.logUploadSettingsSize = parseInt(logUploadSettingsSize);
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

        vm.viewLogUploadSettings = viewLogUploadSettings;
        vm.editLogUploadSettings = editLogUploadSettings;
        vm.deleteLogUploadSettings = deleteLogUploadSettings;
        vm.viewFormula = viewFormula;
        vm.changePageSize = changePageSize;
        vm.getSize = getSize;
        vm.getLogUploadSettingsPage = getLogUploadSettingsPage;
        vm.startParse = startParse;

        init();

        function init() {
            getLogUploadSettingsPage(vm.pageNumber, vm.pageSize, vm.searchParam);
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
            getLogUploadSettingsPage(vm.pageNumber, vm.pageSize);
        });

        function getLogUploadSettingsPage(pageNumber, pageSize) {
            logUploadSettingsService.getLogUploadSettingsPage(pageNumber, pageSize, vm.searchParam).then(
                function(result) {
                    vm.logUploadSettingsPage = result.data;
                    vm.logUploadSettingsSize = result.headers('numberOfItems');
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                    formulaService.getFormulasAvailability(vm.logUploadSettingsPage).then(function(resp) {
                        vm.isFormulasAvailable = resp.data;
                    });
                },
                function(reason) {
                    alertsService.failedToLoadData('logUploadSettingsSettings', reason.data.message);
                }
            );
        }

        function viewLogUploadSettings(id) {
            logUploadSettingsService.getLogUploadSettings(id).then(
                function(result) {
                    if (result.data) {
                        $modal.open({
                            templateUrl: 'app/xconf/dcm/loguploadsettings/loguploadsettings.view.html',
                            size: 'lg',
                            controller: 'LogUploadSettingsViewController as vm',
                            resolve: {
                                logUploadSettings: function () {
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

        function editLogUploadSettings(id) {
            logUploadSettingsService.getLogUploadSettings(id).then(
                function(result) {
                    $modal.open({
                        templateUrl: 'app/xconf/dcm/loguploadsettings/loguploadsettings.edit.html',
                        size: 'lg',
                        controller: 'LogUploadSettingsEditController as vm',
                        resolve: {
                            logUploadSettings: function () {
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

        function deleteLogUploadSettings(logUploadSettings) {
            var dlg = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Log Upload Settings ' + logUploadSettings.name + "? </span>");
            dlg.result.then(function (btn) {
                logUploadSettingsService.deleteLogUploadSettings(logUploadSettings.id)
                    .then(function (result) {
                        alertsService.showSuccessMessage({message: "Deleted " + logUploadSettings.name});
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
                    alertsService.showError({message: reason.message, title: 'Error'});
                }
            );
        }

        function shiftItems() {
            vm.logUploadSettingsSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.logUploadSettingsSize) / vm.pageSize);
            var pageNumber = vm.pageNumber > numberOfPagesAfterDeletion ? numberOfPagesAfterDeletion : vm.pageNumber;
            getLogUploadSettingsPage(pageNumber, vm.pageSize);
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getLogUploadSettingsPage(vm.pageNumber, vm.pageSize);
        }

        function getSize() {
            return vm.logUploadSettingsSize;
        }

        function startParse() {
            return getSize() > 0;
        }
    }
})();
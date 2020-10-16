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
        .controller('FormulasController', controller);

    controller.$inject = ['$scope', '$controller', 'formulaService', '$state', '$q', 'alertsService', 'utilsService', 'dialogs', 'vodSettingsService', '$filter', '$uibModal', 'deviceSettingsService', 'logUploadSettingsService', 'SETTINGS_AVAILABILITY_KEYS', 'paginationService', 'formulasSize', 'ruleHelperService', 'RULE_SEARCH_OPTIONS'];

    function controller($scope, $controller, formulaService, $state, $q, alertsService, utilsService, dialogs, vodSettingsService, $filter, $modal, deviceSettingsService, logUploadSettingsService, SETTINGS_AVAILABILITY_KEYS, paginationService, formulasSize, ruleHelperService, RULE_SEARCH_OPTIONS) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.rules = [];
        vm.SETTINGS_AVAILABILITY_KEYS = SETTINGS_AVAILABILITY_KEYS;
        vm.isSettingsAvailable = {};
        vm.paginationStorageKey = 'dcmFormulasPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.formulasSize = parseInt(formulasSize);
        vm.availablePriorities = [];
        vm.searchParam = {};
        vm.searchOptions = RULE_SEARCH_OPTIONS;

        vm.deleteRule = deleteRule;
        vm.viewVodSettings = viewVodSettings;
        vm.viewDeviceSettings = viewDeviceSettings;
        vm.viewLogUploadSettings = viewLogUploadSettings;
        vm.changePageSize = changePageSize;
        vm.getSize = getSize;
        vm.getFormulas = getFormulas;
        vm.changePriority = changePriority;
        vm.exportFormula = formulaService.exportFormula;
        vm.exportAllFormulas = formulaService.exportAllFormulas;

        init();

        function init() {
            getFormulas(vm.pageNumber, vm.pageSize);
            setAvailablePriorities(getSize());
        }

        function getFormulas() {
            formulaService.getPage(vm.pageSize, vm.pageNumber, vm.searchParam).then(function (result) {
                    vm.rules = result.data;
                    vm.formulasSize = result.headers('numberofitems');
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                    getSettingsAvailability(vm.rules);
                },
                function (reason) {
                    alertsService.failedToLoadData('formulas', reason.data);
                });
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
            getFormulas();
        });

        function getSettingsAvailability(formulas) {
            var formulasIds = _.pluck(formulas, 'id');
            formulaService.getSettingsAvailability(formulasIds).then(function(resp) {
                vm.isSettingsAvailable = resp.data;
            });
        }

        function deleteRule(rule) {
            if(rule && rule.id) {
                var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Formula ' + rule.name + ' ? </span>');
                dialog.result.then(function (btn) {
                    formulaService.deleteFormula(rule.id).then(function () {
                        utilsService.removeItemFromArray(vm.rules, rule);
                        shiftItems();
                        alertsService.successfullyDeleted(rule.name);
                    }, function (error) {
                        alertsService.showError({title: 'Error', message: error.message});
                    });
                });
            }
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

        function viewDeviceSettings(deviceSettingsId) {
            deviceSettingsService.getDeviceSettings(deviceSettingsId).then(
                function(result) {
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
                },
                function(reason) {
                    alertsService.showError({title: 'Error', message: reason.data});
                }
            );
        }

        function viewLogUploadSettings(logUploadSettingsId) {
            logUploadSettingsService.getLogUploadSettings(logUploadSettingsId).then(
                function(result) {
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
                },
                function(reason) {
                    alertsService.showError({title: 'Error', message: reason.data});
                }
            );
        }

        function changePriority(id, priority) {
            formulaService.changePriorities(id, priority).then(function(result){
                init();
            }, function(reason) {
                alertsService.showError({title: 'Error', message: reason.message});
                init();
            });
        }

        function shiftItems() {
            vm.formulasSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.formulasSize) / vm.pageSize);
            var pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getFormulas(pageNumber, vm.pageSize);
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getFormulas(vm.pageNumber, vm.pageSize);
        }

        function getSize() {
            return vm.formulasSize;
        }

        function setAvailablePriorities(size) {
            size = parseInt(size);

            vm.availablePriorities = [];
            for (var i = 1; i < size + 1; i++) {
                vm.availablePriorities.push(i);
            }
        }
    }
})();
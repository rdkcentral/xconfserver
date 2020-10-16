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
        .controller('PercentFilterController', controller);

    controller.$inject = ['$scope', '$controller', 'percentFilterService', 'alertsService', '$uibModal', 'firmwareConfigService', 'percentageBeanService', 'dialogs', 'paginationService', 'RULE_SEARCH_OPTIONS'];

    function controller($scope, $controller, percentFilterService, alertsService, $uibModal, firmwareConfigService, percentageBeanService, dialogs, paginationService, RULE_SEARCH_OPTIONS) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.percentFilter = null;
        vm.firmwareConfigMap = {};
        vm.percentageBeans = [];

        vm.paginationStorageKey = 'percentageBeanPageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.searchParam = {};
        vm.searchOptions = {};
        vm.searchOptionsData = [
                {
                    "name": {
                        friendlyName: "Environment",
                        apiArgs: ["ENVIRONMENT"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Model",
                        apiArgs: ["MODEL"]
                    }
                },
                {
                    "name": {
                        friendlyName: "LKG",
                        apiArgs: ["LAST_KNOWN_GOOD"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Minimum check version",
                        apiArgs: ["MIN_CHECK_VERSION"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Intermediate version",
                        apiArgs: ["INTERMEDIATE_VERSION"]
                    }
                }
            ];

        vm.exportWholeFilter = percentFilterService.exportWholeFilter;
        vm.exportGlobalPercentage = percentFilterService.exportGlobalPercentage;
        vm.exportPercentageBean = percentageBeanService.exportPercentageBean;
        vm.exportAllPercentageBeans = percentageBeanService.exportAllPercentageBeans;
        vm.exportAllPercentageBeansAsRule = percentageBeanService.exportAllPercentageBeansAsRule;
        vm.exportPercentageBeanAsRule = percentageBeanService.exportPercentageBeanAsRule;
        vm.exportGlobalPercentageAsRule = percentFilterService.exportGlobalPercentageAsRule;
        vm.viewPercentageBean = viewPercentageBean;
        vm.deletePercentageBean = deletePercentageBean;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.shiftItems = shiftItems;
        vm.getPercentageBeans = getPercentageBeans;

        init();

        function init() {
            percentFilterService.getFilter().then(function(resp) {
                vm.filter = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
            firmwareConfigService.getFirmwareConfigMap().then(function(resp) {
                vm.firmwareConfigMap = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Exception', message: error.data.message});
            });
            getPercentageBeans();
            buildSearchOptions();
        }

        function buildSearchOptions() {
            vm.searchOptions = angular.copy(RULE_SEARCH_OPTIONS);
            vm.searchOptions.data = vm.searchOptions.data.concat(vm.searchOptionsData);
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
            getPercentageBeans();
        });

        function getPercentageBeans() {
            percentageBeanService.getPage(vm.pageSize, vm.pageNumber, vm.searchParam).then(function(resp) {
                vm.percentageBeans = resp.data;
                vm.generalItemsNumber = resp.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function viewPercentageBean(percentageBean) {
            percentageBeanService.sortPercentageBeanFirmwareVersionsIfExistOrNot(percentageBean).then(function(firmwareVersions) {
                showViewPercentageBean(percentageBean, firmwareVersions);
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function showViewPercentageBean(percentageBean, firmwareVersions) {
            $uibModal.open({
                templateUrl: 'app/xconf/firmware/percentfilter/percentfilter.view.html',
                controller: 'PercentFilterViewController as vm',
                size: 'md',
                resolve : {
                    percentageBean: function() {
                        return percentageBean;
                    },
                    firmwareVersions: function() {
                        return firmwareVersions;
                    },
                    firmwareConfigMap: function() {
                        return vm.firmwareConfigMap;
                    }
                }
            });
        }

        function deletePercentageBean(percentageBean) {
            var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete PercentageBean ' + percentageBean.name + ' ? </span>');
            dialog.result.then(function (btn) {
                percentageBeanService.deleteById(percentageBean.id).then(function(resp) {
                    alertsService.successfullyDeleted(percentageBean.name);
                    for (var i=0; i < vm.percentageBeans.length; i++) {
                        if (percentageBean.id === vm.percentageBeans[i].id) {
                            vm.percentageBeans.splice(i, 1);
                        }
                    }
                    shiftItems();
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            });
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getPercentageBeans();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }
    }
})();
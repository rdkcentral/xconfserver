/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

(function() {
    'use strict';

    angular
        .module('app.activation-version')
        .controller('ActivationVersionController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'activationVersionService', 'utilsService', 'dialogs', 'alertsService', 'paginationService'];

    function controller($rootScope, $scope, $controller, activationVersionService, utilsService, dialogs, alertsService, paginationService) {
        var vm = this;
        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.activationVersions = [];
        vm.paginationStorageKey = 'activationVersionPageSize';
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
                        friendlyName: "Description",
                        apiArgs: ["DESCRIPTION"]
                    }
                },
                {
                    "name": {
                        friendlyName: "PartnerId",
                        apiArgs: ["PARTNER_ID"]
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
                        friendlyName: "Regular Expression",
                        apiArgs: ["REGULAR_EXPRESSION"]
                    }
                }
            ]
        };

        vm.deleteOne = deleteOne;
        vm.exportOne = exportOne;
        vm.exportAll = exportAll;
        vm.getActivationVersions = getActivationVersions;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.shiftItems = shiftItems;

        init();

        function init() {
            getActivationVersions();
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
            getActivationVersions();
        });

        function getActivationVersions() {
            activationVersionService.getPage(vm.pageSize, vm.pageNumber, vm.searchParam).then(function(result) {
                vm.activationVersions = result.data;
                vm.generalItemsNumber = result.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            }, function(reason) {
                alertsService.failedToLoadData('ActivationVersions', reason.data.message);
            });
        }

        function deleteOne(activationVersion) {
            var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete ActivationVersion ' + activationVersion.description + ' ? </span>');
            dialog.result.then(function (btn) {
                activationVersionService.deleteById(activationVersion.id).then(function(resp) {
                    alertsService.successfullyDeleted(activationVersion.description);
                    utilsService.removeItemFromListById(vm.activationVersions, activationVersion.id);
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            });
        }

        function exportAll() {
            activationVersionService.exportAll();
        }

        function exportOne(id) {
            activationVersionService.exportOne(id);
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getActivationVersions();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }
    }
})();
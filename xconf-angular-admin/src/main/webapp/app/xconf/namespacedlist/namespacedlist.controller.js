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
 * Author: rdolomansky
 * Created: 3/30/15
 */

(function() {
    'use strict';

    angular
        .module('app.namespacedlist')
        .controller('NamespacedListsController', controller);

    controller.$inject=['$scope', '$uibModal', '$stateParams', 'dialogs', 'alertsService', 'namespacedListService', 'EDIT_MODE', 'NAMESPACED_LIST_TYPE', 'paginationService'];

    function controller($scope, $modal, $stateParams, dialogs, alertsService, namespacedListService, EDIT_MODE, NAMESPACED_LIST_TYPE, paginationService) {
        var vm = this;

        vm.namespacedLists = [];
        vm.EDIT_MODE = EDIT_MODE;
        vm.NAMESPACED_LIST_TYPE = NAMESPACED_LIST_TYPE;
        vm.currentType = $stateParams.type || vm.NAMESPACED_LIST_TYPE.MAC_LIST;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Name",
                        apiArgs: ['NAME']
                    }
                },
                {
                    "name": {
                        friendlyName: "Data",
                        apiArgs: ["DATA"]
                    }
                }
            ]
        };
        vm.paginationStorageKey = 'namespacedListPageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;

        vm.searchList = searchList;
        vm.getNamespacedLists = getNamespacedLists;
        vm.viewNamespacedList = viewNamespacedList;
        vm.deleteNamespacedList = deleteNamespacedList;
        vm.exportAllNamespacedLists = exportAllNamespacedLists;
        vm.exportNamespacedList = exportNamespacedList;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.shiftItems = shiftItems;
        vm.bulkDelete = bulkDelete;

        init();

        function init() {
            getNamespacedLists();
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
            getNamespacedLists();
        });

        function getNamespacedLists() {
            vm.searchParam.TYPE = vm.currentType;
            namespacedListService.getNamespacedLists(vm.pageNumber, vm.pageSize, vm.searchParam).then(function (result) {
                vm.namespacedLists = result.data;
                vm.generalItemsNumber = result.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            }, function (reason) {
                alertsService.showError({message: reason.data.message, title: 'Error'});
            });
        }

        function viewNamespacedList(id) {
            namespacedListService.getNamespacedList(id)
                .then(function (result) {
                    namespacedListService.sortNamespacedListsData(result.data);
                    var modalInstance = $modal.open({
                        templateUrl: 'app/xconf/namespacedlist/namespacedlist-view.html',
                        size: 'lg',
                        controller: 'NamespacedListViewController as vm',
                        resolve: {
                            namespacedList: function () {
                                return result.data;
                            }
                        }
                    });
                }, function(reason) {
                    alertsService.showError({message: reason.data.message, title: 'Error'});
                });
        }

        function deleteNamespacedList(namespacedList, type) {
            var dlg = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Namespaced List ' + namespacedList.id + "? </span>");
            dlg.result.then(function (btn) {
                namespacedListService.deleteNamespacedList(namespacedList.id, type)
                    .then(function (result) {
                        alertsService.showSuccessMessage({message: "Deleted " + namespacedList.id});
                        shiftItems();
                    }, function (reason) {
                        alertsService.showError({message: reason.data.message, title: 'Error'});
                    });
            });
        }

        function exportAllNamespacedLists(type) {
            return namespacedListService.exportAllNamespacedLists(type);
        }

        function exportNamespacedList(id, type) {
            return namespacedListService.exportNamespacedList(id, type);
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getNamespacedLists();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }

        function searchList(type, searchName, searchData, pageSize, pageNumber) {
            vm.isMacPart = namespacedListService.isMacPart(searchData);
            if (vm.currentType ===  vm.NAMESPACED_LIST_TYPE.IP_LIST || vm.isMacPart || searchData === '' || searchName === '') {
                namespacedListService.searchList(type, searchName, searchData, pageSize, pageNumber).then(function (resp) {
                    vm.namespacedLists = resp.data;
                    vm.generalItemsNumber = resp.headers('numberOfItems');
                    paginationService.savePaginationSettingsInLocation(pageNumber, pageSize);
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function bulkDelete(id) {
            namespacedListService.getNamespacedList(id).then(function (result) {
                namespacedListService.sortNamespacedListsData(result.data);
                var option = {
                    templateUrl: 'app/xconf/namespacedlist/namespacedlist-bulk-delete.view.html',
                    size: 'md',
                    controller: 'NamespacedListBulkDeleteController as vm',
                    resolve: {
                        namespacedList: function () {
                            return result.data;
                        }, shiftItems: function() {
                            return shiftItems;
                        }, updateItems: function() {
                            return getNamespacedLists;
                        }
                    }
                };
                $modal.open(option);
            }, function(reason) {
                alertsService.showError({message: reason.data.message, title: data.type});
            });
        }
    }
})();
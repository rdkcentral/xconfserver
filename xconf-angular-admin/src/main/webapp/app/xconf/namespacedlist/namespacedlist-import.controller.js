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
        .controller('NamespacedListImportController', controller);

    controller.$inject=['$scope', '$log', '$uibModal', '$stateParams', 'alertsService', 'namespacedListService', 'importService', 'utilsService', 'NAMESPACED_LIST_TYPE', 'paginationService', 'ENTITY_TYPE'];

    function controller($scope, $log, $modal, $stateParams, alertsService, namespacedListService, importService, utilsService, NAMESPACED_LIST_TYPE, paginationService, ENTITY_TYPE) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.viewNamespacedList = viewNamespacedList;
        vm.importNamespacedList = importNamespacedList;
        vm.importAllNamespacedLists = importAllNamespacedLists;
        vm.namespacedLists = null;
        vm.wrappedNamespacedLists = null;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.NAMESPACED_LIST_TYPE = NAMESPACED_LIST_TYPE;
        vm.currentType = $stateParams.type;
        vm.paginationStorageKey = 'namespacedListPageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.progressBarControl = importService.progressBarControl;
        vm.errorMessagesById = {};

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        async function retrieveFile(fileName) {
            vm.namespacedLists = null;
            vm.wrappedNamespacedLists = null;
            vm.errorMessagesById = {};
            try {
                let file = await importService.openFile(fileName, null, this);
                processFile(file);
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function processFile(file) {
            vm.namespacedLists = getNamespacedListsFromFile(file);
            if (vm.namespacedLists) {
                namespacedListService.sortNamespacedListsData(vm.namespacedLists);
                utilsService.sortObjectsById(vm.namespacedLists);
                vm.wrappedNamespacedLists = importService.wrapToImport(vm.namespacedLists);
            }
            vm.isOverwritten = false;
            selectPage();
        }

        function getNamespacedListsFromFile(data) {
            try {
                var namespacedLists = JSON.parse(data);
                vm.mainErrorMessage = '';
                if (vm.currentType === NAMESPACED_LIST_TYPE.MAC_LIST) {
                    namespacedLists = _.map(namespacedLists, function(namespacedList) {
                        namespacedList.data = _.map(namespacedList.data, function(mac) {
                            return namespacedListService.normalizeMacAddress(mac);
                        });
                        return namespacedList;
                    });
                }
                if (validateNamespacedList(namespacedLists, vm.currentType)) {
                    vm.errorMessagesById = {};
                    return namespacedLists;
                }
            } catch(e) {
                vm.mainErrorMessage = 'Namespaced list JSON has some errors! Please, check this file!';
                $log.error('error', e);
            }
        }

        function importNamespacedList(namespacedList) {
            if (namespacedList.overwrite) {
                namespacedListService.updateNamespacedList(namespacedList.entity, namespacedList.entity.id).then(function () {
                    alertsService.successfullySaved(namespacedList.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.wrappedNamespacedLists, namespacedList.entity.id);
                    clearErrorByEntityId(namespacedList.entity.id);
                }, function (reason) {
                    vm.errorMessagesById[namespacedList.entity.id] = reason.data.message;
                });
            } else {
                namespacedListService.createNamespacedList(namespacedList.entity).then(function () {
                    alertsService.successfullySaved(namespacedList.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.wrappedNamespacedLists, namespacedList.entity.id);
                    clearErrorByEntityId(namespacedList.entity.id);
                }, function (reason) {
                    vm.errorMessagesById[namespacedList.entity.id] = reason.data.message;
                });
            }
        }

        function importAllNamespacedLists(type) {
            importService.importAllEntities(namespacedListService, vm.wrappedNamespacedLists, null, null, ENTITY_TYPE.NS_LIST);
        }

        function viewNamespacedList(namespacedList) {
            var modalInstance = $modal.open({
                templateUrl: 'app/xconf/namespacedlist/namespacedlist-view.html',
                size: 'lg',
                controller: 'NamespacedListViewController as vm',
                resolve: {
                    namespacedList: function () {
                        return namespacedList;
                    }
                }
            });
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedNamespacedLists, function (val) {
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
            return vm.wrappedNamespacedLists ? vm.wrappedNamespacedLists.length : 0;
        }

        $scope.$on("import::error", function(event, data) {
            vm.errorMessagesById[data.id] = data.message;
        });

        function clearErrorByEntityId(id) {
            if (id && vm.errorMessagesById[id]) {
                delete vm.errorMessagesById[id];
            }
        }

        function validateNamespacedList(namespacedLists, type) {
            if(angular.isArray(namespacedLists[0].data)) {
                var listItem = namespacedLists[0].data[0];
                if(type === NAMESPACED_LIST_TYPE.IP_LIST && !namespacedListService.isValidIpAddress(listItem)) {
                    vm.mainErrorMessage = 'Invalid data, import file is not an IpList';
                    return false;
                }
                if ((type === NAMESPACED_LIST_TYPE.MAC_LIST || type === NAMESPACED_LIST_TYPE.RI_MAC_LIST) && !namespacedListService.isMacAddress(listItem)) {
                    vm.mainErrorMessage = 'Invalid data, import file is not a MacList';
                    return false;
                }
            }

            var missingFields = [];
            var i = 0;
            while(i < namespacedLists.length) {
                if (!namespacedLists[i].id) {
                    missingFields.push('id');
                }
                if (!namespacedLists[i].data || !angular.isArray(namespacedLists[i].data)) {
                    missingFields.push('data');
                }

                if (missingFields.length > 0) {
                    var errorMessage = 'Namespaced list JSON file is invalid! Next fields are missing: ' + missingFields.join(', ') + '. Please, check it!';
                    vm.mainErrorMessage =  errorMessage;
                    $log.error(errorMessage);
                    return false;
                }
                i++;
            }
            return true;
        }
    }
})();
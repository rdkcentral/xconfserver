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
        .controller('NamespacedListEditController', controller);

    controller.$inject=['$scope', '$stateParams', '$state', 'utilsService', 'alertsService', 'namespacedListService', 'EDIT_MODE', 'NAMESPACED_LIST_TYPE', 'importService'];

    function controller($scope, $stateParams, $state, utilsService, alertsService, namespacedListService, EDIT_MODE, NAMESPACED_LIST_TYPE, importService) {
        var vm = this;

        vm.namespacedList = {
            id: '',
            typeName: '',
            data: []
        };
        vm.dataItemValue = '';
        vm.dataItemValueError = '';
        vm.EDIT_MODE = EDIT_MODE;
        vm.currentEditMode = $stateParams.editMode || vm.EDIT_MODE.CREATE;
        vm.itemsForRemoving = [];
        vm.newlyAddedItems = [];
        vm.initialData = [];
        vm.copiedNamespacedListId = '';
        vm.NAMESPACED_LIST_TYPE = NAMESPACED_LIST_TYPE;
        vm.currentType = $stateParams.type;
        vm.namespacedList.typeName = vm.currentType;
        vm.newId = null;
        vm.dataFromFile = [];

        vm.validateDataItemValue = validateDataItemValue;
        vm.addItemToData = addItemToData;
        vm.removeItemFromData = removeItemFromData;
        vm.saveNamespacedList = saveNamespacedList;
        vm.isItemForRemoving = isItemForRemoving;
        vm.isNewlyAddedItem = isNewlyAddedItem;
        vm.removeOrMarkAsItemForRemoving = removeOrMarkAsItemForRemoving;
        vm.restore = restore;
        vm.retrieveNamespacedListDataFromFile = retrieveNamespacedListDataFromFile;
        vm.addDataFromFile = addDataFromFile;
        vm.replaceDataFromFile = replaceDataFromFile;

        init();

        function init() {
            var id = $stateParams.id;
            if (vm.currentEditMode === vm.EDIT_MODE.UPDATE) {
                getNamespacedList(id);
            }
        }

        function getNamespacedList(id) {
            namespacedListService.getNamespacedList(id)
                .then(function (result) {
                    vm.namespacedList = result.data;
                    namespacedListService.sortNamespacedListsData(vm.namespacedList);
                    angular.copy(result.data.data, vm.initialData);
                    vm.newId = vm.namespacedList.id;
                }, function (reason) {
                    alertsService.showError({message: reason.data.message, title: 'Error'});
                });
        }

        function addItemToData() {
            if (vm.currentType == NAMESPACED_LIST_TYPE.MAC_LIST || vm.currentType == NAMESPACED_LIST_TYPE.RI_MAC_LIST) {
                vm.dataItemValue = namespacedListService.normalizeMacAddress(vm.dataItemValue);
            }
            if(validateDataItemValue(vm.dataItemValue)) {
                vm.namespacedList.data.push(vm.dataItemValue);
                vm.newlyAddedItems.push(vm.dataItemValue);
                vm.dataItemValue = '';
                vm.dataItemValueError = '';
            }
        }

        function removeItemFromData(value) {
            var array = vm.namespacedList.data;
            if (utilsService.removeItemFromArray(array, value) >= 0) {
                vm.dataItemValue = '';
            }
            utilsService.removeItemFromArray(vm.newlyAddedItems, value);
        }

        /**
         * removes item right away if it was added to namespacedlist during this editing
         * otherwise adds item to itemsForRemoving list
         * @param value
         */
        function removeOrMarkAsItemForRemoving(value) {
            if(vm.initialData.indexOf(value) > -1) {
                vm.itemsForRemoving.push(value);
            } else {
                removeItemFromData(value);
            }
        }

        function isItemForRemoving(value) {
            if(vm.itemsForRemoving.indexOf(value) > -1)
                return true;
            return false;
        }

        function isNewlyAddedItem(value) {
            if(vm.newlyAddedItems.indexOf(value) > -1)
                return true;
            return false;
        }

        function restore(value) {
            utilsService.removeItemFromArray(vm.itemsForRemoving, value);
        }

        function validateDataItemValue(value) {
            if (!utilsService.isEmptyString(value)) {
                if (vm.namespacedList && vm.namespacedList.data.indexOf(value) != -1) {
                    vm.dataItemValueError = 'Item "' + value + '" already exists';
                    return false;
                }
                if(!namespacedListService.isMacAddress(value) && (isCurrentTypeMacAddress() || isCurrentTypeRIMacAddress())) {
                    vm.dataItemValueError = 'Item "' + value + '" must be MAC address';
                    return false;
                }
                if(!namespacedListService.isValidIpAddress(value) && isCurrentTypeIpAddress()) {
                    vm.dataItemValueError = 'Item "' + value + '" must be ipv4 or ipv6 address';
                    return false;
                }
            }
            vm.dataItemValueError = '';

            return true;
        }

        function validateId(id) {
            var idRegEx = new RegExp("^[-a-zA-Z0-9_.' ]+$");
            return idRegEx.test(id);
        }

        function saveNamespacedList() {
            if (vm.currentEditMode === EDIT_MODE.CREATE) {
                createNamespacedList();
            } else {
                updateNamespacedList();
            }
        }

        function createNamespacedList() {
            namespacedListService.createNamespacedList(vm.namespacedList)
                .then(function (resp) {
                    alertsService.successfullySaved(resp.data.id);
                    $state.go('namespacedlist', {type: vm.currentType});
                }, function (reason) {
                    handleDataError(reason);
                });
        }

        function updateNamespacedList() {
            var dataBeforeUpdate = angular.copy(vm.namespacedList.data);
            utilsService.removeMultipleItemsFromArray(vm.namespacedList.data, vm.itemsForRemoving);
            if (!validateId(vm.newId)) {
                alertsService.showError({title: 'Error', message: 'Name is invalid'});
                return;
            }
            namespacedListService.updateNamespacedList(vm.namespacedList, vm.newId)
                .then(function (resp) {
                    alertsService.successfullySaved(resp.data.id);
                    $state.go('namespacedlist', {type: vm.currentType});
                }, function (reason) {
                    vm.namespacedList.data = dataBeforeUpdate;
                    handleDataError(reason);
                });
        }

        function isCurrentTypeMacAddress() {
            return vm.currentType === vm.NAMESPACED_LIST_TYPE.MAC_LIST;
        }

        function isCurrentTypeIpAddress() {
            return vm.currentType === vm.NAMESPACED_LIST_TYPE.IP_LIST;
        }

        function isCurrentTypeRIMacAddress() {
            return vm.currentType === vm.NAMESPACED_LIST_TYPE.RI_MAC_LIST;
        }

        function retrieveNamespacedListDataFromFile(fileName) {
            importService.openFile(fileName, null, this).then(function (result) {
                var dataFromFile = result.match(/[^\r\n]+/g);
                var dataByListType = namespacedListService.filterNamespacedListDataFromFile(dataFromFile, vm.currentType);
                vm.dataItemValueError = namespacedListService.validateDataFromFile(dataFromFile, dataByListType);
                if (vm.dataItemValueError === '') {
                    if (vm.currentType === NAMESPACED_LIST_TYPE.MAC_LIST) {
                        vm.dataFromFile = _.map(dataByListType, function (mac) {
                            return namespacedListService.normalizeMacAddress(mac);
                        });
                    } else {
                        vm.dataFromFile = dataByListType;
                    }
                }
            }, function (reason) {
                alertsService.showError({message: reason.data.message, title: 'Error'});
            });
        }

        function addDataFromFile() {
            var itemsForAdding = _.difference(vm.dataFromFile, vm.namespacedList.data);
            vm.newlyAddedItems = vm.newlyAddedItems.concat(itemsForAdding);
            vm.namespacedList.data = vm.namespacedList.data.concat(itemsForAdding);
            vm.dataFromFile = [];
            vm.dataItemValueError = '';
        }

        function replaceDataFromFile() {
            var itemsForRemoving = _.difference(vm.namespacedList.data, vm.dataFromFile);
            var itemsForAdding = _.difference(vm.dataFromFile, vm.namespacedList.data);
            itemsForRemoving.forEach(function(item) {
                removeOrMarkAsItemForRemoving(item);
            });
            vm.newlyAddedItems = vm.newlyAddedItems.concat(itemsForAdding);
            vm.namespacedList.data = vm.namespacedList.data.concat(itemsForAdding);
            vm.dataFromFile = [];
            vm.dataItemValueError = '';
        }

        function handleDataError(reason) {
            var errorMessage = reason.data.message;
            if (errorMessage.includes('address')) {
                vm.dataItemValueError = errorMessage;
            } else {
                alertsService.showError({title: 'Error', message: errorMessage});
            }
        }
    }
})();
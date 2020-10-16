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
angular
    .module('app.namespacedlist')
    .controller('NamespacedListBulkDeleteController', controller);

controller.$inject = ['$uibModalInstance', 'importService', 'namespacedList', 'shiftItems', 'updateItems', 'namespacedListService', 'alertsService'];

function controller($modalInstance, importService, namespacedList, shiftItems, updateItems, namespacedListService, alertsService) {
    var vm = this;
    vm.isFileLoaded = false;
    vm.namespacedList = namespacedList;
    vm.matchedData = [];
    vm.existedNotMatchedData = [];

    vm.retrieveFile = retrieveFile;
    vm.updateNamespaceList = updateNamespaceList;
    vm.deleteNamespacedList = deleteNamespacedList;
    vm.closeModal = closeModal;

    async function retrieveFile(fileName) {
        try {
            let file = await importService.openFile(fileName, null, this);
            processFile(file);
        } catch (e) {
            alertsService.showError({message: e});
        }

    }

    function processFile(file) {
        let entityNames = parseFile(file);
        vm.namespacedList.data.forEach(function(entityName) {
            if (entityNames.indexOf(entityName) !== -1) {
                vm.matchedData.push(entityName);
            } else {
                vm.existedNotMatchedData.push(entityName);
            }
        });
        vm.isFileLoaded = true;
    }

    function parseFile(result) {
        var uniqueEntityNames = [];
        result.split(/\n|,/).forEach(function(entityName) {
            entityName = entityName.trim()
            if (uniqueEntityNames.indexOf(entityName) === -1) {
                uniqueEntityNames.push(entityName);
            }
        });
        return uniqueEntityNames;
    }

    function deleteNamespacedList() {
        namespacedListService.deleteNamespacedList(vm.namespacedList.id).then(function (result) {
            alertsService.showSuccessMessage({message: "Namespaced List deleted successfully"});
            $modalInstance.dismiss('close');
            shiftItems();
        }, function (reason) {
            alertsService.showError({message: reason.data.message, title: reason.data.type});
            $modalInstance.dismiss('close');
        });
     }

    function updateNamespaceList() {
        vm.namespacedList.data = vm.existedNotMatchedData;
        namespacedListService.updateNamespacedList(vm.namespacedList, vm.namespacedList.id).then(function () {
            alertsService.showSuccessMessage({message: 'Namespaced List ' + vm.namespacedList.id +  ' updated successfully'});
            $modalInstance.dismiss('close');
            updateItems();
        }, function (reason) {
            alertsService.showError({message: reason.data.message, title: reason.data.type});
            $modalInstance.dismiss('close');
        });
    }

    function closeModal() {
        $modalInstance.dismiss('close');
    }
}
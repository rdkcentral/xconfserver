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
        .controller('ActivationVersionImportController', controller);

    controller.$inject=['$rootScope', '$scope', '$log', 'alertsService', 'utilsService', 'importService', 'firmwareConfigService', '$uibModal', 'paginationService', 'authUtilsService', 'PERMISSION', 'activationVersionService'];

    function controller($rootScope, $scope, $log, alertsService, utilsService, importService, firmwareConfigService, $modal, paginationService, authUtils, PERMISSION, activationVersionService) {
        var vm = this;

        vm.activationVersions = null;
        vm.wrappedActivationVersions = null;
        vm.isOverwritten = false;
        vm.PERMISSION = PERMISSION;
        vm.paginationStorageKey = 'activationVersionPageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();

        vm.retrieveFile = retrieveFile;
        vm.importOne = importOne;
        vm.importAll = importAll;
        vm.overwriteAll = overwriteAll;
        vm.progressBarControl = importService.progressBarControl;
        vm.authUtils = authUtils;
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        async function retrieveFile(fileName) {
            vm.activationVersions = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedActivationVersions = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importOne(wrappedActivationVersion) {
            if (!wrappedActivationVersion.entity.applicationType) {
                wrappedActivationVersion.entity.applicationType = $rootScope.applicationType;
            }
            if (wrappedActivationVersion.overwrite) {
                activationVersionService.update(wrappedActivationVersion.entity).then(function () {
                    alertsService.successfullySaved(wrappedActivationVersion.entity.description);
                    utilsService.removeSelectedItemFromListById(vm.wrappedActivationVersions, wrappedActivationVersion.entity.id);
                }, function (error) {
                    alertsService.showError({message: error.data.message, title: 'Exception'});
                });
            } else {
                activationVersionService.create(wrappedActivationVersion.entity).then(function () {
                    alertsService.successfullySaved(wrappedActivationVersion.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.wrappedActivationVersions, wrappedActivationVersion.entity.id);
                }, function (error) {
                    alertsService.showError({message: error.data.message, title: 'Exception'});
                });
            }
        }

        function importAll() {
            importService.importAllEntities(activationVersionService, vm.wrappedActivationVersions);
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedActivationVersions, function (val) {
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
            return vm.wrappedActivationVersions ? vm.wrappedActivationVersions.length : 0;
        }
    }
})();
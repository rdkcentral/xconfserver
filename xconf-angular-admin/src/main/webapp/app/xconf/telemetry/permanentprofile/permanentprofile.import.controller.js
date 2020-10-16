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
 *  Author: mdolina
 *  Created: 11/25/15 12:59 PM
 */

(function() {
    'use strict';

    angular
        .module('app.permanentprofile')
        .controller('PermanentProfileImportController', controller);

    controller.$inject=['$scope', '$log', '$uibModal', 'alertsService', 'utilsService', 'importService', 'permanentProfileService', 'paginationService'];

    function controller($scope, $log, $modal, alertsService, utilsService, importService, permanentProfileService, paginationService) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importPermanentProfile = importPermanentProfile;
        vm.importAllPermanentProfiles = importAllPermanentProfiles;
        vm.permanentProfiles = null;
        vm.wrappedPermanentProfiles = null;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.viewPermanentProfile = viewPermanentProfile;
        vm.paginationStorageKey = 'permanentProfilePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.progressBarControl = importService.progressBarControl;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        async function retrieveFile(fileName) {
            vm.permanentProfiles = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedPermanentProfiles = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importPermanentProfile(wrappedPermanentProfile) {
            if (wrappedPermanentProfile.overwrite) {
                permanentProfileService.updateProfile(wrappedPermanentProfile.entity)
                    .then(function (resp) {
                        handleSuccessfulUpdate(resp, wrappedPermanentProfile.entity);
                        utilsService.removeSelectedItemFromListById(vm.wrappedPermanentProfiles, wrappedPermanentProfile.entity.id);
                    }, function (error) {
                        alertsService.showError({message: error.data.message, title: 'Exception'});
                    });
            } else {
                permanentProfileService.createProfile(wrappedPermanentProfile.entity)
                    .then(function () {
                        alertsService.showSuccessMessage({message: wrappedPermanentProfile.entity['telemetryProfile:name'] + ' profile added to the pending changes'});
                        utilsService.removeSelectedItemFromListById(vm.wrappedPermanentProfiles, wrappedPermanentProfile.entity.id);
                    }, function (error) {
                        alertsService.showError({message: error.data.message, title: 'Exception'});
                    });
            }
        }

        function importAllPermanentProfiles() {
            importService.importAllEntities(permanentProfileService, vm.wrappedPermanentProfiles);
        }

        function viewPermanentProfile(permanentProfile) {
            $modal.open({
                controller: 'PermanentProfileViewController as vm',
                templateUrl: 'app/xconf/telemetry/permanentprofile/permanentprofile.view.html',
                size: 'md',
                resolve: {
                    permanentProfile: function () {
                        return permanentProfile;
                    }
                }
            });
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedPermanentProfiles, function (val) {
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
            return vm.wrappedPermanentProfiles ? vm.wrappedPermanentProfiles.length : 0;
        }

        function handleSuccessfulUpdate(response, profile) {
            var addedToPending = response.data;
            if (addedToPending) {
                alertsService.showSuccessMessage({message: profile['telemetryProfile:name'] + ' profile added to the pending changes'});
            } else {
                alertsService.showSuccessMessage({message: profile['telemetryProfile:name'] + ' profile updated'});
            }
        }
    }
})();
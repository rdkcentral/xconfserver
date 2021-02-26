/* 
 * If not stated otherwise in this file or this component's Licenses.txt file the 
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
        .module('app.telemetrytwoprofile')
        .controller('TelemetryTwoProfileImportController', controller);

    controller.$inject=['$scope', '$log', '$uibModal', 'alertsService', 'utilsService', 'importService', 'telemetryTwoProfileService', 'paginationService'];

    function controller($scope, $log, $modal, alertsService, utilsService, importService, telemetryTwoProfileService, paginationService) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importProfile = importProfile;
        vm.importAllProfiles = importAllProfiles;
        vm.telemetryProfiles = null;
        vm.wrappedProfiles = null;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.viewTelemetryTwoProfile = viewTelemetryTwoProfile;
        vm.paginationStorageKey = 'telemetryTwoProfilePageSize';
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
            vm.telemetryProfiles = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedProfiles = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importProfile(wrappedProfile) {
            if (wrappedProfile.overwrite) {
                telemetryTwoProfileService.updateTelemetryTwoProfile(wrappedProfile.entity)
                    .then(function (resp) {
                        handleSuccessfulUpdate(resp, wrappedProfile.entity);
                        utilsService.removeSelectedItemFromListById(vm.wrappedProfiles, wrappedProfile.entity.id);
                    }, alertsService.errorHandler);
            } else {
                telemetryTwoProfileService.createTelemetryTwoProfile(wrappedProfile.entity)
                    .then(function () {
                        alertsService.showSuccessMessage({message: wrappedProfile.entity.name + ' profile saved'});
                        utilsService.removeSelectedItemFromListById(vm.wrappedProfiles, wrappedProfile.entity.id);
                    }, alertsService.errorHandler);
            }
        }

        function importAllProfiles() {
            importService.importAllEntities(telemetryTwoProfileService, vm.wrappedProfiles);
        }

        function viewTelemetryTwoProfile(profile) {
            $modal.open({
                templateUrl: 'app/xconf/telemetry/telemetrytwoprofile/telemetrytwoprofile.view.html',
                controller: 'TelemetryTwoProfileViewController as vm',
                size: 'lg',
                resolve : {
                    profile: function() {
                        return profile;
                    }
                }
            });
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedProfiles, function (val) {
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
            return vm.wrappedProfiles ? vm.wrappedProfiles.length : 0;
        }

        function handleSuccessfulUpdate(response, profile) {
            let addedToPending = response.data;
            if (addedToPending) {
                alertsService.showSuccessMessage({message: profile.name + ' profile saved'});
            } else {
                alertsService.showSuccessMessage({message: profile.name + ' profile updated'});
            }
        }
    }
})();
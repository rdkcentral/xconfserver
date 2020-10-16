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
 * Author: Igor Kostrov
 * Created: 3/21/2016
 */
(function () {
    'use strict';

    angular
        .module('app.settingprofile')
        .controller('SettingProfileImportController', controller);

    controller.$inject = ['$scope', '$log', 'alertsService', 'utilsService', 'importService', 'settingProfileService', 'paginationService'];

    function controller($scope, $log, alertsService, utilsService, importService, settingProfileService, paginationService) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importSettingProfile = importSettingProfile;
        vm.importAllSettingProfiles = importAllSettingProfiles;
        vm.settingProfiles = null;
        vm.wrappedSettingProfiles = null;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.progressBarControl = importService.progressBarControl;

        vm.paginationStorageKey = 'settingProfilePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
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
            vm.settingProfiles = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedSettingProfiles = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importSettingProfile(wrappedSettingProfile) {
            var promise = wrappedSettingProfile.overwrite ?
                settingProfileService.updateProfile(wrappedSettingProfile.entity) :
                settingProfileService.createProfile(wrappedSettingProfile.entity);
            promise.then(function () {
                alertsService.successfullySaved(wrappedSettingProfile.entity.settingProfileId);
                utilsService.removeSelectedItemFromListById(vm.wrappedSettingProfiles, wrappedSettingProfile.entity.id);
            }, function (error) {
                alertsService.showError({message: error.data.message, title: 'Exception'});
            });
        }

        function importAllSettingProfiles() {
            importService.importAllEntities(settingProfileService, vm.wrappedSettingProfiles);
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedSettingProfiles, function (val) {
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
            return vm.wrappedSettingProfiles ? vm.wrappedSettingProfiles.length : 0;
        }

    }
})();
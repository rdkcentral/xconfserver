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
        .module('app.telemetrytwoprofile')
        .controller('TelemetryTwoProfilesController', controller);

    controller.$inject = ['$scope', '$controller', 'telemetryTwoProfileService', 'alertsService', 'utilsService', 'dialogs', '$log', 'paginationService', '$uibModal'];

    function controller($scope, $controller, telemetryTwoProfileService, alertsService, utilsService, dialogs, $log, paginationService, $modal) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.telemetryTwoProfiles = [];
        vm.paginationStorageKey = 'telemetryTwoProfilePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Name",
                        apiArgs: ['NAME']
                    }
                }
            ]
        };

        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.deleteTelemetryTwoProfile = deleteTelemetryTwoProfile;
        vm.getTelemetryTwoProfiles = getTelemetryTwoProfiles;
        vm.exportOne = telemetryTwoProfileService.exportOne;
        vm.exportAll = telemetryTwoProfileService.exportAll
        vm.viewTelemetryTwoProfile = viewTelemetryTwoProfile;

        init();

        function init() {
            getTelemetryTwoProfiles();
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
            getTelemetryTwoProfiles();
        });

        function getTelemetryTwoProfiles() {
            telemetryTwoProfileService.getTelemetryTwoProfiles(vm.pageNumber, vm.pageSize, vm.searchParam).then(function(result) {
                vm.telemetryTwoProfiles = result.data;
                vm.generalItemsNumber = result.headers.length;
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            },
            function(error) {
                alertsService.showError({title: 'Error', message: 'Error by loading Telemetry 2.0 profiles'});
            });
        }

        function deleteTelemetryTwoProfile(profile) {
            if (profile.id) {
                var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete ' + profile.name + ' Telemetry 2.0 Profile? </span>');
                dialog.result.then(function (btn) {
                    telemetryTwoProfileService.deleteTelemetryTwoProfile(profile.id)
                        .then(function() {
                            utilsService.removeItemFromArray(vm.telemetryTwoProfiles, profile);
                            alertsService.successfullyDeleted(profile.id);
                            shiftItems();
                        }, function(error) {
                            alertsService.showError({title: 'Error', message: error.data.message});
                        });
                });
            }
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

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((getGeneralItemsNumber() - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getTelemetryTwoProfiles();
        }

        function startParse() {
            return getGeneralItemsNumber() > 0;
        }

        function getGeneralItemsNumber() {
            return vm.generalItemsNumber;
        }
    }
})();
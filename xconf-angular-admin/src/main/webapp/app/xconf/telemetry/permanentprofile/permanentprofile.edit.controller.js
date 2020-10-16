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
        .module('app.permanentprofile')
        .controller('PermanentProfileEditController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'PROTOCOL', 'permanentProfileService', '$stateParams', '$state', 'alertsService', 'utilsService'];

    function controller($rootScope, $scope, $controller, PROTOCOL, permanentProfileService, $stateParams, $state, alertsService, utilsService) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'permanentprofiles',
            stateParameters: null
        }));

        vm.protocols = PROTOCOL;
        vm.profile = null;
        vm.isNewProfile = true;

        vm.addProfileEntry = addProfileEntry;
        vm.removeProfileEntry = removeProfileEntry;
        vm.save = save;
        init();

        function init() {
            if ($stateParams.profileId) {
                vm.isNewProfile = false;
                permanentProfileService.getProfile($stateParams.profileId)
                    .then(function(resp) {
                            if (resp) {
                                vm.profile = resp.data;
                            }
                        }, alertsService.errorHandler);
            } else {
                vm.isNewProfile = true;
                vm.profile = {
                    applicationType: $rootScope.applicationType,
                    schedule: '',
                    'telemetryProfile:name': '',
                    'uploadRepository:URL': '',
                    'uploadRepository:uploadProtocol': '',
                    telemetryProfile: [{
                        content: '',
                        header: '',
                        pollingFrequency: '',
                        type: '',
                        component: ''
                    }]
                }
            }
        }

        function addProfileEntry() {
            var telemetryProfile = {
                content: '',
                header: '',
                pollingFrequency: '',
                type: '',
                component:''
            };
            vm.profile.telemetryProfile.push(telemetryProfile);
        }

        function removeProfileEntry(entry) {
            utilsService.removeItemFromArray(vm.profile.telemetryProfile, entry);
        }

        function save() {
            if (isValid(vm.profile)) {
                if (vm.isNewProfile) {
                    permanentProfileService.createProfile(vm.profile).then(handleSuccessfulResponse, alertsService.errorHandler);
                } else {
                    permanentProfileService.updateProfile(vm.profile).then(handleSuccessfulResponse, alertsService.errorHandler);
                }
            }
        }

        function isValid(profile) {
            var missingFields = [];
            var missingTelemetryEntryFields = [];
            if (!profile['telemetryProfile:name']) {
                missingFields.push('name');
            }
            if (!profile['uploadRepository:URL']) {
                missingFields.push('upload repository URL');
            }
            if (!profile['uploadRepository:uploadProtocol']) {
                missingFields.push('upload repository protocol');
            }
            if (!profile.schedule) {
                missingFields.push('schedule');
            }
            if (!profile.telemetryProfile) {
                missingFields.push('telemetryProfile');
            } else {
                for (var i = 0; i < profile.telemetryProfile.length; i++) {
                    if(!profile.telemetryProfile[i].content) {
                        missingTelemetryEntryFields.push('content');
                    }
                    if (!profile.telemetryProfile[i].header) {
                        missingTelemetryEntryFields.push('header');
                    }
                    if (!profile.telemetryProfile[i].type) {
                        missingTelemetryEntryFields.push('type');
                    }
                    if (!profile.telemetryProfile[i].pollingFrequency) {
                        missingTelemetryEntryFields.push('polling frequency');
                    }
                    if (missingTelemetryEntryFields.length > 0) {
                        break;
                    }
                }
            }
            if (missingFields.length > 0 || missingTelemetryEntryFields.length > 0) {
                missingFields = missingFields.concat(missingTelemetryEntryFields);
                alertsService.showError({title: 'Error', message: 'Next fields are missing: ' + missingFields.join(", ")});
                return false;
            }

            return true;
        }

        function handleSuccessfulResponse(response) {
            var addedToPending = response.data;
            if (addedToPending) {
                alertsService.showSuccessMessage({message: vm.profile['telemetryProfile:name'] + ' profile added to the pending changes'});
            } else {
                alertsService.showSuccessMessage({message: vm.profile['telemetryProfile:name'] + ' profile updated'});
            }
            $state.go('permanentprofiles');
        }
    }
})();
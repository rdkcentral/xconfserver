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
 * Created: 3/18/2016
 */
(function () {
    'use strict';

    angular
        .module('app.settingprofile')
        .controller('SettingProfileEditController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'SETTING_TYPE', 'settingProfileService', '$stateParams', '$state', 'alertsService', 'utilsService'];

    function controller($rootScope, $scope, $controller, SETTING_TYPE, settingProfileService, $stateParams, $state, alertsService, utilsService) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'settingprofiles',
            stateParameters: null
        }));

        vm.settingTypes = SETTING_TYPE;
        vm.profile = null;
        vm.isNewProfile = true;

        vm.addProfileEntry = addProfileEntry;
        vm.removeProfileEntry = removeProfileEntry;
        vm.save = save;
        init();

        function init() {
            if ($stateParams.profileId) {
                vm.isNewProfile = false;
                settingProfileService.getProfile($stateParams.profileId)
                    .then(function (resp) {
                        if (resp && resp.data) {
                            initProfileObject(resp.data);
                        }
                    }, alertsService.errorHandler);
            } else {
                vm.isNewProfile = true;
                vm.profile = {
                    settingProfileId: '',
                    settingType: '',
                    properties: [{ key:'', value:'' }]
                }
            }
        }

        function initProfileObject(data) {
            vm.profile = {
                settingProfileId: data.settingProfileId,
                settingType: data.settingType,
                properties: []
            };
            Object.keys(data.properties).forEach(function(propKey) {
                vm.profile.properties.push({key: propKey, value: data.properties[propKey]})
            });
        }

        function addProfileEntry() {
            vm.profile.properties.push({key: '', value: ''});
        }

        function removeProfileEntry(entry) {
            utilsService.removeItemFromArray(vm.profile.properties, entry);
        }

        function save() {
            if (isValid(vm.profile)) {
                var converted = convertBeforeSaving(vm.profile);
                var promise = vm.isNewProfile ?
                    settingProfileService.createProfile(converted) :
                    settingProfileService.updateProfile(converted);
                promise.then(function (resp) {
                    alertsService.successfullySaved(resp.data.settingProfileId);
                    $state.go('settingprofiles');
                }, alertsService.errorHandler);
            }
        }

        function convertBeforeSaving(data) {
            var profile = {
                id: $stateParams.profileId,
                applicationType: $rootScope.applicationType,
                settingProfileId: data.settingProfileId,
                settingType: data.settingType,
                properties: {}
            };

            data.properties.forEach(function(pair){
                profile.properties[pair.key] = pair.value;
            });
            return profile;
        }

        function isValid(profile) {
            var missingFields = [];
            var missingPropertiesFields = [];
            if (!profile.settingProfileId) {
                missingFields.push('name');
            }
            if (!profile.settingType) {
                missingFields.push('settingType');
            }
            if (!profile.properties) {
                missingFields.push('properties');
            } else {
                for (var i = 0; i < profile.properties.length; i++) {
                    if (!profile.properties[i].key) {
                        missingPropertiesFields.push('key');
                    }
                    if (!profile.properties[i].value) {
                        missingPropertiesFields.push('value');
                    }
                    if (missingPropertiesFields.length > 0) {
                        break;
                    }
                }
            }
            if (missingFields.length > 0 || missingPropertiesFields.length > 0) {
                missingFields = missingFields.concat(missingPropertiesFields);
                alertsService.showError({
                    title: 'Error',
                    message: 'Next fields are missing: ' + missingFields.join(", ")
                });
                return false;
            }

            return true;
        }
    }
})();
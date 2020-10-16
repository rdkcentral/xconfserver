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

    angular.module('app.settingsTestPage').controller('SettingsTestPageController', controller)

    controller.$inject = ['utilsService', 'settingsTestPageService', 'alertsService'];

    function controller(utilsService, settingsTestPageService, alertsService) {
        var vm = this;
        vm.settingsType = [
            {"name": 'EPON', "value": "epon"},
            {"name": 'PARTNER SETTINGS', "value": "partnersettings"}
        ];
        vm.selectedTypes = [];
        vm.parameters = [{key: '', value: ''}];
        vm.autoCompleteValues = null;
        vm.quickAddValues = [];
        vm.matchRules = matchRules;
        vm.getProfileById = getProfileById;

        function getProfileById(id) {
            var profile = settingsTestPageService.findProfileById(vm.profiles, id);
            return profile ? profile.settingProfileId : null;
        }

        function matchRules() {
            vm.profiles = null
            vm.matchedRules = null;
            vm.context = null;

            if (validateInput()) {
                settingsTestPageService.getMatchRules(vm.selectedTypes, vm.parameters).then(
                    function(result) {
                        vm.profiles = result.data.profiles
                        vm.matchedRules = result.data.result;
                        vm.context = result.data.context;
                    }, function(reason) {
                        alertsService.showError({title: 'Error', message: reason.data.message});
                    }
                );
            }
        }

        function validateInput() {
            var isInputValid = true;
            vm.parameters.forEach(function (item) {
                if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(item.key)) {
                    isInputValid = false;
                }
            });
            if (!isInputValid) {
                alertsService.showError({title: 'Error', message: 'Key is required'});
            }
            return isInputValid;
        }

    };
})();
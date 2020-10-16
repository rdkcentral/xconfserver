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
        .module('app.sharedTestPage')
        .controller('SharedTestPageController', controller);

    controller.$inject = ['sharedTestPageService', 'alertsService', 'LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE', 'testPageUtils', '$state'];

    function controller(sharedTestPageService, alertsService, LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE, testPageUtils, $state) {
        var vm = this;
        vm.matchedRules = null;
        vm.parameters = [{key: '', value: ''}];
        vm.autoCompleteValues = LOG_UPLOAD_FREE_ARG_AUTOCOMPLETE_VALUE;
        vm.context = null;
        vm.pageType = $state.current.data.pageType;
        vm.apiUrl = $state.current.data.matchRuleApiUrl;
        vm.settingsType = [
            {"name": 'EPON', "value": "epon"},
            {"name": 'PARTNER SETTINGS', "value": "partnersettings"}
        ];
        vm.selectedTypes = [];
        vm.featureControl = null;

        vm.matchRule = matchRule;
        vm.hasMatchedRules = hasMatchedRules;

        function matchRule() {
            vm.context = null;
            vm.matchedRules = null;
            if (testPageUtils.validateInput(vm.parameters)) {
                var objParams = testPageUtils.getParametersAsObject(vm.parameters);
                sharedTestPageService.getMatchedRules(vm.apiUrl, vm.selectedTypes, objParams).then(function (resp) {
                    vm.matchedRules = resp.data.result;
                    vm.featureControl = JSON.stringify(resp.data.featureControl, null, 2);
                    vm.context = resp.data.context;
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function hasMatchedRules() {
            return vm.matchedRules ? Object.keys(vm.matchedRules).length != 0 : false;
        }
    }
})();
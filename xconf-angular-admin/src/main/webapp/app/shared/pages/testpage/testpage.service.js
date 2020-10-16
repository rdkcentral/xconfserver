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
        .factory('sharedTestPageService', service);

    service.$inject = ['$http', 'permanentProfileService', 'settingProfileService', 'featureService'];

    function service($http, permanentProfileService, settingProfileService, featureService) {

        return {
            getMatchedRules: getMatchedRules,
            getProfiles: getProfiles
        };

        function getMatchedRules(url, selectedTypes, params) {
            return $http.post(url + getSelectedTypesAsString(selectedTypes), params);
        }

        function getProfiles(pageType) {
            return selectRequestService(pageType).getAll();
        }

        function getSelectedTypesAsString(selectedTypes) {
            if (selectedTypes && selectedTypes.length > 0) {
                var settingParams = '?';
                selectedTypes.forEach(function(type) {
                    settingParams += 'settingType=' + type + '&';
                });
                settingParams = settingParams.slice(0, -1);
                return settingParams;
            }
            return '';
        }

        function selectRequestService(pageType) {
            var requestService = null;
            switch(pageType) {
                case 'SETTINGS':
                    requestService = settingProfileService;
                    break;
                case 'TELEMETRY':
                    requestService = permanentProfileService;
                    break;
                case 'FEATURE':
                    requestService = featureService;
                    break;
                default:
                    console.error('Cannot get service, because page type is ' + pageType);
            }
            return requestService;
        }
    }
})();
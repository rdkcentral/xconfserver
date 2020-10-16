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
angular
    .module('app.permanentprofile')
    .factory('permanentProfileService', service);

    service.$inject=['utilsService', '$http', 'syncHttpService'];

    function service(utilsService, $http, syncHttpService) {

        var API_URL = 'api/telemetry/profile/';

        var PROFILE_NAME = 'telemetryProfile:name';
        var UPLOAD_PROTOCOL = 'uploadRepository:uploadProtocol';
        var UPLOAD_URL = 'uploadRepository:URL';

        return {
            getAll: getAll,
            getProfile: getProfile,
            createProfile: createProfile,
            updateProfile: updateProfile,
            deleteProfile: deleteProfile,
            getProfiles: getProfiles,
            searchProfileByName: searchProfileByName,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            exportOne: exportOne,
            exportAll: exportAll,
            getProfileView: getProfileView,
            getProfileName: getProfileName
        };

        function getAll() {
            return $http.get(API_URL);
        }

        function getProfile(id) {
            return $http.get(API_URL + id);
        }

        function createProfile(firmwareRule) {
            return $http.post(API_URL, firmwareRule);
        }

        function updateProfile(firmwareRule) {
            return $http.put(API_URL, firmwareRule);
        }

        function deleteProfile(id) {
            return $http.delete(API_URL + id);
        }

        function getProfiles(pageNumber, pageSize, context) {
            var url = API_URL + 'filtered?pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.post(url, context);
        }

        function searchProfileByName(searchName, pageNumber, pageSize) {
            var url = API_URL + 'filtered?searchName=' + searchName + '&pageNumber=' + pageNumber + '&pageSize=' + pageSize;
            return $http.get(url);
        }

        function updateSyncEntities(permanentProfiles) {
            var requests = utilsService.generateRequestList(permanentProfiles, {url: API_URL + 'entities', method: 'PUT'});
           return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(permanentProfiles) {
            var requests = utilsService.generateRequestList(permanentProfiles, {url: API_URL + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function exportOne(id) {
            window.open(API_URL + id + '?export');
        }

        function exportAll() {
            window.open(API_URL + '?export');
        }

        function getProfileView(profile1, profile2) {
            var profile = getProfileChanges(profile1, profile2);
            if (!profile) {
                return '';
            }
            var view = '';
            if (profile[PROFILE_NAME]) {
                view += 'NAME: ' + profile[PROFILE_NAME] + '\n';
            }
            if (profile[UPLOAD_PROTOCOL]) {
                view += 'UPLOAD PROTOCOL: ' + profile[UPLOAD_PROTOCOL] + '\n';
            }
            if (profile[UPLOAD_URL]) {
                view += 'UPLOAD REPOSITORY: ' + profile[UPLOAD_URL] + '\n';
            }
            if(profile.schedule) {
                view += 'SCHEDULE: ' + profile.schedule + '\n';
            }
            if (profile.telemetryProfile && profile.telemetryProfile.length > 0) {
                view += 'TELEMETRY ELEMENTS: \n';
                for(var i = 0; i < profile.telemetryProfile.length; i++) {
                    var telemetryElement = profile.telemetryProfile[i];
                    if (i >= 1) {
                        view += '\r';
                    }
                    view += '\tHEADER: ' + telemetryElement.header + '\n';
                    view += '\tCONTENT: ' + telemetryElement.content + '\n';
                    view += '\tTYPE: ' + telemetryElement.type + '\n';
                    view += '\tPOLLING FREQUENCY: ' + telemetryElement.pollingFrequency + '\n';
                    view += '\tCOMPONENT: ' + utilsService.getString(telemetryElement.component) + '\n';
                }
            }

            return view;
        }

        function getProfileChanges(profile1, profile2) {
            var oldProfileChanges = {};
            if (!profile1) {
                profile1 = {};
            }
            if (!profile2) {
                profile2 = {};
            }
            if (!angular.equals(profile1[PROFILE_NAME], profile2[PROFILE_NAME])) {
                oldProfileChanges[PROFILE_NAME] = profile1[PROFILE_NAME];
            }
            if (!angular.equals(profile1[UPLOAD_PROTOCOL], profile2[UPLOAD_PROTOCOL])) {
                oldProfileChanges[UPLOAD_PROTOCOL] = profile1[UPLOAD_PROTOCOL];
            }
            if (!angular.equals(profile1[UPLOAD_URL], profile2[UPLOAD_URL])) {
                oldProfileChanges[UPLOAD_URL] = profile1[UPLOAD_URL];
            }
            if (!angular.equals(profile1.schedule, profile2.schedule)) {
                oldProfileChanges.schedule = profile1.schedule;
            }
            oldProfileChanges.telemetryProfile = getDifference(profile1, profile2);
            return oldProfileChanges;
        }

        function getDifference(profile1, profile2) {
            var differentObjects = [];
            _.each(profile1.telemetryProfile, function(entry1) {
                if (!_.find(profile2.telemetryProfile, function(entry2) {return equalsTelemetryEntry(entry1, entry2)})) {
                    differentObjects.push(entry1)
                }
            });
            return differentObjects;
        }

        function equalsTelemetryEntry(entry1, entry2) {
            return entry1 && entry2
                && angular.equals(entry1.header, entry2.header)
                && angular.equals(entry1.content, entry2.content)
                && angular.equals(entry1.type, entry2.type)
                && angular.equals(entry1.pollingFrequency, entry2.pollingFrequency)
                && angular.equals(entry1.component, entry2.component);
        }

        function getProfileName(profile) {
            return profile[PROFILE_NAME];
        }
    }
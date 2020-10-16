/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

(function() {
    'use strict';

    angular
        .module('app.change')
        .factory('changeService', service);

    service.$inject=['$http', 'permanentProfileService', 'CHANGE_ENTITY_TYPE'];

    function service($http, permanentProfileService, CHANGE_ENTITY_TYPE) {
        var API_URL = 'api/change';

        return {
            getApprovedPage : getApprovedPage,
            getChangePage : getChangePage,
            getFilteredApprovedChanges : getFilteredApprovedChanges,
            getFilteredChanges : getFilteredChanges,
            approve: approve,
            cancel: cancel,
            revert: revert,
            getEntityView: getEntityView,
            getEntityName: getEntityName,
            getChangedEntityIds: getChangedEntityIds,
            cancelChangesByEntityId: cancelChangesByEntityId,
            approveChanges: approveChanges,
            revertChanges: revertChanges
        };

        function getApprovedPage(pageSize, pageNumber) {
            return $http.get(API_URL + '/approved/grouped/byId?pageSize=' + pageSize + '&pageNumber=' + pageNumber);
        }

        function getFilteredApprovedChanges(pageSize, pageNumber, searchParam) {
            return $http.post(API_URL + '/approved/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchParam)
        }

        function getChangePage(pageSize, pageNumber) {
            return $http.get(API_URL + '/changes/grouped/byId?pageSize=' + pageSize + '&pageNumber=' + pageNumber);
        }

        function getFilteredChanges(pageSize, pageNumber, searchParam) {
            return $http.post(API_URL + '/changes/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchParam)
        }

        function approve(changeId) {
            return $http.get(API_URL + '/approve/' + changeId);
        }

        function cancel(changeId) {
            return $http.get(API_URL + '/cancel/' + changeId);
        }

        function revert(changeId) {
            return $http.get(API_URL + '/revert/' + changeId);
        }

        function getEntityView(oldEntity, newEntity) {
            return permanentProfileService.getProfileView(oldEntity, newEntity);
        }

        function getEntityName(change) {
            if (CHANGE_ENTITY_TYPE.TELEMETRY_PROFILE === change.entityType) {
                return permanentProfileService.getProfileName(getNotEmptyEntity(change.oldEntity, change.newEntity));
            }
            return change.entityId;
        }

        function getNotEmptyEntity(oldEntity, newEntity) {
            if (oldEntity) {
                return oldEntity;
            } else if (newEntity) {
                return newEntity;
            }
            return {};
        }

        function getChangedEntityIds() {
            return $http.get(API_URL + '/entityIds');
        }

        function cancelChangesByEntityId(entityIds, notApprovedIds) {
            return $http.post(API_URL + '/cancel/byEntityIds', {entityIds: entityIds, changeIdsToExclude: notApprovedIds});
        }

        function approveChanges(changeIds) {
            return $http.post(API_URL + '/approveChanges', changeIds);
        }

        function revertChanges(changeIds) {
            return $http.post(API_URL + '/revertChanges', changeIds);
        }
    }
})();

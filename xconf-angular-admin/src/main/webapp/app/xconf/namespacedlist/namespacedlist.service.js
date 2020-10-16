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
 * Author: rdolomansky
 * Created: 3/30/15
 */

(function() {
    'use strict';

    angular
        .module('app.namespacedlist')
        .factory('namespacedListService', service);

    service.$inject=['utilsService', 'syncHttpService', '$http', '$log', 'alertsService', 'NAMESPACED_LIST_TYPE', 'globalValidationService', 'importService'];

    function service(utilsService, syncHttpService, $http, $log, alertsService, NAMESPACED_LIST_TYPE, globalValidationService, importService) {
        var urlMapping = 'api/genericnamespacedlist/';

        return {
            getNamespacedLists: getNamespacedLists,
            getAllNamespacedLists: getAllNamespacedLists,
            getNamespacedListIds: getNamespacedListIds,
            getNamespacedList: getNamespacedList,
            updateNamespacedList: updateNamespacedList,
            deleteNamespacedList: deleteNamespacedList,
            exportAllNamespacedLists: exportAllNamespacedLists,
            exportNamespacedList: exportNamespacedList,
            sortNamespacedListsData: sortNamespacedListsData,
            isMacAddress: isMacAddress,
            isValidIpAddress: isValidIpAddress,
            createNamespacedList: createNamespacedList,
            getAllNamespacedListsIdToNameMap: getAllNamespacedListsIdToNameMap,
            getAllNamespacedListsByTypeIdToNameMap: getAllNamespacedListsByTypeIdToNameMap,
            getNamespacedListIdsByType: getNamespacedListIdsByType,
            getAllIpAddressGroups: getAllIpAddressGroups,
            isMacPart: isMacPart,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            normalizeMacAddress: normalizeMacAddress,
            filterNamespacedListDataFromFile: filterNamespacedListDataFromFile,
            validateDataFromFile: validateDataFromFile
        };

        function getNamespacedListIdsByType(type) {
            return $http.get(urlMapping + type + "/ids");
        }

        function getAllNamespacedLists() {
            return $http.get(urlMapping);
        }

        function getNamespacedLists(pageNumber, pageSize, searchParam) {
            return $http.post(urlMapping + 'filtered/?pageNumber=' + pageNumber + '&pageSize=' + pageSize, searchParam);
        }

        function getAllIpAddressGroups() {
            return $http.get(urlMapping + 'ipAddressGroups');
        }

        function getNamespacedListIds() {
            return $http.get(urlMapping + 'ids');
        }

        function getNamespacedList(id) {
            return $http.get(urlMapping + id);
        }

        function createNamespacedList(namespacedList) {
            return $http.post(urlMapping, namespacedList);
        }

        function updateNamespacedList(namespacedList, newId) {
            return $http.put(urlMapping + newId, namespacedList);
        }

        function deleteNamespacedList(id) {
            return $http.delete(urlMapping + id);
        }

        function exportAllNamespacedLists(type) {
            window.open(urlMapping + '/all/' + type + "?export");
        }

        function exportNamespacedList(id) {
            window.open(urlMapping + id + "?export");
        }

        function updateSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: urlMapping + 'entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(entities) {
            var requests = utilsService.generateRequestList(entities, {url: urlMapping + 'entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function sortNamespacedListsData(namespacedLists) {
            if (!Array.isArray(namespacedLists)) {
                namespacedLists = [namespacedLists];
            }
            var length = namespacedLists.length;
            for(var i = 0; i < length; i++) {
                namespacedLists[i].data.sort(function(a, b) {
                    return a.localeCompare(b);
                });
            }
        }

        function getAllNamespacedListsIdToNameMap() {
            return $http.get(urlMapping + "names/");
        }

        function getAllNamespacedListsByTypeIdToNameMap(type) {
            return $http.get(urlMapping + type + "/names/");
        }

        function isValidIpAddress(ipAddress) {
            return globalValidationService.isIpValid(ipAddress);
        }

        function isMacAddress(data) {
            return data.replace(/^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$/, '') === '';
        }

        function filterNamespacedListDataFromFile(data, type) {
            if (type == NAMESPACED_LIST_TYPE.IP_LIST) {
                var validIps = _.filter(data, function (ip) {
                    return isValidIpAddress(ip);
                });
                return validIps;
            } else {
                var validMacs = _.filter(data, function(mac) {
                    return isMacAddress(normalizeMacAddress(mac));
                });
                return validMacs;
            }
        }

        function validateDataFromFile(data, filteredData) {
            if (filteredData.length !== data.length) {
                return 'Invalid addresses were not added: ' + _.difference(data, filteredData).join(', ');
            }
            return '';
        }

        function validateDataFromFile(data, filteredData) {
            if (filteredData.length !== data.length) {
                return 'File contains an invalid addresses: ' + _.difference(data, filteredData).join(', ');
            }
            return '';
        }

        function isMacPart(macPart) {
            var splittedMacPart = macPart.split(':');
            for (var i = 0; i < splittedMacPart.length; i++) {
                if ((splittedMacPart[i].length <= 2 && splittedMacPart[i].length >= 1)
                    && splittedMacPart[i].replace(/^([0-9A-Fa-f])+/g, '') === '') {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }

        function normalizeMacAddress(macAddress) {
            macAddress = macAddress.replace(/:/g, "")
                .replace(/-/g, "")
                .replace(/\./g, "").toUpperCase().trim();
            var normalizedMac = '';
            for (var i = 0; i < macAddress.length; ++i) {
                if (i % 2 == 1 && i < (macAddress.length - 1)) {
                    normalizedMac += macAddress.substring(i, i + 1) + ":";
                } else {
                    normalizedMac += macAddress.substring(i, i + 1);
                }
            }
            return normalizedMac;
        }
    }
})();

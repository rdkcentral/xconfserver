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
        .module('app.percentfilter')
        .factory('percentageBeanService', service);

    service.$inject=['$http', 'utilsService', 'syncHttpService', 'firmwareConfigService', '$q', 'ruleHelperService'];

    function service($http, utilsService, syncHttpService, firmwareConfigService, $q, ruleHelperService) {
        var URL = 'api/percentfilter/percentageBean/';

        return {
            getAll: getAll,
            getById: getById,
            create: create,
            update: update,
            deleteById: deleteById,
            exportPercentageBean: exportPercentageBean,
            getTotalDistributionPercentage: getTotalDistributionPercentage,
            exportAllPercentageBeansAsRule: exportAllPercentageBeansAsRule,
            exportPercentageBeanAsRule: exportPercentageBeanAsRule,
            exportAllPercentageBeans: exportAllPercentageBeans,
            updateSyncEntities: updateSyncEntities,
            createSyncEntities: createSyncEntities,
            sortPercentageBeanFirmwareVersionsIfExistOrNot: sortPercentageBeanFirmwareVersionsIfExistOrNot,
            getPage: getPage
        };

        function getPage(pageSize, pageNumber, searchContext) {
            return $http.post(URL + '/filtered?pageSize=' + pageSize + '&pageNumber=' + pageNumber, searchContext);
        }

        function getAll() {
            return $http.get(URL + '/page?pageSize=2147483647&pageNumber=1');
        }

        function getById(id) {
            return $http.get(URL + id);
        }

        function create(percentageBean) {
            return $http.post(URL, percentageBean);
        }

        function update(percentageBean) {
            return $http.put(URL, percentageBean);
        }

        function deleteById(id) {
            return $http.delete(URL + id);
        }

        function exportPercentageBean(id) {
            window.open(URL + id + '?export');
        }

        function getTotalDistributionPercentage(percentageBean) {
            if (!percentageBean || !percentageBean.distributions || percentageBean.distributions.length === 0) {
                return 0;
            }
            var percentage = 0;
            percentageBean.distributions.forEach(function(val, key) {
                percentage += val.percentage;
            });
            return parseFloat(percentage).toFixed(3);
        }

        function exportAllPercentageBeansAsRule() {
            window.open(URL + 'allAsRules?export');
        }

        function exportPercentageBeanAsRule(id) {
            window.open(URL + 'asRule/' + id + '?export');
        }

        function exportAllPercentageBeans() {
            window.open(URL + '?export');
        }

        function updateSyncEntities(percentageBeans) {
            var requests = utilsService.generateRequestList(percentageBeans, {url: URL + '/entities', method: 'PUT'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function createSyncEntities(percentageBeans) {
            var requests = utilsService.generateRequestList(percentageBeans, {url: URL + '/entities', method: 'POST'});
            return requests && requests.length ? syncHttpService.http(requests) : null;
        }

        function sortPercentageBeanFirmwareVersionsIfExistOrNot(percentageBean) {
            var deferred = $q.defer();
            var models = [];
            if (percentageBean && percentageBean.firmwareVersions && percentageBean.firmwareVersions.length > 0) {
                firmwareConfigService.getSortedFirmwareVersionsIfDoesExistOrNot([percentageBean.model], percentageBean.firmwareVersions).then(function (resp) {
                    deferred.resolve(resp.data);
                }, function (error) {
                    deferred.reject(error);
                });
            } else {
                deferred.resolve({"existedVersions":[],"notExistedVersions":[]});
            }
            return deferred.promise;
        }
    }

})();
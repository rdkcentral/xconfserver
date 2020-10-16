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
        .module('app.statistics')
        .controller('StatisticsController', controller);

    controller.$inject = ['$log', 'statisticsService', 'alertsService'];

    function controller($log, statisticsService, alertsService) {
        var vm = this;

        vm.cacheMap = {};
        vm.reloadAllCache = reloadAllCache;
        vm.reloadCacheByCfName = reloadCacheByCfName;

        getCacheMap();

        function getCacheMap() {
            statisticsService.getAllStatistics().then(function (result) {
                $log.debug('RESULT', result);
                vm.cacheMap = result.data;
                countSummary(vm.cacheMap);
            }, function (reason) {
                $log.debug('REASON', reason);
            });
        }

        function reloadAllCache() {
            statisticsService.reloadAllCache().then(function (result) {
                alertsService.showSuccessMessage({message: 'All Cache updated successfully'});
                vm.cacheMap = result.data;
                countSummary(vm.cacheMap);
            }, function (reason) {
                $log.debug('REASON', reason);
            });
        }

        function reloadCacheByCfName(cfName) {
            statisticsService.reloadCacheByCfName(cfName).then(function (result) {
                alertsService.showSuccessMessage({message: 'Cache updated successfully'});
                vm.cacheMap[cfName] = result.data;
                countSummary(vm.cacheMap);
            }, function (reason) {
                $log.debug('REASON', reason);
            });
        }

        function countSummary(cacheMap) {
            var sumTotalLoadTime = 0;
            var sumHitRate = 0;
            var sumMissRate = 0;
            var length = 0;
            for(var columnFamily in cacheMap) {
                length++;
                var obj = cacheMap[columnFamily];
                sumTotalLoadTime += parseInt(obj.totalLoadTime);
                sumHitRate += parseInt(obj.hitRate);
                sumMissRate += parseInt(obj.missRate);
            }
            vm.generalTime = sumTotalLoadTime;
            vm.avgHitRate = sumHitRate / length;
            vm.avgMissRate = sumMissRate / length;
        }
    }
})();

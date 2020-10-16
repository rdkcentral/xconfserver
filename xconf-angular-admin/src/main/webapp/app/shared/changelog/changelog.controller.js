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
        .module('app.changeLog')
        .controller('ChangeLogController', controller);

    controller.$inject = ['$log', 'changeLogService'];

    function controller($log, changeLogService) {
        var vm = this;
        vm.intervalsList = [];

        init();

        function init() {
            changeLogService.getChangeLog().then(
                function(result) {
                    fillIntervalsList(result.data);
                },
                function(reason) {
                    $log.debug('REASON', reason);
                }
            );
        }

        function fillIntervalsList(data) {
            //each timestamp corresponds to list of changes
            var sortedTimestamps = getSortedKeys(data);
            var startOfInterval;
            var endOfInterval;
            for(var i = 0; i < sortedTimestamps.length; i++) {
                var changesObj = {};
                startOfInterval = sortedTimestamps[i];
                if (i === sortedTimestamps.length - 1) {
                    endOfInterval = new Date();
                } else {
                    //end of current interval one second less than start of next interval
                    endOfInterval = sortedTimestamps[i+1] - 1000;
                }
                changesObj.startOfInterval = timeFromTimestamp(startOfInterval);
                changesObj.endOfInterval = timeFromTimestamp(endOfInterval);
                changesObj.changesList = data[startOfInterval];
                changeLogService.countAndSaveOperationsNumber(data[startOfInterval], changesObj);
                vm.intervalsList.push(changesObj);
            }
        }

        function getSortedKeys(object) {
            var sortedKeys = [];
            for (var k in object) {
                if (object.hasOwnProperty(k))
                {
                    sortedKeys.push(parseInt(k));
                }
            }
            sortedKeys.sort(function(a, b) {
                return a - b;
            });

            return sortedKeys;
        }

        function timeFromTimestamp(timestamp) {
            var date = new Date(timestamp);
            var regexp = /\((.)*\)/;
            var result = date.toTimeString().replace(regexp, '');
            return result;
        }
    }
})();

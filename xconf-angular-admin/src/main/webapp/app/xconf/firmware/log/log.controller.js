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
 *  Author: mdolina
 *  Created: 11/9/15 5:40 PM
 */

(function() {
    'use strict';

    angular
        .module('app.log')
        .controller('LogController', controller);

    controller.$inject = ['logService', 'alertsService', 'namespacedListService'];

    function controller(logService, alertsService, namespacedListService) {
        var vm = this;
        vm.logs = null;
        vm.macAddress = null;
        vm.hasError = false;

        vm.getLogs = getLogs;
        vm.validateMacAddress = validateMacAddress;

        function getLogs() {
            var normalizedMac = namespacedListService.normalizeMacAddress(vm.macAddress);
            logService.getLogs({param: normalizedMac}, function(resp) {
                vm.logs = resp;
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }


        function validateMacAddress(mac) {
            var normalizedMac = namespacedListService.normalizeMacAddress(mac);
            vm.hasError = !namespacedListService.isMacAddress(normalizedMac);
        }

    }
})();
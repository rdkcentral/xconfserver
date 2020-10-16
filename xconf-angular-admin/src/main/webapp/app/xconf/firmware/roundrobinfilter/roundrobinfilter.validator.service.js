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
 *  Created: 11/9/15 6:34 PM
 */

(function () {
    'use strict';
    angular
        .module('app.roundrobinfilter')
        .factory('roundRobinFilterValidationService', roundRobinFilterValidationService);

    roundRobinFilterValidationService.$inject = ['namespacedListService', 'globalValidationService'];

    function roundRobinFilterValidationService(namespacedListService, globalValidationService) {

        var vm = this;

        vm.cleanErrors = cleanErrors;

        vm.isValidLocation = isValidLocation;
        vm.isValidHttpLocation = isValidHttpLocation;
        vm.isValidIPv4Locations = isValidIPv4Locations;
        vm.isValidIPv6Locations = isValidIPv6Locations;

        vm.ipv4Error = '';
        vm.ipv6Error = '';
        vm.locationError = '';
        vm.fullHttpLocationError = '';

        return vm;

        function cleanErrors() {
            vm.ipv4Error = '';
            vm.ipv6Error = '';
            vm.locationError = '';
            vm.fullHttpLocationError = '';
        }

        function isValidLocation(location) {
            var isValid = globalValidationService.isUrlValid(location);
            vm.locationError = isValid ? '' : 'Location is not valid';

            return isValid;
        }

        function isValidHttpLocation(fullHttpLocation) {
            var isValid = globalValidationService.isUrlProtocolRequiredValid(fullHttpLocation);
            vm.fullHttpLocationError = isValid ? '' : 'Full HTTP location is not valid';

            return isValid;
        }

        function isValidIPv4Locations(ipv4locations) {
            vm.ipv4Error = '';
            var totalPercentage = 0;
            for(var i=0; i<ipv4locations.length; i++) {
                if (ipv4locations[i].locationIp === '') {
                    vm.ipv4Error = 'IP is empty';
                }

                if (!globalValidationService.isIpV4(ipv4locations[i].locationIp)) {
                    vm.ipv4Error = 'IP address is not valid';
                }

                if (ipv4locations[i].percentage === '') {
                    vm.ipv4Error = 'Percentage is empty';
                }
                totalPercentage += parseInt(ipv4locations[i].percentage);
            }
            if (totalPercentage !== 100) {
                vm.ipv4Error = 'Total percentage count should be equal to 100';
            }
        }

        function isValidIPv6Locations(ipv6locations) {
            vm.ipv6Error = '';
            var totalPercentage = 0;
            for(var i=0; i<ipv6locations.length; i++) {
                if (ipv6locations[i].locationIp === '') {
                    vm.ipv6Error = 'IP is empty';
                }
                if (!globalValidationService.isIpV6(ipv6locations[i].locationIp)) {
                    vm.ipv6Error = 'IP address is not valid';
                }
                if (ipv6locations[i].percentage === '') {
                    vm.ipv6Error = 'Percentage is empty';
                }
                totalPercentage += parseInt(ipv6locations[i].percentage);
            }
            if (totalPercentage !== 100) {
                vm.ipv6Error = 'Total percentage count should be equal to 100';

            }
        }
    }

})();
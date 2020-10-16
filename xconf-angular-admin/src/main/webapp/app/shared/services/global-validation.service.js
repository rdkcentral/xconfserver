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
 * Author: Oleksandr Baturynskyi
 * Created: 03/21/15 4:01 PM
 */
(function() {
    'use strict';
    angular.module('app.services')
        .factory('globalValidationService', GlobalValidationService);


    GlobalValidationService.$inject = ['utilsService', 'REGEXP_CONSTANTS'];

    function GlobalValidationService (utilsService, REGEXP_CONSTANTS) {

        return {
            isProtocolValid: isProtocolValid,
            isIpValid: isIpValid,
            isPortValid: isPortValid,
            isPercentValid: isPercentValid,
            isUrlValid: isUrlValid,
            isUrlProtocolRequiredValid: isUrlProtocolRequiredValid,

            isIpV4: isIpV4,
            isIpV6: isIpV6,
            isNumber: isNumber,
            isBoolean: isBoolean
        };

        /**
         * Validates protocol
         * @param protocol
         * @returns {boolean}
         */
        function isProtocolValid(protocol) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(protocol)) {
                return false;
            }
            if (!protocol.match(REGEXP_CONSTANTS().alphabetical)) {
                return false;
            }
            return true;
        }

        /**
         * Validates ip
         * @param ip
         * @returns {boolean}
         */
        function isIpValid(ip) {
            if (utilsService.isNullOrUndefinedOrEmptyOrWhiteSpaceString(ip)) {
                return false;
            }

            var ipv6Valid = false;
            var ipv4Valid = false;

            try {
                ipv6Valid = new v6.Address(ip).isValid();
            }
            catch(err) {}

            try {
                ipv4Valid = new v4.Address(ip).isValid();
            }
            catch(err) {}

            return !(!ipv4Valid && !ipv6Valid);
        }

        function isIpV4(ip) {
            try {
                return new v4.Address(ip).isValid();
            } catch(err) {
                return false;
            }
        }

        function isIpV6(ip) {
            try {
                return new v6.Address(ip).isValid();
            } catch(err) {
                return false;
            }
        }

        function isNumber(value) {
            return $.isNumeric(value);
        }

        function isBoolean(value) {
            return angular.isDefined(value) && ("true" === value.toLowerCase() || "false" === value.toLowerCase());
        }

        /**
         * Validates port
         * @param port
         * @returns {boolean}
         */
        function isPortValid(port) {
            return angular.isDefined(port) && $.isNumeric(port) && (port >= 0) && (port <= 65535);
        }

        /**
         * Validates percent
         * @param percent
         * @returns {boolean}
         */
        function isPercentValid(percent) {
            return angular.isDefined(percent) && $.isNumeric(percent) && (percent > 0) && (percent < 100);
        }

        /**
         * Validates url
         * @param url
         * @returns {boolean}
         */
        function isUrlValid(url) {
            return new RegExp("(?=^.{4,253}$)(^((?!-)[a-zA-Z0-9-]{0,62}[a-zA-Z0-9]\.)+[a-zA-Z]{2,63}$)").test(url);
        }

        /**
         * Validates url (protocol required)
         * @param url
         * @returns {boolean}
         */
        function isUrlProtocolRequiredValid(url) {
            return new RegExp("^(http[s]?:\\/\\/(www\\.)?|ftp:\\/\\/(www\\.)?|www\\.){1}([0-9A-Za-z-\\.@:%_\+~#=]+)+((\\.[a-zA-Z]{2,3})+)(/(.)*)?(\\?(.)*)?").test(url);
        }
    }
})();
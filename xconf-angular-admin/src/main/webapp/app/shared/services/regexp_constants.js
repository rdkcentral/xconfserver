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
 * Created: 03/18/15 4:01 PM
 */
(function () {
    'use strict';

    angular.module('app.services').constant('REGEXP_CONSTANTS', regexpConstants);

    function regexpConstants() {
        var numericPattern = new RegExp('^[-]?[0-9]+([.]{1}[0-9]+)?$');
        var versionPattern = new RegExp('^([0-9a-zA-Z]+([-]?[0-9a-zA-Z]+)*)+([.]{1}[0-9a-zA-Z]+([0-9a-zA-Z-]+[0-9a-zA-Z]+)?)*$');
        var urlPattern = new RegExp('([a-z]+){1}(:\/\/)([\\w]+)([:]*)([0-9]*)[\/]*([\\w]*)');
        var result = {
            alphabetical: /^[a-zA-Z]*$/,
            nonEmptyAlphabetical: /^[a-zA-Z]+$/,
            numerical: /^\d+$/,
            percent: /^(?:100|\d{1,2})(?:\.\d{1,2})?$/,
            alphaNumericalWithUnderscores: /^[a-zA-z0-9_]+$/,
            alphaNumericalWithUnderscoresAndSpaces: /^[a-zA-z0-9_ ]+$/,
            ipv4: /^0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])\.0*([1-9]?\d|1\d\d|2[0-4]\d|25[0-5])$/,
            //ipv6 validates using ipv6.js
            stackName: /^\/[a-zA-Z0-9._-]+\/[a-zA-Z0-9._-]+$/,
            numericPattern: numericPattern,
            versionPattern: versionPattern,
            urlPattern: /^([a-z]+){1}(:\/\/)([\w]+)([:]*)([0-9]*)[\/]*([\w]*)/g,
            urnPattern: /^[a-zA-Z0-9-_;\.]+$/,
            portPattern: '/^[0-9]{1,5}$/'
        };
        return result;
    }
})();
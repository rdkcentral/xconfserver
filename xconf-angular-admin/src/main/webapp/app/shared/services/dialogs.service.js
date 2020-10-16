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
(function () {
    'use strict';

    angular.module('app.services').factory('dialogsService', service);

    service.$inject = ['dialogs', '$window'];

    function service($dialogs, $window) {
        function isOpen(header) {
            var isOpen = false;
            $('.modal-header span[ng-bind-html=header]').each(function() {
                if ($(this).text() === header) {
                    isOpen = true;
                }
            });
            return isOpen;
        }

        return {
            showErrorAsSingleton: function (header, message, options) {
                if (!isOpen(header)) {
                    $dialogs.error(header, message, options);
                }
            },
            showConfirmAsSingleton: function (header, message, options, handler) {
                if (!isOpen(header)) {
                    $dialogs.confirm(header, message, options)
                            .result.then(handler);
                }
            }
        };
    }
})();
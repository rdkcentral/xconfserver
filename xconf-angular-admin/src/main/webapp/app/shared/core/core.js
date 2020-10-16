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
 * Created: 4/3/15  3:21 PM
 */

(function() {
    'use strict';

    angular
        .module('app.core')

        .constant('AUTH_EVENT', {
            'SESSION_TIMEOUT': 'auth-session-timeout',
            'UNAUTHORIZED': 'auth-anauthorized',
            'NO_ACCESS': 'auth-no-access'
        })

        .factory('responseErrorInterceptor', ['$rootScope', '$q', '$injector', 'AUTH_EVENT',
            function($rootScope, $q, $injector, AUTH_EVENT) {
                return {
                    /**
                     * The API returns an error object if there is something wrong.
                     * Example: {
                     *     status: 404,
                     *     type: "EntityNotFoundException",
                     *     message: "NamespacedList "test" does not exist"
                     * }
                     */
                    responseError: function(response) {
                        var status = response.status;
                        if (status === 401) {
                            $rootScope.$broadcast(AUTH_EVENT.UNAUTHORIZED);
                        }
                        return $q.reject(response);
                    }
                };
            }])
        .config(['$httpProvider', function($httpProvider) {
            $httpProvider.interceptors.push('responseErrorInterceptor');
        }]);
})();

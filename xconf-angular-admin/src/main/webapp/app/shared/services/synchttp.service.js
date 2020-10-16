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

    angular.module('app.services')
        .factory('syncHttpService', service);

    service.$inject = ['$http'];

    function service($http) {
        function selectRequest(method, url, data) {
            var request = null;

            if (!method) {
                console.log('Method is NULL');
                return null;
            }

            switch(method.toLowerCase()) {
                case 'get':
                    request = $http.get(url);
                    break;
                case 'put':
                    request = data ? $http.put(url, data) : $http.put(url);
                    break;
                case 'post':
                    request = data ? $http.post(url, data) : $http.post(url);
                    break;
                default:
                    console.log('Did not find method');
                    break;
            }
            return request;
        }


        function http(requests) {
            function performRequest(success, error) {
                var request = (requests && requests.length)
                    ? requests.splice(0, 1)[0] : null;

                if (!request) {
                    return null;
                }

                if (!request.url) {
                    console.log('URL does not exist');
                    console.log(request);
                    return null;
                }

                selectRequest(request.method, request.url, request.data).then(success, error);
            }

            return {
                then: function (success, error) {
                    performRequest(success, error);

                    return {
                        next : function() {
                            performRequest(success, error);
                        }
                    }
                }
            }
        }

        return {
            http: http
        }
    }
})();
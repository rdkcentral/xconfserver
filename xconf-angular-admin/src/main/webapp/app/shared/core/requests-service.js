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
 * Created: 01/21/15  11:00 AM
 */


(function () {
    'use strict';
    angular
        .module('app.core')
        .factory('requestsService', requestsService);


    requestsService.$inject = ['$http', '$q'];

    function requestsService($http, $q) {

        var getData = function (url, headers, params) {
            var defer = $q.defer();
            $http({
                method: 'GET',
                url: url,
                headers: headers,
                params: params
            }).success(function (data, status, header) {
                defer.resolve(data);
            }).error(function (data, status, header) {
                if (status !== 401) {
                    defer.reject({data: data, status: status});
                }
                if (status !== 400 && (typeof data === 'string') && data.indexOf('UnmarshalException') > 0) {
                    defer.resolve('');
                }
            });

            return defer.promise;
        };

        var saveJsonData = function(url, data) {
            return saveData(url, data, {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            });
        };

        var postData = function (url, data, headers) {
            var defer = $q.defer();
            $http({
                method: 'POST',
                data: data,
                url: url,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                }).error(function (data, status, header) {
                    if (status !== 401) {
                        defer.reject(data);
                    }
                });
            return defer.promise;
        };

        var updateData = function (url, data, headers) {
            var defer = $q.defer();
            $http({
                method: 'PUT',
                data: data,
                url: url,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                }).error(function (data, status, header) {
                    if (status !== 401) {
                        defer.reject(data);
                    }
                });
            return defer.promise;
        };

        var deleteItem = function (url, headers, optData) {
            var defer = $q.defer();
            $http({
                method: 'DELETE',
                url: url,
                data: optData,
                headers: headers
            })
                .success(function (data, status, header) {
                    defer.resolve(data);
                })
                .error(function (data, status, header) {
                    if (status !== 401) {
                        defer.reject(data);
                    }
                });
            return defer.promise;
        };

        var exportAllEntities = function(url) {
            window.open(url);
        };

        var exportEntity = function(url) {
            window.open(url);
        };

        return {
            getData: getData,
            postData: postData,
            updateData: updateData,
            saveJsonData: saveJsonData,
            deleteItem: deleteItem,
            exportAllEntities: exportAllEntities,
            exportEntity: exportEntity
        };
    }
})();

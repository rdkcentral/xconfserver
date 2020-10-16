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
angular.module('app.directives').directive('simpleSearchDirective',
    ['$log', function ($log) {
        return {
            link: function (scope, element, attrs) {
                scope.searchQuery = {
                    value: '',
                    multipleKey: '',
                    multipleValue: ''
                };

                var options = scope.options;
                var data = options.data;

                scope.currentData = data[0];

                scope.changeData = changeData;
                scope.search = search;

                init();

                function init() {

                    if (!options.currentData) {
                        options.currentData = data[0];
                    }
                }

                function changeData(data) {
                    scope.currentData = data;
                    scope.searchQuery.value = '';
                    scope.searchQuery.multipleKey = '';
                    scope.searchQuery.multipleValue = '';
                    var searchObject = {};
                    scope.currentData.name.apiArgs.forEach(function(arg) {
                        searchObject[arg] = '';
                    });
                    passSearchParams(searchObject);
                }

                function search(apiArgs, searchValue) {
                    var searchObject = {};
                    if (scope.currentData.name.friendlyName === 'Key and Value') {
                        searchObject.FREE_ARG = scope.searchQuery.multipleKey;
                        searchObject.FIXED_ARG = scope.searchQuery.multipleValue;
                    } else {
                        apiArgs.forEach(function(arg) {
                            searchObject[arg] = scope.searchQuery.value;
                        });
                    }
                    passSearchParams(searchObject);
                }

                function passSearchParams(searchObject) {
                    scope.$emit('search-entities', {
                        searchParam: searchObject
                    })
                }
            },
            restrict: 'E',
            scope: {
                options: "="
            },
            templateUrl: 'app/shared/directives/simplesearch/simplesearch.html'
        };
    }]);

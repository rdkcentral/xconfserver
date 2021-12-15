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

    angular
        .module('app.directives')
        .directive('tagAutocompleteDirective', ['$log', 'alertsService', function ($log, alertsService) {
            function link(scope, element, attrs) {
                scope.suggestions = [];
                scope.selectedIndex = -1;
                scope.searchText = "";

                scope.removeTag = function(index) {
                    scope.selectedTags.splice(index, 1);
                };

                scope.search = function() {
                    if (!scope.disableAutocomplete) {
                        var result = [];
                        for (var i = 0; i < scope.data.length; i++) {
                            var value = scope.data[i];
                            if (value.indexOf(scope.searchText) !== -1 || scope.searchText.trim() == '') {
                                result.push(value);
                            }
                        }
                        scope.suggestions = result;
                        scope.selectedIndex = -1;
                    }
                };

                scope.addToSelectedTags = function(index) {
                    var suggestion = scope.suggestions[index];
                    if(scope.selectedTags.indexOf(suggestion) === -1) {
                        scope.selectedTags.push(suggestion);
                        scope.searchText = '';
                        scope.suggestions = [];
                    }
                };

                scope.checkKeyDown = function(event) {
                    if (scope.disableAutocomplete) {
                        if (event.keyCode === 13 && scope.selectedTags.indexOf(scope.searchText) === -1) {
                            scope.selectedTags.push(scope.searchText);
                            scope.searchText = '';
                        } else if (event.keyCode === 13 && scope.selectedTags.indexOf(scope.searchText) !== -1) {
                            alertsService.showError({message: "Duplicates should not be added"});
                        }
                    } else {
                        if (event.keyCode === 40) {
                            event.preventDefault();
                            if (scope.selectedIndex + 1 !== scope.suggestions.length) {
                                scope.selectedIndex++;
                            }
                        } else if (event.keyCode === 38) {
                            event.preventDefault();
                            if(scope.selectedIndex - 1 !== -1){
                                scope.selectedIndex--;
                            }
                        } else if (event.keyCode === 13 && scope.selectedIndex !== -1) {
                            scope.addToSelectedTags(scope.selectedIndex);
                            scope.selectedIndex = -1;
                        }
                    }
                };

                scope.showAllValuesIfNecessary = function() {
                    if (!scope.searchText) {
                        scope.search();
                    }
                };

                scope.$watch('selectedIndex', function(value) {
                    if(value !== -1) {
                        scope.searchText = scope.suggestions[scope.selectedIndex];
                    }
                });
            }

            return {
                restrict: 'AE',
                scope: {
                    selectedTags: '=',
                    data: "=",
                    disableAutocomplete: "="
                },
                templateUrl: 'app/shared/directives/tagautocomplete/tagautocomplete.html',
                link: link
            };
        }]);
})();

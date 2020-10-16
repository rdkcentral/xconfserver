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
 * Author: Stanislav Menshykov
 * Created: 26.10.15  9:55
 */
(function () {
    "use strict";

    angular.module('app.directives').directive('dropdownMultiselect', function() {
        return {
            restrict: 'E',
            scope:{
                model: '=',
                options: '=',
                labelProperty: '@',
                valueProperty: '@',
                buttonText: '@'
            },
            templateUrl: 'app/shared/directives/dropdown-multiselect.directive.html',

            link: function($scope) {
                $scope.buttonText = angular.isDefined($scope.buttonText) ? $scope.buttonText : 'Select';

                $scope.selectAll = function (event) {
                    $scope.model = _.pluck($scope.options, $scope.valueProperty);
                    event.stopPropagation();
                };
                $scope.deselectAll = function(event) {
                    $scope.model=[];
                    event.stopPropagation();
                };
                $scope.setSelectedItem = function(event) {
                    var valueProperty = this.option[$scope.valueProperty];
                    if (_.contains($scope.model, valueProperty)) {
                        $scope.model = _.without($scope.model, valueProperty);
                    } else {
                        $scope.model.push(valueProperty);
                    }
                    event.stopPropagation();
                };
                $scope.isChecked = function (valueProperty) {
                    return _.contains($scope.model, valueProperty)
                };
                $scope.allUnchecked = function() {
                    return $scope.model.length <= 0;
                }
            }
        }
    });

})();
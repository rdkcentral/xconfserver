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
        .module('app')
        .controller('ToggleButtonCtrl', ToggleButtonCtrl);

    ToggleButtonCtrl.$inject = ['$scope'];

    function ToggleButtonCtrl($scope) {

        var vm = this;

        vm.maxElements = 10;

        vm.click = click;
        vm.showButton = angular.isDefined($scope.data) && ($scope.data.length > vm.maxElements);
        vm.expanded = true;

        click();

        function click() {
            vm.expanded = !vm.expanded;

            if(vm.expanded) {
                vm.slicedData = $scope.data;
                vm.btnTitle = "Show less";
            } else {
                vm.slicedData = vm.showButton ? $scope.data.slice(0, vm.maxElements) : $scope.data;
                vm.btnTitle = "...Show more";
            }
        }
    }
})();

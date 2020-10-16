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

    angular.module('app.directives').
        directive('customProgressbar', directive);

    directive.$inject = ['$timeout'];

    function directive($timeout) {
        var scope = {
            control: '=control'
        };
        var total = null;

        function update(scope) {
            scope.data = {progress: 0};
            total = null;
        }

        function linkFunction(scope, element, attrs) {
            update(scope);

            scope.internalControl = scope.control || {};
            scope.internalControl.total = 0;

            scope.internalControl.progress = function(progress) {

                //shows progress in UI
                scope.data.progress += Math.round(progress * 100 / scope.internalControl.total);
                scope.control.next();

                //updates if total is 0
                total = (total != null) ? total - progress : scope.internalControl.total - progress;
                if(!total){
                    update(scope);
                }
            }
        }

        return {
            restrict: 'E',
            scope: scope,
            link: linkFunction,
            templateUrl: 'app/shared/directives/custom-progressbar/custom-progressbar.directive.html'
        }
    }
})();
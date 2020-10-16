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
'use strict';

angular.module('app.directives')
    .directive('customViewerPanel', directive);

directive.$inject = ['utilsService', '$timeout'];

function directive(utilsService, $timeout) {

    var _scope = null;
    var scope = {
        control: '=',
        bottomShift: '=',
        cssBasicColumns: '=',
        cssSecondColumns: '='
    }

    /*
    * calculate max height of the elements
    */
    function calculateMaxHeight(basicElement, cssBasicColumns) {
        var maxHeight = 0;
        angular.forEach(cssBasicColumns, function(css) {
            var height = 0;
            var elem = basicElement.find(css);
            if (elem && elem.height()) {
                height = elem.height()
            }
            maxHeight = (maxHeight > height) ? maxHeight : height;
        });

        return maxHeight;
    }

    function link(scope, element, attrs) {
        scope.internalControl = scope.control || {};
        scope.planeStyle = {
            "height": "auto",
            "position": "relative",
            "overflow-y": "visible"
        };

        function stopPropagation($event) {
            $event.stopPropagation();
        }

        function toggle($event, isOpen) {
            var panelElement = $($event.currentTarget);
            togglePanel(panelElement, isOpen);
        }

        function togglePanel(element) {
            var basicMaxHeight = calculateMaxHeight(element, scope.cssBasicColumns);
            var secondMaxHeight = calculateMaxHeight(element, scope.cssSecondColumns);

            if (basicMaxHeight > secondMaxHeight) {
                return;
            }

            if (!element.hasClass('opened')) {
                scope.planeStyle['overflow-y'] = 'visible';
                scope.planeStyle['height'] = 'auto';
                utilsService.removeClass(element.find('#blurBottom'), 'blur-bottom');
                utilsService.addClass(element, 'opened');
            } else {
                scope.planeStyle['overflow-y'] = 'hidden';

                var result = (scope.bottomShift) ?  basicMaxHeight + scope.bottomShift : basicMaxHeight;
                scope.planeStyle['height'] = result + 'px';
                utilsService.addClass(element.find('#blurBottom'), 'blur-bottom');
                utilsService.removeClass(element, 'opened');
            }
        }

        //updates new height through 0.5sec
        $timeout(function() {
           scope.$apply(togglePanel(element, false))
        }, 500);

        scope.toggle = toggle;
        scope.internalControl.stopPropagation = stopPropagation;
        _scope = scope;
    }

    return {
        restrict: 'E',
        scope: scope,
        replace: true,
        transclude: true,
        link: link,
        template: '<div ng-transclude ng-click="toggle($event)" ng-style="planeStyle" class="opened ads-tab"></div>'
    }
}
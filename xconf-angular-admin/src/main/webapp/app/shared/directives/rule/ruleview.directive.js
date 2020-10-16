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
 * Created: 5/20/15  3:45 PM
 */

(function () {
    "use strict";

    angular.module('app.directives').directive('ruleviewDirective', ['$compile', 'ruleHelperService', 'OPERATION', 'utilsService', function($compile, ruleHelperService, OPERATION, utilsService) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            link: function(scope, element, attrs) {
                /**
                 * The compile function cannot handle directives that recursively use themselves in their own templates
                 * or compile functions. Compiling these directives results in an infinite loop and a stack overflow errors.
                 * This can be avoided by manually using $compile in the postLink function to imperatively compile
                 * a directive's template instead of relying on automatic template compilation via template or templateUrl
                 * declaration or manual compilation inside the compile function.
                 */
                var template = '\
                    <div rule-type="ruleview">\
                        <div class="ruleview-rule">\
                            <div class="ruleview-relation" ng-hide="!rule[0].relation" ng-bind="rule[0].relation"></div>\
                            <div class="ruleview-condition" ng-class="{\'ads-ruleview-condition-not\': rule[0].negated}">\
                                <div class="ruleview-negated" ng-show="rule[0].negated">not</div>\
                                <div class="ruleview-argument" ng-bind="rule[0].condition.freeArg.name"></div>\
                                <div class="ruleview-operation" ng-bind="rule[0].condition.operation"></div>\
                                <div class="ruleview-value" ng-bind="renderValue(rule[0].condition)"></div>\
                            </div>\
                        </div>\
                        <ul class="ruleview-list" ng-hide="rule.length < 2">\
                            <li ng-repeat="compoundRule in rule | startFrom: 1">\
                                <ruleview-directive data="compoundRule"></ruleview-directive>\
                            </li>\
                        </ul>\
                    </div>';
                var $template = angular.element(template);
                $compile($template)(scope);
                element.replaceWith($template);

                scope.rule = ruleHelperService.flattenRule(scope.data);

                scope.renderValue = function(condition) {
                    return ruleHelperService.renderValue(condition);
                }
            }
        };
    }]);

})();
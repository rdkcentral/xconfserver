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
    "use strict";

    angular
        .module('app.firmwarerule')
        .directive('firmwareruleDefinePropertiesEditorDirective', ['$log', 'alertsService', 'firmwareRuleValidationService', function($log, alertsService, firmwareRuleValidationService) {


            function link(scope, element, attrs) {
                scope.key = "";
                scope.value = "";
                scope.rowDom = null;
                scope.editedItem = null;

                scope.saveRow = saveRow;
                scope.editRow = editRow;
                scope.clear = clear;
                scope.hasError = hasError;


                function saveRow() {
                    var property = scope.templateProperties[scope.key];
                    if (firmwareRuleValidationService.validatePropertyValue(property, scope.value)) {
                        scope.editedItem.key = scope.key;
                        scope.editedItem.value = scope.value;
                        clear();
                    } else {
                        alertsService.showError(
                            {
                                message: "Value is not valid. Value Type(s): " + property.validationTypes,
                                title: 'Validation Error'
                            }
                        );
                    }
                }

                function clear() {
                    scope.key = "";
                    scope.value = "";
                    if (scope.rowDom) {
                        scope.rowDom.removeClass("active");
                        scope.rowDom = null;
                        scope.editedItem = null;
                    }
                }

                function editRow(event, item) {
                    console.log(event);

                    var target =  $(event.target);
                    var rowDom = $(target.closest("tr"));
                    clear();
                    rowDom.addClass("active");
                    scope.key = item.key;
                    scope.value = item.value;
                    scope.rowDom = rowDom;
                    scope.editedItem = item;
                }

                function hasError(item) {

                    var property = scope.templateProperties[item.key];
                    if (firmwareRuleValidationService.validatePropertyValue(property, item.value)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }

            return {
                restrict: 'E',
                scope: {
                    data: '=',
                    templateProperties: '=',
                    isTemplate: "="
                },
                templateUrl: 'app/xconf/firmwarerule/firmwarerule-definePropertiesEditor.html',
                link: link
            };
        }]);

})();
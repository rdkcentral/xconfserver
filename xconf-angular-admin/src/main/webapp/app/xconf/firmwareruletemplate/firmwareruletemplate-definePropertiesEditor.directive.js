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
        .directive('firmwareruletemplateDefinePropertiesEditorDirective', ['$log', 'alertsService', 'firmwareRuleValidationService', function($log, alertsService, firmwareRuleValidationService) {


        function link(scope, element, attrs) {
            scope.key = "";
            scope.value = "";
            scope.rowDom = null;
            scope.editedItem = null;
            scope.optional = false;
            scope.selectedValidationTypes = ['STRING'];
            scope.options = [
                { id: 'STRING', value: 'STRING'},
                { id: 'BOOLEAN', value: 'BOOLEAN'},
                { id: 'NUMBER', value: 'NUMBER' },
                { id: 'PERCENT', value: 'PERCENT' },
                { id: 'PORT', value: 'PORT' },
                { id: 'URL', value: 'URL' },
                { id: 'IPV4', value: 'IPV4' },
                { id: 'IPV6', value: 'IPV6' }
            ];


            scope.saveRow = saveRow;
            scope.editRow = editRow;
            scope.removeRow = removeRow;
            scope.clear = clear;


            function saveRow() {
                var property = {
                    value: scope.value,
                    optional: scope.optional,
                    validationTypes: scope.selectedValidationTypes
                };

                if (!scope.key) {
                    alertsService.showError({message: "Key is blank", title: 'Validation Error'});
                    return;
                }

                if (scope.rowDom) {
                    scope.editedItem.key = scope.key;
                    scope.editedItem.value = property;
                } else {
                    scope.data.push({key: scope.key, value: property});
                }
                clear();
            }

            function clear() {
                scope.key = "";
                scope.value = "";
                scope.optional = false;
                scope.selectedValidationTypes = ['STRING'  ];
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
                scope.value = item.value.value;
                scope.optional = item.value.optional;
                angular.copy(item.value.validationTypes, scope.selectedValidationTypes);
                scope.rowDom = rowDom;
                scope.editedItem = item;
            }

            function removeRow(item) {
                var idx = findIndexByItem(scope.data, item);
                if (idx !== -1) {
                    scope.data.splice(idx, 1);
                }

            }

            function findIndexByItem(array, item) {
                for (var i = 0; i < array.length; i++) {
                    if (array[i] === item) {
                        return i;
                    }
                }
                return -1;
            }
        }

        return {
            restrict: 'E',
            scope: {
                data: '=',
                isTemplate: "="
            },
            templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-definePropertiesEditor.html',
            link: link
        };
    }]);

})();
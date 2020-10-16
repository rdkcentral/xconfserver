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
 * Created: 06.11.15  15:06
 */
(function() {
    'use strict';

    angular
        .module('app.firmwareReportPage')
        .controller('FirmwareReportPageController', controller);

    controller.$inject = ['$scope', 'firmwareReportPageService', 'alertsService', 'FileSaver', 'Blob', 'utilsService', 'firmwareRuleService'];

    function controller($scope, firmwareReportPageService, alertsService, FileSaver, Blob, utilsService, firmwareRuleService) {
        var vm = this;


        vm.getReport = getReport;
        vm.atLeastOneChecked = atLeastOneChecked;
        vm.checkAll = checkAll;
        vm.uncheckAll = uncheckAll;

        vm.macRulesNamesForCheckbox = [];
        vm.idsToNamesMap = {};
        vm.macRuleNameRows = [];

        $scope.$on('applicationType:changed', function(event, data) {
            getMacRules();
        });

        getMacRules();

        function getMacRules() {
            firmwareRuleService.getMacRulesNames().then(
                function(result) {
                    vm.idsToNamesMap = result.data;

                    var names = _.values(vm.idsToNamesMap);
                    vm.macRulesNamesForCheckbox = createMacRulesForCheckbox(names);
                    sortByRuleName(vm.macRulesNamesForCheckbox);
                    vm.macRuleNameRows = utilsService.chunkData(vm.macRulesNamesForCheckbox, 3);
                },
                function(reason) {
                    alertsService.showError({title: 'Error', message: reason.data});
                }
            );
        }

        function createMacRulesForCheckbox(macRulesNames) {
            var length = macRulesNames.length;
            var result = [];
            for (var i = 0; i < length; i++) {
                result.push({
                    ruleName: macRulesNames[i],
                    isChecked: false
                })
            }

            return result;
        }

        function getReport() {
            firmwareReportPageService.getReport(getCheckedIds()).then(
                function(result) {
                    var name = 'report.xls';
                    try {
                        name = result.headers('Content-Disposition').split('=')[1]
                    } catch(err) {}
                    var blob = new Blob([result.data], {type: "application/vnd.ms-excel"});
                    FileSaver.saveAs(blob, name);
                }
            );
        }

        function getCheckedIds() {
            var result = [];
            var namesToIdsMap = _.invert(vm.idsToNamesMap);
            var length = vm.macRulesNamesForCheckbox.length;
            for (var i = 0; i < length; i++) {
                var item = vm.macRulesNamesForCheckbox[i];
                if (item.isChecked) {
                    result.push(namesToIdsMap[item.ruleName]);
                }
            }
            return result;
        }

        function atLeastOneChecked() {
            var length = vm.macRulesNamesForCheckbox.length;
            for (var i = 0; i < length; i++) {
                var item = vm.macRulesNamesForCheckbox[i];
                if (item.isChecked) {
                    return true;
                }
            }

            return false;
        }

        function checkAll() {
            var length = vm.macRulesNamesForCheckbox.length;
            for (var i = 0; i < length; i++) {
                vm.macRulesNamesForCheckbox[i].isChecked = true;
            }
        }

        function uncheckAll() {
            var length = vm.macRulesNamesForCheckbox.length;
            for (var i = 0; i < length; i++) {
                vm.macRulesNamesForCheckbox[i].isChecked = false;
            }
        }

        function sortByRuleName(objectsArray) {
            objectsArray.sort(function(a, b) {
                return a.ruleName.localeCompare(b.ruleName);
            });
        }
    }
})();
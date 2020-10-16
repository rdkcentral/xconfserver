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
 *  Author: mdolina
 *  Created: 11/26/15 5:14 PM
 */

(function() {
    'use strict';

    angular
        .module('app.targetingrule')
        .controller('TargetingRuleImportController', controller);

    controller.$inject = ['$scope', '$log', 'alertsService', 'utilsService', 'importService', 'targetingRuleService', 'permanentProfileService', 'paginationService'];

    function controller($scope, $log, alertsService, utilsService, importService, targetingRuleService, permanentProfileService, paginationService) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importTargetingRule = importTargetingRule;
        vm.importAllTargetingRules = importAllTargetingRules;
        vm.targetingRules = null;
        vm.wrappedTargetingRules = null;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.profiles = [];
        vm.paginationStorageKey = 'targetingRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.selectPage = selectPage;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;
        vm.progressBarControl = importService.progressBarControl;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });

        init();

        function init() {
            permanentProfileService.getAll()
                .then(function(resp) {
                    vm.profiles = resp.data;
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
        }

        async function retrieveFile(fileName) {
            vm.targetingRules = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedTargetingRules = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importTargetingRule(wrappedTargetingRule) {
            if (validateTargetingRule(wrappedTargetingRule.entity)) {
                if (wrappedTargetingRule.overwrite) {
                    targetingRuleService.update(wrappedTargetingRule.entity).then(function () {
                        alertsService.successfullySaved(wrappedTargetingRule.entity.name);
                        utilsService.removeSelectedItemFromListById(vm.wrappedTargetingRules, wrappedTargetingRule.entity.id);
                    }, function (error) {
                        alertsService.showError({message: error.data.message, title: 'Exception'});
                    });
                } else {
                    targetingRuleService.create(wrappedTargetingRule.entity).then(function () {
                        alertsService.successfullySaved(wrappedTargetingRule.entity.name);
                        utilsService.removeSelectedItemFromListById(vm.wrappedTargetingRules, wrappedTargetingRule.entity.id);
                    }, function (error) {
                        alertsService.showError({message: error.data.message, title: 'Exception'});
                    });
                }
            }
        }

        function importAllTargetingRules() {
            importService.importAllEntities(targetingRuleService, vm.wrappedTargetingRules);
        }


        function overwriteAll() {
            angular.forEach(vm.wrappedTargetingRules, function (val) {
                val.overwrite = vm.isOverwritten;
            });
        }

        function validateTargetingRule(targetingRule) {
            var missingFields = [];
            if (!targetingRule.id) {
                missingFields.push('rule id');
            }
            if (!targetingRule.boundTelemetryId) {
                missingFields.push('profile id');
            }
            if (!targetingRule.name) {
                missingFields.push('rule name');
            }
            if ((!targetingRule.compoundParts && !targetingRule.condition)
                || (!targetingRule.condition && targetingRule.compoundParts && targetingRule.compoundParts.length === 0)) {
                missingFields.push('condition');
            }

            if (missingFields.length > 0) {
                alertsService.showError({title: 'ValidationException', message: 'Next fields are missing: ' + missingFields.join(', ')});
                return false;
            }
            return true;
        }

        function selectPage() {
            paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            computeStartAndEndIndex();
        }

        function computeStartAndEndIndex() {
            vm.startIndex = (vm.pageNumber - 1) * vm.pageSize;
            vm.endIndex = vm.pageNumber * vm.pageSize;
        }

        function getGeneralItemsNumber() {
            return vm.wrappedTargetingRules ? vm.wrappedTargetingRules.length : 0;
        }
    }
})();

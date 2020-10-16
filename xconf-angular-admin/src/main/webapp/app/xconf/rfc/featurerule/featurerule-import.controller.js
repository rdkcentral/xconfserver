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

    angular
        .module('app.featurerule')
        .controller('FeatureRuleImportController', controller);

    controller.$inject = ['$scope', 'featureRuleService', 'importService', 'paginationService', 'utilsService', 'alertsService', '$uibModal'];

    function controller($scope, featureRuleService, importService, paginationService, utilsService, alertsService, $uibModal) {
        var vm = this;
        vm.isOverwritten = false;
        vm.wrappedFeatureRules = null;
        vm.paginationStorageKey = 'featureRulePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();

        vm.overwriteAll = overwriteAll;
        vm.importFeatureRule = importFeatureRule;
        vm.importAllFeatureRules = importAllFeatureRules;
        vm.retrieveFile = retrieveFile;
        vm.viewFeatureRule = viewFeatureRule;
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

        async function retrieveFile(fileName) {
            try {
                let file = await importService.openFile(fileName, null, this);
                vm.isOverwritten = false;
                vm.wrappedFeatureRules = importService.prepareEntitiesFromFile(file);
                selectPage();
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function importFeatureRule(wrappedFeatureRule) {
            var promise = wrappedFeatureRule.overwrite
                ? featureRuleService.updateFeatureRule(wrappedFeatureRule.entity)
                : featureRuleService.createFeatureRule(wrappedFeatureRule.entity);
            promise.then(function () {
                alertsService.successfullySaved(wrappedFeatureRule.entity.name);
                utilsService.removeSelectedItemFromListById(vm.wrappedFeatureRules, wrappedFeatureRule.entity.id);
            }, function (error) {
                alertsService.showError({message: error.data.message, title: 'Exception'});
            });
        }

        function importAllFeatureRules() {
            importService.importAllEntities(featureRuleService, vm.wrappedFeatureRules);
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedFeatureRules, function (value) {
                value.overwrite = vm.isOverwritten;
            });
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
            return vm.wrappedFeatureRules ? vm.wrappedFeatureRules.length : 0;
        }

        function viewFeatureRule(featureRule) {
            var modalInstance = $uibModal.open({
                templateUrl: 'app/xconf/rfc/featurerule/featurerule.view.html',
                size: 'md',
                controller: 'FeatureRuleViewController as vm',
                resolve: {
                    featureRule: function () {
                        return featureRule;
                    }
                }
            });
        }
    }
})();
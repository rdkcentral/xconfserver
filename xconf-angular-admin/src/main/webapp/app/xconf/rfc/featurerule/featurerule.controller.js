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
    angular
        .module('app.featurerule')
        .controller('FeatureRuleController', controller);

    controller.$inject = ['$scope', '$controller', 'dialogs', 'featureRulesSize', 'featureRuleService', 'paginationService', 'utilsService', 'alertsService', 'RULE_SEARCH_OPTIONS', '$uibModal'];

    function controller($scope, $controller, dialogs, featureRulesSize, featureRuleService, paginationService, utilsService , alertsService, RULE_SEARCH_OPTIONS, $uibModal) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.wrappedFeatureRules = null;

        vm.paginationStorageKey = 'featurePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.searchParam = {};
        vm.searchOptions = angular.copy(RULE_SEARCH_OPTIONS);
        vm.featureRulesSize = parseInt(featureRulesSize);

        vm.startParse = startParse;
        vm.getFeatureRules = getFeatureRules;
        vm.exportFeatureRule = featureRuleService.exportFeatureRule;
        vm.exportAllFeatureRules = featureRuleService.exportAllFeatureRules;
        vm.deleteFeatureRule = deleteFeatureRule;
        vm.changePriority = changePriority;
        vm.viewFeatureRule = viewFeatureRule;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        init();

        function init() {
            vm.searchOptions = angular.copy(RULE_SEARCH_OPTIONS);
            vm.searchOptions.data.push({
                "name": {
                    friendlyName: 'Feature',
                    apiArgs: ['FEATURE_INSTANCE']
                }
            });
            getFeatureRules();
        }

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            getFeatureRules();
        });

        function deleteFeatureRule(featureRule) {
            if (featureRule.id) {
                var dialog = dialogs.confirm('Delete confirmation'
                    , '<span class="break-word-inline"> Are you sure you want to delete Feature Rule ' + featureRule.name + ' ? </span>');
                dialog.result.then(function () {
                     featureRuleService.deleteFeatureRule(featureRule.id).then(function(result) {
                        utilsService.removeItemFromArray(vm.wrappedFeatureRules, featureRule);
                        alertsService.successfullyDeleted(featureRule.name);
                        shiftItems();
                     }, function(reason) {
                        alertsService.showError({title: 'Error', message: reason.data.message});
                     });
                });
            }
        }

        function getFeatureRules() {
            featureRuleService.getFeatureRules(vm.pageNumber, vm.pageSize, vm.searchParam).then(function(result) {
                vm.wrappedFeatureRules = result.data;
                vm.generalItemsNumber = result.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                setAvailablePriorities(vm.featureRulesSize);
            }, function (error) {
                alertsService.showError({title: 'Error', message: 'Error by loading feature rule'});
            });
        }

        function shiftItems() {
            vm.generalItemsNumber--;
            vm.featureRulesSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.generalItemsNumber) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getFeatureRules();
        }

        function startParse() {
            return vm.generalItemsNumber > 0;
        }

        function setAvailablePriorities(size) {
            size = parseInt(size);

            vm.availablePriorities = [];
            for (var i = 1; i < size + 1; i++) {
                vm.availablePriorities.push(i);
            }
        }

        function changePriority(id, priority) {
            featureRuleService.changePriorities(id, priority).then(function(result){
                init();
            }, function(reason) {
                alertsService.showError({title: 'Error', message: reason.message});
                init();
            });
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
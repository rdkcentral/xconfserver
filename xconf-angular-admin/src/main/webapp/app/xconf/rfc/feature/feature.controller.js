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
        .module('app.feature')
        .controller('FeatureController', controller);

    controller.$inject = ['$scope', '$controller', 'dialogs', 'featureService', 'paginationService', 'alertsService', 'utilsService'];

    function controller($scope, $controller, dialogs, featureService, paginationService, alertsService, utilsService) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.wrappedFeatures = null;
        vm.paginationStorageKey = 'featurePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();
        vm.generalItemsNumber = 0;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Feature Instance",
                        apiArgs: ["FEATURE_INSTANCE"]
                    }
                },
                {
                    "name": {
                        friendlyName: "Name",
                        apiArgs: ["NAME"]
                    }
                },
                {
                    "name": {
                        friendlyName: 'Key',
                        apiArgs: ['FREE_ARG']
                    }
                },
                {
                    "name": {
                        friendlyName: 'Value',
                        apiArgs: ['FIXED_ARG']
                    }
                },
                {
                    "name": {
                        friendlyName: 'Key and Value',
                        apiArgs: ['FREE_ARG', 'FIXED_ARG']
                    }
                }
            ]
        };

        vm.startParse = startParse;
        vm.getFeatures = getFeatures;
        vm.exportFeature = featureService.exportFeature;
        vm.exportAllFeatures = featureService.exportAllFeatures;
        vm.deleteFeature = deleteFeature;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        init();

        function init() {
            getFeatures();
        }

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            getFeatures();
        });

        function deleteFeature(feature) {
            if (feature.id) {
                var dialog = dialogs.confirm('Delete confirmation'
                    , '<span class="break-word-inline"> Are you sure you want to delete Feature ' + feature.name + ' ? </span>');
                dialog.result.then(function () {
                    featureService.deleteFeature(feature.id).then(function(result) {
                        utilsService.removeItemFromArray(vm.wrappedFeatures, feature);
                        alertsService.successfullyDeleted(feature.name);
                        shiftItems();
                    }, function(reason) {
                        alertsService.showError({title: 'Error', message: reason.data.message});
                    });
                });
            }
        }

        function getFeatures() {
            featureService.getFeatures(vm.pageNumber, vm.pageSize, vm.searchParam).then(function (result) {
                vm.wrappedFeatures = result.data;
                vm.generalItemsNumber = result.headers('numberOfItems');
                paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
            }, function (error) {
                alertsService.showError({title: 'Error', message: 'Error by loading feature'});
            });
        }

        function shiftItems() {
            var numberOfPagesAfterDeletion = Math.ceil((vm.generalItemsNumber - 1) / vm.pageSize);
            vm.pageNumber = (vm.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : vm.pageNumber;
            getFeatures();
        }

        function startParse() {
            return vm.generalItemsNumber > 0;
        }

    }
})();
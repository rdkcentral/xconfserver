/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

(function() {
    'use strict';

    angular
        .module('app.activation-version')
        .controller('ActivationVersionEditController', controller);

    controller.$inject = ['$rootScope', '$scope', '$controller', 'firmwareConfigService', 'modelService', '$stateParams', 'alertsService', '$state', 'authUtilsService', 'PERMISSION', 'activationVersionService', '$q', 'utilsService'];

    function controller($rootScope, $scope, $controller, firmwareConfigService, modelService, $stateParams, alertsService, $state, authUtils, PERMISSION, activationVersionService, $q, utilsService) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'activation-versions',
            stateParameters: null
        }));

        vm.models = [];
        vm.activationVersion = {
            id: '',
            description: '',
            model: '',
            regularExpressions: [],
            firmwareVersions: [],
            applicationType: $rootScope.applicationType
        };
        vm.firmwareVersions = [];
        vm.newlyAddedRegularExpressions = [];
        vm.regularExpression = '';
        vm.PERMISSION = PERMISSION;
        vm.authUtils = authUtils;

        vm.save = save;
        vm.selectVersion = selectVersion;

        vm.addRegularExpression = addRegularExpression;
        vm.removeRegularExpression = removeRegularExpression;
        vm.reloadFirmwareVersions = reloadFirmwareVersions;
        vm.isNewlyAddedExpression = isNewlyAddedExpression;
        vm.reloadAndCleanFirmwareVersions = reloadAndCleanFirmwareVersions;

        init();

        function init() {
            modelService.getAll().then(function(resp) {
                vm.models = resp.data;
            }, function(error) {
                alertsService.showError(error.data.message);
            });

            if ($stateParams.id) {
                activationVersionService.getById($stateParams.id).then(function(resp) {
                    vm.activationVersion = resp.data;
                    vm.activationVersion.applicationType = $rootScope.applicationType ? $rootScope.applicationType : resp.data.applicationType;
                    reloadFirmwareVersions(vm.activationVersion.model).then(function() {
                        angular.forEach(vm.firmwareVersions, function(val, key) {
                            if (vm.activationVersion.firmwareVersions.indexOf(val.version) >= 0) {
                                val.selected = true;
                            }
                        });
                    });
                    firmwareConfigService.getSortedFirmwareVersionsIfDoesExistOrNot([vm.activationVersion.model], vm.activationVersion.firmwareVersions).then(function (versionResp) {
                        vm.sortedFirmwareVersions = versionResp.data;
                    }, function(versionsError) {
                       alertsService.showError({title: 'Error', message: error.data.message});
                    });
                }, function(error) {
                    alertsService.showError({title: 'Error', message: 'Error by loading Activation Version'});
                });
            }
        }

        function selectVersion(versionObject) {
            var index = vm.activationVersion.firmwareVersions.indexOf(versionObject.version);
            if (index >= 0) {
                vm.activationVersion.firmwareVersions.splice(index, 1);
                versionObject.selected = false;
            } else {
                vm.activationVersion.firmwareVersions.push(versionObject.version);
                versionObject.selected = true;
            }
        }

        function save() {
            if ($stateParams.id) {
                activationVersionService.update(vm.activationVersion).then(function (resp) {
                    alertsService.successfullySaved(vm.activationVersion.description);
                    $state.go('activation-version');
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            } else {
                activationVersionService.create(vm.activationVersion).then(function (resp) {
                    alertsService.successfullySaved(vm.activationVersion.description);
                    $state.go('activation-version');
                }, function (error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function addRegularExpression() {
            vm.activationVersion.regularExpressions.push(vm.regularExpression);
            vm.newlyAddedRegularExpressions.push(vm.regularExpression);
            vm.regularExpression = '';
        }

        function removeRegularExpression(expression) {
            utilsService.removeItemFromArray(vm.activationVersion.regularExpressions, expression);
            utilsService.removeItemFromArray(vm.newlyAddedRegularExpressions, expression);
        }

        function reloadFirmwareVersions(model) {
            vm.firmwareVersions = [];
            var defer = $q.defer();
            firmwareConfigService.getBySupportedModels([model]).then(function(resp) {
                vm.firmwareVersions = _.map(resp.data, function(firmwareConfig) {
                    return {
                        version: firmwareConfig.firmwareVersion,
                        selected: false
                    };
                });
                defer.resolve(vm.firmwareVersions);
            }, function (error) {
                alertsService.showError({title: 'Error', message: error.data.message});
                defer.reject(error);
            });
            return defer.promise;
        }

        function reloadAndCleanFirmwareVersions(model) {
            reloadFirmwareVersions(model);
            vm.activationVersion.firmwareVersions.length = 0;
        }

        function isNewlyAddedExpression(expression) {
            return vm.newlyAddedRegularExpressions.indexOf(expression) !== -1;
        }
    }

})();
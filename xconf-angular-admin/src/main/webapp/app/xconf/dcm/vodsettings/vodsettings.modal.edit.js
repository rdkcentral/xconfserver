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
    'use strict';

    angular
        .module('app.vodsettings')
        .controller('VodSettingsModalEditController', controller);

    controller.$inject = ['$rootScope', '$log', '$uibModalInstance', '$controller', 'vodSettings', 'formulaId', 'vodSettingsService', 'alertsService', '$scope', 'utilsService', 'EDIT_MODE'];

    function controller($rootScope, $log, $modalInstance, $controller, vodSettings, formulaId, vodSettingsService, alertsService, $scope, utilsService, EDIT_MODE) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'vodsettings',
            stateParameters: null
        }));

        vm.EDIT_MODE = EDIT_MODE;
        vm.currentEditMode = vodSettings ? EDIT_MODE.UPDATE : EDIT_MODE.CREATE;
        vm.vodSetting = vodSettings;
        vm.srmIPList = [];
        vm.dismiss = dismiss;
        vm.addNewIpEntity = addNewIpEntity;
        vm.removeIpEntity = removeIpEntity;
        vm.save = save;
        vm.update = update;
        vm.createDeviceSettings = createVodSettings;
        init();

        function init() {
            if (vm.vodSetting) {
                angular.forEach(vm.vodSetting.srmIPList, function(val, key) {
                    var ipEntity = {
                        name: key,
                        ip: val
                    };
                    vm.srmIPList.push(ipEntity);
                });
            } else {
                vm.vodSetting = {
                    id: formulaId,
                    applicationType: $rootScope.applicationType
                };
            }
        }

        function dismiss() {
            $modalInstance.dismiss();
        }

        function addNewIpEntity() {
            var ipEntity = {
                name: '',
                ip: ''
            };
            vm.srmIPList.push(ipEntity);
        }

        function removeIpEntity(ipEntity) {
            utilsService.removeItemFromArray(vm.srmIPList, ipEntity);
        }

        function setIpsAndNames() {
            vm.vodSetting.ipList = [];
            vm.vodSetting.ipNames = [];
            for(var i = 0; i < vm.srmIPList.length; i++) {
                vm.vodSetting.ipNames.push(vm.srmIPList[i].name);
                vm.vodSetting.ipList.push(vm.srmIPList[i].ip);
            }
        }

        function createVodSettings() {
            if (!vm.vodSetting) {
                alertsService.showError({title: 'Error', message: 'VodSetting is not present'});
                return;
            }
            if (isValidVodSetting(vm.vodSetting)) {
                setIpsAndNames();
                vodSettingsService.create(vm.vodSetting).then(function(resp) {
                    alertsService.successfullySaved(resp.data.name);
                    $modalInstance.close();
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function save() {
            if (vm.currentEditMode === vm.EDIT_MODE.CREATE) {
                createVodSettings();
            }

            if (vm.currentEditMode === vm.EDIT_MODE.UPDATE) {
                update();
            }
        }

        function update() {
            if (!vm.vodSetting) {
                alertsService.showError({title: 'Error', message: 'VodSetting is not present'});
                return;
            }
            if (isValidVodSetting(vm.vodSetting)) {
                setIpsAndNames();
                vodSettingsService.update(vm.vodSetting).then(function(resp) {
                    alertsService.successfullySaved(resp.data.name);
                    $modalInstance.close();
                }, function(error) {
                    alertsService.showError({title: 'Error', message: error.data.message});
                });
            }
        }

        function isValidVodSetting(vodSetting) {
            var missingFields = [];
            if (!vodSetting.name) {
                missingFields.push('name');
            }
            if (!vodSetting.locationsURL) {
                missingFields.push('location url');
            }
            if (missingFields.length > 0) {
                alertsService.showError({'title': 'Error', message: 'Next fields are empty: ' + missingFields.join(', ')});
                return false;
            }
            return true;
        }
    }
})();
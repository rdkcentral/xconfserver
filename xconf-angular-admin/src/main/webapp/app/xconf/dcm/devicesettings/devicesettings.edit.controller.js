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
/**
 * Created by izluben on 17.10.15.
 */
(function() {
    'use strict';

    angular
        .module('app.devicesettings')
        .controller('DeviceSettingsEditController', controller);

    controller.$inject=['$log', '$scope', '$uibModalInstance', '$controller', 'deviceSettings', 'id', 'deviceSettingsService', 'alertsService', 'deviceSettingsValidationService', 'utilsService', 'EDIT_MODE', 'TIME_ZONES'];

    function controller($log, $scope, $modalInstance, $controller, deviceSettings, id, deviceSettingsService, alertsService, deviceSettingsValidationService, utilsService, EDIT_MODE, TIME_ZONES) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'devicesettings',
            stateParameters: null
        }));

        vm.EDIT_MODE = EDIT_MODE;
        vm.currentEditMode = deviceSettings ? EDIT_MODE.UPDATE : EDIT_MODE.CREATE;
        vm.timeZones = [
            'UTC',
            'CST',
            'EST',
            'MST',
            'PST'
        ];
        vm.scheduleType = [
            'ActNow',
            'CronExpression'
        ];
        vm.devSettings = deviceSettings ? deviceSettings : {
            id: id,
            name: '',
            checkOnReboot: false,
            settingsAreActive: false,
            schedule: {
                type: vm.scheduleType[0],
                expression: '',
                timeWindowMinutes: 0,
            }
        };
        vm.deviceSettingsService = deviceSettingsService;
        vm.deviceSettingsValidationService = deviceSettingsValidationService;
        vm.usedNames = [];
        vm.cronFields = {
            minutes: '',
            hours: '',
            month: '',
            dayOfWeek: '',
            dayOfMonth: ''
        };
        vm.schedulerTimeZones = TIME_ZONES;
        vm.updateDeviceSettings = updateDeviceSettings;
        vm.saveDeviceSettings = saveDeviceSettings;
        vm.createDeviceSettings = createDeviceSettings;
        vm.dismiss = dismiss;

        init();


        function init() {
            if (vm.devSettings && vm.devSettings.schedule) {
                vm.cronFields = utilsService.parseCronExpression(vm.devSettings.schedule.expression);
            }
            if (vm.devSettings.schedule && !vm.devSettings.schedule.timeZone) {
                vm.devSettings.schedule.timeZone = vm.timeZones[0];
            }
            getUsedNames();
        }

        function createDeviceSettings() {
            deviceSettingsService.createDeviceSettings(vm.devSettings).then(
                function(result) {
                    alertsService.successfullySaved(vm.devSettings.name);
                    $modalInstance.close();
                },
                function (reason) {
                    alertsService.failedToSave(vm.devSettings.name, reason.data.message);
                }
            );
        }

        function updateDeviceSettings() {
            deviceSettingsService.updateDeviceSettings(vm.devSettings).then(
                function(result) {
                    alertsService.successfullySaved(vm.devSettings.name);
                    $modalInstance.close();
                },
                function (reason) {
                    alertsService.failedToSave(vm.devSettings.name, reason.data.message);
                }
            );
        }

        function saveDeviceSettings() {
            if (vm.currentEditMode === vm.EDIT_MODE.CREATE) {
                createDeviceSettings();
            }

            if (vm.currentEditMode === vm.EDIT_MODE.UPDATE) {
                updateDeviceSettings();
            }
        }

        function dismiss() {
            $modalInstance.dismiss('close');
        }

        function getUsedNames() {
            var initialName = angular.copy(vm.devSettings.name);
            vm.deviceSettingsService.getDeviceSettingsNames().then(
                function(result) {
                    vm.usedNames = result.data;
                    utilsService.removeItemFromArray(vm.usedNames, initialName);
                }
            );
        }

        $scope.$watch('vm.cronFields', function() {
            vm.devSettings.schedule.expression = utilsService.getCronExpressionFromFields(vm.cronFields);
        }, true);
    }
})();
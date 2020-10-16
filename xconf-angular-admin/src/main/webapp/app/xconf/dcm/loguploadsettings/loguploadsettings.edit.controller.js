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
 * Created: 20.10.15  16:49
 */
(function() {
    'use strict';

    angular
        .module('app.loguploadsettings')
        .controller('LogUploadSettingsEditController', controller);

    controller.$inject=['$rootScope', '$scope', '$uibModalInstance', '$controller', 'logUploadSettings', 'id', 'alertsService', 'utilsService', 'logUploadSettingsService', 'EDIT_MODE', 'logUploadSettingsValidationService', 'uploadRepositoryService', 'SCHEDULE_TYPE', 'TIME_ZONES'];

    function controller($rootScope, $scope, $modalInstance, $controller, logUploadSettings, id, alertsService, utilsService, logUploadSettingsService, EDIT_MODE, logUploadSettingsValidationService, uploadRepositoryService, SCHEDULE_TYPE, TIME_ZONES) {
        var vm = this;

        angular.extend(vm, $controller('EditController', {
            $scope: $scope,
            mainPage: 'loguploadsettings',
            stateParameters: null
        }));

        vm.EDIT_MODE = EDIT_MODE;
        vm.currentEditMode = logUploadSettings ? EDIT_MODE.UPDATE : EDIT_MODE.CREATE;
        vm.timeZones = [
            'UTC',
            'CST',
            'EST',
            'MST',
            'PST'
        ];
        vm.SCHEDULE_TYPE = SCHEDULE_TYPE;
        vm.uploadRepositories = [];
        vm.usedNames = [];
        vm.logUploadSettingsValidationService = logUploadSettingsValidationService;
        vm.logUploadSettings = logUploadSettings ? convertForFrontEnd(logUploadSettings) : {
            id: id,
            name: '',
            applicationType: $rootScope.applicationType,
            uploadOnReboot: false,
            numberOfDays: '0',
            areSettingsActive: false,
            schedule: {
                type: vm.SCHEDULE_TYPE.ACT_NOW,
                expression: '',
                expressionL1: '',
                expressionL2: '',
                expressionL3: '',
                timeWindowMinutes: '0'
            },
            uploadRepositoryId: ''
        };
        vm.timeZoneSchedule = vm.timeZones[0];
        vm.timeZoneDateTimeRange = vm.timeZones[0];
        vm.cronFields = {
            minutes: '',
            hours: '',
            month: '',
            dayOfWeek: '',
            dayOfMonth: ''
        };
        vm.schedulerTimeZones = TIME_ZONES;

        vm.dismiss = dismiss;
        vm.saveLogUploadSettings = saveLogUploadSettings;
        vm.updateLogUploadSettings = updateLogUploadSettings;
        vm.createDeviceSettings = createDeviceSettings;

        init();

        function init() {
            if (vm.logUploadSettings && vm.logUploadSettings.schedule) {
                vm.cronFields = utilsService.parseCronExpression(vm.logUploadSettings.schedule.expression);
            }
            if (vm.logUploadSettings.schedule && !vm.logUploadSettings.schedule.timeZone) {
                vm.logUploadSettings.schedule.timeZone = vm.timeZones[0];
            }
            getUsedNames();
            getUploadRepositories();
        }

        function createDeviceSettings() {
            logUploadSettingsService.createLogUploadSettings(convertForBackEnd(vm.logUploadSettings)).then(
                function(result) {
                    alertsService.successfullySaved(vm.logUploadSettings.name);
                    $modalInstance.close();
                },
                function (reason) {
                    alertsService.failedToSave(vm.logUploadSettings.name, reason.data.message);
                }
            );
        }

        function updateLogUploadSettings() {
            logUploadSettingsService.updateLogUploadSettings(convertForBackEnd(vm.logUploadSettings)).then(
                function(result) {
                    alertsService.successfullySaved(vm.logUploadSettings.name);
                    $modalInstance.close();
                },
                function (reason) {
                    alertsService.failedToSave(vm.logUploadSettings.name, reason.data.message);
                }
            );
        }

        function saveLogUploadSettings() {
            if (vm.currentEditMode === vm.EDIT_MODE.CREATE) {
                createDeviceSettings();
            }

            if (vm.currentEditMode === vm.EDIT_MODE.UPDATE) {
                updateLogUploadSettings();
            }
        }

        function convertForFrontEnd(logUploadSettings) {
            if (logUploadSettings.schedule.type === SCHEDULE_TYPE.WHOLE_DAY_RANDOMIZED) {
                logUploadSettings.schedule.expression = '';
                logUploadSettings.schedule.timeWindowMinutes = '0';
            }

            return logUploadSettings;
        }

        function convertForBackEnd(logUploadSettings) {
            var result = angular.copy(logUploadSettings);
            nullifyOptionalFields(result);

            return result;
        }

        function nullifyOptionalFields(logUploadSettings) {
            if (logUploadSettings.schedule.type === SCHEDULE_TYPE.WHOLE_DAY_RANDOMIZED) {
                //we need to set expression not blank to be able to save schedule
                logUploadSettings.schedule.expression = 'someNonEmptyValue';
                logUploadSettings.schedule.expressionL1 = '';
                logUploadSettings.schedule.expressionL2 = '';
                logUploadSettings.schedule.expressionL3 = '';
                logUploadSettings.schedule.timeWindowMinutes = 0;
            }
        }

        function getUsedNames() {
            var initialName = angular.copy(vm.logUploadSettings.name);
            logUploadSettingsService.getLogUploadSettingsNames().then(
                function(result) {
                    vm.usedNames = result.data;
                    utilsService.removeItemFromArray(vm.usedNames, initialName);
                }
            );
        }

        function getUploadRepositories() {
            uploadRepositoryService.getAll().then(function(result) {
                vm.uploadRepositories = result.data;
                if (vm.uploadRepositories.length > 0 && !vm.logUploadSettings.uploadRepositoryId) {
                    vm.logUploadSettings.uploadRepositoryId = vm.uploadRepositories[0].id;
                }
            });
        }

        function dismiss() {
            $modalInstance.dismiss('close');
        }

        $scope.$watch('vm.cronFields', function() {
            vm.logUploadSettings.schedule.expression = utilsService.getCronExpressionFromFields(vm.cronFields);
        }, true);
    }
})();
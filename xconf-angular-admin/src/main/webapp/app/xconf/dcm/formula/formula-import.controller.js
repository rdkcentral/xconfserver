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
 * Created: 25.11.15  14:29
 */
(function() {
    'use strict';

    angular
        .module('app.formula')
        .controller('FormulaImportController', controller);

    controller.$inject=['$scope', '$log', '$uibModal', '$location', 'alertsService', 'formulaService', 'importService', 'utilsService', 'paginationService', 'MODES_TO_GET_LOG_FILES', 'uploadRepositories'];

    function controller($scope, $log, $modal, $location, alertsService, formulaService, importService, utilsService, paginationService, MODES_TO_GET_LOG_FILES, uploadRepositories) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importFormula = importFormula;
        vm.importAllFormulas = importAllFormulas;
        vm.selectPage = selectPage;
        vm.changePageSize = changePageSize;
        vm.paginationStorageKey = 'dcmFormulasPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.formulasWithSettings = null;
        vm.wrappedFormulasWithSettings = null;
        vm.numPages = 0;
        vm.overwriteAll = overwriteAll;
        vm.isOverwritten = false;
        vm.viewVodSettings = viewVodSettings;
        vm.viewDeviceSettings = viewDeviceSettings;
        vm.viewLogUploadSettings = viewLogUploadSettings;
        vm.existingUploadRepositoriesIds = _.pluck(uploadRepositories, 'id');
        vm.progressBarControl = importService.progressBarControl;

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageSizeModel = vm.pageSize;
                paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                selectPage();
            }
        });


        async function retrieveFile(fileName) {
            vm.formulasWithSettings = null;
            try {
                let file = await importService.openFile(fileName, null, this);
                init(file);
            } catch(e) {
                alertsService.showError({message: e});
            }
        }

        function init(file) {
            var formulasWithSettings = importService.getEntitiesFromFile(file);
            var validationResult = validateFormulasWithSettings(formulasWithSettings);
            if (validationResult) {
                openInfoModal(validationResult);
            } else {
                vm.formulasWithSettings = formulasWithSettings;
                vm.formulasWithSettings.sort(function(a, b) {
                    return a.formula.priority >= b.formula.priority;
                });
                vm.wrappedFormulasWithSettings = importService.wrapToImport(vm.formulasWithSettings);
                vm.isOverwritten = false;
                selectPage();
            }
        }

        function validateFormulasWithSettings(formulasWithSettings) {
            var length = formulasWithSettings.length;
            var result = [];
            for (var i = 0; i < length; i++) {
                var logUploadSettings = formulasWithSettings[i].logUploadSettings;
                if (logUploadSettings) {
                    var validationResult = validateLogUploadSettings(logUploadSettings);
                    if (validationResult) {
                        result.push(validationResult);
                    }
                }
            }

            return result.length > 0 ? result : null;
        }

        function openInfoModal(info) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/formula/formula.validation.info.modal.html',
                size: 'lg',
                controller: 'FormulaValidationInfoController as vm',
                resolve: {
                    data: function () {
                        return info;
                    }
                }
            });
        }

        function validateLogUploadSettings(logUploadSettings) {
            var result = [];

            result.push(validateLogUploadSettingsUploadRepository(logUploadSettings));
            utilsService.removeNullOrUndefinedOrEmptyStringValuesFromArray(result);

            return result.length > 0 ? result : null;
        }

        function validateLogUploadSettingsUploadRepository(logUploadSettings) {
            var uploadRepositoryId = logUploadSettings.uploadRepositoryId;
            if (uploadRepositoryId && vm.existingUploadRepositoriesIds.indexOf(uploadRepositoryId) < 0) {
                return 'LogUploadSettings ' + logUploadSettings.name + ' contains invalid uploadRepository id: ' + uploadRepositoryId;
            }
        }

        function importFormula(wrappedFormulaWithSettings) {
            formulaService.importFormula(wrappedFormulaWithSettings.entity, wrappedFormulaWithSettings.overwrite).then(function () {
                alertsService.successfullySaved(wrappedFormulaWithSettings.entity.formula.name);
                removeFormulaWithSettingsById(vm.wrappedFormulasWithSettings, wrappedFormulaWithSettings.entity.formula.id);
            }, function (reason) {
                var data = reason.data;
                alertsService.showError({title: data.type, message: data.message});
            });
        }

        function importAllFormulas() {
            importService.importAllEntities(formulaService, vm.wrappedFormulasWithSettings, function(id) {
                removeFormulaWithSettingsById(vm.wrappedFormulasWithSettings, id);
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

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            selectPage();
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedFormulasWithSettings, function (val, key) {
                val.overwrite = vm.isOverwritten;
            });
        }

        function viewVodSettings(vodSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/vodsettings/vodsettings.modal.view.html',
                controller: 'VodSettingsModalViewController as vm',
                size: 'md',
                resolve : {
                    vodSettings: function() {
                        return vodSettings;
                    }
                }
            });
        }

        function viewDeviceSettings(deviceSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/devicesettings/devicesettings.view.html',
                size: 'lg',
                controller: 'DeviceSettingsViewController as vm',
                resolve: {
                    deviceSettings: function () {
                        return deviceSettings;
                    }
                }
            });
        }

        function viewLogUploadSettings(logUploadSettings) {
            $modal.open({
                templateUrl: 'app/xconf/dcm/loguploadsettings/loguploadsettings.view.html',
                size: 'lg',
                controller: 'LogUploadSettingsViewController as vm',
                resolve: {
                    logUploadSettings: function () {
                        return logUploadSettings;
                    }
                }
            });
        }

        function removeFormulaWithSettingsById(formulasWithSettings, id) {
            var length = formulasWithSettings.length;
            for (var i = 0; i < length; i++) {
                if (formulasWithSettings[i].entity.formula.id === id) {
                    return formulasWithSettings.splice(i, 1);
                }
            }
        }

        function removeImportedItemsFromListByIdsAndShowSuccessMessages(list, ids) {
            var length = ids.length;
            for (var i = 0; i < length; i++) {
                var removed = removeFormulaWithSettingsById(list, ids[i]);
                alertsService.successfullySaved(removed[0].entity.formula.name);
            }
        }

        function showErrorMessages(messages) {
            var length = messages.length;
            for (var i = 0; i < length; i++) {
                alertsService.showError({title: 'Failed to import', message: messages[i]});
            }
        }
    }
})();
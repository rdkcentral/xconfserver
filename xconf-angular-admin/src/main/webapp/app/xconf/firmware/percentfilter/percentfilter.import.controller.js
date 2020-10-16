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
        .module('app.percentfilter')
        .controller('PercentFilterImportController', controller);

    controller.$inject=['$log', '$state', 'alertsService', 'importService', 'percentFilterService', '$uibModal', 'firmwareConfigService', 'percentageBeanService', 'utilsService', '$scope', 'ENTITY_TYPE'];

    function controller($log, $state, alertsService, importService, percentFilterService, $uibModal, firmwareConfigService, percentageBeanService, utilsService, $scope, ENTITY_TYPE) {
        var vm = this;
        vm.filter = createEmptyPercentFilter();
        vm.firmwareConfigMap = {};
        vm.errorsById = {};
        vm.wrappedPercentFilter = createEmptyPercentFilter();
        vm.isOverwriteAll = false;
        vm.progressBarControl = importService.progressBarControl;
        vm.overwriteAll = overwriteAll;
        vm.viewPercentageBean = viewPercentageBean;
        vm.retrieveFile = retrieveFile;
        vm.importFilter = importFilter;
        vm.isObjectEmpty = utilsService.isNullOrUndefinedOrEmptyObject;
        vm.importGlobalPercentage = importGlobalPercentage;
        vm.importPercentageBean = importPercentageBean;
        vm.importAll = importAll;

        init();

        $scope.$on("import::error", function(event, data) {
            vm.errorsById[data.id] = data.message;
        });

        function init() {
            firmwareConfigService.getFirmwareConfigMap().then(function(resp) {
                vm.firmwareConfigMap = resp.data;
            }, function(error) {
                alertsService.showError({title: 'Exception', message: error.data.message});
            });
        }

        async function retrieveFile(fileName) {
            vm.filter = {};
            vm.errorsById = {};
            vm.wrappedPercentFilter = createEmptyPercentFilter();
            try {
                let file = await importService.openFile(fileName, null, this);
                processFile(file);
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function processFile(file) {
            vm.filter = getPercentFilterFromFile(file);
            if (vm.filter.percentageBeans && vm.filter.percentageBeans.length > 0) {
                vm.filter.percentageBeans.forEach(function(val, key) {
                    vm.wrappedPercentFilter.percentageBeans.push({
                        entity: val,
                        overwrite: false
                    });
                });
            }
            if (vm.filter.globalPercentage) {
                vm.wrappedPercentFilter.globalPercentage = {
                    entity: vm.filter.globalPercentage,
                    overwrite: false
                }
            }
        }

        function getPercentFilterFromFile(data) {
            try {
                var parsedFile = JSON.parse(data);
                if (!parsedFile.globalPercentage && !parsedFile.percentageBeans) {
                    alertsService.showError({title: 'Error', message: 'Invalid file'});
                    return {};
                }
                for (let i = 0; i < parsedFile.percentageBeans.length; i++) {
                    parsedFile.percentageBeans[i].distributions = percentageToPercentRange(parsedFile.percentageBeans[i]);
                }
                return parsedFile;
            } catch(e) {
                alertsService.showError({title: 'JSONStructureException', message: 'Percent Filter JSON has some errors! Please, check this file!'});
                $log.error('RoundRobinFilter JSON file is invalid! Please, check it!');
            }

        }

        function percentageToPercentRange(parsedFile) {
            let configEntries = parsedFile.distributions;

            let prevPercentEnd = 0.0;
            for (let i = 0; i < configEntries.length; i++) {
                if (configEntries[i].startPercentRange === undefined || configEntries[i].endPercentRange === undefined) {
                    configEntries[i].startPercentRange = +Number(prevPercentEnd).toFixed(3);
                    configEntries[i].endPercentRange = +Number(prevPercentEnd + configEntries[i].percentage).toFixed(3);
                }
                prevPercentEnd += +Number(configEntries[i].endPercentRange - configEntries[i].startPercentRange).toFixed(3);
            }
            return configEntries;
        }

        function importFilter(filter) {
            percentFilterService.saveFilter(filter).then(function () {
                alertsService.successfullySaved('Percent Filter');
                $state.go('percentfilter');
            }, function (reason) {
                alertsService.showError({title: 'Error', message: reason.data.message});
            });
        }

        function viewPercentageBean(percentageBean) {
            percentageBeanService.sortPercentageBeanFirmwareVersionsIfExistOrNot(percentageBean).then(function(firmwareVersions) {
                showViewPercentageBean(percentageBean, firmwareVersions);
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function showViewPercentageBean(percentageBean, firmwareVersions) {
            $uibModal.open({
                templateUrl: 'app/xconf/firmware/percentfilter/percentfilter.view.html',
                controller: 'PercentFilterViewController as vm',
                size: 'md',
                resolve : {
                    firmwareConfigMap: function() {
                        return vm.firmwareConfigMap;
                    },
                    percentageBean: function() {
                        return percentageBean;
                    },
                    firmwareVersions: function () {
                        return firmwareVersions;
                    }
                }
            });
        }

        function importPercentageBean(wrappedPercentageBean) {
            if (wrappedPercentageBean.overwrite) {
                percentageBeanService.update(wrappedPercentageBean.entity).then(function(resp) {
                    alertsService.successfullySaved(wrappedPercentageBean.entity.name);
                    utilsService.removeSelectedItemFromListById(vm.wrappedPercentFilter.percentageBeans, wrappedPercentageBean.entity.id);
                }, function (error) {
                    vm.errorsById[wrappedPercentageBean.entity.id] = error.data.message;
                });
            } else {
                percentageBeanService.create(wrappedPercentageBean.entity).then(function(resp) {
                    alertsService.successfullySaved(wrappedPercentageBean.entity.name);
                    utilsService.removeSelectedItemFromListById(vm.wrappedPercentFilter.percentageBeans, wrappedPercentageBean.entity.id);
                }, function(error) {
                    vm.errorsById[wrappedPercentageBean.entity.id] = error.data.message;
                });
            }
        }

        function importGlobalPercentage(wrappedPercentPercentage) {
                percentFilterService.saveFilter(wrappedPercentPercentage.entity).then(function(resp) {
                    alertsService.successfullySaved('Global Percent filter');
                    vm.wrappedPercentFilter.globalPercentage = {};
                }, function(error) {
                    vm.errorsById['GLOBAL_PERCENTAGE'] = error.data.message;
                });
        }

        function createEmptyPercentFilter() {
            return {
                globalPercentage: {},
                percentageBeans: []
            }
        }

        function overwriteAll() {
            vm.wrappedPercentFilter.percentageBeans.forEach(function(selectObject) {
                selectObject.overwrite = vm.isOverwriteAll;

            });
        }

        function importAll() {
            if (!utilsService.isNullOrUndefinedOrEmptyObject(vm.wrappedPercentFilter.globalPercentage)) {
                importGlobalPercentage(vm.wrappedPercentFilter.globalPercentage);
            }
            if (vm.wrappedPercentFilter.percentageBeans && vm.wrappedPercentFilter.percentageBeans.length > 0) {
                importService.importAllEntities(percentageBeanService, vm.wrappedPercentFilter.percentageBeans, null, null, ENTITY_TYPE.PERCENT_FILTER);
            }
        }
    }
})();
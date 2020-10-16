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
 * Created: 12/2/15  12:14 PM
 */
(function() {
    'use strict';

    angular
        .module('app.firmwareruletemplate')
        .controller('FirmwareRuleTemplateImportController', controller);

    controller.$inject=['$scope', '$log', '$uibModal', '$location', 'alertsService', 'firmwareRuleTemplateService', 'importService', 'utilsService', 'APPLICABLE_ACTION_TYPE'];

    function controller($scope, $log, $modal, $location, alertsService, firmwareRuleTemplateService, importService, utilsService, APPLICABLE_ACTION_TYPE) {
        var vm = this;

        vm.retrieveFile = retrieveFile;
        vm.importFirmwareRuleTemplate = importFirmwareRuleTemplate;
        vm.importAllFirmwareRuleTemplates = importAllFirmwareRuleTemplates;
        vm.getWrappedFirmwareRuleTemplatesByType = getWrappedFirmwareRuleTemplatesByType;
        vm.separateWrappedFirmwareRuleTemplatesByType = separateWrappedFirmwareRuleTemplatesByType;
        vm.overwriteAll = overwriteAll;
        vm.getSizeByType = getSizeByType;

        vm.viewFirmwareRuleTemplate = viewFirmwareRuleTemplate;
        vm.wrappedFirmwareRuleTemplates = null;
        vm.isOverwritten = false;
        vm.selectedActionType = APPLICABLE_ACTION_TYPE.RULE_TEMPLATE;
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.ruleTypeFirmwareRuleTemplates = [];
        vm.definePropertiesTypeFirmwareRuleTemplates = [];
        vm.blockingFilterTypeFirmwareRuleTemplates = [];
        vm.currentFirmwareRuleTemplates = [];
        vm.progressBarControl = importService.progressBarControl;

        async function retrieveFile(fileName) {
            clean();
            try {
                let file = await importService.openFile(fileName, null, this);
                init(file);
            } catch (e) {
                alertsService.showError({message: e});
            }
        }

        function init(file) {
            let firmwareRuleTemplates = importService.getEntitiesFromFile(file);
            utilsService.sortObjectsByPriority(firmwareRuleTemplates);
            vm.wrappedFirmwareRuleTemplates = importService.wrapToImport(firmwareRuleTemplates);
            separateWrappedFirmwareRuleTemplatesByType(vm.wrappedFirmwareRuleTemplates);
            getWrappedFirmwareRuleTemplatesByType(vm.selectedActionType);
            vm.isOverwritten = false;
            overwriteAll();
        }

        function viewFirmwareRuleTemplate(firmwareRuleTemplate) {
            $modal.open({
                templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-view.html',
                size: 'lg',
                controller: 'FirmwareRuleTemplateViewController as vm',
                resolve: {
                    obj: function () {
                        return firmwareRuleTemplate;
                    }
                }
            });
        }

        function importFirmwareRuleTemplate(wrappedFirmwareRuleTemplate) {
            if (wrappedFirmwareRuleTemplate.overwrite) {
                handleSavePromise(wrappedFirmwareRuleTemplate, firmwareRuleTemplateService.updateFirmwareRuleTemplate(wrappedFirmwareRuleTemplate.entity));
            } else {
                handleSavePromise(wrappedFirmwareRuleTemplate, firmwareRuleTemplateService.createFirmwareRuleTemplate(wrappedFirmwareRuleTemplate.entity));
            }
        }

        function handleSavePromise(wrappedFirmwareRuleTemplate, promise) {
            promise.then(
                function () {
                    alertsService.successfullySaved(wrappedFirmwareRuleTemplate.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.currentFirmwareRuleTemplates, wrappedFirmwareRuleTemplate.entity.id);
                    utilsService.removeSelectedItemFromListById(vm.wrappedFirmwareRuleTemplates, wrappedFirmwareRuleTemplate.entity.id);
                }, function (reason) {
                    var data = reason.data;
                    alertsService.showError({title: data.type, message: data.message});
                }
            );
        }

        function importAllFirmwareRuleTemplates() {
            importService.importAllEntities(firmwareRuleTemplateService, vm.wrappedFirmwareRuleTemplates, function(id) {
                utilsService.removeSelectedItemFromListById(vm.wrappedFirmwareRuleTemplates, id);
                utilsService.removeSelectedItemFromListById(vm.ruleTypeFirmwareRuleTemplates, id);
                utilsService.removeSelectedItemFromListById(vm.definePropertiesTypeFirmwareRuleTemplates, id);
                utilsService.removeSelectedItemFromListById(vm.blockingFilterTypeFirmwareRuleTemplates, id);
            });
        }

        function getWrappedFirmwareRuleTemplatesByType(type) {
            vm.selectedActionType = type;
            switch(type) {
                case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE:
                    vm.currentFirmwareRuleTemplates = vm.ruleTypeFirmwareRuleTemplates;
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE:
                    vm.currentFirmwareRuleTemplates = vm.definePropertiesTypeFirmwareRuleTemplates;
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE:
                    vm.currentFirmwareRuleTemplates = vm.blockingFilterTypeFirmwareRuleTemplates;
                    break;
            }
        }

        function separateWrappedFirmwareRuleTemplatesByType(wrappedFirmwareRuleTemplates) {
            angular.forEach(wrappedFirmwareRuleTemplates, function(value) {
                switch(value.entity.applicableAction.actionType) {
                    case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name:
                        vm.ruleTypeFirmwareRuleTemplates.push(value);
                        break;
                    case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name:
                        vm.definePropertiesTypeFirmwareRuleTemplates.push(value);
                        break;
                    case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name:
                        vm.blockingFilterTypeFirmwareRuleTemplates.push(value);
                        break;
                }
            });
        }

        function getSizeByType(type) {
            switch(type) {
                case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name:
                    return vm.ruleTypeFirmwareRuleTemplates.length;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name:
                    return vm.definePropertiesTypeFirmwareRuleTemplates.length;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name:
                    return vm.blockingFilterTypeFirmwareRuleTemplates.length;
            }
        }

        function clean() {
            vm.wrappedFirmwareRuleTemplates = [];
            vm.currentFirmwareRuleTemplates = [];
            vm.ruleTypeFirmwareRuleTemplates = [];
            vm.definePropertiesTypeFirmwareRuleTemplates = [];
            vm.blockingFilterTypeFirmwareRuleTemplates = [];
        }

        function overwriteAll() {
            angular.forEach(vm.wrappedFirmwareRuleTemplates, function (val, key) {
                val.overwrite = vm.isOverwritten;
            });
        }
    }
})();
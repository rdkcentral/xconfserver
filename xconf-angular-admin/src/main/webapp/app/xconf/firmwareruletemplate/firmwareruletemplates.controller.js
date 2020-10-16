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
        .module('app.firmwareruletemplate')
        .controller('FirmwareRuleTemplatesController', controller);

    controller.$inject=['$uibModal', '$stateParams', '$controller', 'dialogs', 'alertsService', 'utilsService', 'firmwareRuleTemplateService', 'APPLICABLE_ACTION_TYPE', 'firmwareConfigService', 'paginationService', '$scope', 'RULE_SEARCH_OPTIONS', 'SEARCH_OPTIONS'];

    function controller($modal, $stateParams, $controller, dialogs, alertsService, utilsService, firmwareRuleTemplateService, APPLICABLE_ACTION_TYPE, firmwareConfigService, paginationService, $scope, RULE_SEARCH_OPTIONS, SEARCH_OPTIONS) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.data = [];
        vm.availablePriorities = [];
        vm.templatesSize;
        vm.selectedActionType = $stateParams.actionType
            ? APPLICABLE_ACTION_TYPE.getActionTypeByName($stateParams.actionType)
            : APPLICABLE_ACTION_TYPE.RULE_TEMPLATE;
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.sizeOfEachType = [];
        vm.searchParam = {};
        vm.searchOptions = RULE_SEARCH_OPTIONS;

        vm.paginationStorageKey = 'firmwareRuleTemplatePageSize';
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageNumber = paginationService.getPageNumber();

        vm.getFirmwareRuleTemplates = getFirmwareRuleTemplates;
        vm.deleteFirmwareRuleTemplate = deleteFirmwareRuleTemplate;
        vm.getConfigNameById = getConfigNameById;
        vm.viewFirmwareRuleTemplate = viewFirmwareRuleTemplate;
        vm.exportFirmwareRuleTemplate = exportFirmwareRuleTemplate;
        vm.exportAllFirmwareRuleTemplates = exportAllFirmwareRuleTemplates;
        vm.exportAllFirmwareRuleTemplatesByType = exportAllFirmwareRuleTemplatesByType;
        vm.changePriority = changePriority;
        vm.getSizeByType = getSizeByType;
        vm.reloadPageByActionType = reloadPageByActionType;
        vm.searchRuleTemplatesByContext = searchRuleTemplatesByContext;
        vm.getActionNameForRuleCreation = getActionNameForRuleCreation;
        vm.getSizeOfAllTypes = getSizeOfAllTypes;
        vm.startParse = startParse;
        vm.getGeneralItemsNumber = getGeneralItemsNumber;

        reloadPageByActionType(vm.selectedActionType);

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            vm.searchParam[SEARCH_OPTIONS.APPLICABLE_ACTION_TYPE] = vm.selectedActionType.name;
            getFirmwareRuleTemplates();
        });

        function reloadPageByActionType(selectedActionType) {
            setSelectedActionType(selectedActionType);
            getFirmwareRuleTemplates();
        }

        function getFirmwareRuleTemplates() {
            firmwareRuleTemplateService.getFirmwareRuleTemplatesByActionTypePage(vm.pageNumber, vm.pageSize, vm.searchParam)
                .then(function (result) {
                    vm.data = result.data;
                    setTemplatesSize(result);
                    setAvailablePriorities(vm.templatesSize);
                    setSizeOfEachType(result);
                    paginationService.savePaginationSettingsInLocation(vm.pageNumber, vm.pageSize);
                }, function (reason) {
                    alertsService.showError({message: reason.data});
                });
        }

        function getConfigNameById(configId) {
            firmwareConfigService.getById(configId).then(function(response) {
                    return response.data.description;
                }, function(error) {
                    alertsService.showError({title: 'Error', message: 'Error by loading FirmwareConfig'});
                    return "";
                });
        }

        function viewFirmwareRuleTemplate(id) {
            firmwareRuleTemplateService.getFirmwareRuleTemplate(id)
                .then(function (result) {
                    var modalInstance = $modal.open({
                        templateUrl: 'app/xconf/firmwareruletemplate/firmwareruletemplate-view.html',
                        size: 'md',
                        controller: 'FirmwareRuleTemplateViewController as vm',
                        resolve: {
                            obj: function () {
                                return result.data;
                            }
                        }
                    });
                }, function(reason) {
                    var data = reason.data;
                    alertsService.showError({message: data});
                });
        }

        function deleteFirmwareRuleTemplate(firmwareRuleTemplate) {
            dialogs
                .confirm('Delete confirmation', '<span class="break-word-inline">' + 'Are you sure you want to delete Firmware Rule Template ' + firmwareRuleTemplate.id + '?' + '</span>')
                .result.then().then(function (btn) {
                firmwareRuleTemplateService.deleteFirmwareRuleTemplate(firmwareRuleTemplate.id)
                    .then(function (result) {
                        alertsService.successfullyDeleted(firmwareRuleTemplate.id);
                        reloadPageByActionType(vm.selectedActionType);
                    }, function (reason) {
                        alertsService.showError({message: reason.data.message});
                    });
            }, function (btn) {
                //click cancel
            });
        }

        function exportFirmwareRuleTemplate(id) {
            firmwareRuleTemplateService.exportFirmwareRuleTemplate(id);
        }

        function exportAllFirmwareRuleTemplates() {
            firmwareRuleTemplateService.exportAllFirmwareRuleTemplates();
        }

        function exportAllFirmwareRuleTemplatesByType(type) {
            firmwareRuleTemplateService.exportAllFirmwareRuleTemplatesByType(type);
        }

        function changePriority(id, priority) {
            firmwareRuleTemplateService.changePriorities(id, priority).then(
                function(result){
                    getFirmwareRuleTemplates(vm.selectedActionType)
                }, function(reason) {
                    alertsService.showError({title: 'Error', message: reason.data.message});
                    getFirmwareRuleTemplates(vm.selectedActionType)
                }
            );
        }

        function setAvailablePriorities(size) {
            size = parseInt(size);

            vm.availablePriorities = [];
            for (var i = 1; i < size + 1; i++) {
                vm.availablePriorities.push(i);
            }
        }

        function setSizeOfEachType(result) {
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name);
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name);
            vm.sizeOfEachType[vm.APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name] = result.headers(vm.APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name);
        }

        function getSizeByType(type) {
            var size = vm.sizeOfEachType[type];
            return size ? size : 0;
        }

        function getSizeOfAllTypes() {
            var size = 0;
            for (var key in vm.APPLICABLE_ACTION_TYPE) {
                size += parseInt(getSizeByType(vm.APPLICABLE_ACTION_TYPE[key].name));
            }
            return size;
        }

        function setSelectedActionType(selectedActionType) {
            vm.selectedActionType = selectedActionType;
            vm.searchParam[SEARCH_OPTIONS.APPLICABLE_ACTION_TYPE] = selectedActionType.name;
        }

        function setTemplatesSize(result) {
            vm.templatesSize = result.headers('templateSizeByType');
        }

        function searchRuleTemplatesByContext() {
            firmwareRuleTemplateService.searchRuleTemplatesByContext(vm.searchContext, vm.selectedActionType.name).then(function(result) {
                vm.data = result.data;
                setTemplatesSize(result);
                setAvailablePriorities(vm.templatesSize);
                setSizeOfEachType(result);
            }, function(error) {
                alertsService.showError({title: 'Error', message: error.data.message});
            });
        }

        function getActionNameForRuleCreation(templateActionName) {
            var ruleActionName = templateActionName.replace('_TEMPLATE', '');
            return ruleActionName;
        }

        function startParse() {
            return !utilsService.isMapEmpty(vm.sizeOfEachType);
        }

        function getGeneralItemsNumber() {
            return getSizeByType(vm.selectedActionType.name);
        }
    }
})();
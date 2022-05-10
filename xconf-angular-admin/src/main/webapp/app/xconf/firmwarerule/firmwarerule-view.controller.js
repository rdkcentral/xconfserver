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
        .module('app.firmwarerule')
        .controller('FirmwareRuleViewController', controller);

    controller.$inject=['$uibModalInstance', 'obj', 'APPLICABLE_ACTION_TYPE', 'firmwareConfigService', 'alertsService', 'firmwareRuleTemplateService', 'FIRMWARE_RULE_CONNECTION_TYPE'];

    function controller($modalInstance, obj, APPLICABLE_ACTION_TYPE, firmwareConfigService, alertsService, firmwareRuleTemplateService, FIRMWARE_RULE_CONNECTION_TYPE) {
        var vm = this;
        vm.obj = obj;
        vm.dismiss = dismiss;

        vm.blockingFilterTemplates = [];
        vm.firmwareConfigs = {};

        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.FIRMWARE_RULE_CONNECTION_TYPE = FIRMWARE_RULE_CONNECTION_TYPE;

        init();

        function init() {
            switch(vm.obj.applicableAction.actionType) {
                case APPLICABLE_ACTION_TYPE.RULE.name:
                    if (vm.obj.applicableAction.configId) {
                        getConfig(vm.obj.applicableAction.configId);
                    }
                    if (vm.obj.applicableAction.configEntries) {
                        _.each(vm.obj.applicableAction.configEntries, function(item) {
                            getConfig(item.configId);
                        });
                    }
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES.name:
                    getBlockingFilterTemplates();
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER.name:
                    break;
            }
        }

        function getConfig(configId) {
            if (configId) {
                firmwareConfigService.getById(configId).then(function(response) {
                    vm.firmwareConfigs[configId] = response.data;
                }, function (error) {
                    alertsService.showError({title: 'Error', message: 'Error by loading FirmwareConfig'});
                });
            }
        }

        function getBlockingFilterTemplates() {
            firmwareRuleTemplateService.getFirmwareRuleTemplatesByActionType(APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name).then(
                function (result) {
                    var data = result.data;
                    var length = data.length;
                    for (var i = 0; i < length; i++) {
                        var checked = vm.obj.applicableAction.byPassFilters ? vm.obj.applicableAction.byPassFilters.indexOf(data[i].id) >= 0 : false;
                        vm.blockingFilterTemplates.push({
                            checked: checked,
                            filter: data[i]
                        });
                    }
                }, function (reason) {
                    alertsService.showError({message: reason.data});
                });
        }

        function dismiss() {
            $modalInstance.dismiss('close');
        }
    }
})();
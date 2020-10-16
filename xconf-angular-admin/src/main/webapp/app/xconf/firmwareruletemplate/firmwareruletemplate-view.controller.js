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
        .controller('FirmwareRuleTemplateViewController', controller);

    controller.$inject=['$uibModalInstance', 'obj', 'APPLICABLE_ACTION_TYPE', 'firmwareConfigService', 'alertsService', 'firmwareRuleTemplateService', 'utilsService'];

    function controller($modalInstance, obj, APPLICABLE_ACTION_TYPE, firmwareConfigService, alertsService, firmwareRuleTemplateService, utilsService) {
        var vm = this;
        vm.obj = obj;
        vm.dismiss = dismiss;
        vm.isPropertiesEmpty = isPropertiesEmpty;
        vm.APPLICABLE_ACTION_TYPE = APPLICABLE_ACTION_TYPE;
        vm.blockingFilterTemplates = [];

        init();

        function init() {
            switch(vm.obj.applicableAction.actionType) {
                case APPLICABLE_ACTION_TYPE.RULE_TEMPLATE.name:
                    break;
                case APPLICABLE_ACTION_TYPE.DEFINE_PROPERTIES_TEMPLATE.name:
                    getBlockingFilterTemplates();
                    break;
                case APPLICABLE_ACTION_TYPE.BLOCKING_FILTER_TEMPLATE.name:
                    break;
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

        function isPropertiesEmpty() {
            return utilsService.isMapEmpty(vm.obj.applicableAction.properties);
        }
    }
})();
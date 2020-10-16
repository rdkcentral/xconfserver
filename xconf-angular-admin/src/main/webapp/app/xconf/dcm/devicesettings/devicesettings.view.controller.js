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
        .controller('DeviceSettingsViewController', controller);

    controller.$inject=['$uibModalInstance', 'deviceSettings'];

    function controller($modalInstance, deviceSettings) {
        var vm = this;
        vm.deviceSettings = deviceSettings;
        vm.dismiss = dismiss;

        function dismiss() {
            $modalInstance.dismiss('close');
        }
    }
})();
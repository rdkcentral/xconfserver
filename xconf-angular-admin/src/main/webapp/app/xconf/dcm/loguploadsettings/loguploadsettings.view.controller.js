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
 * Created: 20.10.15  16:46
 */
(function() {
    'use strict';

    angular
        .module('app.loguploadsettings')
        .controller('LogUploadSettingsViewController', controller);

    controller.$inject=['$uibModalInstance', 'logUploadSettings', 'uploadRepositoryService'];

    function controller($modalInstance, logUploadSettings, uploadRepositoryService) {
        var vm = this;
        vm.uploadRepositories = [];

        vm.logUploadSettings = logUploadSettings;
        vm.dismiss = dismiss;

        init();

        function init() {
            getUploadRepositories();
        }

        function dismiss() {
            $modalInstance.dismiss('close');
        }

        function getUploadRepositories() {
            uploadRepositoryService.getAll().then(function(result) {
                vm.uploadRepositories = result.data;
            });
        }

    }
})();
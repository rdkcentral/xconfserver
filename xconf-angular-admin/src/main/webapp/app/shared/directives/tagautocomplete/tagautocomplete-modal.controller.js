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
        .module('app.directives')
        .controller('TagautocompleteModal', controller);

    controller.$inject=['$uibModalInstance', '$scope', 'data'];

    function controller($modalInstance, $scope, data) {
        var vm = this;
        vm.selectedTags = data.selectedTags;
        vm.data = data.data;
        vm.disableAutocomplete = data.disableAutocomplete;

        vm.cancel = cancel;
        vm.save = save;


        function save() {
            data.onSave(vm.selectedTags);
            cancel();
        }

        function cancel() {
            $modalInstance.close();
        }
    }
})();
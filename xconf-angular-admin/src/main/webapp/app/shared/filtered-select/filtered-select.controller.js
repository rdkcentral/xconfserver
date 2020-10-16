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
 * Author: mdolina
 * Created: 7/9/15
 */

(function() {
    'use strict';

    angular
        .module('app.filtered-select')
        .controller('FilteredSelect', controller);

    controller.$inject=['$uibModalInstance', '$scope', 'title', 'data', 'onSelect'];

    function controller($modalInstance, $scope, title, data, onSelect) {
        var vm = this;
        vm.title = title ? title : 'Values';
        vm.currentDataEntry = data;
        vm.query = '';

        vm.filterData = function(value) {
            return value.indexOf(vm.query) !== -1;
        };

        vm.selectItem = function(value) {
            onSelect(value);
            vm.cancel();
        };

        vm.cancel = function() {
            $modalInstance.close();
        };
    }
})();
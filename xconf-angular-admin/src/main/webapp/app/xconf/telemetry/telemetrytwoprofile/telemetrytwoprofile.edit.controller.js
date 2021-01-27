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
 (function(){
 	'use strict';

 	angular
 		.module('app.telemetrytwoprofile')
 		.controller('TelemetryTwoProfileEditController', controller);

	controller.$inject = ['$rootScope', '$scope', '$controller', 'PROTOCOL', 'telemetryTwoProfileService', '$stateParams', '$state', 'alertsService', 'utilsService'];

	function controller($rootScope, $scope, $controller, PROTOCOL, telemetryTwoProfileService, $stateParams, $state, alertsService, utilsService){
		var vm = this;

		angular.extend(vm, $controller('EditController',{
			$scope: $scope,
			mainPage: 'telemetrytwoprofiles',
			stateParameters: null
		}));

		vm.telemetryTwoProfile = null;
		vm.isNewTelemetryProfile = null;

		vm.save = save;

		init();

		function init() {
			if($stateParams.telemetryProfileId){
				vm.isNewTelemetryProfile = false;
				telemetryTwoProfileService.getTelemetryTwoProfile($stateParams.telemetryProfileId)
                .then(function(resp) {
                    if (resp) {
                        vm.telemetryTwoProfile = resp.data;
                    }
                }, alertsService.errorHandler);
			}
			else{
				vm.isNewTelemetryProfile = true
				vm.telemetryTwoProfile = {}
				vm.applicationType = $rootScope.applicationType

			}
		}

		function save() {
			if (is_valid(vm.telemetryTwoProfile)) {
				if (vm.isNewTelemetryProfile) {
					telemetryTwoProfileService.createTelemetryTwoProfile(vm.telemetryTwoProfile).then(handleCreateSuccessfulResponse, alertsService.errorHandler);
				} else {
					telemetryTwoProfileService.updateTelemetryTwoProfile(vm.telemetryTwoProfile).then(handleUpdateSuccessfulResponse, alertsService.errorHandler);
				}
			}
		}

		function is_valid(telemetryTwoProfile) {
			var missingFields = [];
			if (!telemetryTwoProfile.name) {
                missingFields.push('name');
            }

           	if (!telemetryTwoProfile.jsonconfig) {
                missingFields.push('jsonconfig');
            }

            if (missingFields.length > 0) {
                alertsService.showError({title: 'Error', message: 'Next fields are missing: ' + missingFields.join(", ")});
                return false;
            }
            return true;
		}

		function handleCreateSuccessfulResponse(response) {
            alertsService.showSuccessMessage({message: response.data.name + ' profile added.'});
            $state.go('telemetrytwoprofiles');
        }

       	function handleUpdateSuccessfulResponse(response) {
            alertsService.showSuccessMessage({message: response.data.name + ' profile updated.'});
            $state.go('telemetrytwoprofiles');
        }

		$scope.fileChanged = function() {
			var reader = new FileReader();
			reader.onload = function(e) {
			  $scope.$apply(function() {
			      $scope.vm.telemetryTwoProfile.jsonconfig = reader.result;
			  });
			};
			var csvFileInput = document.getElementById('fileInput');
			var jsonconfig = csvFileInput.files[0];
			reader.readAsText(jsonconfig);
		};
	}
 })();
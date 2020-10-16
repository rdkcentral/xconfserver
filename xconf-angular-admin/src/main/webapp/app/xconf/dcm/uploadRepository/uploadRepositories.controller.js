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
        .module('app.uploadRepository')
        .controller('UploadRepositoriesController', controller);

    controller.$inject = ['$scope', '$controller', 'uploadRepositoryService', 'alertsService', 'dialogs', 'utilsService', 'uploadRepositoriesSize', 'paginationService', 'EDIT_MODE'];

    function controller($scope, $controller, uploadRepositoryService, alertsService, dialogs, utilsService, uploadRepositoriesSize, paginationService, EDIT_MODE) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.uploadRepositories = [];
        vm.paginationStorageKey = 'uploadRepositoriesPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.uploadRepositoriesSize = parseInt(uploadRepositoriesSize);
        vm.EDIT_MODE = EDIT_MODE;
        vm.searchParam = {};
        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Name",
                        apiArgs: ['NAME']
                    }
                }
            ]
        };

        vm.deleteUploadRepository = deleteUploadRepository;
        vm.changePageSize = changePageSize;
        vm.getSize = getSize;
        vm.getUploadRepositories = getUploadRepositories;
        vm.exportAll = uploadRepositoryService.exportAll;
        vm.exportOne = uploadRepositoryService.exportOne;
        vm.startParse = startParse;


        init();

        function init() {
            getUploadRepositories(vm.pageNumber, vm.pageSize);
        }

        $scope.$on('$locationChangeSuccess', function () {
            if (paginationService.paginationSettingsInLocationHaveChanged(vm.pageNumber, vm.pageSize)) {
                vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
                vm.pageSizeModel = vm.pageSize;
                paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
                vm.pageNumber = paginationService.getPageNumber();
                init();
            }
        });

        $scope.$on('search-entities', function(event, data) {
            vm.searchParam = data.searchParam;
            getUploadRepositories(vm.pageNumber, vm.pageSize);
        });

        function getUploadRepositories(pageNumber, pageSize) {
            uploadRepositoryService.getPage(pageSize, pageNumber, vm.searchParam).then(function(resp) {
                vm.uploadRepositories = resp.data;
                vm.uploadRepositoriesSize = resp.headers('numberofitems');
                paginationService.savePaginationSettingsInLocation(pageNumber, pageSize);
            }, function(error) {
                alertsService.showError({title: "Error", message: 'Error on loading upload repositories'})
            });
        }

        function deleteUploadRepository(uploadRepository) {
            if (!uploadRepository) {
                alertsService.showError({title: 'Error', message: 'Repository does not present'});
                return;
            }

            var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete Upload Repository ' + uploadRepository.name + ' ? </span>');
            dialog.result.then(function (btn) {
                uploadRepositoryService.deleteUploadRepository(uploadRepository.id).then(function() {
                    utilsService.removeItemFromArray(vm.uploadRepositories, uploadRepository);
                    shiftItems();
                    alertsService.successfullyDeleted(uploadRepository.name);
                }, function(error) {
                    alertsService.showError({'title': 'Error', message: error.data});
                });
            });
        }

        function shiftItems() {
            vm.uploadRepositoriesSize--;
            var numberOfPagesAfterDeletion = Math.ceil((vm.uploadRepositoriesSize) / vm.pageSize);
            var pageNumber = vm.pageNumber > numberOfPagesAfterDeletion ? numberOfPagesAfterDeletion : vm.pageNumber;
            getUploadRepositories(pageNumber, vm.pageSize);
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getUploadRepositories(vm.pageNumber, vm.pageSize);
        }

        function getSize() {
            return vm.uploadRepositoriesSize;
        }

        function startParse() {
            return getSize() > 0;
        }
    }
})();
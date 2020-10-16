/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

(function() {
    'use strict';

    angular
        .module('app.change')
        .controller('ChangeController', controller);

    controller.$inject=['$scope', '$rootScope', 'changeService', 'alertsService', 'CHANGE_TYPE', 'utilsService', 'dialogs', 'paginationService', 'CHANGE_OPERATION', 'ENTITY_TYPE', '$controller', 'authUtilsService'];
    function controller($scope, $rootScope, changeService, alertsService, CHANGE_TYPE, utilsService, dialogs, paginationService, CHANGE_OPERATION, ENTITY_TYPE, $controller, authUtils) {
        var vm = this;

        angular.extend(vm, $controller('MainController', {
            $scope: $scope
        }));

        vm.currentChangeType = CHANGE_TYPE.PENDING;
        vm.CHANGE_TYPE = CHANGE_TYPE;
        vm.ENTITY_TYPE = ENTITY_TYPE;
        vm.changes = [];
        vm.changeDiffs = {};
        vm.approvedChangesSize = null;
        vm.pendingChangesSize = null;
        vm.paginationStorageKey = 'changesPageSize';
        vm.availablePageSizes = paginationService.getAvailablePageSizes();
        vm.pageSize = paginationService.getPageSize(vm.paginationStorageKey);
        vm.pageSizeModel = vm.pageSize;
        vm.pageNumber = paginationService.getPageNumber();
        vm.numPages = 0;
        vm.changesForMultipleOperation = [];
        vm.CHANGE_OPERATION = CHANGE_OPERATION;

        vm.isNullOrUndefinedOrEmptyObject = utilsService.isNullOrUndefinedOrEmptyObject;
        vm.errorMessageById = {};

        vm.searchOptions = {
            data: [
                {
                    "name": {
                        friendlyName: "Entity",
                        apiArgs: ['ENTITY']
                    }
                },
                {
                    "name": {
                        friendlyName: "User",
                        apiArgs: ["AUTHOR"]
                    }
                }
            ]
        };
        vm.cancel = cancel;
        vm.getChanges = getChanges;
        vm.getApprovedChanges = getApprovedChanges;
        vm.getEntityView = changeService.getEntityView;
        vm.getEntityName = changeService.getEntityName;
        vm.getSizeByType = getSizeByType;
        vm.getChangesByType = getChangesByType;
        vm.changePageSize = changePageSize;
        vm.updateChangeList = updateChangeList;
        vm.applySelectedChanges = applySelectedChanges;
        vm.isAddedToMultipleOperation = isAddedToMultipleOperation;
        vm.isEmptyString = utilsService.isEmptyString;
        vm.canWriteChangeAndTelemetry = canWriteChangeAndTelemetry;

        init();

        function init() {
            getChanges();
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
            var changePromise;
            if (vm.currentChangeType === CHANGE_TYPE.APPROVED) {
                changePromise = changeService.getFilteredApprovedChanges(vm.pageSize, vm.pageNumber, vm.searchParam);
            } else {
                changePromise = changeService.getFilteredChanges(vm.pageSize, vm.pageNumber, vm.searchParam);
            }
            changePromise.then(responseHandler, alertsService.errorHandler);
        });

        function getApprovedChanges() {
            cleanUpChangeIds(vm.currentChangeType, CHANGE_TYPE.APPROVED, vm.changesForMultipleOperation);
            vm.currentChangeType = CHANGE_TYPE.APPROVED;
            changeService.getFilteredApprovedChanges(vm.pageSize, vm.pageNumber, vm.searchParam).then(
                responseHandler, alertsService.errorHandler
            );
        }

        function responseHandler(resp) {
            vm.changes = resp.data;
            getSizes(resp);
        }

        function getChanges() {
            cleanUpChangeIds(vm.currentChangeType, CHANGE_TYPE.PENDING, vm.changesForMultipleOperation);
            vm.currentChangeType = CHANGE_TYPE.PENDING;
            changeService.getFilteredChanges(vm.pageSize, vm.pageNumber, vm.searchParam).then(
                responseHandler, alertsService.errorHandler
            );
        }

        function cancel(change) {
            var dlg = dialogs.confirm('Cancel confirmation', '<span class="break-word-inline">Are you sure you want to cancel change of ' + vm.getEntityName(change) + " entity? </span>");
            dlg.result.then(function(btn) {
                changeService.cancel(change.id).then(function (resp) {
                    alertsService.showSuccessMessage({message: 'Change of ' + vm.getEntityName(change) + ' entity successfully canceled'});
                    utilsService.removeItemFromListById(vm.changes, change.id);
                    utilsService.removeItemFromListById(vm.changesForMultipleOperation, change.id);
                    getSizes(resp);
                    vm.pageNumber = getPageNumberAfterUpdate(vm.currentChangeType, vm.pageSize, vm.pageNumber);
                    getChangesByType(vm.currentChangeType);
                }, alertsService.errorHandler);
            }, function() {
                // click cancel
            });
        }

        function getSizes(resp) {
            vm.approvedChangesSize = resp.headers('approvedChangesSize');
            vm.pendingChangesSize = resp.headers('pendingChangesSize');
        }

        function getSizeByType(type) {
            return type === CHANGE_TYPE.PENDING ? vm.pendingChangesSize : vm.approvedChangesSize;
        }

        function getChangesByType(type) {
            if (type === CHANGE_TYPE.PENDING) {
                getChanges();
            } else {
                getApprovedChanges();
            }
        }

        function changePageSize(pageSizeModel) {
            vm.pageSize = pageSizeModel;
            paginationService.saveDefaultPageSize(vm.pageSize, vm.paginationStorageKey);
            getChangesByType(vm.currentChangeType);
        }

        function getPageNumberAfterUpdate(currentPendingChangeType, pageSize, pageNumber) {
            var numberOfPagesAfterDeletion = Math.ceil((getSizeByType(currentPendingChangeType)) / pageSize);
            var newPageNumber = pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion !== 0 ? numberOfPagesAfterDeletion : pageNumber;
            return newPageNumber;
        }

        function updateChangeList(checked, change) {
           if (checked) {
               vm.changesForMultipleOperation.push(change);
           } else {
               utilsService.removeItemFromArrayWithDeepEquals(vm.changesForMultipleOperation, change);
           }
           if (!vm.isEmptyString(vm.errorMessageById[change.id])) {
               vm.errorMessageById[change.id] = '';
           }
        }

        function applySelectedChanges(operation) {
            var dlg = dialogs.confirm('Approve confirmation', '<span class="break-word-inline">Are you sure you want to ' + operation.toLowerCase() + ' ' + vm.changesForMultipleOperation.length + ' selected changes?</span>');
            dlg.result.then(function(btn) {
                applyChanges(vm.changesForMultipleOperation);
            }, function() {
                // click cancel
            });
        }

        function applyChanges(changes) {
            var changeIds = _.map(changes, function(change){return change.id});
            var changedPromise;
            if (vm.currentChangeType === vm.CHANGE_TYPE.PENDING) {
                changedPromise = changeService.approveChanges(changeIds);
            } else {
                changedPromise = changeService.revertChanges(changeIds);
            }
            changedPromise.then(function (resp) {
                vm.errorMessageById = resp.data;
                if (utilsService.isMapEmpty(vm.errorMessageById)) {
                    showSuccessApproveRevertMessage();
                } else {
                    alertsService.showError({title: 'Errors occurred when trying to revert selected changes'});
                }
                getChangesByType(vm.currentChangeType);
                cleanUpSelectedChanges(vm.changesForMultipleOperation, _.keys(vm.errorMessageById));
            }, function (error) {
                alertsService.showError({title: 'Error', message: error.data.message});
                getChangesByType(vm.currentChangeType);
            });
        }

        function showSuccessApproveRevertMessage() {
            if (vm.currentChangeType === vm.CHANGE_TYPE.PENDING) {
                alertsService.showSuccessMessage({message: 'Selected changes have been successfully approved and unselected changes canceled'});
            } else {
                alertsService.showSuccessMessage({message: 'Selected changes were reverted successfully'});
            }
        }

        function cleanUpSelectedChanges(selectedChanges, changeIdsWithError) {
            console.log(changeIdsWithError);
            _.each(selectedChanges, function(change) {
                if (!changeIdsWithError.includes(change.id)) {
                    utilsService.removeItemFromListById(selectedChanges, change.id);
                }
            });
        }

        function isAddedToMultipleOperation(change) {
            for(var i = 0; i < vm.changesForMultipleOperation.length; i++) {
                if (angular.equals(vm.changesForMultipleOperation[i], change)) {
                    return true
                }
            }
        }

        function cleanUpChangeIds(currentPendingChangeType, newPendingChangeType, changesForMultipleOperation) {
            if (currentPendingChangeType !== newPendingChangeType) {
                changesForMultipleOperation.length = 0;
                clearErrorMessages();
            }
        }

        function clearErrorMessages() {
            vm.errorMessageById = {};
        }

        function canWriteChangeAndTelemetry() {
            return authUtils.canWriteChangesByApplication($rootScope.applicationType) && authUtils.canWriteTelemetryByApplication($rootScope.applicationType);
        }
    }
})();
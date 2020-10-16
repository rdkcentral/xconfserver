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
angular.module('app.directives').directive('environmentModelViewDirective',
    ['modelService', 'environmentService', 'alertsService', 'ENTITY_TYPE', '$state', 'dialogs', '$log', 'utilsService', '$filter', 'paginationService', '$controller',
        function (modelService, environmentService, alertsService, ENTITY_TYPE, $state, dialogs, $log, utilsService, $filter, paginationService, $controller) {
        return {
            link: function (scope, element, attrs) {
                var entityService = null;
                angular.extend(scope, $controller('MainController', {
                    $scope: scope
                }));
                scope.entityEditLink = null;
                scope.entityImportLink = null;
                scope.entities = null;
                scope.entityName = null;
                scope.searchId = '';
                scope.paginationStorageKey = '';
                scope.pageSize = '';
                scope.pageNumber = '';
                scope.generalItemsNumber = 0;
                scope.searchParam = {};
                scope.searchOptions = {
                    data: [
                        {
                            "name": {
                                friendlyName: "Id",
                                apiArgs: ['ID']
                            }
                        },
                        {
                            "name": {
                                friendlyName: "Description",
                                apiArgs: ["DESCRIPTION"]
                            }
                        }
                    ]
                };

                scope.getPage = function() {
                    entityService.getPage(scope.pageNumber.value, scope.pageSize.value, scope.searchParam).then(function(result) {
                            scope.entities = result.data;
                            scope.generalItemsNumber = result.headers('numberofitems');
                            paginationService.savePaginationSettingsInLocation(scope.pageNumber.value, scope.pageSize.value);
                        }, function(error) {
                            alertsService.showError({title: 'Error', message: 'Error by loading ' + scope.entityType});
                        }
                    );
                };

                init();

                scope.$on('$locationChangeSuccess', function () {
                    if (paginationService.paginationSettingsInLocationHaveChanged(scope.pageNumber.value, scope.pageSize.value)) {
                        scope.pageSize = {value : paginationService.getPageSize(scope.paginationStorageKey)};
                        scope.pageNumber = {value: paginationService.getPageNumber()};
                        init();
                    }
                });

                scope.$on('search-entities', function(event, data) {
                    scope.searchParam = data.searchParam;
                    scope.getPage();
                });

                function init() {
                    if (scope.entityType === ENTITY_TYPE.MODEL) {
                        entityService  = modelService;
                        scope.entityEditLink = 'model-edit';
                        scope.entityImportLink = 'model-import';
                        scope.entityName = 'Models';
                        scope.paginationStorageKey = 'modelPageSize';
                        scope.pageSize = paginationService.getPageSize(scope.paginationStorageKey);
                        scope.pageNumber = paginationService.getPageNumber();
                    } else if(scope.entityType === ENTITY_TYPE.ENVIRONMENT) {
                        entityService = environmentService;
                        scope.entityEditLink = 'environment-edit';
                        scope.entityImportLink = 'environment-import';
                        scope.entityName = 'Environments';
                        scope.paginationStorageKey = 'environmentPageSize';
                        scope.pageSize = paginationService.getPageSize(scope.paginationStorageKey);
                        scope.pageNumber = paginationService.getPageNumber();
                    }

                    scope.pageSize = {value : paginationService.getPageSize(scope.paginationStorageKey)};
                    scope.pageNumber = {value :paginationService.getPageNumber()};

                    scope.getPage();
                }

                scope.goToEditPage = function(entityId) {
                    if (entityId) {
                        $state.go(scope.entityEditLink, {entityId: entityId});
                    } else {
                        $state.go(scope.entityEditLink);
                    }
                };

                scope.goToImportPage = function() {
                    $state.go(scope.entityImportLink);
                };

                scope.deleteEntity = function(entityId) {
                    var dialog = dialogs.confirm('Delete confirmation', '<span class="break-word-inline"> Are you sure you want to delete ' + scope.entityType + ' ' + entityId + ' ? </span>');
                    dialog.result.then(function (btn) {
                        entityService.deleteById(entityId).then(function (resp) {
                            alertsService.successfullyDeleted(entityId);
                            for(var i=0; i<scope.entities.length; i++) {
                                if (entityId === scope.entities[i].id) {
                                    scope.entities.splice(i, 1);
                                    break;
                                }
                            }
                            scope.shiftItems();
                        }, function (error) {
                            alertsService.showError({title: 'Error', message: error.data.message});
                        });
                    });
                };

                scope.exportOne = function(entityId) {
                    entityService.exportOne(entityId);
                };

                scope.exportAll = function() {
                    entityService.exportAll();
                };

                scope.shiftItems = function() {
                    var numberOfPagesAfterDeletion = Math.ceil((scope.getGeneralItemsNumber() - 1) / scope.pageSize.value);
                    scope.pageNumber.value = (scope.pageNumber > numberOfPagesAfterDeletion && numberOfPagesAfterDeletion > 0) ? numberOfPagesAfterDeletion : scope.pageNumber.value;
                    scope.getPage();
                };

                scope.startParse = function() {
                    return scope.getGeneralItemsNumber() > 0;
                };

                scope.getGeneralItemsNumber = function() {
                    return scope.generalItemsNumber;
                };

                scope.searchEntitiesById = function(entityId, pageSize, pageNumber) {
                    entityService.searchById(pageNumber, pageSize, entityId).then(function(resp) {
                        scope.entities = resp.data;
                    }, function(error) {
                        alertsService.showError(error.data.message);
                    });
                }
            },
            restrict: 'E',
            scope: {
                entityType: '='
            },
            templateUrl: 'app/shared/directives/environment-model/environment-model.view.directive.html'
        };
    }]);

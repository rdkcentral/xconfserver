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
angular.module('app.directives').directive('environmentModelImportDirective',
    ['modelService', 'environmentService', 'alertsService', 'ENTITY_TYPE', '$state', 'dialogs', '$log', 'utilsService', '$filter', 'importService', 'paginationService',
        function (modelService, environmentService, alertsService, ENTITY_TYPE, $state, dialogs, $log, utilsService, $filter, importService, paginationService) {
            return {
                link: function (scope, element, attrs) {
                    var entityService = null;
                    scope.entityLink = null;
                    scope.entities = null;
                    scope.entityName = null;
                    scope.retrieveFile = retrieveFile;
                    scope.importEntity = importEntity;
                    scope.importAllEntities = importAllEntities;
                    scope.entities = [];
                    scope.wrappedEntities = null;
                    scope.overwriteAll = overwriteAll;
                    scope.isOverwritten = false;
                    scope.progressBarControl = importService.progressBarControl;

                    scope.computeStartAndEndIndex = function() {
                        scope.startIndex = (scope.pageNumber.value - 1) * scope.pageSize.value;
                        scope.endIndex = scope.pageNumber.value * scope.pageSize.value;
                    };

                    scope.selectPage = function() {
                        paginationService.savePaginationSettingsInLocation(scope.pageNumber.value, scope.pageSize.value);
                        scope.computeStartAndEndIndex();
                    };

                    scope.getGeneralItemsNumber = function() {
                        return scope.wrappedEntities ? scope.wrappedEntities.length : 0;
                    };

                    scope.$on('$locationChangeSuccess', function () {
                        if (paginationService.paginationSettingsInLocationHaveChanged(scope.pageNumber.value, scope.pageSize.value)) {
                            scope.pageSize = {value : paginationService.getPageSize(scope.paginationStorageKey)};
                            scope.pageNumber = {value: paginationService.getPageNumber()};
                            scope.selectPage();
                        }
                    });

                    init();

                    function init() {
                        if (scope.entityType === ENTITY_TYPE.MODEL) {
                            entityService  = modelService;
                            scope.entityLink = 'models';
                            scope.entityName = 'Models';
                            scope.paginationStorageKey = 'modelPageSize';
                        } else if(scope.entityType === ENTITY_TYPE.ENVIRONMENT) {
                            entityService = environmentService;
                            scope.entityLink = 'environments';
                            scope.entityName = 'Environments';
                            scope.paginationStorageKey = 'environmentRulePageSize';
                        }

                        scope.pageSize = {value : paginationService.getPageSize(scope.paginationStorageKey)};
                        scope.pageNumber = {value :paginationService.getPageNumber()};
                    }

                    async function retrieveFile(fileName) {
                        scope.entities = null;
                        try {
                            let file = await importService.openFile(fileName, null, this);
                            scope.isOverwritten = false;
                            scope.wrappedEntities = importService.prepareEntitiesFromFile(file);
                            scope.selectPage();
                        } catch (e) {
                            alertsService.showError({message: e});
                        }
                    }

                    function importEntity(data) {
                        if (validateEntity(data.entity)) {
                            if (data.overwrite) {
                                entityService.update(data.entity).then(function () {
                                    alertsService.successfullySaved(data.entity.id);
                                    utilsService.removeSelectedItemFromListById(scope.wrappedEntities, data.entity.id);
                                }, function (reason) {
                                    alertsService.showError({message: reason.data.message, title: 'Unable to import'});
                                });
                            } else {
                                entityService.create(data.entity).then(function () {
                                    alertsService.successfullySaved(data.entity.id);
                                    utilsService.removeSelectedItemFromListById(scope.wrappedEntities, data.entity.id);
                                }, function (reason) {
                                    alertsService.showError({message: reason.data.message, title: 'Unable to import'});
                                });
                            }
                        }
                    }

                    function importAllEntities() {
                        importService.importAllEntities(entityService, scope.wrappedEntities);
                    }

                    function overwriteAll() {
                        angular.forEach(scope.wrappedEntities, function (val) {
                            val.overwrite = scope.isOverwritten;
                        });
                    }

                    function validateEntity(entity) {
                        if (!entity || !entity.id) {
                            alertsService.showError({title: 'Validation Exception', message: 'Id is not present'});
                            return false;
                        }
                        return true;
                    }
                },
                restrict: 'E',
                scope: {
                    entityType: '='
                },
                templateUrl: 'app/shared/directives/environment-model/environment-model.import.directive.html'
            };
        }]);

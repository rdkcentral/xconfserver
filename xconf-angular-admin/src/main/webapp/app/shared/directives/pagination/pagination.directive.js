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
 * Created by izluben on 20.02.16.
 */
(function () {
    "use strict";

    angular.module('app.directives').directive('xconfPagination', ['$timeout', 'paginationService',
        function($timeout, paginationService) {
        return {
            restrict: 'E',
            scope:{
                pageSize: '=',
                pageNumber: '=',
                paginationStorageKey: '=',
                getPage: '&',
                getGeneralItemsNumber: '&'
            },
            templateUrl: 'app/shared/directives/pagination/pagination.directive.html',
            link: function(scope) {
                scope.availablePageSizes = paginationService.getAvailablePageSizes();

                scope.$watch('pageSize', function() {
                    $timeout(function() {
                        if (paginationService.paginationSettingsInLocationHaveChanged(scope.pageNumber, scope.pageSize)) {
                            paginationService.saveDefaultPageSize(scope.pageSize, scope.paginationStorageKey);
                            scope.getPage();

                        }
                    }, 0);
                });

                scope.$watch('pageNumber', function() {
                    if (paginationService.paginationSettingsInLocationHaveChanged(scope.pageNumber, scope.pageSize)) {
                        scope.getPage();
                    }
                });
            }
        }
    }]);

})();
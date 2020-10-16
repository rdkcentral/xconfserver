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
(function () {
    "use strict";

    angular
        .module('app.percentfilter')
        .directive('distributionDirective', ['percentFilterValidationService', 'utilsService', function(percentFilterValidationService, utilsService) {
            function link(scope, element, attrs) {
                scope.validator = percentFilterValidationService;
                scope.removeDistribution = removeDistribution;
                scope.updatePercentageRange = updatePercentageRange;

                scope.validateDistributionConfigId = validateDistributionConfigId;
                scope.isValidStartRange = isValidStartRange;
                scope.isValidEndRange = isValidEndRange;

                scope.percentageError = '';
                scope.overlappingError = '';
                scope.configError = '';
                scope.duplicateError = '';
                scope.percentageRangeError = '';
                scope.startRangeError = '';
                scope.endRangeError = '';

                function removeDistribution(distributions, distribution, percentageBean) {
                    utilsService.removeItemFromArray(distributions, distribution);
                    if (distributions.length === 0) {
                        percentageBean.lastKnownGood = '';
                    }
                }

                function updatePercentageRange(distribution) {
                    cleanErrors();
                    if (!percentFilterValidationService.isValidDistributionPercentRanges(distribution)) {
                        scope.percentError = 'Percentage bounds should be within [0; 100] and contain up to three decimal points';
                    }
                    if (distribution.startPercentRange && distribution.endPercentRange
                        && !percentFilterValidationService.isValidStartAndEndPercentageValues(distribution)) {
                        scope.percentageRangeError = 'Start percentage should be less than end percentage'
                    }
                    if (percentFilterValidationService.percentRangesOverlapEachOther(distribution, scope.percentageBean.distributions)) {
                        scope.overlappingError = 'Distributions are overlapped by each other';
                    }
                    if (percentFilterValidationService.hasDuplicates(scope.percentageBean.distributions)) {
                        scope.duplicateError = 'Distributions have duplicates';
                    }
                    if (distribution) {
                        if (distribution.startPercentRange != undefined && distribution.endPercentRange != undefined) {
                            distribution.percentage = +Number(distribution.endPercentRange - distribution.startPercentRange).toFixed(3);
                        } else {
                            distribution.percentage = undefined;
                        }
                    }
                    return distribution;
                }

                function validateDistributionConfigId(distribution) {
                    if (!percentFilterValidationService.isValidDistributionConfig(distribution)) {
                        scope.configError = 'FirmwareConfig version is empty';
                    } else {
                        scope.configError = '';
                    }
                }

                function isValidStartRange(percentage) {
                    if (!percentage && percentage !== 0) {
                        scope.startRangeError = 'Start range should be specified';
                    } else {
                        scope.startRangeError = '';
                    }
                }

                function isValidEndRange(percentage) {
                    if (!percentage) {
                        scope.endRangeError = 'End range should be specified';
                    } else {
                        scope.endRangeError = '';
                    }
                }

                function cleanErrors() {
                    scope.configError = '';
                    scope.duplicateError = '';
                    scope.overlappingError = '';
                    scope.percentageError = '';
                    scope.percentageRangeError = '';
                    scope.endRangeError = '';
                    scope.startRangeError = '';
                }
            }

            return {
                restrict: 'E',
                scope: {
                    percentageBean: '=',
                    distribution: '=',
                    firmwareVersionSelectObjects: '=',
                    index: '='
                },
                templateUrl: 'app/xconf/firmware/percentfilter/distribution.directive.html',
                link: link
            };
        }]);
})();
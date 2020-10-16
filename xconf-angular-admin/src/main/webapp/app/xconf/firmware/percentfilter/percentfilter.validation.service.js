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
    'use strict';
    angular
        .module('app.percentfilter')
        .factory('percentFilterValidationService', percentFilterValidationService);

    percentFilterValidationService.$inject = ['utilsService', 'percentageBeanService'];

    function percentFilterValidationService(utilsService, percentageBeanService) {

        var vm = this;
        vm.percentageError = '';
        vm.firmwareVersionError = '';
        vm.distributionError = '';
        vm.distributionPercentageError = '';
        vm.lastKnownGoodError = '';
        vm.intermediateVersionError = '';

        vm.cleanErrors = cleanErrors;
        vm.isValidPercentage = isValidPercentage;
        vm.isValidFirmwareVersions = isValidFirmwareVersions;
        vm.isValidDistributionPercentages = isValidDistributionPercentages;
        vm.validateLastKnownGood = validateLastKnownGood;
        vm.isValidDistributionConfig = isValidDistributionConfig;
        vm.validateEnvirontment = validateEnvirontment;
        vm.validateModel = validateModel;
        vm.validateName = validateName;
        vm.validatePercentFilter = validatePercentFilter;
        vm.validatePercentageBean = validatePercentageBean;
        vm.validatePercentFilter = validatePercentFilter;
        vm.validateDistributionMinCheckList = validateDistributionMinCheckList;
        vm.cleanFirmwareVersionError = cleanFirmwareVersionError;
        vm.isValidDistributionPercentRanges = isValidDistributionPercentRanges;
        vm.percentRangesOverlapEachOther = percentRangesOverlapEachOther;
        vm.validateAllPercentageValues = validateAllPercentageValues;
        vm.sortDistributionsByStartRange = sortDistributionsByStartRange;
        vm.hasDuplicates = hasDuplicates;
        vm.isValidStartAndEndPercentageValues = isValidStartAndEndPercentageValues;
        return vm;

        function cleanErrors() {
            vm.percentageError = '';
            vm.firmwareVersionError = '';
            vm.lastKnownGoodError = '';
            vm.intermediateVersionError = '';
            vm.environmentError = '';
            vm.modelError = '';
            vm.nameError = '';
        }

        function cleanFirmwareVersionError() {
            vm.firmwareVersionError = '';
        }

        function validatePercentFilter(filter) {
            return isValidPercentage(filter.percentage);
        }

        function validatePercentageBean(percentageBean, selectedFirmwareVersions, firmwareVersions) {
            var isValid = validateName(percentageBean)
                && isValidDistributionPercentages(percentageBean)
                && validateLastKnownGood(percentageBean)
                && isValidFirmwareVersions(percentageBean, selectedFirmwareVersions, firmwareVersions)
                && validateDistributionMinCheckList(percentageBean, selectedFirmwareVersions, firmwareVersions);
            return isValid;
        }

        function isValidPercentage(percentage) {
            var isValid =  angular.isNumber(percentage)
                && percentage >= 0
                && percentage <= 100
                && isValidDecimalPoints(percentage);
            vm.percentageError = isValid ? '' : 'Percentage should be from 0 to 100 and contain up to three decimal points';

            return isValid;
        }

        function isValidFirmwareVersions(percentageBean, selectedFirmwareVersions, firmwareVersions) {
            if (!percentageBean.firmwareCheckRequired) {
                return true;
            }
            if (selectedFirmwareVersions.length === 0) {
                vm.firmwareVersionError = 'At least one Firmware Version should be selected';
                return false;
            }
            if (percentageBean.lastKnownGood) {
                var lkgConfig = utilsService.getItemFromListById(percentageBean.lastKnownGood, firmwareVersions);
                if (lkgConfig && selectedFirmwareVersions.indexOf(lkgConfig.firmwareVersion) === -1) {
                    vm.firmwareVersionError = 'Last Known Good Version should be selected';
                    return false;
                }
            }
            vm.firmwareVersionError = '';
            return true;
        }

        function isValidDistributionPercentages(percentageBean) {
            vm.totalDistributionPercentageError = '';
            for (var i = 0; i < percentageBean.distributions.length; i++) {
                var distributionEntry = percentageBean.distributions[i];
                if (!isValidDistributionPercentRanges(distributionEntry)
                    || !isValidStartAndEndPercentageValues(distributionEntry)
                    || percentRangesOverlapEachOther(distributionEntry, percentageBean.distributions)
                    || hasDuplicates(percentageBean.distributions)) {
                        return false;
                }
            }
            var totalPercentage = percentageBeanService.getTotalDistributionPercentage(percentageBean);
            if (totalPercentage > 100) {
                vm.totalDistributionPercentageError = 'Total percentage count should not be bigger than 100';
                return false;
            }
            return true;
        }

        function isValidStartAndEndPercentageValues(distribution) {
            return distribution.endPercentRange > distribution.startPercentRange;
        }

        function isValidDistributionPercentRanges(distribution) {
            return isValidPercentageRange(distribution.startPercentRange)
                && isValidPercentageRange(distribution.endPercentRange)
                && isValidPercentageRange(distribution.percentage);
        }

        function equalDistributions(distr1, distr2) {
            return distr1.configId === distr2.configId
                && distr1.startPercentRange === distr2.startPercentRange
                && distr1.endPercentRange === distr2.endPercentRange
                && distr1.percentage === distr2.percentage;
        }

        function hasDuplicates(distributions) {
            for (var i = 0; i < distributions.length; i++) {
                for (var j = 0; j < distributions.length; j++) {
                    if (equalDistributions(distributions[i], distributions[j]) && i !== j) {
                        return true;
                    }
                }
            }
            return false;
        }

        function percentRangesOverlapEachOther(distributionToCheck, distributions) {
            var sortedDistributions = angular.copy(distributions);
            sortDistributionsByStartRange(sortedDistributions);
            for (var i = 0; i < sortedDistributions.length; i++) {
                var distribution = sortedDistributions[i];
                if (!equalDistributions(distributionToCheck, distribution)
                    && distribution.startPercentRange <= distributionToCheck.startPercentRange && distributionToCheck.startPercentRange < distribution.endPercentRange
                    || distribution.startPercentRange > distributionToCheck.startPercentRange && distribution.startPercentRange < distributionToCheck.endPercentRange) {
                    return true;
                }
            }
            return false;
        }

        function sortDistributionsByStartRange(distributions) {
            if (distributions && distributions.length > 0) {
                distributions.sort(function(distribution1, distribution2) {
                    if (distribution1.startPercentRange > distribution2.startPercentRange) {
                        return 1;
                    } else if (distribution1.startPercentRange <distribution2.startPercentRange) {
                        return -1;
                    }
                    return 0;
                })
            }
        }

        function validateAllPercentageValues(percentageBean) {
            if (percentageBean && percentageBean.distributions) {
                for (var i = 0; i < percentageBean.distributions.length; i++) {
                    if (isValidDistributionPercentRanges(percentageBean.distributions[i])) {
                        return false;
                    }
                }
            }
            return true;
        }

        function validateLastKnownGood(percentageBean) {

            if (percentageBean.distributions
                && percentageBean.distributions.length > 0
                && percentageBeanService.getTotalDistributionPercentage(percentageBean) < 100 && !percentageBean.lastKnownGood) {
                vm.lastKnownGoodError = 'LastKnownGood firmware version should be specified if total distribution percentage < 100';
                return false;
            }
            vm.lastKnownGoodError = '';
            return true;
        }

        function isValidDistributionConfig(distribution) {
            if (distribution && !distribution.configId) {
                return false;
            }
            return true;
        }

        function validateEnvirontment(percentageBean) {
            if (!percentageBean.environment) {
                vm.environmentError = 'Environment should be specified';
                return false;
            }
            vm.environmentError = '';
            return true;
        }

        function validateModel(percentageBean) {
            if (!percentageBean.model) {
                vm.modelError = 'Model should be specified';
                return false;
            }
            vm.modelError = '';
            return true;
        }

        function validateName(percentageBean) {
            if (!percentageBean.name) {
                vm.nameError = 'Name should be specified';
                return false;
            }
            vm.nameError = '';
            return true;
        }

        function validateDistributionMinCheckList(percentageBean, selectedFirmwareVersions, firmwareConfigs) {
            if (!percentageBean.firmwareCheckRequired) {
                return true;
            }
            var missingDistributionVersions = [];
            for (var i = 0; i < percentageBean.distributions.length; i++) {
                var distributionConfig = utilsService.getItemFromListById(percentageBean.distributions[i].configId, firmwareConfigs);
                if (distributionConfig && selectedFirmwareVersions.indexOf(distributionConfig.firmwareVersion) === -1) {
                    missingDistributionVersions.push(distributionConfig.firmwareVersion);
                }
            }
            if (missingDistributionVersions.length > 0) {
                vm.firmwareVersionError = 'Distribution firmware version should be selected in MinCheck:' + missingDistributionVersions.join(', ');
                return false;
            }
            return true;
        }

        function isValidPercentageRange(value) {
            if (!isValidNumericRange(value, 0, 100) || !isValidDecimalPoints(value)) {
                return false;
            }
            return true;
        }

        function isValidNumericRange(value, start, end) {
            return utilsService.isNumeric(value)
                && parseFloat(value) >= start
                && parseFloat(value) <= end;
        }

        function isValidDecimalPoints(value) {
            var values = value.toString().split('.');
            var decimalNumbers = values[1];
            if (decimalNumbers && decimalNumbers.length > 3) {
                return false;
            }
            return true;
        }
    }

})();
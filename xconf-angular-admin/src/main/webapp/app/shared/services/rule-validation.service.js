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
 * Author: Stanislav Menshykov
 * Created: 2/9/16  2:18 PM
 */
(function() {
    'use strict';

    angular
        .module('app.services')
        .factory('ruleValidationService', service);

    service.$inject=['OPERATION', 'alertsService', 'utilsService'];

    function service(OPERATION, alertsService, utilsService) {

        // works for time in format hh:mm or hh:mm:ss
        function validateTime(timeString) {
            var time = timeString.split(':');
            var length = time.length;
            if (length < 2 || length > 3) {
                return 'Time must be in hh:mm or hh:mm:ss format';
            }
            if (!isHours(time[0])) {
                return 'Hours must be from 0 to 23';
            }
            if (!isMinutesOrSeconds(time[1])) {
                return 'Minutes must be from 0 to 59';
            }
            if (length == 3 && !isMinutesOrSeconds(time[2])) {
                return 'Seconds must be from 0 to 59';
            }

            return null;
        }

        function isHours(string) {
            return utilsService.isInt(string) && (0 <= parseInt(string) && parseInt(string) <= 23);
        }

        function isMinutesOrSeconds(string) {
            return utilsService.isInt(string) && (0 <= parseInt(string) && parseInt(string) <= 59);
        }

        function validate(ruleBuilderScope) {
            if (ruleBuilderScope.disableValidation) {
                return 0;
            }
            var rule = ruleBuilderScope.rule;
            if (ruleBuilderScope.showRelation) {
                if (!rule.relation) {
                    alertsService.showError({title: "Relation", message: "Please specify"});
                    return 1;
                }
            }

            var condition = ruleBuilderScope.rule.condition;

            switch (condition.operation) {
                case OPERATION.PERCENT:
                    var value = rule.condition.fixedArg.bean.value;
                    var doubleValue = value['java.lang.Double'];
                    var floatPercentRegExp = new RegExp(/^[0-9]+(\.[0-9]{1,10})?$/);
                    if ((!floatPercentRegExp.test(doubleValue) || doubleValue <= 0 || doubleValue >= 100)
                        && (doubleValue !== '' && doubleValue !== null && doubleValue !== undefined)) {
                        alertsService.showError({title: "ValidationException", message: "FixedArg by PERCENT operation must be from 0.0000000001 to 99.9999999999"});
                        return 1;
                    }
            }

            if (condition.operation !== OPERATION.PERCENT) {
                if (!condition.freeArg.name) {
                    alertsService.showError({message: "FreeArg is required"});
                    return 1;
                }
            }
            if (condition.operation !== OPERATION.EXISTS) {
                var isFixedArgExist = false;
                if (condition.operation === OPERATION.IN) {
                    isFixedArgExist = condition.fixedArg.collection.value && condition.fixedArg.collection.value.length > 0;
                } else if (angular.isObject(condition.fixedArg.bean.value)) {
                    var fixedArgValues = _.values(condition.fixedArg.bean.value);
                    var notEmptyValues = _.filter(fixedArgValues, function(fixedArgValue) {
                        return !utilsService.isNullOrUndefined(fixedArgValue) && fixedArgValue.length !== 0;
                    });
                    isFixedArgExist = notEmptyValues.length !== 0;
                } else {
                    isFixedArgExist = !utilsService.isNullOrUndefined(condition.fixedArg.bean.value) && condition.fixedArg.bean.value !== null;
                }
                if (ruleBuilderScope.fixedArgRequired && !isFixedArgExist) {
                    alertsService.showError({message: "FixedArg is required"});
                    return 1;
                } else {
                    if (condition.operation === OPERATION.LTE || condition.operation === OPERATION.GTE) {
                        var validationMessage = validateTime(_.values(condition.fixedArg.bean.value)[0]);
                        if (validationMessage !== null) {
                            alertsService.showError({title: "FreeArg", message: validationMessage});
                            return 1;
                        }
                    }
                }
            }

            if (!isUniqueRule(rule, ruleBuilderScope.data)) {
                alertsService.showError({title: "Rule", message: "Rule conditions must be unique. You can not add same conditions"});
                return 1;
            }

            return 0;
        }

        function isUniqueRule(rule, data) {
            var newCondition = rule.condition;
            if (data.condition) {
                if (angular.equals(newCondition.freeArg.name, data.condition.freeArg.name)
                    && angular.equals(newCondition.freeArg.type, data.condition.freeArg.type)
                    && angular.equals(newCondition.fixedArg, data.condition.fixedArg)
                    && angular.equals(newCondition.operation, data.condition.operation)
                    && isEqualRelations(rule.relation, data.relation)) {
                    return false;
                }
            } else {
                var isUnique = true;
                var n = 0;
                while (isUnique && data.compoundParts && (data.compoundParts.length - n) >= 1) {
                    if (!isUniqueRule(rule, data.compoundParts[n])) {
                        isUnique = false;
                    }
                    n++;
                }
                return isUnique;
            }
            return true;
        }

        function isEqualRelations(relation1, relation2) {
            if (!relation1 || !relation2) {
                return true;
            }

            return angular.equals(relation1, relation2);
        }

        return {
            validate: validate
        };

    }
})();

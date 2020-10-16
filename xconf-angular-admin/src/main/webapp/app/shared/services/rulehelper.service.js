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
 * Author: rdolomansky
 * Created: 5/15/15  1:58 PM
 */

(function() {
    'use strict';

    angular
        .module('app.services')
        .factory('ruleHelperService', service);

    service.$inject=['OPERATION', 'namespacedListService', 'environmentService', 'modelService', 'NAMESPACED_LIST_TYPE', 'firmwareConfigService'];

    function service(OPERATION, namespacedListService, environmentService, modelService, NAMESPACED_LIST_TYPE, firmwareConfigService) {
        var _rule = {
            "negated": false,
            "relation": null,
            "compoundParts": [],
            "condition": null
        };

        return {
            flattenRule: flattenRule,
            isCompound: isCompound,
            addRule: addRule,
            copyRule: copyRule,
            createEmptyRule: createEmptyRule,
            ruleToString: ruleToString,
            renderValue: renderValue,
            getFixedArgValuesFromRuleByFreeArg: getFixedArgValuesFromRuleByFreeArg,
            buildFirmwareConfigsBySupportedModels: buildFirmwareConfigsBySupportedModels,
            watchRuleRemoveOperation: watchRuleRemoveOperation,
            buildRepresentation: buildRepresentation,
            buildNamespacedListData: buildNamespacedListData,
            buildFirmwareConfigs: buildFirmwareConfigs,
            isSearchContextNotEmpty: isSearchContextNotEmpty,
            equalRules: equalRules
        };


        function flattenRule(rule) {
            if (isCompound(rule)) {
                return rule.compoundParts;
            } else {
                return [rule];
            }
        }

        function isCompound(rule) {
            return !rule.condition;
        }

        function addRule(rule, rootRule) {
            var clonedRootRule = angular.copy(rootRule);
            if (clonedRootRule.condition) {
                $.extend(clonedRootRule, angular.copy(_rule));
                clonedRootRule.compoundParts = (clonedRootRule.compoundParts || []).concat(copyRule(rootRule), rule);
            } else {
                if (clonedRootRule.compoundParts && clonedRootRule.compoundParts.length >= 1) {
                    clonedRootRule.compoundParts = (clonedRootRule.compoundParts || []).concat(angular.copy(rule));
                } else {
                    $.extend(clonedRootRule, rule);
                }
            }
            return clonedRootRule;
        }

        function copyRule(rule) {
            var clonedRule = angular.copy(rule);
            // to avoid additional properties in rule,e.g. action
            return {
                "negated": clonedRule.negated,
                "relation": clonedRule.relation,
                "compoundParts": clonedRule.compoundParts,
                "condition": clonedRule.condition
            };
        }

        function createEmptyRule() {
            return angular.copy(_rule);
        }

        function ruleToString(rule) {
            var result = '(';
            if (rule.compoundParts && rule.compoundParts.length !== 0) {
                angular.forEach(rule.compoundParts, function(val, key) {
                    if (val.relation) {
                        result += ' ' + val.relation + ' ';
                    }

                    result += '(';
                    result += val.condition.freeArg.name;
                    if (val.negated) {
                        result += ' not';
                    }
                    result += ' ' + val.condition.operation;
                    result += ' ' + val.condition.fixedArg.bean.value;
                    result += ')';
                });
            } else {
                result += '(';
                result += rule.condition.freeArg.name;
                if (rule.negated) {
                    result += ' NOT';
                }
                result += ' ' + rule.condition.operation;
                result += ' ' + rule.condition.fixedArg.bean.value;
                result += ')';

            }
            result += ')';
            return result;
        }

        function renderValue(condition) {
            if (!condition) {
                return;
            }
            switch (condition.operation) {
                case OPERATION.IN:
                    var result = condition.fixedArg.collection.value;
                    return result ? result.join(", ") : '';
                case OPERATION.EXISTS:
                    break;
                default :
                    var value = condition.fixedArg.bean.value;
                    if (angular.isObject(value)) {
                        return value[resolveFixedArgType(value)];
                    } else {
                        return value;
                    }
            }
        }

        function resolveFixedArgType(value) {
            for (var type in value) {
                return type;
            }
        }

        function getFixedArgValuesFromRuleByFreeArg(rule, freeArgToFind, result) {
            var condition = rule.condition;
            if (rule.compoundParts && rule.compoundParts.length) {
                _.each(rule.compoundParts, function(value) {
                    getFixedArgValuesFromRuleByFreeArg(value, freeArgToFind, result);
                });
            } else {
                if (condition && condition.freeArg && condition.freeArg.name === freeArgToFind) {
                    switch (condition.operation) {
                        case OPERATION.IN:
                            var values = condition.fixedArg.collection.value;
                            if (values) {
                                _.each(values, function(value) {
                                    result.push(value);
                                });
                            }
                            break;

                        default:
                            result.push(_.values(condition.fixedArg.bean.value)[0]);
                            break;
                    }
                }
            }
        }

        function buildFirmwareConfigsBySupportedModels(rule, allFirmwareConfigs) {
            var firmwareConfigs = [];
            var selectedModels = [];
            getFixedArgValuesFromRuleByFreeArg(rule, 'model', selectedModels);
            if (selectedModels.length > 0) {
                _.each(allFirmwareConfigs, function(firmwareConfig) {
                    var intersection = _.intersection(selectedModels, firmwareConfig.supportedModelIds);
                    if (intersection.length) {
                        if (firmwareConfigs.indexOf(firmwareConfig) === -1) {
                            firmwareConfigs.push(firmwareConfig);
                        }
                    }
                });
            } else {
                _.each(allFirmwareConfigs, function(firmwareConfig) {
                    firmwareConfigs.push(firmwareConfig);
                });
            }
            return firmwareConfigs;
        }

        function watchRuleRemoveOperation(isValidCondition, rule, obj) {
            if (isCompound(rule)) {
                var compoundParts = rule.compoundParts || [];
                for (var i = 0; i < compoundParts.length; i++) {
                    var currentRule = compoundParts[i];
                    if (currentRule === obj.rule) {
                        if (i === 0) {
                            if (compoundParts.length === 1) {
                                $.extend(rule, createEmptyRule());
                            } else {
                                compoundParts.shift();
                                delete rule.compoundParts[0].relation;
                                reorganizeRuleIfNotCompound(rule, compoundParts);
                            }
                        } else {
                            compoundParts.splice(i, 1);
                            reorganizeRuleIfNotCompound(rule, compoundParts);
                        }
                        if ((!rule.compoundParts || rule.compoundParts.length === 0) && !rule.condition) {
                            isValidCondition = false;
                        }
                    }
                }
            } else {
                if (obj.rule === rule) {
                    $.extend(rule, createEmptyRule());
                    isValidCondition = false;
                }
            }
            return {
                rule: rule,
                isValidCondition: isValidCondition
            }
        }

        function reorganizeRuleIfNotCompound(rule, compoundParts) {
            if (compoundParts.length === 1) {
                var clonedFirmwareRule = angular.copy(rule);
                $.extend(clonedFirmwareRule, compoundParts[0]);
                delete clonedFirmwareRule.compoundParts;
                rule = clonedFirmwareRule;
            }
        }

        function buildRepresentation(application) {
            var result = {env: [], model: [], firmwareVersion: []};
            environmentService.getAll().then(function(resp) {
                _.each(resp.data, function(entity) { result['env'].push(entity.id); });
            }, function(error) {});
            modelService.getAll().then(function(resp) {
                _.each(resp.data, function(entity) { result['model'].push(entity.id); });
            }, function(error) {});
            firmwareConfigService.getAll(application).then(function(resp) {
                _.each(resp.data, function(firmwareConfig) { result['firmwareVersion'].push(firmwareConfig.firmwareVersion)});
            });
            return result;
        }

        function buildNamespacedListData() {
            var macRules = {name: 'MAC Lists', data: []};
            var ipFilters = {name: 'IP Lists', data: []};
            namespacedListService.getAllNamespacedLists().then(function(response) {
                _.each(response.data, function(entity) {
                    switch (entity.typeName) {
                        case NAMESPACED_LIST_TYPE.MAC_LIST:
                            macRules.data.push(entity.id);
                            break;
                        case NAMESPACED_LIST_TYPE.IP_LIST:
                            ipFilters.data.push(entity.id);
                            break;
                    }
                });
            });
            return [macRules, ipFilters];
        }

        function buildFirmwareConfigs(data, allFirmwareConfigs) {
            var firmwareConfigs = buildFirmwareConfigsBySupportedModels(data, allFirmwareConfigs);
            var firmwareVersions = [];
            _.each(firmwareConfigs, function(firmwareConfig) {firmwareVersions.push(firmwareConfig.firmwareVersion)});
            return firmwareVersions;
        }

        function isSearchContextNotEmpty(searchContext) {
            var searchContextValues = _.values(searchContext);
            for(var i = 0; i < searchContextValues.length; i++) {
                if (searchContextValues[i] && searchContextValues[i] !== '') {
                    return true;
                }
            }
            return false;
        }

        function equalRules(rule, rule2) {
            if (rule.condition.freeArg.name !== rule2.condition.freeArg.name) {
                return false;
            }
            if (rule.condition.operation !== rule2.condition.operation) {
                return false;
            }
            if (rule.condition.operation === OPERATION.IN && rule2.condition.operation === OPERATION.IN) {
                var collection = rule.condition.fixedArg.collection.value;
                var collection2 = rule2.condition.fixedArg.collection.value;
                if (collection.length !== collection2.length) {
                    return false;
                }
                if (_.intersection(collection, collection2).length !== collection.length) {
                    return false;
                }
            } else {
                if (rule.condition.fixedArg.bean.value !== rule2.condition.fixedArg.bean.value) {
                    return false;
                }
            }
            if (rule.relation !== rule2.relation) {
                return false;
            }
            return true;
        }
    }
})();

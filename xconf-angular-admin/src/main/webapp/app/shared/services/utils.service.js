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
 * Author: Oleksandr Baturynskyi
 * Created: 01/21/15  11:00 AM
 */

(function () {
    'use strict';
    angular.module('app.services')
        .factory('utilsService', UtilsService);

    function UtilsService() {

        var isEmptyString = function (stringToTest) {
            if (!$.isEmptyObject(stringToTest) && angular.isDefined(stringToTest)) {
                return (stringToTest.replace(/\s/g, '').length === 0);
            } else {
                return true;
            }
        };

        var getString = function (str) {
            return (angular.isDefined(str) && str != null) ? str.toString() : '';
        };

        var arrayHasDuplicates = function (array) {
            var valuesSoFar = {};
            for (var i = 0; i < array.length; ++i) {
                var value = array[i];
                if (Object.prototype.hasOwnProperty.call(valuesSoFar, value)) {
                    return true;
                }
                valuesSoFar[value] = true;
            }
            return false;
        };

        /**
         *   This method checks if JS object (Map) is empty (it has only its prototype properties)
         */
        function isMapEmpty(object) {
            if (Object.getOwnPropertyNames(object).length === 0) {
                //is empty
                return true;
            }
            return false;
        }

        /**
         * Check if value not null and defined
         * @param value
         * @returns {boolean}
         */
        function isNullOrUndefined(value) {
            if (value == null || angular.isUndefined(value)) {
                return true;
            }
            return false;
        }

        /**
         * Check if object null and undefined or object (Map) is empty (it has only its prototype properties)
         * @param value
         * @returns {boolean}
         */
        function isNullOrUndefinedOrEmptyObject(object) {
            return isNullOrUndefined(object) || isMapEmpty(object);
        }

        /**
         * Check if any value in array of fields not null and defined
         * @param value
         * @returns {boolean}
         */
        function isNullOrUndefinedOrEmptyStringArrayOfValues(array) {
            var valid = false;
            valid = isNullOrUndefined(array);

            if (valid === false) {
                for (var i = 0; i < array.length; i++) {
                    valid = isNullOrUndefined(array[i]);
                    if (valid === true) {
                        break;
                    }
                }
            }

            return valid;
        }

        function removeNullOrUndefinedOrEmptyStringValuesFromArray(array) {
            var length = array.length;
            for (var i = length - 1; i >= 0; i--) {
                var value = array[i];
                if (isNullOrUndefinedOrEmptyOrWhiteSpaceString(value)) {
                    array.splice(i, 1);
                }
            }
        }

        /**
         * Checks whether array contains any elements
         * @param array
         * @returns {boolean}
         */
        function isNullOrUndefinedOrEmpty(array) {
            var result = false;
            result = isNullOrUndefined(array);

            if (result === false) {
                return array.length <= 0;
            }

            return result;
        }

        var checkObjHasDeepPath = function(obj, keys) {
            // check that obj has deep path
            // for example keys=['child', 'childOfChild'] => check that obj.child.childOfChild exists
            var next = keys.shift();
            return obj.hasOwnProperty(next) && (!keys.length || checkObjHasDeepPath(obj[next], keys));
        };

        function isNullOrUndefinedOrEmptyOrWhiteSpaceString (value) {
            return isNullOrUndefined(value) || isEmptyString(value);
        }

        /**
         * Check value, should be only chars and numbers without special symbols
         * @param name
         * @returns {boolean}
         */
        function isAlphaNumericAndNotEmpty(value) {
            var alphanumerical = /^[a-zA-z0-9_]+$/;
            if (!isNullOrUndefined(value) && !alphanumerical.exec(value) || value === '') {
                return false;
            }
            return true;
        }

        function convertObjectToArray(obj) {
            if (obj instanceof Object && !Array.isArray(obj)) {
                var result = [];
                for (var key in obj) {
                    var item = {};
                    item.key = key;
                    item.value = obj[key];
                    result.push(item);
                }
                return result;
            } else {
                return obj;
            }
        }

        function convertArrayToObject(array) {
            var result = {};
            for (var i = 0; i < array.length; i++) {
                result[array[i].key] = array[i].value;
            }
            return result;
        }

        function removeItemFromArray(array, value) {
            for (var i = 0; i < array.length; i++) {
                if (array[i] === value) {
                    array.splice(i, 1);
                    return i;
                }
            }
            return -1;
        }

        function removeItemFromArrayWithDeepEquals(array, value) {
            for (var i = 0; i < array.length; i++) {
                if (angular.equals(array[i], value)) {
                    array.splice(i, 1);
                    return i;
                }
            }
            return -1;
        }

        function removeMultipleItemsFromArray(array, itemsList) {
            if(!Array.isArray(itemsList)) itemsList = [itemsList];
            var length = itemsList.length;
            for(var i = 0; i < length; i++) {
                var indexToRemove = array.indexOf(itemsList[i]);
                if(indexToRemove > -1) array.splice(indexToRemove, 1);
            }
        }

        function removeNonAlpha(string) {
            return string.replace(/\W/g, '');
        }

        function hashCode(string) {
            var hash = 0, i, chr, len;
            if (string.length == 0) return hash;
            for (i = 0, len = string.length; i < len; i++) {
                chr   = string.charCodeAt(i);
                hash  = ((hash << 5) - hash) + chr;
                hash |= 0; // Convert to 32bit integer
            }
            return hash;
        }

        function removeItemFromListById(list, id) {
            var length = list.length;
            for (var i = 0; i < length; i++) {
                if (list[i].id === id) {
                    list.splice(i, 1);
                    return i;
                }
            }
            return -1;
        }

        function removeSelectedItemFromListById(list, id) {
            var length = list.length;
            for (var i = 0; i < length; i++) {
                if (list[i].entity.id === id) {
                    list.splice(i, 1);
                    return i;
                }
            }
            return -1;
        }

        function sortObjectsById(objectsArray) {
            objectsArray.sort(function(a, b) {
                return a.id.localeCompare(b.id);
            });
        }

        function sortObjectsByPriority(objectsArray) {
            objectsArray.sort(function(a, b) {
                var priorityA = isInt(a.priority) ? parseInt(a.priority) : 0;
                var priorityB = isInt(b.priority) ? parseInt(b.priority) : 0;
                return priorityA - priorityB;
            });
        }

        function isInt(value) {
            return $.isNumeric(value) && value.toString().indexOf(".") === -1;
        }

        function getItemFromListById(id, list) {
            var length = list.length;
            for (var i = 0; i < length; i++) {
                if (id === list[i].id) {
                    return list[i];
                }
            }

            return null;
        }

        function spliceArray(array, size) {
            var newArr = [];
            for (var i=0; i<array.length; i+=size) {
                newArr.push(array.slice(i, i+size));
            }
            return newArr;
        }

        function saveJsonContent(jsonContent, filename) {
            var data = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(jsonContent));
            var a = document.createElement('a');
            a.href = 'data:' + data;
            a.download = filename + '.json';
            a.click();
        }

        function getEntityFromArrayByFieldValue(fieldName, fieldValue, values) {
            return _.find(values, function(firmareConfig) {
                if (firmareConfig[fieldName] === fieldValue) {
                    return fieldValue;
                }
            });
        }

        function hasValue(value) {
            var result = angular.isDefined(value) && !$.isEmptyObject(value);
            return result;
        }

        function chunkData(dataArray, chunkCount) {
            var result = [];
            var chunkSize = Math.round(dataArray.length / chunkCount);
            var sliceStartNumber = 0;
            for (var i = 0; i < chunkCount; i++) {
                if ((i + 1) === chunkCount) {
                    result.push(dataArray.slice(sliceStartNumber, dataArray.length));
                } else {
                    result.push(dataArray.slice(sliceStartNumber, sliceStartNumber + chunkSize));
                }
                sliceStartNumber += chunkSize;
            }
            return result;
        }

        function addClass(element, addClass) {
            if (addClass) {
                element.addClass(addClass);
            }
        }

        function removeClass(element, removeClass) {
            if (removeClass) {
                element.removeClass(removeClass);
            }
        }
        
        function splitListByPercentage(list, percentage) {
            var blocks = [];
            var newList = list.slice();
            var x = parseInt(newList.length * percentage / 100);
            x = (x < 1) ? 1 : x;
        
            while(newList.length) {
                blocks.push(newList.splice(0, x));
            }
        
            return blocks;
        }

        function generateRequestList(dataList, options) {
            var requests = [];
            angular.forEach(dataList, function(data) {
                var request = {};
                request.url = options.url;
                request.method = options.method;
                request.data = data;
                requests.push(request);
            });
            return requests;
        }

        function isNumeric(value) {
            return !isNaN(parseFloat(value)) && isFinite(value);
        }

        function parseCronExpression(cronExpression) {
            var cronFields = cronExpression.split(/\s/);
            var cronFieldsObject = {};
            var i = 0;
            if (cronFields.length === 6) {
                i = 1;
            }
            if (cronFields.length > 0) {
                cronFieldsObject.minutes = cronFields[i];
            }
            if (cronFields.length > 1) {
                cronFieldsObject.hours = cronFields[i + 1];
            }
            if (cronFields.length > 2) {
                cronFieldsObject.dayOfMonth = cronFields[i + 2];
            } else {
                cronFieldsObject.dayOfMonth = '*';
            }
            if (cronFields.length > 3) {
                cronFieldsObject.month = cronFields[i + 3];
            } else {
                cronFieldsObject.month = '*';
            }
            if (cronFields.length > 4) {
                cronFieldsObject.dayOfWeek = cronFields[i + 4];
            } else {
                cronFieldsObject.dayOfWeek = '*';
            }
            return cronFieldsObject;
        }

        function getCronExpressionFromFields(cronFields) {
            var expression = '';
            if (cronFields.minutes) {
                expression += cronFields.minutes + " ";
            }
            if (cronFields.hours) {
                expression += cronFields.hours + " ";
            }
            if (cronFields.dayOfMonth) {
                expression += cronFields.dayOfMonth + " ";
            } else {
                expression += '* ';
            }
            if (cronFields.month) {
                expression += cronFields.month + " ";
            } else {
                expression += '* ';
            }
            if (cronFields.dayOfWeek) {
                expression += cronFields.dayOfWeek;
            } else {
                expression += '*';
            }
            return expression;
        }

        function removeEmptyStringParams(object) {
            var objectToRemoveProps = angular.copy(object);
            var keys = _.keys(objectToRemoveProps);
            keys.forEach(function(key) {
                if (isEmptyString(objectToRemoveProps[key])) {
                    delete objectToRemoveProps[key];
                }
            });
            return objectToRemoveProps;
        }

        return {
            isEmptyString: isEmptyString,
            getString: getString,
            arrayHasDuplicates: arrayHasDuplicates,
            isNullOrUndefined: isNullOrUndefined,
            isMapEmpty: isMapEmpty,
            checkObjHasDeepPath: checkObjHasDeepPath,
            isNullOrUndefinedOrEmptyOrWhiteSpaceString: isNullOrUndefinedOrEmptyOrWhiteSpaceString,
            isNullOrUndefinedOrEmptyStringArrayOfValues: isNullOrUndefinedOrEmptyStringArrayOfValues,
            isAlphaNumericAndNotEmpty: isAlphaNumericAndNotEmpty,
            convertObjectToArray: convertObjectToArray,
            convertArrayToObject: convertArrayToObject,
            removeItemFromArray: removeItemFromArray,
            removeNonAlpha: removeNonAlpha,
            isNullOrUndefinedOrEmpty: isNullOrUndefinedOrEmpty,
            hashCode: hashCode,
            removeMultipleItemsFromArray: removeMultipleItemsFromArray,
            removeItemFromListById: removeItemFromListById,
            sortObjectsById: sortObjectsById,
            removeSelectedItemFromListById: removeSelectedItemFromListById,
            isInt : isInt,
            getItemFromListById: getItemFromListById,
            removeNullOrUndefinedOrEmptyStringValuesFromArray: removeNullOrUndefinedOrEmptyStringValuesFromArray,
            saveJsonContent: saveJsonContent,
            sortObjectsByPriority: sortObjectsByPriority,
            getEntityFromArrayByFieldValue: getEntityFromArrayByFieldValue,
            hasValue: hasValue,
            chunkData: chunkData,
            addClass: addClass,
            removeClass: removeClass,
            splitListByPercentage: splitListByPercentage,
            generateRequestList: generateRequestList,
            isNullOrUndefinedOrEmptyObject: isNullOrUndefinedOrEmptyObject,
            isNumeric: isNumeric,
            parseCronExpression: parseCronExpression,
            getCronExpressionFromFields: getCronExpressionFromFields,
            removeEmptyStringParams: removeEmptyStringParams,
            removeItemFromArrayWithDeepEquals: removeItemFromArrayWithDeepEquals
        };
    }
})();

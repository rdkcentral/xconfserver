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
        .module('app.services')
        .factory('fileReader', fileReader);


    fileReader.$inject = [];

    function fileReader() {
        let service = {
            readAsDataURLContent: readAsDataURLContent,
            readAsTextContent: readAsTextContent
        };

        return service;

        function onProgress(reader, scope) {
            return function (event) {
                scope.$emit('fileReaderProgress',
                    {
                        total: event.total,
                        progress: event.loaded
                    });
            };
        }

        function onLoad(reader, resolve, scope) {
            return function (event) {
                scope.$emit('fileReaderProgress',
                    {
                        total: event.loaded,
                        progress: event.loaded
                    });
                scope.$apply(function () {
                    resolve(reader.result);
                });
            };
        }

        function onError(reader, reject, scope) {
            return function () {
                scope.$apply(function () {
                    reject(reader.result);
                });
            };
        }

        function initAsyncReader(reader, scope, resolve, reject) {
            reader.onprogress = onProgress(reader, scope);
            reader.onload = onLoad(reader, resolve, scope);
            reader.onerror = onError(reader, reject, scope);
        }

        async function read(file, readFunction, scope) {
            let reader = new FileReader();
            return new Promise((resolve, reject) => {
                initAsyncReader(reader, scope, resolve, reject)
                readFunction(reader, file);
            });
        }

        function readAsText(reader, file) {
            reader.readAsText(file);
        }

        function readAsDataURL(reader, file) {
            reader.readAsDataURL(file);
        }

        async function readAsDataURLContent(file, scope) {
            return await read(file, readAsDataURL, scope)
        }

        async function readAsTextContent(file, scope) {
            return await read(file, readAsText, scope);
        }
    }
}());

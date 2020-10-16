/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Paul Guslisty (pguslisty@productengine.com)
 */
package com.comcast.apps.dataaccess.annotation;

public interface ListingCFDefinition {

    String cfName();

    default DataType keyIs() {
        return DataType.PRIMITIVE;
    }

    default Class<?> keyType() {
        return String.class;
    }

    default String keyColumnName() {
        return "key";
    }

    default DataType key2Is() {
        return DataType.PRIMITIVE;
    }

    default Class<?> key2Type() {
        return String.class;
    }

    default String key2FieldName() {
        return "id";
    }

    default DataType valueIs() {
        return DataType.JSON;
    }

    default String valueColumnName() {
        return "value";
    }

    default int ttl() {
        return 0;
    }

    default boolean compress() {
        return false;
    }
}

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
 * @author Philipp Bura
 */
package com.comcast.apps.dataaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for persistable objects.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CF {

    String cfName();

    DataType keyIs() default DataType.PRIMITIVE;

    Class<?> keyType() default String.class;

    String keyColumnName() default "key";

    DataType valueIs() default DataType.JSON;

    String valueColumnName() default "value";

    CF.CompressionPolicy compressionPolicy() default CF.CompressionPolicy.NONE;

    String defaultColumnName() default "data";

    /**
     * measured in kilobytes
     */
    int compressionChunkSize() default 64;

    /**
     * ttl for columns this definition produces in seconds, 0 - columns do not expire
     */
    int ttl() default 0;

    enum CompressionPolicy {
        COMPRESS_AND_SPLIT,
        NONE;

        CompressionPolicy() {
        }
    }

}

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
 * @author Sergei Lavrenyuk
 */
package com.comcast.apps.dataaccess.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * replaced by @link {@link ListingCFDefinition}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListingCF {

    String cfName();

    DataType keyIs() default DataType.PRIMITIVE;

    Class<?> keyType() default String.class;

    String keyColumnName() default "key";

    DataType key2Is() default DataType.PRIMITIVE;

    Class<?> key2Type() default String.class;

    String key2FieldName() default "id";

    DataType valueIs() default DataType.JSON;

    String valueColumnName() default "value";

    int ttl() default 0;

    boolean compress() default false;

}

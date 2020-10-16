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
package com.comcast.hydra.astyanax.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation describes Cassandra column.
 * This annotation is option and the fields are considered as unordered if it is omitted
 * and included in list of persistable field by default
 *
 * The annotation is intended to be used with field but it may be applied to getter to override behaviour for subclasses
 * if the original property wasn't annotated previously. The priority is on property
 */
@Retention(RetentionPolicy.RUNTIME)
//make it for field only
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface HColumn {
    /**
     * Set a sequence number of the field in composite column. Start from zero.
     * Note:
     * It is recommended to set the order of id component to the end of begging of the column.
     * Default behaviour the first component
     *
     * @return a sequence number of the field
     */
    public int order() default -1;

    /**
     * Indicates that field is excluded from processing and wont be persistent to Cassandra column family
     *
     * @return true if the field should be excluded
     */
    public boolean excluded() default false;
}

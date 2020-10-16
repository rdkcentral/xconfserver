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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation is intended to instruct Cassandra to create a secondary index for the field with annotated getter/setter
 * of the persistable bean.<br/>
 * <ul>Please note, that Cassandra has restrictions on secondary index:
 * <li>Not recommended for high cardinality values (i.e.timestamps,birthdates,keywords,etc.) </li>
 * <li>Unsorted-results are in token order,not query value order </li>
 * <li>Limited to search on datatypes Cassandra natively understands </li>
 * <li>you can NOT use secondary index with super column.</li>
 * <li>index name must be unique across column families of the whole keyspace.</li>
 *  </ul>
 * Author: jmccann
 * Date: 10/18/11
 * Time: 2:00 PM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {
	public String validationClass() default "UTF8Type";
}

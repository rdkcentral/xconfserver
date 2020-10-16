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
package com.comcast.hydra.astyanax;


import java.util.regex.Pattern;

/**
 * Author: jmccann
 * Date: 2/16/12
 * Time: 11:25 AM
 */
public class DataServiceConstants {
	public static final String MAX_RESULTS_PARAM = "maxResults";
	public static final String START_PARAM = "start";
	public static final String DEDUPLICATE_FIELD_PARAM = "deduplicateField";
	public static final int DEFAULT_MAX_RESULTS = 300;
    public static final Pattern COMMA_PATTERN = Pattern.compile(",") ;
	public static final String KEYSPACE_PROPERTY_KEY = "keyspace";
	public static final String DOT_SPLIT_PATTERN = "\\.";
    public static final int MAX_ATTEMPT = 2;
    public static final String COMPOSITE_KEY_DELIMITER = ":";
    public static final String PAGE_SIZE_PARAM_NAME = "pageSize";
    public static final int DEFAULT_PAGE_SIZE = 256;
    public static final String ENTITY_TYPES = "validEntityTypes";
    public static final String LAST_ENTITY_ONLY = "lastEntityOnly";
}

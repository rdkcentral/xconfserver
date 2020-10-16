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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.hydra.astyanax.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * User: andrey
 * Date: 9/4/12
 * Time: 1:32 PM
 */
public class PropertiesConfiguration extends org.apache.commons.configuration.PropertiesConfiguration {

    /**
     * Returns an object from the store described by the key. If the value is a
     * Collection object, replace it with the last object in the collection.
     *
     * @param key The property key.
     * @return value Value, transparently resolving a possible collection dependency.
     */
    protected Object resolveContainerStore(String key) {
        Object value = getProperty(key);
        if (value != null) {
            if (value instanceof List) {
                List list = List.class.cast(value);
                value = list.isEmpty() ? null : list.get(list.size() - 1);
            } else if (value instanceof Collection) {
                Collection collection = Collection.class.cast(value);
                value = null;

                for (Object item : collection) {
                    value = item;
                }
            } else if (value.getClass().isArray() && Array.getLength(value) > 0) {
                value = Array.get(value, Array.getLength(value) - 1);
            } else {
                value = super.resolveContainerStore(key);
            }
        }

        return value;
    }
}

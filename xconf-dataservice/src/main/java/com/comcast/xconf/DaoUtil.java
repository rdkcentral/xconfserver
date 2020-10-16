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
package com.comcast.xconf;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.google.common.base.Optional;

/**
 * User: ikostrov
 * Date: 25.12.14
 * Time: 16:23
 */
public class DaoUtil {

    public static <T extends IPersistable> T getFromCache(CachedSimpleDao<String, T> dao, String id) {
        Optional<T> optional = dao.asLoadingCache().asMap().get(id);
        return optional != null ? optional.orNull() : null;
    }
}

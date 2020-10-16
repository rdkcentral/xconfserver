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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.cache;

import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.data.Persistable;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;


public class CacheLoaderFactory {
    private static Logger logger = LoggerFactory.getLogger(CacheLoaderFactory.class);

    public static final <K, T> CacheLoader<K, Optional<T>> createSimpleCacheLoader(final SimpleDao<K, T> source, final Class<K> keyType, final Class<T> valueType) {
        return new CacheLoader<K, Optional<T>>() {
	   @Override
	   public Optional<T> load(K key) throws Exception {
	       final T value = source.getOne(key);
	       if (value == null) {
		  logger.warn("loaded null for ".concat(valueType.getSimpleName()).concat(" ").concat(JsonUtil.toJson(key)).concat(", rendering value Absent"));
	       }
	       return Optional.fromNullable(value);
	   }

	   @Override
	   public Map<K, Optional<T>> loadAll(Iterable<? extends K> keys) throws Exception {
		   final Map<K, Optional<T>> loaded = Maps.newHashMap();
		   final Set<K> reqKeys = Sets.newHashSet(keys);
		   final Map<K, Optional<T>> values = source.getAllAsMap(Sets.newHashSet(reqKeys));
		   if (values.size() < reqKeys.size()) {
			   for (K key : keys) {
				   loaded.put(key, load(key));
			   }
			   return loaded;
		   }
		   return values;
	   }
        };
    }

	public static final <K, N, T> CacheLoader<TwoKeys<K, N>, Optional<T>> createListingCacheLoader(final ListingDao<K, N, T> source, final Class<T> valueType) {
		return new CacheLoader<TwoKeys<K, N>, Optional<T>>() {
			@Override
			public Optional<T> load(TwoKeys<K, N> key) throws Exception {
				final T value = source.getOne(key.getKey(), key.getKey2());
				if (value == null) {
					logger.warn("loaded null for ".concat(valueType.getSimpleName()).concat(" ").concat(JsonUtil.toJson(key)).concat(", rendering value Absent"));
				}
				return Optional.fromNullable(value);
			}

			@Override
			public Map<TwoKeys<K, N>, Optional<T>> loadAll(Iterable<? extends TwoKeys<K, N>> keys) throws Exception {
				final Map<TwoKeys<K, N>, Optional<T>> loaded = Maps.newHashMap();
				for (TwoKeys<K, N> key : keys) {
					loaded.put(key, load(key));
				}
                return loaded;
			}

		};
	}

}

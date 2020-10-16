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
package com.comcast.apps.dataaccess.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.google.common.base.GuavaOptionalSerializer;
import de.javakaffee.kryoserializers.*;
import de.javakaffee.kryoserializers.jodatime.JodaDateTimeSerializer;
import org.joda.time.DateTime;

import java.lang.reflect.InvocationHandler;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;

public class CloneUtil {
	private static final KryoPool kryoPool = new KryoPool.Builder(new KryoFactory() {
		@Override
		public Kryo create() {
			final Kryo kryo = new Kryo();
			kryo.setAsmEnabled(true);

			final Serializer<Collection> collectionSerializer = kryo.getSerializer(Collection.class);
			collectionSerializer.setAcceptsNull(true);
			kryo.register(Collection.class, collectionSerializer);
			/*
			 * Copyright 2010 Martin Grotzke
			 * Licensed under the Apache License, Version 2.0
			 */
			kryo.register(Arrays.asList("").getClass(), new ArraysAsListSerializer());
			kryo.register(Collections.EMPTY_LIST.getClass(), new CollectionsEmptyListSerializer());
			kryo.register(Collections.EMPTY_MAP.getClass(), new CollectionsEmptyMapSerializer());
			kryo.register(Collections.EMPTY_SET.getClass(), new CollectionsEmptySetSerializer());
			kryo.register(Collections.singletonList("").getClass(), new CollectionsSingletonListSerializer());
			kryo.register(Collections.singleton("").getClass(), new CollectionsSingletonSetSerializer());
			kryo.register(Collections.singletonMap("", "").getClass(), new CollectionsSingletonMapSerializer());
			kryo.register(GregorianCalendar.class, new GregorianCalendarSerializer());
			kryo.register(InvocationHandler.class, new JdkProxySerializer());
			UnmodifiableCollectionsSerializer.registerSerializers(kryo);
			SynchronizedCollectionsSerializer.registerSerializers(kryo);
			kryo.register(DateTime.class, new JodaDateTimeSerializer());
			GuavaOptionalSerializer.registerSerializers(kryo);
			return kryo;
		}
	}).softReferences().build();

	public static <T> T clone(final T entity) {
		return kryoPool.run(new KryoCallback<T>() {
			@Override
			public T execute(Kryo kryo) {
				return kryo.copy(entity);
			}
		});
	}

}

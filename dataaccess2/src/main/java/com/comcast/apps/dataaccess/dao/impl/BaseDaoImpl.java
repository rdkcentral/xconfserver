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
package com.comcast.apps.dataaccess.dao.impl;

import com.comcast.apps.dataaccess.codec.CompressingJsonCodec;
import com.comcast.apps.dataaccess.codec.JsonCodec;
import com.comcast.apps.dataaccess.dao.BaseDao;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.CodecNotFoundException;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class BaseDaoImpl<K, T> implements BaseDao<K, T> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String legacyColumn = "column1";

    private final Session session;
    private final Class<T> persistentClass;

    public BaseDaoImpl(Session session, Class<T> persistentClass) {
        this.session = session;
        this.persistentClass = persistentClass;
    }

    public Session getSession() {
        return session;
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    public <R> R executeStatementAndGetResultAsObject(final Statement statement, final String columnName, final Class<R> clazz) {
        final Row row = getSession().execute(statement).one();
        if (row == null) {
            return null;
        }
        return row.get(columnName, clazz);
    }

    public <R> List<R> executeStatementAndGetResultAsList(final Statement statement, final String columnName, final Class<R> clazz) {
        final List<R> result = new ArrayList<>();
        getSession().execute(statement).forEach(row -> result.add(row.get(columnName, clazz)));
        return result;
    }

    public Map<K, Optional<T>> executeStatementAndGetResultAsMap(final Statement statement, final String valueColumn) {

        final Map<K, Optional<T>> result = new HashMap<>();
        getSession().execute(statement).forEach(row ->
                result.put(
                        row.get(0, getKeyClass()), Optional.of(row.get(valueColumn, getValueClass()))
                )
        );
        return result;
    }

    public void registerCodec(final Cluster cluster, final Class<?> clazz) {
        registerCodec(cluster, clazz, DataType.varchar(), false);
        registerCodec(cluster, clazz, DataType.blob(), false);
    }

    public void registerCodec(final Cluster cluster, final Class<?> clazz, boolean compress) {
        registerCodec(cluster, clazz, DataType.varchar(), compress);
        registerCodec(cluster, clazz, DataType.blob(), compress);
    }

    private void registerCodec(Cluster cluster, Class<?> clazz, DataType cqlType, boolean compress) {
        try {
            cluster.getConfiguration().getCodecRegistry().codecFor(cqlType, clazz);
        } catch (CodecNotFoundException e) {
            TypeCodec<?> codec = compress ? new CompressingJsonCodec<>(cqlType, clazz) : new JsonCodec<>(cqlType, clazz);
            cluster.getConfiguration().getCodecRegistry().register(codec);
            logger.info("Registered {} for {} <-> {} ", codec.getClass().getSimpleName(), cqlType.getName(), clazz);
        }
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public Class<T> getValueClass() {
        return persistentClass;
    }

    @Override
    public int id() {
        return getCfName()
                .concat(getKeyClass().getCanonicalName())
                .concat(getValueClass().getCanonicalName())
                .hashCode();
    }
}

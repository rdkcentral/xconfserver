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

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.apps.dataaccess.annotation.SimpleCFDefinition;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.base.Optional;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;



public class SimpleDaoImpl<K, T> extends BaseDaoImpl<K, T> implements SimpleDao<K, T> {

    protected final String keyColumnName;
    protected final String valueColumnName;
    protected final String defaultColumnName;
    protected final int ttl;
    protected final String cfName;
    protected final Class<?> keyType;

    /**
     * Use {@link #SimpleDaoImpl(Session, Class, SimpleCFDefinition)}
     * @param session
     * @param persistentClass
     */
    public SimpleDaoImpl(Session session, Class<T> persistentClass) {
        super(session, persistentClass);

        CF cfAnnotation = persistentClass.getAnnotation(CF.class);
        keyColumnName = cfAnnotation.keyColumnName();
        valueColumnName = cfAnnotation.valueColumnName();
        defaultColumnName = cfAnnotation.defaultColumnName();
        keyType = cfAnnotation.keyType();
        cfName = cfAnnotation.cfName();
        ttl = cfAnnotation.ttl();
        DataType keyIs = cfAnnotation.keyIs();
        DataType valueIs = cfAnnotation.valueIs();

        registerCodec(keyIs, valueIs, session, persistentClass);
    }

    public SimpleDaoImpl(Session session, Class<T> persistentClass, SimpleCFDefinition simpleCFDefinition) {
        super(session, persistentClass);

        keyColumnName = simpleCFDefinition.keyColumnName();
        valueColumnName = simpleCFDefinition.valueColumnName();
        defaultColumnName = simpleCFDefinition.defaultColumnName();
        keyType = simpleCFDefinition.keyType();
        cfName = simpleCFDefinition.cfName();
        ttl = simpleCFDefinition.ttl();
        DataType keyIs = simpleCFDefinition.keyIs();
        DataType valueIs = simpleCFDefinition.valueIs();

        registerCodec(keyIs, valueIs, session, persistentClass);
    }

    private void registerCodec(DataType keyIs, DataType valueIs, Session session, Class<T> persistentClass) {

        if (keyIs != DataType.PRIMITIVE) {
            registerCodec(session.getCluster(), keyType);
        }
        if (valueIs != DataType.PRIMITIVE) {
            registerCodec(session.getCluster(), persistentClass);
        }
    }

    @Override
    public T getOne(K rowKey) {
        final Statement statement = select()
                .from(tableName())
                .where(eq(keyColumnName, rowKey));

        return executeStatementAndGetResultAsObject(statement, valueColumnName, getValueClass());
    }

    @Override
    public T setOne(K rowKey, T entity) {
        final Statement statement = insertInto(tableName())
                .value(keyColumnName, rowKey)
                .value(legacyColumn, defaultColumnName)
                .value(valueColumnName, entity)
                .using(ttl(ttl));
        getSession().execute(statement);
        return entity;
    }

    @Override
    public void deleteOne(K rowKey) {
        final Statement statement = delete()
                .from(tableName())
                .where(eq(keyColumnName, rowKey));
        getSession().execute(statement);
    }

    @Override
    public List<T> getAll() {
        final Statement statement = select().from(tableName());

        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getAll(Set<K> keys) {
        final Statement statement = select()
                .from(tableName())
                .allowFiltering()
                .where(
                        in(keyColumnName, new ArrayList<>(keys))
                );

        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getAll(int maxResults) {
        final Statement statement = select().from(tableName()).limit(maxResults);

        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public Map<K, Optional<T>> getAllAsMap(Set<K> keys) {
        final Statement statement = select()
                .from(tableName())
                .allowFiltering()
                .where(
                        in(keyColumnName, new ArrayList<>(keys))
                );

        return executeStatementAndGetResultAsMap(statement, valueColumnName);
    }

    @Override
    public Iterable<K> getKeys() {
        final Statement statement = select(keyColumnName).from(tableName());

        return new HashSet<>(executeStatementAndGetResultAsList(statement, keyColumnName, getKeyClass()));
    }

    @Override
    public String getCfName() {
        return cfName;
    }

    @Override
    public Class<K> getKeyClass() {
        return (Class<K>) keyType;
    }

    protected String tableName() {
        return "\"" + cfName + "\"";
    }
}

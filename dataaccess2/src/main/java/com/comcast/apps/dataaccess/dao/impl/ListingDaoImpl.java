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

import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.apps.dataaccess.annotation.ListingCFDefinition;
import com.comcast.apps.dataaccess.cache.dao.impl.TwoKeys;
import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.query.RangeInfo;
import com.comcast.apps.dataaccess.dao.util.ListingCFDefinitionBuilder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Optional;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;


public class ListingDaoImpl<K, N, T> extends BaseDaoImpl<K, T> implements ListingDao<K, N, T> {

    private final String keyColumnName;
    private final String valueColumnName;
    private final String key2FieldName;
    private final String cfName;
    private final Class<?> keyType;
    private final Class<?> key2Type;
    protected final int ttl;

    /**
     * Use @link {@link #ListingDaoImpl(Session, Class, ListingCFDefinition)}
     * @param session
     * @param persistentClass must be a type annotated with deprecated @ListingCF
     */
    public ListingDaoImpl(Session session, Class<T> persistentClass) {
        this(session, persistentClass, ListingCFDefinitionBuilder.buildFromAnnotation(persistentClass));
    }

    public ListingDaoImpl(Session session, Class<T> persistentClass, String cfName) {
        this(session, persistentClass,
                ListingCFDefinitionBuilder.fromAnnotation(persistentClass.getAnnotation(ListingCF.class))
                        .setCfName(cfName)
                        .build());
    }

    public ListingDaoImpl(Session session, Class<T> persistentClass, ListingCFDefinition listingCFDefinition) {
        super(session, persistentClass);

        keyColumnName = listingCFDefinition.keyColumnName();
        valueColumnName = listingCFDefinition.valueColumnName();
        key2FieldName = listingCFDefinition.key2FieldName();
        cfName = listingCFDefinition.cfName();
        keyType = listingCFDefinition.keyType();
        key2Type = listingCFDefinition.key2Type();

        ttl = listingCFDefinition.ttl();
        DataType dataType = listingCFDefinition.keyIs();
        DataType key2Is = listingCFDefinition.key2Is();
        DataType valueIs = listingCFDefinition.valueIs();
        boolean compress = listingCFDefinition.compress();

        registerCodec(dataType, key2Is, valueIs, session, keyType, persistentClass, compress);
    }

    private void registerCodec(DataType dataType, DataType key2Is, DataType valueIs, Session session, Class<?> keyType, Class<T> persistentClass, boolean compress) {

        if (dataType != DataType.PRIMITIVE) {
            registerCodec(session.getCluster(), keyType);
        }
        if (key2Is != DataType.PRIMITIVE) {
            registerCodec(session.getCluster(), key2Type);
        }
        if (valueIs != DataType.PRIMITIVE) {
            registerCodec(session.getCluster(), persistentClass, compress);
        }
    }

    @Override
    public T setOne(K rowKey, T entity) {
        final Object key2;
        try {
            key2 = FieldUtils.readDeclaredField(entity, key2FieldName,true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return executeInsertStatement(rowKey, key2, entity);
    }

    @Override
    public T setOne(K rowKey, N key2, T entity) {
        return executeInsertStatement(rowKey, key2, entity);
    }

    private T executeInsertStatement(K rowKey, Object key2, T entity) {
        final Statement statement = insertInto(tableName())
                .value(keyColumnName, rowKey)
                .value(key2FieldName, key2)
                .value(valueColumnName, entity)
                .using(ttl(ttl));
        getSession().execute(statement);
        return entity;
    }

    @Override
    public void deleteAll(K rowKey) {
        final Statement statement = delete().from(tableName())
                .where(eq(keyColumnName, rowKey));
        getSession().execute(statement);
    }

    @Override
    public void deleteOne(K rowKey, N key2) {
        final Statement statement = delete().from(tableName())
                .where(eq(keyColumnName, rowKey)).and(eq(key2FieldName, key2));
        getSession().execute(statement);
    }

    @Override
    public T getOne(K rowKey, N key2) {
        final Statement statement = select().from(tableName())
                .where(eq(keyColumnName, rowKey)).and(eq(key2FieldName, key2));
        return executeStatementAndGetResultAsObject(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getAll(K rowKey) {
        final Statement statement = select().from(tableName())
                .where(eq(keyColumnName, rowKey));
        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getAll() {
        final Statement statement = select().from(tableName());

        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getRange(K rowKey, final RangeInfo range) {
        final Select.Where statement = select()
                .from(tableName())
                .allowFiltering()
                .where(
                        eq(keyColumnName, rowKey)
                );
        if (range.getStartValue() != null) {
            statement.and(
                    gt(key2FieldName, range.getStartValue())
            );
        }
        if (range.getEndValue() != null) {
            statement.and(
                    lt(key2FieldName, range.getEndValue())
            );
        }
        return executeStatementAndGetResultAsList(statement, valueColumnName, getValueClass());
    }

    @Override
    public List<T> getRange(Map<K, RangeInfo<N>> ranges) {
        final List<T> result = new ArrayList<>();
        for (final Map.Entry<K, RangeInfo<N>> entry : ranges.entrySet()) {
            result.addAll(getRange(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    @Override
    public Iterable<TwoKeys<K, N>> getKeys() {
        final Statement statement = select(keyColumnName, key2FieldName).from(tableName());

        final List<TwoKeys<K, N>> result = new ArrayList<>();
        getSession().execute(statement).forEach(row -> {
            final K key = row.get(keyColumnName, getKeyClass());
            final N key2 = row.get(key2FieldName, (Class<N>) key2Type);

            result.add(new TwoKeys<>(key, key2));
        });
        return result;
    }

    @Override
    public Iterable<N> getKeys(K rowKey) {
        final Statement statement = select(key2FieldName).from(tableName()).where(eq(keyColumnName, rowKey));

        return executeStatementAndGetResultAsList(statement, key2FieldName, getKey2Class());
    }

    @Override
    public Map<N, Optional<T>> getAllAsMap(K rowKey, Set<N> keys2) {
        final Statement statement = select()
                .from(tableName())
                .allowFiltering()
                .where(
                        eq(keyColumnName, rowKey)
                )
                .and(
                        in(key2FieldName, new ArrayList<>(keys2))
                );

        final Map<N, Optional<T>> result = new HashMap<>();
        getSession().execute(statement).forEach(row ->
                result.put(
                        row.get(1, getKey2Class()), Optional.of(row.get(valueColumnName, getPersistentClass()))
                )
        );

        return result;
    }

    protected String tableName() {
        return "\"" + cfName + "\"";
    }

    @Override
    public String getCfName() {
        return cfName;
    }

    @Override
    public Class<K> getKeyClass() {
        return (Class<K>) keyType;
    }

    @Override
    public Class<N> getKey2Class() {
        return (Class<N>) key2Type;
    }

}

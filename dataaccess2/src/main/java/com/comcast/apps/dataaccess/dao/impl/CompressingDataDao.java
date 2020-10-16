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
 * @author Igor Kostrov (ikostrov@productengine.com)
 */
package com.comcast.apps.dataaccess.dao.impl;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.annotation.SimpleCFDefinition;
import com.comcast.apps.dataaccess.config.BlobAsStringCodec;
import com.comcast.apps.dataaccess.util.Archiver;
import com.comcast.apps.dataaccess.util.CompressionUtil;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

public class CompressingDataDao<K, T> extends SimpleDaoImpl<K, T> {

    private static final Archiver archiver = CompressionUtil.createArchiver();

    private int compressionChunkSize = 64;

    public CompressingDataDao(Session session, Class<T> persistentClass) {
        super(session, persistentClass);
        init(session, persistentClass.getAnnotation(CF.class).compressionChunkSize());
    }

    public CompressingDataDao(Session session, Class<T> persistentClass, SimpleCFDefinition simpleCFDefinition) {
        super(session, persistentClass, simpleCFDefinition);
        init(session, simpleCFDefinition.compressionChunkSize());
    }

    private void init(Session session, int compressionChunkSize) {
        this.compressionChunkSize = compressionChunkSize;
        session.getCluster().getConfiguration().getCodecRegistry().register(new BlobAsStringCodec());
    }

    @Override
    public T setOne(K rowKey, T entity) {
        String strValue = JsonUtil.toJson(entity);
        ByteBuffer data = ByteBuffer.wrap(strValue.getBytes(Charsets.UTF_8));
        data = archiver.compress(data);
        // cfDef.compressionChunkSize() * 1024 - convert from kilobytes to bytes
        ByteBuffer[] splitData = archiver.split(data, compressionChunkSize * 1024);

        BatchStatement batchStatement = new BatchStatement();
        batchStatement.add(
                insertInto(tableName())
                .value(keyColumnName, rowKey)
                .value(legacyColumn, defaultColumnName + "_parts_count")
                .value(valueColumnName, toBytes(splitData.length))
                .using(ttl(ttl))
        );

        for (int i = 0; i < splitData.length; i++) {
            batchStatement.add(
                    insertInto(tableName())
                            .value(keyColumnName, rowKey)
                            .value(legacyColumn, defaultColumnName + "_part_" + i)
                            .value(valueColumnName, splitData[i])
                            .using(ttl(ttl))
            );
        }

        getSession().execute(batchStatement);
        return entity;
    }

    @Override
    public <R> R executeStatementAndGetResultAsObject(Statement statement, String columnName, Class<R> clazz) {
        List<Row> rows = getSession().execute(statement).all();
        return convertRowsIntoEntity(rows, clazz);
    }

    @Override
    public <R> List<R> executeStatementAndGetResultAsList(Statement statement, String columnName, Class<R> clazz) {
        final Multimap<K, Row> map = HashMultimap.create();
        getSession().execute(statement).forEach(row -> {
            K rowKey = row.get(keyColumnName, getKeyClass());
            map.put(rowKey, row);
        });

        List<R> result = new ArrayList<>();
        for (K rowKey : map.keySet()) {
            result.add(convertRowsIntoEntity(map.get(rowKey), clazz));
        }

        return result;
    }

    @Override
    public Map<K, Optional<T>> executeStatementAndGetResultAsMap(final Statement statement, final String valueColumn) {
        final Multimap<K, Row> map = HashMultimap.create();
        getSession().execute(statement).forEach(row -> {
            K rowKey = row.get(keyColumnName, getKeyClass());
            map.put(rowKey, row);
        });

        Map<K, Optional<T>> result = new HashMap<>();
        for (K rowKey : map.keySet()) {
            result.put(rowKey, Optional.of(convertRowsIntoEntity(map.get(rowKey), getValueClass())));
        }

        return result;
    }

    @Override
    public Iterable<K> getKeys() {
        final Statement statement = select(keyColumnName).from(tableName());
        final Set<K> result = new HashSet<>();
        getSession().execute(statement).forEach(row -> result.add(row.get(keyColumnName, getKeyClass())));
        return result;
    }

    private <R> R convertRowsIntoEntity(Collection<Row> rows, final Class<R> clazz) {
        final AtomicLong partsCount = new AtomicLong(0);
        final Map<String, ByteBuffer> splitDataMap = new TreeMap<>();


        rows.forEach(row -> {
            String counterColumnName = defaultColumnName + "_parts_count";
            String columnName = row.get(legacyColumn, String.class);
            if (counterColumnName.equals(columnName)) {
                ByteBuffer bytes = row.getBytes(valueColumnName);
                partsCount.set(bytes.getInt(bytes.position()));
            } else {
                splitDataMap.put(columnName, row.getBytes(valueColumnName));
            }
        });

        int partCountVal = partsCount.intValue();
        if (partCountVal == 0) {
            return null;
        }

        ByteBuffer[] splitData = new ByteBuffer[partCountVal];
        for (int i = 0; i < partCountVal; i++) {
            splitData[i] = splitDataMap.get(defaultColumnName + "_part_"+ i);
        }

        final ByteBuffer compressed = archiver.join(splitData);
        try {
            final ByteBuffer data = archiver.decompress(compressed);
            final String strData = Charsets.UTF_8.decode(data).toString();
            return JsonUtil.fromJson(clazz, strData);
        } catch (Archiver.DataFormatException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ByteBuffer toBytes(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);
        b.rewind();
        return b;
    }
}

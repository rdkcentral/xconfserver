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
package com.comcast.apps.dataaccess.dao.util;


import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.apps.dataaccess.annotation.SimpleCFDefinition;

public class SimpleCFDefinitionBuilder {

    /**
     * column family name
     */
    private String cfName;

    private DataType keyIs;

    /**
     * row key type
     */
    private Class<?> keyType;

    private String keyColumnName;

    private DataType valueIs;

    private String valueColumnName;

    private String defaultColumnName;

    /**
     * requires MarshalingPolicy.WHOLE, otherwise should be ignored
     */
    private CF.CompressionPolicy compressionPolicy;

    /**
     * measured in kilobytes
     */
    private int compressionChunkSize;

    /**
     * ttl for columns this definition produces in seconds, 0 - columns do not expire
     */
    private int ttl;

    public SimpleCFDefinitionBuilder setCfName(String cfName) {
        this.cfName = cfName;
        return this;
    }

    public SimpleCFDefinitionBuilder setKeyIs(DataType keyIs) {
        this.keyIs = keyIs;
        return this;
    }

    public SimpleCFDefinitionBuilder setKeyType(Class<?> keyType) {
        this.keyType = keyType;
        return this;
    }

    public SimpleCFDefinitionBuilder setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
        return this;
    }

    public SimpleCFDefinitionBuilder setValueIs(DataType valueIs) {
        this.valueIs = valueIs;
        return this;
    }

    public SimpleCFDefinitionBuilder setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
        return this;
    }

    public SimpleCFDefinitionBuilder setDefaultColumnName(String defaultColumnName) {
        this.defaultColumnName = defaultColumnName;
        return this;
    }

    public SimpleCFDefinitionBuilder setCompressionPolicy(CF.CompressionPolicy compressionPolicy) {
        this.compressionPolicy = compressionPolicy;
        return this;
    }

    public SimpleCFDefinitionBuilder setCompressionChunkSize(int compressionChunkSize) {
        this.compressionChunkSize = compressionChunkSize;
        return this;
    }

    public SimpleCFDefinitionBuilder setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public static SimpleCFDefinitionBuilder create() {
        return new SimpleCFDefinitionBuilder();
    }

    public static SimpleCFDefinitionBuilder fromAnnotation(CF cfDef) {
        return new SimpleCFDefinitionBuilder()
                .setCfName(cfDef.cfName())
                .setKeyIs(cfDef.keyIs())
                .setKeyType(cfDef.keyType())
                .setKeyColumnName(cfDef.keyColumnName())
                .setValueIs(cfDef.valueIs())
                .setValueColumnName(cfDef.valueColumnName())
                .setDefaultColumnName(cfDef.defaultColumnName())
                .setCompressionChunkSize(cfDef.compressionChunkSize())
                .setCompressionPolicy(cfDef.compressionPolicy())
                .setTtl(cfDef.ttl());
    }

    public SimpleCFDefinition build() {

        return new SimpleCFDefinition() {
            @Override
            public String cfName() {
                return cfName;
            }
            @Override
            public DataType keyIs() {
                return keyIs;
            }
            @Override
            public Class<?> keyType() {
                return keyType;
            }
            @Override
            public String keyColumnName() {
                return keyColumnName;
            }
            @Override
            public DataType valueIs() {
                return valueIs;
            }
            @Override
            public String valueColumnName() {
                return valueColumnName;
            }
            @Override
            public String defaultColumnName() {
                return defaultColumnName;
            }
            @Override
            public int compressionChunkSize() {
                return compressionChunkSize;
            }
            @Override
            public CF.CompressionPolicy compressionPolicy() {
                return compressionPolicy;
            }
            @Override
            public int ttl() {
                return ttl;
            }
        };
    }
}

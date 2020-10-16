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

import com.comcast.apps.dataaccess.annotation.DataType;
import com.comcast.apps.dataaccess.annotation.ListingCF;
import com.comcast.apps.dataaccess.annotation.ListingCFDefinition;

public class ListingCFDefinitionBuilder {

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

    private DataType key2Is;

    private Class<?> key2Type;

    private String key2FieldName;

    private DataType valueIs;

    private String valueColumnName;

    private int ttl;

    private boolean compress;

    public ListingCFDefinitionBuilder setCfName(String cfName) {
        this.cfName = cfName;
        return this;
    }

    public ListingCFDefinitionBuilder setKeyIs(DataType keyIs) {
        this.keyIs = keyIs;
        return this;
    }

    public ListingCFDefinitionBuilder setKeyType(Class<?> keyType) {
        this.keyType = keyType;
        return this;
    }

    public ListingCFDefinitionBuilder setKeyColumnName(String keyColumnName) {
        this.keyColumnName = keyColumnName;
        return this;
    }

    public ListingCFDefinitionBuilder setKey2Is(DataType key2Is) {
        this.key2Is = key2Is;
        return this;
    }

    public ListingCFDefinitionBuilder setKey2Type(Class<?> key2Type) {
        this.key2Type = key2Type;
        return this;
    }

    public ListingCFDefinitionBuilder setKey2FieldName(String key2FieldName) {
        this.key2FieldName = key2FieldName;
        return this;
    }

    public ListingCFDefinitionBuilder setValueIs(DataType valueIs) {
        this.valueIs = valueIs;
        return this;
    }

    public ListingCFDefinitionBuilder setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
        return this;
    }

    public ListingCFDefinitionBuilder setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public ListingCFDefinitionBuilder setCompress(boolean compress) {
        this.compress = compress;
        return this;
    }

    public static ListingCFDefinitionBuilder create() {
        return new ListingCFDefinitionBuilder();
    }

    public static ListingCFDefinition buildFromAnnotation(Class<?> clazz) {
        return fromAnnotation(clazz.getAnnotation(ListingCF.class)).build();
    }

    public static ListingCFDefinitionBuilder fromAnnotation(ListingCF cfDef) {
        return new ListingCFDefinitionBuilder()
                .setCfName(cfDef.cfName())
                .setKeyIs(cfDef.keyIs())
                .setKeyType(cfDef.keyType())
                .setKeyColumnName(cfDef.keyColumnName())
                .setKey2Is(cfDef.key2Is())
                .setKey2Type(cfDef.key2Type())
                .setKey2FieldName(cfDef.key2FieldName())
                .setValueIs(cfDef.valueIs())
                .setValueColumnName(cfDef.valueColumnName())
                .setTtl(cfDef.ttl())
                .setCompress(cfDef.compress());
    }

    public ListingCFDefinition build() {

        return new ListingCFDefinition() {
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
            public DataType key2Is() {
                return key2Is;
            }
            @Override
            public Class<?> key2Type() {
                return key2Type;
            }
            @Override
            public String key2FieldName() {
                return key2FieldName;
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
            public int ttl() {
                return ttl;
            }
            @Override
            public boolean compress() {
                return compress;
            }
        };
    }
}

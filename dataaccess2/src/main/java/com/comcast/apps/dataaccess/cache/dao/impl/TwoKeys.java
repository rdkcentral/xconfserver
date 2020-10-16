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
package com.comcast.apps.dataaccess.cache.dao.impl;


public class TwoKeys<K, K2> {
    private K key;
    private K2 key2;

    public TwoKeys() {
    }

    public TwoKeys(K key, K2 key2) {
        this.key = key;
        this.key2 = key2;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public K2 getKey2() {
        return key2;
    }

    public void setKey2(K2 key2) {
        this.key2 = key2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwoKeys<?, ?> twoKeys = (TwoKeys<?, ?>) o;

        if (key != null ? !key.equals(twoKeys.key) : twoKeys.key != null) return false;
        return key2 != null ? key2.equals(twoKeys.key2) : twoKeys.key2 == null;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (key2 != null ? key2.hashCode() : 0);
        return result;
    }
}

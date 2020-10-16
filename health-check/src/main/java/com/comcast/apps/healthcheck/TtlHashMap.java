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
package com.comcast.apps.healthcheck;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TtlHashMap<KEY, VALUE> implements Map<KEY, VALUE> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private final ReentrantLock lock;

    private final Map<KEY, VALUE> data;
    private final Map<KEY, Long> time;

    private long ttlValue;

    public TtlHashMap(TimeUnit ttlUnit, long ttl) {
        this(ttlUnit, ttl, DEFAULT_INITIAL_CAPACITY);
    }

    public TtlHashMap(TimeUnit ttlUnit, long ttl, int initialCapacity) {
        lock = new ReentrantLock();
        data = new HashMap<KEY, VALUE>(initialCapacity);
        time = new HashMap<KEY, Long>(initialCapacity);
        ttlValue = ttlUnit.toNanos(ttl);
    }

    private boolean isExpired(Object key) {
        Long ttl = time.get(key);
        return ttl == null || (System.nanoTime() - time.get(key)) > ttlValue;
    }

    private void removeExpired() {
        for (KEY key : data.keySet()) {
            if (isExpired(key)) {
                data.remove(key);
                time.remove(key);
            }
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            removeExpired();
            return data.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            removeExpired();
            return data.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        lock.lock();
        try {
            boolean containsKey = data.containsKey(key);
            return containsKey && !isExpired(key) ? true : false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        lock.lock();
        try {
            if (value == null) {
                return containsNullValue();
            } else {
                for (KEY key : data.keySet()) {
                    if (!isExpired(key)) {
                        if (value.equals(data.get(key))) {
                            return true;
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return false;
    }

    private boolean containsNullValue() {
        for (KEY key : data.keySet()) {
            if (!isExpired(key)) {
                if (data.get(key) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public VALUE get(Object key) {
        VALUE value;
        lock.lock();
        try {
            value = data.get(key);
            if (value != null && isExpired(key)) {
                data.remove(key);
                time.remove(key);
                value = null;
            }
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public VALUE put(KEY key, VALUE value) {
        VALUE oldValue;
        lock.lock();
        try {
            oldValue = data.put(key, value);
            oldValue = isExpired(key) ? null : oldValue;
            time.put(key, System.nanoTime());
        } finally {
            lock.unlock();
        }
        return oldValue;
    }

    @Override
    public VALUE remove(Object key) {
        VALUE value;
        lock.lock();
        try {
            value = data.remove(key);
            value = isExpired(key) ? null : value;
            time.remove(key);
        } finally {
            lock.unlock();
        }
        return value;
    }

    @Override
    public void putAll(Map<? extends KEY, ? extends VALUE> m) {
        lock.lock();
        try {
            long nano = System.nanoTime();
            for (Entry<? extends KEY, ? extends VALUE> e : m.entrySet()) {
                KEY key = e.getKey();
                data.put(key, e.getValue());
                time.put(key, nano);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            data.clear();
            time.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<KEY> keySet() {
        lock.lock();
        try {
            removeExpired();
            return Collections.unmodifiableSet(data.keySet());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<VALUE> values() {
        lock.lock();
        try {
            removeExpired();
            return Collections.unmodifiableCollection(data.values());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Set<Entry<KEY, VALUE>> entrySet() {
        lock.lock();
        try {
            removeExpired();
            return Collections.unmodifiableSet(data.entrySet());
        } finally {
            lock.unlock();
        }
    }
}

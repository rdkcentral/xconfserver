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
package com.comcast.hydra.astyanax.data;

import com.eaio.uuid.UUIDGen;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

/**
 * Overview:
 *
 * @author Vladimir Iordanov
 */
public abstract class KeyGenerator<I extends KeyGenerator.IdGenerator, O extends KeyGenerator.OwnerIdGenerator> implements IKeyGenerator {

    protected Map<Class<? extends IPersistable>, I> idGenerators;
    protected Map<Class<? extends IPersistable>, O> ownerIdGenerators;

    public KeyGenerator() {
        // Fill idGenerator map with the supported generators
        initIdGenerators();
        // Fill ownerIdGenerator map with the supported generators
        initOwnerIdGenerators();
    }

    protected abstract void initIdGenerators();

    protected abstract void initOwnerIdGenerators();

    @Override
    public String getOwnerId(Class<? extends IPersistable> clazz) {
        return getOwnerIdGenerator(clazz).generate();
    }

    @Override
    public String getOwnerId(IPersistable obj) {
        return getOwnerIdGenerator(obj.getClass()).generate(obj);
    }

    protected O getOwnerIdGenerator(Class<? extends IPersistable> clazz) {
        O ownerIdGenerator = ownerIdGenerators.get(clazz);
        if (ownerIdGenerator == null) {
            throw new UnsupportedOperationException("There is no ownerId generator for class " + clazz);
        }

        return ownerIdGenerator;
    }

    @Override
    public Object getId(IPersistable obj) {
        return getIdGenerator(obj.getClass()).generate(obj);
    }

    @Override
    public String getStringId(IPersistable obj) throws UnsupportedOperationException {
        return getId(obj).toString();
    }

    @Override
    public Object getId(Class<? extends IPersistable> clazz, Object... args) {
        return getIdGenerator(clazz).generate(args);
    }

    @Override
    public String getStringId(Class<? extends IPersistable> clazz, Object... args) {
        return getId(clazz, args).toString();
    }

    protected I getIdGenerator(Class<? extends IPersistable> clazz) {
        I idGenerator = idGenerators.get(clazz);
        if (idGenerator == null) {
            throw new UnsupportedOperationException("There is no id generator for class " + clazz);
        }

        return idGenerator;
    }

    @Override
    public Object idToObject(String id, Class<? extends IPersistable> clazz) {
        return getIdGenerator(clazz).idToObject(id);
    }

    /**
     * Generates an unique UUID according to the current timestamp
     *
     * @return UUID object
     */
    protected UUID getUniqueTimeUUIDinMillis() {
        return new UUID(UUIDGen.newTime(), UUIDGen.getClockSeqAndNode());
    }

    /**
     * Returns an array of byte suitable for passing to the function UUID.nameUUIDFromBytes.  Without
     * this, we have no way of predictably arriving at the ID of an object in a language agnostic way.  By
     * using this function to prepare the byte array that you pass to UUID.nameUUIDFromBytes you can guarantee
     * that the result will be compliant with RFC 4122 type 3 (name-based) UUIDs.
     * <p/>
     * For more info, see the RFC 4122 spec:
     * http://tools.ietf.org/html/rfc4122.html#section-4.3
     *
     * @param namespace The namespace to use for generating the UUID
     * @param name      The string you want to use to generate the UUID
     * @return An array of bytes that can safely be passed UUID.nameUUIDFromBytes.
     */
    protected byte[] bytesFromNamespaceAndName(UUID namespace, String name) {
        ByteBuffer buf = ByteBuffer.allocate(16 + name.getBytes().length);
        buf.putLong(namespace.getMostSignificantBits());
        buf.putLong(namespace.getLeastSignificantBits());
        buf.put(name.getBytes());
        return buf.array();

    }

    /**
     * Shortcut for invoking bytesFromNamespaceAndName with the URL namespace.
     *
     * @param name The string you want to use to generate the UUID
     * @return An array of bytes that can safely be passed UUID.nameUUIDFromBytes.
     */
    protected byte[] bytesFromNamespaceAndName(String name) {
        return bytesFromNamespaceAndName(NAMESPACE_URL, name);
    }

    /**
     * Base class for all OwnerId generators.
     * These generators allow to obtain ownerId value for the any IPersistable object and use it
     * in Hydra client requests.
     *
     * @param <T> Instance of any IPersistable child
     */
    public static abstract class OwnerIdGenerator<T extends IPersistable> {
        public String generate(T obj) {
            return generateInternal(obj);
        }

        public String generate() {
            return generateInternal(null);
        }

        protected abstract String generateInternal(T obj);
    }

    /**
     * Base class for all id generators. The id value is used as a part of column name.
     * These generators allow to obtain id value for the any IPersistable object and use it
     * in Hydra client requests.
     * Also generators may produce id without object instance. In this case user should pass valid parameter(s)
     * in args parameter.
     *
     * @param <T> Instance of any IPersistable child
     */
    public static abstract class IdGenerator<T extends IPersistable> {
        public Object generate(T obj) {
            return generateInternal(obj);
        }

        public Object generate(Object... args) {
            return generateInternal(args);
        }

        public Object idToObject(String id) {
            return id;
        }

        protected abstract Object generateInternal(T obj);

        protected abstract Object generateInternal(Object... args);
    }

    public class EmptyIdGenerator<P extends IPersistable> extends IdGenerator<P> {

        @Override
        protected Object generateInternal(P obj) {
            return "";
        }

        @Override
        protected Object generateInternal(Object... args) {
            return "";
        }
    }
}

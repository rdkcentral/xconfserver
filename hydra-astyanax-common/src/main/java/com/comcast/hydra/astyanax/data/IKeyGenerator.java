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

import java.util.UUID;

/**
 * This manager is intended to encapsulate id and ownerId generation logic
 * at the one place and use it in other components: Hydra DataServices,
 * Migration Tool, XRE Server, etc.
 * <pre>
 *     <ul>
 *         <li><i>ownerId</i> refers to row key of the Cassandra DataStore and also can be composite.</li>
 *
 *         <li><i>id</i> uniquely identifies an object and it is a part of a composite column name to column duplication</li>
 *
 *     </ul>
 *
 * </pre>
 *
 * @author Vladimir Iordanov
 */
public interface IKeyGenerator {

    // Universally recognized namespaces, per the RFC 4122 spec: http://tools.ietf.org/html/rfc4122.html#section-4.3
    UUID NAMESPACE_DNS  = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
    UUID NAMESPACE_URL  = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
    UUID NAMESPACE_OID  = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
    UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

    /**
     * Generates ownerId based on object class
     *
     *
     * @param clazz
     * @return return default ownerId for class
     * @throws UnsupportedOperationException if the manager isn't able to generate ownerId for that <code>obj</code>
     */
    public String getOwnerId(Class<? extends IPersistable> clazz) throws UnsupportedOperationException;

    /**
     * Generates ownerId of the object based on internal state of <code>obj</code>.
     *
     *
     * @param obj the persistable object the ownerId to be generated for
     * @return
     * @throws UnsupportedOperationException if the manager isn't able to generate ownerId for that <code>obj</code>
     */
    public String getOwnerId(IPersistable obj) throws UnsupportedOperationException;

    /**
     * Generate id that uniquely identifies this object.
     *
     * @param obj
     * @return object that represents <code>id</code> for the given <code>obj</code>
     * @throws UnsupportedOperationException if the manager isn't able to generate id for that <code>obj</code>
     */
    public Object getId(IPersistable obj) throws UnsupportedOperationException;

    /**
     *
     * @param obj
     * @return
     * @throws UnsupportedOperationException
     */
    public String getStringId(IPersistable obj) throws UnsupportedOperationException;

    /**
     * Generates id based on list of passed params.
     * It's up to implementation to limit and describe list of supported args
     * It allows to generate id without having a object instance
     * and MUST be consistent with {@link #getId(IPersistable)}
     *
     * @param clazz
     * @param args
     * @return
     * @throws UnsupportedOperationException
     */
    public Object getId(Class<? extends IPersistable> clazz, Object... args) throws UnsupportedOperationException;

    /**
     *
     * @param clazz
     * @param args
     * @return
     * @throws UnsupportedOperationException
     */
    public String getStringId(Class<? extends IPersistable> clazz, Object... args) throws UnsupportedOperationException;

    /**
     * Converts id from string representation to Object.
     * The object representation are being used with CompositeDataService classes and allows construct composite column name
     * for Cassandra data storage by using non-string object (e.g. UUID).
     * The non-string objects in composite column allow to use special comparators and change the composite name sort order.
     * @param id
     * @param clazz
     * @return
     */
    public Object idToObject(String id, Class<? extends IPersistable> clazz);


    /**
     * Splits composite ownerId into components.
     * A vice versa operation to #getOwnerId.
     * @param clazz
     * @param ownerId
     * @return
     */
    public Object[] ownerIdToComponents(Class<? extends IPersistable> clazz, Object ownerId);
}

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comcast.hydra.astyanax.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * IPersistable implementation that omits updated and ttlMap fields during marshalling
 * and maps id to xml attribute. It is intended to be used to have single marshalling solution
 * as for data sent to client and also one stored in cassandra.
 * @author PBura
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class XMLPersistable implements IPersistable{
    /*
     * Persistable impl
     */
    @XmlAttribute
    protected String id;
    @XmlTransient
    protected Date updated;
    @XmlTransient
    protected Map<String, Integer> ttlMap = new HashMap<String, Integer>();

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    @Override
    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public void setTTL(String column, int value) {
        ttlMap.put(column, value);
    }

    @Override
    public int getTTL(String column) {
        return ttlMap.containsKey(column) ? ttlMap.get(column) : 0;
    }

    @Override
    public void clearTTL() {
        ttlMap.clear();
    }
}

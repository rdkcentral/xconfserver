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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.Applicationable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.XEnvModel;
import org.apache.commons.collections.comparators.NullComparator;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * eSTB model.
 * <p/>
 * For this class the id is meaningful - something like PARKER234, etc. Probably
 * don't need description, but whatever.
 */
@CF(cfName = CfNames.Firmware.MODEL)
@XmlRootElement
public class Model extends XMLPersistable implements Comparable<Model>, XEnvModel {

    //@NotBlank
    private String description;

    public Model() {
    }

    public Model(String id, String description) {
        setId(id);
        this.description = description;
    }

    public Model(String id) {
        setId(id);
    }

    public void setId(@Pattern(regexp = "^[a-zA-Z0-9_]+$")@NotBlank String id) {
        this.id = (id != null) ? id.trim().toUpperCase() : null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int compareTo(Model o) {
        String name1 = (id != null) ? id.toLowerCase() : null;
        String name2 = (o != null && o.id != null) ? o.id.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Model)) {
            return false;
        }
        Model other = (Model) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Model{");
        sb.append("id='").append(id).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

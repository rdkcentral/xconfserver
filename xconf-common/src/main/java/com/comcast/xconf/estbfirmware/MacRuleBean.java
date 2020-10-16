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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * Mutable bean for creating mac rules.
 */
@XmlRootElement(name="macRule")
public class MacRuleBean implements Comparable<MacRuleBean> {

	private String id;

	@NotBlank
	private String name;

    @NotBlank
    private String macListRef;

    /**
     * left for backward compatibility
     */
    private String macAddresses;

	private Set<String> targetedModelIds = new HashSet<String>();

	private FirmwareConfig firmwareConfig;

    @JsonIgnore
    public boolean getNoop() {
		return (targetedModelIds == null || targetedModelIds.isEmpty())
				&& firmwareConfig == null;
	}

	public void setNoop(boolean b) {
		// nada
	}

	public MacRuleBean(String id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Defensive copy constructor.
	 */
	public MacRuleBean(MacRuleBean o) {
		id = o.id;
		name = o.name;
		macAddresses = o.macAddresses;
		macListRef = o.macListRef;
	}

	public MacRuleBean() {

	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof MacRuleBean)) {
			return false;
		}
		MacRuleBean other = (MacRuleBean) obj;
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
    public int compareTo(MacRuleBean o) {
        String name1 = (name != null) ? name.toLowerCase() : null;
        String name2 = (o != null && o.name != null) ? o.name.toLowerCase() : null;
        return new NullComparator().compare(name1, name2);
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddresses() {
		return macAddresses;
	}

	public void setMacAddresses(String macAddresses) {
		this.macAddresses = macAddresses;
	}

    public String getMacListRef() {
        return macListRef;
    }

    public void setMacListRef(String macListRef) {
        this.macListRef = macListRef;
    }

    public Set<String> getTargetedModelIds() {
		return targetedModelIds;
	}

	public void setTargetedModelIds(Set<String> targetedModelIds) {
		this.targetedModelIds = targetedModelIds;
	}

	public FirmwareConfig getFirmwareConfig() {
		return firmwareConfig;
	}

	public void setFirmwareConfig(FirmwareConfig firmwareConfig) {
		this.firmwareConfig = firmwareConfig;
	}

}

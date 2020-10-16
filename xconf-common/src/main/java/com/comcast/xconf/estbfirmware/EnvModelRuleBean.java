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

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.validator.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

/**
 * Allows us to get input from the user about our env/model rule so we can save
 * it in the db and create a rule from it.
 */
public class EnvModelRuleBean implements Comparable<EnvModelRuleBean> {

    private String id;

    @NotBlank
    private String name;

    private FirmwareConfig firmwareConfig;

//	@NotEmpty
//	private Set<String> targetedModelIds = new HashSet<String>();

    @NotBlank
    private String environmentId;

    @NotBlank
    private String modelId;

    @JsonIgnore
    public boolean getNoop() {
        return firmwareConfig == null;
    }

    public void setNoop(boolean b) {
        // nada
    }

    /**
     * Defensive copy constructor.
     */
    public EnvModelRuleBean(EnvModelRuleBean o) {
        id = o.id;
        name = o.name;
        firmwareConfig = new FirmwareConfig(o.firmwareConfig);

//		targetedModelIds = new HashSet<String>(o.targetedModelIds);

        environmentId = o.environmentId;
        modelId = o.modelId;
    }

    public EnvModelRuleBean(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @JsonIgnore
    public Expression getExpression() {
        Expression s = new Expression();

//		s.setTargetedModelIds(targetedModelIds);

        s.setEnvironmentId(environmentId);
        s.setModelId(modelId);

        return s;
    }

    public void setFromExpression(Expression e) {
//		setTargetedModelIds(e.targetedModelIds);

        setEnvironmentId(e.environmentId);
        setModelId(e.modelId);
    }

    public static class Expression {
        private Set<String> targetedModelIds = new HashSet<String>();

        private String environmentId;
        private String modelId;
        private IpAddressGroup ipAddressGroup;

        public Set<String> getTargetedModelIds() {
            return targetedModelIds;
        }

        public void setTargetedModelIds(Set<String> targetedModelIds) {
            this.targetedModelIds = targetedModelIds;
        }

        public String getEnvironmentId() {
            return environmentId;
        }

        public void setEnvironmentId(String environmentId) {
            this.environmentId = environmentId;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public IpAddressGroup getIpAddressGroup() {
            return ipAddressGroup;
        }

        public void setIpAddressGroup(IpAddressGroup ipAddressGroup) {
            this.ipAddressGroup = ipAddressGroup;
        }
    }

    public EnvModelRuleBean() {

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
        if (!(obj instanceof EnvModelRuleBean)) {
            return false;
        }
        EnvModelRuleBean other = (EnvModelRuleBean) obj;
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
    public int compareTo(EnvModelRuleBean o) {
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

//	public Set<String> getTargetedModelIds() {
//		return targetedModelIds;
//	}
//
//	public void setTargetedModelIds(Set<String> targetedModelIds) {
//		this.targetedModelIds = targetedModelIds;
//	}

    public FirmwareConfig getFirmwareConfig() {
        return firmwareConfig;
    }

    public void setFirmwareConfig(FirmwareConfig firmwareConfig) {
        this.firmwareConfig = firmwareConfig;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
}

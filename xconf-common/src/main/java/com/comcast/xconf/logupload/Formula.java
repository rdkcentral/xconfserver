/* 
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
 *
 * Author: slavrenyuk
 * Created: 4/29/14
 */
package com.comcast.xconf.logupload;

import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.XMLPersistable;

import com.comcast.xconf.CfNames;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@CF(cfName = CfNames.LogUpload.FORMULA)
public class Formula extends XMLPersistable implements Comparable<Formula> {

    @NotBlank
    private String name;
    @NotBlank
    private String description;

    protected String estbIP; // id for IpAddressGroup

    protected String estbMacAddress;

    protected String ecmMacAddress;

    protected Set<String> env;

    protected Set<String> model;

    protected Set<String> firmwareVersion;

    protected Set<String> controllerId;

    protected Set<String> channelMapId;

    protected Set<String> vodId;

    private Integer priority;
    private String ruleExpression;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer percentage = 100;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer percentageL1 = 0;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer percentageL2 = 0;

    @Min(0)
    @Max(100)
    @NotNull
    private Integer percentageL3 = 0;


    public Formula() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEstbIP() {
        return estbIP;
    }

    public void setEstbIP(String estbIP) {
        this.estbIP = estbIP;
    }

    public String getEstbMacAddress() {
        return estbMacAddress;
    }

    public String toLogString() {
        return
                "estbIP=" + estbIP +
                        " estbMacAddress=" + estbMacAddress +
                        " ecmMacAddress=" + ecmMacAddress +
                        " env=" + env +
                        " model=" + model +
                        " firmwareVersion=" + firmwareVersion +
                        " controllerId=" + controllerId +
                        " channelMapId=" + channelMapId +
                        " vodId=" + vodId;
    }


    public void setEstbMacAddress(String estbMacAddress) {
        this.estbMacAddress = estbMacAddress;
    }

    public String getEcmMacAddress() {
        return ecmMacAddress;
    }

    public void setEcmMacAddress(String ecmMacAddress) {
        this.ecmMacAddress = ecmMacAddress;
    }

    public Set<String> getEnv() {
        return env;
    }

    public void setEnv(Set<String> env) {
        this.env = env;
    }

    public Set<String> getModel() {
        return model;
    }

    public void setModel(Set<String> model) {
        this.model = model;
    }

    public Set<String> getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(Set<String> firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Set<String> getControllerId() {
        return controllerId;
    }

    public void setControllerId(Set<String> controllerId) {
        this.controllerId = controllerId;
    }

    public Set<String> getChannelMapId() {
        return channelMapId;
    }

    public void setChannelMapId(Set<String> channelMapId) {
        this.channelMapId = channelMapId;
    }

    public Set<String> getVodId() {
        return vodId;
    }

    public void setVodId(Set<String> vodId) {
        this.vodId = vodId;
    }

    public String getRuleExpression() {
        return ruleExpression;
    }

    public void setRuleExpression(String ruleExpression) {
        this.ruleExpression = ruleExpression;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public Integer getPercentageL1() {
        return percentageL1;
    }

    public void setPercentageL1(Integer percentageL1) {
        this.percentageL1 = percentageL1;
    }

    public Integer getPercentageL2() {
        return percentageL2;
    }

    public void setPercentageL2(Integer percentageL2) {
        this.percentageL2 = percentageL2;
    }

    public Integer getPercentageL3() {
        return percentageL3;
    }

    public void setPercentageL3(Integer percentageL3) {
        this.percentageL3 = percentageL3;
    }

    public String toStringOnlyBaseProperties() {
        return
                "estbIP='" + estbIP + '\'' +
                ", estbMacAddress='" + estbMacAddress + '\'' +
                ", ecmMacAddress='" + ecmMacAddress + '\'' +
                ", env='" + env + '\'' +
                ", model='" + model + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", controllerId='" + controllerId + '\'' +
                ", channelMapId='" + channelMapId + '\'' +
                ", vodId='" + vodId + '\'';
    }

    @Override
    public String toString() {
        return "Formula{" +
                "ruleId='" + super.id + '\'' +
                ", estbIP='" + estbIP + '\'' +
                ", estbMacAddress='" + estbMacAddress + '\'' +
                ", ecmMacAddress='" + ecmMacAddress + '\'' +
                ", env='" + env + '\'' +
                ", model='" + model + '\'' +
                ", firmwareVersion='" + firmwareVersion + '\'' +
                ", controllerId='" + controllerId + '\'' +
                ", channelMapId='" + channelMapId + '\'' +
                ", vodId='" + vodId + '\'' +
                ", percentage='" + percentage + '\'' +
                ", percentageL1='" + percentageL1 + '\'' +
                ", percentageL2='" + percentageL2 + '\'' +
                ", percentageL3='" + percentageL3 + '\'' +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Formula formula = (Formula) o;

        if (name != null ? !name.equals(formula.name) : formula.name != null) return false;
        if (description != null ? !description.equals(formula.description) : formula.description != null) return false;
        if (estbIP != null ? !estbIP.equals(formula.estbIP) : formula.estbIP != null) return false;
        if (estbMacAddress != null ? !estbMacAddress.equals(formula.estbMacAddress) : formula.estbMacAddress != null)
            return false;
        if (ecmMacAddress != null ? !ecmMacAddress.equals(formula.ecmMacAddress) : formula.ecmMacAddress != null)
            return false;
        if (env != null ? !env.equals(formula.env) : formula.env != null) return false;
        if (model != null ? !model.equals(formula.model) : formula.model != null) return false;
        if (firmwareVersion != null ? !firmwareVersion.equals(formula.firmwareVersion) : formula.firmwareVersion != null)
            return false;
        if (controllerId != null ? !controllerId.equals(formula.controllerId) : formula.controllerId != null)
            return false;
        if (channelMapId != null ? !channelMapId.equals(formula.channelMapId) : formula.channelMapId != null)
            return false;
        if (vodId != null ? !vodId.equals(formula.vodId) : formula.vodId != null) return false;
        if (priority != null ? !priority.equals(formula.priority) : formula.priority != null) return false;
        if (ruleExpression != null ? !ruleExpression.equals(formula.ruleExpression) : formula.ruleExpression != null)
            return false;
        if (percentage != null ? !percentage.equals(formula.percentage) : formula.percentage != null) return false;
        if (percentageL1 != null ? !percentageL1.equals(formula.percentageL1) : formula.percentageL1 != null)
            return false;
        if (percentageL2 != null ? !percentageL2.equals(formula.percentageL2) : formula.percentageL2 != null)
            return false;
        return !(percentageL3 != null ? !percentageL3.equals(formula.percentageL3) : formula.percentageL3 != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (estbIP != null ? estbIP.hashCode() : 0);
        result = 31 * result + (estbMacAddress != null ? estbMacAddress.hashCode() : 0);
        result = 31 * result + (ecmMacAddress != null ? ecmMacAddress.hashCode() : 0);
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (model != null ? model.hashCode() : 0);
        result = 31 * result + (firmwareVersion != null ? firmwareVersion.hashCode() : 0);
        result = 31 * result + (controllerId != null ? controllerId.hashCode() : 0);
        result = 31 * result + (channelMapId != null ? channelMapId.hashCode() : 0);
        result = 31 * result + (vodId != null ? vodId.hashCode() : 0);
        result = 31 * result + (priority != null ? priority.hashCode() : 0);
        result = 31 * result + (ruleExpression != null ? ruleExpression.hashCode() : 0);
        result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
        result = 31 * result + (percentageL1 != null ? percentageL1.hashCode() : 0);
        result = 31 * result + (percentageL2 != null ? percentageL2.hashCode() : 0);
        result = 31 * result + (percentageL3 != null ? percentageL3.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Formula formula) {
        int thisPriority = (this.getPriority() != null) ? this.getPriority() : 0;
        int formulaPriority = (formula != null && formula.getPriority() != null) ? formula.getPriority() : 0;

        return thisPriority - formulaPriority;
    }

    @JsonIgnore
    public Map<String, Set<String>> getProperties() {
        return new HashMap<String, Set<String>>() {{
            put("env", env);
            put("model", model);
            put("firmwareVersion", firmwareVersion);
            put("controllerId", controllerId);
            put("channelMapId", channelMapId);
            put("vodId", vodId);
        }};
    }
}

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
* Author: rdolomansky
* Created: 07.07.2014 13:28
*/
package com.comcast.xconf.dcm.manager.web;

import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.logupload.annotation.Compare;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

public class FormulaDataObject implements Comparable<FormulaDataObject> {

    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Compare
    private String estbIP;
    @Compare
    private String estbMacAddress;
    @Compare
    private String ecmMacAddress;
    @Compare
    private String env;

    private List<String> envList;
    @Compare
    private String model;
    @Compare
    private String firmwareVersion;
    @Compare
    private String controllerId;
    @Compare
    private String channelMapId;
    @Compare
    private String vodId;

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

    private String applicationType = ApplicationType.STB;

    protected int fieldsCount = -1;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setEstbMacAddress(String estbMacAddress) {
        this.estbMacAddress = estbMacAddress;
    }

    public String getEcmMacAddress() {
        return ecmMacAddress;
    }

    public void setEcmMacAddress(String ecmMacAddress) {
        this.ecmMacAddress = ecmMacAddress;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<String> getEnvList() {
        return envList;
    }

    public void setEnvList(List<String> envList) {
        this.envList = envList;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getChannelMapId() {
        return channelMapId;
    }

    public void setChannelMapId(String channelMapId) {
        this.channelMapId = channelMapId;
    }

    public String getVodId() {
        return vodId;
    }

    public void setVodId(String vodId) {
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

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public int getFieldsCount() {
        if (fieldsCount < 0) {
            fieldsCount = 0;
            testField(estbMacAddress);
            testField(ecmMacAddress);
            testField(estbIP);
            testField(env);
            testField(model);
            testField(firmwareVersion);
            testField(controllerId);
            testField(channelMapId);
            testField(vodId);
        }

        return fieldsCount;
    }

    private void testField(String fieldValue) {
        if (fieldValue != null && !fieldValue.isEmpty()) {
            fieldsCount ++;
        }
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
                "ruleId='" + id + '\'' +
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
    public int compareTo(FormulaDataObject o) {
        if (this.priority == null) this.priority=0;
        if (o.getPriority() == null) o.setPriority(0);
        return this.priority-o.getPriority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormulaDataObject)) return false;

        FormulaDataObject that = (FormulaDataObject) o;

        if (channelMapId != null ? !channelMapId.equals(that.channelMapId) : that.channelMapId != null) return false;
        if (controllerId != null ? !controllerId.equals(that.controllerId) : that.controllerId != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (ecmMacAddress != null ? !ecmMacAddress.equals(that.ecmMacAddress) : that.ecmMacAddress != null)
            return false;
        if (env != null ? !env.equals(that.env) : that.env != null) return false;
        if (envList != null ? !envList.equals(that.envList) : that.envList != null) return false;
        if (estbIP != null ? !estbIP.equals(that.estbIP) : that.estbIP != null) return false;
        if (estbMacAddress != null ? !estbMacAddress.equals(that.estbMacAddress) : that.estbMacAddress != null)
            return false;
        if (firmwareVersion != null ? !firmwareVersion.equals(that.firmwareVersion) : that.firmwareVersion != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (model != null ? !model.equals(that.model) : that.model != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (percentage != null ? !percentage.equals(that.percentage) : that.percentage != null) return false;
        if (percentageL1 != null ? !percentageL1.equals(that.percentageL1) : that.percentageL1 != null) return false;
        if (percentageL2 != null ? !percentageL2.equals(that.percentageL2) : that.percentageL2 != null) return false;
        if (percentageL3 != null ? !percentageL3.equals(that.percentageL3) : that.percentageL3 != null) return false;
        if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
        if (ruleExpression != null ? !ruleExpression.equals(that.ruleExpression) : that.ruleExpression != null)
            return false;
        if (vodId != null ? !vodId.equals(that.vodId) : that.vodId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (id != null) ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (estbIP != null ? estbIP.hashCode() : 0);
        result = 31 * result + (estbMacAddress != null ? estbMacAddress.hashCode() : 0);
        result = 31 * result + (ecmMacAddress != null ? ecmMacAddress.hashCode() : 0);
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (envList != null ? envList.hashCode() : 0);
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
}

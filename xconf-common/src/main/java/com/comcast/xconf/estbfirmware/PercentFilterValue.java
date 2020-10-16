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
 * Created: 6/11/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.Map;

public class PercentFilterValue extends SingletonFilterValue {

    public static final String SINGLETON_ID = "PERCENT_FILTER_VALUE";

    private IpAddressGroup whitelist;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double percentage = 100;

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private int percent = 100;

    @Valid
    private Map<String, EnvModelPercentage> envModelPercentages;

    public PercentFilterValue() {
    }

    public PercentFilterValue(IpAddressGroup whiteList, double percentage, Map<String, EnvModelPercentage> envModelPercentages) {
        this.whitelist = whiteList;
        this.percentage = percentage;
        this.envModelPercentages = envModelPercentages;
    }

    @Override
    public String getId() {
        return SINGLETON_ID;
    }

    @Override
    public void setId(String id) {
        if (!SINGLETON_ID.equals(id)) {
            throw new IllegalArgumentException(PercentFilterValue.class.getSimpleName() + " id is " + SINGLETON_ID);
        }
    }

    public IpAddressGroup getWhitelist() {
        return whitelist;
    }

    public void setWhitelist(IpAddressGroup whitelist) {
        this.whitelist = whitelist;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Map<String, EnvModelPercentage> getEnvModelPercentages() {
        return envModelPercentages;
    }

    public void setEnvModelPercentages(Map<String, EnvModelPercentage> envModelPercentages) {
        this.envModelPercentages = envModelPercentages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PercentFilterValue that = (PercentFilterValue) o;

        if (Double.compare(that.percentage, percentage) != 0) return false;
        if (whitelist != null ? !whitelist.equals(that.whitelist) : that.whitelist != null) return false;
        return !(envModelPercentages != null ? !envModelPercentages.equals(that.envModelPercentages) : that.envModelPercentages != null);

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = whitelist != null ? whitelist.hashCode() : 0;
        temp = Double.doubleToLongBits(percentage);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (envModelPercentages != null ? envModelPercentages.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PercentFilterValue{" +
                "percentage=" + percentage +
                ", whiteList=" + whitelist +
                ", envModelPercentages=" + envModelPercentages +
                '}';
    }
}

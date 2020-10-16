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
package com.comcast.xconf.queries.beans;

import com.comcast.xconf.estbfirmware.EnvModelPercentage;

/**
 * Created by mdolina on 7/23/15.
 */
public class EnvModelPercentageWrapper extends EnvModelPercentage{
    private String name;

    public EnvModelPercentageWrapper() {
    }

    public EnvModelPercentageWrapper(String name, EnvModelPercentage percentage) {
        this.name = name;
        setPercentage(percentage.getPercentage());
        setWhitelist(percentage.getWhitelist());
        setActive(percentage.isActive());
        setFirmwareCheckRequired(percentage.isFirmwareCheckRequired());
        setFirmwareVersions(percentage.getFirmwareVersions());
        setRebootImmediately(percentage.isRebootImmediately());
        setLastKnownGood(percentage.getLastKnownGood());
        setIntermediateVersion(percentage.getIntermediateVersion());
    }

    public EnvModelPercentage toEnvModelPercentage() {
        EnvModelPercentage percentage = new EnvModelPercentage();
        percentage.setPercentage(getPercentage());
        percentage.setWhitelist(getWhitelist());
        percentage.setActive(isActive());
        percentage.setFirmwareCheckRequired(isFirmwareCheckRequired());
        percentage.setFirmwareVersions(getFirmwareVersions());
        percentage.setRebootImmediately(isRebootImmediately());
        percentage.setLastKnownGood(getLastKnownGood());
        percentage.setIntermediateVersion(getIntermediateVersion());
        return percentage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EnvModelPercentageWrapper{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

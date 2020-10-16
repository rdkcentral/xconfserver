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
 * Author: Stanislav Menshykov
 * Created: 25.11.15  13:26
 */
package com.comcast.xconf.logupload;

public class DCMRuleWithSettings {

    private DCMGenericRule formula;

    private DeviceSettings deviceSettings;

    private LogUploadSettings logUploadSettings;

    private VodSettings vodSettings;

    public DCMRuleWithSettings() {}

    public DCMRuleWithSettings(DCMGenericRule formula, DeviceSettings deviceSettings, LogUploadSettings logUploadSettings, VodSettings vodSettings) {
        this.formula = formula;
        this.deviceSettings = deviceSettings;
        this.logUploadSettings = logUploadSettings;
        this.vodSettings = vodSettings;
    }

    public DCMGenericRule getFormula() {
        return formula;
    }

    public void setFormula(DCMGenericRule formula) {
        this.formula = formula;
    }

    public DeviceSettings getDeviceSettings() {
        return deviceSettings;
    }

    public void setDeviceSettings(DeviceSettings deviceSettings) {
        this.deviceSettings = deviceSettings;
    }

    public LogUploadSettings getLogUploadSettings() {
        return logUploadSettings;
    }

    public void setLogUploadSettings(LogUploadSettings logUploadSettings) {
        this.logUploadSettings = logUploadSettings;
    }

    public VodSettings getVodSettings() {
        return vodSettings;
    }

    public void setVodSettings(VodSettings vodSettings) {
        this.vodSettings = vodSettings;
    }
}

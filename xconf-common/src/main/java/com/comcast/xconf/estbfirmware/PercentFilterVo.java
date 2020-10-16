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
 * Author: Igor Kostrov
 * Created: 12/12/2016
*/
package com.comcast.xconf.estbfirmware;

import java.util.List;

public class PercentFilterVo {

    private GlobalPercentage globalPercentage;
    private List<PercentageBean> percentageBeans;

    public PercentFilterVo() {}

    public PercentFilterVo(GlobalPercentage globalPercentage, List<PercentageBean> percentageBeans) {
        this.globalPercentage = globalPercentage;
        this.percentageBeans = percentageBeans;
    }

    public GlobalPercentage getGlobalPercentage() {
        return globalPercentage;
    }

    public void setGlobalPercentage(GlobalPercentage globalPercentage) {
        this.globalPercentage = globalPercentage;
    }

    public List<PercentageBean> getPercentageBeans() {
        return percentageBeans;
    }

    public void setPercentageBeans(List<PercentageBean> percentageBeans) {
        this.percentageBeans = percentageBeans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PercentFilterVo that = (PercentFilterVo) o;

        if (globalPercentage != null ? !globalPercentage.equals(that.globalPercentage) : that.globalPercentage != null)
            return false;
        return !(percentageBeans != null ? !percentageBeans.equals(that.percentageBeans) : that.percentageBeans != null);

    }

    @Override
    public int hashCode() {
        int result = globalPercentage != null ? globalPercentage.hashCode() : 0;
        result = 31 * result + (percentageBeans != null ? percentageBeans.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PercentFilterVo{" +
                "globalPercentage=" + globalPercentage +
                ", percentageBeans=" + percentageBeans +
                '}';
    }
}

/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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
 */

package com.comcast.xconf.estbfirmware;

import java.util.Objects;

public class RunningVersionInfo {

    private boolean hasActivationMinFW = false;

    private boolean hasMinimumFW = false;

    public boolean isHasActivationMinFW() {
        return hasActivationMinFW;
    }

    public void setHasActivationMinFW(boolean hasActivationMinFW) {
        this.hasActivationMinFW = hasActivationMinFW;
    }

    public boolean isHasMinimumFW() {
        return hasMinimumFW;
    }

    public void setHasMinimumFW(boolean hasMinimumFW) {
        this.hasMinimumFW = hasMinimumFW;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningVersionInfo that = (RunningVersionInfo) o;
        return hasActivationMinFW == that.hasActivationMinFW &&
                hasMinimumFW == that.hasMinimumFW;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasActivationMinFW, hasMinimumFW);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RunningVersionInfo{");
        sb.append("hasActivationMinFW=").append(hasActivationMinFW);
        sb.append(", hasMinimumFW=").append(hasMinimumFW);
        sb.append('}');
        return sb.toString();
    }
}

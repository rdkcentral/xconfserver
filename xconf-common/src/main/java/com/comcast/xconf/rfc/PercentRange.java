/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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

package com.comcast.xconf.rfc;

import java.util.Objects;

public class PercentRange implements Comparable<PercentRange> {

    private Double startRange;

    private Double endRange;

    public Double getStartRange() {
        return startRange;
    }

    public void setStartRange(Double startRange) {
        this.startRange = startRange;
    }

    public Double getEndRange() {
        return endRange;
    }

    public void setEndRange(Double endRange) {
        this.endRange = endRange;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PercentRange that = (PercentRange) o;
        return Objects.equals(startRange, that.startRange) &&
                Objects.equals(endRange, that.endRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startRange, endRange);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PercentRange{");
        sb.append(", startRange=").append(startRange);
        sb.append(", endRange=").append(endRange);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(PercentRange o) {
        if (o == null || this.startRange == null || o.startRange == null) {
            return 0;
        }
        if (o.startRange < this.startRange) return 1;
        if (o.startRange > this.startRange) return -1;
        return 0;
    }
}

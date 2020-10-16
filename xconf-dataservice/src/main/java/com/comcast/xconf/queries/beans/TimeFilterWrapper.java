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
 * Author: ikostrov
 * Created: 15.10.15 16:29
*/
package com.comcast.xconf.queries.beans;

import com.comcast.xconf.estbfirmware.TimeFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.LocalTime;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TimeFilter")
@JsonIgnoreProperties({"start", "end"})
public class TimeFilterWrapper extends TimeFilter {

    public static final String TIME_PATTERN = "HH:mm";

    private String startTime;
    private String endTime;

    public TimeFilterWrapper() {}

    public TimeFilterWrapper(TimeFilter filter) {
        super(filter);
        startTime = filter.getStart().toString(TIME_PATTERN);
        endTime = filter.getEnd().toString(TIME_PATTERN);
    }

    public TimeFilter toTimeFilter() {
        TimeFilter timeFilter = new TimeFilter(this);
        timeFilter.setStart(LocalTime.parse(startTime));
        timeFilter.setEnd(LocalTime.parse(endTime));
        return timeFilter;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TimeFilterWrapper that = (TimeFilterWrapper) o;

        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        return !(endTime != null ? !endTime.equals(that.endTime) : that.endTime != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0);
        return result;
    }
}

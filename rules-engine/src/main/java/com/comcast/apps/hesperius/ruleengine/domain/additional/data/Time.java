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
 * Created: 6/3/14
 */
package com.comcast.apps.hesperius.ruleengine.domain.additional.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.joda.time.LocalTime;

/**
 * Time without a time zone with second accuracy (HH:mm:ss format).
 *
 * ToStringSerializer + constructor with string argument allows serialization / deserialization as HH:mm:ss string
 */
@JsonSerialize(using = ToStringSerializer.class)
public final class Time implements Comparable<Time> {
    private static final String PATTERN = "HH:mm:ss";
    private final LocalTime localTime;

    /**
     * @param time HH:mm:ss or HH:mm
     */
    public Time(String time) {
        int[] intParts = parseAndMaybeAddSeconds(time);
        localTime = new LocalTime(intParts[0], intParts[1], intParts[2]);
    }

    public static Time parse(String time) {
        return new Time(time);
    }

    @Override
    public String toString() {
        return localTime.toString(PATTERN);
    }

    @Override
    public int compareTo(Time other) {
        return localTime.compareTo(other.localTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Time)) {
            return false;
        }
        return localTime.equals(((Time)obj).localTime);
    }

    @Override
    public int hashCode() {
        return localTime.hashCode();
    }

    /**
     * @param time HH:mm:ss or HH:mm
     */
    private static int[] parseAndMaybeAddSeconds(String time) {
        String[] parts = time.split(":");
        if (parts.length < 2 || parts.length > 3) {
            throw new IllegalArgumentException("Time string illegal format. Expected 'HH:mm:ss' or 'HH:mm'. Actual '" + time + "'");
        }
        int[] result = new int[3]; // all elements are initially zeros by java spec, so if args.length==2 anyway correct result
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i]);
        }
        return result;
    }
}

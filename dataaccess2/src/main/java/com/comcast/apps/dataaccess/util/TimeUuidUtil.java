/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.util;

import java.util.UUID;

public class TimeUuidUtil {

    /**
     * Utility method for building range for query.
     * Create UUID for specified timestamp. Returned UUID will be less or equal ({@link UUID#compareTo(java.util.UUID)})
     * than any other UUID with specified timestamp.
     *
     * @param timestampMillis timestamp in standard Java format
     * @return UUID for timestampMillis
     */
    public static UUID createQueryUuid(long timestampMillis) {
        return new UUID(createTime(timestampMillis), Long.MIN_VALUE);
    }

    /**
     * Create time value for UUID. See {@link com.eaio.uuid.UUIDGen#createTime(long)}. Algorithm converts time from
     * standard Java format (number of milliseconds since January 1, 1970, 00:00:00 GMT) to format used in UUID
     * (number of 100 nanoseconds intervals from October 15, 1582) and performs additional conversion.
     * Returned time value will be less or equal than any other value for the timestamp created by UUID generation algorithm.
     *
     * @param timestampMillis standard Java time
     * @return converted time for UUID
     */
    public static long createTime(long timestampMillis) {
        long time;
        // UTC time
        long timeMillis = (timestampMillis * 10000) + 0x01B21DD213814000L;
        // time low
        time = timeMillis << 32;
        // time mid
        time |= (timeMillis & 0xFFFF00000000L) >> 16;
        // time hi and version
        time |= 0x1000 | ((timeMillis >> 48) & 0x0FFF); // version 1
        return time;
    }

}

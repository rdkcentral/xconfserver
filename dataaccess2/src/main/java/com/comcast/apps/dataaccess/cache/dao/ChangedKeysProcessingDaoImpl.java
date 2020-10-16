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
 */
package com.comcast.apps.dataaccess.cache.dao;

import com.comcast.apps.dataaccess.cache.data.ChangedData;
import com.comcast.apps.dataaccess.dao.impl.ListingDaoImpl;
import com.comcast.apps.dataaccess.dao.query.RangeInfo;
import com.comcast.apps.dataaccess.util.TimeUuidUtil;
import com.datastax.driver.core.Session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ChangedKeysProcessingDaoImpl extends ListingDaoImpl<Long, UUID, ChangedData> {
    private final long changedKeysTimeWindowSize;

    public ChangedKeysProcessingDaoImpl(final Session session, final String cfName, final long changedKeysTimeWindowSize) {
        super(session, ChangedData.class, cfName);
        this.changedKeysTimeWindowSize = changedKeysTimeWindowSize;
    }

    /**
     * @param tickStart start timestamp of data that should be read, inclusive
     * @param tickEnd   end timestamp of data that should be read, exclusive
     * @return iterator over retrieved objects
     */
    public Iterator<ChangedData> getIteratedChangedKeysForTick(final long tickStart, final long tickEnd) {
        long currentRowKey = tickStart - (tickStart % changedKeysTimeWindowSize);
        final long endRowKey = tickEnd - (tickEnd % changedKeysTimeWindowSize);

        final UUID startUuid = TimeUuidUtil.createQueryUuid(tickStart);

        final Map<Long, RangeInfo<UUID>> ranges = new HashMap<>();
        ranges.put(currentRowKey, buildRange(startUuid, null));
        currentRowKey += changedKeysTimeWindowSize;
        while(currentRowKey <= endRowKey) {
	   ranges.put(currentRowKey, buildRange(null, null));
	   currentRowKey += changedKeysTimeWindowSize;
        }
        getLogger().info("Getting changed keys for tick {} - {} @ {}", tickStart, tickEnd, buildLogForRanges(ranges));
        return getRange(ranges).iterator();
    }

    private RangeInfo<UUID> buildRange(final UUID startColumn, final UUID endColumn) {
        return new RangeInfo<>(startColumn, endColumn);
    }

    private String buildLogForRanges(final Map<Long, RangeInfo<UUID>> ranges) {
        final StringBuilder result = new StringBuilder();
        final Iterator<Map.Entry<Long, RangeInfo<UUID>>> rangesIterator = ranges.entrySet().iterator();
        while (rangesIterator.hasNext()) {
	   final Map.Entry<Long, RangeInfo<UUID>> entry = rangesIterator.next();
	   final RangeInfo<UUID> columnRange = entry.getValue();
	   result
		  .append("Row Key: ").append(entry.getKey()).append("; ")
		  .append("Start Column Name: ").append(columnRange.getStartValue());
	   if (rangesIterator.hasNext()) {
	       result.append(" @ ");
	   }
        }
        return result.toString();
    }
}

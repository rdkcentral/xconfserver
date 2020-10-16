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
 * Author: Igor Kostrov
 * Created: 10/29/19
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.dao.ListingDao;
import com.comcast.apps.dataaccess.dao.query.RangeInfo;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ConfigChangeLogService {

    public static final String DEFAULT_PREFIX = "XCONF";
    public static final Byte BOUNDS = 5;
    private String prefix;

    @Autowired
    private ListingDao<String, String, LastConfigLog> lastConfigLogDAO;

    @Autowired
    private ListingDao<String, String, ConfigChangeLog> configChangeLogDAO;

    @PostConstruct
    public void init() {
        try {
            prefix = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            prefix = DEFAULT_PREFIX;
        }
    }

    public ConfigChangeLog setOne(String rowKey, ConfigChangeLog log) {
        String currentId = getCurrentId(rowKey);
        log.setId(currentId);
        return configChangeLogDAO.setOne(rowKey, currentId, log);
    }

    public LastConfigLog setOne(String mac, LastConfigLog log) {
        return lastConfigLogDAO.setOne(mac, LastConfigLog.LAST_CONFIG_LOG_ID, log);
    }

    public LastConfigLog getLastConfigLog(String mac) {
        return lastConfigLogDAO.getOne(mac, LastConfigLog.LAST_CONFIG_LOG_ID);
    }

    public List<ConfigChangeLog> getChangeLogsOnly(String rowKey) {
        return getAll(rowKey, ConfigChangeLog.FILTER_LAST_CONFIG);
    }

    private List<ConfigChangeLog> getAll(String rowKey, Predicate<ConfigChangeLog> filter) {
        List<ConfigChangeLog> result = Lists.newArrayList(
                Iterables.filter(configChangeLogDAO.getAll(rowKey), filter)
        );
        Collections.sort(result, COMPARATOR_BY_UPDATE_DESCENDING);
        return result;
    }

    private String getCurrentId(String rowKey) {
        Byte count  = getCountFromCassandra(rowKey);
        count = (count == null || count == 1) ? BOUNDS : (byte)(count - 1);
        return numberToColumnName(count);
    }

    private Byte getCountFromCassandra(String rowKey) {
        RangeInfo<String> rangeInfo = new RangeInfo<>(numberToColumnName((byte) 0), numberToColumnName((byte)(BOUNDS + 1)));
        List<ConfigChangeLog> logs = configChangeLogDAO.getRange(rowKey, rangeInfo);

        if (!logs.isEmpty()) {
            Collections.sort(logs, COMPARATOR_BY_UPDATE_DESCENDING);
            return columnNameToNumber(logs.get(0).getId());
        }

        return null;
    }

    private String numberToColumnName(byte number) {
        return prefix + "_" + number;
    }

    private byte columnNameToNumber(String columnName) {
        return Byte.parseByte(columnName.substring((prefix + "_").length()));
    }

    private final Comparator<ConfigChangeLog> COMPARATOR_BY_UPDATE_DESCENDING = new Comparator<ConfigChangeLog>() {
        @Override
        public int compare(ConfigChangeLog o1, ConfigChangeLog o2) {
            return (o2.getUpdated() != null && o1.getUpdated() != null) ? o2.getUpdated().compareTo(o1.getUpdated()) : 0;
        }
    };

}

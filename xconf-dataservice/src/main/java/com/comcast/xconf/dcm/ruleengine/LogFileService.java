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
package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.DaoUtil;
import com.comcast.xconf.logupload.LogFile;
import com.comcast.xconf.logupload.LogFileList;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: ikostrov
 * Date: 24.09.14
 * Time: 21:20
 */
@Component
public class LogFileService {

    @Autowired
    private CachedSimpleDao<String, LogFileList> logFileListDAO;

    public LogFile getOne(String rowKey, String id) {
        LogFileList one = DaoUtil.getFromCache(logFileListDAO, rowKey);
        if (one != null && one.getData() != null) {
            for (LogFile logFile : one.getData()) {
                if (StringUtils.equals(id, logFile.getId())) {
                    return logFile;
                }
            }
        }
        return null;
    }

    public LogFile setOne(String rowKey, LogFile obj) {
        LogFileList one = getLogFileList(rowKey);

        for (Iterator<LogFile> iterator = one.getData().iterator(); iterator.hasNext(); ) {
            LogFile logFile = iterator.next();
            if (StringUtils.equals(obj.getId(), logFile.getId())) {
                iterator.remove();
                break;
            }
        }

        one.getData().add(obj);
        logFileListDAO.setOne(rowKey, one);
        return obj;
    }

    public List<LogFile> setMultiple(String rowKey, List<LogFile> list) {
        LogFileList one = getLogFileList(rowKey);
        for (Iterator<LogFile> iterator = one.getData().iterator(); iterator.hasNext(); ) {
            LogFile logFile = iterator.next();
            for (LogFile newLogFile : list) {
                if (StringUtils.equals(newLogFile.getId(), logFile.getId())) {
                    iterator.remove();
                }
            }
        }
        one.getData().addAll(list);
        logFileListDAO.setOne(rowKey, one);
        return list;
    }

    public void deleteOne(String rowKey, String id) {
        LogFileList one = getLogFileList(rowKey);

        for (Iterator<LogFile> iterator = one.getData().iterator(); iterator.hasNext(); ) {
            LogFile logFile = iterator.next();
            if (StringUtils.equals(id, logFile.getId())) {
                iterator.remove();
                saveLogFileList(rowKey, one);
                break;
            }
        }
    }

    private LogFileList getLogFileList(String rowKey) {
        LogFileList one = DaoUtil.getFromCache(logFileListDAO, rowKey);
        if (one == null) {
            one = new LogFileList();
        }
        if (one.getData() == null) {
            one.setData(new ArrayList<LogFile>());
        }
        return one;
    }

    private void saveLogFileList(String rowKey, LogFileList one) {
        try {
            logFileListDAO.setOne(rowKey, one);
        } catch (Exception e) {
            throw new RuntimeException("Not able to save LogFileList with rowKey: " + rowKey, e);
        }
    }

    public void deleteAll(String rowKey) {
        logFileListDAO.deleteOne(rowKey);
    }

    public List<LogFile> getAll(String rowKey, int maxResults) {
        LogFileList one = getLogFileList(rowKey);

        List<LogFile> logFiles = one.getData();
        if (logFiles.size() <= maxResults) {
            return logFiles;
        } else {
            return new ArrayList<>(logFiles.subList(0, maxResults-1));
        }
    }

    public List<LogFile> getAll(String rowKey) {
        LogFileList one = getLogFileList(rowKey);
        return new ArrayList<>(one.getData());
    }

}

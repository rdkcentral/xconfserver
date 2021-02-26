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
package com.comcast.xconf.admin.controller.shared;

import com.comcast.apps.dataaccess.cache.CacheManager;
import com.comcast.apps.dataaccess.cache.mbean.CacheInfo;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.xconf.auth.AuthService;
import com.comcast.xconf.shared.utils.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.logging.Level;

@RestController
@RequestMapping(StatisticsController.URL_MAPPING)
public class StatisticsController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    public static final String URL_MAPPING = "api/stats";

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AuthService authService;

    final List<String> legacyCFs = new ArrayList<>();

    {
        String unusedList = "LogFile,LogFilesGroups,LogFileList,ConfigurationServiceURLs";
        String legacyList = "XconfNamedList,IpAddressGroupExtended,Formula2";
        legacyCFs.addAll(Arrays.asList(unusedList.split("[,]")));
        legacyCFs.addAll(Arrays.asList(legacyList.split("[,]")));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, CacheInfo> statistics() {
        Map<String, CacheInfo> beans = new HashMap<>(cacheManager.getCacheMBeans());
        beans.keySet().removeAll(legacyCFs);
        return beans;
    }

    @RequestMapping(value = "/cache/reloadAll", method = RequestMethod.GET)
    public Map<String, CacheInfo> reloadAllCache() throws Exception {
        cacheManager.refreshAll();
        Map<String, CacheInfo> statistics = statistics();
        LoggingUtils.log(logger, Level.INFO, "Reloaded All cache: {}", authService.getUserName(), JsonUtil.toJson(statistics));
        return statistics;
    }

    @RequestMapping(value = "/cache/{cfname}/reload", method = RequestMethod.GET)
    public CacheInfo reloadCacheByCfName(@PathVariable("cfname") final String cfName) throws Exception {
        cacheManager.refreshAll(cfName);
        CacheInfo cacheInfo = cacheManager.getCacheMBeans().get(cfName);
        LoggingUtils.log(logger, Level.INFO, "Reloaded cache for {}: {}", authService.getUserName(), cfName, JsonUtil.toJson(cacheInfo));
        return cacheInfo;
    }

}

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
 * Created: 13.08.15 19:52
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.DownloadLocationFilterConverter;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.DOWNLOAD_LOCATION_FILTER;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class DownloadLocationFilterService {

    private static final Logger log = LoggerFactory.getLogger(DownloadLocationFilterService.class);

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private DownloadLocationFilterConverter converter;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public Set<DownloadLocationFilter> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(DOWNLOAD_LOCATION_FILTER))
                .filter(byApplication(applicationType))
                .map(this::convertOrReturnNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private DownloadLocationFilter convertOrReturnNull(FirmwareRule firmwareRule) {
        try {
            return convertFirmwareRuleToDownloadLocationFilter(firmwareRule);
        } catch (Exception e) {
            log.error("Could not convert: ", e);
            return null;
        }
    }

    public FirmwareRule convertDownloadLocationFilterToFirmwareRule(DownloadLocationFilter bean) {
        return converter.convert(bean);
    }

    public DownloadLocationFilter convertFirmwareRuleToDownloadLocationFilter(FirmwareRule firmwareRule) {
        return converter.convert(firmwareRule);
    }

    public DownloadLocationFilter getOneDwnLocationFilterFromDBById(String id) {
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if (fr != null) {
            return convertFirmwareRuleToDownloadLocationFilter(fr);
        }
        return null;
    }

    public DownloadLocationFilter getOneDwnLocationFilterFromDBByName(String name, String applicationType) {
        for (DownloadLocationFilter locationFilter : getByApplicationType(applicationType)) {
            if (locationFilter.getName().equalsIgnoreCase(name)) {
                return locationFilter;
            }
        }

        return null;
    }

    public void save(DownloadLocationFilter filter, String applicationType) {
        if(StringUtils.isBlank(filter.getId())){
            filter.setId(UUID.randomUUID().toString());
        }
        FirmwareRule rule = convertDownloadLocationFilterToFirmwareRule(filter);
        rule.setApplicationType(ApplicationType.get(applicationType));
        firmwareRuleDao.setOne(rule.getId(), rule);
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }
}

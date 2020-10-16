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
 * Created: 01.09.15 16:33
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.TimeFilterConverter;
import com.comcast.xconf.firmware.ApplicationType;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.TIME_FILTER;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class TimeFilterService {

    @Autowired
    private TimeFilterConverter timeFilterConverter;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public void save(TimeFilter filter, String applicationType) {
        if (StringUtils.isBlank(filter.getId())) {
            String id = UUID.randomUUID().toString();
            filter.setId(id);
        }
        FirmwareRule rule = timeFilterConverter.convert(filter);
        if (StringUtils.isNotBlank(applicationType)) {
            rule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(rule.getId(), rule);
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public TimeFilter getOneTimeFilterFromDB(String id) {
        TimeFilter timeFilter = null;
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if (fr != null) {
            timeFilter = convertToFilter(fr);
        }
        return timeFilter;
    }

    public TimeFilter getByName(String name, String applicationType) {
        for (FirmwareRule firmwareRule : firmwareRuleDao.getAll()) {
            if (TIME_FILTER.equals(firmwareRule.getType())
                    && firmwareRule.getName().equalsIgnoreCase(name)
                    && ApplicationType.equals(applicationType, firmwareRule.getApplicationType())) {
                return convertToFilter(firmwareRule);
            }
        }
        return null;
    }

    public Set<TimeFilter> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(TIME_FILTER))
                .filter(byApplication(applicationType))
                .map(this::convertToFilter)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private TimeFilter convertToFilter(FirmwareRule rule) {
        return timeFilterConverter.convert(rule);
    }

}

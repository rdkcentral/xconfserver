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
 * Created: 01.09.15 16:44
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.IpFilterConverter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.IP_FILTER;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class IpFilterService {

    @Autowired
    private IpFilterConverter ipFilterConverter;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public IpFilter getOneIpFilterFromDB(String id){
        IpFilter ipFilter = null;
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if(fr != null){
            ipFilter = convertFirmwareRuleToIpFilter(fr);
        }
        return ipFilter;
    }

    public IpFilter getIpFilterByName(String name, String applicationType) {
        for (IpFilter ipFilter : getByApplicationType(applicationType)) {
            if (StringUtils.equals(ipFilter.getName(), name)) {
                return ipFilter;
            }
        }
        return null;
    }

    public Set<IpFilter> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(IP_FILTER))
                .filter(byApplication(applicationType))
                .map(this::convertFirmwareRuleToIpFilter)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public void save(IpFilter filter, String applicationType) {
        if (StringUtils.isBlank(filter.getId())) {
            filter.setId(UUID.randomUUID().toString());
        }

        FirmwareRule rule = convertIpFilterToFirmwareRule(filter);
        if (StringUtils.isNotBlank(applicationType)) {
            rule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(rule.getId(), rule);
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public IpFilter convertFirmwareRuleToIpFilter(FirmwareRule firmwareRule){

        return ipFilterConverter.convertFirmwareRuleToIpFilter(firmwareRule);
    }

    public FirmwareRule convertIpFilterToFirmwareRule(IpFilter ipFilter){

        return ipFilterConverter.convertIpFilterToFirmwareRule(ipFilter);
    }

}

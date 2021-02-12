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
 * Created: 13.08.15 19:41
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.converter.IpRuleConverter;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.comcast.xconf.estbfirmware.TemplateNames.IP_RULE;
import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class IpRuleService {

    private static final Logger log = LoggerFactory.getLogger(IpRuleService.class);

    @Autowired
    private IpRuleConverter ipRuleConverter;

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public Set<IpRuleBean> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(firmwareRulePredicates.byTemplate(IP_RULE))
                .filter(byApplication(applicationType))
                .map(this::convertToIpRuleOrReturnNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private IpRuleBean convertToIpRuleOrReturnNull(FirmwareRule firmwareRule) {
        try{
            return convertFirmwareRuleToIpRuleBean(firmwareRule);
        } catch (Exception e) {
            log.error("Could not convert: ", e);
            return null;
        }
    }

    public FirmwareRule save(IpRuleBean bean, String applicationType) {
        if (StringUtils.isBlank(bean.getId())) {
            bean.setId(UUID.randomUUID().toString());
        }
        FirmwareRule ipRule = convertIpRuleBeanToFirmwareRule(bean);
        if (StringUtils.isNotBlank(applicationType)) {
            ipRule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(ipRule.getId(), ipRule);
        return ipRule;
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public IpRuleBean getOne(String id) {
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if (fr != null) {
            return convertFirmwareRuleToIpRuleBean(fr);
        }
        return null;
    }

    public FirmwareRule convertIpRuleBeanToFirmwareRule(IpRuleBean bean) {
        return ipRuleConverter.convertIpRuleBeanToFirmwareRule(bean);
    }

    public IpRuleBean convertFirmwareRuleToIpRuleBean(FirmwareRule firmwareRule) {
        IpRuleBean bean = ipRuleConverter.convertFirmwareRuleToIpRuleBean(firmwareRule);
        RuleAction action = (RuleAction) firmwareRule.getApplicableAction();
        if (StringUtils.isNotBlank(action.getConfigId())) {
            bean.setFirmwareConfig(firmwareConfigDAO.getOne(action.getConfigId()));
        }

        return bean;
    }

    public FirmwareConfig nullifyUnwantedFields(FirmwareConfig config) {
        if (config != null) {
            config.setUpdated(null);
            config.setFirmwareDownloadProtocol(null);
            config.setRebootImmediately(null);
        }

        return config;
    }

}

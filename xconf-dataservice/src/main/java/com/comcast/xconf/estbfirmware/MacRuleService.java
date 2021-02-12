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
 * Created: 06.08.15 19:33
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.MacAddressUtil;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.converter.MacRuleConverter;
import com.comcast.xconf.firmware.ApplicableAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.queries.beans.MacRuleBeanWrapper;
import com.comcast.xconf.search.firmware.FirmwareRulePredicates;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.comcast.xconf.search.ApplicationTypePredicate.byApplication;

@Service
public class MacRuleService {

    private static final Logger log = LoggerFactory.getLogger(MacRuleService.class);

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListLegacyService;

    @Autowired
    private FirmwareRulePredicates firmwareRulePredicates;

    public void save(MacRuleBean bean, String applicationType) {
        if (StringUtils.isBlank(bean.getId())) {
            bean.setId(UUID.randomUUID().toString());
        }
        FirmwareRule macRule = convertMacRuleBeanToFirmwareRule(bean);
        if (StringUtils.isNotBlank(applicationType)) {
            macRule.setApplicationType(applicationType);
        }
        firmwareRuleDao.setOne(macRule.getId(), macRule);
    }

    public MacRuleBean getOne(String id) {
        FirmwareRule fr = firmwareRuleDao.getOne(id);
        if (fr != null) {
            return convertFirmwareRuleToMacRuleBean(fr);
        }
        return null;
    }

    public Set<MacRuleBeanWrapper> getByApplicationType(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(byApplication(applicationType))
                .filter(firmwareRulePredicates.byActionType(ApplicableAction.Type.RULE.name()))
                .filter(firmwareRulePredicates.byTemplate(TemplateNames.MAC_RULE))
                .map(this::convertToMacRuleBeanOrReturnNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<MacRuleBeanWrapper> getRulesWithMacCondition(String applicationType) {
        return firmwareRuleDao.getAll()
                .stream()
                .filter(byApplication(applicationType))
                .filter(firmwareRulePredicates.byActionType(ApplicableAction.Type.RULE.name()))
                .filter(firmwareRulePredicates.byKey(StbContext.ESTB_MAC))
                .map(this::convertToMacRuleBeanOrReturnNull)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private MacRuleBeanWrapper convertToMacRuleBeanOrReturnNull(FirmwareRule firmwareRule) {
        try {
            return convertFirmwareRuleToMacRuleBean(firmwareRule);
        } catch (Exception e) {
            log.error("Could not convert: ", e);
            return null;
        }
    }

    public void delete(String id) {
        firmwareRuleDao.deleteOne(id);
    }

    public Set<MacRuleBeanWrapper> searchMacRules(String macPart, String applicationType) {

        macPart = MacAddressUtil.removeNonAlphabeticSymbols(macPart);

        Set<MacRuleBeanWrapper> macRules = getRulesWithMacCondition(applicationType);
        for (Iterator<MacRuleBeanWrapper> iterator = macRules.iterator(); iterator.hasNext(); ) {
            MacRuleBeanWrapper macRuleBean = iterator.next();
            Set<String> macAddressesToSearch = new HashSet<>();
            if (StringUtils.isNotBlank(macRuleBean.getMacListRef())) {
                NamespacedList macList = genericNamespacedListLegacyService.getNamespacedList(macRuleBean.getMacListRef());
                if (macList != null && CollectionUtils.isNotEmpty(macList.getData())) {
                    macAddressesToSearch.addAll(macList.getData());
                }
            }
            if (CollectionUtils.isNotEmpty(macRuleBean.getMacList())) {
                macAddressesToSearch.addAll(macRuleBean.getMacList());
            }

            if (!isExistMacAddressInList(macAddressesToSearch, macPart)) {
                iterator.remove();
            }
        }

        return macRules;
    }

    public FirmwareRule convertMacRuleBeanToFirmwareRule(MacRuleBean bean) {
        return MacRuleConverter.convertMacRuleBeanToFirmwareRule(bean);
    }

    public MacRuleBeanWrapper convertFirmwareRuleToMacRuleBean(FirmwareRule firmwareRule) {
        MacRuleBeanWrapper macRuleBean = MacRuleConverter.convertFirmwareRuleToMacRuleBeanWrapper(firmwareRule);
        RuleAction action = (RuleAction) firmwareRule.getApplicableAction();
        if (action != null && StringUtils.isNotBlank(action.getConfigId())) {
            FirmwareConfig config = firmwareConfigDAO.getOne(action.getConfigId());
            macRuleBean.setFirmwareConfig(config);
            macRuleBean.setTargetedModelIds(config != null ? config.getSupportedModelIds() : new HashSet<String>());
        }
        return macRuleBean;
    }

    private boolean isExistMacAddressInList(Set<String> macAddresses, String macPart) {
        for (String macAddress : macAddresses) {
            if (macAddress.replaceAll(":", "").contains(macPart)) {
                return true;
            }
        }
        return false;
    }

    public MacRuleBeanWrapper getRuleWithMacConditionByName(String ruleName, String applicationType) {
        for (MacRuleBeanWrapper macRule : getRulesWithMacCondition(applicationType)) {
            if (ruleName.equals(macRule.getName())) {
                return macRule;
            }
        }
        return null;
    }

    public MacRuleBeanWrapper getMacRuleByName(String ruleName, String applicationType) {
        for (MacRuleBeanWrapper macRule : getByApplicationType(applicationType)) {
            if (ruleName.equals(macRule.getName())) {
                return macRule;
            }
        }
        return null;
    }
}

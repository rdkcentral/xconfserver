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
 * Author: Igor Kostrov
 * Created: 12/29/2015
*/
package com.comcast.xconf.estbfirmware.converter;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.estbfirmware.DownloadLocationFilter;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.TemplateNames;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.firmware.DefinePropertiesAction;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DownloadLocationFilterConverter {

    private static final String HTTP_SUFFIX = "_http";
    private static final String TFTP_SUFFIX = "_tftp";
    private static final String HTTP_PROTOCOL = FirmwareConfig.DownloadProtocol.http.toString();
    private static final String TFTP_PROTOCOL = FirmwareConfig.DownloadProtocol.tftp.toString();

    @Autowired
    private GenericNamespacedListLegacyService genericNamespacedListService;

    public FirmwareRule convert(DownloadLocationFilter bean) {
        String ipList = bean.getIpAddressGroup().getName();
        boolean forceHttp = bean.getForceHttp() != null ? bean.getForceHttp() : false;
        IpAddress ipv4Location = bean.getFirmwareLocation();
        IpAddress ipv6Location = bean.getIpv6FirmwareLocation();
        String httpLocation = bean.getHttpLocation();

        boolean isTftpEmpty = ipv4Location == null;
        boolean isHttpEmpty = StringUtils.isBlank(httpLocation);
        if (!forceHttp && !isTftpEmpty && !isHttpEmpty) {
            throw new IllegalArgumentException("Can't convert DownloadLocationFilter into FirmwareRule because filter contains both locations for http and tftp.");
        } else if (forceHttp || isTftpEmpty) {
            Rule rule = hasProtocolSuffix(bean.getName()) ?
                    RuleFactory.newDownloadLocationFilter(ipList, HTTP_PROTOCOL) :
                    RuleFactory.newDownloadLocationFilter(ipList);
            return newFilter(rule, bean.getId(), bean.getName(), newHttpAction(httpLocation));
        } else {
            Rule rule = hasProtocolSuffix(bean.getName()) ?
                    RuleFactory.newDownloadLocationFilter(ipList, TFTP_PROTOCOL) :
                    RuleFactory.newDownloadLocationFilter(ipList);
            return newFilter(rule, bean.getId(), bean.getName(), newTftpAction(ipv4Location, ipv6Location));
        }
    }

    private boolean hasProtocolSuffix(String name) {
        return name != null && (name.endsWith(HTTP_SUFFIX) || name.endsWith(TFTP_SUFFIX));
    }

    public DownloadLocationFilter convert(FirmwareRule firmwareRule) {
        DownloadLocationFilter filter = new DownloadLocationFilter();
        filter.setId(firmwareRule.getId());
        filter.setName(firmwareRule.getName());

        String listRef = getListRef(firmwareRule.getRule());
        filter.setIpAddressGroup(genericNamespacedListService.getIpAddressGroup(listRef));
        DefinePropertiesAction action = (DefinePropertiesAction) firmwareRule.getApplicableAction();

        String location = action.getProperties().get(ConfigNames.FIRMWARE_LOCATION);
        String ipv6Location = action.getProperties().get(ConfigNames.IPV6_FIRMWARE_LOCATION);
        String protocol = action.getProperties().get(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL);

        if (TFTP_PROTOCOL.equals(protocol)) {
            filter.setForceHttp(false);
            filter.setFirmwareLocation(new IpAddress(location));
            filter.setIpv6FirmwareLocation(StringUtils.isNotBlank(ipv6Location) ? new IpAddress(ipv6Location) : null);
        } else {
            filter.setForceHttp(true);
            filter.setHttpLocation(location);
        }

        return filter;
    }

    public String getListRef(Rule firmwareRule) {
        List<Rule> rulesToSearch = new ArrayList<>();
        rulesToSearch.add(firmwareRule);
        while(!rulesToSearch.isEmpty()) {
            Rule rule = rulesToSearch.remove(0);

            String listRef = findListRef(rule.getCondition());
            if (listRef != null) {
                return listRef;
            }

            if (CollectionUtils.isNotEmpty(rule.getCompoundParts())) {
                rulesToSearch.addAll(rule.getCompoundParts());
            }
        }
        return null;
    }

    private String findListRef(Condition condition) {
        if (condition == null || condition.getFreeArg() == null) {
            return null;
        }
        if (ConverterHelper.isLegacyIpCondition(condition)) {
            IpAddressGroup group = (IpAddressGroup) condition.getFixedArg().getValue();
            return group.getName();
        }  else if (RuleFactory.IN_LIST.equals(condition.getOperation()) && RuleFactory.IP.equals(condition.getFreeArg())) {
            return (String) condition.getFixedArg().getValue();
        }
        return null;
    }

    private FirmwareRule newFilter(Rule rule, String id, String name, DefinePropertiesAction action) {
        FirmwareRule firmwareRule = new FirmwareRule();
        firmwareRule.setId(id != null ? id : UUID.randomUUID().toString());
        firmwareRule.setType(TemplateNames.DOWNLOAD_LOCATION_FILTER);
        firmwareRule.setName(name);
        firmwareRule.setRule(rule);
        firmwareRule.setApplicableAction(action);
        return firmwareRule;
    }

    private DefinePropertiesAction newTftpAction(IpAddress firmwareLocation, IpAddress ipv6FirmwareLocation) {
        if (firmwareLocation == null) {
            throw new IllegalArgumentException("Firmware location could not be null for TFTP location filter.");
        }
        String location = firmwareLocation.toString();
        String ipv6Location = ipv6FirmwareLocation != null ? ipv6FirmwareLocation.toString() : "";
        return newAction(location, ipv6Location, TFTP_PROTOCOL);
    }

    private DefinePropertiesAction newHttpAction(String location) {
        return newAction(location, "", HTTP_PROTOCOL);
    }

    private DefinePropertiesAction newAction(String location, String ipv6Location, String protocol) {
        Map<String, String> map = new HashMap<>();
        map.put(ConfigNames.FIRMWARE_LOCATION, location);
        map.put(ConfigNames.IPV6_FIRMWARE_LOCATION, ipv6Location);
        map.put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, protocol);
        return new DefinePropertiesAction(map);
    }
}

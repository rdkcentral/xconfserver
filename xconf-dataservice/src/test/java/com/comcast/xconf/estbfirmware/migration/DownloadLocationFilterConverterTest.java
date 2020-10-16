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
 * Created: 1/11/2016
*/
package com.comcast.xconf.estbfirmware.migration;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.estbfirmware.DownloadLocationFilter;
import com.comcast.xconf.estbfirmware.DownloadLocationFilterService;
import com.comcast.xconf.estbfirmware.FilterAction;
import com.comcast.xconf.estbfirmware.FirmwareRule;
import com.comcast.xconf.estbfirmware.converter.DownloadLocationFilterConverter;
import com.comcast.xconf.service.GenericNamespacedListLegacyService;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadLocationFilterConverterTest {

    public static final String CONFIG_ID = "configId";
    public static final String FILTER_ID = "filterId";
    public static final String GROUP_ID = "ipAddressGroupId";

    private IpAddress ipAddress = new IpAddress("1.1.1.1");
    private IpAddress ipv6Address = new IpAddress("1::1");
    private String httpLocation = "http://location.comcast.com";
    private IpAddressGroup ipAddressGroup = createIpAddressGroup();
    private FilterAction action = new FilterAction();

    private DownloadLocationFilterConverter converter = new DownloadLocationFilterConverter();
    private DownloadLocationFilterService service = new DownloadLocationFilterService();

    @Before
    public void setUp() throws Exception {
        CachedSimpleDao<String, FilterAction> filterActionDAO = mock(CachedSimpleDao.class);
        when(filterActionDAO.getOne(CONFIG_ID)).thenReturn(action);
        injectField(filterActionDAO, "filterActionDAO");

        GenericNamespacedListLegacyService nsListService = mock(GenericNamespacedListLegacyService.class);
        when(nsListService.getIpAddressGroup(GROUP_ID)).thenReturn(new IpAddressGroupExtended(ipAddressGroup));
        injectField(nsListService, "genericNamespacedListService");
    }

    private void injectField(Object object, String fieldName) {
        Field field = ReflectionUtils.findField(DownloadLocationFilterConverter.class, fieldName);
        field.setAccessible(true);
        ReflectionUtils.setField(field, converter, object);
    }

    @Test
    public void convertIntoTwoRulesWhenBothHttpAndTftpLocationExist() throws Exception {
        setupFullAction();
        DownloadLocationFilter filter = createBean();
        FirmwareRule firmwareRule = createFirmwareRule(filter);

        List<com.comcast.xconf.firmware.FirmwareRule> rules = converter.convert(firmwareRule);

        Assert.assertEquals(2, rules.size());
        Assert.assertEquals(ipAddressGroup, converter.convert(rules.get(0)).getIpAddressGroup());
        Assert.assertEquals(ipAddressGroup, converter.convert(rules.get(1)).getIpAddressGroup());
    }

    @Test
    public void convertIntoOneRule_Http() throws Exception {
        setupHttpAction();
        DownloadLocationFilter filter = createBean();
        FirmwareRule firmwareRule = createFirmwareRule(filter);

        List<com.comcast.xconf.firmware.FirmwareRule> rules = converter.convert(firmwareRule);

        Assert.assertEquals(1, rules.size());
        DownloadLocationFilter converted = converter.convert(rules.get(0));
        Assert.assertEquals(filter, converted);
    }

    @Test
    public void convertIntoOneRule_Tftp() throws Exception {
        setupTftpAction();
        DownloadLocationFilter filter = createBean();
        FirmwareRule firmwareRule = createFirmwareRule(filter);

        List<com.comcast.xconf.firmware.FirmwareRule> rules = converter.convert(firmwareRule);

        Assert.assertEquals(1, rules.size());
        DownloadLocationFilter converted = converter.convert(rules.get(0));
        Assert.assertEquals(filter, converted);
    }

    private FirmwareRule createFirmwareRule(DownloadLocationFilter bean) {
        FirmwareRule firmwareRule = FirmwareRule.newDownloadLocationFilter(
                bean.getIpAddressGroup(), bean.getEnvironments(), bean.getModels());
        firmwareRule.setId(bean.getId());
        firmwareRule.setName(bean.getName());
        firmwareRule.setBoundConfigId(CONFIG_ID);
        return firmwareRule;
    }

    private FilterAction setupFullAction() {
        action.setId(CONFIG_ID);
        action.setFirmwareLocation(ipAddress);
        action.setHttpLocation(httpLocation);
        action.setIpv6FirmwareLocation(ipv6Address);
        action.setForceHttp(false);
        return action;
    }

    private FilterAction setupTftpAction() {
        action.setId(CONFIG_ID);
        action.setFirmwareLocation(ipAddress);
        action.setIpv6FirmwareLocation(ipv6Address);
        action.setHttpLocation(null);
        action.setForceHttp(false);
        return action;
    }

    private FilterAction setupHttpAction() {
        action.setId(CONFIG_ID);
        action.setHttpLocation(httpLocation);
        action.setForceHttp(true);
        action.setFirmwareLocation(null);
        action.setIpv6FirmwareLocation(null);
        return action;
    }

    private DownloadLocationFilter createBean() {
        DownloadLocationFilter filter = new DownloadLocationFilter();
        filter.setId(FILTER_ID);
        filter.setName(FILTER_ID);
        filter.setIpAddressGroup(ipAddressGroup);
        filter.setIpv6FirmwareLocation(ipv6Address);
        filter.setFirmwareLocation(ipAddress);
        filter.setHttpLocation(httpLocation);
        return filter;
    }

    private IpAddressGroup createIpAddressGroup() {
        IpAddressGroup group = new IpAddressGroup();
        group.setId(GROUP_ID);
        group.setName("ipAddressGroupId");
        group.setIpAddresses(Sets.newHashSet(new IpAddress("127.0.0.1")));
        return group;
    }

}

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
 * Author: obaturynskyi
 * Created: 04.07.2014  17:55
 */
package com.comcast.xconf;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.estbfirmware.FirmwareConfig;
import com.comcast.xconf.estbfirmware.Model;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * This is the Spring conversion service -
 * http://static.springsource.org/spring/
 * docs/3.1.2.RELEASE/spring-framework-reference
 * /htmlsingle/spring-framework-reference.html#core-convert
 * <p>
 * Converters are used in Spel, Controllers, etc. They are super handy. Get to
 * know them.
 * <p>
 * Had to use ApplicationContextAware to get around circular dependency issues.
 */
@Component("conversionService")
public class ConversionServiceImpl extends GenericConversionService {

    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;

    @PostConstruct
    public void init() throws BeansException {

        addConverter(new IpAddressGroupToString());

        addConverter(new StringToModel());
        addConverter(new ModelToString());

        addConverter(new StringToIpAddress());
        addConverter(new IpAddressToString());

        addConverter(new LocalTimeToString());
        addConverter(new StringToLocalTime());

        addConverter(new FirmwareConfigToString());
        addConverter(new StringToFirmwareConfig());

        addConverter(new LocalDateTimeToString());
        addConverter(new StringToLocalDateTime());
    }

    public static class ModelToString implements
            Converter<Model, String> {
        @Override
        public String convert(Model m) {
            return m == null ? null : m.getId();
        }
    }

    public static class StringToModel implements
            Converter<String, Model> {
        @Override
        public Model convert(String id) {
            if (StringUtils.isBlank(id)) {
                return null;
            }
            Model m = new Model();
            m.setId(id);
            return m;
        }
    }

    public static class LocalDateTimeToString implements
            Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime g) {
            return g == null ? null : g.toString("M/d/yyyy H:mm");
        }
    }

    public static class StringToLocalDateTime implements
            Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String id) {
            if (StringUtils.isBlank(id)) {
                return null;
            }
            return LocalDateTime.parse(id,
                    DateTimeFormat.forPattern("M/d/yyyy H:mm"));
        }
    }

    public static class FirmwareConfigToString implements
            Converter<FirmwareConfig, String> {
        @Override
        public String convert(FirmwareConfig g) {
            return g == null ? null : g.getId();
        }
    }

    public class StringToFirmwareConfig implements
            Converter<String, FirmwareConfig> {
        @Override
        public FirmwareConfig convert(String id) {
            if (StringUtils.isBlank(id)) {
                return null;
            }

            return firmwareConfigDAO.getOne(id);
        }
    }

    public static class IpAddressToString implements Converter<IpAddress, String> {
        @Override
        public String convert(IpAddress g) {
            return g == null ? null : g.toString();
        }
    }

    public static class StringToIpAddress implements Converter<String, IpAddress> {
        @Override
        public IpAddress convert(String s) {
            if (StringUtils.isBlank(s)) {
                return null;
            }
            return new IpAddress(s);
        }
    }

    public static class IpAddressGroupToString implements
            Converter<IpAddressGroup, String> {
        @Override
        public String convert(IpAddressGroup g) {
            return g.getId(); //TODO: some stange id format
        }
    }

    private static class LocalTimeToString implements
            Converter<LocalTime, String> {
        @Override
        public String convert(LocalTime t) {
            if (t == null) {
                return null;
            } else {
                return t.toString("HH:mm");
            }
        }
    }

    private static class StringToLocalTime implements
            Converter<String, LocalTime> {
        @Override
        public LocalTime convert(String s) {
            if (s == null) {
                return null;
            } else {
                String[] sa = s.split(":");
                return new LocalTime(Integer.parseInt(sa[0].trim()),
                        Integer.parseInt(sa[1].trim()), 0);
            }
        }
    }

}

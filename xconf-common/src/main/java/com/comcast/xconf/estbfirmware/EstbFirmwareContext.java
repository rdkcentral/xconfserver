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
 * Author: slavrenyuk
 * Created: 7/8/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.xconf.Environment;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.logupload.TimeZoneUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.*;

/**
 * Models the request from the eSTB. We need to handle anything in the request
 * or input from STB so it's all strings, but when evaluating rules we need more
 * strong typing, thus the getInput(). Then you can do stuff like controllerId >
 * 234 or ipAddress.isInRange('31.24.122.4/22').
 */
public class EstbFirmwareContext {

    private static final Logger log = LoggerFactory.getLogger(EstbFirmwareContext.class);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("M/d/yyyy H:mm");

    private Converted converted;

    private MultiValueMap<String, String> context;

    public EstbFirmwareContext() {
        context = new LinkedMultiValueMap<>();
    }

    public EstbFirmwareContext(MultiValueMap<String, String> context) {
        this.context = context;
    }

    public final Converted convert() {
        if (converted == null) {
            converted = getConverted();
        }

        return converted;
    }

    public MultiValueMap<String, String> getContext() {
        return context;
    }

    public void setContext(MultiValueMap<String, String> context) {
        this.context = context;
    }

    private Converted getConverted() {

        Converted c = new Converted();

        /**
         * Requests with invalid mac addresses are junk, we don't care about
         * them, we just return 500 error and don't even log it.
         */
        c.setEstbMac(new MacAddress(geteStbMac()));

        c.setEnv(getEnv());

        c.setModel(getModel());

        c.setAccountId(getAccountId());

        c.firmwareVersion = getFirmwareVersion();

        String ecmMac = geteCMMac();
        if (ecmMac != null && MacAddress.isValid(ecmMac)) {
            c.ecmMac = new MacAddress(ecmMac);
        }

        c.receiverId = getReceiverId();

        if (NumberUtils.isDigits(getControllerId())) {
            c.controllerId = Long.valueOf(getControllerId());
        }

        if (NumberUtils.isDigits(getChannelMapId())) {
            c.channelMapId = Long.valueOf(getChannelMapId());
        }

        if (NumberUtils.isDigits(getVodId())) {
            c.vodId = Long.valueOf(getVodId());
        }

        c.timeZone = offsetToTimeZone(getTimeZoneOffset());

        c.setTime(getTime());

        c.ipAddress = new IpAddress(getIpAddress());

        if (getCapabilities() != null) {

            c.capabilities = createCapabilitiesList();
        }

        if (getBypassFilters() != null) {
            addFiltersIntoConverted(getBypassFilters(), c.bypassFilters);
        }
        if (getForceFilters() != null) {
            addFiltersIntoConverted(getForceFilters(), c.forceFilters);
        }
        c.setXconfHttpHeader(getXconfHttpHeader());
        return c;
    }

    private void addFiltersIntoConverted(String filterStr, Collection<String> filters) {
        String[] split = filterStr.trim().split("[,]");
        for (String f : split) {
            filters.add(f.trim());
        }
    }

    private List<Capabilities> createCapabilitiesList() {
        List<Capabilities> list = new ArrayList<Capabilities>();
        for (String capability : getCapabilities()) {
            try {
                list.add(Capabilities.valueOf(capability));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown capability will be ignored: " + capability);
            }
        }
        return list;
    }

    public static DateTimeZone offsetToTimeZone(String offset) {
        if (StringUtils.isNotBlank(offset)) {
            String[] aa = offset.split(":");
            if (aa.length == 2) {
                if (NumberUtils.isNumber(aa[0]) && NumberUtils.isDigits(aa[1])) {
                    try {
                        return DateTimeZone.forOffsetHoursMinutes(
                                Integer.parseInt(aa[0]),
                                Integer.parseInt(aa[1]));
                    } catch (Exception e) {
                        return DateTimeZone.UTC;
                    }
                }
            }
        }
        return DateTimeZone.UTC;
    }

    /**
     * Typed class for rules expression evaluation also with property names
     * changed a bit to adhere to JavaBean naming convention.
     */
    public static final class Converted {

        private MacAddress estbMac;
        private String env;
        private String model;
        private String firmwareVersion;
        private MacAddress ecmMac;
        private String receiverId;
        private Long controllerId;
        private Long channelMapId;
        private Long vodId;
        private Set<String> bypassFilters = new HashSet<>();
        public Set<String> forceFilters = new HashSet<>();
        public String xconfHttpHeader;
        public String accountId;

        private List<Capabilities> capabilities = new ArrayList<Capabilities>();

        @JsonSerialize(using = TimeZoneSerializer.class)
        @JsonDeserialize(using = TimeZoneDeserializer.class)
        private DateTimeZone timeZone = DateTimeZone.UTC;

        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        private LocalDateTime time;
        private IpAddress ipAddress;

        public boolean isRcdl() {
            return capabilities != null && capabilities.contains(Capabilities.RCDL);
        }

        public boolean isRebootDecoupled() {
            return capabilities != null && capabilities.contains(Capabilities.rebootDecoupled);
        }

        public boolean isSupportsFullHttpUrl() {
            return capabilities != null && capabilities.contains(Capabilities.supportsFullHttpUrl);
        }

        public MacAddress getEstbMac() {
            return estbMac;
        }

        public void setEstbMac(MacAddress estbMac) {
            this.estbMac = estbMac;
        }

        public String getEnv() {
            return env;
        }

        public void setEnv(String env) {
            if (env != null) {
                this.env = new Environment(env, "").getId();
            }
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            if (model != null) {
                this.model = new Model(model, "").getId();
            }
        }

        public String getFirmwareVersion() {
            return firmwareVersion;
        }

        public void setFirmwareVersion(String firmwareVersion) {
            this.firmwareVersion = firmwareVersion;
        }

        public MacAddress getEcmMac() {
            return ecmMac;
        }

        public void setEcmMac(MacAddress ecmMac) {
            this.ecmMac = ecmMac;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }

        public Long getControllerId() {
            return controllerId;
        }

        public void setControllerId(Long controllerId) {
            this.controllerId = controllerId;
        }

        public Long getChannelMapId() {
            return channelMapId;
        }

        public void setChannelMapId(Long channelMapId) {
            this.channelMapId = channelMapId;
        }

        public Long getVodId() {
            return vodId;
        }

        public void setVodId(Long vodId) {
            this.vodId = vodId;
        }

        public String getXconfHttpHeader() {
            return xconfHttpHeader;
        }

        public void setXconfHttpHeader(String xconfHttpHeader) {
            this.xconfHttpHeader = xconfHttpHeader;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this,
                    ToStringStyle.MULTI_LINE_STYLE);
        }

        /**
         * This value will always be non-null, it is derived as follows.
         * <p>
         * If "time" parameter was sent in query string, this value will be that
         * value. No time zone offset will be applied.
         * <p>
         * If "time" parameter was not sent in query string, this value will be
         * current UTC time plus time zone offset if specified.
         * <p>
         */
        public LocalDateTime getTime() {
            return time;
        }

        /**
         * WARNING: time zone must be set before time.
         */
        public void setTime(LocalDateTime time) {
            if (time == null) {
                time = new LocalDateTime(timeZone);
            }
            this.time = time;
        }

        public IpAddress getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(IpAddress ipAddress) {
            this.ipAddress = ipAddress;
        }

        /**
         * This value will never null. It is derived as follows.
         * <p>
         * If a timeZoneOffset was sent, we use that offset to construct
         * timeZone.
         * <p>
         * If no timeZoneOffset was sent (or if it was invalid), we set timeZone
         * to utc.
         * <p>
         * If timeZone is UTC, we use the OLD and soon to be deprecated IP
         * Address + UTC time blocking filter. If timeZone is anything other
         * than UTC, we use the new local time based blocking filter. Once boot
         * blocking and download scheduling are both fixed, both time based
         * blocking filters will be deprecated.
         */
        public DateTimeZone getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(DateTimeZone timeZone) {
            this.timeZone = timeZone;
        }

        @JsonIgnore
        public boolean isUTC() {
            return DateTimeZone.UTC.equals(getTimeZone());
        }

        public List<Capabilities> getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(List<Capabilities> capabilities) {
            this.capabilities = capabilities;
        }

        public Set<String> getBypassFilters() {
            return bypassFilters;
        }

        public void setBypassFilters(Set<String> bypassFilters) {
            this.bypassFilters = bypassFilters;
        }

        public Set<String> getForceFilters() {
            return forceFilters;
        }

        public void setForceFilters(Set<String> forceFilters) {
            this.forceFilters = forceFilters;
        }

        public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

            public LocalDateTimeSerializer() {
            }

            @Override
            public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException,
                    JsonProcessingException {
                jgen.writeString(value.toString("MM/dd/yyyy HH:mm:ss"));
            }
        }

        public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

            public LocalDateTimeDeserializer() {
            }

            @Override
            public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException,
                    JsonProcessingException {
                return DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss")
                        .parseLocalDateTime(jp.getText());
            }
        }

        public static class TimeZoneSerializer extends JsonSerializer<DateTimeZone> {
            @Override
            public void serialize(DateTimeZone val, JsonGenerator jg,
                                  SerializerProvider sp) throws IOException,
                    JsonProcessingException {
                jg.writeString(val.toString());
            }
        }

        public static class TimeZoneDeserializer extends JsonDeserializer<DateTimeZone> {
            @Override
            public DateTimeZone deserialize(JsonParser jp,
                                            DeserializationContext ctx) throws IOException,
                    JsonProcessingException {
                return TimeZoneUtils.parseDateTimeZone(jp.getText());
            }
        }
    }

    public String geteStbMac() {
        return context.getFirst(StbContext.ESTB_MAC);
    }

    public void seteStbMac(String eStbMac) {
        context.set(StbContext.ESTB_MAC, eStbMac);
    }

    public String getEnv() {
        return context.getFirst(StbContext.ENVIRONMENT);
    }

    public void setEnv(String env) {
        context.set(StbContext.ENVIRONMENT, env);
    }

    public String getModel() {
        return context.getFirst(StbContext.MODEL);
    }

    public void setModel(String model) {
        context.set(StbContext.MODEL, model);
    }

    public String getFirmwareVersion() {
        return context.getFirst(StbContext.FIRMWARE_VERSION);
    }

    public void setFirmwareVersion(String firmwareVersion) {
        context.set(StbContext.FIRMWARE_VERSION, firmwareVersion);
    }

    public String geteCMMac() {
        return context.getFirst(StbContext.ECM_MAC);
    }

    public void seteCMMac(String eCMMac) {
        context.set(StbContext.ECM_MAC, eCMMac);
    }

    public String getReceiverId() {
        return context.getFirst(StbContext.RECEIVER_ID);
    }

    public void setReceiverId(String receiverId) {
        context.set(StbContext.RECEIVER_ID, receiverId);
    }

    public String getControllerId() {
        return context.getFirst(StbContext.CONTROLLER_ID);
    }

    public void setControllerId(String controllerId) {
        context.set(StbContext.CONTROLLER_ID, controllerId);
    }

    public String getChannelMapId() {
        return context.getFirst(StbContext.CHANNEL_MAP);
    }

    public void setChannelMapId(String channelMapId) {
        context.set(StbContext.CHANNEL_MAP, channelMapId);
    }

    public String getVodId() {
        return context.getFirst(StbContext.VOD_ID);
    }

    public void setVodId(String vodId) {
        context.set(StbContext.VOD_ID, vodId);
    }

    public String getAccountHash() {
        return context.getFirst(StbContext.ACCOUNT_HASH);
    }

    public void setAccountHash(String accountHash) {
        context.set(StbContext.ACCOUNT_HASH, accountHash);
    }

    public String getXconfHttpHeader() {
        return context.getFirst(StbContext.XCONF_HTTP_HEADER);
    }

    public void setXconfHttpHeader(String xconfHttpHeader) {
        context.set(StbContext.XCONF_HTTP_HEADER, xconfHttpHeader);
    }

    /**
     * This is an optional parameter used mostly for testing to override actual
     * local time. This is always LOCAL time. We do NOT apply time zone offset
     * to this value. If time zone offset is sent, it is assumed to have already
     * been applied to this time.
     */
    public LocalDateTime getTime() {
        String time = context.getFirst(StbContext.TIME);
        return StringUtils.isNotBlank(time) ? LocalDateTime.parse(time, DATE_TIME_FORMATTER) : null;
    }

    public void setTime(LocalDateTime time) {
        context.set(StbContext.TIME, time.toString(DATE_TIME_FORMATTER));
    }

    public String getIpAddress() {
        return context.getFirst(StbContext.IP_ADDRESS);
    }

    public void setIpAddress(String ipAddress) {
        context.set(StbContext.IP_ADDRESS, ipAddress);
    }

    public String getBypassFilters() {
        return context.getFirst(StbContext.BYPASS_FILTERS);
    }

    public void setBypassFilters(String bypassFilters) {
        context.set(StbContext.BYPASS_FILTERS, bypassFilters);
    }

    public String getForceFilters() {
        return context.getFirst(StbContext.FORCE_FILTERS);
    }

    public void setForceFilters(String forceFilters) {
        context.set(StbContext.FORCE_FILTERS, forceFilters);
    }

    /**
     * Tells us the STB offset from UTC
     * http://joda-time.sourceforge.net/timezones.html Will be a string like
     * "-04:00". From this we can derive the SBT local time.
     * <p>
     * The normal case will be that "time" parameter is NOT sent and
     * "timeZoneOffset" parameter IS specified. In this case we will derive STB
     * local time from current UTC plus this offset.
     * <p>
     * For testing "time" parameter may be set. If it is set, it is assumed to
     * be local time, we do not apply time zone offset to it.
     */
    public String getTimeZoneOffset() {
        return context.getFirst(StbContext.TIME_ZONE_OFFSET);
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        context.set(StbContext.TIME_ZONE_OFFSET, timeZoneOffset);
    }

    public List<String> getCapabilities() {
        return context.get(StbContext.CAPABILITIES);
    }

    public void setCapabilities(List<String> capabilities) {
        context.put(StbContext.CAPABILITIES, capabilities);
    }

    public String getPartnerId() {
        return context.getFirst(StbContext.PARTNER_ID);
    }

    public void setPartnerId(String partnerId) {
        context.set(StbContext.PARTNER_ID, partnerId);
    }

    public String getAccountId() {
        return context.getFirst(StbContext.ACCOUNT_ID);
    }

    public void setAccountId(String accountId) {
        context.set(StbContext.ACCOUNT_ID, accountId);
    }

    public Map<String, String> getProperties() {
        Map<String, String> map = context.toSingleValueMap();

        List<String> capabilities = getCapabilities();
        if (capabilities != null) {
            for (String capability : capabilities) {
                map.put(capability, "");
            }
        }

        DateTimeZone timeZone = offsetToTimeZone(getTimeZoneOffset());
        map.put("timeZone", timeZone.toString());

        LocalDateTime time = getTime();
        String timeStr = (time != null) ? time.toString() : new LocalDateTime(timeZone).toString();
        map.put("time", timeStr);

        return map;
    }

    @Override
    public String toString() {
        return new StringBuilder("estbMac=").append(geteStbMac())
                .append(" env=").append(getEnv())
                .append(" model=").append(getModel())
                .append(" reportedFirmwareVersion=").append(getFirmwareVersion())
                .append(" ecmMac=").append(geteCMMac())
                .append(" receiverId=").append(getReceiverId())
                .append(" controllerId=").append(getControllerId())
                .append(" channelMapId=").append(getChannelMapId())
                .append(" vodId=").append(getVodId())
                .append(" partnerId=").append(getPartnerId())
                .append(" accountId=").append(getAccountId())
                .append(" capabilities=").append(getCapabilities())
                .append(" timeZone=").append(getTimeZoneOffset())
                .append(" time=\"").append(getTime()).append("\"")
                .append(" ipAddress=").append(getIpAddress())
                .append(" bypassFilters=").append(getBypassFilters())
                .append(" forceFilters=").append(getForceFilters())
                .toString();
    }

    public String toLogString() {
        return toString();
    }
}

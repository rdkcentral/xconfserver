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
 * Created: 2/22/2016
*/
package com.comcast.xconf.estbfirmware;

import com.comcast.xconf.ConfigNames;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonSerialize(using = FirmwareConfigFacade.Serializer.class)
@JsonDeserialize(using = FirmwareConfigFacade.Deserializer.class)
public class FirmwareConfigFacade {

    private Map<String, Object> properties = new LinkedHashMap<>();

    public FirmwareConfigFacade() {}

    public FirmwareConfigFacade(FirmwareConfig firmwareConfig) {
        Map<String, Object> map = new LinkedHashMap<>();
        putIfPresent(map, ConfigNames.ID, firmwareConfig.getId());
        putIfPresent(map, ConfigNames.UPDATED, firmwareConfig.getUpdated());
        putIfPresent(map, ConfigNames.DESCRIPTION, firmwareConfig.getDescription());
        putIfPresent(map, ConfigNames.SUPPORTED_MODEL_IDS, firmwareConfig.getSupportedModelIds());
        putIfPresent(map, ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, firmwareConfig.getFirmwareDownloadProtocol());
        putIfPresent(map, ConfigNames.FIRMWARE_FILENAME, firmwareConfig.getFirmwareFilename());
        putIfPresent(map, ConfigNames.FIRMWARE_LOCATION, firmwareConfig.getFirmwareLocation());
        putIfPresent(map, ConfigNames.FIRMWARE_VERSION, firmwareConfig.getFirmwareVersion());
        putIfPresent(map, ConfigNames.IPV6_FIRMWARE_LOCATION, firmwareConfig.getIpv6FirmwareLocation());
        putIfPresent(map, ConfigNames.UPGRADE_DELAY, firmwareConfig.getUpgradeDelay());
        putIfPresent(map, ConfigNames.REBOOT_IMMEDIATELY, firmwareConfig.getRebootImmediately());
        properties = map;
    }

    public void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (!isEmpty(value)) {
            map.put(key, value);
        }
    }

    private boolean isEmpty(Object value) {
        return (value == null) || (value instanceof String && StringUtils.isBlank((String) value));
    }

    public FirmwareConfigFacade(Map<String, Object> properties) {
        this.properties.putAll(properties);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public void putAll(Map<String, Object> map) {
        properties.putAll(map);
    }

    public String getFirmwareFilename() {
        return (String) properties.get(ConfigNames.FIRMWARE_FILENAME);
    }

    public String getFirmwareVersion() {
        return (String) properties.get(ConfigNames.FIRMWARE_VERSION);
    }

    public String getFirmwareDownloadProtocol() {
        Object protocol = properties.get(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL);
        return protocol != null ? protocol.toString() : null;
    }

    public void setFirmwareDownloadProtocol(FirmwareConfig.DownloadProtocol protocol) {
        properties.put(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, protocol);
    }

    public String getIpv6FirmwareLocation() {
        return (String) properties.get(ConfigNames.IPV6_FIRMWARE_LOCATION);
    }

    public void setIpv6FirmwareLocation(String location) {
        properties.put(ConfigNames.IPV6_FIRMWARE_LOCATION, location);
    }

    public void setFirmwareLocation(String location) {
        properties.put(ConfigNames.FIRMWARE_LOCATION, location);
    }

    public String getFirmwareLocation() {
        return (String) properties.get(ConfigNames.FIRMWARE_LOCATION);
    }

    public void setRebootImmediately(boolean flag) {
        properties.put(ConfigNames.REBOOT_IMMEDIATELY, flag);
    }

    public Long getUpgradeDelay() {
        Object delay = properties.get(ConfigNames.UPGRADE_DELAY);
        return delay != null && delay instanceof Long ? (Long) delay : null;
    }

    public Boolean getRebootImmediately() {
        Object flag = properties.get(ConfigNames.REBOOT_IMMEDIATELY);
        return flag != null && flag instanceof Boolean ? (Boolean) flag : false;
    }

    /**
     * Will exclude from response fields, like id, description, supportedModelIds, updated.
     * And also empty values and blank strings.
     */
    public void removeRedundantValues() {
        properties.remove(ConfigNames.ID);
        properties.remove(ConfigNames.DESCRIPTION);
        properties.remove(ConfigNames.SUPPORTED_MODEL_IDS);
        properties.remove(ConfigNames.UPDATED);

        Iterator<String> keyIterator = properties.keySet().iterator();
        while (keyIterator.hasNext()) {
            Object value = properties.get(keyIterator.next());
            if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
                keyIterator.remove();
            }
        }
    }

    /**
     * The order is important for STB team as they read json response via bash commands in upgrade script.
     * Will exclude from response fields, like id, description, supportedModelIds, updated.
     * And also empty values and blank strings.
     * @return Firmware Config with ordered fields
     */
    public FirmwareConfigFacade createResponseEntity() {
        Map<String, Object> map = new LinkedHashMap<>();

        putIfPresent(map, ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL, getFirmwareDownloadProtocol());
        putIfPresent(map, ConfigNames.FIRMWARE_FILENAME, getFirmwareFilename());
        putIfPresent(map, ConfigNames.FIRMWARE_LOCATION, getFirmwareLocation());
        putIfPresent(map, ConfigNames.FIRMWARE_VERSION, getFirmwareVersion());
        putIfPresent(map, ConfigNames.IPV6_FIRMWARE_LOCATION, getIpv6FirmwareLocation());
        putIfPresent(map, ConfigNames.UPGRADE_DELAY, getUpgradeDelay());
        putIfPresent(map, ConfigNames.REBOOT_IMMEDIATELY, getRebootImmediately());

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!isRedundantEntry(entry)) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        return new FirmwareConfigFacade(map);
    }

    public boolean isRedundantEntry(Map.Entry<String, Object> entry) {
        String key = entry.getKey();
        return ConfigNames.ID.equals(key) || ConfigNames.DESCRIPTION.equals(key)
                || ConfigNames.SUPPORTED_MODEL_IDS.equals(key) || ConfigNames.UPDATED.equals(key)
                || isEmpty(entry.getValue());
    }

    @Override
    public String toString() {
        Map<String, Object> map = new LinkedHashMap<>(properties);
        StringBuilder builder = new StringBuilder("FirmwareConfig[");

        // preserve order from FirmwareConfig.toString method
        appendAndRemove(builder, map, ConfigNames.DESCRIPTION);
        appendAndRemove(builder, map, ConfigNames.SUPPORTED_MODEL_IDS);
        appendAndRemove(builder, map, ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL);
        appendAndRemove(builder, map, ConfigNames.FIRMWARE_FILENAME);
        appendAndRemove(builder, map, ConfigNames.FIRMWARE_LOCATION);
        appendAndRemove(builder, map, ConfigNames.FIRMWARE_VERSION);
        appendAndRemove(builder, map, ConfigNames.IPV6_FIRMWARE_LOCATION);
        appendAndRemove(builder, map, ConfigNames.UPGRADE_DELAY);
        appendAndRemove(builder, map, ConfigNames.REBOOT_IMMEDIATELY);

        // exclude updated field
        map.remove(ConfigNames.UPDATED);

        // write what is left
        for (String key : map.keySet()) {
            append(builder, key, map.get(key));
        }
        return builder.append("]").toString();
    }

    private void appendAndRemove(StringBuilder builder, Map<String, Object> map, String key) {
        Object value = map.get(key);
        String valueStr;
        if (value != null) {
            valueStr = value.toString();
            map.remove(key);
        } else {
            valueStr = "<null>";
        }
        append(builder, key, valueStr);
    }

    private void append(StringBuilder builder, String key, Object value) {
        builder.append(SystemUtils.LINE_SEPARATOR).append("  ").append(key).append("=").append(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FirmwareConfigFacade that = (FirmwareConfigFacade) o;

        return !(properties != null ? !properties.equals(that.properties) : that.properties != null);
    }

    @Override
    public int hashCode() {
        return properties != null ? properties.hashCode() : 0;
    }

    public static class Serializer extends JsonSerializer<FirmwareConfigFacade> {
        @Override
        public void serialize(FirmwareConfigFacade val, JsonGenerator jg,
                              SerializerProvider sp) throws IOException {
            jg.writeObject(val.getProperties());
        }
    }

    public static class Deserializer extends JsonDeserializer<FirmwareConfigFacade> {
        @Override
        public FirmwareConfigFacade deserialize(JsonParser jp,
                                        DeserializationContext ctx) throws IOException {
            LinkedHashMap<String, Object> map = jp.readValueAs(new TypeReference<LinkedHashMap<String, Object>>() {
            });
            return new FirmwareConfigFacade(map);
        }
    }
}

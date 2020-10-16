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
package com.comcast.xconf.logupload;

import com.comcast.xconf.StbContext;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LogUploaderContext {

    public static final String ESTB_IP = "estbIP";
    public static final String ESTB_MAC = "estbMacAddress";
    public static final String ECM_MAC = "ecmMacAddress";
    public static final String ENV = "env";
    public static final String MODEL = "model";
    public static final String ACCOUNT_MGMT = "accountMgmt";
    public static final String SERIAL_NUM = "serialNum";
    public static final String PARTNER_ID = "partnerId";
    public static final String FIRMWARE_VERSION = "firmwareVersion";
    public static final String CONTROLLER_ID = "controllerId";
    public static final String CHANNEL_MAP_ID = "channelMapId";
    public static final String VOD_ID = "vodId";
    public static final String UPLOAD_IMMEDIATELY = "uploadImmediately";
    public static final String TIME_ZONE = "timezone";
    public static final String APPLICATION = "applicationType";
    public static final String ACCOUNT_HASH = "accountHash";
    public static final String ACCOUNT_ID = "accountId";
    public static final String CONFIG_SET_HASH = "configSetHash";

    private Map<String, String> context;

    public LogUploaderContext() {
        this.context = new HashMap<>();
    }

    public LogUploaderContext(final Map<String, String> context) {
        this.context = context;
    }

    public String getEstbIP() {
        return context.get(ESTB_IP);
    }

    public void setEstbIP(String estbIP) {
        context.put(ESTB_IP, estbIP);
    }

    public String getEstbMacAddress() {
        return context.get(ESTB_MAC);
    }

    public void setEstbMacAddress(String estbMacAddress) {
        context.put(ESTB_MAC, estbMacAddress);
    }

    public String getEcmMacAddress() {
        return context.get(ECM_MAC);
    }

    public void setEcmMacAddress(String ecmMacAddress) {
        context.put(ECM_MAC, ecmMacAddress);
    }

    public String getEnv() {
        return context.get(ENV);
    }

    public void setEnv(String env) {
        context.put(ENV, env);
    }

    public String getModel() {
        return context.get(StbContext.MODEL);
    }

    public void setModel(String model) {
        context.put(StbContext.MODEL, model);
    }

    public String getFirmwareVersion() {
        return context.get(StbContext.FIRMWARE_VERSION);
    }

    public void setFirmwareVersion(String firmwareVersion) {
        context.put(StbContext.FIRMWARE_VERSION, firmwareVersion);
    }

    public String getControllerId() {
        return context.get(StbContext.CONTROLLER_ID);
    }

    public void setControllerId(String controllerId) {
        context.put(StbContext.CONTROLLER_ID, controllerId);
    }

    public String getChannelMapId() {
        return context.get(StbContext.CHANNEL_MAP);
    }

    public void setChannelMapId(String channelMapId) {
        context.put(StbContext.CHANNEL_MAP, channelMapId);
    }

    public String getVodId() {
        return context.get(StbContext.VOD_ID);
    }

    public void setVodId(String vodId) {
        context.put(StbContext.VOD_ID, vodId);
    }

    public boolean getUploadImmediately() {
        return Boolean.valueOf(context.get(UPLOAD_IMMEDIATELY));
    }

    public void setUploadImmediately(boolean uploadImmediately) {
        context.put(UPLOAD_IMMEDIATELY, String.valueOf(uploadImmediately));
    }

    public String getTimeZone() {
        return context.get(TIME_ZONE);
    }

    public void setTimeZone(String timeZone) {
        context.put(TIME_ZONE, timeZone);
    }

    public String getApplication() {
        return context.get(APPLICATION);
    }

    public void setApplication(String application) {
        context.put(APPLICATION, application);
    }

    public String getPartnerId() {
        return context.get(PARTNER_ID);
    }

    public void setPartnerId(String partner) {
        context.put(PARTNER_ID, partner);
    }

    public String getAccountHash() {
        return context.get(ACCOUNT_HASH);
    }

    public void setAccountHash(String accountHash) {
        context.put(ACCOUNT_HASH, accountHash);
    }

    public String getAccountId() {
        return context.get(ACCOUNT_ID);
    }

    public void setAccountId(String accountId) {
        context.put(ACCOUNT_ID, accountId);
    }

    public void setConfigSetHash(String configSetHash) {
        context.put(CONFIG_SET_HASH, configSetHash);
    }

    public String getConfigSetHash() {
        return context.get(CONFIG_SET_HASH);
    }

    public String toLogString() {
        final StringBuilder sb = new StringBuilder("LogUploaderContext{");
        final Iterator<Map.Entry<String, String>> iterator = context.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey()).append("='").append(entry.getValue()).append('\'');
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    @JsonIgnore
    public Map<String, String> getProperties() {
        return context;
    }
}

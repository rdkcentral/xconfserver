/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.hydra.astyanax.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

import static com.comcast.apps.dataaccess.util.PropertyValidationUtil.validateProperty;

/**
 * Defines Xconf DataService specific section
 * User: ikostrov
 * Date: 16.10.14
 */
@Configuration
@PropertySource(
        value = {"file:${appConfig}"}, ignoreResourceNotFound = true)
public class XconfSpecificConfig {

    @Value("${specific.authKey:}")
    private String authKey;

    @Value("${specific.maxConnections:2000}")
    private Integer maxConnections;

    @Value("${specific.maxConnectionsPerRoute:2000}")
    private Integer maxConnectionsPerRoute;

    @Value("${specific.requestTimeoutInMs:5000}")
    private Integer requestTimeoutInMs;

    @Value("${specific.connectionTimeoutInMs:1000}")
    private Integer connectionTimeoutInMs;

    @Value("${specific.socketTimeoutInMs:5000}")
    private Integer socketTimeoutInMs;

    @Value("${specific.haProxyHeaderName:'HA-Forwarded-For'}")
    private String haProxyHeaderName;

    @Value("${specific.enableUpdateDeleteAPI:true}")
    private boolean enableUpdateDeleteAPI; // enabled by default for legacy purposes

    @Value("${specific.recoveryFirmwareVersions:}")
    private String recoveryFirmwareVersions;

    @Value("${specific.allowedNumberOfFeatures:20}")
    private Integer allowedNumberOfFeatures;

    @PostConstruct
    public void init() {
        validateProperties();
    }

    public void validateProperties() {
        validateProperty(authKey, "specific.authKey");
        validateProperty(maxConnections, "specific.maxConnections");
        validateProperty(maxConnectionsPerRoute, "specific.maxConnectionsPerRoute");
        validateProperty(requestTimeoutInMs, "specific.requestTimeoutInMs");
        validateProperty(connectionTimeoutInMs, "specific.connectionTimeoutInMs");
        validateProperty(socketTimeoutInMs, "specific.socketTimeoutInMs");
        validateProperty(haProxyHeaderName, "specific.haProxyHeaderName");
        validateProperty(recoveryFirmwareVersions, "specific.recoveryFirmwareVersions");
        validateProperty(allowedNumberOfFeatures, "specific.allowedNumberOfFeatures");
    }

    public String getHaProxyHeaderName() {
        return haProxyHeaderName;
    }

    public void setHaProxyHeaderName(String haProxyHeaderName) {
        this.haProxyHeaderName = haProxyHeaderName;
    }

    public boolean isEnableUpdateDeleteAPI() {
        return enableUpdateDeleteAPI;
    }

    public void setEnableUpdateDeleteAPI(boolean enableUpdateDeleteAPI) {
        this.enableUpdateDeleteAPI = enableUpdateDeleteAPI;
    }

    public String getRecoveryFirmwareVersions() {
        return recoveryFirmwareVersions;
    }

    public Integer getAllowedNumberOfFeatures() {
        return allowedNumberOfFeatures;
    }

    public void setAllowedNumberOfFeatures(Integer allowedNumberOfFeatures) {
        this.allowedNumberOfFeatures = allowedNumberOfFeatures;
    }

    public void setRecoveryFirmwareVersions(String recoveryFirmwareVersions) {
        this.recoveryFirmwareVersions = recoveryFirmwareVersions;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Integer getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }

    public Integer getRequestTimeoutInMs() {
        return requestTimeoutInMs;
    }

    public void setRequestTimeoutInMs(Integer requestTimeoutInMs) {
        this.requestTimeoutInMs = requestTimeoutInMs;
    }

    public Integer getConnectionTimeoutInMs() {
        return connectionTimeoutInMs;
    }

    public void setConnectionTimeoutInMs(Integer connectionTimeoutInMs) {
        this.connectionTimeoutInMs = connectionTimeoutInMs;
    }

    public Integer getSocketTimeoutInMs() {
        return socketTimeoutInMs;
    }

    public void setSocketTimeoutInMs(Integer socketTimeoutInMs) {
        this.socketTimeoutInMs = socketTimeoutInMs;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("XconfSpecificConfig{");
        sb.append(", authKey='").append(authKey).append('\'');
        sb.append(", maxConnections=").append(maxConnections);
        sb.append(", maxConnectionsPerRoute=").append(maxConnectionsPerRoute);
        sb.append(", requestTimeoutInMs=").append(requestTimeoutInMs);
        sb.append(", connectionTimeoutInMs=").append(connectionTimeoutInMs);
        sb.append(", socketTimeoutInMs=").append(socketTimeoutInMs);
        sb.append(", haProxyHeaderName='").append(haProxyHeaderName).append('\'');
        sb.append(", enableUpdateDeleteAPI=").append(enableUpdateDeleteAPI);
        sb.append(", recoveryFirmwareVersions=").append(recoveryFirmwareVersions);
        sb.append(", allowedNumberOfFeatures=").append(allowedNumberOfFeatures);
        sb.append('}');
        return sb.toString();
    }
}

/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2019 RDK Management
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
 * @author Roman Dolomansky (rdolomansky@productengine.com)
 */
package com.comcast.apps.dataaccess.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.comcast.apps.dataaccess.util.PropertyValidationUtil.validateProperty;


@Component
@PropertySource(
        value = {"classpath:service.properties", "file:${appConfig}"}, ignoreResourceNotFound = true)
public class CassandraSettings {

    @Value("${cassandra.keyspaceName}")
    private String keyspaceName;

    @Value("#{'${cassandra.contactPoints}'.split(',')}")
    private String[] contactPoints;

    @Value("${cassandra.port}")
    private int port;

    @Value("${cassandra.authKey}")
    private String authKey;

    @Value("${cassandra.username}")
    private String username;

    @Value("${cassandra.password}")
    private String password;

    @Value("${cassandra.consistencyLevel:ONE}")
    private String consistencyLevel;

    @Value("${cassandra.localDataCenter:#{null}}")
    private String localDataCenter;

    public CassandraSettings() {}

    public CassandraSettings(final CassandraSettings settings) {
        keyspaceName = settings.keyspaceName;
        contactPoints = settings.contactPoints;
        port = settings.port;
        username = settings.username;
        password = settings.password;
        consistencyLevel = settings.consistencyLevel;
    }

    @PostConstruct
    public void validateProperties() {
        validateProperty(keyspaceName, "cassandra.keyspaceName");
        validateProperty(contactPoints, "cassandra.contactPoints");
        validateProperty(port, "cassandra.port");
        validateProperty(authKey, "cassandra.authKey");
        validateProperty(username, "cassandra.username");
        validateProperty(password, "cassandra.password");
        validateProperty(consistencyLevel, "cassandra.consistencyLevel");
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    public String[] getContactPoints() {
        return contactPoints;
    }

    public void setContactPoints(String[] contactPoints) {
        this.contactPoints = contactPoints;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    public String getLocalDataCenter() {
        return localDataCenter;
    }

    public void setLocalDataCenter(String localDataCenter) {
        this.localDataCenter = localDataCenter;
    }
}

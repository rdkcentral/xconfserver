/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2021 RDK Management
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
 * @author Maksym Dolina (mdolina@productengine.com)
 */
package com.comcast.apps.dataaccess.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@PropertySource(
        value = {"classpath:service.properties", "file:${appConfig}"}, ignoreResourceNotFound = true)
public class SslSettings {

    public static final String SSL = "SSL";
    public static final String JKS = "JKS";

    @Value("${ssl.truststore.path}")
    private String truststorePath;

    @Value("${ssl.truststore.password}")
    private String truststorePassword;

    @Value("${ssl.keystore.path}")
    private String keystorePath;

    @Value("${ssl.keystore.password}")
    private String keystorePassword;

    public String getTruststorePath() {
        return truststorePath;
    }

    public void setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public void setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SslSettings that = (SslSettings) o;
        return Objects.equals(truststorePath, that.truststorePath) && Objects.equals(truststorePassword, that.truststorePassword) && Objects.equals(keystorePath, that.keystorePath) && Objects.equals(keystorePassword, that.keystorePassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(truststorePath, truststorePassword, keystorePath, keystorePassword);
    }

    @Override
    public String toString() {
        return "SslSettings{" +
                "truststorePath='" + truststorePath + '\'' +
                ", keystorePath='" + keystorePath + '\'' +
                '}';
    }
}

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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

@Component
@PropertySource(
        value = {"classpath:service.properties", "file:${appConfig}"}, ignoreResourceNotFound = true)
public class SslSettings {

    public static final String SSL = "SSL";
    public static final String JKS = "JKS";
    public static final String DEFAULT_SSL_CIPHER_SUITES = "TLS_RSA_WITH_AES_256_CBC_SHA";

    private static final String VAULT_PREFIX = "vault";

    @Value("${ssl.authKey}")
    private String authKey;

    @Value("${ssl.truststore.path}")
    private String truststorePath;

    @Value("${ssl.truststore.password}")
    private String truststorePassword;

    @Value("${ssl.keystore.path}")
    private String keystorePath;

    @Value("${ssl.keystore.password}")
    private String keystorePassword;

    @Value("${ssl.truststore}")
    private String truststore;

    @Value("${ssl.keystore}")
    private String keystore;

    @Value("#{'${ssl.cipherSuites:TLS_RSA_WITH_AES_256_CBC_SHA}'.split(',')}")
    private String[] cipherSuites;

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

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

    public String[] getCipherSuites() {
        return cipherSuites;
    }

    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public String getTruststore() {
        return this.truststore;
    }

    public String getKeystore() {
        return this.keystore;
    }
    public byte[] getDecodedTruststore() {
        return Base64.getDecoder().decode(getTruststore());
    }

    public byte[] getDecodedKeystore() {
        return Base64.getDecoder().decode(getKeystore());
    }

    public static boolean readSecureStoreFileAsVaultProperty(String path) {
        return StringUtils.startsWith(path, VAULT_PREFIX);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SslSettings that = (SslSettings) o;
        return Objects.equals(authKey, that.authKey) && Objects.equals(truststorePath, that.truststorePath) && Objects.equals(truststorePassword, that.truststorePassword) && Objects.equals(keystorePath, that.keystorePath) && Objects.equals(keystorePassword, that.keystorePassword) && Arrays.equals(cipherSuites, that.cipherSuites);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(authKey, truststorePath, truststorePassword, keystorePath, keystorePassword);
        result = 31 * result + Arrays.hashCode(cipherSuites);
        return result;
    }

    @Override
    public String toString() {
        return "SslSettings{" +
                "truststorePath='" + truststorePath + '\'' +
                ", keystorePath='" + keystorePath + '\'' +
                ", cipherSuites=" + Arrays.toString(cipherSuites) +
                '}';
    }
}

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

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.comcast.apps.dataaccess.config.SslSettings.*;


@Configuration
public class CassandraConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraConfiguration.class);

    protected CassandraSettings cassandraSettings;

    protected SslSettings sslSettings;

    protected HostStateListener hostStateListener;

    protected ClusterLatencyListener clusterLatencyListener;

    @Autowired
    public CassandraConfiguration(final CassandraSettings cassandraSettings, final SslSettings sslSettings, HostStateListener hostStateListener, ClusterLatencyListener clusterLatencyListener) {
        this.cassandraSettings = cassandraSettings;
        this.sslSettings = sslSettings;
        this.hostStateListener = hostStateListener;
        this.clusterLatencyListener = clusterLatencyListener;
    }

    @Bean
    public Cluster cluster() {
        return cluster(cassandraSettings.getUsername(), cassandraSettings.getPassword());
    }

    @Bean
    public Session session() {
        return cluster().connect(cassandraSettings.getKeyspaceName());
    }

    protected Cluster cluster(String username, String password) {
        Cluster.Builder clusterBuilder = Cluster.builder();

        if (cassandraSettings.isUseSsl()) {
            Optional<RemoteEndpointAwareJdkSSLOptions> sslOptions = getSslOptions();
            sslOptions.ifPresent(sslOption -> clusterBuilder.withSSL(sslOption));
        }

        clusterBuilder.addContactPoints(cassandraSettings.getContactPoints())
                .withPort(cassandraSettings.getPort())
                .withInitialListeners(Collections.singleton(hostStateListener))
                .withCredentials(username, password)
                .withQueryOptions(new QueryOptions().setConsistencyLevel(
                        ConsistencyLevel.valueOf(cassandraSettings.getConsistencyLevel())));
        if (StringUtils.isNotBlank(cassandraSettings.getLocalDataCenter())) {
            DCAwareRoundRobinPolicy roundRobinPolicy = DCAwareRoundRobinPolicy.builder()
                    .withLocalDc(cassandraSettings.getLocalDataCenter())
                    .build();
            clusterBuilder.withLoadBalancingPolicy(roundRobinPolicy);
        }

        Cluster cluster = clusterBuilder.build();
        cluster.register(clusterLatencyListener);
        return cluster;
    }

    protected Optional<RemoteEndpointAwareJdkSSLOptions> getSslOptions() {
        SSLContext sslContext = getSSLContext();

        if (Objects.isNull(sslContext)) return Optional.empty();

        RemoteEndpointAwareJdkSSLOptions sslOptions = RemoteEndpointAwareJdkSSLOptions.builder()
                .withSSLContext(sslContext)
                .withCipherSuites(sslSettings.getCipherSuites())
                .build();

        return Optional.of(sslOptions);
    }

    protected SSLContext getSSLContext() {
        try (InputStream tsf = readTruststore();
             InputStream ksf = readKeystore()) {

            SSLContext sslContext = initSslContext(tsf, sslSettings.getTruststorePassword(), ksf, sslSettings.getKeystorePassword());
            return sslContext;

        } catch (Exception e) {
            LOGGER.error("SSL Context Initialization Exception:", e);
        }
        return null;
    }

    protected InputStream readKeystore() throws FileNotFoundException {
        return readSecureStoreFileAsVaultProperty(sslSettings.getKeystorePath()) ? new ByteArrayInputStream(sslSettings.getDecodedKeystore()) : new FileInputStream(sslSettings.getKeystorePath());
    }

    protected InputStream readTruststore() throws FileNotFoundException {
        return readSecureStoreFileAsVaultProperty(sslSettings.getTruststorePath()) ? new ByteArrayInputStream(sslSettings.getDecodedTruststore()) : new FileInputStream(sslSettings.getTruststorePath());
    }

    protected SSLContext initSslContext(InputStream truststoreInputStream, String truststorePassword, InputStream keystoreInputStream, String keystorePassword) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(SSL);
        TrustManagerFactory tmf = initTrustManagerFactory(truststoreInputStream, truststorePassword);
        KeyManagerFactory kmf = initKeyManagerFactory(keystoreInputStream, keystorePassword);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    protected TrustManagerFactory initTrustManagerFactory(InputStream tsf, String truststorePassword) throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException {
        KeyStore ts = KeyStore.getInstance(JKS);
        ts.load(tsf, truststorePassword.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);

        return tmf;
    }

    protected KeyManagerFactory initKeyManagerFactory(InputStream ksf, String keystorePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance(JKS);
        ks.load(ksf, keystorePassword.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keystorePassword.toCharArray());

        return kmf;
    }
}

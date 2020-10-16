/*
 * If not stated otherwise in this file or this component's LICENSE file the
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
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.comcast.xconf.contextconfig;

import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.aspect.UpdateDateAspect;
import com.comcast.xconf.util.HttpClientUtils;
import com.comcast.xconf.util.LoggingAspect;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataserviceContext {

    @Bean
    CloseableHttpAsyncClient httpClient() {
        CloseableHttpAsyncClient client = httpAsyncClientBuilder().build();
        client.start();
        return client;
    }

    @Bean
    HttpAsyncClientBuilder httpAsyncClientBuilder() {
        XconfSpecificConfig config = xconfSpecificConfig();
        return HttpAsyncClients.custom()
                .setDefaultIOReactorConfig(IOReactorConfig.
                        custom().
                        setSoReuseAddress(true).
                        setSoKeepAlive(true).
                        setConnectTimeout(config.getConnectionTimeoutInMs()).
                        setSoTimeout(config.getSocketTimeoutInMs()).
                        build()).
                        setDefaultRequestConfig(RequestConfig.
                                custom().
                                setConnectTimeout(config.getConnectionTimeoutInMs()).
                                setConnectionRequestTimeout(config.getRequestTimeoutInMs()).
                                setSocketTimeout(config.getSocketTimeoutInMs()).
                                build()).
                        setSSLHostnameVerifier(HttpClientUtils.getHostNameVerifier()).
                        setSSLContext(HttpClientUtils.getSSLContext()).
                        setMaxConnTotal(config.getMaxConnections()).
                        setMaxConnPerRoute(config.getMaxConnectionsPerRoute()).
                        setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE).
                        setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE);
    }

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public UpdateDateAspect updateDateAspect() {
        return new UpdateDateAspect();
    }

    @Bean
    XconfSpecificConfig xconfSpecificConfig() {
        return new XconfSpecificConfig();
    }
}

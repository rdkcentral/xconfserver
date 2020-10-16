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
 * Author: Stanislav Menshykov
 * Created: 5/22/15  2:49 PM
 */
package com.comcast.xconf.admin.contextconfig;

import com.comcast.xconf.contextconfig.DataserviceContext;
import com.comcast.xconf.contextconfig.DatastoreContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

@Configuration
@ComponentScan(
        basePackages = {
                "com.comcast.xconf.admin",
                "com.comcast.xconf.auth",
                "com.comcast.xconf.clients",
                "com.comcast.xconf.config",
                "com.comcast.xconf.report",
                "com.comcast.xconf.evaluators",
                "com.comcast.xconf.util",
                "com.comcast.xconf.estbfirmware",
                "com.comcast.xconf.dcm",
                "com.comcast.xconf.queries",
                "com.comcast.xconf.service",
                "com.comcast.xconf.shared.handler",
                "com.comcast.xconf.migration",
                "com.comcast.xconf.validators",
                "com.comcast.xconf.featurecontrol",
                "com.comcast.xconf.converter",
                "com.comcast.xconf.search",
                "com.comcast.xconf.permissions",
                "com.comcast.xconf.util"
        }
)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({DatastoreContext.class, DataserviceContext.class})
public class AdminWebConfig extends WebMvcConfigurationSupport {
    @Override
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        handlerMapping.setUseSuffixPatternMatch(false);
        handlerMapping.setUrlDecode(false);
        return handlerMapping;
    }

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/app/**").addResourceLocations("/app/");
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }

    @Bean
    public InternalResourceViewResolver viewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(converter());
        converters.add(xmlConverter());
        addDefaultHttpMessageConverters(converters);
    }

    @Bean
    MappingJackson2HttpMessageConverter converter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(configureObjectMapper(new ObjectMapper()));
        return converter;
    }

    @Bean
    public MappingJackson2XmlHttpMessageConverter xmlConverter() {
        MappingJackson2XmlHttpMessageConverter converter = new MappingJackson2XmlHttpMessageConverter();
        converter.setObjectMapper(configureObjectMapper(new XmlMapper()));
        return converter;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setLocation(new FileSystemResource("${appConfig}"));
        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }

    private ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}

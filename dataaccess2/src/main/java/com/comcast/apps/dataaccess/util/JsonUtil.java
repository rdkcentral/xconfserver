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
package com.comcast.apps.dataaccess.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import javax.xml.bind.DataBindingException;
import java.io.IOException;
import java.io.InputStream;


public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    static {
        MAPPER.registerModule(new AfterburnerModule());
        MAPPER.registerModule(new JodaModule());
    }

    /**
     * Provides Jackson aided marshalling facility
     *
     * @param entity entity to marshall
     * @param <T>    inferred type parameter
     * @return string(json) marshaled representation of object supplied
     */
    public static <T> String toJson(T entity) {
        final ObjectWriter jsonWriter = MAPPER.writer();
        try {
            return jsonWriter.writeValueAsString(entity);
        } catch (IOException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Provides Jackson aided JSON unmarshalling facility
     *
     * @param clazz      class to unmarshall against
     * @param marshalled marshalled representation
     * @param <T>        inferred from class type
     * @return instance if given class
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(final Class<T> clazz, final String marshalled) {
        return fromJson(MAPPER.getTypeFactory().constructType(clazz), marshalled);
    }

    public static <T> T fromJson(final TypeReference<T> tref, final String marshalled) {
        return fromJson(MAPPER.getTypeFactory().constructType(tref), marshalled);
    }

    private static <T> T fromJson(final JavaType javaType, final String marshalled) {
        if (null == marshalled || marshalled.isEmpty()) {
            return null;
        } else {
            try {
                return MAPPER.reader(javaType).readValue(new MappingJsonFactory(MAPPER).createParser(marshalled));
            } catch (IOException e) {
                throw new DataBindingException(e);
            }
        }
    }

    public static <T> T fromJson(final Class<T> clazz, final InputStream marshalled) {
        return fromJson(MAPPER.getTypeFactory().constructType(clazz), marshalled);
    }

    public static <T> T fromJson(final TypeReference<T> tref, final InputStream marshalled) {
        return fromJson(MAPPER.getTypeFactory().constructType(tref), marshalled);
    }

    private static <T> T fromJson(final JavaType javaType, final InputStream marshalled) {
        if (null == marshalled) {
            return null;
        } else {
            try {
                return MAPPER.reader(javaType).readValue(new MappingJsonFactory().createParser(marshalled));
            } catch (IOException e) {
                throw new DataBindingException(e);
            }
        }
    }

    public static <T> T fromJson(String json, Class<T> parametrized, Class<?>... parameterClasses) {
        final JavaType type = MAPPER.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new DataBindingException(e);
        }
    }

}

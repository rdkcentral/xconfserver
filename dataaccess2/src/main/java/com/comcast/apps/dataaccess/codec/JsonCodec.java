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
/*
 * Copyright DataStax, Inc.
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
 */
package com.comcast.apps.dataaccess.codec;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.nio.ByteBuffer;


public class JsonCodec<T> extends TypeCodec<T> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public JsonCodec(Class<T> javaType) {
        this(DataType.varchar(), javaType);
    }

    public JsonCodec(DataType cqlType, Class<T> javaType) {
        super(cqlType, javaType);
    }

    @Override
    public ByteBuffer serialize(T value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (value == null)
	   return null;
        try {
	   return ByteBuffer.wrap(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException e) {
	   throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        if (bytes == null)
	   return null;
        try {
	   byte[] b = new byte[bytes.remaining()];
	   // always duplicate the ByteBuffer instance before consuming it!
	   bytes.duplicate().get(b);
	   return (T) objectMapper.readValue(b, toJacksonJavaType());
        } catch (IOException e) {
	   throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    @Override
    public String format(T value) throws InvalidTypeException {
        if (value == null)
	   return "NULL";
        String json;
        try {
	   json = objectMapper.writeValueAsString(value);
        } catch (IOException e) {
	   throw new InvalidTypeException(e.getMessage(), e);
        }
        return '\'' + json.replace("\'", "''") + '\'';
    }

    @Override
    @SuppressWarnings("unchecked")
    public T parse(String value) throws InvalidTypeException {
        if (value == null || value.isEmpty()|| value.equalsIgnoreCase("NULL"))
	   return null;
        if (value.charAt(0) != '\'' || value.charAt(value.length() - 1) != '\'')
	   throw new InvalidTypeException("JSON strings must be enclosed by single quotes");
        String json = value.substring(1, value.length() - 1).replace("''", "'");
        try {
	   return (T) objectMapper.readValue(json, toJacksonJavaType());
        } catch (IOException e) {
	   throw new InvalidTypeException(e.getMessage(), e);
        }
    }

    protected JavaType toJacksonJavaType() {
        return TypeFactory.defaultInstance().constructType(getJavaType().getType());
    }

}

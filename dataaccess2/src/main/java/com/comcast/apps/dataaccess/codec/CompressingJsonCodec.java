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
 * @author Igor Kostrov (ikostrov@productengine.com)
 */
package com.comcast.apps.dataaccess.codec;

import com.comcast.apps.dataaccess.util.Archiver;
import com.comcast.apps.dataaccess.util.CompressionUtil;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class CompressingJsonCodec<T> extends JsonCodec<T> {

    private static final Logger log = LoggerFactory.getLogger(CompressingJsonCodec.class);

    private static final Archiver archiver = CompressionUtil.createArchiver();

    public CompressingJsonCodec(DataType cqlType, Class<T> javaType) {
        super(cqlType, javaType);
    }

    @Override
    public ByteBuffer serialize(T value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return archiver.compress(super.serialize(value, protocolVersion));
    }

    @Override
    public T deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        try {
            return super.deserialize(archiver.decompress(bytes), protocolVersion);
        } catch (Archiver.DataFormatException e) {
            log.error("Exception while trying to decompress entity from db response", e);
            throw new RuntimeException(e);
        }
    }
}

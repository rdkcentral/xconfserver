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
package com.comcast.apps.dataaccess.util;

import org.iq80.snappy.CorruptionException;
import org.iq80.snappy.Snappy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CompressionUtil {

    /**
     * Creates an utility for byte oriented compressing/decompressing and joining/splitting.
     *
     * @return new Archiver instance
     */
    public static Archiver createArchiver() {
        return new SnappyArchiver();
    }

    //----- archivers section - see Archiver interface for methods description ---------//

    /**
     * Base class for archivers. Implements {@link Archiver#split(java.nio.ByteBuffer, int)} and
     * {@link Archiver#join(java.nio.ByteBuffer[])} methods.
     */
    private static abstract class BaseArchiver implements Archiver {
        @Override
        public final ByteBuffer[] split(final ByteBuffer data, final int chunkSize) {
            final int chunksCount = data.remaining() / chunkSize + 1;
            final ByteBuffer[] result = new ByteBuffer[chunksCount];

            int offset = data.position();
            for (int i = 0; i < chunksCount - 1; i++) {
                result[i] = ByteBuffer.wrap(data.array(), offset, chunkSize);
                offset += chunkSize;
            }
            result[chunksCount - 1] = ByteBuffer.wrap(data.array(), offset, data.remaining() - offset);

            return result;
        }

        @Override
        public final ByteBuffer join(final ByteBuffer[] data) {
            final ByteArrayOutputStream builder = new ByteArrayOutputStream();
            for (ByteBuffer byteBuffer : data) {
                builder.write(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
            }
            return ByteBuffer.wrap(builder.toByteArray());
        }
    }

    /**
     * Wrapper over a fast compressor/decompressor snappy library.
     */
    private static class SnappyArchiver extends BaseArchiver {
        @Override
        public ByteBuffer compress(ByteBuffer data) {
            byte[] array = data.array();
            int offset = data.position();
            int length = data.remaining();

            int maxCompressedLength = Snappy.maxCompressedLength(length);
            byte[] rawResult = new byte[maxCompressedLength];
            int compressedBytesCount = Snappy.compress(array, offset, length, rawResult, 0);
            return ByteBuffer.wrap(rawResult, 0, compressedBytesCount);
        }

        @Override
        public ByteBuffer decompress(ByteBuffer data) throws DataFormatException {
            byte[] array = data.array();
            int offset = data.position();
            int length = data.remaining();
            try {
                byte[] rawResult = Snappy.uncompress(array, offset, length);
                return ByteBuffer.wrap(rawResult);
            } catch (CorruptionException ex) {
                throw new DataFormatException(ex);
            }
        }
    }

}

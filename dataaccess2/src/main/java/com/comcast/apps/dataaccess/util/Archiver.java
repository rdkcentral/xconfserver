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
 */
package com.comcast.apps.dataaccess.util;

import java.nio.ByteBuffer;

/**
 * Interface for concrete archivers, e.g. java.util.zip or org.xerial.snappy.
 */
public interface Archiver {
    /**
     * Compress ByteBuffer.
     */
    ByteBuffer compress(final ByteBuffer data);

    /**
     * Decompress ByteBuffer.
     * @exception DataFormatException if input data has invalid format
     */
    ByteBuffer decompress(final ByteBuffer data) throws DataFormatException;

    /**
     * Split data into ByteBuffer array. As a rule all elements of a resulting array, except last, represents
     * chunkSize count of bytes.
     * @param data ByteBuffer to split
     * @param chunkSize maximum count of bytes in one chunk, i.e. ByteBuffer.
     * @return split data
     */
    ByteBuffer[] split(final ByteBuffer data, final int chunkSize);

    /**
     * Join bytes from ByteBuffer array.
     */
    ByteBuffer join(final ByteBuffer[] data);

    /**
     * Signals that a data format exception has occurred. E.g. unknown compression method or unexpected end of data.
     */
    public class DataFormatException extends Exception {
        public DataFormatException() {
            super();
        }

        public DataFormatException(String message) {
            super(message);
        }

        public DataFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public DataFormatException(Throwable cause) {
            super(cause);
        }
    }
}

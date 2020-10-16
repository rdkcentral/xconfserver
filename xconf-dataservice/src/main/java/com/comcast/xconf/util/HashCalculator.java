/*
 * If not stated otherwise in this file or this component's LICENSE file the
 * following copyright and licenses apply:
 *
 * Copyright 2018 RDK Management
 *
 * Licensed under the Apache License, Version 2.0 (the License);
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

package com.comcast.xconf.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashCalculator {

    private static final String UTF_8 = "UTF-8";

    private static final Logger log = LoggerFactory.getLogger(HashCalculator.class);

    public static String calculateHash(final String plainText) throws NoSuchAlgorithmException {
        if (StringUtils.isNotBlank(plainText)) {
            return calculateHash(plainText.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }


    public static String calculateHash(byte[] bytes) throws NoSuchAlgorithmException {

        byte[] newBytes = bytes;

        final int byteSize = bytes.length;
        if (byteSize < 128) {
            newBytes = pkcs7Pad(bytes, byteSize);
        }

        if (newBytes.length < 128) {
            log.error("Exception, must be minimum of 128 bytes for input");
        }

        MessageDigest sha256MessageDigest = MessageDigest.getInstance("SHA-256");


        sha256MessageDigest.update(newBytes);
        final byte[] sha256Digest = sha256MessageDigest.digest();

        final RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();

        ripemd160Digest.update(sha256Digest, 0, sha256Digest.length);
        final byte[] ridDigest = new byte[ripemd160Digest.getDigestSize()];
        ripemd160Digest.doFinal(ridDigest, 0);

        return base58checkEncode(ridDigest);
    }

    private static String base58checkEncode(byte[] payload) throws NoSuchAlgorithmException {
        byte[] version = {0x00};

        byte[] hash = sha256DoubleDigest(ArrayUtils.addAll(version, payload));
        byte[] checkSum = ArrayUtils.subarray(hash, 0, 4);

        byte[] result = ArrayUtils.addAll(ArrayUtils.addAll(version, payload), checkSum);
        Base58.encode(result);
        return Base58.encode(result);
    }

    static byte[] pkcs7Pad(final byte[] bytes, final int byteSize) {

        if (byteSize >= 128) {
            return bytes;
        }

        final PKCS7Padding padding = new PKCS7Padding();
        final int padLen = 128 - bytes.length;
        byte[] padBytes = new byte[padLen];
        padding.addPadding(padBytes, 0);

        byte[] finalBytes = new byte[bytes.length + padBytes.length];
        System.arraycopy(bytes, 0, finalBytes, 0, bytes.length);
        System.arraycopy(padBytes, 0, finalBytes, bytes.length, padBytes.length);

        return finalBytes;
    }

    public static byte[] sha256DoubleDigest(byte[] value) throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(value);
        byte[] digest1 = messageDigest.digest();
        messageDigest.update(digest1);
        return messageDigest.digest();
    }

    public static String encode(byte[] data) throws UnsupportedEncodingException {
        byte[] encodedData = Base64.encode(data);
        return new String(encodedData, UTF_8);
    }

    public static byte[] decode(String encoded) {
        return Base64.decode(encoded);
    }
}
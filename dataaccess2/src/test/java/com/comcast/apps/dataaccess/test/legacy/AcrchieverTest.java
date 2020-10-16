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
package com.comcast.apps.dataaccess.test.legacy;

import com.comcast.apps.dataaccess.util.Archiver;
import com.comcast.apps.dataaccess.util.CompressionUtil;
import com.google.common.base.Charsets;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.ByteBuffer;

public class AcrchieverTest {

    @Ignore
    @Test
    public void name() throws Exception {
        String javaPath = System.getProperty("java.library.path");
        System.out.println("Java path: " + javaPath);
        String str = "57f0407b226964223a2248415453444556514131222c2275706461746564223a313432373437363439343235362c2264617461223a5b2241525332222c2241525333222c010e003409072835222c2241525336225d7d";
        Archiver archiver = CompressionUtil.createArchiver();
        ByteBuffer decompress = archiver.decompress(ByteBuffer.wrap(str.getBytes()));
        String strData = Charsets.UTF_8.decode(decompress).toString();
        System.out.println("strData: " + strData);
    }
}

/*
 * If not stated otherwise in this file or this component's Licenses.txt file the
 * following copyright and licenses apply:
 *
 * Copyright 2020 RDK Management
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
 * @author Maxym Dolina (mdolina@productengine.com)
 */
package com.comcast.xconf.admin.validator;

import com.comcast.xconf.admin.core.Utils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlValidatorTest {

    @Test
    public void urlIsValid() {
        boolean valid = Utils.isValidUrl("https://abc.pqr.xyz.cloud");
        assertTrue(valid);

        valid = Utils.isValidUrl("https://abc.com");
        assertTrue(valid);

        valid = Utils.isValidUrl("https://www.abc.com");
        assertTrue(valid);

        valid = Utils.isValidUrl("tftp://10.10.10.10");
        assertTrue(valid);
    }

    @Test
    public void urlIsNotValid() {
        boolean notValid = Utils.isValidUrl("https:// abc.com");
        assertFalse(notValid);

        notValid = Utils.isValidUrl("abc.com");
        assertFalse(notValid);

        notValid = Utils.isValidUrl("www.abc.com");
        assertFalse(notValid);
    }
}

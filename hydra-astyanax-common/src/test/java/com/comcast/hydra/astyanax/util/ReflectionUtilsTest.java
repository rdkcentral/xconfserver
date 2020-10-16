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
 * Author: slavrenyuk
 * Created: 12/6/13
 */
package com.comcast.hydra.astyanax.util;

import com.comcast.hydra.astyanax.data.Excluded;
import com.comcast.hydra.astyanax.data.HColumn;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ReflectionUtilsTest {

    @Test
    public void testGetColumnNamesFromFields() {
        List<String> actual = ReflectionUtils.getColumnNamesFromFields(PersistableObj.class);

        List<String> expected = Arrays.asList("d", "b", "a", "c", "deprecatedExcluded"/* pay attention, deprecatedExcluded is present */, "e");
        Assert.assertEquals(expected, actual);
    }

    public static class PersistableObj {
        protected String deprecatedExcluded;
        protected String excluded;

        // alphabetical order is important for ReflectionUtils.getColumnNamesFromFields()
        protected String a;
        protected String b;
        protected String c;
        protected String d;
        protected String e;

        @Excluded
        public String getDeprecatedExcluded() {
            return deprecatedExcluded;
        }

        @HColumn(excluded = true)
        public String getExcluded() {
            return excluded;
        }

        public String getA() {
            return a;
        }

        @HColumn(order = 100)
        public String getB() {
            return b;
        }

        public String getC() {
            return c;
        }

        @HColumn(order = 0)
        public String getD() {
            return d;
        }

        public String getE() {
            return e;
        }

        //==================== all setters ===========================================

        public void setDeprecatedExcluded(String deprecatedExcluded) {
            this.deprecatedExcluded = deprecatedExcluded;
        }

        public void setExcluded(String excluded) {
            this.excluded = excluded;
        }

        public void setA(String a) {
            this.a = a;
        }

        public void setB(String b) {
            this.b = b;
        }

        public void setC(String c) {
            this.c = c;
        }

        public void setD(String d) {
            this.d = d;
        }

        public void setE(String e) {
            this.e = e;
        }
    }
}

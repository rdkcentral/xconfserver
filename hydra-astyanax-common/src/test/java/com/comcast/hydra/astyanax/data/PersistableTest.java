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
package com.comcast.hydra.astyanax.data;

import org.junit.Assert;
import org.junit.Test;

public class PersistableTest {
    @Test
    public void testTtl() {
        // fields: id and updated
        Persistable persistable = new Persistable() {};
        Assert.assertEquals(0, persistable.getTTL("id"));

        persistable.setAllTTLs(1);
        Assert.assertEquals(1, persistable.getTTL("id"));
        Assert.assertEquals(1, persistable.getTTL("updated"));

        persistable.setAllTTLs(2);
        persistable.setTTL("updated", 3);
        Assert.assertEquals(2, persistable.getTTL("id"));
        Assert.assertEquals(3, persistable.getTTL("updated"));

        persistable.clearTTL();
        Assert.assertEquals(0, persistable.getTTL("id"));
        Assert.assertEquals(0, persistable.getTTL("updated"));
    }
}

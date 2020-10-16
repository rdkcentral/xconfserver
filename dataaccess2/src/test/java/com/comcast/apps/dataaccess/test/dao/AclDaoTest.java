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
package com.comcast.apps.dataaccess.test.dao;

import com.comcast.apps.dataaccess.acl.AccessControlInfo;
import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.test.config.AppConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashSet;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AppConfig.class})
public class AclDaoTest {

    @Autowired
    private CachedSimpleDao<Long, AccessControlInfo> aclDao;

    @Test
    public void get() throws Exception {
        AccessControlInfo info = save();

        Assert.assertEquals(info, aclDao.getOne(0L));
    }

    @Test
    public void delete() throws Exception {
        save();
        aclDao.deleteOne(0L);

        Assert.assertNull(aclDao.getOne(0L));
    }

    private AccessControlInfo save() {
        AccessControlInfo info = new AccessControlInfo();
        info.setGroupPermissions(AccessControlInfo.PERM_READ);
        info.setTrustedGroups(new HashSet<>());
        info.setOwnerId("wownerId");
        aclDao.setOne(0L, info);
        return info;
    }
}

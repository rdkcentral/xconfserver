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

package com.comcast.xconf.service;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.queries.controllers.BaseQueriesControllerTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericNamespacedListQueriesServiceTest extends BaseQueriesControllerTest {

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void validateNamespacedIdListExistenceNotUsingCache() throws Exception {
        GenericNamespacedList macList = createGenericNamespacedList(defaultMacListId, GenericNamespacedListTypes.MAC_LIST, defaultMacAddress);
        nonCachedNamespacedListDao.setOne(macList.getId(), macList);

        exceptionRule.expect(EntityExistsException.class);
        exceptionRule.expectMessage("List with name macListId already exists");
        genericNamespacedListQueriesService.createNamespacedList(macList, GenericNamespacedListTypes.MAC_LIST);
    }
}
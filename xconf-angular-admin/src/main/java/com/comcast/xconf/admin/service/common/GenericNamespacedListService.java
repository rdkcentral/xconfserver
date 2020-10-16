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
 *  Author: mdolina
 *  Created: 7:59 PM
 */
package com.comcast.xconf.admin.service.common;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.IpAddressGroupExtended;
import com.comcast.xconf.admin.validator.common.GenericNamespacedListValidator;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.common.GenericNamespacedListPredicates;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.shared.service.AbstractService;
import com.comcast.xconf.util.GenericNamespacedListUtils;
import com.comcast.xconf.validators.IValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static com.comcast.xconf.util.GenericNamespacedListUtils.getTypeName;

@Service
public class GenericNamespacedListService extends AbstractService<GenericNamespacedList> {

    @Autowired
    private CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO;

    @Autowired
    private GenericNamespacedListValidator genericNamespacedListValidator;

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    @Autowired
    private GenericNamespacedListPredicates namespacedListPredicates;

    @Override
    public CachedSimpleDao<String, GenericNamespacedList> getEntityDAO() {
        return genericNamespacedListDAO;
    }

    @Override
    public IValidator<GenericNamespacedList> getValidator() {
        return genericNamespacedListValidator;
    }

    @Override
    public void normalizeOnSave(GenericNamespacedList genericNamespacedList) {
        if (GenericNamespacedListTypes.MAC_LIST.equals(genericNamespacedList.getTypeName())) {
            GenericNamespacedListUtils.normalizeMacAddress(genericNamespacedList.getData());
        }
    }

    @Override
    public void validateUsage(String namespacedListId) {
        genericNamespacedListQueriesService.checkUsage(namespacedListId);
    }

    @Override
    public void validateOnSave(GenericNamespacedList namespacedList) {
        getValidator().validate(namespacedList);

        Iterable<GenericNamespacedList> all = genericNamespacedListQueriesService.getNonCachedAllByType(namespacedList.getTypeName());
        getValidator().validateAll(namespacedList, all);
    }

    @Override
    protected void beforeCreating(GenericNamespacedList entity) {
        String id = entity.getId();
        if (StringUtils.isBlank(id)) {
            entity.setId(UUID.randomUUID().toString());
        } else if (genericNamespacedListQueriesService.getOneByTypeNonCached(id, entity.getTypeName()) != null) {
            throw new EntityExistsException(getTypeName(entity.getTypeName()) + " " + id + " already exists");
        }
    }

    public GenericNamespacedList getOneNonCached(String id) {
        return genericNamespacedListQueriesService.getOneNonCached(id);
    }

    public List<String> getNamespacedListsIds() {
        return genericNamespacedListQueriesService.getNamespacedListsIds();
    }

    public List<String> getNamespacedListsIdsByType(String typeName) {
        return genericNamespacedListQueriesService.getNamespacedListsIdsByType(typeName);
    }

    public List<GenericNamespacedList> getAllByType(String typeName) {
        return genericNamespacedListQueriesService.getAllByType(typeName);
    }

    public GenericNamespacedList update(GenericNamespacedList list, String newId) {
        return genericNamespacedListQueriesService.updateNamespacedList(list, list.getTypeName(), newId);
    }

    public List<IpAddressGroupExtended> getAllIpAddressGroups() {
        List<GenericNamespacedList> ipLists = getAllByType(GenericNamespacedListTypes.IP_LIST);
        return GenericNamespacedListsConverter.convertToListOfIpAddressGroups(ipLists);
    }

    protected List<Predicate<GenericNamespacedList>> getPredicatesByContext(final Map<String, String> context) {
        return namespacedListPredicates.getPredicates(new ContextOptional(context));
    }

}

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
 * Author: Stanislav Menshykov
 * Created: 09.10.15  10:42
 */
package com.comcast.xconf.service.impl;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.support.exception.ValidationRuntimeException;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.XRule;
import com.comcast.xconf.converter.GenericNamespacedListsConverter;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.exception.EntityConflictException;
import com.comcast.xconf.exception.EntityExistsException;
import com.comcast.xconf.exception.EntityNotFoundException;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.queries.beans.StringListWrapper;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureRule;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.util.GenericNamespacedListUtils;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.comcast.xconf.util.GenericNamespacedListUtils.getTypeName;


@Service
public class GenericNamespacedListQueriesServiceImpl implements GenericNamespacedListQueriesService {

    public static final String LIST_SUFFIX = "_LIST";
    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    @Autowired
    private CachedSimpleDao<String, GenericNamespacedList> genericNamespacedListDAO;

    @Autowired
    private SimpleDao<String, GenericNamespacedList> nonCachedNamespacedListDao;

    @Autowired
    private CachedSimpleDao<String, TelemetryRule> telemetryRuleDAO;

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;

    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;

    @Autowired
    private CachedSimpleDao<String, SettingRule> settingRuleDAO;

    @Autowired
    private CachedSimpleDao<String, Feature> featureDAO;

    @Autowired
    private CachedSimpleDao<String, FeatureRule> featureRuleDAO;

    @Override
    public List<String> getNamespacedListsIds() {
        final List<String> result = new ArrayList<>();
        for (GenericNamespacedList list : genericNamespacedListDAO.getAll()) {
            result.add(list.getId());
        }

        return result;
    }

    @Override
    public List<String> getNamespacedListsIdsByType(String typeName) {
        final List<String> result = new ArrayList<>();
        for (GenericNamespacedList list : getAllByType(typeName)) {
            result.add(list.getId());
        }

        return result;
    }

    @Override
    public List<GenericNamespacedList> getAllNamespacedLists() {
        List<GenericNamespacedList> allLists = genericNamespacedListDAO.getAll();
        Collections.sort(allLists);

        return allLists;
    }

    @Override
    public List<GenericNamespacedList> getAllByType(final String typeName) {
        List<GenericNamespacedList> result = genericNamespacedListDAO.getAll();
        CollectionUtils.filter(result, new org.apache.commons.collections.Predicate() {
            @Override
            public boolean evaluate(Object namespacedList) {
                return typeName.equals(((GenericNamespacedList) namespacedList).getTypeName());
            }
        });

        return result;
    }

    @Override
    public List<GenericNamespacedList> getNonCachedAllByType(final String type) {
        List<GenericNamespacedList> result = nonCachedNamespacedListDao.getAll(Integer.MAX_VALUE);
        CollectionUtils.filter(result, new org.apache.commons.collections.Predicate() {
            @Override
            public boolean evaluate(Object namespacedList) {
                return type.equals(((GenericNamespacedList) namespacedList).getTypeName());
            }
        });
        return  result;
    }

    @Override
    public List<GenericNamespacedList> getListsByIds(final Set<String> ids) {
        return genericNamespacedListDAO.getAll(
                new Predicate<GenericNamespacedList>() {
                    @Override
                    public boolean apply(GenericNamespacedList list) {
                        if (ids.contains(list.getId())) {
                            return true;
                        }
                        return false;
                    }
                }
        );
    }

    @Override
    public GenericNamespacedList getListById(final String listId) {
        return genericNamespacedListDAO.getOne(listId);
    }

    @Override
    public GenericNamespacedList createNamespacedList(final GenericNamespacedList list, final String type) {
        validateOnCreate(list, type);
        list.setTypeName(type);

        if (GenericNamespacedListTypes.MAC_LIST.equals(list.getTypeName()) || GenericNamespacedListTypes.RI_MAC_LIST.equals(list.getTypeName())) {
            list.setData(GenericNamespacedListUtils.normalizeMacAddress(list.getData()));
        }

        genericNamespacedListDAO.setOne(list.getId(), list);
        
        return list;
    }

    @Override
    public GenericNamespacedList updateNamespacedList(GenericNamespacedList list, String type) {
        beforeUpdate(list, type);

        genericNamespacedListDAO.setOne(list.getId(), list);
        return list;
    }

    @Override
    public GenericNamespacedList updateNamespacedList(final GenericNamespacedList list, final String type, final String newId) {
        beforeUpdate(list, type);
        validateNamespacedListId(newId);
        if (!StringUtils.equals(newId, list.getId()) && getOneByTypeNonCached(newId, type) != null) {
            throw new EntityConflictException(getTypeName(type) + " " + newId + " already exists");
        }

        if(!StringUtils.equals(newId, list.getId())) {
            renameNamespacedListInUsedEntities(list.getId(), newId);
            genericNamespacedListDAO.deleteOne(list.getId());
            list.setId(newId);
        }
        
        genericNamespacedListDAO.setOne(list.getId(), list);
        return list;
    }

    @Override
    public GenericNamespacedList deleteNamespacedList(final String typeName, final String id) {
        final GenericNamespacedList namespacedList = getOneByType(id, typeName);
        if (namespacedList == null) {
            throw new EntityNotFoundException("List with id: " + id + " does not exist");
        }
        namespacedList.setUpdated(null);

        checkUsage(id);
        genericNamespacedListDAO.deleteOne(id);

        return namespacedList;
    }

    @Override
    public List<GenericNamespacedList> getListsByMacPart(final String macPart) {
        final String mac = macPart.replaceAll(":", "").replaceAll("-", "")
                .replaceAll("\\.", "").toUpperCase().trim();

        final List<GenericNamespacedList> result = Lists.newArrayList(Iterables.filter(getAllByType(GenericNamespacedListTypes.MAC_LIST), new Predicate<GenericNamespacedList>() {
            @Override
            public boolean apply(GenericNamespacedList input) {
                return Iterables.any(input.getData(), new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        return input.replaceAll(":", "").contains(mac);
                    }
                });
            }
        }));

        return result;
    }

    @Override
    public GenericNamespacedList addNamespacedListData(final String listId, final String listType, final StringListWrapper wrappedMacList) {
        if (StringUtils.isEmpty(listId)) {
            throw new ValidationRuntimeException("Id is empty");
        }

        final Set<String> itemsToAdd = Sets.newHashSet(wrappedMacList.getList());
        GenericNamespacedListUtils.validateListData(listType, itemsToAdd);

        GenericNamespacedList listToUpdate = getOneByTypeNonCached(listId, listType);

        if (listToUpdate == null) {
            throw new EntityNotFoundException("List with current ID doesn't exist");
        }

        Set<String> macsSet = listToUpdate.getData();
        macsSet.addAll(GenericNamespacedListTypes.MAC_LIST.equals(listType) ?
                GenericNamespacedListUtils.normalizeMacAddress(itemsToAdd) : itemsToAdd);
        listToUpdate.setData(macsSet);

        List<GenericNamespacedList> allMacLists = getAllByType(GenericNamespacedListTypes.MAC_LIST);
        GenericNamespacedListUtils.validateDataIntersection(listToUpdate, macsSet, allMacLists);

        genericNamespacedListDAO.setOne(listToUpdate.getId(), listToUpdate);
        return listToUpdate;
    }

    @Override
    public GenericNamespacedList removeNamespacedListData(final String listId, final String listType, final StringListWrapper wrappedMacList){
        if (StringUtils.isEmpty(listId)) {
            throw new ValidationRuntimeException("ID is empty");
        }

        Set<String> itemsToRemove = Sets.newHashSet(wrappedMacList.getList());
        GenericNamespacedListUtils.validateListData(listType, itemsToRemove);

        GenericNamespacedList listToUpdate = getOneByTypeNonCached(listId, listType);

        if (listToUpdate == null) {
            throw new EntityNotFoundException("List with current ID doesn't exist");
        }

        itemsToRemove = GenericNamespacedListTypes.MAC_LIST.equals(listType) ?
                GenericNamespacedListUtils.normalizeMacAddress(itemsToRemove) : itemsToRemove;

        Set<String> macsSet = listToUpdate.getData();

        Sets.SetView itemsNotInList = Sets.difference(itemsToRemove, macsSet);
        if(itemsNotInList.size() > 0) {
            String message = String.format("List contains %ss, which are not present in current Namespaced list: %s", getItemName(listType), itemsNotInList.toString());
            throw new ValidationRuntimeException(message);
        }

        macsSet.removeAll(itemsToRemove);
        if (macsSet.isEmpty()) {
            String message = String.format("Namespaced list should contain at least one %s address", getItemName(listType));
            throw new ValidationRuntimeException(message);
        }
        listToUpdate.setData(macsSet);

        genericNamespacedListDAO.setOne(listToUpdate.getId(), listToUpdate);
        return listToUpdate;
    }

    private String getItemName(String listType) {
        return listType.replaceAll(LIST_SUFFIX, "");
    }

    @Override
    public List<GenericNamespacedList> getListsByIp(String ip) {
        final List<GenericNamespacedList> result = new ArrayList();
        final IpAddress ipForSearch = new IpAddress(ip);
        for (GenericNamespacedList ipList : getAllByType(GenericNamespacedListTypes.IP_LIST)) {
            if (IpAddress.isInRange(ipForSearch, GenericNamespacedListsConverter.convertToIpAddressGroupExtended(ipList).getIpAddresses())) {
                result.add(ipList);
            }
        }
        return result;
    }

    @Override
    public GenericNamespacedList getOneByType(final String id, final String type) {
        GenericNamespacedList result = genericNamespacedListDAO.getOne(id);
        if (result != null && result.getTypeName().equals(type)) {
            return result;
        }

        return null;
    }

    @Override
    public GenericNamespacedList getOneByTypeNonCached(final String id, final String type) {
        GenericNamespacedList result = nonCachedNamespacedListDao.getOne(id);
        if (result != null && StringUtils.equals(result.getTypeName(), type)) {
            return result;
        }
        return null;
    }

    @Override
    public GenericNamespacedList getOneNonCached(final String id) {
        return nonCachedNamespacedListDao.getOne(id);
    }

    @Override
    public void checkUsage(String id) {
        List<? extends CachedSimpleDao<String, ? extends XRule>> ruleDaos = Lists.newArrayList(firmwareRuleDao, firmwareRuleTemplateDao,
                dcmRuleDAO, telemetryRuleDAO, settingRuleDAO, featureRuleDAO);
        for (CachedSimpleDao<String, ? extends XRule> dao : ruleDaos) {
            for (final XRule xRule : dao.getAll()) {
                List<String> ids = RuleUtil.getFixedArgsFromRuleByOperation(xRule.getRule(), RuleFactory.IN_LIST);
                if (ids.contains(id)) {
                    throw new EntityConflictException("List is used by " + xRule.getRuleType() + " "  + xRule.getName());
                }
            }
        }
        for (Feature feature : Optional.presentInstances(featureDAO.asLoadingCache().asMap().values())) {
            if (feature.isWhitelisted() && feature.getWhitelistProperty() != null && StringUtils.equals(id, feature.getWhitelistProperty().getValue())) {
                throw new EntityConflictException("NamespacedList is used by " + feature.getFeatureName() + " feature");
            }
        }
    }

    private void validateOnCreate(final GenericNamespacedList namespacedList, final String typeName) {
        validateNamespacedListId(namespacedList.getId());
        if (nonCachedNamespacedListDao.getOne(namespacedList.getId()) != null) {
            throw new EntityExistsException("List with name " + namespacedList.getId() + " already exists");
        }

        if (!GenericNamespacedListTypes.isValidType(typeName)) {
            throw new ValidationRuntimeException("Type " + typeName + " is invalid");
        }

        List<GenericNamespacedList> allByType = getNonCachedAllByType(typeName);
        GenericNamespacedListUtils.validateListData(namespacedList);
        GenericNamespacedListUtils.validateDataIntersection(namespacedList, namespacedList.getData(), allByType);
    }

    private void validateOnUpdate(final GenericNamespacedList namespacedList, final String type) {
        validateNamespacedListId(namespacedList.getId());
        final GenericNamespacedList namespacedListToUpdate = getOneByTypeNonCached(namespacedList.getId(), type);
        if (namespacedListToUpdate == null) {
            throw new EntityNotFoundException("List with id " + namespacedList.getId() + " doesn't exist");
        }

        List<GenericNamespacedList> allByType = getNonCachedAllByType(type);
        GenericNamespacedListUtils.validateListData(namespacedList);
        GenericNamespacedListUtils.validateDataIntersection(namespacedList, namespacedList.getData(), allByType);
    }

    private void renameNamespacedListInUsedEntities(String oldNamespacedListId, String newNamespacedListId) {
        List<? extends CachedSimpleDao<String, ? extends IPersistable>> daos = Lists.newArrayList(firmwareRuleTemplateDao, firmwareRuleDao, telemetryRuleDAO, dcmRuleDAO, settingRuleDAO, featureRuleDAO);
        for (CachedSimpleDao dao : daos) {
            for (Object persistable : dao.getAll()) {
                XRule xRule = (XRule) persistable;
                if (RuleUtil.changeFixedArgToNewValue(oldNamespacedListId, newNamespacedListId, xRule.getRule(), RuleFactory.IN_LIST)) {
                    dao.setOne(xRule.getId(), xRule);
                }
            }
        }
        for (Feature feature : featureDAO.getAll()) {
            if (feature.isWhitelisted() && StringUtils.equals(feature.getWhitelistProperty().getValue(), oldNamespacedListId)) {
                feature.getWhitelistProperty().setValue(newNamespacedListId);
                featureDAO.setOne(feature.getId(), feature);
            }
        }
    }

    private void beforeUpdate(GenericNamespacedList list, String type) {
        validateOnUpdate(list, type);

        GenericNamespacedList namespacedListToUpdate = getOneByType(list.getId(), type);
        list.setTypeName(namespacedListToUpdate.getTypeName());

        if (GenericNamespacedListTypes.MAC_LIST.equals(list.getTypeName())) {
            list.setData(GenericNamespacedListUtils.normalizeMacAddress(list.getData()));
        }
    }

    @Override
    public boolean isMacListHasMacPart(String macPart, Set<String> macs) {
        final String  normalizedMacPart = macPart.replaceAll(":", "").replaceAll("-", "")
                .replaceAll("\\.", "").toUpperCase().trim();

        return Iterables.any(macs, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input.replaceAll(":", "").contains(normalizedMacPart);
            }
        });
    }

    @Override
    public boolean isIpAddressHasIpPart(final String ipPart, Set<String> ipAddresses) {
        return Iterables.any(ipAddresses, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                IpAddress ipAddress = new IpAddress(input);
                return input.contains(ipPart) || ipAddress.isInRange(ipPart);
            }
        });
    }

    private void validateNamespacedListId(String id) {
        if (StringUtils.isEmpty(id) || !id.matches("^[-a-zA-Z0-9_' ]+$")) {
            throw new ValidationRuntimeException("Name is invalid");
        }
    }
}

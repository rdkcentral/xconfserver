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
package com.comcast.apps.dataaccess.acl;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Provides basic notion of access control based on ideas of UNIX ACL implementation
 * where permissions are defined separately for owner, group and everyone else.
 * In this implementation notion of group is swapped for 'trusted groups' as they appear for ApplicationDiscovery
 */
@Component
public final class AccessControlManager {

    /**
     * Defines authentication bean used to identify user trying to get access to any entity managed by AccessControlManager
     */
    public static final class AuthInfo {
        private final String ownerGroup;
        private final Set<String> userGroups;

        public AuthInfo(String ownerGroup, Set<String> userGroups) {
            this.ownerGroup = ownerGroup;
            this.userGroups = userGroups;
        }


    }

    /**
     * Defines default permission state when no ACL is defined for entry and no CF level ACL present either
     * if permissive full access is granted by default and everything is restricted otherwise
     */
    public enum AccessControlPolicy {
        PERMISSIVE(0xffff),
        RESTRICTIVE(0);

        private int permissions;

        AccessControlPolicy(int permissions) {
            this.permissions = permissions;
        }

        public int permissions() {
            return permissions;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlManager.class);
    public static final String ACL_CF_PREFIX = "_ACL";
    private static final Long CF_ACCESSCONTROL_KEY = 0L;


    private static final AccessControlPolicy defaultAccessControlPolicy = AccessControlManager.AccessControlPolicy.RESTRICTIVE; // todo make configurable

    /**
     * User interface to AccessControl. Any actions involving accessControl are to be done via this interface so that
     * actual underlying DAO is not to be exposed.
     */
    public interface AccessControlled {

        /**
         * Permission type to be used with setPermission methods for either CF or particular key
         */
        enum PermissionType {
            PERMISSION_TYPE_OWNER,
            PERMISSION_TYPE_GROUP,
            PERMISSION_TYPE_EVERYONE
        }

        boolean canRead(final Object rowKey);

        boolean canWrite(final Object rowKey);

        boolean canReadCF();

        boolean canWriteCF();

        void setKeyOwner(final String ownerId, final Object rowKey);

        void setCFOwner(final String ownerId);

        /**
         * Gets ACL for given key if exists
         *
         * @param rowKey
         * @return cloned copy of ACL so that no changes to it are propagated back to actual stored thing
         */
        AccessControlInfo getAclForKey(final Object rowKey);

        AccessControlInfo getAclForCF();

        void addCFTrustedGroup(final String groupId);

        void removeCFTrustedGroup(final String groupId);

        void setCFTrustedGroups(final Set<String> groups);

        void addTrustedGroupForKey(final Object rowKey, final String groupId);

        void removeTrustedGroupForKey(final Object rowKey, final String groupId);

        void setTrustedGroupsForKey(final Object rowKey, final Set<String> groups);

        void setCFPermission(final PermissionType permissionType, final boolean read, final boolean write);

        void setPermissionForKey(final Object key, final PermissionType permission, final boolean read, final boolean write);

        void setAclForKey(final Object key, final AccessControlInfo accessControlInfo);

        void setAclForCF(final AccessControlInfo accessControlInfo);

        void dropAclForKey(final Object key);

        void dropAclForCF();

    }

    /**
     * Creates an implementation of {@link AccessControlManager.AccessControlled} that manages access control
     * for a CF given by name with given {@link com.comcast.apps.dataaccess.acl.AccessControlManager.AuthInfo}
     *
     * @param aclDao dao to access acl objects
     * @param auth authentication to use
     */
    public static AccessControlled getAccessControlledImpl(
            final CachedSimpleDao<Long, AccessControlInfo> aclDao, final Set<String> allTrustedGroups, final AuthInfo auth) {

        final AccessControlInfo cfLevelACL = aclDao.getOne(CF_ACCESSCONTROL_KEY);

        return new AccessControlled() {

            @Override
            public boolean canRead(Object key) {
                return checkPermission(key, AccessControlInfo.PERM_READ);
            }

            @Override
            public boolean canWrite(Object key) {
                return checkPermission(key, AccessControlInfo.PERM_WRITE);
            }

            @Override
            public boolean canReadCF() {
                return checkPermission(null, AccessControlInfo.PERM_READ);
            }

            @Override
            public boolean canWriteCF() {
                return checkPermission(null, AccessControlInfo.PERM_WRITE);
            }

            @Override
            public void setKeyOwner(String ownerId, Object key) {
                setKeyOwner(ownerId, genericKeyToAclKey(key));
            }

            @Override
            public void setCFOwner(String ownerId) {
                setKeyOwner(ownerId, CF_ACCESSCONTROL_KEY);
            }

            @Override
            public AccessControlInfo getAclForKey(Object key) {
                return aclDao.getOne(genericKeyToAclKey(key));
            }

            @Override
            public AccessControlInfo getAclForCF() {
                return aclDao.getOne(CF_ACCESSCONTROL_KEY);
            }

            @Override
            public void addCFTrustedGroup(String groupId) {
                addTrustedGroupForKey(CF_ACCESSCONTROL_KEY, groupId);
            }

            @Override
            public void removeCFTrustedGroup(String groupId) {
                removeTrustedGroupForKey(CF_ACCESSCONTROL_KEY, groupId);
            }

            @Override
            public void setCFTrustedGroups(Set<String> groups) {
                   setTrustedGroupsForKey(CF_ACCESSCONTROL_KEY, groups);
            }

            @Override
            public void addTrustedGroupForKey(Object key, String groupId) {
                addTrustedGroupForKey(genericKeyToAclKey(key), groupId);
            }

            @Override
            public void removeTrustedGroupForKey(Object rowKey, String groupId) {
                 removeTrustedGroupForKey(genericKeyToAclKey(rowKey), groupId);
            }

            @Override
            public void setTrustedGroupsForKey(Object rowKey, Set<String> groups) {
                 setTrustedGroupsForKey(genericKeyToAclKey(rowKey),groups);
            }

            @Override
            public void setCFPermission(PermissionType permissionType, boolean read, boolean write) {
                setPermissionForKey(CF_ACCESSCONTROL_KEY, permissionType, booleanPermissionsToBitmask(read, write));
            }

            @Override
            public void setPermissionForKey(Object key, PermissionType permission, boolean read, boolean write) {
                setPermissionForKey(genericKeyToAclKey(key), permission, booleanPermissionsToBitmask(read, write));
            }

            @Override
            public void setAclForKey(Object key, AccessControlInfo acl) {
                writeAclUnchecked(genericKeyToAclKey(key), acl);
            }

            @Override
            public void setAclForCF(AccessControlInfo acl) {
                writeAclUnchecked(CF_ACCESSCONTROL_KEY, acl);
            }

            @Override
            public void dropAclForKey(Object key) {
                aclDao.deleteOne(genericKeyToAclKey(key));
            }

            @Override
            public void dropAclForCF() {
                aclDao.deleteOne(CF_ACCESSCONTROL_KEY);
            }

            private void removeTrustedGroupForKey(final Long key, final String groupId) {
                final AccessControlInfo acl = aclDao.getOne(key);
                if (acl == null || acl.getTrustedGroups() == null || !acl.getTrustedGroups().contains(groupId)) return;
                acl.getTrustedGroups().remove(groupId);
                writeAclUnchecked(key, acl);
            }

            private void setTrustedGroupsForKey(final Long key, final Set<String> groupIds) {
                Preconditions.checkArgument(groupIds!=null);

                final AccessControlInfo acl = getOrCreateAclForKey(key);
                if(acl.getTrustedGroups() == null) {
                    acl.setGroupPermissions(AccessControlInfo.PERM_READ);
                }
                acl.setTrustedGroups(groupIds);
                writeAclUnchecked(key, acl);
            }

            private void addTrustedGroupForKey(final Long key, final String groupId) {
                final AccessControlInfo acl = getOrCreateAclForKey(key);
                final Set<String> groups = Optional.fromNullable(acl.getTrustedGroups()).or(new HashSet<String>());
                groups.add(groupId);
                setTrustedGroupsForKey(key,groups);
            }

            private Integer booleanPermissionsToBitmask(final boolean read, final boolean write) {
                return !read && !write ?
                        0
                        : read && write ?
                        AccessControlInfo.PERM_READ | AccessControlInfo.PERM_WRITE
                        : read ?
                        AccessControlInfo.PERM_READ
                        : AccessControlInfo.PERM_WRITE;
            }

            private void setPermissionForKey(final Long key, final PermissionType permissionType, final Integer permission) {
                final AccessControlInfo acl = getOrCreateAclForKey(key);
                switch (permissionType) {
                    case PERMISSION_TYPE_OWNER:
                        acl.setOwnerPermissions(permission);
                        break;
                    case PERMISSION_TYPE_GROUP:
                        acl.setGroupPermissions(permission);
                        break;
                    case PERMISSION_TYPE_EVERYONE:
                        acl.setOthersPermissions(permission);
                        break;
                }
                writeAclUnchecked(key, acl);
            }

            private void setKeyOwner(final String ownerId, final Long key) {
                AccessControlInfo acl = aclDao.getOne(key);
                if (acl == null) {
                    acl = new AccessControlInfo();
                    acl.setOwnerPermissions(acl.getOwnerPermissions() | AccessControlInfo.PERM_READ | AccessControlInfo.PERM_WRITE);
                    acl.setTrustedGroups(new HashSet<String>());
                }
                acl.setOwnerId(ownerId);
                writeAclUnchecked(key, acl);
            }

            private int getEffectivePermissionsforACL(final AccessControlInfo acl) {
                if (auth.userGroups.contains(acl.getOwnerId())) {
                    return acl.getOwnerPermissions();
                } else if (!Sets.intersection(allTrustedGroups, auth.userGroups).isEmpty()) {
                    if (!Sets.intersection(auth.userGroups, Objects.firstNonNull(acl.getTrustedGroups(), new HashSet<String>())).isEmpty() ||
                            (cfLevelACL != null && !Sets.intersection(auth.userGroups, Objects.firstNonNull(cfLevelACL.getTrustedGroups(), new HashSet<String>())).isEmpty())) {

                        return acl.getGroupPermissions();
                    } else if (acl.getGroupPermissions() == (AccessControlInfo.PERM_READ | AccessControlInfo.PERM_WRITE)) {
                        return AccessControlInfo.PERM_WRITE;
                    } else if (Sets.intersection(allTrustedGroups, auth.userGroups).size() == auth.userGroups.size()) {
                        return 0;
                    }
                }
                return acl.getOthersPermissions();
            }

            private long genericKeyToAclKey(final Object key) {
                final HashCode hashcode = Hashing.sipHash24().hashString(JsonUtil.toJson(key), Charsets.UTF_8);
                return hashcode.asLong();
            }

            private AccessControlInfo getOrCreateAclForKey(final Object key) {
                return getOrCreateAclForKey(genericKeyToAclKey(key));
            }

            private AccessControlInfo getOrCreateAclForKey(final Long key) {
                final AccessControlInfo acl = aclDao.getOne(key);
                if (acl == null) {
                    return createAcl();
                }
                return acl;
            }

            private AccessControlInfo createAcl() {
                final AccessControlInfo acl = new AccessControlInfo();
                acl.setOwnerId(auth.ownerGroup);
                acl.setOwnerPermissions(AccessControlInfo.PERM_READ | AccessControlInfo.PERM_WRITE);
                acl.setTrustedGroups(new HashSet<>());
                return acl;
            }

            private boolean checkPermission(final Object key, final int permissionToCheck) {
                AccessControlInfo keyACL = null;
                if (key != null) {
                    keyACL = aclDao.getOne(genericKeyToAclKey(key));
                }
                if (keyACL == null) {
                    keyACL = cfLevelACL;
                }
                if (keyACL == null) {
                    return defaultAccessControlPolicy.equals(AccessControlPolicy.PERMISSIVE);
                } else {
                    return ((getEffectivePermissionsforACL(keyACL) & permissionToCheck) == permissionToCheck);
                }
            }

            private void writeAclUnchecked(final Long key, final AccessControlInfo acl) {
                aclDao.setOne(key, acl);
            }
        };
    }
}

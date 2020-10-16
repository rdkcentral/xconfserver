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
 * Created: 6/3/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.AuxFreeArgType;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddressGroup;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.Time;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardFreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArg;
import com.comcast.apps.hesperius.ruleengine.main.api.FreeArgType;
import com.comcast.apps.hesperius.ruleengine.main.api.Operation;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.apps.dataaccess.annotation.CF;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.CfNames;
import com.comcast.xconf.StbContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation.*;

/**
 * Rules migrated from XCONF DCM project (firmware part) to new rule engine
 */
@CF(cfName = CfNames.Firmware.FIRMWARE_RULE)
public class FirmwareRule extends Rule implements IPersistable, Comparable<FirmwareRule> {

    public final static FreeArg MAC = new FreeArg(AuxFreeArgType.MAC_ADDRESS, StbContext.ESTB_MAC);
    public final static FreeArg STRING = new FreeArg(StandardFreeArgType.STRING, StbContext.ESTB_MAC);
    public final static FreeArg IP = new FreeArg(AuxFreeArgType.IP_ADDRESS, StbContext.IP_ADDRESS);
    public final static FreeArg VERSION = new FreeArg(StandardFreeArgType.STRING, StbContext.FIRMWARE_VERSION);
    public final static FreeArg ENV = new FreeArg(StandardFreeArgType.STRING, StbContext.ENVIRONMENT);
    public final static FreeArg MODEL = new FreeArg(StandardFreeArgType.STRING, StbContext.MODEL);
    public final static FreeArg TIME_ZONE = new FreeArg(StandardFreeArgType.STRING, StbContext.TIME_ZONE); // may be "UTC"
    public final static FreeArg TIME = new FreeArg(AuxFreeArgType.TIME, StbContext.TIME);
    public final static FreeArg LOCAL_TIME = new FreeArg(FreeArgType.forName("LOCAL_TIME"), StbContext.TIME);
    // required for TimeFilter. it must be added after rules matching
    public final static FreeArg FIRMWARE_DOWNLOAD_PROTOCOL = new FreeArg(StandardFreeArgType.STRING, StbContext.DOWNLOAD_PROTOCOL); // tftp or http
    public final static FreeArg REBOOT_DECOUPLED = new FreeArg(StandardFreeArgType.ANY, StbContext.REBOOT_DECOUPLED);

    private final static FixedArg<?> HTTP = FixedArg.from(FirmwareConfig.DownloadProtocol.http.toString());
    private final static FixedArg<?> UTC = FixedArg.from("UTC");

    public static final Operation IN_LIST = Operation.forName("IN_LIST");

    protected String id;
    protected String boundConfigId; // FirmwareConfigId for rules, FilterActionId for filters
    protected String name;
    protected RuleType type;
    protected Set<String> targetedModelIds; // looks like is required only for MAC_RULE
    protected List<MacAddress> macAddressList; // only for MAC_RULE


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return FirmwareConfig id for rules, FilterAction id for filters
     */
    public String getBoundConfigId() {
        return boundConfigId;
    }

    public void setBoundConfigId(String boundConfigId) {
        this.boundConfigId = boundConfigId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleType getType() {
        return type;
    }

    public void setType(RuleType type) {
        this.type = type;
    }

    public Set<String> getTargetedModelIds() {
        return targetedModelIds;
    }

    public void setTargetedModelIds(Set<String> targetedModelIds) {
        this.targetedModelIds = targetedModelIds;
    }

    public List<MacAddress> getMacAddressList() {
        return macAddressList;
    }

    public void setMacAddressList(List<MacAddress> macAddressList) {
        this.macAddressList = macAddressList;
    }

    @Override
    public Date getUpdated() {
        return null;
    }

    @Override
    public void setUpdated(Date timestamp) {
    }

    @Override
    public int getTTL(String column) {
        return 0;
    }

    @Override
    public void setTTL(String column, int value) {
    }

    @Override
    public void clearTTL() {
    }

    // TODO DCM MacRuleBean has Set<String> targetedModelIds, but it's not used in MacRule.applyMyLogic()
    public static FirmwareRule newMacRule(String macAddressList) {
        FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.MAC_RULE);
        result.setCondition(new Condition(STRING, IN_LIST, FixedArg.from(macAddressList)));
        return result;
    }

    public static FirmwareRule newIpRule(IpAddressGroup ipAddressGroup, String environment, String model) {
        final FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.IP_RULE);

        final Rule.Builder builder = Rule.Builder
                .of(new Condition(IP, IN, FixedArg.from(ipAddressGroup)))
                .and(new Condition(ENV, IS, FixedArg.from(environment)))
                .and(new Condition(MODEL, IS, FixedArg.from(model)));

        return builder.copyTo(result);
    }

    public static FirmwareRule newEnvModelRule(String environment, String model) {
        FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.ENV_MODEL_RULE);

        final Rule.Builder builder = Builder
                .of(new Condition(ENV, IS, FixedArg.from(environment)))
                .and(new Condition(MODEL, IS, FixedArg.from(model)));

        return builder.copyTo(result);
    }

    public static FirmwareRule newDownloadLocationFilter(IpAddressGroup ipAddressGroup, Set<String> environments, Set<String> models) {
        FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.DOWNLOAD_LOCATION_FILTER);

        Builder filterBuilder = Builder.create();

        if (ipAddressGroup != null) {
            filterBuilder.and(new Condition(IP, IN, FixedArg.from(ipAddressGroup)));
        }

        if (CollectionUtils.isNotEmpty(environments)) {
            filterBuilder.and(new Condition(ENV, IN, FixedArg.from(environments)));
        }

        if (CollectionUtils.isNotEmpty(models)) {
            filterBuilder.and(new Condition(MODEL, IN, FixedArg.from(models)));
        }

        return filterBuilder.copyTo(result);
    }

    /**
     * each param may be null or empty collection
     */
    public static FirmwareRule newRebootImmediatelyFilter(Set<IpAddressGroup> ipAddressGroups, Set<MacAddress> macAddresses,
                                                          Set<String> environments, Set<String> models) {
        FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.REBOOT_IMMEDIATELY_FILTER);

        final Builder riFilterBuilder = Builder.create();

        if (!CollectionUtils.isEmpty(ipAddressGroups)) {

            final Builder inOneOfGroupsRuleBuilder = Builder.create();

            for (IpAddressGroup group : ipAddressGroups) {
                inOneOfGroupsRuleBuilder.or(new Condition(IP, IN, FixedArg.from(group)));
            }

            riFilterBuilder.and(inOneOfGroupsRuleBuilder.build());
        }
        if (!CollectionUtils.isEmpty(macAddresses)) {

            //TODO express this via ns lists
            riFilterBuilder.and(new Condition(MAC, IN, FixedArg.from(macAddresses)));
        }
        if (!CollectionUtils.isEmpty(environments)) {

            //TODO express this via ns lists
            riFilterBuilder.and(new Condition(ENV, IN, FixedArg.from(environments)));
        }
        if (!CollectionUtils.isEmpty(models)) {

            //TODO express this via ns lists
            riFilterBuilder.and(new Condition(MODEL, IN, FixedArg.from(models)));
        }

        return riFilterBuilder.copyTo(result);
    }

    public static FirmwareRule newIpFilter(IpAddressGroup ipAddressGroup) {
        FirmwareRule result = new FirmwareRule();
        result.setType(RuleType.IP_FILTER);
        result.setCondition(new Condition(IP, IN, FixedArg.from(ipAddressGroup)));
        return result;
    }

    /**
     * TODO how stb input capabilities.rebootDecoupled is passed to context? now assuming context contains "rebootDecoupled" key
     * <p/>
     * filter description
     * <p/>
     * do not block if
     * flag neverBlockRebootDecoupled is true and context rebootDecoupled is true
     * or flag neverBlockHttpDownload is true and download protocol for firmware is true, see {@link #FIRMWARE_DOWNLOAD_PROTOCOL}
     * or filter time in UTC format and context input in local
     * or filter time in local format and context input in UTC
     * or filter environment + model equals input environment + model
     * or in ipWhiteList
     * or not in time range (start, end)
     * <p/>
     * otherwise block
     *
     * @return true if firmware output should be blocked, false otherwise
     */
    public static FirmwareRule newTimeFilter(boolean neverBlockRebootDecoupled, boolean neverBlockHttpDownload,
                                             boolean isLocalTime, String environment, String model, IpAddressGroup ipWhiteList,
                                             Time start, Time end) {

        FirmwareRule doNotBlock = new FirmwareRule();
        doNotBlock.setType(RuleType.TIME_FILTER);
        final Builder doNotBlockBuilder = Builder.create();

        if (neverBlockRebootDecoupled) { // TODO exists,or true context value, or something else? see method todo
            doNotBlockBuilder.and(new Condition(REBOOT_DECOUPLED, EXISTS, null));
        }
        if (neverBlockHttpDownload) {
            doNotBlockBuilder.or(new Condition(FIRMWARE_DOWNLOAD_PROTOCOL, IS, HTTP));
        }
        if (environment != null && model != null) {
            final Builder doNotBlockThisEnvModelBuilder = Builder
                    .of(new Condition(ENV, IS, FixedArg.from(environment)))
                    .and(new Condition(MODEL, IS, FixedArg.from(model)));
            doNotBlockBuilder.or(doNotBlockThisEnvModelBuilder.build());
        }
        if (ipWhiteList != null) {
            doNotBlockBuilder.or(new Condition(IP, IN, FixedArg.from(ipWhiteList)));
        }
        {
            final Condition isUTC = new Condition(TIME_ZONE, IS, UTC);
            final Condition ltEnd = new Condition(LOCAL_TIME, LTE, FixedArg.from(end.toString()));
            final Builder doNotBlockIfNOTInTimeRangeBuilder = isLocalTime ? Builder.of(Builder.not(isUTC)) : Builder.of(isUTC);
            doNotBlockIfNOTInTimeRangeBuilder.and(new Condition(LOCAL_TIME, GTE, FixedArg.from(start.toString())));
            if (start.compareTo(end) < 0) {
                doNotBlockIfNOTInTimeRangeBuilder.and(ltEnd);
            } else {
                doNotBlockIfNOTInTimeRangeBuilder.or(ltEnd);
            }
            doNotBlockBuilder.or(Builder.not(doNotBlockIfNOTInTimeRangeBuilder.build()));
        }

        return not(doNotBlockBuilder.copyTo(doNotBlock));
    }

    // added for clarification. be careful, modifies input
    private static FirmwareRule not(FirmwareRule r) {
        r.setNegated(!r.isNegated());
        return r;
    }

    @Override
    public int compareTo(FirmwareRule that) {
        return this.type.ordinal() - that.type.ordinal();
    }

    @Override
    public String toString() {
        return "FirmwareRule{" +
                "id='" + id + '\'' +
                ", boundConfigId='" + boundConfigId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", targetedModelIds=" + targetedModelIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FirmwareRule)) return false;
        if (!super.equals(o)) return false;

        FirmwareRule that = (FirmwareRule) o;

        if (boundConfigId != null ? !boundConfigId.equals(that.boundConfigId) : that.boundConfigId != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (macAddressList != null ? !macAddressList.equals(that.macAddressList) : that.macAddressList != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (targetedModelIds != null ? !targetedModelIds.equals(that.targetedModelIds) : that.targetedModelIds != null)
            return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (boundConfigId != null ? boundConfigId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (targetedModelIds != null ? targetedModelIds.hashCode() : 0);
        result = 31 * result + (macAddressList != null ? macAddressList.hashCode() : 0);
        return result;
    }

    public static enum RuleType {
        // rules associated with FirmwareConfig class used as keys for "FirmwareConfig" CF
        MAC_RULE("MacRule"),
        IP_RULE("IpRule"),
        ENV_MODEL_RULE("EnvModelRule"),
        // for legacy support
        VERSION_RULE("VersionRule"),
        // rules which are used to modify FirmwareConfig. also DownloadLocationRoundRobinFilter class
        DOWNLOAD_LOCATION_FILTER("DownloadLocationFilter"),
        REBOOT_IMMEDIATELY_FILTER("RebootImmediatelyFilter"),
        // rules which are used to block FirmwareConfig. also PercentFilter class
        IP_FILTER("IpFilter"),
        TIME_FILTER("TimeFilterRule");

        private String friendlyName;

        private RuleType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }
}

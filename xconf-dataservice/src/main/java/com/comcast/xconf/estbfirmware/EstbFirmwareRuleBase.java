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
 * Created: 7/8/14
 */
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.apps.hesperius.ruleengine.domain.RuleUtils;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.standard.StandardOperation;
import com.comcast.apps.hesperius.ruleengine.main.api.FixedArg;
import com.comcast.apps.hesperius.ruleengine.main.impl.Condition;
import com.comcast.apps.hesperius.ruleengine.main.impl.Rule;
import com.comcast.xconf.ConfigNames;
import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.GenericNamespacedListTypes;
import com.comcast.xconf.StbContext;
import com.comcast.xconf.estbfirmware.evaluation.DownloadLocationRoundRobinFilter;
import com.comcast.xconf.estbfirmware.evaluation.EvaluationResult;
import com.comcast.xconf.estbfirmware.factory.RuleFactory;
import com.comcast.xconf.evaluators.RuleProcessorFactory;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.*;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import com.comcast.xconf.util.RuleUtil;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;

import static com.comcast.xconf.estbfirmware.TemplateNames.ACTIVATION_VERSION;
import static com.comcast.xconf.estbfirmware.TemplateNames.ENV_MODEL_RULE;
import static com.comcast.xconf.firmware.ApplicableAction.Type.DEFINE_PROPERTIES_TEMPLATE;

@Component
public class EstbFirmwareRuleBase {

    private static final Logger log = LoggerFactory.getLogger(EstbFirmwareRuleBase.class);

    public static final String PERCENT_FILTER_NAME = "PercentFilter";
    public static final String FIRMWARE_SOURCE = "firmwareVersionSource";

    @Autowired
    private CachedSimpleDao<String, FirmwareRule> firmwareRuleDao;
    @Autowired
    private CachedSimpleDao<String, FirmwareRuleTemplate> firmwareRuleTemplateDao;
    @Autowired
    private CachedSimpleDao<String, FirmwareConfig> firmwareConfigDAO;
    @Autowired
    private CachedSimpleDao<String, SingletonFilterValue> singletonFilterValueDAO;
    @Autowired
    private IpRuleService ipRuleService;
    @Autowired
    private RuleProcessorFactory ruleProcessorFactory;

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    private boolean driAlwaysReply = true;

    private String driStateIdentifiers = "P-DRI,B-DRI";

    public EvaluationResult eval(EstbFirmwareContext context, String applicationType) {
        EvaluationResult result = new EvaluationResult();
        Multimap<String, FirmwareRule> rules = sort(Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values()), applicationType);

        FirmwareRule matchedRule = findMatchedRule(rules, ApplicableAction.Type.RULE_TEMPLATE, context.getProperties(), context.convert().getBypassFilters());
        if (matchedRule == null) {
            log.info("no rules matched context: " + context);
            result.setDescription("No rules matched");
            return result;
        }
        result.setMatchedRule(matchedRule);

        FirmwareConfigFacade firmwareConfig = null;
        String boundConfigId = getBoundConfigId(context, matchedRule, result.getAppliedVersionInfo());
        if (StringUtils.isNotBlank(boundConfigId)) { // check for no-op rules
            FirmwareConfig config = firmwareConfigDAO.getOne(boundConfigId);
            if (config == null) {
                log.warn("no config found by {}: {} boundConfigId: {}, it was deleted", matchedRule.getType(), matchedRule.getName(), boundConfigId);
                result.setDescription("no config found by id: " + boundConfigId);
                return result;
            } else if (!ApplicationType.equals(config.getApplicationType(), matchedRule.getApplicationType())) {
                log.error("ApplicationTypeMatchingException: Application types of FirmwareConfig " + config.getDescription() + " and FirmwareRule " + matchedRule + " do not match");
                result.setDescription("no config found by id: " + boundConfigId);
                return result;
            } else {
                firmwareConfig = new FirmwareConfigFacade(config);
                result.getAppliedVersionInfo().put(FIRMWARE_SOURCE, matchedRule.getType());
            }
        } else if (!matchedRule.isNoop()) {
            log.info("output is blocked by distribution percent in " + matchedRule.getApplicableAction());
            result.setBlocked(true);
            result.getAppliedFilters().add(matchedRule.getApplicableAction());
            result.setDescription("output is blocked by distribution percent in rule action");
            return result;
        } else {
            log.info("rule {}: {} is noop: {} ", matchedRule.getType(), matchedRule.getName(), matchedRule.getId());
            result.setDescription("rule is noop: {} " + matchedRule.getId());
            return result;
        }
        result.setFirmwareConfig(firmwareConfig);
        boolean blocked = doFilters(context, applicationType, rules, result);
        if (driAlwaysReply) {
            blocked = checkForDRIState(context, firmwareConfig, blocked);
        }

        result.setBlocked(blocked);
        if (blocked) {
            result.setDescription("output is blocked by filter");
        }
        return result;
    }

    private String getBoundConfigId(EstbFirmwareContext context, FirmwareRule firmwareRule, Map<String, String> appliedVersionInfo) {
        RuleAction ruleAction = (RuleAction) firmwareRule.getApplicableAction();
        if (ruleAction == null) {
            return null;
        }
        if (!TemplateNames.ENV_MODEL_RULE.equals(firmwareRule.getTemplateId()) || !ruleAction.isActive() || isInWhitelist(context, ruleAction.getWhitelist())) {
            return extractAnyPresentConfig(ruleAction);
        }
        return extractConfigFromAction(context.convert(), ruleAction, appliedVersionInfo);
    }

    private boolean isInWhitelist(EstbFirmwareContext context, String whitelist) {
        return StringUtils.isNotBlank(whitelist) && ruleProcessorFactory.get().evaluate(RuleFactory.newIpFilter(whitelist), context.getProperties());
    }

    private String extractConfigFromAction(EstbFirmwareContext.Converted context, RuleAction ruleAction, Map<String, String> appliedVersionInfo) {
        boolean firmwareVersionIsAbsentInFilter = ruleAction.getFirmwareVersions() == null
                || context.getFirmwareVersion() == null
                || !ruleAction.getFirmwareVersions().contains(context.getFirmwareVersion());

        if (ruleAction.isFirmwareCheckRequired() && firmwareVersionIsAbsentInFilter) {
            if (ruleAction.isRebootImmediately()) {
                context.getForceFilters().add(TemplateNames.REBOOT_IMMEDIATELY_FILTER);
            }
            context.getBypassFilters().add(TemplateNames.TIME_FILTER);

            FirmwareConfig config = getFirmwareConfig(ruleAction.getIntermediateVersion());
            if (config != null && !StringUtils.equals(context.getFirmwareVersion(), config.getFirmwareVersion())) {
                // return IntermediateVersion firmware config
                appliedVersionInfo.put(FIRMWARE_SOURCE, "IV,doesntMeetMinCheck");
                return ruleAction.getIntermediateVersion();
            } else {
                config = getFirmwareConfig(ruleAction.getConfigId()); // lkg config

                if (config != null) {
                    // return LKG firmware config
                    appliedVersionInfo.put(FIRMWARE_SOURCE, "LKG,doesntMeetMinCheck");
                    return ruleAction.getConfigId();
                }
            }
            return extractAnyPresentConfig(ruleAction);
        }

        FirmwareConfig config = getFirmwareConfig(ruleAction.getConfigId());
        if (config != null) {
            appliedVersionInfo.put(FIRMWARE_SOURCE, "LKG,meetMinCheck");
        }

        if (ruleAction.isUseAccountPercentage() && StringUtils.isBlank(context.getAccountId())) {
            return ruleAction.getConfigId();
        }

        if (ruleAction.getConfigEntries() != null) {
            double currentPercent = 0;
            Object source = getSource(context, ruleAction);
            for (RuleAction.ConfigEntry entry : ruleAction.getConfigEntries()) {
                Double percentage = entry.getPercentage();
                Double startPercentRange = entry.getStartPercentRange();
                Double endPercentRange = entry.getEndPercentRange();
                if (startPercentRange != null && startPercentRange >= 0
                        && endPercentRange != null && endPercentRange >= 0) {
                    if(!RuleUtils.fitsPercent(source, startPercentRange)
                            && RuleUtils.fitsPercent(source, endPercentRange)) {
                        appliedVersionInfo.put(FIRMWARE_SOURCE, "MultipleVersionDistribution");
                        return entry.getConfigId();
                    }
                } else if (percentage != null && percentage > 0) {
                    currentPercent += percentage;
                    if (RuleUtils.fitsPercent(source, currentPercent)) {
                        appliedVersionInfo.put(FIRMWARE_SOURCE, "MultipleVersionDistribution");
                        return entry.getConfigId();
                    }
                }
            }
        }

        return ruleAction.getConfigId();
    }

    private Object getSource(EstbFirmwareContext.Converted context, RuleAction ruleAction) {
        if (ruleAction.isUseAccountPercentage()) {
            return context.getAccountId();
        }
        return context.getEstbMac() != null ? context.getEstbMac() : context;
    }

    private String extractAnyPresentConfig(RuleAction ruleAction) {
        if (ruleAction.getConfigEntries() != null) {
            Iterator<RuleAction.ConfigEntry> iterator = ruleAction.getConfigEntries().iterator();
            if (iterator.hasNext()) {
                return iterator.next().getConfigId();
            }
        }
        return ruleAction.getConfigId();
    }

    private FirmwareConfig getFirmwareConfig(String id) {
        return StringUtils.isNotBlank(id) ? firmwareConfigDAO.getOne(id): null;
    }

    private List<FirmwareRule> findMatchedRules(Multimap<String, FirmwareRule> rules, ApplicableAction.Type templateType,
                                                Map<String, String> contextMap, Set<String> bypassFilters, boolean isSingle, boolean reverse) {
        List<FirmwareRule> results = new ArrayList<>();
        List<FirmwareRuleTemplate> templates = getSortedTemplate(templateType, reverse);
        for (FirmwareRuleTemplate template : templates) {
            String ruleType = template.getId();
            if (bypassFilters.contains(ruleType)) {
                continue;
            }

            Collection<FirmwareRule> firmwareRules = rules.get(ruleType);

            Collection<FirmwareRule> sortedRules = TemplateNames.ENV_MODEL_RULE.equals(ruleType) || ACTIVATION_VERSION.equals(ruleType) ?
                    sortByConditionsSize(firmwareRules) : firmwareRules;

            for (FirmwareRule firmwareRule : sortedRules) {
                if (firmwareRule.isActive() && ruleProcessorFactory.get().evaluate(firmwareRule.getRule(), contextMap)) {
                    results.add(firmwareRule);
                    if (CollectionUtils.isNotEmpty(template.getByPassFilters())) {
                        bypassFilters.addAll(template.getByPassFilters());
                    }
                    if (isSingle) {
                        return results;
                    }
                }
            }
        }
        return results;
    }

    private FirmwareRule findMatchedRule(Multimap<String, FirmwareRule> rules, ApplicableAction.Type templateType, Map<String, String> contextMap, Set<String> bypassFilters) {
        List<FirmwareRule> matchedRules = findMatchedRules(rules, templateType, contextMap, bypassFilters, true, false);
        return matchedRules.isEmpty() ? null : matchedRules.get(0);
    }

    /**
     * Evaluate rules and collect properties from matched rules. All matched rules will be added into applied filters list
     * @param rules multimap with all firmware rules
     * @param templateType template type
     * @param context request context
     * @param bypassFilters filters to exclude from evaluation
     * @param appliedFilters list to collect all applied filters
     * @return map with properties defined in matching rules
     */
    private Map<String, Object> applyMatchedFilters(Multimap<String, FirmwareRule> rules, ApplicableAction.Type templateType,
                                                    Map<String, String> context, Set<String> bypassFilters, List<Object> appliedFilters) {
        Map<String, Object> map = new HashMap<>();
        List<FirmwareRule> matchedRules = findMatchedRules(rules, templateType, context, bypassFilters, false, true);
        boolean matchedActivationVersion = false;
        for (FirmwareRule firmwareRule : matchedRules) {
            if (firmwareRule.getApplicableAction() instanceof DefinePropertiesAction) {
                DefinePropertiesAction action = (DefinePropertiesAction) firmwareRule.getApplicableAction();
                FirmwareRuleTemplate template = firmwareRuleTemplateDao.getOne(firmwareRule.getType(), false);
                String firmwareVersion = context.get(StbContext.FIRMWARE_VERSION);
                if (!matchedActivationVersion && ACTIVATION_VERSION.equals(template.getId()) && StringUtils.isNotBlank(firmwareVersion)) {
                    matchedActivationVersion = true;
                    if (CollectionUtils.isNotEmpty(action.getFirmwareVersions()) && action.getFirmwareVersions().contains(firmwareVersion)
                            || CollectionUtils.isNotEmpty(action.getFirmwareVersionRegExs()) && matchFirmwareVersionRegEx(action.getFirmwareVersionRegExs(), firmwareVersion)) {
                        applyDefinePropertiesFilter(template, firmwareRule, action, map, appliedFilters);
                    } else {
                        map.put(ConfigNames.REBOOT_IMMEDIATELY, true);
                    }
                } else if (!ACTIVATION_VERSION.equals(template.getId())) {
                    applyDefinePropertiesFilter(template, firmwareRule, action, map, appliedFilters);
                    if (CollectionUtils.isNotEmpty(action.getByPassFilters())) {
                        bypassFilters.addAll(action.getByPassFilters());
                    }
                }
            }
        }
        return map;
    }

    private void applyDefinePropertiesFilter(FirmwareRuleTemplate template, FirmwareRule firmwareRule, DefinePropertiesAction action, Map<String, Object> properties, List<Object> appliedFilters) {
        properties.putAll(convertProperties(template, action.getProperties()));
        appliedFilters.add(firmwareRule);
    }

    private boolean matchFirmwareVersionRegEx(Set<String> regExs, String firmwareVersion) {
        for (String regEx : regExs) {
            if (Pattern.matches(regEx, firmwareVersion)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> convertProperties(FirmwareRuleTemplate template, Map<String, String> properties) {
        Map<String, Object> converted = new HashMap<>();
        ApplicableAction templateAction = template.getApplicableAction();
        if (templateAction instanceof DefinePropertiesTemplateAction) {
            Map<String, DefinePropertiesTemplateAction.PropertyValue> templateProperties = ((DefinePropertiesTemplateAction) templateAction).getProperties();
            for (String key : properties.keySet()) {
                DefinePropertiesTemplateAction.PropertyValue propertyValue = templateProperties.get(key);
                String value = properties.get(key);
                Object valueObject = (propertyValue == null) ? value : convertBasedOnValidationType(propertyValue.getValidationTypes(), value);
                converted.put(key, valueObject);
            }
        }
        return converted;
    }

    private Object convertBasedOnValidationType(List<DefinePropertiesTemplateAction.ValidationType> validationTypes, String value) {
        if (validationTypes != null && validationTypes.size() == 1) {
            switch (validationTypes.get(0)) {
                case NUMBER:
                case PERCENT:
                case PORT:
                    return Long.valueOf(value);
                case BOOLEAN:
                    return Boolean.valueOf(value);
            }
        }
        return value;
    }

    /**
     * @return true if firmware output must be blocked, false if must be returned
     */
    private boolean doFilters(EstbFirmwareContext context, String applicationType, Multimap<String, FirmwareRule> rules, EvaluationResult evaluationResult) {
        EstbFirmwareContext.Converted convertedContext = context.convert();
        Set<String> bypassFilters = convertedContext.getBypassFilters();
        List<Object> appliedFilters = evaluationResult.getAppliedFilters();

        FirmwareConfigFacade firmwareConfig = evaluationResult.getFirmwareConfig();
        String filterId = getRoundRobinIdByApplication(applicationType);
        DownloadLocationRoundRobinFilterValue downloadLocationRoundRobinFilterValue =
                (DownloadLocationRoundRobinFilterValue) singletonFilterValueDAO.getOne(filterId);
        if (downloadLocationRoundRobinFilterValue != null) {
            if (DownloadLocationRoundRobinFilter.filter(firmwareConfig, downloadLocationRoundRobinFilterValue, convertedContext)) {
                appliedFilters.add(downloadLocationRoundRobinFilterValue);
            }
        }

        Map<String, String> contextProperties = context.getProperties();

        // download protocol should be setup in download location filter
        contextProperties.put(StbContext.DOWNLOAD_PROTOCOL, firmwareConfig.getFirmwareDownloadProtocol());
        contextProperties.put(StbContext.MATCHED_RULE_TYPE, evaluationResult.getMatchedRule().getType());
        Map<String, Object> map;
        if (isPercentFilter(evaluationResult.getMatchedRule())) {
            map = applyMatchedFilters(rules, DEFINE_PROPERTIES_TEMPLATE, contextProperties, bypassFilters, appliedFilters);
        } else {
            rules.removeAll(ACTIVATION_VERSION);
            map = applyMatchedFilters(rules, DEFINE_PROPERTIES_TEMPLATE, contextProperties, bypassFilters, appliedFilters);
        }

        firmwareConfig.putAll(map);

        // legacy: if protocol=tftp but ipv6 location is empty then read from round robing filter
        if (FirmwareConfig.DownloadProtocol.tftp.name().equals(firmwareConfig.getFirmwareDownloadProtocol())
                && StringUtils.isBlank(firmwareConfig.getIpv6FirmwareLocation())
                && downloadLocationRoundRobinFilterValue != null) {
            DownloadLocationRoundRobinFilter.setupIPv6Location(firmwareConfig, downloadLocationRoundRobinFilterValue);
        }

        // legacy: if force reboot immediately then set to true flag
        if (convertedContext.getForceFilters().contains(TemplateNames.REBOOT_IMMEDIATELY_FILTER)) {
            firmwareConfig.setRebootImmediately(true);
        }

        FirmwareRule blockingFilter = findMatchedRule(rules, ApplicableAction.Type.BLOCKING_FILTER_TEMPLATE, contextProperties, bypassFilters);
        boolean blocked = blockingFilter != null;
        if (blocked) {
            appliedFilters.add(blockingFilter);
        }
        return blocked;
    }

    /**
     * @return true if firmware output must be blocked, false if must be returned
     */
    private boolean checkForDRIState(EstbFirmwareContext context, FirmwareConfigFacade config, boolean blocked) {
        if (StringUtils.isEmpty(driStateIdentifiers) || StringUtils.isEmpty(context.getFirmwareVersion()))
            return blocked;
        String[] identifiers = driStateIdentifiers.split(",");
        for (String identifier : identifiers) {
            if (context.getFirmwareVersion().toUpperCase().contains(identifier.toUpperCase())) {
                blocked = false;
                if (config != null) {
                    config.setRebootImmediately(true);
                }
                break;
            }
        }
        return blocked;
    }

    public static List<FirmwareRule> sortByConditionsSize(Collection<FirmwareRule> rules) {
        List<FirmwareRule> firmwareRules = new ArrayList<>(rules);
        firmwareRules.sort((o1, o2) -> Integer.compare(getConditionsSize(o2.getRule()), getConditionsSize(o1.getRule())));
        return firmwareRules;
    }

    private boolean isPercentFilter(FirmwareRule firmwareRule) {
        return Objects.nonNull(firmwareRule)
                && ENV_MODEL_RULE.equals(firmwareRule.getType());
    }

    public static int getConditionsSize(Rule rule) {
        return RuleUtil.toConditions(rule).size();
    }

    private static Multimap<String, FirmwareRule> sort(Iterable<FirmwareRule> rules, String applicationType) {
        Multimap<String, FirmwareRule> result = HashMultimap.create();
        for (FirmwareRule rule : rules) {
            if (rule.getType() == null) {
                log.error("ruleType is null: " + rule);
                continue;
            }
            if (ApplicationType.equals(rule.getApplicationType(), applicationType)) {
                result.put(rule.getType(), rule);
            }
        }
        return result;
    }

    private List<FirmwareRuleTemplate> getSortedTemplate(final ApplicableAction.Type type, final boolean reverse) {
        Iterable<FirmwareRuleTemplate> templates = Optional.presentInstances(firmwareRuleTemplateDao.asLoadingCache().asMap().values());
        List<FirmwareRuleTemplate> all = Lists.newArrayList(Iterables.filter(templates, new Predicate<FirmwareRuleTemplate>() {
            @Override
            public boolean apply(FirmwareRuleTemplate input) {
                return input.getApplicableAction() != null && input.getApplicableAction().getActionType() == type;
            }
        }));
        Collections.sort(all, new Comparator<FirmwareRuleTemplate>() {
            @Override
            public int compare(FirmwareRuleTemplate o1, FirmwareRuleTemplate o2) {
                int p1 = (o1 != null && o1.getPriority() != null) ? o1.getPriority() : 0;
                int p2 = (o2 != null && o2.getPriority() != null) ? o2.getPriority() : 0;
                int result = p1 - p2;
                return reverse ? -1 * result : result;
            }
        });
        return all;
    }

    public boolean hasMinimumFirmware(EstbFirmwareContext context) {
        EvaluationResult eval = eval(context, ApplicationType.STB);
        FirmwareRule matchedRule = eval.getMatchedRule();
        if (matchedRule != null && !eval.isBlocked() && eval.getFirmwareConfig() != null
                && TemplateNames.ENV_MODEL_RULE.equals(matchedRule.getType())) {

            ApplicableAction applicableAction = matchedRule.getApplicableAction();
            if (applicableAction != null && applicableAction instanceof RuleAction) {
                RuleAction ruleAction = (RuleAction) applicableAction;

                boolean firmwareVersionIsAbsentInFilter = ruleAction.getFirmwareVersions() == null
                        || context.getFirmwareVersion() == null
                        || !ruleAction.getFirmwareVersions().contains(context.getFirmwareVersion());

                if (ruleAction.isFirmwareCheckRequired() && firmwareVersionIsAbsentInFilter) {
                    return false;
                }
            }
        }

        return true;
    }

    public BseConfiguration getBseConfiguration(final IpAddress address) {
        List<BseConfiguration.ModelFirmwareConfiguration> modelConfigs = new ArrayList<>();
        Iterable<FirmwareRule> firmwareRules = Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values());
        for (FirmwareRule firmwareRule : firmwareRules) {
            if (TemplateNames.IP_RULE.equals(firmwareRule.getType()) && !firmwareRule.isNoop() && ApplicationType.equals(firmwareRule.getApplicationType(), ApplicationType.STB)) {

                IpRuleBean ipRuleBean = ipRuleService.convertFirmwareRuleToIpRuleBean(firmwareRule);
                FirmwareConfig firmwareConfig = ipRuleBean.getFirmwareConfig();

                if (ipRuleBean.getIpAddressGroup().isInRange(address) && firmwareConfig != null) {
                    modelConfigs.add(new BseConfiguration.ModelFirmwareConfiguration(
                            ipRuleBean.getModelId(), firmwareConfig.getFirmwareFilename(), firmwareConfig.getFirmwareVersion()));
                }
            }
        }
        if (modelConfigs.isEmpty()) {
            return null;
        }
        BseConfiguration config = new BseConfiguration();
        config.setModelConfigurations(modelConfigs);

        DownloadLocationRoundRobinFilterValue downloadLocationRoundRobinFilterValue =
                (DownloadLocationRoundRobinFilterValue) singletonFilterValueDAO.getOne(DownloadLocationRoundRobinFilterValue.SINGLETON_ID);
        if (downloadLocationRoundRobinFilterValue != null) {
            String[] locations = downloadLocationRoundRobinFilterValue.getDownloadLocations();
            config.setProtocol(FirmwareConfig.DownloadProtocol.tftp.name());
            config.setLocation(locations[0]);
            config.setIpv6Location(locations[1]);
        }
        for (FirmwareRule firmwareRule : firmwareRules) {
            if (TemplateNames.DOWNLOAD_LOCATION_FILTER.equals(firmwareRule.getType())) {
                if (isIpAddressInRange(firmwareRule.getRule(), address)) {
                    setupLocations(firmwareRule, config);
                }
            }
        }
        return config;
    }

    private void setupLocations(FirmwareRule firmwareRule, BseConfiguration config) {

        DefinePropertiesAction action = (DefinePropertiesAction) firmwareRule.getApplicableAction();

        String location = action.getProperties().get(ConfigNames.FIRMWARE_LOCATION);
        String ipv6Location = action.getProperties().get(ConfigNames.IPV6_FIRMWARE_LOCATION);
        String protocol = action.getProperties().get(ConfigNames.FIRMWARE_DOWNLOAD_PROTOCOL);

        boolean useHttp = StringUtils.isNotBlank(location) && !FirmwareConfig.DownloadProtocol.tftp.toString().equals(protocol);

        if (useHttp) {
            config.setProtocol(FirmwareConfig.DownloadProtocol.http.name());
            config.setLocation(location);
            if (StringUtils.isNotBlank(ipv6Location)) {
                config.setIpv6Location(ipv6Location);
            }
        } else if (StringUtils.isNotBlank(location)) {
            config.setProtocol(FirmwareConfig.DownloadProtocol.tftp.name());
            config.setLocation(location);
            if (StringUtils.isNotBlank(ipv6Location)) {
                config.setIpv6Location(ipv6Location);
            }
        }
    }

    private boolean isIpAddressInRange(Rule rule, IpAddress address) {

        for (Condition condition : RuleUtil.toConditions(rule)) {
            FixedArg fixedArg = condition.getFixedArg();
            if (!RuleFactory.IP.equals(condition.getFreeArg()) || fixedArg == null || fixedArg.getValue() == null) {
                continue;
            }
            if (StandardOperation.IS.equals(condition.getOperation()) && fixedArg.getValue() != null) {
                return isIpInRange((String) fixedArg.getValue(), address);
            } else if (StandardOperation.IN.equals(condition.getOperation()) ) {
                for (Object ipAddressStr : ((Collection) fixedArg.getValue())) {
                    if (isIpInRange((String) ipAddressStr, address)) {
                        return true;
                    }
                }
            } else if (RuleFactory.IN_LIST.equals(condition.getOperation())) {
                String ipListId = (String) fixedArg.getValue();
                GenericNamespacedList ipList = genericNamespacedListQueriesService.getOneByType(ipListId, GenericNamespacedListTypes.IP_LIST);
                if (ipList != null) {
                    for (String ipListItem : ipList.getData()) {
                        if (isIpInRange(ipListItem, address)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isIpInRange(String ipAddressStr, IpAddress addressToCheck) {
        try {
            IpAddress ipAddress = new IpAddress(ipAddressStr);
            return ipAddress.isInRange(addressToCheck);
        } catch (Exception e) {
            log.error("Exception: addressInWhichNeedToCheck " + ipAddressStr + ", addressToCheck " + addressToCheck + " " + e.getMessage());
        }
        return false;
    }

    private String getRoundRobinIdByApplication(String applicationType) {
        if (ApplicationType.equals(ApplicationType.STB, applicationType)) {
            return DownloadLocationRoundRobinFilterValue.SINGLETON_ID;
        }
        return applicationType.toUpperCase() + "_" + DownloadLocationRoundRobinFilterValue.SINGLETON_ID;
    }

    static List<String> percentFilterTemplateNames() {
        return Arrays.asList(TemplateNames.ENV_MODEL_RULE, TemplateNames.MIN_CHECK_RULE, TemplateNames.IV_RULE);
    }

    public RunningVersionInfo getAppliedActivationVersionType(EstbFirmwareContext context, String applicationType) {
        RunningVersionInfo runningVersionInfo = new RunningVersionInfo(true, true);
        String firmwareVersion = context.getFirmwareVersion();
        Multimap<String, FirmwareRule> firmwareRules = sort(Optional.presentInstances(firmwareRuleDao.asLoadingCache().asMap().values()), applicationType);

        EvaluationResult eval = eval(context, applicationType);
        FirmwareRule matchedRule = eval.getMatchedRule();

        boolean isPercentRuleIsMatched = matchedRule != null && !eval.isBlocked() && eval.getFirmwareConfig() != null
                && TemplateNames.ENV_MODEL_RULE.equals(matchedRule.getType());

        if (isPercentRuleIsMatched && matchedRule.getApplicableAction() != null && matchedRule.getApplicableAction() instanceof RuleAction) {
            RuleAction ruleAction = (RuleAction) matchedRule.getApplicableAction();
            if (CollectionUtils.isEmpty(ruleAction.getFirmwareVersions())
                    || StringUtils.isBlank(firmwareVersion)
                    || !ruleAction.getFirmwareVersions().contains(firmwareVersion)) {
                runningVersionInfo.setHasMinimumFW(false);
            }

            FirmwareRule matchedActivationVersionRule = findMatchedRule(firmwareRules, DEFINE_PROPERTIES_TEMPLATE, ACTIVATION_VERSION,
                    context.getProperties(), context.convert().getBypassFilters());

            boolean isAmvRuleMatched = false;
            if (matchedActivationVersionRule != null) {
                DefinePropertiesAction action = (DefinePropertiesAction) matchedActivationVersionRule.getApplicableAction();
                isAmvRuleMatched = StringUtils.isNotBlank(firmwareVersion)
                        && (firmwareVersionIsMatched(firmwareVersion, action)
                        || firmwareVersionRegExIsMatched(firmwareVersion, action));
            }
            runningVersionInfo.setHasActivationMinFW(isAmvRuleMatched);
        }

        return runningVersionInfo;
    }

    private boolean firmwareVersionIsMatched(String firmwareVersion, DefinePropertiesAction action) {
        return CollectionUtils.isNotEmpty(action.getFirmwareVersions()) && action.getFirmwareVersions().contains(firmwareVersion);
    }

    private boolean firmwareVersionRegExIsMatched(String firmwareVersion, DefinePropertiesAction action) {
        return CollectionUtils.isNotEmpty(action.getFirmwareVersionRegExs()) && matchFirmwareVersionRegEx(action.getFirmwareVersionRegExs(), firmwareVersion);
    }

    private FirmwareRule findMatchedRule(Multimap<String, FirmwareRule> firmwareRules, ApplicableAction.Type actionType,
                                         String template, Map<String, String> contextMap, Set<String> bypassFilters) {
        List<FirmwareRule> matchedRules = findMatchedRules(firmwareRules, actionType,
                contextMap, bypassFilters, false, true);
        List<FirmwareRule> filteredByTemplateRules = filterByTemplate(matchedRules, template);
        return CollectionUtils.isNotEmpty(filteredByTemplateRules) ? filteredByTemplateRules.get(0) : null;
    }

    private List<FirmwareRule> filterByTemplate(final List<FirmwareRule> firmwareRules, final String templateName) {
        return Lists.newArrayList(Iterables.filter(firmwareRules, new Predicate<FirmwareRule>() {
            @Override
            public boolean apply(@Nullable FirmwareRule input) {
                return StringUtils.equals(templateName, input.getType());
            }
        }));
    }
}

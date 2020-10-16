/*******************************************************************************
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
 *******************************************************************************/
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.dataaccess.util.ExecutorUtil;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.*;
import com.comcast.xconf.estbfirmware.evaluation.EvaluationResult;
import com.comcast.xconf.estbfirmware.util.LogsCompatibilityUtils;
import com.comcast.xconf.firmware.FirmwareRule;
import com.comcast.xconf.firmware.RuleAction;
import com.comcast.xconf.util.RequestUtil;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_HEADER;
import static com.comcast.xconf.util.RequestUtil.XCONF_HTTP_VALUE;

/**
 * Anything more complex than a one liner call to the datastore goes in here -
 * keeps controllers clean.
 */
@Service
public class EstbFirmwareService {
    protected static final Logger log = LoggerFactory.getLogger(EstbFirmwareService.class);

    @Autowired
    ConfigChangeLogService configChangeLogService;

    @Autowired
    private XconfSpecificConfig xconfSpecificConfig;

    @Autowired
    protected EstbFirmwareRuleBase ruleBase;

    @Autowired
    protected EstbFirmwareLogger estbFirmwareLogger;

    @Autowired
    protected RequestUtil requestUtil;

    protected ResponseEntity getSwuResponse(HttpServletRequest request, EstbFirmwareContext context, String
            applicationType, String version) {
        if (StringUtils.isBlank(context.geteStbMac())) {
            HttpStatus status = ApiVersionUtils.greaterOrEqual(version, 2.0f) ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
            return new ResponseEntity<>("eStbMac should be specified", status);
        }

        String ipAddress = requestUtil.findValidIpAddress(request, context.getIpAddress());
        context.setIpAddress(ipAddress);

        normalizeContext(context);

        String xconfHttp = request.getHeader(XCONF_HTTP_HEADER);
        if (!isSecureConnection(context, xconfHttp)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        EvaluationResult evaluationResult = ruleBase.eval(context, applicationType);
        FirmwareConfigFacade firmwareConfig = evaluationResult.getFirmwareConfig();

        String explanation = getExplanation(context, evaluationResult);

        if (evaluationResult.isBlocked() || firmwareConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("returning 404:" + explanation);
            }
            logResponse(context, explanation, null, evaluationResult);
            return new ResponseEntity<>("<h2>404 NOT FOUND</h2><div>" + explanation + "</div>", HttpStatus.NOT_FOUND);

        } else {

            FirmwareConfigFacade responseEntity = firmwareConfig.createResponseEntity();

            if (log.isDebugEnabled()) {
                log.debug("returning 200: " + responseEntity);
            }
            logResponse(context, explanation, responseEntity, evaluationResult);
            return new ResponseEntity<>(responseEntity, HttpStatus.OK);
        }
    }

    protected boolean isSecureConnection(EstbFirmwareContext context, String xconfHttp) {
        if (XCONF_HTTP_VALUE.equals(xconfHttp)) {
            String model = context.getModel();
            String firmwareVersion = context.getFirmwareVersion();
            if (!isAllowedNonSecureConnection(firmwareVersion, model, xconfSpecificConfig.getRecoveryFirmwareVersions())) {
                log.error("Non-secure connection is forbidden for firmwareVersion={} and model={}", firmwareVersion, model);
                return false;
            }
        }
        return true;
    }

    protected boolean isAllowedNonSecureConnection(String firmwareVersion, String model, String allowedCombinations) {
        if (allowedCombinations == null) {
            return false;
        }
        try {
            String[] combinations = allowedCombinations.split("[;]");
            for (String combination : combinations) {
                String[] parts = combination.split("\\s+");
                if (parts.length != 2) {
                    log.warn("Wrong format for recoveryFirmwareVersions. Each combination should contain 2 parts. Got " + combination);
                    continue;
                }
                if (StringUtils.isNotBlank(firmwareVersion) && StringUtils.isNotBlank(model) &&
                        firmwareVersion.matches(parts[0]) && model.matches(parts[1])) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Can't match recoveryFirmwareVersions: " + e.getMessage());
        }
        return false;
    }

    protected void logResponse(EstbFirmwareContext context, String explanation, FirmwareConfigFacade responseEntity, EvaluationResult evaluationResult) {
        try {
            log(context, explanation, responseEntity, evaluationResult);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    protected void normalizeContext(EstbFirmwareContext context) {

        if (context.getEnv() != null) {
            context.setEnv(context.getEnv().toUpperCase());
        }

        if (context.getModel() != null) {
            context.setModel(context.getModel().toUpperCase());
        }

        if (context.geteStbMac() != null) {
            context.seteStbMac(MacAddressUtil.normalizeMacAddress(context.geteStbMac()));
        }

        if (context.geteCMMac() != null) {
            context.seteCMMac(MacAddressUtil.normalizeMacAddress(context.geteCMMac()));
        }

        if (context.getTime() == null) {
            DateTimeZone timeZone = EstbFirmwareContext.offsetToTimeZone(context.getTimeZoneOffset());
            context.setTime(new LocalDateTime(timeZone));
        }

        if (containsLegacyPercentFilter(context.getBypassFilters())) {
            context.setBypassFilters(context.getBypassFilters() + "," + TemplateNames.GLOBAL_PERCENT);
        }

        if (context.getPartnerId() != null) {
            context.setPartnerId(context.getPartnerId().toUpperCase());
        }
    }

    protected boolean containsLegacyPercentFilter(String bypassFilters) {
        if (bypassFilters != null) {
            for (String filter : bypassFilters.split("[,]")) {
                if (EstbFirmwareRuleBase.PERCENT_FILTER_NAME.equals(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected String getExplanation(EstbFirmwareContext context, EvaluationResult evaluationResult) {
        String input = context.convert().toString();
        StringBuilder explanation = new StringBuilder();

        if (evaluationResult.getMatchedRule() == null) {
            explanation.append("Request: ").append(input).append("\ndid not match any rule.");
        } else {
            FirmwareRule rule = evaluationResult.getMatchedRule();
            FirmwareConfigFacade config = evaluationResult.getFirmwareConfig();
            if (config == null && evaluationResult.isBlocked()) {
                explanation.append("Request: ").append(input).append("\n matched ")
                        .append(getRuleInfo(rule))
                        .append("\n and blocked by Distribution percent in ").append(rule.getApplicableAction());

            } else if (config == null) {
                explanation.append("Request: ").append(input).append("\n matched NO OP ")
                        .append(getRuleInfo(rule))
                        .append("\n received NO config.");
            } else {
                explanation.append("Request: ").append(input).append("\n matched ")
                        .append(getRuleInfo(rule))
                        .append("\n received config: ").append(config);

                Object filter = evaluationResult.getAppliedFilters().size() > 0 ? evaluationResult.getAppliedFilters().get(evaluationResult.getAppliedFilters().size() - 1) : null;

                if (filter != null) {
                    String filterStr = "";
                    if (filter instanceof FirmwareRule) {
                        filterStr = estbFirmwareLogger.toString((FirmwareRule) filter);
                    } else if (filter instanceof PercentFilterValue) {
                        filterStr = estbFirmwareLogger.toString((PercentFilterValue) filter);
                    } else if (filter instanceof DownloadLocationRoundRobinFilterValue) {
                        filterStr = LogsCompatibilityUtils.getRuleIdInfo(filter) + " " + ((SingletonFilterValue) filter).getId();
                    } else if (filter instanceof RuleAction) {
                        filterStr = "DistributionPercent in " + filter;
                    } else if (filter instanceof PercentageBean) {
                        filterStr = estbFirmwareLogger.toString((PercentageBean) filter);
                    }
                    explanation.append("\n was blocked/modified by filter ").append(filterStr);
                }
            }
        }

        return explanation.toString();
    }

    protected String getRuleInfo(FirmwareRule rule) {
        return rule.getType() + " " + rule.getId() + ": " + rule.getName();
    }

    protected ResponseEntity getMinimumVersion(EstbFirmwareContext context) {
        List<String> missingFields = new ArrayList<>();
        List<String> emptyFields = new ArrayList<>();
        validateContext(context, missingFields, emptyFields);
        if (!missingFields.isEmpty()) {
            return new ResponseEntity<>("Required field(s) are missing: " + missingFields, HttpStatus.BAD_REQUEST);
        }
        if (!emptyFields.isEmpty()) {
            log.warn("Missing fields: " + emptyFields + ", returning hasMinimumFirmware as true.");
            return new ResponseEntity<>(new EstbFirmwareController.MinimumFirmwareCheckBean(true), HttpStatus.OK);
        }

        normalizeContext(context);

        EstbFirmwareController.MinimumFirmwareCheckBean bean = new EstbFirmwareController.MinimumFirmwareCheckBean(ruleBase.hasMinimumFirmware(context));
        return new ResponseEntity<>(bean, HttpStatus.OK);
    }

    protected void validateContext(EstbFirmwareContext context, List<String> missingFields, List<String> emptyFields) {
        if (context.geteStbMac() == null) {
            missingFields.add("estbMac");
        } else if (StringUtils.isBlank(context.geteStbMac())) {
            emptyFields.add("estbMac");
        }
        if (context.getIpAddress() == null) {
            missingFields.add("ipAddress");
        } else if (StringUtils.isBlank(context.getIpAddress())) {
            emptyFields.add("ipAddress");
        }
        if (context.getFirmwareVersion() == null) {
            missingFields.add("firmwareVersion");
        } else if (StringUtils.isBlank(context.getFirmwareVersion())) {
            emptyFields.add("firmwareVersion");
        }
        if (context.getModel() == null) {
            missingFields.add("model");
        } else if (StringUtils.isBlank(context.getModel())) {
            emptyFields.add("model");
        }
        if (context.getEnv() == null) {
            missingFields.add("env");
        } else if (StringUtils.isBlank(context.getEnv())) {
            emptyFields.add("env");
        }
    }

    protected ResponseEntity getLastLog(String macAddress) {
        if (!MacAddress.isValid(macAddress)) {
            return new ResponseEntity<>("Mac is invalid: " + macAddress, HttpStatus.BAD_REQUEST);
        }
        String mac = MacAddress.normalize(macAddress);

        LastConfigLog lastConfigLog = configChangeLogService.getLastConfigLog(mac);
        if (lastConfigLog != null) {
            logPreDisplayCleanup(lastConfigLog);
        } else {
            log.info("Last log is not found for mac {}", mac);
        }
        return new ResponseEntity<>(lastConfigLog, HttpStatus.OK);
    }

    protected ResponseEntity getChangeLogs(String macAddress) {
        if (!MacAddress.isValid(macAddress)) {
            return new ResponseEntity<>("Mac is invalid: " + macAddress, HttpStatus.BAD_REQUEST);
        }
        String mac = MacAddress.normalize(macAddress);

        List<ConfigChangeLog> logs = configChangeLogService.getChangeLogsOnly(mac);
        if (!logs.isEmpty()) {
            for (ConfigChangeLog log : logs) {
                logPreDisplayCleanup(log);
            }
        } else {
            log.info("No logs found for mac {}", mac);
        }
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    protected void logPreDisplayCleanup(LastConfigLog log) {
        log.setId(null);
        log.setUpdated(null);
    }

    protected ResponseEntity getSwuBSE(IpAddress address) {
        BseConfiguration config = ruleBase.getBseConfiguration(address);
        if (config == null) {
            return new ResponseEntity<>("<h2>404 NOT FOUND</h2>", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    protected ResponseEntity getRunningFirmwareVersion(HttpServletRequest request, EstbFirmwareContext context, String applicationType) {
        if (StringUtils.isBlank(context.geteStbMac())) {
            return new ResponseEntity<>("eStbMac should be specified", HttpStatus.BAD_REQUEST);
        }

        String ipAddress = requestUtil.findValidIpAddress(request, context.getIpAddress());
        context.setIpAddress(ipAddress);

        normalizeContext(context);

        String xconfHttp = request.getHeader(XCONF_HTTP_HEADER);
        if (!isSecureConnection(context, xconfHttp)) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        RunningVersionInfo runningVersionInfo = ruleBase.getAppliedActivationVersionType(context, applicationType);
        log.info("ActivationVersion context=" + context.toLogString());
        return new ResponseEntity<>(runningVersionInfo, HttpStatus.OK);
    }

    /**
     * Logs this request and response to cassandra log table, if same firmware
     * version just logs this request as the last request, if different firmware
     * version logs each time the version changes.
     */
    public void log(final EstbFirmwareContext context,
                    final String explanation,
                    final FirmwareConfigFacade config,
                    final EvaluationResult evaluationResult) {
        final FirmwareConfigFacade firmwareConfig;

        if (config == null) {
            firmwareConfig = new FirmwareConfigFacade();
        } else {
             firmwareConfig = new FirmwareConfigFacade(config.getProperties());
        }

        doSplunkLog(context, config, evaluationResult);

        if (StringUtils.isBlank(context.getFirmwareVersion())) {
            log.debug("doing nothing, no firmware version given");
        } else {
            log.trace("logging last config request.");
            ExecutorUtil.doAsync(new Runnable() {
                @Override
                public void run() {
                    /**
                     * In order to keep bounds permanent we are doing "rotation of columns".
                     * It means that we have columns named from 1 to [BOUNDS value]. And each time we writing to one of them.
                     * Once we wrote into all [BOUNDS value] columns we reset the counter and start writing into them again.
                     * This way on each write the older record will be replaced.
                     *
                     * In column "0" we store LastConfigLog object which has currentCounter variable indicating column name we should write into at the moment.
                     *
                     * In this rotation we are doing 3 operations:
                     * 1. read column 0 to get previous counter value
                     * 2. write column 0 to write new counter value
                     * 3. write column [counter]
                     *
                     * Those operations should happen in single synchronized block to avoid racing and data inconsistency.
                     */
                    try {
                        String rowKey = context.geteStbMac();
                        LastConfigLog lastConfigLog = new LastConfigLog(context, explanation, firmwareConfig, evaluationResult.getAppliedFilters(), evaluationResult.getMatchedRule());
                        configChangeLogService.setOne(rowKey, lastConfigLog);

                        if (evaluationResult.getMatchedRule() != null && !evaluationResult.isBlocked()
                                && !context.getFirmwareVersion().equalsIgnoreCase(firmwareConfig.getFirmwareVersion())) {
                            log.trace("logging config change from {} to {}", context.getFirmwareVersion(), context.getFirmwareVersion());

                            ConfigChangeLog configChangeLog = new ConfigChangeLog(context, explanation, firmwareConfig, evaluationResult.getAppliedFilters(), evaluationResult.getMatchedRule());
                            configChangeLogService.setOne(context.geteStbMac(), configChangeLog);
                        }
                    } catch (Exception e) {
                        log.error("Can't save config request", e);
                    }
                }
            });
        }
    }

    /**
     * Writes an info log message out in splunk friendly format.
     */
    public void doSplunkLog(EstbFirmwareContext context,
                             FirmwareConfigFacade config,
                             EvaluationResult evaluationResult) {
        /*
         * we don't want this to error because of NPE or other oversight.
		 */
        try {
            StringBuilder sb = new StringBuilder();

            sb.append(" XCONF_LOG");
            sb.append(" estbMac=").append(context.geteStbMac());
            sb.append(" env=").append(context.getEnv());
            sb.append(" model=").append(context.getModel());
            sb.append(" reportedFirmwareVersion=").append(context.getFirmwareVersion());
            sb.append(" ipAddress=").append(context.getIpAddress());
            sb.append(" timeZone=").append(context.getTimeZoneOffset());
            sb.append(" capabilities=").append(context.getCapabilities());

            appendCustomContextFields(context, sb);

            sb.append(" appliedRule=").append(getMatchedRuleName(evaluationResult.getMatchedRule()));
            sb.append(" ruleType=").append(getMatchedRuleType(evaluationResult.getMatchedRule()));

            if (config != null) {
                sb.append(" firmwareVersion=").append(evaluationResult.isBlocked() ? EvaluationResult.DefaultValue.BLOCKED : config.getFirmwareVersion());
                sb.append(" firmwareDownloadProtocol=").append(config.getFirmwareDownloadProtocol());
                sb.append(" firmwareLocation=").append(config.getFirmwareLocation());
                sb.append(" rebootImmediately=").append(config.getRebootImmediately());
                appendAdditionalConfigProperties(config, sb);
            } else {
                sb.append(" firmwareVersion=").append(evaluationResult.isBlocked() ? EvaluationResult.DefaultValue.BLOCKED : EvaluationResult.DefaultValue.NOMATCH);
            }

            sb.append(" ")
                    .append(Joiner.on(" ").withKeyValueSeparator("=").join(evaluationResult.getAppliedVersionInfo()));

            if (!evaluationResult.getAppliedFilters().isEmpty()) {
                sb.append(" appliedFilters=[");
                for (Object filter : evaluationResult.getAppliedFilters()) {
                    if (filter instanceof FirmwareRule) {
                        FirmwareRule rule = (FirmwareRule) filter;
                        sb.append("{type:").append(rule.getType()).append(",name:\"").append(rule.getName()).append("\"}");
                    } else if (filter instanceof PercentFilterValue) {
                        sb.append("{type:PercentFilter}");
                    } else if (filter instanceof DownloadLocationRoundRobinFilterValue) {
                        sb.append("{type:DownloadLocationRoundRobinFilter}");
                    } else if (filter instanceof RuleAction) {
                        sb.append("{type:DistributionPercentInRuleAction}");
                    } else if (filter instanceof PercentageBean) {
                        PercentageBean bean = (PercentageBean) filter;
                        sb.append("{type:PercentageBean").append(",name:\"").append(bean.getName()).append("\"}");
                    }
                }
                sb.append("]");
            }

            log.info(sb.toString());
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private void appendCustomContextFields(EstbFirmwareContext context, StringBuilder sb) {
        MultiValueMap<String, String> fullContext = context.getContext();
        for (String key : fullContext.keySet()) {
            if (isCustomField(key)) {
                sb.append(" ").append(key).append("=").append(fullContext.getFirst(key));
            }
        }
    }

    private void appendAdditionalConfigProperties(FirmwareConfigFacade config, StringBuilder sb) {
        Map<String, Object> properties = config.getProperties();
        for (String property : properties.keySet()) {
            if (isAdditionalProperties(property)) {
                sb.append(" ").append(property).append("=").append(properties.get(property));
            }
        }
    }

    protected Object getMatchedRuleName(FirmwareRule rule) {
        return (rule != null) ? rule.getName() : EvaluationResult.DefaultValue.NOMATCH;
    }

    protected Object getMatchedRuleType(FirmwareRule rule) {
        return (rule != null && rule.getType() != null) ? rule.getType() : EvaluationResult.DefaultValue.NORULETYPE;
    }

    private boolean isCustomField(String field) {
        return !StbContext.BASE_FIRMWARE_FIELDS.contains(field);
    }

    private boolean isAdditionalProperties(String property) {
        return !ConfigNames.BASE_PROPERTIES.contains(property);

    }
}

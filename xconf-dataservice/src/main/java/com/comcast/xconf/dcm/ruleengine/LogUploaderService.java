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

package com.comcast.xconf.dcm.ruleengine;

import com.comcast.apps.dataaccess.cache.dao.CachedSimpleDao;
import com.comcast.hydra.astyanax.config.XconfSpecificConfig;
import com.comcast.xconf.ApiVersionUtils;
import com.comcast.xconf.estbfirmware.evaluation.EvaluationResult;
import com.comcast.xconf.logupload.DCMGenericRule;
import com.comcast.xconf.logupload.LogUploaderContext;
import com.comcast.xconf.logupload.Settings;
import com.comcast.xconf.logupload.settings.SettingProfile;
import com.comcast.xconf.logupload.settings.SettingRule;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryRule;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryTwoRule;
import com.comcast.xconf.queries.QueriesHelper;
import com.comcast.xconf.util.RequestUtil;
import com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jackson.JsonProcessingException;
import org.json.JSONObject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.zip.CRC32;
import java.io.IOException;

import static com.comcast.xconf.firmware.ApplicationType.SKY;
import static com.comcast.xconf.firmware.ApplicationType.STB;

@Service
public class LogUploaderService {

    private static final Logger log = LoggerFactory.getLogger(LogUploaderService.class);

    @Autowired
    private XconfSpecificConfig xconfSpecificConfig;
    @Autowired
    private RequestUtil requestUtil;
    @Autowired
    private LogUploadRuleBase ruleBase;
    @Autowired
    private TelemetryProfileService telemetryProfileService;
    @Autowired
    private SettingsProfileService settingProfileService;
    @Autowired
    private CachedSimpleDao<String, DCMGenericRule> dcmRuleDAO;

    public ResponseEntity evaluateSettings(HttpServletRequest request, Boolean checkNow, String apiVersion,
                                           Set<String> settingTypes, LogUploaderContext context, boolean isTelemetry2Settings) {

        String ipAddress = requestUtil.findValidIpAddress(request, context.getEstbIP());
        context.setEstbIP(ipAddress);

        if (xconfSpecificConfig.isReadSkyApplicationTypeFromPartnerParam() &&
                STB.equals(context.getApplication()) && StringUtils.startsWithIgnoreCase(context.getPartnerId(), SKY)) {
            context.setApplication(SKY);
        }

        normalizeContext(context);

        final Map<String, String> contextProps = context.getProperties();

        if (checkNow != null && checkNow) return getTelemetryProfile(contextProps, isTelemetry2Settings);

        Settings result = ruleBase.eval(context);

        TelemetryRule telemetryRule = null;
        if (result != null) {
            normalizeTelemetryContext(contextProps);
            telemetryRule = telemetryProfileService.getTelemetryRuleForContext(contextProps);

            PermanentTelemetryProfile permanentTelemetryProfile = telemetryProfileService.getPermanentProfileByTelemetryRule(telemetryRule);
            result.setTelemetryProfile(permanentTelemetryProfile);
            result.setUploadImmediately(context.getUploadImmediately());
            if (permanentTelemetryProfile != null) {
                permanentTelemetryProfile = isTelemetry2Settings ? toTelemetry2Response(permanentTelemetryProfile) : permanentTelemetryProfile;
                QueriesHelper.nullifyUnwantedFields(permanentTelemetryProfile);
            }
            cleanupLusUploadRepository(result, apiVersion);
        }

        Set<SettingRule> settingRules = null;
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 2.1f) && CollectionUtils.isNotEmpty(settingTypes)) {
            Set<SettingProfile> settingProfiles = new HashSet<>();
            settingRules = new HashSet<>();

            for (String settingType : settingTypes) {
                SettingRule settingRule = settingProfileService.getSettingRuleByTypeForContext(settingType, contextProps);
                SettingProfile settingProfile = settingProfileService.getSettingProfileBySettingRule(settingRule);

                if (settingProfile != null) {
                    settingProfiles.add(settingProfile);
                    settingRules.add(settingRule);
                }
            }

            if (result == null && CollectionUtils.isNotEmpty(settingProfiles)) {
                result = new Settings();
            }
            if (result != null) {
                result.setSettingProfiles(settingProfiles);
            }
        }

        if (result != null) {
            logResultSettings(result, telemetryRule, settingRules);

            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        if (log.isDebugEnabled()) {
            log.debug("returning 404: settings not found");
        }
        return new ResponseEntity<>("<h2>404 NOT FOUND</h2><div>settings not found</div>", HttpStatus.NOT_FOUND);
    }

    protected void normalizeContext(LogUploaderContext context) {
        requestUtil.normalizeContext(context);
    }

    protected void normalizeTelemetryContext(Map<String, String> context) {}

    private PermanentTelemetryProfile toTelemetry2Response(PermanentTelemetryProfile profile) {
        profile.setTelemetryProfile(toTelemetry2Elements(profile.getTelemetryProfile()));
        return profile;
    }

    private TelemetryProfile toTelemetry2Response(TelemetryProfile profile) {
        profile.setTelemetryProfile(toTelemetry2Elements(profile.getTelemetryProfile()));
        return profile;
    }

    private List<TelemetryProfile.TelemetryElement> toTelemetry2Elements(List<TelemetryProfile.TelemetryElement> telemetryElements) {
        for (TelemetryProfile.TelemetryElement telemetryElement : telemetryElements) {
            if (StringUtils.isNotBlank(telemetryElement.getComponent())) {
                telemetryElement.setContent(telemetryElement.getComponent());
                telemetryElement.setType("<event>");
                telemetryElement.setComponent(null);
            }
        }
        return telemetryElements;
    }

    private static void cleanupLusUploadRepository(Settings settings, String apiVersion) {
        if (ApiVersionUtils.greaterOrEqual(apiVersion, 2)) {
            settings.setLusUploadRepositoryURL(null);
        } else {
            settings.setLusUploadRepositoryUploadProtocol(null);
            settings.setLusUploadRepositoryURLNew(null);
        }
    }

    private ResponseEntity getTelemetryProfile (final Map<String,String> context, boolean isTelemetry2Settings) {
        TelemetryProfile profile = telemetryProfileService.getTelemetryForContext(context);
        if (profile != null) {
            profile = isTelemetry2Settings ? toTelemetry2Response(profile) : profile;
            return new ResponseEntity<>(profile, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("<h2>404 NOT FOUND</h2><div> telemetry profile not found</div>", HttpStatus.NOT_FOUND);
        }
    }

    private void logResultSettings(Settings settings, TelemetryRule telemetryRule, Set<SettingRule> settingRules) {
        List<String> ruleNames = new ArrayList<>();
        for(String ruleId: settings.getRuleIDs()) {
            DCMGenericRule dcmRule = dcmRuleDAO.getOne(ruleId);
            if (dcmRule != null && StringUtils.isNotBlank(dcmRule.getName())) {
                ruleNames.add(dcmRule.getName());
            }
        }

        List<String> settingRuleNames = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(settingRules)) {
            for (SettingRule settingRule : settingRules) {
                settingRuleNames.add(settingRule.getName());
            }
        }

        log.info("AppliedRules: formulaNames={}, telemetryRuleName={}, settingRuleName={}",
                CollectionUtils.isNotEmpty(ruleNames) ? Joiner.on(",").join(ruleNames) : EvaluationResult.DefaultValue.NOMATCH,
                telemetryRule != null ? telemetryRule.getName() : EvaluationResult.DefaultValue.NOMATCH,
                CollectionUtils.isNotEmpty(settingRuleNames) ? Joiner.on(",").join(settingRuleNames) : EvaluationResult.DefaultValue.NOMATCH);
    }

    public String getHashValue(String jsonConfig) {
        CRC32 crc = new CRC32();
        crc.update(jsonConfig.getBytes());
        String hashValue = Long.toHexString(crc.getValue());
        return hashValue;
    }

    public ResponseEntity getTelemetryTwoProfiles(HttpServletRequest request, LogUploaderContext context) throws JsonProcessingException, IOException {

        if (context.getEnv() != null) {
            context.setEnv(context.getEnv().toUpperCase());
        }
        String ipAddress = requestUtil.findValidIpAddress(request, context.getEstbIP());
        context.setEstbIP(ipAddress);
        normalizeContext(context);

        final Map<String, String> contextProps = context.getProperties();

        List<TelemetryTwoRule> telemetryTwoRules = telemetryProfileService.processTelemetryTwoRules(contextProps);
        List<TelemetryTwoProfile> telemetryTwoProfiles = telemetryProfileService.getTelemetryTwoProfileByTelemetryRules(telemetryTwoRules);

        List<Object> profiles = new ArrayList<>();
        for (final TelemetryTwoProfile telemetryTwoProfile : telemetryTwoProfiles) {
            Map<String, Object> newHashProfile = new HashMap<>();
            newHashProfile.put("name", telemetryTwoProfile.getName());
            JSONObject jsonObj = new JSONObject(telemetryTwoProfile.getJsonconfig());
            newHashProfile.put("versionHash", getHashValue(jsonObj.toString()));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(telemetryTwoProfile.getJsonconfig());
            newHashProfile.put("value", node);
            profiles.add(newHashProfile);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("profiles", profiles);

        if (profiles.isEmpty()) return new ResponseEntity<>("<h2>404 NOT FOUND</h2>profiles not found", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

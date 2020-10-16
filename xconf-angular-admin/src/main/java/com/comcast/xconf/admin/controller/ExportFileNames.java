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
 *  Created: 5:20 PM
 */
package com.comcast.xconf.admin.controller;

public enum ExportFileNames {
    ALL("all"),
    FIRMWARE_CONFIG("firmwareConfig_"),
    ALL_FIRMWARE_CONFIGS("allFirmwareConfigs"),
    FIRMWARE_RULE("firmwareRule_"),
    ALL_FIRMWARE_RULES("allFirmwareRules"),
    FIRMWARE_RULE_TEMPLATE("firmwareRuleTemplate_"),
    ALL_FIRMWARE_RULE_TEMPLATES("allFirmwareRuleTemplates"),
    ALL_PERMANENT_PROFILES("allPermanentProfiles"),
    PERMANENT_PROFILE("permanentProfile_"),
    ALL_TELEMETRY_RULES("allTelemetryRules"),
    TELEMETRY_RULE("telemetryRule_"),
    ALL_SETTING_PROFILES("allSettingProfiles"),
    SETTING_PROFILE("settingProfile_"),
    ALL_SETTING_RULES("allSettingRules"),
    SETTING_RULE("settingRule_"),
    ALL_FORMULAS("allFormulas"),
    FORMULA("formula_"),
    ALL_ENVIRONMENTS("allEnvironments"),
    ENVIRONMENT("environment_"),
    ALL_MODELS("allModels"),
    MODEL("model_"),
    UPLOAD_REPOSITORY("uploadRepository_"),
    ALL_UPLOAD_REPOSITORIES("allUploadRepositories"),
    ROUND_ROBIN_FILTER("roundRobinFilter"),
    GLOBAL_PERCENT("globalPercent"),
    GLOBAL_PERCENT_AS_RULE("globalPercentAsRule"),
    ENV_MODEL_PERCENTAGE_BEANS("envModelPercentageBeans"),
    ENV_MODEL_PERCENTAGE_BEAN("envModelPercentageBean_"),
    ENV_MODEL_PERCENTAGE_AS_RULES("envModelPercentageAsRules"),
    ENV_MODEL_PERCENTAGE_AS_RULE("envModelPercentageAsRule_"),
    PERCENT_FILTER("percentFilter"),
    ALL_NAMESPACEDLISTS("allNamespacedLists"),
    NAMESPACEDLIST("namespacedList_"),
    ALL_FEATURES("allFeatures"),
    FEATURE("feature_"),
    ALL_FEATURE_SETS("allFeatureSets"),
    FEATURE_SET("featureSet_"),
    ALL_FEATURE_RUlES("allFeatureRules"),
    FEATURE_RULE("featureRule_"),
    ACTIVATION_MINIMUM_VERSION("activationMinimumVersion_"),
    ALL_ACTIVATION_MINIMUM_VERSIONS("allActivationMinimumVersions");

    private final String name;

    ExportFileNames(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}

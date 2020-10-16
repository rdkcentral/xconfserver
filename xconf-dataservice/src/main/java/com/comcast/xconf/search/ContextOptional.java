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

package com.comcast.xconf.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.comcast.xconf.search.SearchFields.*;

public class ContextOptional {

    private Map<String, String> context = new HashMap<>();

    public ContextOptional(Map<String, String> context) {
        this.context = Objects.isNull(context) ? new HashMap<>() : context;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(context.get(ID));
    }

    public Optional<String> getName() {
        return Optional.ofNullable(context.get(NAME));
    }

    public Optional<String> getAuthor() {
        return Optional.ofNullable(context.get(AUTHOR));
    }

    public Optional<String> getEntity() {
        return Optional.ofNullable(context.get(ENTITY));
    }

    public Optional<String> getFirmwareVersion() {
        return Optional.ofNullable(context.get(FIRMWARE_VERSION));
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(context.get(FREE_ARG));
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(context.get(FIXED_ARG));
    }

    public Optional<String> getData() {
        return Optional.ofNullable(context.get(DATA));
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(context.get(DESCRIPTION));
    }

    public Optional<String> getModel() {
        return Optional.ofNullable(context.get(MODEL));
    }

    public Optional<String> getType() {
        return Optional.ofNullable(context.get(TYPE));
    }

    public Optional<String> getApplicableActionType() {
        return Optional.ofNullable(context.get(APPLICABLE_ACTION_TYPE));
    }

    public Optional<String> getTemplateId() {
        return Optional.ofNullable(context.get(TEMPLATE_ID));
    }

    public Optional<String> getFeatureInstance() {
        return Optional.ofNullable(context.get(FEATURE_INSTANCE));
    }

    public Optional<String> getApplicationType() {
        return Optional.ofNullable(context.get(APPLICATION_TYPE));
    }

    public void setApplicationTypeIfNotPresent(String applicationType) {
        if (!getApplicationType().isPresent()) {
            context.put(APPLICATION_TYPE, applicationType);
        }
    }

    public Optional<String> getRegExp() {
        return Optional.ofNullable(context.get(REGULAR_EXPRESSION));
    }

    public Optional<String> getPartnerId() {
        return Optional.ofNullable(context.get(PARTNER_ID));
    }

    public Optional<String> getEnvironment() {
        return Optional.ofNullable(context.get(ENVIRONMENT));
    }

    public Optional<String> getLKG() {
        return Optional.ofNullable(context.get(LAST_KNOWN_GOOD));
    }

    public Optional<String> getMinCheckVersion() {
        return Optional.ofNullable(context.get(MIN_CHECK_VERSION));
    }

    public Optional<String> getDistributionVersion() {
        return Optional.ofNullable(context.get(DISTRIBUTION_VERSION));
    }

    public Optional<String> getIntermediateVersion() {
        return Optional.ofNullable(context.get(INTERMEDIATE_VERSION));
    }
}

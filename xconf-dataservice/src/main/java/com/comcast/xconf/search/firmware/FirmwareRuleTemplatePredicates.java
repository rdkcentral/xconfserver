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

package com.comcast.xconf.search.firmware;

import com.comcast.xconf.firmware.FirmwareRuleTemplate;
import com.comcast.xconf.search.ContextOptional;
import com.comcast.xconf.search.XRulePredicates;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static com.comcast.xconf.firmware.ApplicableAction.readFromString;

@Service
public class FirmwareRuleTemplatePredicates extends XRulePredicates<FirmwareRuleTemplate> {

    public Predicate<FirmwareRuleTemplate> byActionType(String type) {
        return firmwareRule -> Objects.nonNull(firmwareRule)
                && Objects.equals(firmwareRule.getApplicableAction().getActionType(), readFromString(type));
    }

    public Predicate<FirmwareRuleTemplate> byEditable(boolean editable) {
        return firmwareRuleTemplate -> Objects.nonNull(firmwareRuleTemplate)
                && firmwareRuleTemplate.isEditable() == editable;
    }

    public List<Predicate<FirmwareRuleTemplate>> getPredicates(ContextOptional context) {
        return getBaseRulePredicates(context);
    }
}

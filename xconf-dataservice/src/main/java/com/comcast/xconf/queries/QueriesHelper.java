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
 * Author: ikostrov
 * Created: 31.08.15 16:13
*/
package com.comcast.xconf.queries;

import com.comcast.hydra.astyanax.data.XMLPersistable;
import com.comcast.xconf.NamespacedList;
import com.comcast.xconf.estbfirmware.*;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.logupload.telemetry.TelemetryProfile;
import com.comcast.xconf.queries.beans.MacRuleBeanWrapper;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureResponse;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class QueriesHelper {

    public static FirmwareConfig nullifyUnwantedFields(FirmwareConfig config) {
        if (config != null) {
            config.setUpdated(null);
            config.setFirmwareDownloadProtocol(null);
            config.setRebootImmediately(null);
            config.setApplicationType(null);
        }

        return config;
    }

    public static List<IpRuleBean> nullifyUnwantedFields(List<IpRuleBean> ipRules) {
        for(IpRuleBean rule : ipRules) {
            FirmwareConfig config = rule.getFirmwareConfig();
            if (config != null) {
                config.setUpdated(null);
                config.setFirmwareDownloadProtocol(null);
                config.setRebootImmediately(null);
            }
        }

        return ipRules;
    }

    public static NamespacedList nullifyUnwantedFields(NamespacedList nsList) {
        if (nsList != null) {
            nsList.setUpdated(null);
        }

        return nsList;
    }
    public static MacRuleBean nullifyUnwantedFields(MacRuleBean bean) {
        if (bean != null) {
            bean.setFirmwareConfig(nullifyUnwantedFields(bean.getFirmwareConfig()));
        }

        return bean;
    }

    public static MacRuleBeanWrapper nullifyUnwantedFields(MacRuleBeanWrapper bean) {
        if (bean != null) {
            bean.setFirmwareConfig(nullifyUnwantedFields(bean.getFirmwareConfig()));
        }

        return bean;
    }

    public static XMLPersistable nullifyUnwantedFields(XMLPersistable obj) {
        if (obj != null) {
            obj.setUpdated(null);
        }
        return obj;
    }

    public static SingletonFilterValue nullifyUnwantedFields(SingletonFilterValue filter) {
        filter.setUpdated(null);
        return filter;
    }

    public static DownloadLocationRoundRobinFilterValue nullifyUnwantedFields(DownloadLocationRoundRobinFilterValue filter) {
        filter.setUpdated(null);
        filter.setApplicationType(null);
        return filter;
    }

    public static PercentageBean nullifyUnwantedFields(PercentageBean bean) {
        if (bean != null) {
            bean.setApplicationType(null);
        }
        return bean;
    }

    public static Feature nullifyUnwantedFields(Feature feature) {
        feature.setId(null);
        return feature;
    }

    public static FeatureResponse nullifyUnwantedFields(FeatureResponse featureResponse) {
        featureResponse.setId(null);
        featureResponse.setApplicationType(null);
        return featureResponse;
    }

    public static PermanentTelemetryProfile nullifyUnwantedFields(PermanentTelemetryProfile profile) {
        if (CollectionUtils.isNotEmpty(profile.getTelemetryProfile())) {
            for (TelemetryProfile.TelemetryElement telemetryElement : profile.getTelemetryProfile()) {
                telemetryElement.setId(null);
                telemetryElement.setComponent(null);
            }
        }
        return profile;
    }
}

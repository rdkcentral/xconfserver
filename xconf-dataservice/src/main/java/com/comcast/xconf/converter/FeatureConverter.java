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
 * <p>
 * Author: mdolina
 * Created: 5/15/17  5:06 PM
 */
package com.comcast.xconf.converter;

import com.comcast.xconf.GenericNamespacedList;
import com.comcast.xconf.rfc.Feature;
import com.comcast.xconf.rfc.FeatureResponse;
import com.comcast.xconf.service.GenericNamespacedListQueriesService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeatureConverter {

    @Autowired
    private GenericNamespacedListQueriesService genericNamespacedListQueriesService;

    private Logger log = LoggerFactory.getLogger(FeatureConverter.class);

    public FeatureResponse toRfcResponse(Feature feature) {
        FeatureResponse featureResponse = new FeatureResponse(feature);
        if (!feature.isWhitelisted()) {
            return featureResponse;
        }
        if (feature.getWhitelistProperty() != null
                && StringUtils.isNotBlank(feature.getWhitelistProperty().getValue())
                && StringUtils.isNotBlank(feature.getWhitelistProperty().getNamespacedListType())) {
            GenericNamespacedList namespacedList = genericNamespacedListQueriesService.getOneByType(feature.getWhitelistProperty().getValue(), feature.getWhitelistProperty().getNamespacedListType());
            if (namespacedList != null) {
                featureResponse.getProperties().put(namespacedList.getId(), namespacedList.getData());
                featureResponse.setListType(feature.getWhitelistProperty().getTypeName());
                featureResponse.setListSize(namespacedList.getData().size());
            }
        } else {
            log.warn("Whitelist property has a wrong value: " + feature);
        }

        return featureResponse;
    }
}

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
package com.comcast.apps.dataaccess.support.services;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

@XmlRootElement
public class HashCheckerResult {
    private Map<String, Long> syncedHosts;
    private Map<String, Long> desyncedHosts;
    private Map<String, String> badResponses;

    public HashCheckerResult() {

    }

    public HashCheckerResult(Map<String, Long> syncedHosts, Map<String, Long> desyncedHosts, Map<String,
            String> badResponses) {
        this.syncedHosts = syncedHosts;
        this.desyncedHosts = desyncedHosts;
        this.badResponses = badResponses;
    }

    public Map<String, Long> getSyncedHosts() {
        return syncedHosts;
    }

    public void setSyncedHosts(Map<String, Long> syncedHosts) {
        this.syncedHosts = syncedHosts;
    }

    public Map<String, Long> getDesyncedHosts() {
        return desyncedHosts;
    }

    public void setDesyncedHosts(Map<String, Long> desyncedHosts) {
        this.desyncedHosts = desyncedHosts;
    }

    public Map<String, String> getBadResponses() {
        return badResponses;
    }

    public void setBadResponses(Map<String, String> badResponses) {
        this.badResponses = badResponses;
    }
}

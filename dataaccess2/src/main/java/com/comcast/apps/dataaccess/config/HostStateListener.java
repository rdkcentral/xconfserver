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
package com.comcast.apps.dataaccess.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class HostStateListener implements Host.StateListener {

    private AtomicLong hostAddedCount = new AtomicLong();
    private AtomicLong hostRemovedCount = new AtomicLong();
    private AtomicLong hostDownCount = new AtomicLong();
    private AtomicLong hostReactivatedCount = new AtomicLong();

    public AtomicLong getHostAddedCount() {
        return hostAddedCount;
    }

    public AtomicLong getHostRemovedCount() {
        return hostRemovedCount;
    }

    public AtomicLong getHostDownCount() {
        return hostDownCount;
    }

    public AtomicLong getHostReactivatedCount() {
        return hostReactivatedCount;
    }

    @Override
    public void onAdd(Host host) {
        hostAddedCount.incrementAndGet();
    }

    @Override
    public void onUp(Host host) {
        hostReactivatedCount.incrementAndGet();
    }

    @Override
    public void onDown(Host host) {
        hostDownCount.incrementAndGet();
    }

    @Override
    public void onRemove(Host host) {
        hostRemovedCount.incrementAndGet();
    }

    @Override
    public void onRegister(Cluster cluster) {

    }

    @Override
    public void onUnregister(Cluster cluster) {

    }
}

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
package com.comcast.apps.healthcheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ManagedResource
public class HealthCheckManager implements IMetricManager {

    private static Logger log = LoggerFactory.getLogger(HealthCheckManager.class);

    public static final String STATUS_SEPARATOR = ",";

    public static final int DEFAULT_WINDOW = 5; // 5 min

    public static final int DEFAULT_DURATION_GRANULARITY = 100; // 100 ms

    public static final int DEFAULT_FAILED_DURATION_THRESHOLD = 5000; // 5000 ms

    public static final double DEFAULT_FADE_FACTOR = 0.1; // 10 %

    private volatile int window = DEFAULT_WINDOW; // volatile, because may be changed via JMX

    private volatile int durationGranularity = DEFAULT_DURATION_GRANULARITY;

    private volatile int failedDurationThreshold = DEFAULT_FAILED_DURATION_THRESHOLD;

    private volatile double fadeFactor = DEFAULT_FADE_FACTOR;

    private volatile Set<Integer> successStatus = new LinkedHashSet<Integer>();

    private Map<Integer, Map> successMap = new TtlHashMap<Integer, Map>(TimeUnit.MINUTES, 59, 60);
    private Map<Integer, AtomicInteger> failedMap = new TtlHashMap<Integer, AtomicInteger>(TimeUnit.MINUTES, 59, 60);

    public static final double DEFAULT_HEALTHY_SCORE = 0.5;

    private double healthyScore = DEFAULT_HEALTHY_SCORE;

    public HealthCheckManager() {
    }

    @ManagedAttribute
    public double getHealthyScore() {
        return healthyScore;
    }

    @ManagedAttribute
    public void setHealthyScore(double healthyScore) {
        this.healthyScore = healthyScore;
    }

    @ManagedAttribute
    public String getSuccessStatuses() {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<Integer> it = successStatus.iterator();
        if (it.hasNext()) {
            stringBuilder.append(it.next());
            while (it.hasNext()) {
                stringBuilder.append(STATUS_SEPARATOR).append(it.next());
            }
        }
        return stringBuilder.toString();
    }

    @ManagedAttribute
    public void setSuccessStatuses(String statuses) {
        successStatus = new LinkedHashSet<Integer>();
        if (statuses == null) {
            log.error("Success statuses are not set, all statuses are considered failed!");
            return;
        }
        List<String> list = Arrays.asList(statuses.split(STATUS_SEPARATOR));
        for (String s : list) {
            try {
                successStatus.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                log.error("Can't parse {} as integer", s);
            }
        }
        log.info("Success statuses: " + successStatus);
    }

    @ManagedAttribute
    public int getWindow() {
        return window;
    }

    @ManagedAttribute
    public void setWindow(int window) {
        log.info("Window: {}", window);
        this.window = window;
    }

    @ManagedAttribute
    public int getDurationGranularity() {
        return durationGranularity;
    }

    @ManagedAttribute
    public void setDurationGranularity(int durationGranularity) {
        log.info("Duration Granularity: {}", durationGranularity);
        this.durationGranularity = durationGranularity;
    }

    @ManagedAttribute
    public int getFailedDurationThreshold() {
        return failedDurationThreshold;
    }

    @ManagedAttribute
    public void setFailedDurationThreshold(int failedDurationThreshold) {
        log.info("Failed Duration Threshold: {}", failedDurationThreshold);
        this.failedDurationThreshold = failedDurationThreshold;
    }

    @ManagedAttribute
    public double getFadeFactor() {
        return fadeFactor;
    }

    @ManagedAttribute
    public void setFadeFactor(double fadeFactor) {
        log.info("Fade Factor: {}", fadeFactor);
        this.fadeFactor = fadeFactor;
    }

    @Override
    public void addMetric(int status, int duration) {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        int roundedDuration = getRoundedDuration(duration);
        if (successStatus.contains(status) && roundedDuration <= failedDurationThreshold) {
            incrementSuccessCount(minute, roundedDuration);
        } else {
            incrementFailedCount(minute);
        }
    }

    @Override
    public boolean hasCurrent() {
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        // are there any failures?
        AtomicInteger atomicFailedCount = failedMap.get(minute);
        if (atomicFailedCount != null && atomicFailedCount.get() > 0) {
            return true;
        }
        // are there any successes
        Map<Integer, AtomicInteger> durationMap = successMap.get(minute);
        if (durationMap != null && ! durationMap.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isHealthy() {
        return getScore() >= healthyScore;
    }

    public int getRoundedDuration(int duration) {
        duration += durationGranularity >> 1;
        return duration - duration % durationGranularity;
    }

    private void incrementSuccessCount(int minute, int duration) {
        Map<Integer, AtomicInteger> durationMap = getDurationMap(minute);
        AtomicInteger count = durationMap.get(duration);
        if (count == null) {
            durationMap.put(duration, new AtomicInteger(1));
        } else {
            count.incrementAndGet();
        }
    }

    private Map<Integer, AtomicInteger> getDurationMap(int minute) {
        Map<Integer, AtomicInteger> durationMap = successMap.get(minute);
        if (durationMap == null) {
            durationMap = new ConcurrentHashMap<Integer, AtomicInteger>();
            successMap.put(minute, durationMap);
        }
        return durationMap;
    }

    private void incrementFailedCount(int minute) {
        AtomicInteger count = failedMap.get(minute);
        if (count == null) {
            failedMap.put(minute, new AtomicInteger(1));
        } else {
            count.incrementAndGet();
        }
    }

    @ManagedAttribute
    public float getScore() {
        double healthScore = 0.0;
        double baseWeight = 1.0 / window;
        double fadeBaseWeight = baseWeight * fadeFactor;
        double weight = baseWeight + fadeBaseWeight * (window - 1) / 2;

        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < window; i++) {
            calendar.add(Calendar.MINUTE, -1);
            int minute = calendar.get(Calendar.MINUTE);

            AtomicInteger atomicFailedCount = failedMap.get(minute);
            int failedCount = atomicFailedCount != null ? atomicFailedCount.get() : 0;

            int successCount = 0;
            Map<Integer, AtomicInteger> durationMap = successMap.get(minute);
            if (durationMap != null) {
                for (AtomicInteger count : durationMap.values()) {
                    successCount += count.get();
                }
            }
            double minuteHealthScore = failedCount == 0 ? 1.0 : (double) successCount / (successCount + failedCount);
            healthScore += minuteHealthScore * weight;
            weight -= fadeBaseWeight;
        }
        return (float) Math.round(healthScore * 100) / 100;
    }

    @ManagedAttribute
    public String getFailed() {
        HashMap<Integer, AtomicInteger> m = new HashMap<Integer, AtomicInteger>();
        m.putAll(failedMap);
        return m.toString();
    }

    @ManagedAttribute
    public String getSuccess() {
        HashMap<Integer, Map> m = new HashMap<Integer, Map>();
        m.putAll(successMap);
        return m.toString();
    }

}

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

import com.comcast.apps.dataaccess.config.ClusterLatencyListener;
import com.comcast.apps.dataaccess.config.HostStateListener;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.Session;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@XmlRootElement(name = "ConnectionPoolMonitor")
@XmlAccessorType(XmlAccessType.NONE)
public class ConnectionPoolMonitor {

    private Metrics metrics;

    @XmlElement
    private Operations operations;

    @XmlElement
    private Connections connections;

    @XmlElement
    private Hosts hosts;

    public ConnectionPoolMonitor() {
    }

    public ConnectionPoolMonitor(Session session, HostStateListener hostStateListener, ClusterLatencyListener clusterLatencyListener) {
        metrics = session.getCluster().getMetrics();
        operations = new Operations(metrics, clusterLatencyListener);
        connections = new Connections(metrics);
        hosts = new Hosts(hostStateListener);
    }

    @XmlElement(name = "OpenConnections")
    public int getOpenConnectionsMetric() {
        return metrics.getOpenConnections().getValue();
    }

    /**
     * Datastax driver doesn't provide information about busy connections. Added for compatibility.
     *
     * @return always 0
     */
    @XmlElement(name = "BusyConnections")
    public long getNumBusyConnections() {
        return 0L;
    }

    @XmlElement(name = "HostActiveCount")
    public int getHostActiveCount() {
        return metrics.getConnectedToHosts().getValue();
    }

    @XmlElement(name = "HostCount")
    public int getHostCount() {
        return metrics.getKnownHosts().getValue();
    }

    @XmlElement(name = "BadRequest")
    public long getBadRequests() {
        return getAllErrorsCount(metrics.getErrorMetrics());
    }

    public double getRequestCount() {
        return metrics.getRequestsTimer().getSnapshot().get98thPercentile();
    }

    @XmlElement(name = "UptimeMinutes")
    public Long getUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        return rb.getUptime() / (60 * 1000);
    }

    static long getAllErrorsCount(Metrics.Errors errors) {
        return errors.getAuthenticationErrors().getCount() + errors.getClientTimeouts().getCount()
                + errors.getConnectionErrors().getCount() + errors.getReadTimeouts().getCount() + errors.getWriteTimeouts().getCount()
                + errors.getUnavailables().getCount() + errors.getUnavailables().getCount();
    }

    public static class Operations {

        private Metrics.Errors errorMetrics;
        private ClusterLatencyListener clusterLatencyListener;

        public Operations() {
        }

        public Operations(Metrics metrics, ClusterLatencyListener clusterLatencyListener) {
            errorMetrics = metrics.getErrorMetrics();
            this.clusterLatencyListener = clusterLatencyListener;
        }

        @XmlElement(name = "Failure")
        public Long getOperationFailureCountValue() {
            return getAllErrorsCount(errorMetrics);
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations.
         * Added for compatibility
         *
         * @return always 0
         */
        @XmlElement(name = "Success")
        public Long getOperationSuccessValue() {
            return clusterLatencyListener.getRequestCount();
        }

        @XmlElement(name = "Failover")
        public Long getOperationFailoverValue() {
            return errorMetrics.getRetries().getCount();
        }

        @XmlElement(name = "Timeout")
        public Long getOperationTimeoutValue() {
            return errorMetrics.getReadTimeouts().getCount() + errorMetrics.getWriteTimeouts().getCount();
        }

        @XmlElement(name = "SocketTimeout")
        public Long getSocketTimeoutValue() {
            return errorMetrics.getClientTimeouts().getCount();
        }

        @XmlElement(name = "NoHosts")
        public Long getNoHostsValue() {
            return errorMetrics.getUnavailables().getCount();
        }

        @XmlElement(name = "UnknownError")
        public Long getUnknownErrorValue() {
            return errorMetrics.getOthers().getCount();
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "Interrupted")
        public Long getInterruptedValue() {
            return 0L;
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "PoolExhasted")
        public Long getPoolExhastedValue() {
            return 0L;
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "TransportError")
        public Long getTransportErrorValue() {
            return 0L;
        }
    }

    public static class Connections {

        private Metrics metrics;

        public Connections() {
        }

        public Connections(Metrics metrics) {
            this.metrics = metrics;
        }

        @XmlElement(name = "Create")
        public Long getConnectionCreateValue() {
            return Long.valueOf(metrics.getOpenConnections().getValue());
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "Closed")
        public Long getConnectionClosedValue() {
            return 0L;
        }

        @XmlElement(name = "CreateFailure")
        public Long getConnectionCreateFailureValue() {
            return metrics.getErrorMetrics().getConnectionErrors().getCount();
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "Borrow")
        public Long getConnectionBorrowValue() {
            return 0L;
        }

        /**
         * Cassandra driver doesn't provide info about interrupted operations. Added for compatibility.
         *
         * @return always 0
         */
        @XmlElement(name = "Return")
        public Long getConnectionReturnValue() {
            return 0L;
        }
    }

    public static class Hosts {

        private HostStateListener hostStateListener;

        public Hosts() {
        }

        public Hosts(HostStateListener hostStateListener) {
            this.hostStateListener = hostStateListener;
        }

        @XmlElement(name = "Added")
        Long getHostAddedValue() {
            return hostStateListener.getHostAddedCount().get();
        }

        @XmlElement(name = "Removed")
        Long getHostRemovedValue() {
            return hostStateListener.getHostRemovedCount().get();
        }

        @XmlElement(name = "Down")
        Long getHostDownValue() {
            return hostStateListener.getHostDownCount().get();
        }

        @XmlElement(name = "Reactivated")
        Long getHostReactivatedValue() {
            return hostStateListener.getHostReactivatedCount().get();
        }
    }
}

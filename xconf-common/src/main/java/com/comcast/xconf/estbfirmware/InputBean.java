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
package com.comcast.xconf.estbfirmware;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.MacAddress;
import com.comcast.xconf.Environment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import com.comcast.apps.hesperius.ruleengine.domain.additional.data.IpAddress;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

/**
 * Models the request from the eSTB. We need to handle anything in the request
 * or input from STB so it's all strings, but when evaluating rules we need more
 * strong typing, thus the getInput(). Then you can do stuff like controllerId >
 * 234 or ipAddress.isInRange('31.24.122.4/22').
 */
public class InputBean {

	// private static final Logger log =
	// LoggerFactory.getLogger(InputBean.class);

	private String eStbMac;
	private String env;
	private String model;
	private String firmwareVersion;
	private String eCMMac;
	private String receiverId;
	private String controllerId;
	private String channelMapId;
	private String vodId;

	private String timeZoneOffset;
	private LocalDateTime time;
	private IpAddress ipAddress;

    private List<String> capabilities = new ArrayList<String>();

	public Input getInput() {

        Input i = new Input();

		/**
		 * Requests with invalid mac addresses are junk, we don't care about
		 * them, we just return 500 error and don't even log it.
		 */
		i.setEstbMac(new MacAddress(eStbMac));

		i.setEnv(env);

		i.setModel(model);

		i.firmwareVersion = firmwareVersion;

		/*if (MacAddress.isValidMacAddress(eCMMac)) {
			i.ecmMac = new MacAddress(eCMMac);
		}*/

		i.receiverId = receiverId;

		if (NumberUtils.isDigits(controllerId)) {
			i.controllerId = Long.valueOf(controllerId);
		}

		if (NumberUtils.isDigits(channelMapId)) {
			i.channelMapId = Long.valueOf(channelMapId);
		}

		if (NumberUtils.isDigits(vodId)) {
			i.vodId = Long.valueOf(vodId);
		}

		if (StringUtils.isNotBlank(timeZoneOffset)) {
			//i.timeZone = DataObjectMapper.offsetToTimeZone(timeZoneOffset);
		}

		i.setTime(time);

		i.ipAddress = ipAddress;

        i.capabilities = capabilities;

		return i;
	}

	/**
	 * Typed class for rules expression evaluation also with property names
	 * changed a bit to adhere to JavaBean naming convention.
	 */
	public static final class Input {

        private MacAddress estbMac;
		private String env;
		private String model;
		private String firmwareVersion;
		private MacAddress ecmMac;
		private String receiverId;
		private Long controllerId;
		private Long channelMapId;
		private Long vodId;
        private List<String> capabilities = new ArrayList<String>();

        private DateTimeZone timeZone = DateTimeZone.UTC;
		private LocalDateTime time;
		private IpAddress ipAddress;

        public boolean isRcdl() {
            return capabilities != null && capabilities.contains(Capabilities.RCDL.toString());
        }

        public boolean isRebootDecoupled() {
            return capabilities != null && capabilities.contains(Capabilities.rebootDecoupled.toString());
        }

        public boolean isSupportsFullHttpUrl() {
            return capabilities != null && capabilities.contains(Capabilities.supportsFullHttpUrl.toString());
        }

        public MacAddress getEstbMac() {
			return estbMac;
		}

		public void setEstbMac(MacAddress estbMac) {
			this.estbMac = estbMac;
		}

		public String getEnv() {
			return env;
		}

		public void setEnv(String env) {
			if (env != null) {
				this.env = new Environment(env, "").getId();
			}
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			if (model != null) {
				this.model = new Model(model, "").getId();
			}
		}

		public String getFirmwareVersion() {
			return firmwareVersion;
		}

		public void setFirmwareVersion(String firmwareVersion) {
			this.firmwareVersion = firmwareVersion;
		}

		public MacAddress getEcmMac() {
			return ecmMac;
		}

		public void setEcmMac(MacAddress ecmMac) {
			this.ecmMac = ecmMac;
		}

		public String getReceiverId() {
			return receiverId;
		}

		public void setReceiverId(String receiverId) {
			this.receiverId = receiverId;
		}

		public Long getControllerId() {
			return controllerId;
		}

		public void setControllerId(Long controllerId) {
			this.controllerId = controllerId;
		}

		public Long getChannelMapId() {
			return channelMapId;
		}

		public void setChannelMapId(Long channelMapId) {
			this.channelMapId = channelMapId;
		}

		public Long getVodId() {
			return vodId;
		}

		public void setVodId(Long vodId) {
			this.vodId = vodId;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this,
					ToStringStyle.MULTI_LINE_STYLE);
		}

		/**
		 * This value will always be non-null, it is derived as follows.
		 * <p>
		 * If "time" parameter was sent in query string, this value will be that
		 * value. No time zone offset will be applied.
		 * <p>
		 * If "time" parameter was not sent in query string, this value will be
		 * current UTC time plus time zone offset if specified.
		 * <p>
		 */
		public LocalDateTime getTime() {
			return time;
		}

		/**
		 * WARNING: time zone must be set before time.
		 */
		public void setTime(LocalDateTime time) {
			if (time == null) {
				time = new LocalDateTime(timeZone);
			}
			this.time = time;
		}

		public IpAddress getIpAddress() {
			return ipAddress;
		}

		public void setIpAddress(IpAddress ipAddress) {
			this.ipAddress = ipAddress;
		}

		/**
		 * This value will never null. It is derived as follows.
		 * <p>
		 * If a timeZoneOffset was sent, we use that offset to construct
		 * timeZone.
		 * <p>
		 * If no timeZoneOffset was sent (or if it was invalid), we set timeZone
		 * to utc.
		 * <p>
		 * If timeZone is UTC, we use the OLD and soon to be deprecated IP
		 * Address + UTC time blocking filter. If timeZone is anything other
		 * than UTC, we use the new local time based blocking filter. Once boot
		 * blocking and download scheduling are both fixed, both time based
		 * blocking filters will be deprecated.
		 */
		public DateTimeZone getTimeZone() {
			return timeZone;
		}

		public void setTimeZone(DateTimeZone timeZone) {
			this.timeZone = timeZone;
		}

		@JsonIgnore
		public boolean isUTC() {
			return getTimeZone().equals(DateTimeZone.UTC);
		}

        public List<String> getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(List<String> capabilities) {
            this.capabilities = capabilities;
        }

        public String toLogString() {
            return "estbMac=" + estbMac +
                    " env=" + env +
                    " model=" + model +
                    " reportedFirmwareVersion=" + firmwareVersion +
                    " ecmMac=" + ecmMac +
                    " receiverId=" + receiverId +
                    " capabilities=" + capabilities +
                    " timeZone=" + timeZone +
                    " time=\"" + time + "\"" +
                    " ipAddress=" + ipAddress;
        }
    }

	public String geteStbMac() {
		return eStbMac;
	}

	public void seteStbMac(String eStbMac) {
		this.eStbMac = eStbMac;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String geteCMMac() {
		return eCMMac;
	}

	public void seteCMMac(String eCMMac) {
		this.eCMMac = eCMMac;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public String getChannelMapId() {
		return channelMapId;
	}

	public void setChannelMapId(String channelMapId) {
		this.channelMapId = channelMapId;
	}

	public String getVodId() {
		return vodId;
	}

	public void setVodId(String vodId) {
		this.vodId = vodId;
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

	/**
	 * This is an optional parameter used mostly for testing to override actual
	 * local time. This is always LOCAL time. We do NOT apply time zone offset
	 * to this value. If time zone offset is sent, it is assumed to have already
	 * been applied to this time.
	 */
	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public IpAddress getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(IpAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Tells us the STB offset from UTC
	 * http://joda-time.sourceforge.net/timezones.html Will be a string like
	 * "-04:00". From this we can derive the SBT local time.
	 * <p>
	 * The normal case will be that "time" parameter is NOT sent and
	 * "timeZoneOffset" parameter IS specified. In this case we will derive STB
	 * local time from current UTC plus this offset.
	 * <p>
	 * For testing "time" parameter may be set. If it is set, it is assumed to
	 * be local time, we do not apply time zone offset to it.
	 */
	public String getTimeZoneOffset() {
		return timeZoneOffset;
	}

	public void setTimeZoneOffset(String timeZoneOffset) {
		this.timeZoneOffset = timeZoneOffset;
	}

    public List<String> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<String> capabilities) {
        this.capabilities = capabilities;
    }
}

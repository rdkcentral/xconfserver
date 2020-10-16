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
package com.comcast.xconf;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This guy sets the default time zone (for Joda Time) to UTC so it doesn't
 * matter what time zone the servers are on, everything will be in UTC.
 * <p>
 * NOTE!!! XXX WARNING future developers. All time functions must be done with
 * Joda Time (which is likely to replace Java's crappy date time implementation
 * at some point) or else time zones may be wrong.
 * <p>
 * http://joda-time.sourceforge.net/timezones.html
 * http://joda-time.sourceforge.net/userguide.html#Changing_TimeZone
 */
@Component
public class TimezoneConfigBean {

	private static final Logger log = LoggerFactory
			.getLogger(TimezoneConfigBean.class);

	@PostConstruct
	private void postConstruct() {
		DateTimeZone.setDefault(DateTimeZone.forID("UTC"));
		log.info("Time zone set to: " + DateTimeZone.getDefault());
		System.out.println("Time zone set to: " + DateTimeZone.getDefault());
	}
}

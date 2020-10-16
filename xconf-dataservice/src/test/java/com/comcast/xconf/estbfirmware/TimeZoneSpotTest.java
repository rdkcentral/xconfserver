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

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Test;

public class TimeZoneSpotTest {

	@Test
	public void millis() {
		System.out.println(new Date(1350572879931l));
		System.out.println(new Date(1350572899292l));
	}

	@Test
	public void currentTime() {
		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void utcTime() {
		System.setProperty("user.timezone", "UTC");
		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void timeZoneIsCached() {
		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());

		System.setProperty("user.timezone", "UTC");

		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void timeZoneIsCachedIndirectly() {
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());

		System.setProperty("user.timezone", "UTC");

		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(new Date());
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void timeZoneIsNotCachedMillis() {
		System.out.println(System.currentTimeMillis());

		System.setProperty("user.timezone", "UTC");

		System.out.println(Calendar.getInstance().getTimeZone().getID());
		System.out.println(System.currentTimeMillis());
	}

	@Test
	public void setFixedTime() {
		DateTimeUtils.setCurrentMillisFixed(1350572879931l);
		System.out.println(new DateTime());
	}

	@Test
	public void setFixedTimeTzFirst() {
		System.setProperty("user.timezone", "UTC");
		DateTimeUtils.setCurrentMillisFixed(1350572879931l);
		System.out.println(new DateTime());
	}

	@Test
	public void setFixedTimeTzSecond() {
		DateTimeUtils.setCurrentMillisFixed(1350572879931l);
		System.setProperty("user.timezone", "UTC");
		System.out.println(new DateTime());
	}

	@Test
	public void jodaDefaultTimeZone() {
		System.out.println("joda: " + DateTimeZone.getDefault());
		System.out.println("java: "
				+ Calendar.getInstance().getTimeZone().getID());
		System.out.println("joda ldt: " + new LocalDateTime());
		System.out.println("joda  dt: " + new DateTime());
		System.out.println("java: " + new Date());

		DateTimeZone.setDefault(DateTimeZone.UTC);
		System.out.println("joda: " + DateTimeZone.getDefault());
		System.out.println("java: "
				+ Calendar.getInstance().getTimeZone().getID());
		System.out.println("joda ldt: " + new LocalDateTime());
		System.out.println("joda  dt: " + new DateTime());
		System.out.println("java: " + new Date());

		DateTimeZone.setDefault(DateTimeZone.forID("America/New_York"));
		System.out.println("joda: " + DateTimeZone.getDefault());
		System.out.println("java: "
				+ Calendar.getInstance().getTimeZone().getID());
		System.out.println("joda ldt: " + new LocalDateTime());
		System.out.println("joda  dt: " + new DateTime());
		System.out.println("java: " + new Date());

        DateTimeZone dateTimeZone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        System.out.println("timeZone00: " + dateTimeZone);
    }
	
	@Test
	public void displayChangesToNewTimeZone() {

		System.out.println(DateTimeZone.getDefault());

		DateTimeUtils.setCurrentMillisFixed(new LocalDateTime(2012, 6, 30, 5,
				00).toDate().getTime());
		
		System.out.println(DateTimeUtils.currentTimeMillis());

		System.out.println("ldt: " + new LocalDateTime());
		System.out.println(" dt: " + new DateTime());
		
		DateTimeZone.setDefault(DateTimeZone.UTC);
		System.out.println(DateTimeZone.getDefault());

		System.out.println("ldt: " + new LocalDateTime());
		System.out.println(" dt: " + new DateTime());
	}

	@Test
	public void displayChangesToNewTimeZoneB() {

		System.out.println(DateTimeZone.getDefault());

		DateTimeZone.setDefault(DateTimeZone.UTC);
		System.out.println(DateTimeZone.getDefault());

		DateTimeUtils.setCurrentMillisFixed(new LocalDateTime(2012, 6, 30, 5,
				00).toDate().getTime());

		System.out.println(DateTimeUtils.currentTimeMillis());

		System.out.println("ldt: " + new LocalDateTime());
		System.out.println(" dt: " + new DateTime());
		
		DateTimeZone.setDefault(DateTimeZone.UTC);
		System.out.println(DateTimeZone.getDefault());
		
		System.out.println("ldt: " + new LocalDateTime());
		System.out.println(" dt: " + new DateTime());
	}
}
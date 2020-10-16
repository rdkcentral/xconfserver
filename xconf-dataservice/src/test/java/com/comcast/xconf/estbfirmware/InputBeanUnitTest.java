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

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class InputBeanUnitTest {

	static DateTime now;

	@BeforeClass
	public static void bc() {
		System.setProperty("user.timezone", "UTC");
		DateTimeUtils.setCurrentMillisFixed(1341046800000L);
		now = new DateTime();
		System.out.println("now: " + now);
	}

	@Test
	public void baseline() {
		InputBean b = testBean();
		Assert.assertEquals(new LocalDateTime(2012, 6, 30, 9, 0), b.getInput()
				.getTime());
		Assert.assertEquals("2012-06-30T09:00:00.000", b.getInput().getTime()
				.toString());
		Assert.assertTrue(b.getInput().isUTC());
		System.out.println(b.getInput());
	}

	@Test
	public void testFakeTimeOk() {
		InputBean b = testBean();
		b.setTime(new LocalDateTime(2012, 11, 11, 11, 11));
		Assert.assertEquals(new LocalDateTime(2012, 11, 11, 11, 11), b
				.getInput().getTime());
	}

	@Test
	public void testFakeTimeOkWTimeZone() {
		InputBean b = testBean();
		b.setTimeZoneOffset("-04:00");
		b.setTime(new LocalDateTime(2012, 11, 11, 11, 11));
		Assert.assertEquals(new LocalDateTime(2012, 11, 11, 11, 11), b
				.getInput().getTime());
	}

	@Test
	@Ignore
	public void testTimeReflectsOffset() {
		InputBean b = testBean();
		b.setTimeZoneOffset("-04:00");
		Assert.assertEquals("2012-06-30T05:00:00.000", b.getInput().getTime()
				.toString());
		Assert.assertEquals(new LocalDateTime(2012, 6, 30, 5, 0), b.getInput()
				.getTime());
	}

	@Test
	public void testBadOffsetDefaultsToUTC() {
		InputBean b = testBean();
		b.setTimeZoneOffset("2343");
		Assert.assertTrue(b.getInput().isUTC());
	}

	@Test
	@Ignore
	public void testValidOffsetIsNotUTC() {
		InputBean b = testBean();
		b.setTimeZoneOffset("-04:00");
		Assert.assertFalse(b.getInput().isUTC());
	}

	/*
	 * helper methods
	 */

	public InputBean testBean() {
		InputBean b = new InputBean();
		b.seteStbMac("aaaaaaaaaaaa");
		return b;
	}

	public static LocalDateTime ldt() {
		LocalDateTime t = new LocalDateTime();
		System.out.println(t);
		return t;
	}

	public static LocalTime lt() {
		LocalTime t = new LocalTime();
		System.out.println(t);
		return t;
	}

	public static DateTime dt() {
		DateTime t = new DateTime();
		System.out.println(t);
		return t;
	}
}

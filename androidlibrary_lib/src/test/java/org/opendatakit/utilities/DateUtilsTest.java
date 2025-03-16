/*
 * Copyright (C) 2015 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.utilities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class DateUtilsTest {

  private static DateUtils dateUtils;
  private static TimeZone timezone;

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());

    // Create a DateUtils instance with US locale and the first available timezone
    timezone = TimeZone.getTimeZone(TimeZone.getAvailableIDs()[0]);
    dateUtils = new DateUtils(Locale.US, timezone);
  }

  @Test
  public void testDateInterpretation() {
    String value = dateUtils.validifyDateValue("3/4/2015");

    String expected = "2015-03-04T";
    assertEquals(expected, value.substring(0, expected.length()));
  }

  @Test
  public void testValidifyDateValue_Now() {
    // Get current time for comparison
    DateTime now = new DateTime(DateTimeZone.forTimeZone(timezone));
    String result = dateUtils.validifyDateValue("now");

    assertNotNull("Should parse 'now'", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Extract date and time components for comparison
    String dateComponent = result.substring(0, 10); // YYYY-MM-DD
    String nowDateStr = now.toString("yyyy-MM-dd");
    assertEquals("Date should match current date", nowDateStr, dateComponent);
  }

  @Test
  public void testValidifyDateValue_RelativeTime() {
    // Test relative time in the future
    String result = dateUtils.validifyDateValue("now + 1h");
    assertNotNull("Should parse relative time in future", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Test relative time in the past
    result = dateUtils.validifyDateValue("now - 30m");
    assertNotNull("Should parse relative time in past", result);
    assertTrue("Format should be valid", isDbFormatValid(result));
  }

  @Test
  public void testValidifyDateValue_StandardFormats() {
    // Test ISO format
    String result = dateUtils.validifyDateValue("2023-05-15T14:30:45.123-0700");
    assertNotNull("Should parse ISO format", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Test US format
    result = dateUtils.validifyDateValue("5/15/2023 2:30:45PM");
    assertNotNull("Should parse US format", result);
    assertTrue("Format should be valid", isDbFormatValid(result));
  }

  @Test
  public void testValidifyDateValue_PartialDateFormats() {
    // Test partial date with only minutes precision
    String result = dateUtils.validifyDateValue("5/15/2023 2:30PM");
    assertNotNull("Should parse date with minute precision", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Test partial date with only hour precision
    result = dateUtils.validifyDateValue("5/15/2023 2PM");
    assertNotNull("Should parse date with hour precision", result);
    assertTrue("Format should be valid", isDbFormatValid(result));
  }

  @Test
  public void testValidifyDateValue_SpecialKeywords() {
    // Test "today"
    String result = dateUtils.validifyDateValue("today");
    assertNotNull("Should parse 'today'", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Test "yesterday"
    result = dateUtils.validifyDateValue("yesterday");
    assertNotNull("Should parse 'yesterday'", result);
    assertTrue("Format should be valid", isDbFormatValid(result));

    // Test "tomorrow"
    result = dateUtils.validifyDateValue("tomorrow");
    assertNotNull("Should parse 'tomorrow'", result);
    assertTrue("Format should be valid", isDbFormatValid(result));
  }

  @Test
  public void testValidifyDateValue_InvalidFormats() {
    // Test completely invalid format
    String result = dateUtils.validifyDateValue("not a date");
    assertNull("Should return null for invalid format", result);

    // Test malformed relative time
    result = dateUtils.validifyDateValue("now + 1x");
    assertNull("Should return null for invalid relative time", result);
  }

  @Test
  public void testFormatDateTimeForDb() {
    // Create DateTime in the timezone being used by DateUtils
    DateTime testDate = new DateTime(2023, 5, 15, 14, 30, 45, 123, DateTimeZone.forTimeZone(timezone));
    String result = dateUtils.formatDateTimeForDb(testDate);

    assertTrue("Format should be valid", isDbFormatValid(result));
    // Check only the date part to avoid timezone issues
    assertTrue("Should contain the correct date", result.startsWith("2023-05-15T"));
    // Check time with more flexibility because of potential timezone conversions
    assertTrue("Should contain correct time components",
            result.substring(11).matches("\\d{2}:\\d{2}:\\d{2}\\.123000000"));
  }

  /**
   * Helper method to check if a string is in the expected database format
   * Expected format: yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS
   */
  private boolean isDbFormatValid(String dateString) {
    return dateString.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{9}");
  }
}
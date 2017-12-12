/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.utilities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.opendatakit.aggregate.odktables.rest.TableConstants;

import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used in OdkDatabaseServiceTest
 */
@SuppressWarnings("WeakerAccess")
public class DateUtils {

  @SuppressWarnings("unused") private static final String TAG = DateUtils.class.getSimpleName();

  private static final String[] USER_FULL_DATETIME_PATTERNS = {
      "yyyy-MM-dd'T'HH:mm:ss.SSSZ", // ODK Collect format
      "M/d/yy h:mm:ssa",
      "M/d/yy HH:mm:ss",
      "M/d/yyyy h:mm:ssa",
      "M/d/yyyy HH:mm:ss",
      "M/d h:mm:ssa",
      "M/d HH:mm:ss",
      "d h:mm:ssa",
      "d HH:mm:ss",
      "E h:mm:ssa",
      "E HH:mm:ss",
      "HH:mm:ss.SSSZ" // ODK Collect format for time
  };
  private static final String[][] USER_PARTIAL_DATETIME_PATTERNS = {
    {
      // minute
      "M/d/yy h:mma",
      "M/d/yy HH:mm",
      "M/d/yyyy h:mma",
      "M/d/yyyy HH:mm",
      "M/d h:mma",
      "M/d HH:mm",
      "d h:mma",
      "d HH:mm",
      "E h:mma",
      "E HH:mm"
    },
    {
      // hour
      "M/d/yy ha",
      "M/d/yy HH",
      "M/d/yyyy ha",
      "M/d/yyyy HH",
      "M/d ha",
      "M/d HH",
      "d ha",
      "d HH",
      "E ha",
      "E HH"
    },
    {
      // day
      "yyyy-MM-dd", // ODK Collect format for date
      "M/d/yy",
      "M/d/yyyy",
      "M/d",
      "d",
      "E"
    }
  };
  private static final int[] USER_INTERVAL_DURATIONS = {60, 3600, 86400};

  private static final Pattern USER_DURATION_FORMAT =
      Pattern.compile("(\\d+)(s|m|h|d)");

  private static final Pattern USER_NOW_RELATIVE_FORMAT =
      Pattern.compile("^now\\s*(-|\\+)\\s*(\\d+(s|m|h|d))$");

  private final Locale locale;
  private final DateTimeFormatter userFullParser;
  private final DateTimeFormatter[] userPartialParsers;

  public DateUtils(Locale locale, TimeZone tz) {
    this.locale = locale;
    DateTimeZone timeZone = DateTimeZone.forTimeZone(tz);
    DateTimeFormatterBuilder fpBuilder = new DateTimeFormatterBuilder();
    for (String pattern : USER_FULL_DATETIME_PATTERNS) {
      DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
      fpBuilder.appendOptional(f.getParser());
    }
    userFullParser = fpBuilder.toFormatter().withLocale(locale).withZone(timeZone);
    userPartialParsers = new DateTimeFormatter[USER_PARTIAL_DATETIME_PATTERNS.length];
    for (int i = 0; i < USER_PARTIAL_DATETIME_PATTERNS.length; i++) {
      DateTimeFormatterBuilder dtfb = new DateTimeFormatterBuilder();
      for (String pattern : USER_PARTIAL_DATETIME_PATTERNS[i]) {
        DateTimeFormatter f = DateTimeFormat.forPattern(pattern);
        dtfb.appendOptional(f.getParser());
      }
      userPartialParsers[i] = dtfb.toFormatter().withLocale(locale).withZone(timeZone);
    }
  }

  public String validifyDateValue(String input) {
    DateTime instant = tryParseInstant(input);
    if (instant != null) {
      return formatDateTimeForDb(instant);
    }
    Interval interval = tryParseInterval(input);
    if (interval != null) {
      return formatDateTimeForDb(interval.getStart());
    }
    return null;
  }

  private DateTime tryParseInstant(String input) {
    input = input.trim();
    if ("now".equalsIgnoreCase(input)) {
      return new DateTime();
    }
    Matcher matcher = USER_NOW_RELATIVE_FORMAT.matcher(input);
    if (matcher.matches()) {
      int delta = tryParseDuration(matcher.group(2));
      if (delta < 0) {
        return null;
      } else if ("-".equals(matcher.group(1))) {
        return new DateTime().minusSeconds(delta);
      } else {
        return new DateTime().plusSeconds(delta);
      }
    }
    try {
      return userFullParser.parseDateTime(input);
    } catch (IllegalArgumentException ignored) {
      // TODO
    }
    //if (!locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
    //  return null;
    //}
    return null;
  }

  private Interval tryParseInterval(String input) {
    for (int i = 0; i < userPartialParsers.length; i++) {
      try {
        DateTime start = userPartialParsers[i].parseDateTime(input);
        DateTime end = start.plusSeconds(USER_INTERVAL_DURATIONS[i]);
        return new Interval(start, end);
      } catch (IllegalArgumentException ignored) {
        // TODO
      }
    }
    if (!locale.getLanguage().equals(Locale.ENGLISH.getLanguage())) {
      return null;
    }
    DateTime start = new DateTime().withTimeAtStartOfDay();
    boolean match = false;
    if ("today".equalsIgnoreCase(input)) {
      match = true;
    } else if ("yesterday".equalsIgnoreCase(input)) {
      start = start.minusDays(1);
      match = true;
    } else if ("tomorrow".equalsIgnoreCase(input) || "tmw".equalsIgnoreCase(input)) {
      start = start.plusDays(1);
      match = true;
    }
    if (match) {
      DateTime end = start.plusDays(1);
      return new Interval(start, end);
    }
    return null;
  }

  private int tryParseDuration(String input) {
    Matcher matcher = USER_DURATION_FORMAT.matcher(input);
    if (!matcher.matches()) {
      return -1;
    }
    int quant = Integer.parseInt(matcher.group(1));
    char unit = matcher.group(2).charAt(0);
    switch (unit) {
    case 's':
      return quant;
    case 'm':
      return quant * 60;
    case 'h':
      return quant * 3600;
    case 'd':
      return quant * 86400;
    default:
      return -1;
    }
  }

  public String formatDateTimeForDb(ReadableInstant dt) {
    return TableConstants.nanoSecondsFromMillis(dt.getMillis());
  }
}

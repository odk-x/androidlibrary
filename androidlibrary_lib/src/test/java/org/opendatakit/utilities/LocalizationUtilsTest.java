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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class LocalizationUtilsTest {

  String appName = "default";
  String tableId = null;

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testHackedName() {
    Locale defaultLocale = Locale.getDefault();
    String full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    assertEquals("aname", LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale,
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("aname"))));
    assertEquals("a name", LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale,
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("a_name"))));
    assertEquals("_ an am e", LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale,
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("_an_am_e"))));
    assertEquals("an ame _", LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale,
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("an_ame_"))));
  }

  @Test
  public void testNormalizeDisplayName() throws JsonProcessingException {
    Map<String,Object> langMap = new TreeMap<String,Object>();
    langMap.put("en_US", "This is a test");
    langMap.put("en_GB", "Test is This");
    langMap.put("en", "Huh Test");
    langMap.put("fr", "Je suis");
    langMap.put("default", "No way!");
    Map<String,Object> topMap = new TreeMap<String,Object>();
    topMap.put("text", langMap);
    String value = ODKFileUtils.mapper.writeValueAsString(topMap);

    Locale defaultLocale;
    String full_locale;
    String match;

    Locale.setDefault(Locale.US);
    defaultLocale = Locale.getDefault();
    full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    match = LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale, value);
    assertEquals("This is a test", match);

    Locale.setDefault(Locale.UK);
    defaultLocale = Locale.getDefault();
    full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    match = LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale, value);
    assertEquals("Test is This", match);

    Locale.setDefault(Locale.CANADA);
    defaultLocale = Locale.getDefault();
    full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    match = LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale, value);
    assertEquals("Huh Test", match);

    Locale.setDefault(Locale.CANADA_FRENCH);
    defaultLocale = Locale.getDefault();
    full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    match = LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale, value);
    assertEquals("Je suis", match);

    Locale.setDefault(Locale.GERMANY);
    defaultLocale = Locale.getDefault();
    full_locale = defaultLocale.getLanguage() + "_" + defaultLocale.getCountry();
    match = LocalizationUtils.getLocalizedDisplayName(appName, tableId, full_locale, value);
    assertEquals("No way!", match);

    Locale.setDefault(Locale.US);
  }
}

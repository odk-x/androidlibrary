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
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class LocalizationUtilsTest {

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testHackedName() {
    assertEquals("aname", LocalizationUtils.getLocalizedDisplayName(
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("aname"))));
    assertEquals("a name", LocalizationUtils.getLocalizedDisplayName(
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("a_name"))));
    assertEquals("_ an am e", LocalizationUtils.getLocalizedDisplayName(
        NameUtil.normalizeDisplayName(NameUtil.constructSimpleDisplayName("_an_am_e"))));
    assertEquals("an ame _", LocalizationUtils.getLocalizedDisplayName(
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
    String value = ODKFileUtils.mapper.writeValueAsString(langMap);

    String match;

    Locale.setDefault(Locale.US);
    match = LocalizationUtils.getLocalizedDisplayName(value);
    assertEquals("This is a test", match);

    Locale.setDefault(Locale.UK);
    match = LocalizationUtils.getLocalizedDisplayName(value);
    assertEquals("Test is This", match);

    Locale.setDefault(Locale.CANADA);
    match = LocalizationUtils.getLocalizedDisplayName(value);
    assertEquals("Huh Test", match);

    Locale.setDefault(Locale.CANADA_FRENCH);
    match = LocalizationUtils.getLocalizedDisplayName(value);
    assertEquals("Je suis", match);

    Locale.setDefault(Locale.GERMANY);
    match = LocalizationUtils.getLocalizedDisplayName(value);
    assertEquals("No way!", match);

    Locale.setDefault(Locale.US);
  }

  @Test
  public void testNormalizeDisplayName2() {
    Map<String,Object> langMap = new TreeMap<String,Object>();
    langMap.put("en_US", "This is a test");
    langMap.put("en_GB", "Test is This");
    langMap.put("en", "Huh Test");
    langMap.put("fr", "Je suis");
    langMap.put("default", "No way!");

    String match;

    Locale.setDefault(Locale.US);
    match = LocalizationUtils.getLocalizedDisplayName(langMap);
    assertEquals("This is a test", match);

    Locale.setDefault(Locale.UK);
    match = LocalizationUtils.getLocalizedDisplayName(langMap);
    assertEquals("Test is This", match);

    Locale.setDefault(Locale.CANADA);
    match = LocalizationUtils.getLocalizedDisplayName(langMap);
    assertEquals("Huh Test", match);

    Locale.setDefault(Locale.CANADA_FRENCH);
    match = LocalizationUtils.getLocalizedDisplayName(langMap);
    assertEquals("Je suis", match);

    Locale.setDefault(Locale.GERMANY);
    match = LocalizationUtils.getLocalizedDisplayName(langMap);
    assertEquals("No way!", match);

    Locale.setDefault(Locale.US);
  }
}

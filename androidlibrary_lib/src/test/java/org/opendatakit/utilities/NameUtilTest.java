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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class NameUtilTest {

  @BeforeClass
  public static void oneTimeSetUp() throws Exception {
    StaticStateManipulator.get().reset();
    WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
  }

  @Test
  public void testValidName() {
    assertEquals(false, NameUtil.isValidUserDefinedDatabaseName("select"));
    assertEquals(false, NameUtil.isValidUserDefinedDatabaseName("1select"));
    assertEquals(false, NameUtil.isValidUserDefinedDatabaseName("1ss elect"));
    assertEquals(true, NameUtil.isValidUserDefinedDatabaseName("ss_elect"));
    assertEquals(true, NameUtil.isValidUserDefinedDatabaseName("ss_elect3"));
  }

  @Test
  public void testDisplayName() {
    assertEquals("{\"text\":\"aname\"}", NameUtil.constructSimpleDisplayName("aname"));
    assertEquals("{\"text\":\"a name\"}", NameUtil.constructSimpleDisplayName("a_name"));
    assertEquals("{\"text\":\"_ an am e\"}", NameUtil.constructSimpleDisplayName("_an_am_e"));
    assertEquals("{\"text\":\"an ame _\"}", NameUtil.constructSimpleDisplayName("an_ame_"));
  }

  @Test
  public void testNormalizeDisplayName() {
    assertEquals("\"aname\"", NameUtil.normalizeDisplayName("aname"));
    assertEquals("\"a name\"", NameUtil.normalizeDisplayName("\"a name\""));
    assertEquals("{\"text\":{\"default\":\"_an_am_e\"}\"}",
        NameUtil.normalizeDisplayName("{\"text\":{\"default\":\"_an_am_e\"}\"}"));
  }
}

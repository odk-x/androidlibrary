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

package org.opendatakit.data;

import android.graphics.Color;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.utilities.StaticStateManipulator;

@RunWith(JUnit4.class)
public class ColorRuleTest {

   @BeforeClass
   public static void oneTimeSetUp() throws Exception {
      StaticStateManipulator.get().reset();
      WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
   }

   @Test
   public void testColorRule() {
      ColorRule cr1 = new ColorRule("myElement", ColorRule.RuleType.EQUAL, "5", Color.BLUE, Color
          .WHITE);
      ColorRule cr2 = new ColorRule("myElement", ColorRule.RuleType.EQUAL, "5", Color.BLUE, Color
          .WHITE);

      Assert.assertTrue(cr1.equalsWithoutId(cr2));
      Assert.assertTrue(cr2.equalsWithoutId(cr1));

      Assert.assertEquals(Color.WHITE, cr1.getBackground());
      Assert.assertEquals(Color.BLUE, cr1.getForeground());
      Assert.assertEquals(ColorRule.RuleType.EQUAL, cr1.getOperator());
      Assert.assertEquals("myElement", cr1.getColumnElementKey());
      Assert.assertEquals("5", cr1.getVal());

      String crs1 = cr1.toString();
      String crs2 = cr2.toString();
      Assert.assertEquals(crs1.substring(crs1.indexOf(',')), crs2.substring((crs2.indexOf(','))));

      Assert.assertNotEquals(cr1.getRuleId(), cr2.getRuleId());
      Assert.assertNotEquals(cr1, cr2);

      cr2.setVal("6");
      Assert.assertFalse(cr1.equalsWithoutId(cr2));
      Assert.assertEquals("6", cr2.getVal());
      cr1.setVal("6");

      Assert.assertTrue(cr1.equalsWithoutId(cr2));

      cr2.setBackground(Color.GREEN);
      Assert.assertFalse(cr1.equalsWithoutId(cr2));
      Assert.assertEquals(Color.GREEN, cr2.getBackground());
      cr1.setBackground(Color.GREEN);

      Assert.assertTrue(cr1.equalsWithoutId(cr2));

      cr2.setForeground(Color.RED);
      Assert.assertFalse(cr1.equalsWithoutId(cr2));
      Assert.assertEquals(Color.RED, cr2.getForeground());
      cr1.setForeground(Color.RED);

      Assert.assertTrue(cr1.equalsWithoutId(cr2));

      cr2.setOperator(ColorRule.RuleType.GREATER_THAN);
      Assert.assertFalse(cr1.equalsWithoutId(cr2));
      Assert.assertEquals(ColorRule.RuleType.GREATER_THAN, cr2.getOperator());
      cr1.setOperator(ColorRule.RuleType.GREATER_THAN);

      Assert.assertTrue(cr1.equalsWithoutId(cr2));

      cr2.setColumnElementKey("fredColumn");
      Assert.assertFalse(cr1.equalsWithoutId(cr2));
      Assert.assertEquals("fredColumn", cr2.getColumnElementKey());
      cr1.setColumnElementKey("fredColumn");

      Assert.assertTrue(cr1.equalsWithoutId(cr2));
   }

}

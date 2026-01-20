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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.opendatakit.data.ColorRule.RuleType.getValues;

import android.graphics.Color;

import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.util.TreeMap;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.database.data.BaseTable;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.data.Row;
import org.opendatakit.database.data.TypedRow;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.logging.desktop.WebLoggerDesktopFactoryImpl;
import org.opendatakit.utilities.StaticStateManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RunWith(JUnit4.class)
public class ColorRuleTest {

   //Constants
   private static final String APP_NAME = "colorRuleTest";
   private static final String TABLE_ID_1 = "myTableId_1";
   private static final String COLOR_COL = "Color_Col";
   private static final String MY_ELEMENT = "myElement";
   private static final String MY_ELEMENT_1 = "myElement1";
   private static final String MY_ELEMENT_2 = "myElement2";
   private static final String MY_ELEMENT_3 = "myElement3";
   private static final String MY_ELEMENT_4 = "myElement4";
   private static final String MY_ELEMENT_5 = "myElement5";
   private static final String MY_ELEMENT_6 = "myElement6";
   private static final String EQUAL_SYMBOL = "=";
   private static final String LESS_THAN_SYMBOL = "<";
   private static final String LESS_THAN_OR_EQUAL_SYMBOL = "<=";
   private static final String GREATER_THAN_OR_EQUAL_SYMBOL = ">=";
   private static final String GREATER_THAN_SYMBOL = ">";
   //

   String ruleId = UUID.randomUUID().toString();
   ColorRule cr;

   @BeforeClass
   public static void oneTimeSetUp() {
      StaticStateManipulator.get().reset();
      WebLogger.setFactory(new WebLoggerDesktopFactoryImpl());
   }

   @Before
   public void setupColorRule(){
      cr = new ColorRule(ruleId, MY_ELEMENT,  ColorRule.RuleType.EQUAL, "1", Color.YELLOW, Color.BLACK);
   }
   @After
   public void tearDownColorRule(){
      cr = null;
   }

   @Test
   public void testGetJsonRepresentation() {
      ColorRule cr = new ColorRule("myElement", ColorRule.RuleType.EQUAL, "5", 0, 0);
      TreeMap<String, Object> jsonMap = cr.getJsonRepresentation();

      Assert.assertEquals("5", jsonMap.get("mValue"));
      Assert.assertEquals("myElement", jsonMap.get("mElementKey"));
      Assert.assertEquals(ColorRule.RuleType.EQUAL.name(), jsonMap.get("mOperator"));
      Assert.assertNotNull(jsonMap.get("mId"));
      Assert.assertEquals(0, jsonMap.get("mForeground"));
      Assert.assertEquals(0, jsonMap.get("mBackground"));
   }

   @Test
   public void testColorRule() {
      ColorRule cr1 = new ColorRule(MY_ELEMENT, ColorRule.RuleType.EQUAL, "5", Color.BLUE, Color
          .WHITE);
      ColorRule cr2 = new ColorRule(MY_ELEMENT, ColorRule.RuleType.EQUAL, "5", Color.BLUE, Color
          .WHITE);

      Assert.assertTrue(cr1.equalsWithoutId(cr2));
      Assert.assertTrue(cr2.equalsWithoutId(cr1));

      Assert.assertEquals(Color.WHITE, cr1.getBackground());
      Assert.assertEquals(Color.BLUE, cr1.getForeground());
      Assert.assertEquals(ColorRule.RuleType.EQUAL, cr1.getOperator());
      Assert.assertEquals(MY_ELEMENT, cr1.getColumnElementKey());
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

   /**getSymbol() test.
    * Acceptance:
    * Given: color rule with a valid rule type
    * When: asked for the rule symbol
    * Then: Return the correct string value
    * [RULE_TYPE: EXPECTED_SYMBOL] = {EQUAL: =, LESS_THAN: <, LESS_THAN_OR_EQUAL: <=, GREATER_THAN_OR_EQUAL: >=, GREATER_THAN: >}
   */
   @Test
   public void givenColorRuleType_whenGetSymbolIsCalled_thenReturnCorrectSymbol() {
      assertEquals(EQUAL_SYMBOL, cr.getOperator().getSymbol());

      cr.setOperator(ColorRule.RuleType.LESS_THAN);
      assertEquals(LESS_THAN_SYMBOL, cr.getOperator().getSymbol());

      cr.setOperator(ColorRule.RuleType.LESS_THAN_OR_EQUAL);
      assertEquals(LESS_THAN_OR_EQUAL_SYMBOL, cr.getOperator().getSymbol());

      cr.setOperator(ColorRule.RuleType.GREATER_THAN);
      assertEquals(GREATER_THAN_SYMBOL, cr.getOperator().getSymbol());

      cr.setOperator(ColorRule.RuleType.GREATER_THAN_OR_EQUAL);
      assertEquals(GREATER_THAN_OR_EQUAL_SYMBOL, cr.getOperator().getSymbol());
   }

   /**getValues() test.
    * Acceptance:
    * Given: a list of strings
    * When: the list is accessed
    * Then: the list has the rule type symbols in the correct order
    * [STRING_VALUE: EXPECTED_RULE_TYPE] = {=: EQUAL, <: LESS_THAN, <=: LESS_THAN_OR_EQUAL, >=: GREATER_THAN_OR_EQUAL,  >: GREATER_THAN}
    */
   @Test
   public void givenCharSequenceOfValues_whenSequenceIsAccessed_thenReturnRuleTypeSymbolInCorrectOrder() {
      CharSequence[] ruleTypeStrings = getValues();
      assertEquals(LESS_THAN_SYMBOL, ruleTypeStrings[0].toString());
      assertEquals(LESS_THAN_OR_EQUAL_SYMBOL, ruleTypeStrings[1].toString());
      assertEquals(EQUAL_SYMBOL, ruleTypeStrings[2].toString());
      assertEquals(GREATER_THAN_OR_EQUAL_SYMBOL, ruleTypeStrings[3].toString());
      assertEquals(GREATER_THAN_SYMBOL, ruleTypeStrings[4].toString());
   }

      /**getEnumFromString() test.
       * Acceptance:
       * Given: a string value AND string is a valid rule symbol
       * When: corresponding rule type is fetched
       * Then: return the correct rule type
       * [STRING_VALUE: EXPECTED_RULE_TYPE] = {=: EQUAL, <: LESS_THAN, <=: LESS_THAN_OR_EQUAL, >=: GREATER_THAN_OR_EQUAL,  >: GREATER_THAN}
       */
   @Test
   public void givenValidStringValue_whenGetEnumIsCalled_thenReturnCorrectRuleType() {
      assertEquals(ColorRule.RuleType.LESS_THAN, ColorRule.RuleType.getEnumFromString(LESS_THAN_SYMBOL));
      assertEquals(ColorRule.RuleType.LESS_THAN_OR_EQUAL, ColorRule.RuleType.getEnumFromString(LESS_THAN_OR_EQUAL_SYMBOL));
      assertEquals(ColorRule.RuleType.EQUAL, ColorRule.RuleType.getEnumFromString(EQUAL_SYMBOL));
      assertEquals(ColorRule.RuleType.GREATER_THAN_OR_EQUAL, ColorRule.RuleType.getEnumFromString(GREATER_THAN_OR_EQUAL_SYMBOL));
      assertEquals(ColorRule.RuleType.GREATER_THAN, ColorRule.RuleType.getEnumFromString(GREATER_THAN_SYMBOL));
      assertEquals(ColorRule.RuleType.NO_OP, ColorRule.RuleType.getEnumFromString(""));
   }

   /**getEnumFromString() test.
       * Acceptance:
       * Given: a string value AND string is not a valid rule symbol
       * When: corresponding rule type is fetched
       * Then: throw an IllegalArgumentException with the correct error message
    **/
   @Test
   public void givenInvalidStringValue_whenGetEnumIsCalled_thenReturnCorrectRuleType() {
      assertThrows(IllegalArgumentException.class, () -> ColorRule.RuleType.getEnumFromString("odk"));
   }

   //Test to show if the returned Json representation for ColorRule is correct
   /**getJsonRepresentation() test.
    * Acceptance:
    * Given: a colorRule
    * When: asked for the json representation
    * Then: return a map of the color rule attributes to their corresponding value
    **/
   @Test
   public void givenColorRule_whenJsonRepresentationRequested_thenReturnMapOfAttributesToValues(){
      TreeMap<String,Object> expected = new TreeMap<>();
      expected.put("mValue", "1");
      expected.put("mElementKey", MY_ELEMENT);
      expected.put("mOperator", "EQUAL");
      expected.put("mId", ruleId);
      expected.put("mForeground", Color.YELLOW);
      expected.put("mBackground", Color.BLACK);
      assertEquals(expected, cr.getJsonRepresentation());
   }

   /**toString() test.
    * Acceptance:
    * Given: a colorRule
    * When: asked for the string representation
    * Then: return a string containing assignment of the color rule value to their corresponding
    attributes, each separated by comma(,).
    **/
   @Test
   public void givenColorRule_whenStringRequested_thenReturnStringOfAttributesToValuesAssignmentSeparatedByComma() {
      String expected = "[id="+ruleId+", elementKey=myElement, operator=EQUAL, value=1, background=-16777216, foreground=-256]";
      assertEquals(expected, cr.toString());
   }

   /**checkMatch() test.
    * Acceptance:
    * Given: a colorRule that exists in a table row AND has a valid operator
    * When: searched for with a specified element type in a table row of valid values
    * Then: return true if match found with correct type.
    **/
   @Test
   public void givenValidColorRuleInTableRow_whenRowSearched_thenReturnTrue() {
      TypedRow rowToMatch = setupTableWithRowEntriesAndReturnTypedRow(
              new String[]{"1","1","3","5","5","false"}
      );
      //Check that all RuleTypes work with integer or number type
      updateColorRule(MY_ELEMENT_1, "1", ColorRule.RuleType.EQUAL);
      assertTrue(cr.checkMatch(ElementDataType.integer, rowToMatch));
      updateColorRule(MY_ELEMENT_2, "2", ColorRule.RuleType.LESS_THAN);
      assertTrue(cr.checkMatch(ElementDataType.integer, rowToMatch));
      updateColorRule(MY_ELEMENT_3, "3", ColorRule.RuleType.LESS_THAN_OR_EQUAL);
      assertTrue(cr.checkMatch(ElementDataType.integer, rowToMatch));
      updateColorRule(MY_ELEMENT_4, "4", ColorRule.RuleType.GREATER_THAN);
      assertTrue(cr.checkMatch(ElementDataType.integer, rowToMatch));
      updateColorRule(MY_ELEMENT_5, "5", ColorRule.RuleType.GREATER_THAN_OR_EQUAL);
      assertTrue(cr.checkMatch(ElementDataType.number, rowToMatch));
      updateColorRule(MY_ELEMENT_6, "true", ColorRule.RuleType.LESS_THAN);
      assertTrue(cr.checkMatch(ElementDataType.bool, rowToMatch));
   }

   /**checkMatch() test.
    * Acceptance:
    * Given: a colorRule that doesn't exist in a table row OR has an invalid operator OR has a value with unexpected type
    * When: searched for with the expected element type in the table row
    * Then: return false.
    **/
   @Test
   public void givenValidColorRuleInTableRow_whenRowSearched_andOperatorIsNoOp_orDiffElementTypeSpec_thenReturnFalse() {
      TypedRow rowToMatch = setupTableWithRowEntriesAndReturnTypedRow(new String[]{"","[44,67]","4","3",null,"odk"});
      //Check that RuleTypes work with non-numeric types
      updateColorRule(MY_ELEMENT_1, "1", ColorRule.RuleType.EQUAL);
      assertFalse(cr.checkMatch(ElementDataType.string, rowToMatch));
      updateColorRule(MY_ELEMENT_2, "[2,3,4]", ColorRule.RuleType.LESS_THAN_OR_EQUAL);
      assertFalse(cr.checkMatch(ElementDataType.array, rowToMatch));
      updateColorRule(MY_ELEMENT_3, "4", ColorRule.RuleType.GREATER_THAN);
      assertFalse(cr.checkMatch(ElementDataType.string, rowToMatch));
      updateColorRule(MY_ELEMENT_4, "5", ColorRule.RuleType.GREATER_THAN_OR_EQUAL);
      assertFalse(cr.checkMatch(ElementDataType.string, rowToMatch));
      updateColorRule(MY_ELEMENT_5, "5", ColorRule.RuleType.NO_OP);
      assertFalse(cr.checkMatch(ElementDataType.string, rowToMatch));
   }
   @Test
   public void givenInValidColorRuleInTableRow_whenRowSearched_thenThrowException() {
      TypedRow rowToMatch = setupTableWithRowEntriesAndReturnTypedRow(new String[]{"odk"});
      updateColorRule(MY_ELEMENT_1, "1", ColorRule.RuleType.NO_OP);
      assertThrows(IllegalArgumentException.class, () -> cr.checkMatch(ElementDataType.string, rowToMatch));
   }

   private TypedRow setupTableWithRowEntriesAndReturnTypedRow(String[] rowEntries){
      //Setup Color table
      BaseTable table = getBaseTable();
      //Define the table's column
      List<Column> columns = new ArrayList<>();
      columns.add(new Column(COLOR_COL, COLOR_COL, ElementDataType.integer.name(), null));
      OrderedColumns orderedColumns = new OrderedColumns(APP_NAME, TABLE_ID_1, columns);
      //Define the table's rows
      Row row;
      row= new Row(rowEntries, table);
      table.addRow(row);
      return new TypedRow(table.getRowAtIndex(0),orderedColumns);
   }

   @NonNull
   private static BaseTable getBaseTable() {
      String[] primaryKey = {"id"};
      String[] elementKeys = {
              MY_ELEMENT_1, MY_ELEMENT_2, MY_ELEMENT_3, MY_ELEMENT_4, MY_ELEMENT_5, MY_ELEMENT_6
      };
      HashMap<String, Integer> elementKeyToIndex = new HashMap<>();
      elementKeyToIndex.put(MY_ELEMENT_1,0);
      elementKeyToIndex.put(MY_ELEMENT_2,1);
      elementKeyToIndex.put(MY_ELEMENT_3,2);
      elementKeyToIndex.put(MY_ELEMENT_4,3);
      elementKeyToIndex.put(MY_ELEMENT_5,4);
      elementKeyToIndex.put(MY_ELEMENT_6,5);
       return new BaseTable(primaryKey, elementKeys, elementKeyToIndex, 1);
   }

   private void updateColorRule(String colName, String value, ColorRule.RuleType operator){
      cr.setColumnElementKey(colName);
      cr.setVal(value);
      cr.setOperator(operator);
   }

   @AfterClass
   public static void clearProperties() {
      StaticStateManipulator.get().reset();
      WebLogger.closeAll();
   }
}

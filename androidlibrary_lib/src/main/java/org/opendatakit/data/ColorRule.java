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
package org.opendatakit.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.database.data.Row;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.UUID;

/**
 * This is a single rule specifying a color for a given datum.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class ColorRule {

  public static final String TAG = "ColColorRule";

  public enum RuleType {

    LESS_THAN("<"), 
    LESS_THAN_OR_EQUAL("<="), 
    EQUAL("="),
    EQUAL_IGNORE_CASE("= (ignore case)"),
    GREATER_THAN_OR_EQUAL(">="),
    GREATER_THAN(">"), 
    NO_OP("operation value");

    private static final String STR_NULL = "null";
    private static final String STR_LESS_THAN = "<";
    private static final String STR_LESS_OR_EQUAL = "<=";
    private static final String STR_EQUAL = "=";
    private static final String STR_EQUAL_IGNORE_CASE = "= (ignore case)";
    private static final String STR_GREATER_OR_EQUAL = ">=";
    private static final String STR_GREATER_THAN = ">";

    // This is the string that represents this operation.
    private String symbol;

    RuleType(String symbol) {
      this.symbol = symbol;
    }

    /**
     * Return the possible values. Intended for a preference screen.
     * 
     * @return
     */
    public static CharSequence[] getValues(String type) {
      ArrayList<CharSequence> result = new ArrayList<>();
      if (type.equals(ElementDataType.string.name())) {
        result.add(STR_EQUAL);
        result.add(STR_EQUAL_IGNORE_CASE);
      } else {
        result.add(STR_LESS_THAN);
        result.add(STR_LESS_OR_EQUAL);
        result.add(STR_EQUAL);
        result.add(STR_GREATER_OR_EQUAL);
        result.add(STR_GREATER_THAN);
      }
      return result.toArray(new CharSequence[result.size()]);
    }

    public String getSymbol() {
      return (symbol == null) ? STR_NULL : symbol;
    }

    public static RuleType getEnumFromString(String inputString) {
      if (inputString.equals(LESS_THAN.symbol)) {
        return LESS_THAN;
      } else if (inputString.equals(LESS_THAN_OR_EQUAL.symbol)) {
        return LESS_THAN_OR_EQUAL;
      } else if (inputString.equals(EQUAL.symbol)) {
        return EQUAL;
      } else if (inputString.equals(EQUAL_IGNORE_CASE.symbol)) {
        return EQUAL_IGNORE_CASE;
      } else if (inputString.equals(GREATER_THAN_OR_EQUAL.symbol)) {
        return GREATER_THAN_OR_EQUAL;
      } else if (inputString.equals(GREATER_THAN.symbol)) {
        return GREATER_THAN;
        // this case is just to handle original code's nonsense
      } else if (inputString.equals("") || inputString.equals(" ")) {
        return NO_OP;
      } else {
        throw new IllegalArgumentException("unrecognized rule operator: " + inputString);
      }
    }
  }

  // The UUID of the rule.
  private String mId;
  /**
   * Element key of the column this rule queries on.
   */
  private String mElementKey;
  private RuleType mOperator;
  private String mValue;
  private int mForeground;
  private int mBackground;

  // ONLY FOR SERIALIZATION
  @SuppressWarnings("unused")
  private ColorRule() {
    // not implemented, used only for serialization
  }

  /**
   * Construct a new color rule to dictate the coloring of cells. Constructs a
   * UUID for the column id.
   * 
   * @param colElementKey
   *          the element key of the column against which this rule will be
   *          checking values
   * @param compType
   *          the comparison type of the rule
   * @param value
   *          the target value of the rule
   * @param foreground
   *          the foreground color of the rule
   * @param background
   *          the background color of the rule
   */
  public ColorRule(String colElementKey, RuleType compType, String value, int foreground,
      int background) {
    // generate a UUID for the color rule. We can't let it autoincrement ints
    // as was happening before, as this would become corrupted when rules were
    // imported from other dbs.
    this(UUID.randomUUID().toString(), colElementKey, compType, value, foreground, background);
  }

  /**
   * Construct a new color rule.
   * 
   * @param id
   * @param colName
   * @param compType
   * @param value
   * @param foreground
   * @param background
   */
  public ColorRule(String id, String colName, RuleType compType, String value, int foreground,
      int background) {
    this.mId = id;
    this.mElementKey = colName;
    this.mOperator = compType;
    this.mValue = value;
    this.mForeground = foreground;
    this.mBackground = background;
  }

  public TreeMap<String,Object> getJsonRepresentation() {
    TreeMap<String,Object> map = new TreeMap<String,Object>();
    map.put("mValue", mValue);
    map.put("mElementKey", mElementKey);
    map.put("mOperator", mOperator.name());
    map.put("mId", mId);
    map.put("mForeground", mForeground);
    map.put("mBackground", mBackground);
    return map;
  }
  /**
   * Get the UUID of the rule.
   * 
   * @return
   */
  @JsonIgnore
  public String getRuleId() {
    return mId;
  }

  /**
   * Get the element key of the column to which this rule applies.
   * 
   * @return
   */
  @JsonIgnore
  public String getColumnElementKey() {
    return mElementKey;
  }

  /**
   * Get the target value to which the rule is being compared.
   * 
   * @return
   */
  @JsonIgnore
  public String getVal() {
    return mValue;
  }

  public void setVal(String newVal) {
    this.mValue = newVal;
  }

  /**
   * Get the foreground color of this rule.
   * 
   * @return
   */
  @JsonIgnore
  public int getForeground() {
    return mForeground;
  }

  @Override
  public String toString() {
    return "[id=" + getRuleId() + ", elementKey=" + getColumnElementKey() + ", operator="
        + getOperator() + ", value=" + getVal() + ", background=" + getBackground()
        + ", foreground=" + getForeground() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ColorRule)) {
      return false;
    }
    ColorRule other = (ColorRule) o;
    boolean sameId = (mId == null) ? other.mId == null : mId.equals(other.mId);
    return sameId && equalsWithoutId(other);
  }

  /**
   * Returns true if the given rule equals this one in all fields except for id.
   * 
   * @param other
   * @return
   */
  public boolean equalsWithoutId(ColorRule other) {
    if ( mBackground != other.mBackground ) {
      return false;
    }
    if ( mForeground != other.mForeground ) {
      return false;
    }
    if ( (mOperator == null) ? (other.mOperator != null) : (mOperator != other.mOperator) ) {
      return false;
    }
    if ( (mElementKey == null) ? (other.mElementKey != null) : !mElementKey.equals(other.mElementKey) ) {
      return false;
    }
    if ( (mValue == null) ? (other.mValue != null) : !mValue.equals(other.mValue) ) {
      return false;
    }
    // otherwise it is the same (excluding the mId)!
    return true;
  }

  public void setForeground(int newForeground) {
    this.mForeground = newForeground;
  }

  /**
   * Get the background color of this rule.
   * 
   * @return
   */
  @JsonIgnore
  public int getBackground() {
    return mBackground;
  }

  public void setBackground(int newBackground) {
    this.mBackground = newBackground;
  }

  @JsonIgnore
  public RuleType getOperator() {
    return mOperator;
  }

  public void setOperator(RuleType newOperator) {
    this.mOperator = newOperator;
  }

  /**
   * Set the element key of the column to which this rule will apply.
   * 
   * @param elementKey
   */
  public void setColumnElementKey(String elementKey) {
    this.mElementKey = elementKey;
  }

  public boolean checkMatch(ElementDataType type, Row row) {
    try {
      // Get the value we're testing against.
      String testValue = row.getDataByKey(mElementKey);
      
      // nulls are never matched (mValue is never null)
      if (testValue == null) {
        return false;
      }
      
      int compVal;
      if ((type == ElementDataType.number || type == ElementDataType.integer)) {
        double doubleValue = Double.parseDouble(testValue);
        double doubleRule = Double.parseDouble(mValue);
        compVal = (Double.valueOf(doubleValue)).compareTo(Double.valueOf(doubleRule));
      } else {
        compVal = testValue.compareTo(mValue);
      }
      switch (mOperator) {
      case LESS_THAN:
        return (compVal < 0);
      case LESS_THAN_OR_EQUAL:
        return (compVal <= 0);
      case EQUAL:
        return (compVal == 0);
      case EQUAL_IGNORE_CASE:
        return testValue.equalsIgnoreCase(mValue);
      case GREATER_THAN_OR_EQUAL:
        return (compVal >= 0);
      case GREATER_THAN:
        return (compVal > 0);
      case NO_OP:
        return false;
      default:
        throw new IllegalArgumentException("unrecognized op passed to checkMatch: " + mOperator);
      }
    } catch (NumberFormatException e) {
      // this should never happen
      e.printStackTrace();
      throw new IllegalArgumentException("error parsing value as number, removing the offending rule");
    }
  }
}

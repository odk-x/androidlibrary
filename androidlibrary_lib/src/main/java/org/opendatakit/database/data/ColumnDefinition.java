/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.database.data;

import androidx.annotation.NonNull;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.DataTableColumns;
import org.opendatakit.utilities.NameUtil;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ColumnDefinition implements Comparable<ColumnDefinition> {
  private static final String TAG = "ColumnDefinition";
  private static final String JSON_SCHEMA_NOT_UNIT_OF_RETENTION = "notUnitOfRetention";
  private static final String JSON_SCHEMA_IS_NOT_NULLABLE = "isNotNullable";
  private static final String JSON_SCHEMA_ELEMENT_SET = "elementSet";
  private static final String JSON_SCHEMA_INSTANCE_METADATA_VALUE = "instanceMetadata";
  private static final String JSON_SCHEMA_INSTANCE_DATA_VALUE = "data";
  // not currently tracking the default value for a data field...
  // private static final String JSON_SCHEMA_DEFAULT = "default";
  // the database column under which this data is persisted (e.g., myPoint_latitude)
  private static final String JSON_SCHEMA_ELEMENT_KEY = "elementKey";
  // the Javascript path to this data if reconstructed into JS object (e.g., myPoint.latitude)
  private static final String JSON_SCHEMA_ELEMENT_PATH = "elementPath";
  // the Javascript name for this element within a Javascript path (e.g., latitude) (rightmost term)
  private static final String JSON_SCHEMA_ELEMENT_NAME = "elementName";
  private static final String JSON_SCHEMA_ELEMENT_TYPE = "elementType";
  private static final String JSON_SCHEMA_LIST_CHILD_ELEMENT_KEYS = "listChildElementKeys";
  private static final String JSON_SCHEMA_PROPERTIES = "properties";
  private static final String JSON_SCHEMA_ITEMS = "items";
  private static final String JSON_SCHEMA_TYPE = "type";

  private final Column column;
  private final ArrayList<ColumnDefinition> children = new ArrayList<>();
  // public final String elementKey;
  // public final String elementName;
  // public final String elementType;
  private boolean isUnitOfRetention = true; // assumed until revised...
  private ElementType type = null;
  private ColumnDefinition parent = null;

  private ColumnDefinition(String elementKey, String elementName, String elementType,
      String listChildElementKeys) {
    this.column = new Column(elementKey, elementName, elementType, listChildElementKeys);
  }

  /**
   * Binary search using elementKey. The ColumnDefinition list returned by
   * ColumnDefinition.buildColumnDefinitions() is ordered. This function makes
   * use of that property to quickly retrieve the definition for an elementKey.
   *
   * @param orderedDefns the definitions to search through
   * @param elementKey the key of the definition we're looking for
   * @return the definition with that key in the set
   * @throws IllegalArgumentException - if elementKey not found
   */
  static ColumnDefinition find(ArrayList<ColumnDefinition> orderedDefns, String elementKey)
      throws IllegalArgumentException {
    if (elementKey == null) {
      throw new NullPointerException("elementKey cannot be null in ColumnDefinition::find()");
    }
    int iLow = 0;
    int iHigh = orderedDefns.size();
    int iGuess = (iLow + iHigh) / 2;
    while (iLow != iHigh) {
      ColumnDefinition cd = orderedDefns.get(iGuess);
      int cmp = elementKey.compareTo(cd.getElementKey());
      if (cmp == 0) {
        return cd;
      }
      if (cmp < 0) {
        iHigh = iGuess;
      } else {
        iLow = iGuess + 1;
      }
      iGuess = (iLow + iHigh) / 2;
    }

    if (iLow >= orderedDefns.size()) {
      throw new IllegalArgumentException(
          "could not find elementKey in columns list: " + elementKey);
    }

    ColumnDefinition cd = orderedDefns.get(iGuess);
    if (cd.getElementKey().equals(elementKey)) {
      return cd;
    }
    throw new IllegalArgumentException("could not find elementKey in columns list: " + elementKey);
  }

  /**
   * Construct the rich ColumnDefinition objects for a table from the underlying
   * information in the list of Column objects.
   *
   * @param appName the app name
   * @param tableId the id of the table with the columns
   * @param columns a list of columns to be turned into column definitions
   * @return a list of column definitions from those columns
   */
  @SuppressWarnings("unchecked")
  static ArrayList<ColumnDefinition> buildColumnDefinitions(String appName, String tableId,
      List<Column> columns) {

    if (appName == null || appName.isEmpty()) {
      throw new IllegalArgumentException("appName cannot be null or an empty string");
    }

    if (tableId == null || tableId.isEmpty()) {
      throw new IllegalArgumentException("tableId cannot be null or an empty string");
    }

    if (columns == null) {
      throw new IllegalArgumentException("columns cannot be null");
    }

    WebLogger.getLogger(appName).d(TAG,
        "[buildColumnDefinitions] tableId: " + tableId + " size: " + columns.size()
            + " first column: " + (columns.isEmpty() ? "<none>" : columns.get(0).getElementKey()));

    Map<String, ColumnDefinition> colDefs = new HashMap<>();
    List<ColumnContainer> ccList = new ArrayList<>();
    for (Column col : columns) {
      if (!NameUtil.isValidUserDefinedDatabaseName(col.getElementKey())) {
        throw new IllegalArgumentException(
            "ColumnDefinition: invalid user-defined column name: " + col.getElementKey());
      }
      ColumnDefinition cd = new ColumnDefinition(col.getElementKey(), col.getElementName(),
          col.getElementType(), col.getListChildElementKeys());
      ColumnContainer cc = new ColumnContainer();
      cc.defn = cd;
      String children = col.getListChildElementKeys();
      if (children != null && !children.isEmpty()) {
        ArrayList<String> chi;
        try {
          chi = ODKFileUtils.mapper.readValue(children, ArrayList.class);
        } catch (IOException e) {
          // JsonMappingException and JsonParseException extends IOException and will be caught here
          WebLogger.getLogger(appName).printStackTrace(e);
          throw new IllegalArgumentException("Invalid list of children: " + children);
        }
        cc.children = chi;
        ccList.add(cc);
      }
      colDefs.put(cd.getElementKey(), cd);
    }
    for (ColumnContainer cc : ccList) {
      ColumnDefinition cparent = cc.defn;
      for (String childKey : cc.children) {
        ColumnDefinition cchild = colDefs.get(childKey);
        if (cchild == null) {
          throw new IllegalArgumentException(
              "Child elementkey " + childKey + " was never defined but referenced in " + cparent
                  .getElementKey() + "!");
        }
        // set up bi-directional linkage of child and parent.
        cparent.addChild(cchild);
      }
    }

    // Sanity check:
    // (1) all children elementKeys must have been defined in the Columns list.
    // (2) arrays must have only one child.
    // (3) children must belong to at most one parent
    for (ColumnContainer cc : ccList) {
      ColumnDefinition defn = cc.defn;

      if (defn.getChildren().size() != cc.children.size()) {
        throw new IllegalArgumentException(
            "Not all children of element have been defined! " + defn.getElementKey());
      }

      ElementType type = defn.getType();

      if (type.getDataType() == ElementDataType.array) {
        if (defn.getChildren().isEmpty()) {
          throw new IllegalArgumentException("Column is an array but does not list its children");
        }
        if (defn.getChildren().size() != 1) {
          throw new IllegalArgumentException("Column is an array but has more than one item entry");
        }
      }

      for (ColumnDefinition child : defn.getChildren()) {
        if (child.getParent() != defn) {
          throw new IllegalArgumentException(
              "Column is enclosed by two or more groupings: " + defn.getElementKey());
        }
        if (!child.getElementKey().equals(defn.getElementKey() + '_' + child.getElementName())) {
          throw new IllegalArgumentException(
              "Children are expected to have elementKey equal to parent's "
                  + "elementKey-underscore-childElementName: " + child.getElementKey());
        }
      }
    }
    markUnitOfRetention(colDefs);
    ArrayList<ColumnDefinition> defns = new ArrayList<>(colDefs.values());
    Collections.sort(defns);

    return defns;
  }

  /**
   * This must match the code in the javascript layer
   * <p>
   * See databaseUtils.markUnitOfRetention
   * <p>
   * Sweeps through the collection of ColumnDefinition objects and marks the
   * ones that exist in the actual database table.
   *
   * @param defn the map of column definitions to mark
   */
  private static void markUnitOfRetention(Map<String, ColumnDefinition> defn) {
    // for all arrays, mark all descendants of the array as not-retained
    // because they are all folded up into the json representation of the array
    for (Map.Entry<String, ColumnDefinition> stringColumnDefinitionEntry : defn.entrySet()) {
      ColumnDefinition colDefn = stringColumnDefinitionEntry.getValue();
      if (!colDefn.isUnitOfRetention()) {
        // this has already been processed
        continue;
      }
      ElementType type = colDefn.getType();
      if (ElementDataType.array == type.getDataType()) {
        ArrayList<ColumnDefinition> descendantsOfArray = new ArrayList<>(
            colDefn.getChildren());
        ArrayList<ColumnDefinition> scratchArray = new ArrayList<>();
        while (true) {
          for (ColumnDefinition subDefn : descendantsOfArray) {
            if (!subDefn.isUnitOfRetention()) {
              // this has already been processed
              continue;
            }
            subDefn.setNotUnitOfRetention();
            scratchArray.addAll(subDefn.getChildren());
          }

          descendantsOfArray.clear();
          descendantsOfArray.addAll(scratchArray);
          scratchArray.clear();
          if (descendantsOfArray.isEmpty()) {
            break;
          }
        }
      }
    }
    // and mark any non-arrays with multiple fields as not retained
    for (Map.Entry<String, ColumnDefinition> stringColumnDefinitionEntry : defn.entrySet()) {
      ColumnDefinition colDefn = stringColumnDefinitionEntry.getValue();
      if (!colDefn.isUnitOfRetention()) {
        // this has already been processed
        continue;
      }
      ElementType type = colDefn.getType();
      if (ElementDataType.array != type.getDataType()) {
        if (!colDefn.getChildren().isEmpty()) {
          colDefn.setNotUnitOfRetention();
        }
      }
    }
  }

  /**
   * Convert the ColumnDefinition map to an ordered list of columns for
   * transport layer.
   * Used in AggregateSynchronizer, CsvUtil, others
   *
   * @param orderedDefns a list of ColumnDefinition objects
   * @return ordered list of Column objects
   */
  @SuppressWarnings("WeakerAccess")
  public static ArrayList<Column> getColumns(ArrayList<ColumnDefinition> orderedDefns) {
    ArrayList<Column> columns = new ArrayList<Column>();
    for (ColumnDefinition col : orderedDefns) {
      columns.add(col.column);
    }
    return columns;
  }

  /**
   * Covert the ColumnDefinition map into a JSON schema. and augment it with
   * the schema for the administrative columns.
   * <p>
   * The structure of this schema matches the dataTableModel produced by XLSXConverter
   *
   * @param orderedDefns Used for getting the data model
   * @return An extended data model for the columns
   */
  static TreeMap<String, Object> getExtendedDataModel(List<ColumnDefinition> orderedDefns) {
    TreeMap<String, Object> model = getDataModel(orderedDefns);

    TreeMap<String, Object> jsonSchema;
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.ID, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.TRUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.ID);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.ID);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.ID);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.ROW_ETAG, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.ROW_ETAG);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.ROW_ETAG);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.ROW_ETAG);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.SYNC_STATE, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.TRUE);
    // don't force a default value -- the database layer handles sync state initialization itself.
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.SYNC_STATE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.SYNC_STATE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.SYNC_STATE);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.CONFLICT_TYPE, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.integer.name());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.CONFLICT_TYPE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.CONFLICT_TYPE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.CONFLICT_TYPE);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.DEFAULT_ACCESS, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.DEFAULT_ACCESS);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.DEFAULT_ACCESS);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.DEFAULT_ACCESS);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.ROW_OWNER, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.ROW_OWNER);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.ROW_OWNER);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.ROW_OWNER);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.GROUP_READ_ONLY, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.GROUP_READ_ONLY);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.GROUP_READ_ONLY);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.GROUP_READ_ONLY);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.GROUP_MODIFY, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.GROUP_MODIFY);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.GROUP_MODIFY);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.GROUP_MODIFY);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.GROUP_PRIVILEGED, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.GROUP_PRIVILEGED);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.GROUP_PRIVILEGED);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.GROUP_PRIVILEGED);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.FORM_ID, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.FORM_ID);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.FORM_ID);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.FORM_ID);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.LOCALE, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.LOCALE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.LOCALE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.LOCALE);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.SAVEPOINT_TYPE, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.SAVEPOINT_TYPE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.SAVEPOINT_TYPE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.SAVEPOINT_TYPE);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.SAVEPOINT_TIMESTAMP, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.TRUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.SAVEPOINT_TIMESTAMP);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.SAVEPOINT_TIMESTAMP);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.SAVEPOINT_TIMESTAMP);
    //
    jsonSchema = new TreeMap<>();
    model.put(DataTableColumns.SAVEPOINT_CREATOR, jsonSchema);
    jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
    jsonSchema.put(JSON_SCHEMA_IS_NOT_NULLABLE, Boolean.FALSE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_METADATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, DataTableColumns.SAVEPOINT_CREATOR);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, DataTableColumns.SAVEPOINT_CREATOR);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, DataTableColumns.SAVEPOINT_CREATOR);

    return model;
  }

  /**
   * Covert the ColumnDefinition map into a JSON schema.
   * <p>
   * Add elements to this schema to match the dataTableModel produced by XLSXConverter
   *
   * @param orderedDefns a list of column definitions
   * @return the JSON schema for those columns
   */
  static TreeMap<String, Object> getDataModel(List<ColumnDefinition> orderedDefns) {
    TreeMap<String, Object> model = new TreeMap<>();

    for (ColumnDefinition c : orderedDefns) {
      if (c.getParent() == null) {
        TreeMap<String, Object> jsonSchema = new TreeMap<>();
        model.put(c.getElementName(), jsonSchema);
        jsonSchema.put(JSON_SCHEMA_ELEMENT_PATH, c.getElementName());
        getDataModelHelper(jsonSchema, c, false);
        if (!c.isUnitOfRetention()) {
          jsonSchema.put(JSON_SCHEMA_NOT_UNIT_OF_RETENTION, Boolean.TRUE);
        }
      }
    }
    return model;
  }

  private static void getDataModelHelper(TreeMap<String, Object> jsonSchema, ColumnDefinition c,
      boolean nestedInsideUnitOfRetention) {
    ElementType type = c.getType();
    ElementDataType dataType = type.getDataType();

    // this is a user-defined field
    jsonSchema.put(JSON_SCHEMA_ELEMENT_SET, JSON_SCHEMA_INSTANCE_DATA_VALUE);
    jsonSchema.put(JSON_SCHEMA_ELEMENT_NAME, c.getElementName());
    jsonSchema.put(JSON_SCHEMA_ELEMENT_KEY, c.getElementKey());

    if (nestedInsideUnitOfRetention) {
      jsonSchema.put(JSON_SCHEMA_NOT_UNIT_OF_RETENTION, Boolean.TRUE);
    }

    if (dataType == ElementDataType.array) {
      jsonSchema.put(JSON_SCHEMA_TYPE, dataType.name());
      if (!c.getElementType().equals(dataType.name())) {
        jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
      }
      ColumnDefinition ch = c.getChildren().get(0);
      TreeMap<String, Object> itemSchema = new TreeMap<>();
      jsonSchema.put(JSON_SCHEMA_ITEMS, itemSchema);
      itemSchema.put(JSON_SCHEMA_ELEMENT_PATH,
          (String) jsonSchema.get(JSON_SCHEMA_ELEMENT_PATH) + '.' + ch.getElementName());
      // if it isn't already nested within a unit of retention,
      // an array is always itself a unit of retention
      getDataModelHelper(itemSchema, ch, true); // recursion...

      ArrayList<String> keys = new ArrayList<String>();
      keys.add(ch.getElementKey());
      jsonSchema.put(JSON_SCHEMA_LIST_CHILD_ELEMENT_KEYS, keys);
    } else if (dataType == ElementDataType.bool) {
      jsonSchema.put(JSON_SCHEMA_TYPE, dataType.name());
      if (!c.getElementType().equals(dataType.name())) {
        jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
      }
    } else if (dataType == ElementDataType.configpath) {
      jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
      jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
    } else if (dataType == ElementDataType.integer || dataType == ElementDataType.number) {
      jsonSchema.put(JSON_SCHEMA_TYPE, dataType.name());
      if (!c.getElementType().equals(dataType.name())) {
        jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
      }
    } else if (dataType == ElementDataType.object) {
      jsonSchema.put(JSON_SCHEMA_TYPE, dataType.name());
      if (!c.getElementType().equals(dataType.name())) {
        jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
      }
      TreeMap<String, Object> propertiesSchema = new TreeMap<>();
      jsonSchema.put(JSON_SCHEMA_PROPERTIES, propertiesSchema);
      ArrayList<String> keys = new ArrayList<>();
      for (ColumnDefinition ch : c.getChildren()) {
        TreeMap<String, Object> itemSchema = new TreeMap<>();
        propertiesSchema.put(ch.getElementName(), itemSchema);
        itemSchema.put(JSON_SCHEMA_ELEMENT_PATH,
            (String) jsonSchema.get(JSON_SCHEMA_ELEMENT_PATH) + '.' + ch.getElementName());
        // objects are not units of retention -- propagate retention status.
        getDataModelHelper(itemSchema, ch, nestedInsideUnitOfRetention); // recursion...
        keys.add(ch.getElementKey());
      }
      jsonSchema.put(JSON_SCHEMA_LIST_CHILD_ELEMENT_KEYS, keys);
    } else if (dataType == ElementDataType.rowpath) {
      jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
      jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, ElementDataType.rowpath.name());
    } else if (dataType == ElementDataType.string) {
      jsonSchema.put(JSON_SCHEMA_TYPE, ElementDataType.string.name());
      if (!c.getElementType().equals(dataType.name())) {
        jsonSchema.put(JSON_SCHEMA_ELEMENT_TYPE, c.getElementType());
      }
    } else {
      throw new IllegalStateException("unexpected alternative ElementDataType");
    }
  }

  public String getElementKey() {
    return column.getElementKey();
  }

  public String getElementName() {
    return column.getElementName();
  }

  public String getElementType() {
    return column.getElementType();
  }

  public synchronized ElementType getType() {
    if (type == null) {
      type = ElementType.parseElementType(getElementType(), !getChildren().isEmpty());
    }
    return type;
  }

  public String getListChildElementKeys() {
    return column.getListChildElementKeys();
  }

  public ColumnDefinition getParent() {
    return this.parent;
  }

  private void setParent(ColumnDefinition parent) {
    this.parent = parent;
  }

  /**
   * Despite being public, this method is only used in this class
   * @param child the child to add
   */
  public void addChild(ColumnDefinition child) {
    child.setParent(this);
    children.add(child);
  }

  /**
   * Used in ODKDatabaseImplUtils
   * @return a copy of the children
   */
  @SuppressWarnings("WeakerAccess")
  public List<ColumnDefinition> getChildren() {
    return Collections.unmodifiableList(this.children);
  }

  public boolean isUnitOfRetention() {
    return isUnitOfRetention;
  }

  /**
   * Used in DbColumnDefinitions
   */
  @SuppressWarnings("WeakerAccess")
  void setNotUnitOfRetention() {
    isUnitOfRetention = false;
  }

  public String toString() {
    return column.toString();
  }

  public int hashCode() {
    return column.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ColumnDefinition)) {
      return false;
    }
    ColumnDefinition o = (ColumnDefinition) obj;

    return column.equals(o.column);
  }

  @Override
  public int compareTo(@NonNull ColumnDefinition another) {
    return this.getElementKey().compareTo(another.getElementKey());
  }

  /**
   * Helper class for building ColumnDefinition objects from Column objects.
   *
   * @author mitchellsundt@gmail.com
   */
  private static class ColumnContainer {
    ColumnDefinition defn = null;
    ArrayList<String> children = null;
  }

}
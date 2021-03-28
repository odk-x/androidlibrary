/*
 * Copyright (C) 2015 University of Washington
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
package org.opendatakit.builder;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader;
import org.opendatakit.aggregate.odktables.rest.RFC4180CsvWriter;
import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.database.data.ColumnDefinition;
import org.opendatakit.database.data.ColumnList;
import org.opendatakit.database.data.KeyValueStoreEntry;
import org.opendatakit.database.data.OrderedColumns;
import org.opendatakit.database.utilities.KeyValueStoreUtils;
import org.opendatakit.exception.ServicesAvailabilityException;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.ColumnDefinitionsColumns;
import org.opendatakit.provider.KeyValueStoreColumns;
import org.opendatakit.utilities.ODKFileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utilities for importing/exporting tables from/to CSV.
 * <ul>
 * <li>tables/tableId/properties.csv</li>
 * <li>tables/tableId/definition.csv</li>
 * </ul>
 *
 * @author sudar.sam@gmail.com
 * @author mitchellsundt@gmail.com
 */
public final class PropertiesFileUtils {
  // used for logging
  private static final String TAG = PropertiesFileUtils.class.getSimpleName();

  public static class DataTableDefinition {
    public ColumnList columnList;
    public List<KeyValueStoreEntry> kvsEntries;
  }

  // ===========================================================================================
  // EXPORT
  // ===========================================================================================

  /**
   * Common routine to write the definition and properties files.
   * Assumes the kvsEntries have had the choice list entries expanded
   * from the choiceList table.
   *
   * @param appName       the app name
   * @param tableId       the id of the table to be exported
   * @param orderedDefns  a list of the columns in the table
   * @param definitionCsv the file to write the table definitions to
   * @param propertiesCsv the file to write the properties to
   * @return whether successful or not
   * @throws ServicesAvailabilityException if the database is down
   */
  public static synchronized boolean writePropertiesIntoCsv(String appName, String tableId,
      OrderedColumns orderedDefns, List<KeyValueStoreEntry> kvsEntries, File definitionCsv,
      File propertiesCsv) throws ServicesAvailabilityException {
    WebLogger.getLogger(appName).i(TAG, "writePropertiesIntoCsv: tableId: " + tableId);

    // writing metadata
    FileOutputStream out = null;
    RFC4180CsvWriter cw;
    OutputStreamWriter output = null;
    try {
      // emit definition.csv table...
      out = new FileOutputStream(definitionCsv);
      output = new OutputStreamWriter(out, StandardCharsets.UTF_8);
      cw = new RFC4180CsvWriter(output);

      // Emit ColumnDefinitions

      ArrayList<String> colDefHeaders = new ArrayList<>();
      colDefHeaders.add(ColumnDefinitionsColumns.ELEMENT_KEY);
      colDefHeaders.add(ColumnDefinitionsColumns.ELEMENT_NAME);
      colDefHeaders.add(ColumnDefinitionsColumns.ELEMENT_TYPE);
      colDefHeaders.add(ColumnDefinitionsColumns.LIST_CHILD_ELEMENT_KEYS);

      cw.writeNext(colDefHeaders.toArray(new String[colDefHeaders.size()]));
      String[] colDefRow = new String[colDefHeaders.size()];

      // Since the md5Hash of the file identifies identical schemas, ensure that the list of
      // columns is in alphabetical order.
      // This writes data about each of the columns to definitionsCsv
      for (ColumnDefinition cd : orderedDefns.getColumnDefinitions()) {
        colDefRow[0] = cd.getElementKey();
        colDefRow[1] = cd.getElementName();
        colDefRow[2] = cd.getElementType();
        colDefRow[3] = cd.getListChildElementKeys();
        cw.writeNext(colDefRow);
      }

      cw.flush();
      cw.close();

      // emit properties.csv...
      out = new FileOutputStream(propertiesCsv);
      output = new OutputStreamWriter(out, StandardCharsets.UTF_8);
      cw = new RFC4180CsvWriter(output);

      // Emit KeyValueStore

      ArrayList<String> kvsHeaders = new ArrayList<>();
      kvsHeaders.add(KeyValueStoreColumns.PARTITION);
      kvsHeaders.add(KeyValueStoreColumns.ASPECT);
      kvsHeaders.add(KeyValueStoreColumns.KEY);
      kvsHeaders.add(KeyValueStoreColumns.VALUE_TYPE);
      kvsHeaders.add(KeyValueStoreColumns.VALUE);

      // This sorts the KeyValueStore entries first based on their partition, then based on their
      // aspect, and finally based on their key. It shuffles things with null properties to the end
      Collections.sort(kvsEntries, new Comparator<KeyValueStoreEntry>() {

        @Override
        public int compare(KeyValueStoreEntry lhs, KeyValueStoreEntry rhs) {
          int outcome;
          if (lhs.partition == null && rhs.partition == null) {
            outcome = 0;
          } else if (lhs.partition == null) {
            return -1;
          } else if (rhs.partition == null) {
            return 1;
          } else {
            outcome = lhs.partition.compareTo(rhs.partition);
          }
          if (outcome != 0)
            return outcome;
          if (lhs.aspect == null && rhs.aspect == null) {
            outcome = 0;
          } else if (lhs.aspect == null) {
            return -1;
          } else if (rhs.aspect == null) {
            return 1;
          } else {
            outcome = lhs.aspect.compareTo(rhs.aspect);
          }
          if (outcome != 0)
            return outcome;
          if (lhs.key == null && rhs.key == null) {
            outcome = 0;
          } else if (lhs.key == null) {
            return -1;
          } else if (rhs.key == null) {
            return 1;
          } else {
            outcome = lhs.key.compareTo(rhs.key);
          }
          return outcome;
        }
      });

      // Writes the CSV header to the output file
      cw.writeNext(kvsHeaders.toArray(new String[kvsHeaders.size()]));
      // kvsRow has the length of the number of columns in the table
      String[] kvsRow = new String[kvsHeaders.size()];
      for (KeyValueStoreEntry entry : kvsEntries) {
        // but only the first five are used? Seems strange
        kvsRow[0] = entry.partition;
        kvsRow[1] = entry.aspect;
        kvsRow[2] = entry.key;
        kvsRow[3] = entry.type;
        kvsRow[4] = entry.value;
        cw.writeNext(kvsRow);
      }
      cw.flush();
      cw.close();

      return true;
    } catch (IOException e) {
      return false;
    } finally {
      try {
        if (output != null) output.close();
        if (out != null) out.close();
      } catch (IOException e) {
        // we have failed. We should have already returned false at this point
      }
    }
  }

  /**
   * Does the same thing as CsvUtil.countUpToLastNonNullElement
   * Returns the index of the last non-null element in the row. So [1, 2, 3, null, null] would
   * return 3, but so would [null, null, 3, null, null] and [1, 2, 3] and [null, null, 3]
   *
   * @param row an array of strings representing all the values in a row
   * @return the index of the last non-null element, or zero if the row was all nulls
   */
  private static int countUpToLastNonNullElement(String[] row) {
    for (int i = row.length - 1; i >= 0; --i) {
      if (row[i] != null) {
        return (i + 1);
      }
    }
    return 0;
  }

  /**
   * Retrieve the contents of
   * <ul>
   * <li>tables/tableId/properties.csv</li>
   * <li>tables/tableId/definition.csv</li>
   * </ul>
   * And return them to the caller.
   * <p>
   * The returned kvsEntries will not have had their choice list
   * entries consolidated.
   *
   * @param appName the app name
   * @param tableId the table id to import
   * @return the table that it read, a list of columns and a list of KeyValueStoreEntries
   * @throws IOException if we couldn't properly read the file
   * @throws IllegalStateException if the CSV file was improperly formatted
   */
  public static synchronized DataTableDefinition readPropertiesFromCsv(String appName,
      String tableId) throws IOException {

    WebLogger.getLogger(appName).i(TAG, "readPropertiesFromCsv: tableId: " + tableId);

    // Initialize what will later be passed into the DataTableDefinition constructor
    List<Column> columns = new ArrayList<>();
    List<KeyValueStoreEntry> kvsEntries = new ArrayList<>();

    // reading data
    File file = null;
    FileInputStream in = null;
    InputStreamReader input = null;
    RFC4180CsvReader cr = null;
    try {
      file = new File(ODKFileUtils.getTableDefinitionCsvFile(appName, tableId));
      in = new FileInputStream(file);
      input = new InputStreamReader(in, StandardCharsets.UTF_8);
      cr = new RFC4180CsvReader(input);

      String[] row;

      // Read ColumnDefinitions
      // get the column headers
      String[] colHeaders = cr.readNext();
      int colHeadersLength = countUpToLastNonNullElement(colHeaders);
      // get the first row
      row = cr.readNext();
      while (row != null && countUpToLastNonNullElement(row) != 0) {

        String elementKeyStr = null;
        String elementNameStr = null;
        String elementTypeStr = null;
        String listChildElementKeysStr = null;
        int rowLength = countUpToLastNonNullElement(row);
        for (int i = 0; i < rowLength; ++i) {
          if (i >= colHeadersLength) {
            throw new IllegalStateException("data beyond header row of ColumnDefinitions table");
          }
          if (ColumnDefinitionsColumns.ELEMENT_KEY.equals(colHeaders[i])) {
            elementKeyStr = row[i];
          }
          if (ColumnDefinitionsColumns.ELEMENT_NAME.equals(colHeaders[i])) {
            elementNameStr = row[i];
          }
          if (ColumnDefinitionsColumns.ELEMENT_TYPE.equals(colHeaders[i])) {
            elementTypeStr = row[i];
          }
          if (ColumnDefinitionsColumns.LIST_CHILD_ELEMENT_KEYS.equals(colHeaders[i])) {
            listChildElementKeysStr = row[i];
          }
        }

        if (elementKeyStr == null || elementTypeStr == null) {
          throw new IllegalStateException("ElementKey and ElementType must be specified");
        }

        columns.add(
            new Column(elementKeyStr, elementNameStr, elementTypeStr, listChildElementKeysStr));

        // get next row or blank to end...
        row = cr.readNext();
      }

      cr.close();
      try {
        input.close();
      } catch (IOException e) {
      }
      try {
        in.close();
      } catch (IOException e) {
      }

      file = new File(ODKFileUtils.getTablePropertiesCsvFile(appName, tableId));
      in = new FileInputStream(file);
      input = new InputStreamReader(in, StandardCharsets.UTF_8);
      cr = new RFC4180CsvReader(input);
      // Read KeyValueStore
      // read the column headers
      String[] kvsHeaders = cr.readNext();
      // read the first row
      row = cr.readNext();
      while (row != null && countUpToLastNonNullElement(row) != 0) {
        String partition = null;
        String aspect = null;
        String key = null;
        String type = null;
        String value = null;
        int rowLength = countUpToLastNonNullElement(row);
        for (int i = 0; i < rowLength; ++i) {
          if (KeyValueStoreColumns.PARTITION.equals(kvsHeaders[i])) {
            partition = row[i];
          }
          if (KeyValueStoreColumns.ASPECT.equals(kvsHeaders[i])) {
            aspect = row[i];
          }
          if (KeyValueStoreColumns.KEY.equals(kvsHeaders[i])) {
            key = row[i];
          }
          if (KeyValueStoreColumns.VALUE_TYPE.equals(kvsHeaders[i])) {
            type = row[i];
          }
          if (KeyValueStoreColumns.VALUE.equals(kvsHeaders[i])) {
            value = row[i];
          }
        }
        KeyValueStoreEntry kvsEntry = KeyValueStoreUtils
            .buildEntry(tableId, partition, aspect, key, ElementDataType.valueOf(type), value);
        kvsEntries.add(kvsEntry);
        // get next row or blank to end...
        row = cr.readNext();
      }
      cr.close();
      try {
        input.close();
      } catch (IOException e) {
      }
      try {
        in.close();
      } catch (IOException e) {
      }

    } finally {
      try {
        if (input != null) {
          input.close();
        }
      } catch (IOException e) {
      }
    }

    DataTableDefinition dtd = new DataTableDefinition();
    dtd.columnList = new ColumnList(columns);
    dtd.kvsEntries = kvsEntries;
    return dtd;
  }
}

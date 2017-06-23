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

package org.opendatakit.database.data;

import android.os.Parcel;
import android.os.Parcelable;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.aggregate.odktables.rest.entity.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class OrderedColumns implements Parcelable {

  public static final Parcelable.Creator<OrderedColumns> CREATOR = new Parcelable.Creator<OrderedColumns>() {
    public OrderedColumns createFromParcel(Parcel in) {
      return new OrderedColumns(in);
    }

    public OrderedColumns[] newArray(int size) {
      return new OrderedColumns[size];
    }
  };
  private final String appName;
  private final String tableId;

  // private static final Map<String, ArrayList<ColumnDefinition> > mapping;
  private final ArrayList<ColumnDefinition> orderedDefns;

  public OrderedColumns(String appName, String tableId, List<Column> columns) {
    this.appName = appName;
    this.tableId = tableId;
    this.orderedDefns = ColumnDefinition.buildColumnDefinitions(appName, tableId, columns);
  }

  public OrderedColumns(Parcel in) {
    appName = in.readString();
    tableId = in.readString();
    ColumnList cl = new ColumnList(in);
    this.orderedDefns = ColumnDefinition.buildColumnDefinitions(appName, tableId, cl.getColumns());
  }

  public ColumnDefinition find(String elementKey) {
    return ColumnDefinition.find(orderedDefns, elementKey);
  }

  /**
   * Get the names of the columns that are written into the underlying database table.
   * These are the isUnitOfRetention() columns.
   *
   * @return the list of column names that will be retained
   */
  public ArrayList<String> getRetentionColumnNames() {
    ArrayList<String> writtenColumns = new ArrayList<>();
    for (ColumnDefinition cd : orderedDefns) {
      if (cd.isUnitOfRetention()) {
        writtenColumns.add(cd.getElementKey());
      }
    }
    return writtenColumns;
  }

  public boolean graphViewIsPossible() {
    for (ColumnDefinition cd : orderedDefns) {
      if (!cd.isUnitOfRetention()) {
        continue;
      }
      ElementType elementType = cd.getType();
      ElementDataType type = elementType.getDataType();
      if (type == ElementDataType.number || type == ElementDataType.integer) {
        return orderedDefns.size() > 1;
      }
    }
    return false;
  }

  /**
   * Extract the list of geopoints from the table.
   * These three methods are used in TableUtil
   *
   * @return the list of geopoints.
   */
  @SuppressWarnings("WeakerAccess")
  public ArrayList<ColumnDefinition> getGeopointColumnDefinitions() {
    ArrayList<ColumnDefinition> cdList = new ArrayList<>();

    for (ColumnDefinition cd : orderedDefns) {
      if (cd.getType().getElementType().equals(ElementType.GEOPOINT)) {
        cdList.add(cd);
      }
    }
    return cdList;
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLatitudeColumnDefinition(List<ColumnDefinition> geoPointList,
      ColumnDefinition cd) {
    if (!cd.isUnitOfRetention()) {
      return false;
    }

    ElementDataType type = cd.getType().getDataType();
    if (!(type == ElementDataType.number || type == ElementDataType.integer)) {
      return false;
    }

    ColumnDefinition cdParent = cd.getParent();

    return cdParent != null && geoPointList.contains(cdParent) && "latitude"
        .equals(cd.getElementName());
  }

  @SuppressWarnings("WeakerAccess")
  public boolean isLongitudeColumnDefinition(List<ColumnDefinition> geoPointList,
      ColumnDefinition cd) {
    if (!cd.isUnitOfRetention()) {
      return false;
    }

    ElementDataType type = cd.getType().getDataType();
    if (!(type == ElementDataType.number || type == ElementDataType.integer)) {
      return false;
    }

    ColumnDefinition cdParent = cd.getParent();

    return cdParent != null && geoPointList.contains(cdParent) && "longitude"
        .equals(cd.getElementName());
  }

  public boolean mapViewIsPossible() {
    List<ColumnDefinition> geoPoints = getGeopointColumnDefinitions();
    if (!geoPoints.isEmpty()) {
      return true;
    }

    boolean hasLatitude = false;
    boolean hasLongitude = false;
    for (ColumnDefinition cd : orderedDefns) {
      hasLatitude = hasLatitude || isLatitudeColumnDefinition(geoPoints, cd);
      hasLongitude = hasLongitude || isLongitudeColumnDefinition(geoPoints, cd);
    }

    return hasLatitude && hasLongitude;
  }

  public ArrayList<ColumnDefinition> getColumnDefinitions() {
    return orderedDefns;
  }

  public String getAppName() {
    return appName;
  }

  public String getTableId() {
    return tableId;
  }

  public ArrayList<Column> getColumns() {
    return ColumnDefinition.getColumns(orderedDefns);
  }

  public TreeMap<String, Object> getDataModel() {
    return ColumnDefinition.getDataModel(orderedDefns);
  }

  /**
   * Returns the JSON schema of the table that includes all the metadata columns.
   * Used in ExecutorProcessor, ColumnDefinition
   *
   * @return the JSON schema of the table that includes all the metadata columns.
   */
  @SuppressWarnings("unused")
  public TreeMap<String, Object> getExtendedDataModel() {
    return ColumnDefinition.getExtendedDataModel(orderedDefns);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeString(appName);
    out.writeString(tableId);
    ColumnList cl = new ColumnList(getColumns());
    cl.writeToParcel(out, flags);
  }

}

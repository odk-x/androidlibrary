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
package org.opendatakit.utilities;

import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.database.data.ColumnDefinition;
import org.opendatakit.database.data.OrderedColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class RowPathColumnUtil {

  private static RowPathColumnUtil rowPathColumnUtil = new RowPathColumnUtil();

  static {
    // register a state-reset manipulator for 'rowPathColumnUtil' field.
    StaticStateManipulator.get().register(new StaticStateManipulator.IStaticFieldManipulator() {

      @Override
      public void reset() {
        rowPathColumnUtil = new RowPathColumnUtil();
      }

    });
  }

  private RowPathColumnUtil() {
  }

  public static RowPathColumnUtil get() {
    return rowPathColumnUtil;
  }

  /**
   * For mocking -- supply a mocked object.
   *
   * @param util the object to set the RowPathColumnUtil to
   */
  public static void set(RowPathColumnUtil util) {
    rowPathColumnUtil = util;
  }

  /**
   * Return the element groups that define a uriFragment of type rowpath
   * and a contentType. Whatever these are named, they are media attachment groups.
   *
   * @param orderedDefns A list of definitions to be put into the result list
   * @return a list of the column definitions given an OrderedColumns object
   */
  List<ColumnDefinition> getUriColumnDefinitions(OrderedColumns orderedDefns) {

    Set<ColumnDefinition> uriFragmentList = new HashSet<>();
    Set<ColumnDefinition> contentTypeList = new HashSet<>();

    for (ColumnDefinition cd : orderedDefns.getColumnDefinitions()) {
      ColumnDefinition cdParent = cd.getParent();

      if ("uriFragment".equals(cd.getElementName())
          && cd.getType().getDataType() == ElementDataType.rowpath && cdParent != null) {
        uriFragmentList.add(cdParent);
      }
      if ("contentType".equals(cd.getElementName())
          && cd.getType().getDataType() == ElementDataType.string && cdParent != null) {
        contentTypeList.add(cdParent);
      }
    }
    uriFragmentList.retainAll(contentTypeList);

    List<ColumnDefinition> cdList = new ArrayList<>(uriFragmentList);
    Collections.sort(cdList);
    return cdList;
  }

}

package org.opendatakit.builder.csvparser.jackson;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.apache.commons.lang3.time.DateUtils;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.database.utilities.CursorUtils;
import org.opendatakit.utilities.LocalizationUtils;

import java.util.HashMap;
import java.util.Map;

public class CsvRow extends Row {
  private final Map<String, String> columns;

  @JsonAnyGetter
  @JsonUnwrapped
  public Map<String, String> getColumns() {
    return columns;
  }

  @JsonAnySetter
  public void anySetter(String key, String value) {
    // set empty strings to null,
    // because this is how org.opendatakit.aggregate.odktables.rest.RFC4180CsvReader does it
    if (value.isEmpty()) {
      value = null;
    }

    if (!key.startsWith("_")) {
      columns.put(key, value);
    }
  }

  public CsvRow() {
    this.columns = new HashMap<>();
  }
}

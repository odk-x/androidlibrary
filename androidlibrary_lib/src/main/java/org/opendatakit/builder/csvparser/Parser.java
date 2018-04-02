package org.opendatakit.builder.csvparser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import org.apache.commons.lang3.time.DateUtils;
import org.opendatakit.aggregate.odktables.rest.SavepointTypeManipulator;
import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;
import org.opendatakit.builder.csvparser.jackson.CsvRow;
import org.opendatakit.builder.csvparser.jackson.RowCsvMixin;
import org.opendatakit.builder.csvparser.jackson.RowFilterScopeCsvMixin;
import org.opendatakit.database.utilities.CursorUtils;
import org.opendatakit.utilities.LocalizationUtils;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Parser {
  private static final String[] TIMESTAMP_PATTERNS = {
      "yyyy-MM-dd'T'HH:mm:ss.SSS", // ISO 8601 w/o TZ
      "yyyy-MM-dd'T'HH:mm:ss.SSSZ", // ISO 8601 UTC
      "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // ISO 8601 w/ TZ
      "EEE, d MMM yyyy HH:mm:ss Z", // RFC1123
      "MMMM dd, yyyy",
      "dd/MM/yyyy",
      "yyyy/MM/dd",
      "dd-MM-yyyy",
      "yyyy-MM-dd"
  };

  private final ObjectReader objectReader;

  public Parser() {
    CsvMapper csvMapper = (CsvMapper) new CsvMapper()
        .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
        .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
        .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        .addMixIn(CsvRow.class, RowCsvMixin.class)
        .addMixIn(RowFilterScope.class, RowFilterScopeCsvMixin.class);

    CsvSchema csvSchema = CsvSchema.builder()
        .addColumnsFrom(csvMapper.schemaFor(Row.class))
        .build()
        .withHeader()
        .withColumnReordering(true);

    objectReader = csvMapper
        .readerFor(CsvRow.class)
        .with(csvSchema);;
  }

  public List<Row> parse(File file) throws IOException {
    MappingIterator<CsvRow> iterator = null;

    try {
      iterator = objectReader.readValues(file);

      List<Row> rows = new ArrayList<>();
      while (iterator.hasNext()) {
        CsvRow nextRow = iterator.next();
        nextRow.setValues(Row.convertFromMap(nextRow.getColumns()));
        nextRow = populateWithDefault(nextRow);

        rows.add(nextRow);
      }

      return rows;
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  public List<CsvRow> parseAsCsvRow(File file) throws IOException {
    MappingIterator<CsvRow> iterator = null;

    try {
      iterator = objectReader.readValues(file);
      List<CsvRow> rows = iterator.readAll();

      for (CsvRow row : rows) {
        populateWithDefault(row);
      }

      return rows;
    } finally {
      if (iterator != null) {
        iterator.close();
      }
    }
  }

  private <T extends Row> T populateWithDefault(T row) {
    if (row.getRowId() == null || row.getRowId().isEmpty()) {
      row.setRowId(LocalizationUtils.genUUID());
    }

    if (row.getRowETag() == null || row.getRowETag().isEmpty()) {
      row.setRowETag(null);
    }

    if (row.getSavepointCreator() == null || row.getSavepointCreator().isEmpty()) {
      row.setSavepointCreator(CursorUtils.DEFAULT_CREATOR);
    }

    if (row.getFormId() == null || row.getFormId().isEmpty()) {
      row.setFormId(null);
    }

    if (row.getLocale() == null || row.getLocale().isEmpty()) {
      row.setLocale(CursorUtils.DEFAULT_LOCALE);
    }

    // savepointType cannot be null, empty or a value other than complete or incomplete
    if (row.getSavepointType() == null || row.getSavepointType().isEmpty() ||
        !(row.getSavepointType().equals(SavepointTypeManipulator.complete()) ||
            row.getSavepointType().equals(SavepointTypeManipulator.incomplete()))) {
      row.setSavepointType(SavepointTypeManipulator.complete());
    }

    row.setRowFilterScope(populateRowFilterScopeWithDefault(row.getRowFilterScope()));
    row.setSavepointTimestamp(convertInvalidTimestamp(row.getSavepointTimestamp()));

    return row;
  }

  private RowFilterScope populateRowFilterScopeWithDefault(RowFilterScope scope) {
    if (scope.getDefaultAccess() == null) {
      scope.setDefaultAccess(RowFilterScope.EMPTY_ROW_FILTER.getDefaultAccess());
    }

    if (scope.getRowOwner() == null || scope.getRowOwner().isEmpty()) {
      scope.setRowOwner(RowFilterScope.EMPTY_ROW_FILTER.getRowOwner());
    }

    if (scope.getGroupReadOnly() == null || scope.getGroupReadOnly().isEmpty()) {
      scope.setGroupReadOnly(RowFilterScope.EMPTY_ROW_FILTER.getGroupReadOnly());
    }

    if (scope.getGroupModify() == null || scope.getGroupModify().isEmpty()) {
      scope.setGroupModify(RowFilterScope.EMPTY_ROW_FILTER.getGroupModify());
    }

    if (scope.getGroupPrivileged() == null || scope.getGroupPrivileged().isEmpty()) {
      scope.setGroupPrivileged(RowFilterScope.EMPTY_ROW_FILTER.getGroupPrivileged());
    }

    return scope;
  }

  private String convertInvalidTimestamp(String timestamp) {
    // timestamp is null/empty, use the current time
    if (timestamp == null || timestamp.isEmpty()) {
      return TableConstants.nanoSecondsFromMillis(
          System.currentTimeMillis(), TableConstants.TIMESTAMP_LOCALE);
    }

    // timestamp is valid
    try {
      TableConstants.milliSecondsFromNanos(timestamp, TableConstants.TIMESTAMP_LOCALE);
      return timestamp;
    } catch (IllegalArgumentException e) {
      // ignored
      // IllegalArgumentException is thrown when the pattern doesn't match
    }

    // try our timestamp format with system default locale
    try {
      Long timeLong = TableConstants.milliSecondsFromNanos(timestamp, null);
      return TableConstants.nanoSecondsFromMillis(timeLong, TableConstants.TIMESTAMP_LOCALE);
    } catch (IllegalArgumentException e) {
      // ignored
    }

    // try with DateUtils
    try {
      long timeLong = DateUtils
          .parseDate(timestamp, TableConstants.TIMESTAMP_LOCALE, TIMESTAMP_PATTERNS)
          .getTime();
      return TableConstants.nanoSecondsFromMillis(timeLong, TableConstants.TIMESTAMP_LOCALE);
    } catch (ParseException e) {
      // ignored
    }

    // try with DateUtils and system default locale
    try {
      long timeLong = DateUtils
          .parseDate(timestamp, TIMESTAMP_PATTERNS)
          .getTime();
      return TableConstants.nanoSecondsFromMillis(timeLong, TableConstants.TIMESTAMP_LOCALE);
    } catch (ParseException e) {
      // ignored
    }

    throw new IllegalArgumentException("Unable to parse timestamp");
  }
}

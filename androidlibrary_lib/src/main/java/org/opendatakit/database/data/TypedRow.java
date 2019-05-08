package org.opendatakit.database.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opendatakit.aggregate.odktables.rest.ElementDataType;
import org.opendatakit.aggregate.odktables.rest.ElementType;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.provider.DataTableColumns;

import java.util.ArrayList;
import java.util.List;

public final class TypedRow implements Parcelable {

   private final Row row;

   private final OrderedColumns columns;

   private String [] colNames;

   private static final ObjectMapper mapper = new ObjectMapper();

   public TypedRow(Row rowData, OrderedColumns orderedColumns) {
      this.row = rowData;
      this.columns = orderedColumns;
      this.colNames = null;
   }

   protected TypedRow(Parcel in) {
      row = in.readParcelable(Row.class.getClassLoader());
      columns = in.readParcelable(OrderedColumns.class.getClassLoader());
      colNames = in.createStringArray();
   }

   public static final Creator<TypedRow> CREATOR = new Creator<TypedRow>() {
      @Override
      public TypedRow createFromParcel(Parcel in) {
         return new TypedRow(in);
      }

      @Override
      public TypedRow[] newArray(int size) {
         return new TypedRow[size];
      }
   };

   private ElementDataType getColumnDataTypeFromIndex(int index) {
      if(colNames == null) {
         colNames = row.getElementKeyForIndexMap(); // not a Map, bad naming
      }
      return getColumnDataType(colNames[index]);
   }

   private ElementDataType getColumnDataType(String key) {
      // if you change this function make sure to update TypeColumnWebIfCache
      if (DataTableColumns.CONFLICT_TYPE.equals(key)) {
         return ElementDataType.integer;
      }

      List<String> adminColumns = DataTableColumns.getAdminColumns();

      if(adminColumns.contains(key)) {
         return ElementDataType.string;
      }

      ColumnDefinition def;
      try {
         def = columns.find(key);
         ElementType type = def.getType();
         return type.getDataType();
      } catch (IllegalArgumentException e) {
         return ElementDataType.string;
      }
   }

   /**
    * Return the String representing the contents of the cellIndex'th column
    * <p>
    * Null values are returned as nulls.
    *
    * @param cellIndex cellIndex of data or metadata column (0..nCol-1)
    * @return String representation of contents of column. Null values are
    * returned as null. Note that boolean values are reported as "1" or "0"
    */
   public String getRawStringByIndex(int cellIndex) {
      return row.getRawStringByIndex(cellIndex);
   }

   /**
    * Return the String representing the contents of the cell in the "key" column.
    * <p>
    * Null values are returned as nulls.
    *
    * @param key The name of the column holding the desired data
    * @return String representation of contents of column. Null values are
    * returned as null. Note that boolean values are reported as "1" or "0"
    */
   public String getRawStringByKey(String key) {
      return row.getRawStringByKey(key);
   }


   /**
    * Return the Typed object representing the contents of the cell in the "key" column.
    * <p>
    * Null values are returned as nulls.
    *
    * @param key The name of the column holding the desired data
    * @return String representation of contents of column. Null values are
    * returned as null. Note that boolean values are reported as "1" or "0"
    */
   public final Object getDataByKey(String key) {
      ElementDataType dataType = getColumnDataType(key);
      if(dataType == null) {
         return null;
      }
      try {
         if (ElementDataType.string.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else if (ElementDataType.integer.equals(dataType)) {
            return row.getDataType(key, Long.class);
         } else if (ElementDataType.number.equals(dataType)) {
            return row.getDataType(key, Double.class);
         } else if (ElementDataType.bool.equals(dataType)) {
            return row.getDataType(key, Boolean.class);
         } else if (ElementDataType.array.equals(dataType)) {
            return row.getDataType(key, ArrayList.class);
         } else if (ElementDataType.rowpath.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else if (ElementDataType.configpath.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else if (ElementDataType.object.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else {
            throw new IllegalStateException("Unexpected data type in SQLite table");
         }
      } catch (ClassCastException e) {
         // JsonParseException and JsonMappingException extends IOException and will be caught here
         WebLogger.getLogger(null).printStackTrace(e);
         throw new IllegalStateException(
             "Unexpected data type conversion failure " + e + " on SQLite table");
      }

   }

   /**
    * Return the Typed object representing the contents of the cell given cellIndex column position
    * <p>
    * Null values are returned as nulls.
    *
    * @param cellIndex column position in table
    * @return String representation of contents of column. Null values are
    * returned as null.
    */
   public final Object getDataByIndex(int cellIndex) {
      ElementDataType dataType = getColumnDataTypeFromIndex(cellIndex);
      if(dataType == null) {
         return null;
      }
      try {
         if (ElementDataType.string.equals(dataType)) {
            return row.getRawStringByIndex(cellIndex);
         } else if (ElementDataType.integer.equals(dataType)) {
            return row.getDataType(cellIndex, Long.class);
         } else if (ElementDataType.number.equals(dataType)) {
            return row.getDataType(cellIndex, Double.class);
         } else if (ElementDataType.bool.equals(dataType)) {
            return row.getDataType(cellIndex, Boolean.class);
         } else if (ElementDataType.array.equals(dataType)) {
            return row.getDataType(cellIndex, ArrayList.class);
         } else if (ElementDataType.rowpath.equals(dataType)) {
            return row.getRawStringByIndex(cellIndex);
         } else if (ElementDataType.configpath.equals(dataType)) {
            return row.getRawStringByIndex(cellIndex);
         } else if (ElementDataType.object.equals(dataType)) {
            return row.getRawStringByIndex(cellIndex);
         } else {
            throw new IllegalStateException("Unexpected data type in SQLite table");
         }
      } catch (ClassCastException e) {
         // JsonParseException and JsonMappingException extends IOException and will be caught here
         WebLogger.getLogger(null).printStackTrace(e);
         throw new IllegalStateException(
             "Unexpected data type conversion failure " + e + " on SQLite table");
      }

   }


   /**
    * Return the Typed object representing the contents of the cell in the "key" column.
    * <p>
    * Null values are returned as nulls.
    *
    * @param key The name of the column holding the desired data
    * @return String representation of contents of column. Null values are
    * returned as null. Note that boolean values are reported as "1" or "0"
    */
   public final Object getOdkDataIfDataByKey(String key) {
      ElementDataType dataType = getColumnDataType(key);
      if(dataType == null) {
         return null;
      }
      return getDataType(key, getOdkDataWebIfType(dataType));

   }

   /**
    * Return the Typed object representing the contents of the cell given cellIndex column position
    * <p>
    * Null values are returned as nulls.
    *
    * @param cellIndex column position in table
    * @return String representation of contents of column. Null values are
    * returned as null.
    */
   public final Object getOdkDataIfDataByIndex(int cellIndex) {
      ElementDataType dataType = getColumnDataTypeFromIndex(cellIndex);
      if(dataType == null) {
         return null;
      }
      return getDataType(cellIndex, getOdkDataWebIfType(dataType));

   }


   /**
    * Return the string representing the Typed contents of the cell in the "key" column.
    * <p>
    * Null values are returned as nulls.
    *
    * @param key The name of the column holding the desired data
    * @return String representation of the Typed contents of column. Null values are
    * returned as null.
    */
   public final String getStringValueByKey(String key) {
      ElementDataType dataType = getColumnDataType(key);
      if(dataType == null) {
         return null;
      }
      if (row == null) {
         return null;
      }
      try {
         if (ElementDataType.string.equals(dataType)) {
            return row.getRawStringByKey(key); 
         } else if (ElementDataType.integer.equals(dataType)) {
            Long i = row.getDataType(key, Long.class);
            return (i == null ? null : i.toString());
         } else if (ElementDataType.number.equals(dataType)) {
            Double d = row.getDataType(key, Double.class);
            return (d == null ? null : d.toString());
         } else if (ElementDataType.bool.equals(dataType)) {
            Boolean bool = row.getDataType(key, Boolean.class);
            return (bool == null ? null : bool.toString());
         } else if (ElementDataType.array.equals(dataType)) {
            ArrayList<String> a = row.getDataType(key, ArrayList.class);
            return (a == null ? null : mapper.writeValueAsString(a));
         } else if (ElementDataType.rowpath.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else if (ElementDataType.configpath.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else if (ElementDataType.object.equals(dataType)) {
            return row.getRawStringByKey(key);
         } else {
            throw new IllegalStateException("Unexpected data type in SQLite table");
         }
      } catch (ClassCastException | JsonProcessingException e ) {
         // JsonParseException and JsonMappingException extends IOException and will be caught here
         WebLogger.getLogger(null).printStackTrace(e);
         throw new IllegalStateException(
             "Unexpected data type conversion failure " + e + " on SQLite table");
      }

   }

   /**
    * These are the data type mappings used across the odkData Javascript interface.
    * NOTE: integer data types are mapped to Long values.
    * This is consistent with the support for Long values in Javascript.
    *
    * @param dataType
    * @return
    */
   public static Class<?> getOdkDataWebIfType(ElementDataType dataType) {

      if ( dataType == ElementDataType.integer ) {
         return Long.class;
      }

      if ( dataType == ElementDataType.number ) {
         return Double.class;
      }

      if ( dataType == ElementDataType.bool ) {
         return Boolean.class;
      }

      return String.class;
   }

   /**
    * Return a pointer to the table this row belongs to
    * Used in AggregateSynchronizer and BaseTable
    *
    * @return the owner table
    */
   @SuppressWarnings("unused")
   public String[] getElementKeyForIndexMap() {
      return row.getElementKeyForIndexMap();
   }


   /**
    * Return the data stored in the cursor at the given cellIndex column position
    * as null OR whatever data type it is.
    * <p>
    * This does not actually convert data types from one type to the other.
    * Instead, it safely preserves null values and returns boxed data values.
    * If you specify ArrayList or HashMap, it JSON deserializes the value into
    * one of those.
    *
    * @param cellIndex cellIndex of data or metadata column (0..nCol-1)
    * @param clazz the class to deserialize to
    * @return the deserialized data type
    */
   @SuppressWarnings("unchecked")
   public final <T> T getDataType(int cellIndex, Class<T> clazz) throws IllegalStateException {
      return row.getDataType(cellIndex, clazz);
   }

   public final <T> T getDataType(String elementKey, Class<T> clazz) {
      return row.getDataType(elementKey, clazz);
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelable(row, flags);
      dest.writeParcelable(columns, flags);
      dest.writeStringArray(colNames);
   }
}

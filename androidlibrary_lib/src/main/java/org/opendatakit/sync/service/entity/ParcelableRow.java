package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;

import java.util.ArrayList;

public class ParcelableRow extends Row implements Parcelable {
  public ParcelableRow() {
  }

  public ParcelableRow(Row r) {
    super(r);
  }

  protected ParcelableRow(Parcel in) {
    setCreateUser(in.readString());
    setDataETagAtModification(in.readString());
    setDeleted(in.readByte() == 1);
    setFormId(in.readString());
    setLastUpdateUser(in.readString());
    setLocale(in.readString());
    setRowETag(in.readString());
    setRowFilterScope(ParcelableRowFilterScope.CREATOR.createFromParcel(in));
    setRowId(in.readString());
    setSavepointCreator(in.readString());
    setSavepointTimestamp(in.readString());
    setSavepointType(in.readString());
    setValues(readValues(in));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    writeToParcel(this, dest, flags);
  }

  public static void writeToParcel(Row row, Parcel dest, int flags) {
    dest.writeString(row.getCreateUser());
    dest.writeString(row.getDataETagAtModification());
    dest.writeByte((byte) (row.isDeleted() ? 1 : 0));
    dest.writeString(row.getFormId());
    dest.writeString(row.getLastUpdateUser());
    dest.writeString(row.getLocale());
    dest.writeString(row.getRowETag());
    ParcelableRowFilterScope.writeToParcel(row.getRowFilterScope(), dest, flags);
    dest.writeString(row.getRowId());
    dest.writeString(row.getSavepointCreator());
    dest.writeString(row.getSavepointTimestamp());
    dest.writeString(row.getSavepointType());
    writeValues(row.getValues(), dest, flags);
  }

  private static void writeValues(ArrayList<DataKeyValue> values, Parcel dest, int flags) {
    if (values == null) {
      dest.writeInt(-1);
      return;
    }

    dest.writeInt(values.size());
    for (DataKeyValue dkv : values) {
      ParcelableDataKeyValue.writeToParcel(dkv, dest, flags);
    }
  }

  private ArrayList<DataKeyValue> readValues(Parcel in) {
    int size = in.readInt();

    if (size < 0) {
      return null;
    }

    ArrayList<DataKeyValue> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(new ParcelableDataKeyValue(in));
    }

    return list;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRow> CREATOR = new Creator<ParcelableRow>() {
    @Override
    public ParcelableRow createFromParcel(Parcel in) {
      return new ParcelableRow(in);
    }

    @Override
    public ParcelableRow[] newArray(int size) {
      return new ParcelableRow[size];
    }
  };
}

package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.Row;

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
    setValues(in.readArrayList(ParcelableDataKeyValue.class.getClassLoader()));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
//    dest.writeString(getCreateUser());
//    dest.writeString(getDataETagAtModification());
//    dest.writeByte((byte) (isDeleted() ? 1 : 0));
//    dest.writeString(getFormId());
//    dest.writeString(getLastUpdateUser());
//    dest.writeString(getLocale());
//    dest.writeString(getRowETag());
//    ParcelableRowFilterScope.writeToParcel(getRowFilterScope(), dest, flags);
//    dest.writeString(getRowId());
//    dest.writeString(getSavepointCreator());
//    dest.writeString(getSavepointTimestamp());
//    dest.writeString(getSavepointType());
//    dest.writeList(getValues());

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
    dest.writeList(row.getValues());
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

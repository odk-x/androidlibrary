package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.DataKeyValue;
import org.opendatakit.aggregate.odktables.rest.entity.Row;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ParcelableRow extends Row implements Parcelable {
  protected ParcelableRow(Parcel in) {
    setCreateUser(in.readString());
    setDataETagAtModification(in.readString());
    setDeleted(in.readByte() == 1);
    setFormId(in.readString());
    setLastUpdateUser(in.readString());
    setLocale(in.readString());
    setRowETag(in.readString());
    setRowFilterScope(((RowFilterScope) in.readParcelable(ParcelableRowFilterScope.class.getClassLoader())));
    setRowId(in.readString());
    setSavepointCreator(in.readString());
    setSavepointTimestamp(in.readString());
    setSavepointType(in.readString());
    setValues(in.readArrayList(ParcelableDataKeyValue.class.getClassLoader()));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getCreateUser());
    dest.writeString(getDataETagAtModification());
    dest.writeByte((byte) (isDeleted() ? 1 : 0));
    dest.writeString(getFormId());
    dest.writeString(getLastUpdateUser());
    dest.writeString(getLocale());
    dest.writeString(getRowETag());
    dest.writeParcelable(((ParcelableRowFilterScope) getRowFilterScope()), flags);
    dest.writeString(getRowId());
    dest.writeString(getSavepointCreator());
    dest.writeString(getSavepointTimestamp());
    dest.writeString(getSavepointType());

    List<ParcelableDataKeyValue> pDkvl = new ArrayList<>();
    for (DataKeyValue dataKeyValue : getValues()) {
      pDkvl.add(((ParcelableDataKeyValue) dataKeyValue));
    }
    dest.writeTypedList(pDkvl);
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

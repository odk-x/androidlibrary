package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.Column;

public class ParcelableColumn extends Column implements Parcelable {
  public ParcelableColumn(Column column) {
    super(
        column.getElementKey(),
        column.getElementName(),
        column.getElementType(),
        column.getListChildElementKeys()
    );
  }

  protected ParcelableColumn(Parcel in) {
    super(
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString()
    );
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getElementKey());
    dest.writeString(getElementName());
    dest.writeString(getElementType());
    dest.writeString(getListChildElementKeys());
  }

  public static final Creator<ParcelableColumn> CREATOR = new Creator<ParcelableColumn>() {
    @Override
    public ParcelableColumn createFromParcel(Parcel in) {
      return new ParcelableColumn(in);
    }

    @Override
    public ParcelableColumn[] newArray(int size) {
      return new ParcelableColumn[size];
    }
  };
}

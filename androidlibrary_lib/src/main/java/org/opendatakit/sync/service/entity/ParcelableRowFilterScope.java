package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableRowFilterScope extends RowFilterScope implements Parcelable {
  protected ParcelableRowFilterScope(Parcel in) {
    super(
        ((Access) in.readSerializable()),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString()
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(getDefaultAccess());
    dest.writeString(getRowOwner());
    dest.writeString(getGroupReadOnly());
    dest.writeString(getGroupModify());
    dest.writeString(getGroupPrivileged());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableRowFilterScope> CREATOR = new Creator<ParcelableRowFilterScope>() {
    @Override
    public ParcelableRowFilterScope createFromParcel(Parcel in) {
      return new ParcelableRowFilterScope(in);
    }

    @Override
    public ParcelableRowFilterScope[] newArray(int size) {
      return new ParcelableRowFilterScope[size];
    }
  };
}

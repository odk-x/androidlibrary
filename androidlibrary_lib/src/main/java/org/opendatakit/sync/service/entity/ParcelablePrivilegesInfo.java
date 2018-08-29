package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.PrivilegesInfo;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelablePrivilegesInfo extends PrivilegesInfo implements Parcelable {
  protected ParcelablePrivilegesInfo(Parcel in) {
    super(
        in.readString(),
        in.readString(),
        in.readArrayList(null),
        in.readString()
    );
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getUser_id());
    dest.writeString(getFull_name());
    dest.writeStringList(getRoles());
    dest.writeString(getDefaultGroup());
  }

  public static final Creator<ParcelablePrivilegesInfo> CREATOR = new Creator<ParcelablePrivilegesInfo>() {
    @Override
    public ParcelablePrivilegesInfo createFromParcel(Parcel in) {
      return new ParcelablePrivilegesInfo(in);
    }

    @Override
    public ParcelablePrivilegesInfo[] newArray(int size) {
      return new ParcelablePrivilegesInfo[size];
    }
  };
}

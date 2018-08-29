package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.UserInfo;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUserInfo extends UserInfo implements Parcelable {
  protected ParcelableUserInfo(Parcel in) {
    super(
        in.readString(),
        in.readString(),
        in.readArrayList(null)
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getUser_id());
    dest.writeString(getFull_name());
    dest.writeStringList(getRoles());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableUserInfo> CREATOR = new Creator<ParcelableUserInfo>() {
    @Override
    public ParcelableUserInfo createFromParcel(Parcel in) {
      return new ParcelableUserInfo(in);
    }

    @Override
    public ParcelableUserInfo[] newArray(int size) {
      return new ParcelableUserInfo[size];
    }
  };
}

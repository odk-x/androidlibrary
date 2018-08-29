package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.UserInfoList;

import java.util.ArrayList;
import java.util.List;

public class ParcelableUserInfoList extends UserInfoList implements Parcelable {
  public ParcelableUserInfoList() {
  }

  protected ParcelableUserInfoList(Parcel in) {
    super(in.readArrayList(ParcelableUserInfo.class.getClassLoader()));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    List<ParcelableUserInfo> pUserInfoList = new ArrayList<>();
    for (int i = 0; i < size(); i++) {
      pUserInfoList.add(((ParcelableUserInfo) get(i)));
    }
    dest.writeTypedList(pUserInfoList);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableUserInfoList> CREATOR = new Creator<ParcelableUserInfoList>() {
    @Override
    public ParcelableUserInfoList createFromParcel(Parcel in) {
      return new ParcelableUserInfoList(in);
    }

    @Override
    public ParcelableUserInfoList[] newArray(int size) {
      return new ParcelableUserInfoList[size];
    }
  };
}

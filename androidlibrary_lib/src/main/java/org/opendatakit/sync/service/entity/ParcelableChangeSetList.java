package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.ChangeSetList;

public class ParcelableChangeSetList extends ChangeSetList implements Parcelable {
  protected ParcelableChangeSetList(Parcel in) {
    super(in.readArrayList(null), in.readString(), in.readString());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeStringList(getChangeSets());
    dest.writeString(getDataETag());
    dest.writeString(getSequenceValue());
  }

  public static final Creator<ParcelableChangeSetList> CREATOR = new Creator<ParcelableChangeSetList>() {
    @Override
    public ParcelableChangeSetList createFromParcel(Parcel in) {
      return new ParcelableChangeSetList(in);
    }

    @Override
    public ParcelableChangeSetList[] newArray(int size) {
      return new ParcelableChangeSetList[size];
    }
  };
}

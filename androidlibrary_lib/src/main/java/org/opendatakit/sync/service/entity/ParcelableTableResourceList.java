package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ParcelableTableResourceList extends TableResourceList implements Parcelable {
  public ParcelableTableResourceList() {
    super();
  }

  protected ParcelableTableResourceList(Parcel in) {
    super(
        in.readArrayList(ParcelableTableResource.class.getClassLoader()),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readByte() == 1,
        in.readByte() == 1
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeList(getTables());
    dest.writeString(getWebSafeRefetchCursor());
    dest.writeString(getWebSafeBackwardCursor());
    dest.writeString(getWebSafeResumeCursor());
    dest.writeByte((byte) (isHasMoreResults() ? 1 : 0));
    dest.writeByte((byte) (isHasPriorResults() ? 1 : 0));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableResourceList> CREATOR = new Creator<ParcelableTableResourceList>() {
    @Override
    public ParcelableTableResourceList createFromParcel(Parcel in) {
      return new ParcelableTableResourceList(in);
    }

    @Override
    public ParcelableTableResourceList[] newArray(int size) {
      return new ParcelableTableResourceList[size];
    }
  };
}

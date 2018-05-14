package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;

import java.util.ArrayList;
import java.util.List;

public class ParcelableRowResourceList extends RowResourceList implements Parcelable {
  public ParcelableRowResourceList(ArrayList<RowResource> rows, String dataETag, String tableUri, String refetchCursor, String backCursor, String resumeCursor, boolean hasMore, boolean hasPrior) {
    super(rows, dataETag, tableUri, refetchCursor, backCursor, resumeCursor, hasMore, hasPrior);
  }

  protected ParcelableRowResourceList(Parcel in) {
    super(
        in.readArrayList(ParcelableRowResource.class.getClassLoader()),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readByte() == 1,
        in.readByte() == 1
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeList(getRows());
    dest.writeString(getDataETag());
    dest.writeString(getTableUri());
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

  public static final Creator<ParcelableRowResourceList> CREATOR = new Creator<ParcelableRowResourceList>() {
    @Override
    public ParcelableRowResourceList createFromParcel(Parcel in) {
      return new ParcelableRowResourceList(in);
    }

    @Override
    public ParcelableRowResourceList[] newArray(int size) {
      return new ParcelableRowResourceList[size];
    }
  };
}

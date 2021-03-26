package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.RowResource;
import org.opendatakit.aggregate.odktables.rest.entity.RowResourceList;

import java.util.ArrayList;

public class ParcelableRowResourceList extends RowResourceList implements Parcelable {
  public ParcelableRowResourceList() {
    super();
  }

  public ParcelableRowResourceList(ArrayList<RowResource> rows, String dataETag, String tableUri, String refetchCursor, String backCursor, String resumeCursor, boolean hasMore, boolean hasPrior) {
    super(rows, dataETag, tableUri, refetchCursor, backCursor, resumeCursor, hasMore, hasPrior);
  }

  protected ParcelableRowResourceList(Parcel in) {
    super(
        null,
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readString(),
        in.readByte() == 1,
        in.readByte() == 1
    );

    setRows(readRows(in));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getDataETag());
    dest.writeString(getTableUri());
    dest.writeString(getWebSafeRefetchCursor());
    dest.writeString(getWebSafeBackwardCursor());
    dest.writeString(getWebSafeResumeCursor());
    dest.writeByte((byte) (isHasMoreResults() ? 1 : 0));
    dest.writeByte((byte) (isHasPriorResults() ? 1 : 0));
    writeRows(dest, flags);
  }

  private void writeRows(Parcel dest, int flags) {
    if (getRows() == null) {
      dest.writeInt(-1);
      return;
    }

    dest.writeInt(getRows().size());
    for (RowResource rowResource : getRows()) {
      ParcelableRowResource.writeToParcel(rowResource, dest, flags);
    }
  }

  private ArrayList<RowResource> readRows(Parcel in) {
    int size = in.readInt();

    if (size < 0) {
      return null;
    }

    ArrayList<RowResource> list = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      list.add(new ParcelableRowResource(in));
    }

    return list;
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

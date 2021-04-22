package org.opendatakit.sync.service.entity;

import android.os.Parcel;
import android.os.Parcelable;

import org.opendatakit.aggregate.odktables.rest.entity.TableResource;
import org.opendatakit.aggregate.odktables.rest.entity.TableResourceList;

import java.util.ArrayList;

public class ParcelableTableResourceList extends TableResourceList implements Parcelable {
  public ParcelableTableResourceList() {
    super();
  }

  protected ParcelableTableResourceList(Parcel in) {
    super(
        null,
        in.readString(),
        in.readString(),
        in.readString(),
        in.readByte() == 1,
        in.readByte() == 1
    );

    setTables(readTables(in));
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getWebSafeRefetchCursor());
    dest.writeString(getWebSafeBackwardCursor());
    dest.writeString(getWebSafeResumeCursor());
    dest.writeByte((byte) (isHasMoreResults() ? 1 : 0));
    dest.writeByte((byte) (isHasPriorResults() ? 1 : 0));
    writeTables(dest, flags);
  }

  private void writeTables(Parcel dest, int flags) {
    if (getTables() == null) {
      dest.writeInt(-1);
      return;
    }

    dest.writeInt(getTables().size());
    for (TableResource tableResource : getTables()) {
      ParcelableTableResource.writeToParcel(tableResource, dest, flags);
    }
  }

  private ArrayList<TableResource> readTables(Parcel in) {
    int size = in.readInt();

    if (size < 0) {
      return null;
    }

    ArrayList<TableResource> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(new ParcelableTableResource(in));
    }

    return list;
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

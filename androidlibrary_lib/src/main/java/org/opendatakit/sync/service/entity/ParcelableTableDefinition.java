package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinition;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ParcelableTableDefinition extends TableDefinition implements Parcelable {
  protected ParcelableTableDefinition(Parcel in) {
    super(
        in.readString(),
        in.readString(),
        in.readArrayList(ParcelableColumn.class.getClassLoader())
    );
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(getTableId());
    dest.writeString(getSchemaETag());

    List<ParcelableColumn> pColumns = new ArrayList<>();
    for (Column column : getColumns()) {
      pColumns.add(((ParcelableColumn) column));
    }
    dest.writeTypedList(pColumns);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableDefinition> CREATOR = new Creator<ParcelableTableDefinition>() {
    @Override
    public ParcelableTableDefinition createFromParcel(Parcel in) {
      return new ParcelableTableDefinition(in);
    }

    @Override
    public ParcelableTableDefinition[] newArray(int size) {
      return new ParcelableTableDefinition[size];
    }
  };
}

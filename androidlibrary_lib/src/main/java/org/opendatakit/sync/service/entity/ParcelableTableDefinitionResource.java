package org.opendatakit.sync.service.entity;

import org.opendatakit.aggregate.odktables.rest.entity.Column;
import org.opendatakit.aggregate.odktables.rest.entity.TableDefinitionResource;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ParcelableTableDefinitionResource extends TableDefinitionResource implements Parcelable {
  public ParcelableTableDefinitionResource(String tableId, String schemaETag, ArrayList<Column> columns) {
    super(tableId, schemaETag, columns);
  }

  public ParcelableTableDefinitionResource(TableDefinitionResource tableDefinitionResource) {
    super(tableDefinitionResource);

    setSelfUri(tableDefinitionResource.getSelfUri());
    setTableUri(tableDefinitionResource.getTableUri());
  }

  public ParcelableTableDefinitionResource(Parcel in) {
    super(ParcelableTableDefinition.CREATOR.createFromParcel(in));

    setSelfUri(in.readString());
    setTableUri(in.readString());
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    ParcelableTableDefinition.writeToParcel(this, dest, flags);

    dest.writeString(getSelfUri());
    dest.writeString(getTableId());
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ParcelableTableDefinitionResource> CREATOR = new Creator<ParcelableTableDefinitionResource>() {
    @Override
    public ParcelableTableDefinitionResource createFromParcel(Parcel in) {
      return new ParcelableTableDefinitionResource(in);
    }

    @Override
    public ParcelableTableDefinitionResource[] newArray(int size) {
      return new ParcelableTableDefinitionResource[size];
    }
  };
}

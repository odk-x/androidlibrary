package org.opendatakit.builder.csvparser.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.opendatakit.aggregate.odktables.rest.TableConstants;
import org.opendatakit.aggregate.odktables.rest.entity.RowFilterScope;

public abstract class RowFilterScopeCsvMixin extends RowFilterScope {
  private RowFilterScopeCsvMixin(Access access, String rowOwner, String groupReadOnly, String groupModify, String groupPrivileged) {
    super(access, rowOwner, groupReadOnly, groupModify, groupPrivileged);
  }

  @Override
  @JsonProperty(TableConstants.DEFAULT_ACCESS)
  public abstract Access getDefaultAccess();

  @Override
  @JsonProperty(TableConstants.DEFAULT_ACCESS)
  public abstract void setDefaultAccess(Access access);

  @Override
  @JsonProperty(TableConstants.ROW_OWNER)
  public abstract String getRowOwner();

  @Override
  @JsonProperty(TableConstants.ROW_OWNER)
  public abstract void setRowOwner(String rowOwner);

  @Override
  @JsonProperty(TableConstants.GROUP_READ_ONLY)
  public abstract String getGroupReadOnly();

  @Override
  @JsonProperty(TableConstants.GROUP_READ_ONLY)
  public abstract void setGroupReadOnly(String groupReadOnly);

  @Override
  @JsonProperty(TableConstants.GROUP_MODIFY)
  public abstract String getGroupModify();

  @Override
  @JsonProperty(TableConstants.GROUP_MODIFY)
  public abstract void setGroupModify(String groupModify);

  @Override
  @JsonProperty(TableConstants.GROUP_PRIVILEGED)
  public abstract String getGroupPrivileged();

  @Override
  @JsonProperty(TableConstants.GROUP_PRIVILEGED)
  public abstract void setGroupPrivileged(String groupPrivileged);
}

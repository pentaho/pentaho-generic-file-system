/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.genericfile.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GenericFileSid;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link IGenericFileAce}.
 */
public class BaseGenericFileAce implements IGenericFileAce {
  private String recipient;
  private GenericFileSid recipientType;
  private List<GenericFilePermission> permissions;

  public BaseGenericFileAce() {
    this.permissions = new ArrayList<>();
  }

  @Nullable
  @Override
  public String getRecipient() {
    return recipient;
  }

  @Override
  public void setRecipient( @Nullable String recipient ) {
    this.recipient = recipient;
  }

  @Nullable
  @Override
  public GenericFileSid getRecipientType() {
    return recipientType;
  }

  @Override
  public void setRecipientType( @Nullable GenericFileSid recipientType ) {
    this.recipientType = recipientType;
  }

  @NonNull
  @Override
  public List<GenericFilePermission> getPermissions() {
    return permissions;
  }

  @Override
  public void setPermissions( @NonNull List<GenericFilePermission> permissions ) {
    this.permissions = Objects.requireNonNull( permissions );
  }

  @Override
  public void addPermission( @NonNull GenericFilePermission permission ) {
    Objects.requireNonNull( permission );
    this.permissions.add( permission );
  }
}

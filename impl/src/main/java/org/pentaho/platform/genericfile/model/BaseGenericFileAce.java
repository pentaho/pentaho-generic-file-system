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
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;

import java.util.List;
import java.util.Objects;

public class BaseGenericFileAce implements IGenericFileAce {
  private final String recipient;
  private final GenericFilePrincipalType recipientType;
  private final List<GenericFilePermission> permissions;

  public BaseGenericFileAce( @NonNull String recipient, @NonNull GenericFilePrincipalType recipientType,
                             @NonNull List<GenericFilePermission> permissions ) {
    this.recipient = Objects.requireNonNull( recipient );
    this.recipientType = Objects.requireNonNull( recipientType );
    this.permissions = Objects.requireNonNull( permissions );
  }

  @NonNull
  @Override
  public String getRecipient() {
    return recipient;
  }

  @NonNull
  @Override
  public GenericFilePrincipalType getRecipientType() {
    return recipientType;
  }

  @NonNull
  @Override
  public List<GenericFilePermission> getPermissions() {
    return permissions;
  }
}

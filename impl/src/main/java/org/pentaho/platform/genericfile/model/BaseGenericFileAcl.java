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
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;
import org.pentaho.platform.api.genericfile.model.IGenericFileAcl;

import java.util.List;
import java.util.Objects;

public class BaseGenericFileAcl implements IGenericFileAcl {
  private final String owner;
  private final GenericFilePrincipalType ownerType;
  private final boolean entriesInheriting;
  private final List<IGenericFileAce> entries;

  public BaseGenericFileAcl( @NonNull String owner, @NonNull GenericFilePrincipalType ownerType,
                             boolean entriesInheriting, @NonNull List<IGenericFileAce> entries ) {
    this.owner = Objects.requireNonNull( owner );
    this.ownerType = Objects.requireNonNull( ownerType );
    this.entriesInheriting = entriesInheriting;
    this.entries = Objects.requireNonNull( entries );
  }

  @NonNull
  @Override
  public String getOwner() {
    return owner;
  }

  @NonNull
  @Override
  public GenericFilePrincipalType getOwnerType() {
    return ownerType;
  }

  @Override
  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  @NonNull
  @Override
  public List<IGenericFileAce> getEntries() {
    return entries;
  }
}

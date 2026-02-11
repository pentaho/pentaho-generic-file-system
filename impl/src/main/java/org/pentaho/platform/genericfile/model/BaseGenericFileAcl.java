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
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;
import org.pentaho.platform.api.genericfile.model.IGenericFileAce;
import org.pentaho.platform.api.genericfile.model.IGenericFileAcl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of {@link IGenericFileAcl}.
 */
public class BaseGenericFileAcl implements IGenericFileAcl {
  private String owner;
  private GenericFilePrincipalType ownerType;
  private boolean entriesInheriting;
  private List<IGenericFileAce> entries;

  public BaseGenericFileAcl() {
    this.entries = new ArrayList<>();
  }

  @Nullable
  @Override
  public String getOwner() {
    return owner;
  }

  @Override
  public void setOwner( @Nullable String owner ) {
    this.owner = owner;
  }

  @Nullable
  @Override
  public GenericFilePrincipalType getOwnerType() {
    return ownerType;
  }

  @Override
  public void setOwnerType( @Nullable GenericFilePrincipalType ownerType ) {
    this.ownerType = ownerType;
  }

  @Override
  public boolean isEntriesInheriting() {
    return entriesInheriting;
  }

  @Override
  public void setEntriesInheriting( boolean entriesInheriting ) {
    this.entriesInheriting = entriesInheriting;
  }

  @NonNull
  @Override
  public List<IGenericFileAce> getEntries() {
    return entries;
  }

  @Override
  public void setEntries( @NonNull List<IGenericFileAce> entries ) {
    this.entries = Objects.requireNonNull( entries );
  }

  @Override
  public void addEntry( @NonNull IGenericFileAce entry ) {
    Objects.requireNonNull( entry );
    this.entries.add( entry );
  }
}

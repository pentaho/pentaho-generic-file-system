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

package org.pentaho.platform.api.genericfile.model;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;

import java.util.List;

/**
 * Represents the Access Control List (ACL) for a generic file.
 * <p>
 * An ACL defines the owner of the file and a list of access control entries (ACEs) that specify the permissions
 * granted to users or roles.
 * </p>
 */
public interface IGenericFileAcl {
  /**
   * Gets the owner of the file.
   *
   * @return The owner of the file, or {@code null} if not set.
   */
  @Nullable
  String getOwner();

  /**
   * Sets the owner of the file.
   *
   * @param owner The owner of the file.
   */
  void setOwner( @Nullable String owner );

  /**
   * Gets the type of the owner (e.g., {@link GenericFilePrincipalType#USER} or {@link GenericFilePrincipalType#ROLE}).
   *
   * @return The owner type, or {@code null} if not set.
   */
  @Nullable
  GenericFilePrincipalType getOwnerType();

  /**
   * Sets the type of the owner.
   *
   * @param ownerType The owner type.
   */
  void setOwnerType( @Nullable GenericFilePrincipalType ownerType );

  /**
   * Indicates whether entries are inheriting from the parent folder.
   *
   * @return {@code true} if entries are inheriting; {@code false} otherwise.
   */
  boolean isEntriesInheriting();

  /**
   * Sets whether entries should inherit from the parent folder.
   *
   * @param entriesInheriting {@code true} to enable inheritance; {@code false} otherwise.
   */
  void setEntriesInheriting( boolean entriesInheriting );

  /**
   * Gets the list of access control entries (ACEs).
   *
   * @return A list of ACEs, or an empty list if none are defined.
   */
  @NonNull
  List<IGenericFileAce> getEntries();

  /**
   * Sets the list of access control entries (ACEs).
   *
   * @param entries The list of ACEs.
   */
  void setEntries( @NonNull List<IGenericFileAce> entries );

  /**
   * Adds an access control entry (ACE) to the list.
   *
   * @param entry The ACE to add.
   */
  void addEntry( @NonNull IGenericFileAce entry );
}


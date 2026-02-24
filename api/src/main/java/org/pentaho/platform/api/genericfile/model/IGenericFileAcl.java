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
   * @return The owner of the file.
   */
  @NonNull
  String getOwner();

  /**
   * Gets the type of the owner (e.g., {@link GenericFilePrincipalType#USER} or {@link GenericFilePrincipalType#ROLE}).
   *
   * @return The owner type.
   */
  @NonNull
  GenericFilePrincipalType getOwnerType();

  /**
   * Indicates whether entries are inheriting from the parent folder.
   *
   * @return {@code true} if entries are inheriting; {@code false} otherwise.
   */
  boolean isEntriesInheriting();

  /**
   * Gets the list of access control entries (ACEs).
   *
   * @return A list of ACEs. This may be {@code null} if {@code entriesInheriting} is {@code true}, that is, the ACL
   * is inheriting entries from the parent folder and does not have its own entries.
   */
  @Nullable
  List<IGenericFileAce> getEntries();
}


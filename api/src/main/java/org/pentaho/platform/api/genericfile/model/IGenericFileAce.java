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
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GenericFilePrincipalType;

import java.util.List;

/**
 * Represents an Access Control Entry (ACE) in an Access Control List (ACL).
 * <p>
 * An ACE defines the permissions granted to a specific recipient (user or role).
 * </p>
 */
public interface IGenericFileAce {
  /**
   * Gets the recipient of this ACE.
   *
   * @return The recipient, or {@code null} if not set.
   */
  @Nullable
  String getRecipient();

  /**
   * Sets the recipient of this ACE.
   *
   * @param recipient The recipient.
   */
  void setRecipient( @Nullable String recipient );

  /**
   * Gets the type of recipient (e.g., {@link GenericFilePrincipalType#USER} or {@link GenericFilePrincipalType#ROLE}).
   *
   * @return The recipient type, or {@code null} if not set.
   */
  @Nullable
  GenericFilePrincipalType getRecipientType();

  /**
   * Sets the type of recipient.
   *
   * @param recipientType The recipient type (e.g., {@link GenericFilePrincipalType#USER} or {@link GenericFilePrincipalType#ROLE}).
   */
  void setRecipientType( @Nullable GenericFilePrincipalType recipientType );

  /**
   * Gets the list of permissions granted to the recipient.
   *
   * @return A list of permission, or an empty list if none are defined.
   */
  @NonNull
  List<GenericFilePermission> getPermissions();

  /**
   * Sets the list of permissions granted to the recipient.
   *
   * @param permissions The list of permissions.
   */
  void setPermissions( @NonNull List<GenericFilePermission> permissions );

  /**
   * Adds a permission to the list.
   *
   * @param permission The permission to add.
   */
  void addPermission( @NonNull GenericFilePermission permission );
}


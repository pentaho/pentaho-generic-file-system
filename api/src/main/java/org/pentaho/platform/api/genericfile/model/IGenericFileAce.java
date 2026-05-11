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
   * @return The recipient.
   */
  @NonNull
  String getRecipient();

  /**
   * Gets the type of recipient (e.g., {@link GenericFilePrincipalType#USER} or {@link GenericFilePrincipalType#ROLE}).
   *
   * @return The recipient type.
   */
  @NonNull
  GenericFilePrincipalType getRecipientType();

  /**
   * Indicates whether this ACE is modifiable.
   *
   * @return {@code true} if the ACE is modifiable; {@code false} otherwise.
   */
  boolean isModifiable();

  /**
   * Gets the list of permissions granted to the recipient.
   *
   * @return A list of permissions.
   */
  @NonNull
  List<GenericFilePermission> getPermissions();
}


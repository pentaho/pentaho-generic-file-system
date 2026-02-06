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

package org.pentaho.platform.api.genericfile;

/**
 * File security identifier (SID) used as file owners and permission recipients in access control entries.
 *
 * <p>
 * Same abstraction as {@code org.springframework.security.acls.sid.Sid}.
 * </p>
 */
public enum GenericFileSid {
  /**
   * Owner is a user.
   */
  USER( 0 ),

  /**
   * Owner is a role.
   */
  ROLE( 1 );

  private final int value;

  GenericFileSid( int value ) {
    this.value = value;
  }

  /**
   * Gets the numeric value associated with this owner type.
   *
   * @return The numeric value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Gets the owner type enum from its numeric value.
   *
   * @param value The numeric value.
   * @return The corresponding owner type, or {@code null} if not found.
   */
  public static GenericFileSid fromValue( int value ) {
    for ( GenericFileSid type : values() ) {
      if ( type.value == value ) {
        return type;
      }
    }

    return null;
  }
}


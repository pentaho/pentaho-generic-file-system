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

public enum GenericFilePermission {
  READ( 0 ),
  WRITE( 1 ),
  DELETE( 2 ),
  ACL_MANAGEMENT( 3 ),
  ALL( 4 );

  private final int value;

  GenericFilePermission( int value ) {
    this.value = value;
  }

  /**
   * Gets the numeric value associated with this permission.
   *
   * @return The numeric value.
   */
  public int getValue() {
    return value;
  }

  /**
   * Gets the permission enum from its numeric value.
   *
   * @param value The numeric value.
   * @return The corresponding permission, or {@code null} if not found.
   */
  public static GenericFilePermission fromValue( int value ) {
    for ( GenericFilePermission permission : values() ) {
      if ( permission.value == value ) {
        return permission;
      }
    }

    return null;
  }
}

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

public interface IProviderable {
  /**
   * Gets the provider for this object. The provider is a string that identifies the source or type of the object.
   *
   * @return the provider string, never null
   */
  @NonNull
  String getProvider();

  /**
   * Sets the provider for this object. The provider is a string that identifies the source or type of the object.
   *
   * @param provider the provider string to set, must not be null
   */
  void setProvider( @NonNull String provider );
}

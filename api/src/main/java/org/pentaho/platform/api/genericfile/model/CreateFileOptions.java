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

import java.util.Objects;

/**
 * This class contains the options for creating a file.
 */
public class CreateFileOptions {

  /**
   * Flag indicating whether to overwrite an existing file.
   */
  private boolean overwrite;

  public CreateFileOptions() {
    this.overwrite = false;
  }

  public CreateFileOptions( boolean overwrite ) {
    this.overwrite = overwrite;
  }

  public CreateFileOptions( @NonNull CreateFileOptions createFileOptions ) {
    this.overwrite = createFileOptions.overwrite;
  }

  public boolean isOverwrite() {
    return overwrite;
  }

  public void setOverwrite( boolean overwrite ) {
    this.overwrite = overwrite;
  }

  @Override
  public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }

    if ( other == null || getClass() != other.getClass() ) {
      return false;
    }

    CreateFileOptions that = (CreateFileOptions) other;

    return Objects.equals( overwrite, that.overwrite );
  }

  @Override
  public int hashCode() {
    return Objects.hash( overwrite );
  }

}

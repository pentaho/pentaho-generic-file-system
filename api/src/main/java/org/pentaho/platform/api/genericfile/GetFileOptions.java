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

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Objects;

/**
 * This class contains the options for retrieving a file.
 */
public class GetFileOptions {
  private boolean includeMetadata;

  public GetFileOptions() {
  }

  /**
   * Copy constructor.
   *
   * @param other The options instance from which to initialize this instance.
   */
  public GetFileOptions( @NonNull GetFileOptions other ) {
    Objects.requireNonNull( other );

    this.includeMetadata = other.includeMetadata;
  }

  /**
   * Gets a value that indicates whether metadata for files is included in the result.
   * <p>
   * Defaults to {@code false}.
   *
   * @return {@code true} to include metadata; {@code false}, otherwise.
   */
  public boolean isIncludeMetadata() {
    return includeMetadata;
  }

  /**
   * Sets the include metadata value.
   *
   * @param includeMetadata {@code true} to include metadata; {@code false}, otherwise.
   */
  public void setIncludeMetadata( boolean includeMetadata ) {
    this.includeMetadata = includeMetadata;
  }

  @Override
  public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }

    if ( other == null || getClass() != other.getClass() ) {
      return false;
    }

    GetFileOptions that = (GetFileOptions) other;

    return Objects.equals( includeMetadata, that.includeMetadata );
  }

  @Override
  public int hashCode() {
    return Objects.hash( includeMetadata );
  }
}

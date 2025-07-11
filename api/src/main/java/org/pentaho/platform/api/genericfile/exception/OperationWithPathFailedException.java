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

package org.pentaho.platform.api.genericfile.exception;

import org.pentaho.platform.api.genericfile.GenericFilePath;

/**
 * The exception class thrown when an Operation has failed, and we need to have a path to give more context of the
 * error.
 */
public class OperationWithPathFailedException extends OperationFailedException {
  private final transient GenericFilePath path;

  public OperationWithPathFailedException() {
    super();
    this.path = null;
  }

  public OperationWithPathFailedException( String message ) {
    super( message );
    this.path = null;
  }

  public OperationWithPathFailedException( Throwable cause ) {
    super( cause );
    this.path = null;
  }

  public OperationWithPathFailedException( String message, Throwable cause ) {
    super( message, cause );
    this.path = null;
  }

  public OperationWithPathFailedException( GenericFilePath path ) {
    super();
    this.path = path;
  }

  public OperationWithPathFailedException( String message, GenericFilePath path ) {
    super( message );
    this.path = path;
  }

  public OperationWithPathFailedException( String message, GenericFilePath path, Throwable cause ) {
    super( message, cause );
    this.path = path;
  }

  public GenericFilePath getPath() {
    return path;
  }
}

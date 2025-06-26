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

/**
 * The exception class thrown when a generic file path already exists.
 */
public class PathAlreadyExistsException extends OperationFailedException {
  public PathAlreadyExistsException() {
    super();
  }

  public PathAlreadyExistsException( String message ) {
    super( message );
  }

  public PathAlreadyExistsException( Throwable cause ) {
    super( cause );
  }
}

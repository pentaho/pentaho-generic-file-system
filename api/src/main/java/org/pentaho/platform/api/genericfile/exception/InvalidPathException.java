/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.genericfile.exception;

/**
 * The exception class thrown when a generic file path is determined to be invalid.
 */
public class InvalidPathException extends OperationFailedException {
  public InvalidPathException() {
    super();
  }

  public InvalidPathException( String message ) {
    super( message );
  }

  public InvalidPathException( Throwable cause ) {
    super( cause );
  }
}

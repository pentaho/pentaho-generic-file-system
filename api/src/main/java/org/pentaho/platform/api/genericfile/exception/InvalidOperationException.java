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

public class InvalidOperationException extends OperationFailedException {

  public InvalidOperationException() {
    super();
  }

  /**
   * Message should not contain sensitive information.
   *
   * @param message
   */
  public InvalidOperationException( String message ) {
    super( message );
  }

}

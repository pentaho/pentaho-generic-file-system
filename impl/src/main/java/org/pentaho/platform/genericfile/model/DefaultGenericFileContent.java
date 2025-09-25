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


package org.pentaho.platform.genericfile.model;

import org.pentaho.platform.api.genericfile.model.IGenericFileContent;

import java.io.InputStream;

public class DefaultGenericFileContent implements IGenericFileContent {
  private final InputStream inputStream;
  private final String fileName;
  private final String mimeType;

  public DefaultGenericFileContent( InputStream inputStream, String fileName, String mimeType ) {
    this.inputStream = inputStream;
    this.fileName = fileName;
    this.mimeType = mimeType;
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getMimeType() {
    return mimeType;
  }
}

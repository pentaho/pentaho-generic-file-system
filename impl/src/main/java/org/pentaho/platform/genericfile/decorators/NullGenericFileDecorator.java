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

package org.pentaho.platform.genericfile.decorators;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetFileOptions;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileDecorator;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

/**
 * A no-op implementation of {@link IGenericFileDecorator}.
 */
@SuppressWarnings( "RedundantThrows" )
public class NullGenericFileDecorator implements IGenericFileDecorator {
  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateFileMetadata( @NonNull IGenericFileMetadata fileMetadata,
                                    @NonNull GenericFilePath path,
                                    @NonNull IGenericFileService service ) throws OperationFailedException {
    // intentionally left blank
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateFile( @NonNull IGenericFile file,
                            @NonNull IGenericFileService service,
                            @NonNull GetFileOptions options ) throws OperationFailedException {
    // intentionally left blank
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void decorateTree( @NonNull IGenericFileTree fileTree,
                            @NonNull IGenericFileService service,
                            @NonNull GetTreeOptions options ) throws OperationFailedException {
    // intentionally left blank
  }
}

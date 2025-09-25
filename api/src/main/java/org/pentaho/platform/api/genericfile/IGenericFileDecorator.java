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
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

/**
 * The {@code IGenericFileDecorator} interface defines a contract for classes that wish to decorate or enhance file
 * results from the {@link IGenericFileService}. Implementations of this interface can modify or add additional
 * information to file metadata and file tree structures after they have been retrieved from the underlying file
 * providers.
 */
public interface IGenericFileDecorator {
  /**
   * Decorates the metadata for a file identified by a path. This method is invoked when the caller requests a file
   * metadata (see {@link IGenericFileService#getFileMetadata} ).
   *
   * @param fileMetadata the mutable metadata to decorate (never {@code null})
   * @param path         the file path associated with the metadata (never {@code null})
   * @param service      the invoking file service (can be used to fetch additional information) (never {@code null})
   * @throws OperationFailedException if decoration cannot complete successfully. Implementations may
   *                                  either propagate or (in higher-level wrappers) have this swallowed and logged.
   */
  void decorateFileMetadata( @NonNull IGenericFileMetadata fileMetadata,
                             @NonNull GenericFilePath path,
                             @NonNull IGenericFileService service ) throws OperationFailedException;

  /**
   * Decorates a file. This method is invoked when the caller requests a file (see {@link IGenericFileService#getFile}).
   *
   * @param file    the mutable file to decorate (never {@code null})
   * @param service the invoking file service (can be used to fetch additional information) (never {@code null})
   * @param options the file retrieval options (never {@code null})
   * @throws OperationFailedException if decoration cannot complete successfully. Implementations may
   *                                  either propagate or (in higher-level wrappers) have this swallowed and logged.
   */
  void decorateFile( @NonNull IGenericFile file,
                     @NonNull IGenericFileService service,
                     @NonNull GetFileOptions options ) throws OperationFailedException;

  /**
   * Decorates a tree. This method is invoked when the caller requests a tree (see
   * {@link IGenericFileService#getTree} or {@link IGenericFileService#getRootTrees}).
   *
   * @param fileTree the mutable tree to decorate (never {@code null})
   * @param service  the invoking file service (can be used to fetch additional information) (never {@code null})
   * @param options  the tree retrieval options (never {@code null})
   * @throws OperationFailedException if decoration cannot complete successfully. Implementations may
   *                                  either propagate or (in higher-level wrappers) have this swallowed and logged.
   */
  void decorateTree( @NonNull IGenericFileTree fileTree,
                     @NonNull IGenericFileService service,
                     @NonNull GetTreeOptions options ) throws OperationFailedException;
}

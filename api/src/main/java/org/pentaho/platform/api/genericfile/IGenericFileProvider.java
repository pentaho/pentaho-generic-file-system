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
import org.pentaho.platform.api.genericfile.exception.AccessControlException;
import org.pentaho.platform.api.genericfile.exception.ConflictException;
import org.pentaho.platform.api.genericfile.exception.InvalidOperationException;
import org.pentaho.platform.api.genericfile.exception.InvalidPathException;
import org.pentaho.platform.api.genericfile.exception.NotFoundException;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.exception.ResourceAccessDeniedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileContent;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * The {@code IGenericFileProvider} interface contains operations to access and modify generic files
 * owned by a specific generic file provider.
 * <p>
 * Generic file providers are identified by their {@link #getType() type string}.
 * The {@link #owns(GenericFilePath)} method determines if a provider owns a path.
 *
 * @param <T> The specific {@link IGenericFile generic file} class used by the provider.
 * @see IGenericFileService
 */
public interface IGenericFileProvider<T extends IGenericFile> {
  /**
   * Gets specific {@link IGenericFile generic file} class used by the provider, matching the type parameter, {@code T}.
   */
  @NonNull
  Class<T> getFileClass();

  /**
   * Gets the name of the provider.
   * <p>
   * The name of a provider is a localized, human-readable name of the provider. Ideally, it should be unique.
   *
   * @see #getType()
   */
  @NonNull
  String getName();

  /**
   * Gets the unique identifier of the provider.
   *
   * @see #getName()
   */
  @NonNull
  String getType();

  /**
   * Gets a tree of files.
   * <p>
   * The results of this method are cached. To ensure fresh results, set {@link GetTreeOptions#setBypassCache(boolean)}
   * to {@code true} or call {@link #clearTreeCache()} beforehand.
   * beforehand.
   *
   * @param options The operation options.
   * @return The file tree.
   * @throws NotFoundException        If the specified base file does not exist, is not a folder, or the current user
   *                                  is not allowed to read it.
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getTree(GetTreeOptions)
   */
  @NonNull
  IGenericFileTree getTree( @NonNull GetTreeOptions options ) throws OperationFailedException;

  /**
   * Gets a list of the real root trees that this provider provides to the generic file system.
   * <p>
   * The returned root tree folders are considered to have a depth of {@code 0}.
   * <p>
   * The results of this method are not cached, and so {@link GetTreeOptions#isBypassCache()} is ignored.
   *
   * @param options The operation options.
   * @return A list of the real root trees provided by this provider.
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getRootTrees(GetTreeOptions)
   */
  @NonNull
  List<IGenericFileTree> getRootTrees( @NonNull GetTreeOptions options ) throws OperationFailedException;

  /**
   * Clears the cache of trees, for the current user session.
   *
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see #getTree(GetTreeOptions)
   * @see #createFolder(GenericFilePath)
   */
  void clearTreeCache() throws OperationFailedException;

  /**
   * Checks whether a generic file exists, is a folder and the current user can read it, given its path.
   *
   * @param path The path of the generic file.
   * @return {@code true}, if the conditions are met; {@code false}, otherwise.
   * @throws ResourceAccessDeniedException If the current user cannot access the specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#doesFolderExist(GenericFilePath)
   */
  boolean doesFolderExist( @NonNull GenericFilePath path ) throws OperationFailedException;

  /**
   * Creates a folder given its path.
   * <p>
   * This method ensures that each ancestor folder of the specified folder exists,
   * creating it if necessary, and allowed.
   * <p>
   * When the operation is successful, the folder tree session cache is automatically cleared.
   *
   * @param path The path of the generic folder to create.
   * @return {@code true}, if the folder did not exist and was created; {@code false}, if the folder already existed.
   * @throws AccessControlException    If the current user cannot perform this operation.
   * @throws InvalidPathException      If the folder path is not valid.
   * @throws InvalidOperationException If the path, or one of its prefixes, does not exist and cannot be created using
   *                                   this service (e.g. connections, buckets);
   *                                   if the path or its longest existing prefix does not reference a folder;
   *                                   if the path does not exist and the current user is not allowed to create folders
   *                                   on the folder denoted by its longest existing prefix.
   * @throws OperationFailedException  If the operation fails for some other (checked) reason.
   * @see #clearTreeCache()
   * @see IGenericFileService#createFolder(GenericFilePath)
   */
  boolean createFolder( @NonNull GenericFilePath path ) throws OperationFailedException;

  /**
   * Determines if the provider owns a given path.
   * <p>
   * The {@link GenericFilePath#getFirstSegment() provider root segment} of a generic file path is exclusive of each
   * provider and is used to determine if a path is owned by a provider.
   *
   * @param path The generic file path to check.
   * @return {@code true}, if the provider owns the specified generic file path; {@code false}, otherwise.
   */
  boolean owns( @NonNull GenericFilePath path );

  /**
   * Checks whether a generic file exists and the current user has the specified permissions on it.
   *
   * @param path        The path of the generic file.
   * @param permissions Set of permissions needed for any operation like READ/WRITE/DELETE
   * @return {@code true}, if the conditions are; {@code false}, otherwise.
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#hasAccess(String, EnumSet)
   */
  boolean hasAccess( @NonNull GenericFilePath path, @NonNull EnumSet<GenericFilePermission> permissions )
    throws OperationFailedException;

  /**
   * Gets the content of a file, given its path.
   *
   * @param path       The path of the file.
   * @param compressed If {@code true}, returns the content as a compressed archive (works for files and folders).
   *                   If {@code false}, returns the raw file content (only valid for files).
   * @return The file's content.
   * @throws InvalidOperationException     If the path is a folder and {@code compressed} is {@code false}.
   *                                       Or if the path is invalid when the output is compressed.
   * @throws NotFoundException             If the specified file does not exist.
   * @throws ResourceAccessDeniedException If the current user cannot access the content of the specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getFileContent(String, boolean)
   */
  @NonNull
  IGenericFileContent getFileContent( @NonNull GenericFilePath path, boolean compressed )
    throws OperationFailedException;

  /**
   * Gets a file given its path.
   *
   * @param path    The path of the file.
   * @param options The operation options.
   * @return The file.
   * @throws NotFoundException             If the specified file does not exist.
   * @throws ResourceAccessDeniedException If the current user cannot access the specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getFile(GenericFilePath, GetFileOptions)
   */
  @NonNull
  IGenericFile getFile( @NonNull GenericFilePath path, @NonNull GetFileOptions options )
    throws OperationFailedException;

  /**
   * Gets a list of deleted files which are still available in the trash folder.
   *
   * @return The list of deleted files.
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getDeletedFiles()
   */
  @NonNull
  default List<IGenericFile> getDeletedFiles() throws OperationFailedException {
    return Collections.emptyList();
  }

  /**
   * Permanently deletes a file, given its path.
   *
   * @param path The file path to be permanently deleted. This path must refer to an item in the trash (deleted).
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws InvalidPathException     If the specified path is not valid.
   * @throws NotFoundException        If the specified path does not exist, or does not refer to an item in the trash
   *                                  (deleted), or the current user is not allowed to access it.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#deleteFilePermanently(GenericFilePath)
   */
  void deleteFilePermanently( @NonNull GenericFilePath path ) throws OperationFailedException;

  /**
   * Deletes a file, given its path, by sending it to the trash or permanently deleting it, depending on the value of
   * the {@code permanent} variable.
   *
   * @param path      The file path to be deleted. This path must not refer to an item in the trash (deleted).
   * @param permanent If {@code true}, the file is permanently deleted; if {@code false}, the file is sent to the trash.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws NotFoundException             If the specified path does not exist, or does refer to an item in the trash
   *                                       (deleted).
   * @throws ResourceAccessDeniedException If the current user cannot access the specified path.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#deleteFile(GenericFilePath, boolean)
   */
  void deleteFile( @NonNull GenericFilePath path, boolean permanent ) throws OperationFailedException;

  /**
   * Restores a file, given its path.
   *
   * @param path The file path to be restored. This path must refer to an item in the trash (deleted).
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws InvalidPathException     If the specified path is not valid.
   * @throws NotFoundException        If the specified path does not exist, or does not refer to an item in the
   *                                  trash (deleted), or the current user is not allowed to access it.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#restoreFile(GenericFilePath)
   */
  void restoreFile( @NonNull GenericFilePath path ) throws OperationFailedException;

  /**
   * Renames a file or folder, given its path and the new name. If it's a file, this method does not change its
   * extension.
   *
   * @param path    The path of the file or folder to be renamed. This path must not refer to an item in the trash
   *                (deleted).
   * @param newName The new name of the file or folder. If it's a file, the new name must not have its extension.
   *                This name must not be empty, and must not contain any control characters.
   * @return {@code true} if the file or folder was renamed, {@code false} otherwise.
   * @throws ResourceAccessDeniedException If the current user cannot access the content of the specified file or
   *                                       folder.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws InvalidPathException          If the new path is not valid.
   * @throws InvalidOperationException     If the {@code newName} is not valid.
   * @throws NotFoundException             If the specified path does not exist, or does refer to an item in the
   *                                       trash (deleted).
   * @throws ConflictException             If the file or folder with the new name already exists.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#renameFile(GenericFilePath, String)
   */
  boolean renameFile( @NonNull GenericFilePath path, @NonNull String newName ) throws OperationFailedException;

  /**
   * Copies a file or folder from a given path to a destination folder.
   *
   * @param path              The path of the file or folder to be copied. This path must not refer to an item in the
   *                          trash (deleted).
   * @param destinationFolder The path of the destination folder. This path must not refer to a folder in the trash
   *                          (deleted).
   * @throws ResourceAccessDeniedException If the current user cannot access the content of either specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws InvalidPathException          If the destination path is not valid.
   * @throws NotFoundException             If either path does not exist or does refer to an item in the trash
   *                                       (deleted).
   * @throws ConflictException             If the file or folder to be copied already exists on the destination folder.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#copyFile(GenericFilePath, GenericFilePath)
   */
  void copyFile( @NonNull GenericFilePath path, @NonNull GenericFilePath destinationFolder )
    throws OperationFailedException;

  /**
   * Moves a file or folder from a given path to a destination folder.
   *
   * @param path              The path of the file or folder to be moved. This path must not refer to an item in the
   *                          trash (deleted).
   * @param destinationFolder The path of the destination folder. This path must not refer to a folder in the trash
   *                          (deleted).
   * @throws ResourceAccessDeniedException If the current user cannot access the content of either specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws InvalidPathException          If the destination path is not valid.
   * @throws NotFoundException             If either path does not exist or does refer to an item in the trash
   *                                       (deleted).
   * @throws ConflictException             If the file or folder to be moved already exists on the destination folder.
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#moveFile(GenericFilePath, GenericFilePath)
   */
  void moveFile( @NonNull GenericFilePath path, @NonNull GenericFilePath destinationFolder )
    throws OperationFailedException;

  /**
   * Gets the file metadata, given its path.
   *
   * @param path The file path to get the metadata from. This path must not refer to an item in the trash (deleted).
   * @return The file metadata.
   * @throws ResourceAccessDeniedException If the current user cannot access the specified path.
   * @throws AccessControlException        If the current user cannot perform this operation.
   * @throws NotFoundException             If the specified path does not exist, or does refer to an item in the trash
   *                                       (deleted).
   * @throws OperationFailedException      If the operation fails for some other (checked) reason.
   * @see IGenericFileService#getFileMetadata(GenericFilePath)
   */
  @NonNull
  IGenericFileMetadata getFileMetadata( @NonNull GenericFilePath path ) throws OperationFailedException;

  /**
   * Sets the file metadata, given its path and the metadata to set.
   *
   * @param path     The file path to set the metadata for. This path must not refer to an item in the trash (deleted).
   * @param metadata The metadata to set. If empty, all existing metadata is removed.
   * @throws AccessControlException   If the current user cannot perform this operation.
   * @throws NotFoundException        If the specified path does not exist, or does refer to an item in the trash
   *                                  (deleted), or the current user is not allowed to access it.
   * @throws OperationFailedException If the operation fails for some other (checked) reason.
   * @see IGenericFileService#setFileMetadata(GenericFilePath, IGenericFileMetadata)
   */
  void setFileMetadata( @NonNull GenericFilePath path, @NonNull IGenericFileMetadata metadata )
    throws OperationFailedException;
}

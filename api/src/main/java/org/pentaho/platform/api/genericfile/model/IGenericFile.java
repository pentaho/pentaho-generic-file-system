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

import java.util.Date;
import java.util.List;

/**
 * The {@code IGenericFile} interface contains basic information about a generic file.
 * <p>
 * To know whether a generic file is a file proper, or a folder, use {@link #isFolder()} or {@link #getType()}.
 * Folder generic file instances do not directly contain their children. Children of a folder generic file are
 * represented as part of a {@link IGenericFileTree} instance.
 * <p>
 * To know the name or the path of a file, use {@link #getName()} and {@link #getPath()}, respectively.
 */
@SuppressWarnings( "unused" )
public interface IGenericFile extends IProviderable {
  /**
   * The {@link #getType() type} value for a folder generic file.
   */
  String TYPE_FOLDER = "folder";

  /**
   * The {@link #getType() type} value for a file proper generic file.
   */
  String TYPE_FILE = "file";

  /**
   * Gets the physical name of the file.
   * <p>
   * Generally, the physical name of a file is the last segment of its {@link #getPath() path}.
   * <p>
   * A valid generic file instance must not have a {@code null} name.
   *
   * @see #getNameDecoded()
   * @see #getTitle()
   */
  String getName();

  /**
   * Sets the physical name of the file.
   * <p>
   * Generally, the physical name of a file is the last segment of its {@link #getPath() path}.
   * <p>
   * A valid generic file instance must not have a {@code null} name.
   *
   * @param name the physical name of the file; must not be {@code null}.
   * @see #getName()
   */
  void setName( String name );

  /**
   * Gets the physical, non-encoded name of the file.
   * <p>
   * The same as {@link #getName()} but without any encoding.
   * <p>
   * The default implementation simply returns {@link #getName()}.
   */
  default String getNameDecoded() {
    return getName();
  }

  /**
   * Gets the path of the file, as a string.
   * <p>
   * A valid generic file instance must not have a {@code null} path.
   *
   * @see #getName()
   * @see org.pentaho.platform.api.genericfile.GenericFilePath
   */
  String getPath();

  /**
   * Sets the path of the file, as a string.
   * <p>
   * A valid generic file instance must not have a {@code null} path.
   *
   * @param path the path of the file; must not be {@code null}.
   * @see #getName()
   * @see org.pentaho.platform.api.genericfile.GenericFilePath
   */
  void setPath( String path );

  /**
   * Gets the path of the parent folder, as a string, if any.
   * <p>
   * The provider root folders do not have a parent.
   * Otherwise, a valid generic file instance must not have a {@code null} parent path.
   *
   * @see #getPath()
   */
  String getParentPath();

  /**
   * Sets the path of the parent folder, as a string, if any.
   * <p>
   * The provider root folders do not have a parent.
   * Otherwise, a valid generic file instance must not have a {@code null} parent path.
   *
   * @param parentPath the path of the parent folder; may be {@code null} for provider root folders.
   * @see #getPath()
   */
  void setParentPath( String parentPath );

  /**
   * Gets the type of generic file, one of: {@link #TYPE_FOLDER} or {@link #TYPE_FILE}. For the
   * {@link IGenericFolder} the type is always {@link #TYPE_FOLDER}.
   *
   * @see #isFolder()
   */
  String getType();

  /**
   * Sets the type of generic file, one of: {@link #TYPE_FOLDER} or {@link #TYPE_FILE}. For the
   * {@link IGenericFolder} the type is always {@link #TYPE_FOLDER}.
   *
   * @param type the type of generic file; must be one of: {@link #TYPE_FOLDER} or {@link #TYPE_FILE}.
   * @see #isFolder()
   */
  void setType( String type );

  /**
   * Determines if a generic file is a folder.
   * <p>
   * The default implementation checks if the value of {@link #getType()} is equal to {@link #TYPE_FOLDER}.
   *
   * @return {@code true}, if the generic file is a folder; {@code false}, otherwise.
   */
  default boolean isFolder() {
    return TYPE_FOLDER.equals( getType() );
  }

  /**
   * Gets the modified date of the generic file.
   */
  Date getModifiedDate();

  /**
   * Sets the modified date of the generic file.
   *
   * @param modifiedDate the modified date to set.
   */
  void setModifiedDate( Date modifiedDate );

  /**
   * Gets whether the generic file can be edited.
   */
  boolean isCanEdit();

  /**
   * Sets whether the generic file can be edited.
   *
   * @param canEdit {@code true} if the generic file can be edited; {@code false} otherwise.
   */
  void setCanEdit( boolean canEdit );

  /**
   * Gets the original location of the generic file before it was deleted.
   * <p>
   * Each `IGenericFile` in the list corresponds to a folder in the path hierarchy
   * from the root to the original parent folder of the deleted file.
   * <p>
   * Note: Some folders in the original location may not exist anymore.
   */
  List<IGenericFile> getOriginalLocation();

  /**
   * Sets the original location of the generic file before it was deleted.
   * <p>
   * Each `IGenericFile` in the list corresponds to a folder in the path hierarchy
   * from the root to the original parent folder of the deleted file.
   * <p>
   * Note: Some folders in the original location may not exist anymore.
   *
   * @param originalLocation the original location to set.
   */
  void setOriginalLocation( List<IGenericFile> originalLocation );

  /**
   * Gets the deleted date of the generic file.
   */
  Date getDeletedDate();

  /**
   * Sets the deleted date of the generic file.
   *
   * @param deletedDate the deleted date to set.
   */
  void setDeletedDate( Date deletedDate );

  /**
   * Gets the user that deleted the generic file.
   */
  String getDeletedBy();

  /**
   * Sets the user that deleted the generic file.
   *
   * @param deletedBy the user that deleted the generic file.
   */
  void setDeletedBy( String deletedBy );

  /**
   * Gets whether the generic file can be deleted.
   */
  boolean isCanDelete();

  /**
   * Sets whether the generic file can be deleted.
   *
   * @param canDelete {@code true} if the generic file can be deleted; {@code false} otherwise.
   */
  void setCanDelete( boolean canDelete );

  /**
   * Gets the title of the file.
   * <p>
   * The title of a file is a localized, human-readable version of its {@link #getNameDecoded()} non-encoded name.
   * <p>
   * Unlike the name of a file, the title may not be unique amongst siblings.
   * <p>
   * When the title of a file is unspecified, the name of a file can be used in its place.
   *
   * @see #getName()
   * @see #getNameDecoded()
   * @see #getDescription()
   */
  String getTitle();

  /**
   * Sets the title of the file.
   * <p>
   * The title of a file is a localized, human-readable version of its {@link #getNameDecoded()} non-encoded name.
   * <p>
   * Unlike the name of a file, the title may not be unique amongst siblings.
   * <p>
   * When the title of a file is unspecified, the name of a file can be used in its place.
   *
   * @param title the title to set.
   * @see #getName()
   * @see #getNameDecoded()
   * @see #getDescription()
   */
  void setTitle( String title );

  /**
   * Gets the description of the file.
   * <p>
   * The description of a file is a localized, human-readable description of a file. Typically, displayed in a tooltip
   * in a user interface.
   *
   * @see #getName()
   * @see #getTitle()
   */
  String getDescription();

  /**
   * Sets the description of the file.
   * <p>
   * The description of a file is a localized, human-readable description of a file. Typically, displayed in a tooltip
   * in a user interface.
   *
   * @param description the description to set.
   * @see #getName()
   * @see #getTitle()
   */
  void setDescription( String description );

  /**
   * Gets the owner of the generic file.
   */
  String getOwner();

  /**
   * Sets the owner of the generic file.
   *
   * @param owner the owner to set.
   */
  void setOwner( String owner );

  /**
   * Gets the creation date of the generic file.
   */
  Date getCreatedDate();

  /**
   * Sets the creation date of the generic file.
   *
   * @param createdDate the creation date to set.
   */
  void setCreatedDate( Date createdDate );

  /**
   * Gets the user ID that created the generic file.
   */
  String getCreatorId();

  /**
   * Sets the user ID that created the generic file.
   *
   * @param creatorId the user ID to set.
   */
  void setCreatorId( String creatorId );

  /**
   * Gets the file size in bytes.
   */
  long getFileSize();

  /**
   * Sets the file size in bytes.
   *
   * @param fileSize the file size to set.
   */
  void setFileSize( long fileSize );

  /**
   * Gets the metadata of the generic file. The result maybe null for some implementations that don't support metadata.
   */
  IGenericFileMetadata getMetadata();

  /**
   * Sets the metadata of the generic file.
   *
   * @param metadata the metadata to set.
   */
  void setMetadata( IGenericFileMetadata metadata );
}

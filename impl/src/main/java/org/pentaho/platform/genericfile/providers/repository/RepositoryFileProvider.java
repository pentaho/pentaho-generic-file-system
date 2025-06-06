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


package org.pentaho.platform.genericfile.providers.repository;

import com.cronutils.utils.VisibleForTesting;
import com.google.gwt.thirdparty.guava.common.net.MediaType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GenericFilePermission;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.exception.AccessControlException;
import org.pentaho.platform.api.genericfile.exception.InvalidPathException;
import org.pentaho.platform.api.genericfile.exception.NotFoundException;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileContentWrapper;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.genericfile.BaseGenericFileProvider;
import org.pentaho.platform.genericfile.messages.Messages;
import org.pentaho.platform.genericfile.model.BaseGenericFileTree;
import org.pentaho.platform.genericfile.model.DefaultGenericFileContentWrapper;
import org.pentaho.platform.genericfile.providers.repository.model.RepositoryFile;
import org.pentaho.platform.genericfile.providers.repository.model.RepositoryFileTree;
import org.pentaho.platform.genericfile.providers.repository.model.RepositoryFolder;
import org.pentaho.platform.genericfile.providers.repository.model.RepositoryObject;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.ExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
import org.pentaho.platform.repository2.unified.webservices.DateAdapter;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.pentaho.platform.util.RepositoryPathEncoder.encodeRepositoryPath;

public class RepositoryFileProvider extends BaseGenericFileProvider<RepositoryFile> {
  public static final String ROOT_PATH = "/";
  public static final String FOLDER_NAME_TRASH = ".trash";

  // Ignore Sonar rule regarding field name convention. There's no way to mark the field as final due to lazy
  // initialization. Correctly using the name convention for constants.
  private static GenericFilePath ROOT_GENERIC_PATH;

  static {
    try {
      ROOT_GENERIC_PATH = GenericFilePath.parseRequired( ROOT_PATH );
    } catch ( InvalidPathException e ) {
      // Never happens.
    }
  }

  public static final String TYPE = "repository";

  @NonNull
  private final IUnifiedRepository unifiedRepository;

  /**
   * The file service wraps a unified repository and provides additional functionality.
   */
  @NonNull
  private final FileService fileService;

  @NonNull
  private final DateAdapter repositoryWsDateAdapter;

  // TODO: Actually fix the base FileService class to do this and eliminate this class when available on the platform.

  /**
   * Custom {@code FileService} class that ensures that the contained repository web service uses the specified unified
   * repository instance. The methods {@code getRepositoryFileInputStream} and {@code getRepositoryFileOutputStream}
   * also do not pass the correct repository instance forward.
   */
  private static class CustomFileService extends FileService {
    public CustomFileService( @NonNull IUnifiedRepository repository ) {
      this.repository = Objects.requireNonNull( repository );
    }

    @Override
    protected DefaultUnifiedRepositoryWebService getRepoWs() {
      if ( defaultUnifiedRepositoryWebService == null ) {
        defaultUnifiedRepositoryWebService = new DefaultUnifiedRepositoryWebService( repository );
      }

      return defaultUnifiedRepositoryWebService;
    }

    @Override
    public RepositoryFileOutputStream getRepositoryFileOutputStream( String path ) {
      return new RepositoryFileOutputStream( path, false, false, repository, false );
    }

    @Override
    public RepositoryFileInputStream getRepositoryFileInputStream(
      org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile ) throws FileNotFoundException {
      return new RepositoryFileInputStream( repositoryFile, repository );
    }
  }

  public RepositoryFileProvider() {
    this( PentahoSystem.get( IUnifiedRepository.class, PentahoSessionHolder.getSession() ) );
  }

  public RepositoryFileProvider( @NonNull IUnifiedRepository unifiedRepository ) {
    this( unifiedRepository, new CustomFileService( unifiedRepository ) );
  }

  public RepositoryFileProvider( @NonNull IUnifiedRepository unifiedRepository,
                                 @NonNull FileService fileService ) {
    this.unifiedRepository = Objects.requireNonNull( unifiedRepository );
    this.fileService = Objects.requireNonNull( fileService );
    this.repositoryWsDateAdapter = new DateAdapter();
  }

  @NonNull
  @Override
  public Class<RepositoryFile> getFileClass() {
    return RepositoryFile.class;
  }

  @NonNull
  @Override
  public String getName() {
    return Messages.getString( "GenericFileRepository.REPOSITORY_FOLDER_DISPLAY" );
  }

  @NonNull
  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  protected boolean createFolderCore( @NonNull GenericFilePath path ) throws OperationFailedException {
    // When parent path is not found, its creation is attempted.
    try {
      return fileService.doCreateDirSafe( encodeRepositoryPath( path.toString() ) );
    } catch ( UnifiedRepositoryAccessDeniedException e ) {
      throw new AccessControlException( e );
    } catch ( FileService.InvalidNameException e ) {
      throw new InvalidPathException();
    }
  }

  @NonNull
  @Override
  protected List<BaseGenericFileTree> getRootTreesCore( @NonNull GetTreeOptions options )
    throws OperationFailedException {
    // Ignore options.getBasePath()
    // Result already has a null parent path.
    return List.of( getTreeCore( ROOT_GENERIC_PATH, options ) );
  }

  @NonNull
  protected RepositoryFileTree getTreeCore( @NonNull GetTreeOptions options ) throws NotFoundException {
    return getTreeCore( options.getBasePath(), options );
  }

  @NonNull
  protected RepositoryFileTree getTreeCore( @Nullable GenericFilePath basePath, @NonNull GetTreeOptions options )
    throws NotFoundException {

    // Get the whole tree under the provider root (VFS connections)?
    if ( basePath == null ) {
      basePath = ROOT_GENERIC_PATH;
      assert basePath != null;
    } else if ( !owns( basePath ) ) {
      throw new NotFoundException( String.format( "Base path not found '%s'.", basePath ) );
    }

    String repositoryFilterString = getRepositoryFilter( options.getFilter() );

    // TODO: FileService has a bug for depth=0, where an NPE is thrown due to tree.getChildren() being null.
    // So, until that's fixed, must send depth = 1 and then cut children on this side.
    Integer maxDepth = options.getMaxDepth();
    boolean isZeroDepth = maxDepth != null && maxDepth == 0;
    if ( isZeroDepth ) {
      maxDepth = 1;
    }

    RepositoryFileTreeDto nativeTree = fileService.doGetTree(
      encodeRepositoryPath( basePath.toString() ),
      maxDepth,
      repositoryFilterString,
      options.isIncludeHidden(),
      false,
      false );

    if ( nativeTree == null ) {
      throw new NotFoundException( String.format( "Base path not found '%s'.", basePath ) );
    }

    if ( isZeroDepth ) {
      nativeTree.setChildren( null );
    }

    // The parent path of base path.
    String parentPathString = getParentPath( basePath );

    return convertFromNativeFileTreeDto( nativeTree, parentPathString );
  }

  @NonNull
  @Override
  public IGenericFileContentWrapper getFileContentWrapper( @NonNull GenericFilePath path )
    throws OperationFailedException {

    org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile = getNativeFile( path );

    try {
      RepositoryFileInputStream inputStream = fileService.getRepositoryFileInputStream( repositoryFile );

      String fileName = repositoryFile.getName();
      String mimeType = inputStream.getMimeType();

      return new DefaultGenericFileContentWrapper( inputStream, fileName, mimeType );
    } catch ( FileNotFoundException e ) {
      throw new NotFoundException( String.format( "Path not found '%s'.", path ), e );
    }
  }

  @NonNull
  @Override
  public IGenericFile getFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    return convertFromNativeFile( getNativeFile( path ), getParentPath( path ) );
  }

  protected org.pentaho.platform.api.repository2.unified.RepositoryFile getNativeFile( @NonNull GenericFilePath path )
    throws OperationFailedException {
    Objects.requireNonNull( path );

    org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile = null;

    if ( owns( path ) ) {
      repositoryFile = unifiedRepository.getFile( path.toString() );
    }

    if ( repositoryFile == null ) {
      throw new NotFoundException( String.format( "Path not found '%s'.", path ) );
    }

    return repositoryFile;
  }

  /**
   * Get the tree filter's corresponding repository filter
   *
   * @param treeFilter
   * @return
   */
  protected String getRepositoryFilter( GetTreeOptions.TreeFilter treeFilter ) {
    switch ( treeFilter ) {
      case FOLDERS:
        return "*|FOLDERS";
      case FILES:
        return "*|FILES";
      case ALL:
      default:
        return "*";
    }
  }

  @Override
  public boolean doesFolderExist( @NonNull GenericFilePath path ) {
    org.pentaho.platform.api.repository2.unified.RepositoryFile file = unifiedRepository.getFile( path.toString() );
    return file != null && file.isFolder();
  }

  // region Conversion
  @NonNull
  private RepositoryObject createRepositoryObject( String name,
                                                   String path,
                                                   String title,
                                                   boolean isFolder,
                                                   @Nullable String parentPath ) {
    RepositoryObject repositoryObject = isFolder ? new RepositoryFolder() : new RepositoryFile();

    boolean isRoot = parentPath == null;
    if ( isRoot ) {
      assert isFolder;

      RepositoryFolder folder = (RepositoryFolder) repositoryObject;
      // Must match the first segment as parsed by GenericFilePath#parse.
      folder.setName( path );
      folder.setTitle( Messages.getString( "GenericFileRepository.REPOSITORY_FOLDER_DISPLAY" ) );
      folder.setCanEdit( false );
      folder.setCanDelete( false );
      folder.setCanAddChildren( false );
    } else {
      repositoryObject.setName( name );
      repositoryObject.setTitle( title );
      repositoryObject.setCanEdit( true );
      repositoryObject.setCanDelete( true );
      if ( repositoryObject.isFolder() ) {
        assert repositoryObject instanceof RepositoryFolder;
        ( (RepositoryFolder) repositoryObject ).setCanAddChildren( true );
      }
    }

    repositoryObject.setPath( path );
    repositoryObject.setParentPath( parentPath );

    return repositoryObject;
  }

  /**
   * Must be kept in sync with
   * {@link #convertFromNativeFile(org.pentaho.platform.api.repository2.unified.RepositoryFile, String)}.
   */
  @NonNull
  private RepositoryObject convertFromNativeFileDto( @NonNull RepositoryFileDto nativeFile,
                                                     @Nullable String parentPath ) {

    RepositoryObject repositoryObject = createRepositoryObject(
      nativeFile.getName(), nativeFile.getPath(), nativeFile.getTitle(), nativeFile.isFolder(), parentPath );

    repositoryObject.setHidden( nativeFile.isHidden() );
    repositoryObject.setModifiedDate( getModifiedDateFromNativeFileDto( nativeFile ) );
    repositoryObject.setObjectId( nativeFile.getId() );
    repositoryObject.setDescription( nativeFile.getDescription() );
    repositoryObject.setOwner( getOwnerFromNativeFileDto( nativeFile ) );

    return repositoryObject;
  }

  private RepositoryObject convertFromNativeFileDto( @NonNull RepositoryFileDto nativeFile ) {
    return convertFromNativeFileDto( nativeFile, getParentPath( nativeFile ) );
  }

  @Nullable
  private Date parseDate( String date ) {
    try {
      if ( !StringUtil.isEmpty( date ) ) {
        return repositoryWsDateAdapter.unmarshal( date );
      }
    } catch ( Exception e ) {
      // noop
    }

    return null;
  }

  @Nullable
  private Date getModifiedDateFromNativeFileDto( @NonNull RepositoryFileDto nativeFile ) {
    Date lastModified = parseDate( nativeFile.getLastModifiedDate() );
    if ( lastModified != null ) {
      return lastModified;
    }

    return parseDate( nativeFile.getCreatedDate() );
  }

  private String getOwnerFromNativeFileDto( @NonNull RepositoryFileDto nativeFile ) {
    String owner = nativeFile.getOwner();
    if ( owner != null ) {
      return owner;
    }

    // Owner may not be available in the DTO depending on the service and/or parameters used.
    // So we need to fall back to the ACL to get the owner.
    return getOwnerByFileId( nativeFile.getId() );
  }

  @VisibleForTesting
  String getOwnerByFileId( String fileId ) {
    RepositoryFileAcl acl = unifiedRepository.getAcl( fileId );
    if ( acl != null ) {
      return acl.getOwner().getName();
    }

    return null;
  }

  @NonNull
  private RepositoryFileTree convertFromNativeFileTreeDto( @NonNull RepositoryFileTreeDto nativeTree,
                                                           @Nullable String parentPath ) {

    RepositoryObject repositoryObject = convertFromNativeFileDto( nativeTree.getFile(), parentPath );
    RepositoryFileTree repositoryTree = new RepositoryFileTree( repositoryObject );

    if ( nativeTree.getChildren() != null ) {
      // Ensure an empty list is reflected.
      repositoryTree.setChildren( new ArrayList<>() );

      String path = repositoryObject.getPath();

      for ( RepositoryFileTreeDto nativeChildTree : nativeTree.getChildren() ) {
        repositoryTree.addChild( convertFromNativeFileTreeDto( nativeChildTree, path ) );
      }
    }

    return repositoryTree;
  }

  /**
   * Must be kept in sync with {@link #convertFromNativeFileDto(RepositoryFileDto, String)}.
   */
  @NonNull
  private RepositoryObject convertFromNativeFile(
    @NonNull org.pentaho.platform.api.repository2.unified.RepositoryFile nativeFile, @Nullable String parentPath ) {

    RepositoryObject repositoryObject = createRepositoryObject(
      nativeFile.getName(), nativeFile.getPath(), nativeFile.getTitle(), nativeFile.isFolder(), parentPath );

    repositoryObject.setHidden( nativeFile.isHidden() );

    repositoryObject.setModifiedDate(
      nativeFile.getLastModifiedDate() != null ? nativeFile.getLastModifiedDate() : nativeFile.getCreatedDate() );

    if ( nativeFile.getId() != null ) {
      String id = nativeFile.getId().toString();

      repositoryObject.setObjectId( id );
      repositoryObject.setOwner( getOwnerByFileId( id ) );
    }

    repositoryObject.setDescription( nativeFile.getDescription() );

    return repositoryObject;
  }
  // endregion

  @Override
  public boolean owns( @NonNull GenericFilePath path ) {
    return path.getFirstSegment().equals( ROOT_PATH );
  }

  @Override
  public boolean hasAccess( @NonNull GenericFilePath path, @NonNull EnumSet<GenericFilePermission> permissions ) {
    return unifiedRepository.hasAccess( path.toString(), getRepositoryPermissions( permissions ) );
  }

  private EnumSet<RepositoryFilePermission> getRepositoryPermissions( EnumSet<GenericFilePermission> permissions ) {
    EnumSet<RepositoryFilePermission> repositoryFilePermissions = EnumSet.noneOf( RepositoryFilePermission.class );

    for ( GenericFilePermission permission : permissions ) {
      switch ( permission ) {
        case READ:
          repositoryFilePermissions.add( RepositoryFilePermission.READ );
          break;
        case WRITE:
          repositoryFilePermissions.add( RepositoryFilePermission.WRITE );
          break;
        case ALL:
          repositoryFilePermissions.add( RepositoryFilePermission.ALL );
          break;
        case DELETE:
          repositoryFilePermissions.add( RepositoryFilePermission.DELETE );
          break;
        case ACL_MANAGEMENT:
          repositoryFilePermissions.add( RepositoryFilePermission.ACL_MANAGEMENT );
          break;
      }
    }

    return repositoryFilePermissions;
  }

  @NonNull
  @Override
  public List<IGenericFile> getDeletedFiles() {
    return fileService.doGetDeletedFiles().stream()
      .map( fileDto -> {
        RepositoryObject repositoryObject = convertFromNativeFileDto( fileDto );

        repositoryObject.setOriginalLocation( getLocation( fileDto.getOriginalParentFolderPath() ) );
        repositoryObject.setDeletedBy( fileDto.getCreatorId() );
        repositoryObject.setDeletedDate( parseDate( fileDto.getDeletedDate() ) );

        return repositoryObject;
      } )
      .collect( Collectors.toList() );
  }

  @Override
  public void deleteFilePermanently( @NonNull GenericFilePath path ) throws OperationFailedException {
    String fileId = getTrashFileId( path );

    try {
      fileService.doDeleteFilesPermanent( fileId );
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  @Override
  public void deleteFile( @NonNull GenericFilePath path, boolean permanent ) throws OperationFailedException {
    try {
      String fileId = getFileId( path );

      if ( permanent ) {
        fileService.doDeleteFilesPermanent( fileId );
      } else {
        fileService.doDeleteFiles( fileId );
      }
    } catch ( FileNotFoundException e ) {
      throw new NotFoundException( String.format( "Path not found '%s'.", path ), e );
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  @Override
  public void restoreFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    String fileId = getTrashFileId( path );

    try {
      fileService.doRestoreFiles( fileId );
    } catch ( Exception | InternalError e ) {
      throw new OperationFailedException( e );
    }
  }

  @Override
  public void renameFile( @NonNull GenericFilePath path, @NonNull String newName ) throws OperationFailedException {
    try {
      if ( !fileService.isValidFileName( newName ) ) {
        throw new IllegalArgumentException( "Invalid name to rename the file: " + newName );
      }

      boolean success = fileService.doRename( getFileId( path ), newName );

      if ( !success ) {
        throw new OperationFailedException( String.format( "Failed to rename file '%s' to '%s'.", path, newName ) );
      }
    } catch ( OperationFailedException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  @Override
  public IGenericFile getProperties( @NonNull GenericFilePath path ) throws OperationFailedException {
    try {
      return convertFromNativeFileDto( fileService.doGetProperties( getFileId( path ) ) );
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  @Override
  public IGenericFile getRootProperties() throws OperationFailedException {
    try {
      return convertFromNativeFileDto( fileService.doGetRootProperties() );
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  public IGenericFileContentWrapper downloadFile( @NonNull GenericFilePath path ) throws OperationFailedException {
    if ( !SystemUtils.canDownload( path.toString() ) ) {
      throw new AccessControlException( "User is not authorized to perform this operation." );
    }

    if ( !fileService.isPathValid( path.toString() ) ) {
      throw new InvalidPathException();
    }

    org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile = getNativeFile( path );

    try {
      String fileName = repositoryFile.isFolder() ? repositoryFile.getName() + ".zip" : repositoryFile.getName();
      FileInputStream inputStream = getDownloadStream( repositoryFile );

      return new DefaultGenericFileContentWrapper( inputStream, fileName, MediaType.ZIP.type() );
    } catch ( Exception e ) {
      throw new OperationFailedException( e );
    }
  }

  protected BaseExportProcessor getDownloadExportProcessor(
    @NonNull org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile ) {
    BaseExportProcessor exportProcessor = repositoryFile.isFolder()
      ?
      new ZipExportProcessor( repositoryFile.getPath(), fileService.getRepository(), true )
      :
      new SimpleExportProcessor( repositoryFile.getPath(), fileService.getRepository() );

    exportProcessor.addExportHandler( getDownloadExportHandler() );

    return exportProcessor;
  }

  protected ExportHandler getDownloadExportHandler() {
    return PentahoSystem.get( DefaultExportHandler.class );
  }

  protected FileInputStream getDownloadStream(
    org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile ) throws ExportException, IOException {
    BaseExportProcessor exportProcessor = getDownloadExportProcessor( repositoryFile );
    File zipFile = exportProcessor.performExport( repositoryFile );

    return new FileInputStream( zipFile );
  }

  protected String getFileId( @NonNull GenericFilePath path ) throws FileNotFoundException {
    return fileService.doGetProperties( encodeRepositoryPath( path.toString() ) ).getId();
  }

  protected String getTrashFileId( @NonNull GenericFilePath path ) throws InvalidPathException, NotFoundException {
    boolean isTrash = false;
    List<String> segments = path.getSegments();

    for ( int i = 0; i < segments.size(); i++ ) {
      if ( FOLDER_NAME_TRASH.equals( segments.get( i ) ) && i + 1 < segments.size() ) {
        isTrash = true;
        String segment = segments.get( i + 1 );
        int colonIndex = segment.indexOf( ':' );

        if ( colonIndex != -1 ) {
          return segment.substring( colonIndex + 1 );
        }
      }
    }

    if ( isTrash ) {
      throw new InvalidPathException( "File ID not found in the path." );
    }

    throw new NotFoundException( "The path does not correspond to a deleted file." );
  }

  private String getParentPath( RepositoryFileDto fileDto ) {
    try {
      return getParentPath( GenericFilePath.parseRequired( fileDto.getPath() ) );
    } catch ( InvalidPathException e ) {
      // noop
    }

    return null;
  }

  private String getParentPath( GenericFilePath path ) {
    GenericFilePath parentPath = path.getParent();

    return parentPath != null ? parentPath.toString() : null;
  }

  private List<IGenericFile> getLocation( String path ) {
    GenericFilePath locationPath = null;

    try {
      locationPath = GenericFilePath.parseRequired( path );
    } catch ( InvalidPathException e ) {
      // noop
    }

    List<IGenericFile> location = new ArrayList<>();

    while ( locationPath != null ) {
      IGenericFile folder;

      try {
        folder = getFile( locationPath );
      } catch ( OperationFailedException e ) {
        // Location folder not found, most likely because it was deleted.
        String parentPath = getParentPath( locationPath );
        String name = locationPath.getLastSegment();

        folder = createRepositoryObject( name, locationPath.toString(), null, true, parentPath );
      }

      location.add( folder );
      locationPath = locationPath.getParent();
    }

    Collections.reverse( location );

    return location;
  }
}

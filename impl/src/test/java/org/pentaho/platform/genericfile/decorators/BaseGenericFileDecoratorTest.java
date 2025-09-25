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
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetFileOptions;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.exception.OperationFailedException;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseGenericFileDecoratorTest {
  private static class RecordingDecorator extends BaseGenericFileDecorator {
    int fileMetadataPathCalls;
    int fileMetadataFileCalls;
    int fileCoreCalls;
    int treeNodeCalls;

    boolean throwInFileMetadata;
    boolean throwInFile;
    boolean throwInTreeNodeBeforeSuper;

    @Override
    protected void decorateFileMetadataCore( @NonNull IGenericFileMetadata fileMetadata,
                                             @NonNull GenericFilePath path,
                                             @NonNull IGenericFileService service ) throws OperationFailedException {
      fileMetadataPathCalls++;

      if ( throwInFileMetadata ) {
        throw new OperationFailedException( "meta fail" );
      }
    }

    @Override
    protected void decorateFileMetadataCore( @NonNull IGenericFileMetadata fileMetadata,
                                             @NonNull IGenericFile file,
                                             @NonNull IGenericFileService service ) throws OperationFailedException {
      fileMetadataFileCalls++;

      super.decorateFileMetadataCore( fileMetadata, file, service );
    }

    @Override
    protected void decorateFileCore( @NonNull IGenericFile file,
                                     @NonNull IGenericFileService service,
                                     @NonNull GetFileOptions options ) throws OperationFailedException {
      fileCoreCalls++;

      if ( throwInFile ) {
        throw new OperationFailedException( "file fail" );
      }
    }

    @Override
    protected void decorateTreeNode( @NonNull IGenericFile file,
                                     @NonNull IGenericFileTree fileTree,
                                     @NonNull IGenericFileService service,
                                     @NonNull GetTreeOptions options ) throws OperationFailedException {
      treeNodeCalls++;

      if ( throwInTreeNodeBeforeSuper ) {
        throw new OperationFailedException( "tree node fail" );
      }

      super.decorateTreeNode( file, fileTree, service, options );
    }

    GetFileOptions exposeDerive( GetTreeOptions treeOptions ) {
      return deriveFileOptions( treeOptions );
    }
  }

  // region decorateFileMetadata
  @Test
  void testDecorateFileMetadataCallsCoreAndSwallowsException() {
    RecordingDecorator decorator = new RecordingDecorator();
    decorator.throwInFileMetadata = true;

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    GenericFilePath path = mock( GenericFilePath.class );
    IGenericFileService service = mock( IGenericFileService.class );

    assertDoesNotThrow( () -> decorator.decorateFileMetadata( metadata, path, service ) );
    assertEquals( 1, decorator.fileMetadataPathCalls );
  }

  @Test
  void testDecorateFileMetadataCoreNoOpDoesNotThrow() {
    BaseGenericFileDecorator decorator = new BaseGenericFileDecorator() {
      // no additional implementation needed for this test
    };
    IGenericFileMetadata fileMetadata = mock( IGenericFileMetadata.class );
    GenericFilePath path = mock( GenericFilePath.class );
    IGenericFileService service = mock( IGenericFileService.class );

    assertDoesNotThrow( () -> decorator.decorateFileMetadataCore( fileMetadata, path, service ) );
  }
  // endregion

  // region decorateFile
  @Test
  void testDecorateFileIncludesMetadataWhenFlagTrue() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    IGenericFile file = mock( IGenericFile.class );
    when( file.getMetadata() ).thenReturn( metadata );
    when( file.getPath() ).thenReturn( "/root/file.txt" );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();
    options.setIncludeMetadata( true );

    decorator.decorateFile( file, service, options );

    assertEquals( 1, decorator.fileMetadataFileCalls );
    assertEquals( 1, decorator.fileMetadataPathCalls );
    assertEquals( 1, decorator.fileCoreCalls );
  }

  @Test
  void testDecorateFileSkipsMetadataWhenFlagFalse() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    IGenericFile file = mock( IGenericFile.class );
    when( file.getMetadata() ).thenReturn( metadata );
    when( file.getPath() ).thenReturn( "/root/file.txt" );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();

    decorator.decorateFile( file, service, options );

    assertEquals( 0, decorator.fileMetadataFileCalls );
    assertEquals( 0, decorator.fileMetadataPathCalls );
    assertEquals( 1, decorator.fileCoreCalls );
  }

  @Test
  void testDecorateFileContinuesAfterMetadataException() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();
    decorator.throwInFileMetadata = true;

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    IGenericFile file = mock( IGenericFile.class );
    when( file.getMetadata() ).thenReturn( metadata );
    when( file.getPath() ).thenReturn( "/root/file.txt" );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();
    options.setIncludeMetadata( true );

    decorator.decorateFile( file, service, options );

    assertEquals( 1, decorator.fileMetadataFileCalls );
    assertEquals( 1, decorator.fileMetadataPathCalls );
    assertEquals( 1, decorator.fileCoreCalls, "file core should still execute" );
  }

  @Test
  void testDecorateFileSwallowsFileCoreException() {
    RecordingDecorator decorator = new RecordingDecorator();
    decorator.throwInFile = true;

    IGenericFile file = mock( IGenericFile.class );
    when( file.getPath() ).thenReturn( "/p" );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();

    assertDoesNotThrow( () -> decorator.decorateFile( file, service, options ) );
    assertEquals( 1, decorator.fileCoreCalls );
  }

  @Test
  void testDecorateFileCoreNoOpDoesNotThrow() {
    BaseGenericFileDecorator decorator = new BaseGenericFileDecorator() {
      // no additional implementation needed for this test
    };
    IGenericFile file = mock( IGenericFile.class );
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();

    assertDoesNotThrow( () -> decorator.decorateFileCore( file, service, options ) );
  }
  // endregion

  // region deriveFileOptions
  @Test
  void testDeriveFileOptionsCopiesIncludeMetadata() {
    RecordingDecorator decorator = new RecordingDecorator();
    GetTreeOptions treeOptions = new GetTreeOptions();
    treeOptions.setIncludeMetadata( true );

    GetFileOptions fileOptions = decorator.exposeDerive( treeOptions );
    assertTrue( fileOptions.isIncludeMetadata() );
  }
  // endregion

  // region decorateTree / decorateTreeNode
  @Test
  void testDecorateTreeRecursesAllNodes() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata metaRoot = mock( IGenericFileMetadata.class );
    IGenericFile rootFile = mock( IGenericFile.class );
    when( rootFile.getMetadata() ).thenReturn( metaRoot );
    when( rootFile.getPath() ).thenReturn( "/root" );

    IGenericFileTree childTree1 = mock( IGenericFileTree.class );
    IGenericFile childFile1 = mock( IGenericFile.class );
    when( childFile1.getPath() ).thenReturn( "/root/c1" );
    when( childTree1.getFile() ).thenReturn( childFile1 );

    IGenericFileTree childTree2 = mock( IGenericFileTree.class );
    IGenericFile childFile2 = mock( IGenericFile.class );
    when( childFile2.getPath() ).thenReturn( "/root/c2" );
    when( childTree2.getFile() ).thenReturn( childFile2 );

    IGenericFileTree rootTree = mock( IGenericFileTree.class );
    when( rootTree.getFile() ).thenReturn( rootFile );
    when( rootTree.getChildren() ).thenReturn( java.util.Arrays.asList( childTree1, childTree2 ) );

    IGenericFileService service = mock( IGenericFileService.class );
    GetTreeOptions options = new GetTreeOptions();

    decorator.decorateTree( rootTree, service, options );

    assertEquals( 3, decorator.treeNodeCalls, "root + 2 children" );
  }

  @Test
  void testDecorateTreeStopsOnException() {
    RecordingDecorator decorator = new RecordingDecorator();
    decorator.throwInTreeNodeBeforeSuper = true; // fail on root

    IGenericFile rootFile = mock( IGenericFile.class );
    when( rootFile.getPath() ).thenReturn( "/root" );
    IGenericFileTree child1 = mock( IGenericFileTree.class );
    IGenericFile childFile1 = mock( IGenericFile.class );
    when( childFile1.getPath() ).thenReturn( "/root/c1" );
    when( child1.getFile() ).thenReturn( childFile1 );

    IGenericFileTree rootTree = mock( IGenericFileTree.class );
    when( rootTree.getFile() ).thenReturn( rootFile );
    when( rootTree.getChildren() ).thenReturn( java.util.Collections.singletonList( child1 ) );

    IGenericFileService service = mock( IGenericFileService.class );
    GetTreeOptions options = new GetTreeOptions();

    assertDoesNotThrow( () -> decorator.decorateTree( rootTree, service, options ) );
    assertEquals( 1, decorator.treeNodeCalls, "recursion halted after exception" );
  }

  @Test
  void testDecorateTreeNodeDelegatesToDecorateFile() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    IGenericFile file = mock( IGenericFile.class );
    when( file.getMetadata() ).thenReturn( metadata );
    when( file.getPath() ).thenReturn( "/a" );
    IGenericFileTree tree = mock( IGenericFileTree.class );
    when( tree.getFile() ).thenReturn( file );

    IGenericFileService service = mock( IGenericFileService.class );
    GetTreeOptions options = new GetTreeOptions();
    options.setIncludeMetadata( true );

    decorator.decorateTreeNode( file, tree, service, options );

    assertEquals( 1, decorator.treeNodeCalls );
    assertEquals( 1, decorator.fileCoreCalls );
    assertEquals( 1, decorator.fileMetadataFileCalls );
  }
  // endregion

  // region overload metadata via file
  @Test
  void testDecorateFileMetadataOverloadFromFileUsesPathVariant() throws Exception {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata metadata = mock( IGenericFileMetadata.class );
    IGenericFile file = mock( IGenericFile.class );
    when( file.getPath() ).thenReturn( "/p" );

    // invoke protected overload indirectly via decorateFile with includeMetadata true
    IGenericFileService service = mock( IGenericFileService.class );
    GetFileOptions options = new GetFileOptions();
    options.setIncludeMetadata( true );
    when( file.getMetadata() ).thenReturn( metadata );

    decorator.decorateFile( file, service, options );

    assertEquals( 1, decorator.fileMetadataFileCalls );
    assertEquals( 1, decorator.fileMetadataPathCalls );
  }
  // endregion

  // region null parameter validation
  @Test
  @SuppressWarnings( "ConstantConditions" )
  void testPublicMethodsNullParameters() {
    RecordingDecorator decorator = new RecordingDecorator();

    IGenericFileMetadata md = mock( IGenericFileMetadata.class );
    GenericFilePath path = mock( GenericFilePath.class );
    IGenericFileService service = mock( IGenericFileService.class );
    IGenericFile file = mock( IGenericFile.class );
    GetFileOptions fo = new GetFileOptions();
    GetTreeOptions to = new GetTreeOptions();
    IGenericFileTree tree = mock( IGenericFileTree.class );

    assertThrows( NullPointerException.class, () -> decorator.decorateFileMetadata( null, path, service ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateFileMetadata( md, null, service ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateFileMetadata( md, path, null ) );

    assertThrows( NullPointerException.class, () -> decorator.decorateFile( null, service, fo ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateFile( file, null, fo ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateFile( file, service, null ) );

    assertThrows( NullPointerException.class, () -> decorator.decorateTree( null, service, to ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateTree( tree, null, to ) );
    assertThrows( NullPointerException.class, () -> decorator.decorateTree( tree, service, null ) );
  }
  // endregion
}

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pentaho.platform.api.genericfile.GenericFilePath;
import org.pentaho.platform.api.genericfile.GetFileOptions;
import org.pentaho.platform.api.genericfile.GetTreeOptions;
import org.pentaho.platform.api.genericfile.IGenericFileService;
import org.pentaho.platform.api.genericfile.model.IGenericFile;
import org.pentaho.platform.api.genericfile.model.IGenericFileMetadata;
import org.pentaho.platform.api.genericfile.model.IGenericFileTree;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class NullGenericFileDecoratorTest {
  private NullGenericFileDecorator decorator;
  private IGenericFileMetadata fileMetadataMock;
  private GenericFilePath pathMock;
  private IGenericFileService serviceMock;
  private IGenericFile fileMock;
  private GetFileOptions fileOptionsMock;
  private IGenericFileTree fileTreeMock;
  private GetTreeOptions treeOptionsMock;

  @BeforeEach
  void setUp() {
    decorator = new NullGenericFileDecorator();
    fileMetadataMock = mock( IGenericFileMetadata.class );
    pathMock = mock( GenericFilePath.class );
    serviceMock = mock( IGenericFileService.class );
    fileMock = mock( IGenericFile.class );
    fileOptionsMock = mock( GetFileOptions.class );
    fileTreeMock = mock( IGenericFileTree.class );
    treeOptionsMock = mock( GetTreeOptions.class );
  }

  @Test
  void testDecorateFileMetadataDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateFileMetadata( fileMetadataMock, pathMock, serviceMock ) );
  }

  @Test
  void testDecorateFileDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateFile( fileMock, serviceMock, fileOptionsMock ) );
  }

  @Test
  void testDecorateTreeDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateTree( fileTreeMock, serviceMock, treeOptionsMock ) );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateFileMetadataWithNullArgumentsDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateFileMetadata( null, null, null ) );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateFileWithNullArgumentsDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateFile( null, null, null ) );
  }

  @SuppressWarnings( "ConstantConditions" )
  @Test
  void testDecorateTreeWithNullArgumentsDoesNotThrow() {
    assertDoesNotThrow( () -> decorator.decorateTree( null, null, null ) );
  }
}

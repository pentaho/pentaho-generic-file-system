package org.pentaho.platform.genericfile.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseGenericFileMetadataTest {
  private BaseGenericFileMetadata metadata;

  @BeforeEach
  void setUp() {
    metadata = new BaseGenericFileMetadata();
  }

  @Test
  void testGetMetadata_returnsEmptyMapByDefault() {
    Map<String, String> result = metadata.getMetadata();

    assertNotNull( result );
    assertTrue( result.isEmpty() );
  }

  @Test
  void testGetMetadata_returnsNonNull() {
    Map<String, String> result = metadata.getMetadata();

    assertNotNull( result );
  }

  @Test
  void testSetMetadata_setsMetadataSuccessfully() {
    Map<String, String> testMetadata = new HashMap<>();
    testMetadata.put( "key1", "value1" );
    testMetadata.put( "key2", "value2" );

    metadata.setMetadata( testMetadata );

    assertEquals( testMetadata, metadata.getMetadata() );
    assertEquals( 2, metadata.getMetadata().size() );
    assertEquals( "value1", metadata.getMetadata().get( "key1" ) );
    assertEquals( "value2", metadata.getMetadata().get( "key2" ) );
  }

  @Test
  void testSetMetadata_replacesExistingMetadata() {
    Map<String, String> firstMetadata = new HashMap<>();
    firstMetadata.put( "old", "data" );
    metadata.setMetadata( firstMetadata );

    Map<String, String> newMetadata = new HashMap<>();
    newMetadata.put( "new", "data" );
    metadata.setMetadata( newMetadata );

    assertEquals( 1, metadata.getMetadata().size() );
    assertEquals( "data", metadata.getMetadata().get( "new" ) );
    assertNull( metadata.getMetadata().get( "old" ) );
  }

  @Test
  void testSetMetadata_throwsNullPointerException() {
    assertThrows( NullPointerException.class, () -> metadata.setMetadata( null ) );
  }

  @Test
  void testSetMetadata_acceptsEmptyMap() {
    Map<String, String> emptyMap = new HashMap<>();

    assertDoesNotThrow( () -> metadata.setMetadata( emptyMap ) );
    assertTrue( metadata.getMetadata().isEmpty() );
  }

  @Test
  void testGetMetadata_returnsSameInstanceAfterSet() {
    Map<String, String> testMetadata = new HashMap<>();
    testMetadata.put( "test", "value" );
    metadata.setMetadata( testMetadata );

    assertSame( testMetadata, metadata.getMetadata() );
  }

  @Test
  void testSetMetadata_withMultipleEntries() {
    Map<String, String> largeMetadata = new HashMap<>();
    for ( int i = 0; i < 100; i++ ) {
      largeMetadata.put( "key" + i, "value" + i );
    }

    metadata.setMetadata( largeMetadata );

    assertEquals( 100, metadata.getMetadata().size() );
    assertEquals( "value50", metadata.getMetadata().get( "key50" ) );
  }
}

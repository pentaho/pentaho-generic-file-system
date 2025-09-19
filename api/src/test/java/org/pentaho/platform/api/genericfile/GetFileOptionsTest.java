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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link GetFileOptions} class.
 */
class GetFileOptionsTest {
  /**
   * Tests the {@link GetFileOptions#GetFileOptions(GetFileOptions)} constructor.
   */
  @Nested
  class CopyConstructorTests {
    @Test
    void testCopiesAllProperties() {
      GetFileOptions options1 = new GetFileOptions();
      options1.setIncludeMetadata( true );

      GetFileOptions options2 = new GetFileOptions( options1 );
      assertEquals( options1.isIncludeMetadata(), options2.isIncludeMetadata() );
    }
  }

  /**
   * Tests the {@link GetFileOptions#isIncludeMetadata()} and {@link GetFileOptions#setIncludeMetadata(boolean)}
   * methods.
   */
  @Nested
  class IncludeMetadataTests {
    @Test
    void testDefaultsToFalse() {
      GetFileOptions options = new GetFileOptions();
      assertFalse( options.isIncludeMetadata() );
    }

    @Test
    void testAcceptsBeingSetToTrue() {
      GetFileOptions options = new GetFileOptions();
      options.setIncludeMetadata( true );

      assertTrue( options.isIncludeMetadata() );
    }

    @Test
    void testAcceptsBeingSetToFalse() {
      GetFileOptions options = new GetFileOptions();
      options.setIncludeMetadata( false );

      assertFalse( options.isIncludeMetadata() );
    }
  }

  /**
   * Tests the {@link GetFileOptions#equals(Object)} and {@link GetFileOptions#hashCode()} methods.
   */
  @Nested
  class EqualsTests {
    @Test
    void testEqualsItself() {
      GetFileOptions options = new GetFileOptions();
      assertEquals( options, options );
    }

    @Test
    void testDoesNotEqualNull() {
      GetFileOptions options = new GetFileOptions();
      assertNotEquals( null, options );
    }

    @Test
    void testEqualsAnotherBothWithAllNullProperties() {
      GetFileOptions options1 = new GetFileOptions();
      GetFileOptions options2 = new GetFileOptions();

      assertEquals( options1, options2 );
      assertEquals( options1.hashCode(), options2.hashCode() );
    }

    GetFileOptions createSampleGetFileOptions() {
      GetFileOptions options1 = new GetFileOptions();
      options1.setIncludeMetadata( true );

      return options1;
    }

    @Test
    void testEqualsAnotherWithAllEqualProperties() {
      GetFileOptions options1 = createSampleGetFileOptions();
      GetFileOptions options2 = createSampleGetFileOptions();

      assertEquals( options1, options2 );
      assertEquals( options1.hashCode(), options2.hashCode() );
    }

    @Test
    void testDoesNotEqualAnotherWithDifferentIncludeMetadata() {
      GetFileOptions options1 = createSampleGetFileOptions();
      GetFileOptions options2 = createSampleGetFileOptions();
      options1.setIncludeMetadata( true );
      options2.setIncludeMetadata( false );

      assertNotEquals( options1, options2 );
      assertNotEquals( options1.hashCode(), options2.hashCode() );
    }

    @Test
    void testDoesNotEqualObjectsOfOtherClasses() {
      GetFileOptions options = new GetFileOptions();
      Object other = new Object();

      assertNotEquals( options, other );
    }
  }
}

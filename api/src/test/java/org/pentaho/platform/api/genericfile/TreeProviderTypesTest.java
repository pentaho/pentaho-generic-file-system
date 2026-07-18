/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.platform.api.genericfile;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the {@link TreeProviderTypes} class.
 */
class TreeProviderTypesTest {
  /**
   * Tests the {@link TreeProviderTypes#normalizeProvider(String)} method.
   */
  @Nested
  class NormalizeProviderTests {
    @ParameterizedTest
    @ValueSource( strings = { "all", "ALL", "All" } )
    void testNormalizesAllCaseVariants( String provider ) {
      assertEquals( TreeProviderTypes.ALL, TreeProviderTypes.normalizeProvider( provider ) );
    }

    @ParameterizedTest
    @ValueSource( strings = { "vfs", "VFS", "Vfs" } )
    void testNormalizesVfsCaseVariants( String provider ) {
      assertEquals( TreeProviderTypes.VFS, TreeProviderTypes.normalizeProvider( provider ) );
    }

    @ParameterizedTest
    @ValueSource( strings = { "repository", "REPOSITORY", "Repository" } )
    void testNormalizesRepositoryCaseVariants( String provider ) {
      assertEquals( TreeProviderTypes.REPOSITORY, TreeProviderTypes.normalizeProvider( provider ) );
    }

    @Test
    void testThrowsForUnknownProvider() {
      assertThrows( IllegalArgumentException.class, () -> TreeProviderTypes.normalizeProvider( "unknown" ) );
    }

    @Test
    void testThrowsForNullProvider() {
      assertThrows( IllegalArgumentException.class, () -> TreeProviderTypes.normalizeProvider( null ) );
    }
  }

  /**
   * Tests the {@link TreeProviderTypes#includesProviderType(List, String)} method.
   */
  @Nested
  class IncludesProviderTypeTests {
    @Test
    void testReturnsTrueForAllProvider() {
      List<String> providers = List.of( TreeProviderTypes.ALL );

      assertTrue( TreeProviderTypes.includesProviderType( providers, "vfs" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "repository" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "VFS" ) );
    }

    @Test
    void testReturnsTrueForAllProviderCaseInsensitive() {
      List<String> providers = List.of( "ALL" );

      assertTrue( TreeProviderTypes.includesProviderType( providers, "vfs" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "repository" ) );
    }

    @Test
    void testReturnsTrueForMatchingProvider() {
      List<String> providers = List.of( TreeProviderTypes.VFS );

      assertTrue( TreeProviderTypes.includesProviderType( providers, "vfs" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "VFS" ) );
    }

    @Test
    void testReturnsFalseForNonMatchingProvider() {
      List<String> providers = List.of( TreeProviderTypes.VFS );

      assertFalse( TreeProviderTypes.includesProviderType( providers, "repository" ) );
    }

    @Test
    void testReturnsFalseForNullProviderType() {
      List<String> providers = List.of( TreeProviderTypes.ALL );

      assertFalse( TreeProviderTypes.includesProviderType( providers, null ) );
    }

    @Test
    void testReturnsFalseForNullProviders() {
      assertFalse( TreeProviderTypes.includesProviderType( null, "vfs" ) );
    }

    @Test
    void testReturnsFalseForEmptyProviders() {
      List<String> providers = List.of();

      assertFalse( TreeProviderTypes.includesProviderType( providers, "vfs" ) );
    }

    @Test
    void testCaseInsensitiveMatching() {
      List<String> providers = List.of( TreeProviderTypes.VFS, TreeProviderTypes.REPOSITORY );

      assertTrue( TreeProviderTypes.includesProviderType( providers, "vfs" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "VFS" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "repository" ) );
      assertTrue( TreeProviderTypes.includesProviderType( providers, "REPOSITORY" ) );
      assertFalse( TreeProviderTypes.includesProviderType( providers, "all" ) );
    }
  }

  /**
   * Tests the {@link TreeProviderTypes#includesAllProviders(List)} method.
   */
  @Nested
  class IncludesAllProvidersTests {
    @Test
    void testReturnsTrueForAllProvider() {
      List<String> providers = List.of( TreeProviderTypes.ALL );

      assertTrue( TreeProviderTypes.includesAllProviders( providers ) );
    }

    @Test
    void testReturnsTrueForAllProviderCaseInsensitive() {
      List<String> providers = List.of( "ALL" );

      assertTrue( TreeProviderTypes.includesAllProviders( providers ) );
    }

    @Test
    void testReturnsFalseForSpecificProviders() {
      List<String> providers = List.of( TreeProviderTypes.VFS );

      assertFalse( TreeProviderTypes.includesAllProviders( providers ) );
    }

    @Test
    void testReturnsFalseForMultipleSpecificProviders() {
      List<String> providers = List.of( TreeProviderTypes.VFS, TreeProviderTypes.REPOSITORY );

      assertFalse( TreeProviderTypes.includesAllProviders( providers ) );
    }

    @Test
    void testReturnsFalseForNullProviders() {
      assertFalse( TreeProviderTypes.includesAllProviders( null ) );
    }

    @Test
    void testReturnsFalseForEmptyProviders() {
      List<String> providers = List.of();

      assertFalse( TreeProviderTypes.includesAllProviders( providers ) );
    }
  }
}

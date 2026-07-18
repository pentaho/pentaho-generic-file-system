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

/**
 * Provider names accepted by tree endpoints and {@link GetTreeOptions}.
 */
public final class TreeProviderTypes {
  private TreeProviderTypes() {
  }

  public static final String ALL = "all";
  public static final String VFS = "vfs";
  public static final String REPOSITORY = "repository";

  public static final List<String> VALID_PROVIDERS = List.of( ALL, VFS, REPOSITORY );

  /**
   * Normalizes a provider string to its canonical value.
   * <p>
   * Performs a case-insensitive match against known provider types and returns the canonical form.
   *
   * @param provider The provider string to normalize
   * @return The canonical provider string
   * @throws IllegalArgumentException if the provider is unknown
   */
  public static String normalizeProvider( String provider ) {
    if ( provider == null ) {
      throw new IllegalArgumentException( "Unknown provider 'null'." );
    }

    return VALID_PROVIDERS.stream()
      .filter( validProvider -> validProvider.equalsIgnoreCase( provider ) )
      .findFirst()
      .orElseThrow( () -> new IllegalArgumentException( String.format( "Unknown provider '%s'.", provider ) ) );
  }

  /**
   * Checks if the given provider list includes the specified provider type.
   * <p>
   * If the provider list contains {@link #ALL}, returns true for any provider type.
   * Otherwise, performs a case-insensitive match against the provider list.
   *
   * @param providers The list of providers to check
   * @param providerType The provider type to search for
   * @return True if the provider type is included; false otherwise
   */
  public static boolean includesProviderType( List<String> providers, String providerType ) {
    if ( providerType == null || providers == null || providers.isEmpty() ) {
      return false;
    }

    if ( includesAllProviders( providers ) ) {
      return true;
    }

    return providers.stream().anyMatch( provider -> provider != null && provider.equalsIgnoreCase( providerType ) );
  }

  /**
   * Checks if the given provider list includes all providers.
   *
   * @param providers The list of providers to check
   * @return True if the provider list contains {@link #ALL}; false otherwise
   */
  public static boolean includesAllProviders( List<String> providers ) {
    return providers != null && providers.stream().anyMatch( ALL::equalsIgnoreCase );
  }
}

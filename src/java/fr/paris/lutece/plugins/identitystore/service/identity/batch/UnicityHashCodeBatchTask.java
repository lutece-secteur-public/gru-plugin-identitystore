/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.service.identity.batch;

import fr.paris.lutece.plugins.identitystore.service.identity.IdentityQualityService;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.sql.DAOUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Task that batch-initializes the unicity_hash_code column
 * for all non-deleted, non-merged identities.
 * <br>
 * Follows the same pattern as FullIndexTask: status object with logs,
 * exposed via a REST endpoint for UI polling.
 */
public class UnicityHashCodeBatchTask
{
    // SQL
    private static final String SQL_COUNT_IDENTITIES = "SELECT COUNT(*) FROM identitystore_identity WHERE is_deleted = 0 AND is_merged = 0";
    private static final String SQL_SELECT_IDENTITIES_FIRST_PAGE = "SELECT id_identity FROM identitystore_identity WHERE is_deleted = 0 AND is_merged = 0 ORDER BY id_identity ASC LIMIT ?";
    private static final String SQL_SELECT_IDENTITIES_NEXT_PAGE = "SELECT id_identity FROM identitystore_identity WHERE is_deleted = 0 AND is_merged = 0 AND id_identity > ? ORDER BY id_identity ASC LIMIT ?";
    private static final String SQL_SELECT_ATTRIBUTES_FOR_IDENTITY = "SELECT c.key_name, b.attribute_value FROM identitystore_identity_attribute b JOIN identitystore_ref_attribute c ON b.id_attribute = c.id_attribute WHERE b.id_identity = ?";
    private static final String SQL_SELECT_ATTRIBUTES_BULK_PREFIX = "SELECT b.id_identity, c.key_name, b.attribute_value FROM identitystore_identity_attribute b JOIN identitystore_ref_attribute c ON b.id_attribute = c.id_attribute WHERE b.id_identity IN (";
    private static final String SQL_SELECT_ATTRIBUTES_BULK_SUFFIX = ") ORDER BY b.id_identity";
    private static final String SQL_UPDATE_UNICITY_HASH = "UPDATE identitystore_identity SET unicity_hash_code = ? WHERE id_identity = ?";
    private static final String SQL_SELECT_ORIGINAL_BY_HASH = "SELECT id_identity FROM identitystore_identity WHERE unicity_hash_code = ?";

    private static final int BATCH_PAGE_SIZE = 5000;

    private final Plugin plugin = PluginService.getPlugin( "identitystore" );
    private final SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
    private UnicityHashCodeBatchStatus _status;

    /**
     * Result of a hash update attempt.
     */
    private enum UpdateResult
    {
        SUCCESS, DUPLICATE, ERROR
    }

    public UnicityHashCodeBatchStatus getStatus( )
    {
        return _status;
    }

    /**
     * Initializes status and starts the batch.
     */
    public void doJob( )
    {
        _status = new UnicityHashCodeBatchStatus( );
        _status.setRunning( true );
        _status.setStartTime( System.currentTimeMillis( ) );

        try
        {
            executeBatch( );
        }
        catch( final Exception e )
        {
            error( "Fatal error during batch execution: " + e.getMessage( ) );
        }
        finally
        {
            _status.setEndTime( System.currentTimeMillis( ) );
            _status.setRunning( false );
        }
    }

    private void executeBatch( )
    {
        final long startTime = System.currentTimeMillis( );

        // Map to accumulate duplicate groups: hash -> list of (id_identity, attributes, assigned UUID)
        final Map<String, DuplicateGroup> duplicateGroups = new LinkedHashMap<>( );

        // Step 1: count total identities to process
        try ( final DAOUtil daoCount = new DAOUtil( SQL_COUNT_IDENTITIES, plugin ) )
        {
            daoCount.executeQuery( );
            if ( daoCount.next( ) )
            {
                _status.setTotal( daoCount.getLong( 1 ) );
            }
        }

        this.info( "Starting batch for " + _status.getTotal( ) + " identities at " + sdf.format( new Date( startTime ) ) );

        int lastId = 0;

        while ( _status.isRunning( ) )
        {
            // Step 2: read a page of identity IDs using keyset pagination
            final List<Integer> identityIds = new ArrayList<>( BATCH_PAGE_SIZE );
            final boolean isFirstPage = ( lastId == 0 );
            final String sql = isFirstPage ? SQL_SELECT_IDENTITIES_FIRST_PAGE : SQL_SELECT_IDENTITIES_NEXT_PAGE;

            try ( final DAOUtil daoSelect = new DAOUtil( sql, plugin ) )
            {
                if ( isFirstPage )
                {
                    daoSelect.setInt( 1, BATCH_PAGE_SIZE );
                }
                else
                {
                    daoSelect.setInt( 1, lastId );
                    daoSelect.setInt( 2, BATCH_PAGE_SIZE );
                }
                daoSelect.executeQuery( );

                while ( daoSelect.next( ) )
                {
                    identityIds.add( daoSelect.getInt( 1 ) );
                }
            }

            if ( identityIds.isEmpty( ) )
            {
                break;
            }

            lastId = identityIds.get( identityIds.size( ) - 1 );

            // Step 3: bulk-load all attributes for this page in a single query
            final Map<Integer, Map<String, String>> allAttributes = loadAttributeMapBulk( identityIds, plugin );

            // Step 4: compute all hashes first
            final Map<Integer, String> hashByIdentity = new LinkedHashMap<>( );
            for ( final int identityId : identityIds )
            {
                try
                {
                    final Map<String, String> attributes = allAttributes.getOrDefault( identityId, Collections.emptyMap( ) );

                    if ( attributes.isEmpty( ) )
                    {
                        info( "Identity " + identityId + " has no attributes, skipping" );
                        _status.incrementSkipped( );
                        _status.incrementProcessed( );
                        continue;
                    }

                    final String hash = IdentityQualityService.instance( ).computeUnicityHashCode( attributes );
                    hashByIdentity.put( identityId, hash );
                }
                catch( final IdentityStoreException e )
                {
                    _status.incrementErrors( );
                    _status.incrementProcessed( );
                    error( "Identity " + identityId + ": " + e.getMessage( ) );
                }
                catch( final Exception e )
                {
                    _status.incrementErrors( );
                    _status.incrementProcessed( );
                    error( "Unexpected error on identity " + identityId + ": " + e.getMessage( ) );
                }
            }

            // Step 5: bulk update — try all at once, fall back to individual on failure
            if ( !hashByIdentity.isEmpty( ) )
            {
                final List<Integer> failedIds = tryBulkUpdateHash( hashByIdentity, plugin );

                // Count successful updates
                final int successCount = hashByIdentity.size( ) - failedIds.size( );
                for ( int i = 0; i < successCount; i++ )
                {
                    _status.incrementProcessed( );
                }

                // Handle failed (duplicate) updates individually
                for ( final int identityId : failedIds )
                {
                    final String hash = hashByIdentity.get( identityId );
                    final Map<String, String> attributes = allAttributes.getOrDefault( identityId, java.util.Collections.emptyMap( ) );

                    final UpdateResult result = tryUpdateHash( identityId, hash, plugin );
                    if ( result == UpdateResult.DUPLICATE )
                    {
                        final String fallbackUuid = UUID.randomUUID( ).toString( );

                        DuplicateGroup group = duplicateGroups.get( hash );
                        if ( group == null )
                        {
                            group = new DuplicateGroup( );
                            final int originalId = this.findOriginalIdentityByHash( hash, plugin );
                            if ( originalId > 0 )
                            {
                                group.originalId = originalId;
                                group.originalAttributes = this.loadAttributeMap( originalId, plugin );
                            }
                            duplicateGroups.put( hash, group );
                        }
                        group.duplicates.add( new DuplicateEntry( identityId, attributes, fallbackUuid ) );

                        this.info( "DUPLICATE: identity id=" + identityId + " hash=[" + hash + "] -> UUID [" + fallbackUuid + "]" );

                        final UpdateResult fallbackResult = tryUpdateHash( identityId, fallbackUuid, plugin );
                        if ( fallbackResult != UpdateResult.SUCCESS )
                        {
                            _status.incrementErrors( );
                        }
                        _status.incrementDuplicates( );
                    }
                    else if ( result == UpdateResult.ERROR )
                    {
                        _status.incrementErrors( );
                    }

                    _status.incrementProcessed( );
                }
            }

            if ( _status.getProcessed( ) % 10000 == 0 )
            {
                this.info( "Progress: " + _status.getProcessed( ) + "/" + _status.getTotal( ) + " processed, "
                        + _status.getDuplicates( ) + " duplicates, " + _status.getErrors( ) + " errors, " + _status.getSkipped( ) + " skipped" );
            }
        }

        final long endTime = System.currentTimeMillis( );
        final long durationMs = endTime - startTime;
        final long durationSec = durationMs / 1000;
        final long hours = durationSec / 3600;
        final long minutes = ( durationSec % 3600 ) / 60;
        final long seconds = durationSec % 60;
        final String duration = ( hours > 0 ? hours + "h " : "" ) + ( minutes > 0 ? minutes + "min " : "" ) + seconds + "s";

        this.info( "Batch completed at " + sdf.format( new java.util.Date( endTime ) ) );
        this.info( "Duration: " + duration + " (" + durationMs + "ms)" );
        this.info( "Result: " + _status.getProcessed( ) + "/" + _status.getTotal( ) + " processed, "
                + _status.getDuplicates( ) + " duplicates, " + _status.getErrors( ) + " errors, " + _status.getSkipped( ) + " skipped" );

        // Log grouped duplicate report
        if ( !duplicateGroups.isEmpty( ) )
        {
            this.logDuplicateReport( duplicateGroups );
        }
    }

    /**
     * Logs a grouped report of all duplicate identity groups found during the batch.
     */
    private void logDuplicateReport( final Map<String, DuplicateGroup> duplicateGroups )
    {
        this.info( "" );
        this.info( "═══════════════════════════════════════════════════════════════════" );
        this.info( "  DUPLICATE REPORT - " + duplicateGroups.size( ) + " groups, " + _status.getDuplicates( ) + " duplicate identities" );
        this.info( "═══════════════════════════════════════════════════════════════════" );

        int groupNum = 0;
        for ( final Map.Entry<String, DuplicateGroup> entry : duplicateGroups.entrySet( ) )
        {
            groupNum++;
            final String hash = entry.getKey( );
            final DuplicateGroup group = entry.getValue( );

            this.info( "" );
            this.info( "┌─ Group " + groupNum + "/" + duplicateGroups.size( )
                    + " - hash=[" + hash + "]"
                    + " - " + ( group.duplicates.size( ) + 1 ) + " identities" );

            if ( group.originalId > 0 )
            {
                this.info( "│  ★ ORIGINAL  id=" + group.originalId + "  " + formatAttributes( group.originalAttributes ) );
            }
            else
            {
                this.info( "│  ★ ORIGINAL  (not found)" );
            }

            for ( final DuplicateEntry dup : group.duplicates )
            {
                this.info( "│    DUPLICATE id=" + dup.identityId + "  " + formatAttributes( dup.attributes ) + "  → UUID [" + dup.assignedUuid + "]" );
            }

            this.info( "└──────────────────────────────────────────────────────────────" );
        }

        this.info( "" );
        this.info( "═══════════════════════════════════════════════════════════════════" );
        this.info( "  END OF DUPLICATE REPORT" );
        this.info( "═══════════════════════════════════════════════════════════════════" );
    }

    /**
     * Holds a group of duplicate identities sharing the same unicity hash.
     */
    private static class DuplicateGroup
    {
        int originalId = -1;
        Map<String, String> originalAttributes;
        final List<DuplicateEntry> duplicates = new ArrayList<>( );
    }

    /**
     * Holds info about a single duplicate identity.
     */
    private static class DuplicateEntry
    {
        final int identityId;
        final Map<String, String> attributes;
        final String assignedUuid;

        DuplicateEntry( final int identityId, final Map<String, String> attributes, final String assignedUuid )
        {
            this.identityId = identityId;
            this.attributes = attributes;
            this.assignedUuid = assignedUuid;
        }
    }

    /**
     * Tries to bulk-update the unicity_hash_code for a batch of identities using a single SQL statement.
     * <br>
     * If the bulk update fails (e.g., unique constraint violation), falls back to returning
     * all IDs so they can be retried individually.
     *
     * @param hashByIdentity map of id_identity to hash value
     * @param plugin         the current plugin
     * @return list of identity IDs that need individual retry (empty if all succeeded)
     */
    private List<Integer> tryBulkUpdateHash( final Map<Integer, String> hashByIdentity, final Plugin plugin )
    {
        final StringBuilder sql = new StringBuilder( "UPDATE identitystore_identity SET unicity_hash_code = CASE id_identity " );
        final List<Integer> ids = new ArrayList<>( hashByIdentity.keySet( ) );

        for ( final Map.Entry<Integer, String> entry : hashByIdentity.entrySet( ) )
        {
            sql.append( "WHEN " ).append( entry.getKey( ) ).append( " THEN '" ).append( entry.getValue( ) ).append( "' " );
        }
        sql.append( "END WHERE id_identity IN (" );
        for ( int i = 0; i < ids.size( ); i++ )
        {
            if ( i > 0 )
            {
                sql.append( "," );
            }
            sql.append( ids.get( i ) );
        }
        sql.append( ")" );

        try ( final DAOUtil daoUpdate = new DAOUtil( sql.toString( ), plugin ) )
        {
            daoUpdate.executeUpdate( );
            return java.util.Collections.emptyList( );
        }
        catch( final Exception e )
        {
            final String message = e.getMessage( ) != null ? e.getMessage( ).toLowerCase( ) : "";
            if ( message.contains( "unique" ) || message.contains( "duplicate" ) || message.contains( "23505" ) )
            {
                // Unique constraint violation — return all IDs for individual retry
                return ids;
            }
            this.error( "Bulk update failed: " + e.getMessage( ) );
            return ids;
        }
    }

    /**
     * Fall-back to try to update hash individually, so duplicates can be correctly detected.
     *
     * @param identityId the id of the updated identity
     * @param hash       the hash to update
     * @param plugin     the current plugin
     * @return the result of the update
     */
    private UpdateResult tryUpdateHash( final int identityId, final String hash, final Plugin plugin )
    {
        try ( final DAOUtil daoUpdate = new DAOUtil( SQL_UPDATE_UNICITY_HASH, plugin ) )
        {
            daoUpdate.setString( 1, hash );
            daoUpdate.setInt( 2, identityId );
            daoUpdate.executeUpdate( );
            return UpdateResult.SUCCESS;
        }
        catch( final Exception e )
        {
            final String message = e.getMessage( ) != null ? e.getMessage( ).toLowerCase( ) : "";
            if ( message.contains( "unique" ) || message.contains( "duplicate" ) || message.contains( "23505" ) )
            {
                return UpdateResult.DUPLICATE;
            }
            error( "Failed to update hash for identity " + identityId + ": " + e.getMessage( ) );
            return UpdateResult.ERROR;
        }
    }

    /**
     * Load identity attributes
     * @param identityId the id of the loaded identity
     * @param plugin     the current plugin
     * @return a map of key;value of attributes
     */
    private Map<String, String> loadAttributeMap( final int identityId, final Plugin plugin )
    {
        final Map<String, String> attributes = new HashMap<>( );

        try ( final DAOUtil daoAttr = new DAOUtil( SQL_SELECT_ATTRIBUTES_FOR_IDENTITY, plugin ) )
        {
            daoAttr.setInt( 1, identityId );
            daoAttr.executeQuery( );

            while ( daoAttr.next( ) )
            {
                attributes.put( daoAttr.getString( 1 ), daoAttr.getString( 2 ) );
            }
        }

        return attributes;
    }

    /**
     * Bulk-loads attributes for a list of identity IDs in a single SQL query.
     * Returns a map of id_identity -> (key_name -> attribute_value).
     *
     * @param identityIds list of identity IDs
     * @param plugin      the plugin
     * @return map of identity ID to attribute map
     */
    private Map<Integer, Map<String, String>> loadAttributeMapBulk( final List<Integer> identityIds, final Plugin plugin )
    {
        final Map<Integer, Map<String, String>> result = new HashMap<>( );

        if ( identityIds.isEmpty( ) )
        {
            return result;
        }

        // Build IN clause: (?, ?, ?, ...)
        final StringBuilder sqlBuilder = new StringBuilder( SQL_SELECT_ATTRIBUTES_BULK_PREFIX );
        for ( int i = 0; i < identityIds.size( ); i++ )
        {
            if ( i > 0 )
            {
                sqlBuilder.append( "," );
            }
            sqlBuilder.append( "?" );
        }
        sqlBuilder.append( SQL_SELECT_ATTRIBUTES_BULK_SUFFIX );

        try ( final DAOUtil daoAttr = new DAOUtil( sqlBuilder.toString( ), plugin ) )
        {
            for ( int i = 0; i < identityIds.size( ); i++ )
            {
                daoAttr.setInt( i + 1, identityIds.get( i ) );
            }
            daoAttr.executeQuery( );

            while ( daoAttr.next( ) )
            {
                final int idIdentity = daoAttr.getInt( 1 );
                final String keyName = daoAttr.getString( 2 );
                final String value = daoAttr.getString( 3 );

                result.computeIfAbsent( idIdentity, k -> new HashMap<>( ) ).put( keyName, value );
            }
        }

        return result;
    }

    /**
     * Finds the identity that currently holds the given unicity hash in database.
     *
     * @param hash   the unicity hash to search for
     * @param plugin the plugin
     * @return the id_identity of the original, or -1 if not found
     */
    private int findOriginalIdentityByHash( final String hash, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_SELECT_ORIGINAL_BY_HASH, plugin ) )
        {
            daoUtil.setString( 1, hash );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
        }
        return -1;
    }

    private static final String CODE_INSEE_FRANCE = AppPropertiesService.getProperty( "identitystore.code.insee.france", "99100" );

    /**
     * Formats an attribute map as a readable string for log display.
     * Pivot attributes are displayed first (marked with *), then the rest alphabetically.
     */
    private String formatAttributes( final Map<String, String> attributes )
    {
        if ( attributes == null || attributes.isEmpty( ) )
        {
            return "(no attributes)";
        }

        // Determine pivot keys using the same logic as computeUnicityHashCode
        final String birthplaceCode = attributes.get( Constants.PARAM_BIRTH_PLACE_CODE );
        final boolean isCountryWithBirthPlaceCode = birthplaceCode != null && Objects.equals( birthplaceCode, CODE_INSEE_FRANCE );
        final List<String> pivotKeys = IdentityAttributeService.instance( )
                .getPivotAttributeKeys( isCountryWithBirthPlaceCode ).stream( )
                .map( AttributeKey::getKeyName )
                .sorted( )
                .collect( Collectors.toList( ) );

        final StringBuilder sb = new StringBuilder( );

        // First: pivot attributes sorted alphabetically, marked with *
        for ( final String key : pivotKeys )
        {
            if ( attributes.containsKey( key ) )
            {
                if ( sb.length( ) > 0 )
                {
                    sb.append( " | " );
                }
                sb.append( "*" ).append( key ).append( "=" ).append( attributes.get( key ) );
            }
        }

        // Then: non-pivot attributes sorted alphabetically
        new TreeMap<>( attributes ).forEach( (key, value ) -> {
            if ( !pivotKeys.contains( key ) )
            {
                if ( sb.length( ) > 0 )
                {
                    sb.append( " | " );
                }
                sb.append( key ).append( "=" ).append( value );
            }
        } );

        return sb.toString( );
    }

    private void info( final String message )
    {
        AppLogService.info( "UnicityHashBatch - " + message );
        if ( _status != null )
        {
            _status.log( message );
        }
    }

    private void error( final String message )
    {
        AppLogService.error( "UnicityHashBatch - " + message );
        if ( _status != null )
        {
            _status.log( "[ERROR] " + message );
        }
    }
}
/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for SuspiciousIdentity objects
 */
public final class SuspiciousIdentityDAO implements ISuspiciousIdentityDAO
{
    // Constants
    private static final String SQL_QUERY_PURGE = "TRUNCATE TABLE identitystore_quality_suspicious_identity";
    private static final String SQL_QUERY_PURGE_BY_RULE = "DELETE FROM identitystore_quality_suspicious_identity WHERE id_duplicate_rule=?";
    private static final String SQL_QUERY_SELECT = "SELECT i.id_suspicious_identity, i.customer_id, i.duplicate_customer_id, i.id_duplicate_rule, r.code, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id LEFT JOIN identitystore_duplicate_rule r ON r.id_rule = i.id_duplicate_rule WHERE id_suspicious_identity = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_quality_suspicious_identity ( customer_id, duplicate_customer_id, id_duplicate_rule ) VALUES ( ?, ?, ?) ";
    private static final String SQL_QUERY_SELECT_ALL_LOCK = "SELECT customer_id, date_lock_end, author_type, author_name FROM identitystore_quality_suspicious_identity_lock WHERE is_locked = 1";
    private static final String SQL_QUERY_ADD_LOCK = "INSERT INTO identitystore_quality_suspicious_identity_lock ( customer_id, is_locked, date_lock_end, author_type, author_name ) VALUES ( ?, ?, ?, ?, ?) ";
    private static final String SQL_QUERY_REMOVE_LOCK = "DELETE FROM identitystore_quality_suspicious_identity_lock WHERE customer_id = ? ";
    private static final String SQL_QUERY_REMOVE_LOCK_WITH_ID_SUSPICIOUS = "DELETE FROM identitystore_quality_suspicious_identity_lock WHERE customer_id IN ( SELECT customer_id FROM identitystore_quality_suspicious_identity WHERE id_suspicious_identity = ? ) ";
    private static final String SQL_QUERY_PURGE_LOCKS = "DELETE FROM identitystore_quality_suspicious_identity_lock WHERE date_lock_end < NOW()";
    private static final String SQL_QUERY_CHECK_EXCLUDED = "SELECT COUNT(*) FROM identitystore_quality_suspicious_identity_excluded WHERE (first_customer_id = ? AND second_customer_id = ?) OR (first_customer_id = ? AND second_customer_id = ?)";
    private static final String SQL_QUERY_CHECK_LIST_EXCLUDED = "SELECT COUNT(*) FROM identitystore_quality_suspicious_identity_excluded WHERE ";
    private static final String SQL_QUERY_CHECK_SUSPICIOUS = "SELECT COUNT(*) FROM identitystore_quality_suspicious_identity WHERE customer_id IN ( {cuid_list} ) OR duplicate_customer_id IN ( {cuid_list} )";
    private static final String SQL_QUERY_CHECK_SUSPICIOUS_PAIR = "SELECT COUNT(*) FROM identitystore_quality_suspicious_identity WHERE id_duplicate_rule = ? AND ( (customer_id = ? AND duplicate_customer_id = ?) OR (customer_id = ? AND duplicate_customer_id = ?) )";
    private static final String SQL_QUERY_INSERT_EXCLUDED = "INSERT INTO identitystore_quality_suspicious_identity_excluded ( first_customer_id, second_customer_id, author_type, author_name, date_create ) VALUES ( ?, ?, ?, ?, NOW())";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_quality_suspicious_identity WHERE id_suspicious_identity = ? ";
    private static final String SQL_QUERY_DELETE_CUID = "DELETE FROM identitystore_quality_suspicious_identity WHERE customer_id = ? OR duplicate_customer_id = ?";
    private static final String SQL_QUERY_DELETE_CUID_EMPTY_DUPLICATE = "DELETE FROM identitystore_quality_suspicious_identity WHERE customer_id = ? AND duplicate_customer_id IS NULL";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_quality_suspicious_identity SET customer_id = ?, duplicate_customer_id = ? WHERE id_suspicious_identity = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT i.id_suspicious_identity, i.customer_id, i.duplicate_customer_id, i.id_duplicate_rule, d.code, i.date_create, h.metadata, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id LEFT JOIN identitystore_duplicate_rule d on d.id_rule = i.id_duplicate_rule LEFT JOIN identitystore_identity_history h on i.customer_id = h.customer_id AND i.date_create = h.modification_date ";
    private static final String SQL_JOIN_SELECTALL_ATTRIBUTE_FILTER = "LEFT JOIN identitystore_identity id ON id.customer_id = i.customer_id LEFT JOIN identitystore_identity_attribute a ON a.id_identity = id.id_identity LEFT JOIN identitystore_ref_attribute r ON r.id_attribute = a.id_attribute ";
    private static final String SQL_GROUPBY_HAVING_SELECTALL_ATTRIBUTE_FILTER = " GROUP BY i.id_suspicious_identity, d.code, l.date_lock_end, l.is_locked, l.author_type, l.author_name, h.id_history HAVING COUNT(i.id_suspicious_identity) = ${filter_count}";
    private static final String SQL_QUERY_SELECTALL_EXCLUDED = "SELECT first_customer_id, second_customer_id, date_create, author_type, author_name FROM identitystore_quality_suspicious_identity_excluded";
    private static final String SQL_QUERY_SELECTALL_EXCLUDED_BY_CUSTOMER_ID = "SELECT first_customer_id, second_customer_id, date_create, author_type, author_name FROM identitystore_quality_suspicious_identity_excluded WHERE first_customer_id = ? OR second_customer_id = ? ";
    private static final String SQL_QUERY_SELECTALL_CUIDS = "SELECT customer_id FROM identitystore_quality_suspicious_identity si JOIN identitystore_duplicate_rule dr ON dr.id_rule = si.id_duplicate_rule WHERE dr.code = ? ";
    private static final String SQL_QUERY_SELECTALL_DUPLICATE_CUIDS = "SELECT duplicate_customer_id FROM identitystore_quality_suspicious_identity si JOIN identitystore_duplicate_rule dr ON dr.id_rule = si.id_duplicate_rule WHERE dr.code = ? ";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_suspicious_identity FROM identitystore_quality_suspicious_identity";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT i.id_suspicious_identity, i.customer_id, i.duplicate_customer_id, i.id_duplicate_rule, r.code, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id  LEFT JOIN identitystore_duplicate_rule r ON r.id_rule = i.id_duplicate_rule WHERE id_suspicious_identity IN (  ";
    private static final String SQL_QUERY_SELECT_BY_CUSTOMER_IDs = "SELECT i.id_suspicious_identity, i.customer_id, i.duplicate_customer_id, i.date_create, i.id_duplicate_rule, r.code, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id LEFT JOIN identitystore_duplicate_rule r ON r.id_rule = i.id_duplicate_rule WHERE i.customer_id IN {cuid_list} OR i.duplicate_customer_id IN {cuid_list}";
    private static final String SQL_QUERY_SELECT_COUNT = "SELECT count(id_suspicious_identity) FROM identitystore_quality_suspicious_identity ";
    private static final String SQL_QUERY_SELECT_COUNT_BY_RULE_ID = SQL_QUERY_SELECT_COUNT + " WHERE id_duplicate_rule = ? ";
    private static final String SQL_QUERY_REMOVE_EXCLUDED_IDENTITIES = "DELETE FROM identitystore_quality_suspicious_identity_excluded WHERE first_customer_id = ? AND second_customer_id = ?";
    private static final String SQL_QUERY_REMOVE_EXCLUDED_IDENTITIES_ONE_CUID = "DELETE FROM identitystore_quality_suspicious_identity_excluded WHERE first_customer_id = ? OR second_customer_id = ?";

    private final ObjectMapper objectMapper = new ObjectMapper( );

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( SuspiciousIdentity suspiciousIdentity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, suspiciousIdentity.getCustomerId( ) );
            daoUtil.setString(  nIndex++, suspiciousIdentity.getDuplicateCuid() );
            daoUtil.setInt( nIndex,
                    suspiciousIdentity.getIdDuplicateRule( ) != null ? suspiciousIdentity.getIdDuplicateRule( ) : Constants.MANUAL_SUSPICIOUS_RULE_ID );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                suspiciousIdentity.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    @Override
    public void insertExcluded( String firstCuid, String secondCuid, String authorType, String authorName, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_EXCLUDED, plugin ) )
        {
            daoUtil.setString( 1, firstCuid );
            daoUtil.setString( 2, secondCuid );
            daoUtil.setString( 3, authorType );
            daoUtil.setString( 4, authorName );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<SuspiciousIdentity> load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            SuspiciousIdentity suspiciousIdentity = null;

            if ( daoUtil.next( ) )
            {
                suspiciousIdentity = new SuspiciousIdentity( );
                int nIndex = 1;

                suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setDuplicateCuid(  daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setDuplicateRuleCode( daoUtil.getString( nIndex++ ) );
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                suspiciousIdentity.setLock( lock );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex ) );
                }
                else
                {
                    lock.setLocked( false );
                }
            }
            return Optional.ofNullable( suspiciousIdentity );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SuspiciousIdentity> selectByCustomerIDs( List<String> customerIds, Plugin plugin )
    {
        final String sqlQuerySelectByCustomerIDs = SQL_QUERY_SELECT_BY_CUSTOMER_IDs.replace("{cuid_list}", " ( '" + String.join("', '", customerIds) + "' )");
        final List<SuspiciousIdentity> suspicions = new ArrayList<>( );
        try (final DAOUtil daoUtil = new DAOUtil(sqlQuerySelectByCustomerIDs, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next() )
            {
                final SuspiciousIdentity suspiciousIdentity = new SuspiciousIdentity( );
                suspicions.add(suspiciousIdentity);

                int nIndex = 1;

                suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setDuplicateCuid( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setDuplicateRuleCode( daoUtil.getString( nIndex++ ) );
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                suspiciousIdentity.setLock( lock );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex ) );
                }
                else
                {
                    lock.setLocked( false );
                }
                suspiciousIdentity.setLock( lock );
            }

            return suspicions;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeUpdate( );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LOCK_WITH_ID_SUSPICIOUS, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( String customerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_CUID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.setString( 2, customerId );
            daoUtil.executeUpdate( );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LOCK, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void delete( String customerId, boolean emptyDuplicateCuid, Plugin plugin ) {
        if(!emptyDuplicateCuid) {
            this.delete( customerId, plugin );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_CUID_EMPTY_DUPLICATE, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeUpdate( );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LOCK, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( SuspiciousIdentity suspiciousIdentity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setString( nIndex++, suspiciousIdentity.getCustomerId( ) );
            daoUtil.setString( nIndex++, suspiciousIdentity.getDuplicateCuid( ) );
            daoUtil.setInt( nIndex, suspiciousIdentity.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SuspiciousIdentity> selectSuspiciousIdentitysList( final String ruleCode, final int max, final Integer priority, Plugin plugin )
            throws IdentityStoreException
    {
        return selectSuspiciousIdentitysList( ruleCode, Collections.emptyList( ), max, priority, plugin );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SuspiciousIdentity> selectSuspiciousIdentitysList( final String ruleCode, final List<SearchAttribute> attributes, final Integer max,
            final Integer priority, Plugin plugin ) throws IdentityStoreException
    {
        final List<SuspiciousIdentity> suspiciousIdentityList = new ArrayList<>( );
        final StringBuilder query = new StringBuilder( SQL_QUERY_SELECTALL );
        if ( CollectionUtils.isNotEmpty( attributes ) )
        {
            query.append( SQL_JOIN_SELECTALL_ATTRIBUTE_FILTER );
        }
        query.append( " WHERE 1=1 " );

        if ( StringUtils.isNotEmpty( ruleCode ) )
        {
            query.append( "AND d.code = '" ).append( ruleCode ).append( "' " );
        }

        if ( priority != null )
        {
            query.append( "AND d.priority = '" ).append( priority ).append( "' " );
        }

        if ( CollectionUtils.isNotEmpty( attributes ) )
        {
            query.append( attributes.stream( )
                    .map( attr -> "(r.key_name = '" + attr.getKey( ) + "' AND LOWER(a.attribute_value) = '" + attr.getValue( ).toLowerCase( ) + "')" )
                    .collect( Collectors.joining( " OR ", "AND ( ", " ) " ) ) )
                    .append( SQL_GROUPBY_HAVING_SELECTALL_ATTRIBUTE_FILTER.replace( "${filter_count}", String.valueOf( attributes.size( ) ) ) );
        }

        if ( max != null && max != 0 )
        {
            query.append( " LIMIT " ).append( max );
        }

        query.append( " ORDER BY i.date_create DESC " );

        try ( final DAOUtil daoUtil = new DAOUtil( query.toString( ), plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final SuspiciousIdentity suspiciousIdentity = new SuspiciousIdentity( );
                int nIndex = 1;

                suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setDuplicateCuid( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setDuplicateRuleCode( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                final String jsonMap = daoUtil.getString( nIndex++ );
                if ( StringUtils.isNotEmpty( jsonMap ) )
                {
                    final Map<String, String> mapMetaData = objectMapper.readValue( jsonMap, new TypeReference<Map<String, String>>( )
                    {
                    } );
                    suspiciousIdentity.getMetadata( ).clear( );
                    if ( mapMetaData != null && !mapMetaData.isEmpty( ) )
                    {
                        suspiciousIdentity.getMetadata( ).putAll( mapMetaData );
                    }
                }
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                suspiciousIdentity.setLock( lock );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex ) );
                }
                else
                {
                    lock.setLocked( false );
                }
                suspiciousIdentityList.add( suspiciousIdentity );
            }

            return suspiciousIdentityList;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public List<ExcludedIdentities> selectExcludedIdentitiesList( Plugin plugin )
    {
        final List<ExcludedIdentities> excludedIdentitiesList = new ArrayList<>( );

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_EXCLUDED, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final ExcludedIdentities excludedIdentities = new ExcludedIdentities( );
                int nIndex = 1;

                excludedIdentities.setFirstCustomerId( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setSecondCustomerId( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setExclusionDate( daoUtil.getTimestamp( nIndex++ ) );
                excludedIdentities.setAuthorType( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setAuthorName( daoUtil.getString( nIndex ) );
                excludedIdentitiesList.add( excludedIdentities );
            }

            return excludedIdentitiesList;
        }
    }

    @Override
    public List<ExcludedIdentities> selectExcludedIdentitiesList( final String customerId, Plugin plugin )
    {
        final List<ExcludedIdentities> excludedIdentitiesList = new ArrayList<>( );

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_EXCLUDED_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.setString( 2, customerId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final ExcludedIdentities excludedIdentities = new ExcludedIdentities( );
                int nIndex = 1;

                excludedIdentities.setFirstCustomerId( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setSecondCustomerId( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setExclusionDate( daoUtil.getTimestamp( nIndex++ ) );
                excludedIdentities.setAuthorType( daoUtil.getString( nIndex++ ) );
                excludedIdentities.setAuthorName( daoUtil.getString( nIndex ) );
                excludedIdentitiesList.add( excludedIdentities );
            }

            return excludedIdentitiesList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdSuspiciousIdentitysList( Plugin plugin )
    {
        List<Integer> suspiciousIdentityList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                suspiciousIdentityList.add( daoUtil.getInt( 1 ) );
            }

            return suspiciousIdentityList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectSuspiciousIdentitysReferenceList( Plugin plugin )
    {
        ReferenceList suspiciousIdentityList = new ReferenceList( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                suspiciousIdentityList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return suspiciousIdentityList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SuspiciousIdentity> selectSuspiciousIdentitysListByIds( Plugin plugin, List<Integer> listIds )
    {
        final List<SuspiciousIdentity> suspiciousIdentityList = new ArrayList<>( );

        StringBuilder builder = new StringBuilder( );

        if ( !listIds.isEmpty( ) )
        {
            for ( int i = 0; i < listIds.size( ); i++ )
            {
                builder.append( "?," );
            }

            String placeHolders = builder.deleteCharAt( builder.length( ) - 1 ).toString( );
            String stmt = SQL_QUERY_SELECTALL_BY_IDS + placeHolders + ")";

            try ( DAOUtil daoUtil = new DAOUtil( stmt, plugin ) )
            {
                int index = 1;
                for ( Integer n : listIds )
                {
                    daoUtil.setInt( index++, n );
                }

                daoUtil.executeQuery( );
                while ( daoUtil.next( ) )
                {
                    final SuspiciousIdentity suspiciousIdentity = new SuspiciousIdentity( );
                    int nIndex = 1;

                    suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                    suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                    suspiciousIdentity.setDuplicateCuid( daoUtil.getString( nIndex++ ) );
                    suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                    suspiciousIdentity.setDuplicateRuleCode( daoUtil.getString( nIndex++ ) );

                    final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                    suspiciousIdentity.setLock( lock );
                    lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                    if ( lock.getLockEndDate( ) != null )
                    {
                        lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                        lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                        lock.setAuthorName( daoUtil.getString( nIndex ) );
                    }
                    else
                    {
                        lock.setLocked( false );
                    }

                    suspiciousIdentityList.add( suspiciousIdentity );
                }

                daoUtil.free( );

            }
        }
        return suspiciousIdentityList;

    }

    @Override
    public void purge( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_PURGE, plugin ) )
        {
            daoUtil.executeUpdate( );
        }
    }

   @Override
    public void purgeByRuleId( Integer ruleId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_PURGE_BY_RULE, plugin ) )
        {
            daoUtil.setInt(1, ruleId);
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int countSuspiciousIdentities( final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT, plugin ) )
        {
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public int countSuspiciousIdentities( final int ruleId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_BY_RULE_ID, plugin ) )
        {
            daoUtil.setInt( 1, ruleId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
        }
        return 0;
    }

    @Override
    public boolean checkIfExcluded( String firstCuid, String secondCuid, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_EXCLUDED, plugin ) )
        {
            daoUtil.setString( 1, firstCuid );
            daoUtil.setString( 2, secondCuid );
            daoUtil.setString( 3, secondCuid );
            daoUtil.setString( 4, firstCuid );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 ) > 0;
            }

            return false;
        }
    }

    @Override
    public boolean checkIfExcluded( String firstCuid, List<String> cuids, Plugin plugin )
    {
        if ( CollectionUtils.isEmpty( cuids ) )
        {
            return false;
        }
        final String critaeria = String.join( "', '", cuids );
        String query = SQL_QUERY_CHECK_LIST_EXCLUDED;
        query += "(first_customer_id = '" + firstCuid + "' AND second_customer_id IN ( '" + critaeria + "' )) OR (first_customer_id IN ('" + critaeria
                + "') AND second_customer_id = '" + firstCuid + "')";

        try ( final DAOUtil daoUtil = new DAOUtil( query, plugin ) )
        {
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 ) > 0;
            }

            return false;
        }
    }

    @Override
    public boolean checkIfContainsSuspicious( final List<String> customerIds, final Plugin plugin )
    {
        final String query = SQL_QUERY_CHECK_SUSPICIOUS.replace("{cuid_list}", customerIds.stream( ).map( s -> String.format( "'%s'", s ) ).collect( Collectors.joining( "," ) ));
        try ( final DAOUtil daoUtil = new DAOUtil( query, plugin ) )
        {
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 ) > 0;
            }

            return false;
        }
    }

    @Override
    public boolean checkIfExistsSuspicious( final String cuid1, final String cuid2, final int ruleId, final Plugin plugin ) {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_SUSPICIOUS_PAIR, plugin ) )
        {
            int index = 1;
            daoUtil.setInt(index++, ruleId);
            daoUtil.setString(index++, cuid1);
            daoUtil.setString(index++, cuid2);
            daoUtil.setString(index++, cuid2);
            daoUtil.setString( index++, cuid1);
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 ) > 0;
            }

            return false;
        }
    }

    @Override
    public boolean manageLock( String customerId, boolean lock, String authorType, String authorName, Plugin plugin )
    {
        if ( lock )
        {
            try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_ADD_LOCK, plugin ) )
            {
                daoUtil.setString( 1, customerId );
                daoUtil.setBoolean( 2, lock );
                daoUtil.setTimestamp( 3, Timestamp.from( Instant.now( ).plusSeconds( 1800 ) ) ); // TODO paramétrer la durée de vie des locks en prop
                daoUtil.setString( 4, authorType );
                daoUtil.setString( 5, authorName );
                daoUtil.executeUpdate( );
                return true;
            }
        }
        else
        {
            try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LOCK, plugin ) )
            {
                daoUtil.setString( 1, customerId );
                daoUtil.executeUpdate( );
                return false;
            }
        }
    }

    @Override
    public void purgeLocks( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_PURGE_LOCKS, plugin ) )
        {
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<SuspiciousIdentity> getAllLocks( Plugin plugin )
    {
        List<SuspiciousIdentity> suspiciousIdentities = new ArrayList<>();
        try( final DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_ALL_LOCK, plugin ) )
        {
            daoUtil.executeQuery( );
            while (daoUtil.next( ) )
            {
                final SuspiciousIdentity suspiciousIdentity = new SuspiciousIdentity( );
                int nIndex = 1;
                suspiciousIdentity.setCustomerId( daoUtil.getString(nIndex++) );
                SuspiciousIdentityLock suspiciousIdentityLock = new SuspiciousIdentityLock( );
                suspiciousIdentityLock.setLocked(true);
                suspiciousIdentityLock.setLockEndDate(daoUtil.getTimestamp( nIndex++ ));
                suspiciousIdentityLock.setAuthorType( daoUtil.getString(nIndex++) );
                suspiciousIdentityLock.setAuthorName( daoUtil.getString(nIndex) );
                suspiciousIdentity.setLock(suspiciousIdentityLock);

                suspiciousIdentities.add(suspiciousIdentity);
            }
        }
        return suspiciousIdentities;
    }

    @Override
    public void removeLock( String customerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_LOCK, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<String> selectSuspiciousIdentityCuidsList( String ruleCode, Plugin plugin )
    {
        final Set<String> cuids = new HashSet<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CUIDS, plugin ) )
        {
            daoUtil.setString( 1, ruleCode );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                cuids.add( daoUtil.getString( 1 ) );
            }
        }
        try( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_DUPLICATE_CUIDS, plugin ) )
        {
            daoUtil.setString( 1, ruleCode );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                cuids.add( daoUtil.getString( 1 ) );
            }

        }
        return List.copyOf(cuids);
    }

    @Override
    public void removeExcludedIdentities( String firstCuid, String secondCuid, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_EXCLUDED_IDENTITIES, plugin ) )
        {
            daoUtil.setString( 1, firstCuid );
            daoUtil.setString( 2, secondCuid );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void removeExcludedIdentities( final String cuid, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REMOVE_EXCLUDED_IDENTITIES_ONE_CUID, plugin ) )
        {
            daoUtil.setString( 1, cuid );
            daoUtil.setString( 2, cuid );
            daoUtil.executeUpdate( );
        }
    }
}

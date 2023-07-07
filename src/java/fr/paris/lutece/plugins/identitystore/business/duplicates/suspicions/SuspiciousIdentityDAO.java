/*
 * Copyright (c) 2002-2023, City of Paris
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

import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for SuspiciousIdentity objects
 */
public final class SuspiciousIdentityDAO implements ISuspiciousIdentityDAO
{
    // Constants
    private static final String SQL_QUERY_PURGE = "TRUNCATE TABLE identitystore_quality_suspicious_identity";
    private static final String SQL_QUERY_SELECT = "SELECT i.id_suspicious_identity, i.customer_id , i.id_duplicate_rule, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id WHERE id_suspicious_identity = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_quality_suspicious_identity ( customer_id, id_duplicate_rule ) VALUES ( ?, ?) ";
    private static final String SQL_QUERY_ADD_LOCK = "INSERT INTO identitystore_quality_suspicious_identity_lock ( customer_id, is_locked, date_lock_end, author_type, author_name ) VALUES ( ?, ?, ?, ?, ?) ";
    private static final String SQL_QUERY_REMOVE_LOCK = "DELETE FROM identitystore_quality_suspicious_identity_lock WHERE customer_id = ? ";
    private static final String SQL_QUERY_CHECK_EXCLUDED = "SELECT COUNT(*) FROM identitystore_quality_suspicious_identity_excluded WHERE (first_cuid = ? AND second_cuid = ?) OR (first_cuid = ? AND second_cuid = ?)";
    private static final String SQL_QUERY_INSERT_EXCLUDED = "INSERT INTO identitystore_quality_suspicious_identity_excluded ( first_cuid,second_cuid ) VALUES ( ?, ?) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_quality_suspicious_identity WHERE id_suspicious_identity = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_quality_suspicious_identity SET customer_id = ? WHERE id_suspicious_identity = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT i.id_suspicious_identity, i.customer_id, i.id_duplicate_rule, i.date_create, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id ";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_suspicious_identity FROM identitystore_quality_suspicious_identity";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT i.id_suspicious_identity, i.customer_id, i.id_duplicate_rule, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id  WHERE id_suspicious_identity IN (  ";
    private static final String SQL_QUERY_SELECT_BY_CUSTOMER_ID = "SELECT i.id_suspicious_identity, i.customer_id, i.id_duplicate_rule, l.date_lock_end, l.is_locked, l.author_type, l.author_name FROM identitystore_quality_suspicious_identity i LEFT JOIN identitystore_quality_suspicious_identity_lock l ON i.customer_id = l.customer_id  WHERE i.customer_id = ?  ";
    private static final String SQL_QUERY_SELECT_COUNT_BY_RULE_ID = "SELECT count(id_suspicious_identity) FROM identitystore_quality_suspicious_identity WHERE id_duplicate_rule = ? ";

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
            daoUtil.setInt( nIndex++,
                    suspiciousIdentity.getIdDuplicateRule( ) != null ? suspiciousIdentity.getIdDuplicateRule( ) : Constants.MANUAL_SUSPICIOUS_RULE_ID );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                suspiciousIdentity.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    @Override
    public void insertExcluded( String firstCuid, String secondCuid, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_EXCLUDED, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            daoUtil.setString( 1, firstCuid );
            daoUtil.setString( 2, secondCuid );
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
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex++ ) );
                }
                else
                {
                    lock.setLocked( false );
                }
                suspiciousIdentity.setLock( lock );
            }

            return Optional.ofNullable( suspiciousIdentity );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public SuspiciousIdentity selectByCustomerID( String customerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeQuery( );
            SuspiciousIdentity suspiciousIdentity = null;

            if ( daoUtil.next( ) )
            {
                suspiciousIdentity = new SuspiciousIdentity( );
                int nIndex = 1;

                suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex++ ) );
                }
                else
                {
                    lock.setLocked( false );
                }
                suspiciousIdentity.setLock( lock );
            }

            return suspiciousIdentity;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
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
            daoUtil.setInt( nIndex, suspiciousIdentity.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<SuspiciousIdentity> selectSuspiciousIdentitysList( final Integer rule, final int max, Plugin plugin )
    {
        final List<SuspiciousIdentity> suspiciousIdentityList = new ArrayList<>( );
        String query = SQL_QUERY_SELECTALL;
        if ( rule != null )
        {
            query += " where id_duplicate_rule = " + rule + " ";
        }
        query += ( max != 0 ? " LIMIT " + max : "" );

        try ( final DAOUtil daoUtil = new DAOUtil( query, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final SuspiciousIdentity suspiciousIdentity = new SuspiciousIdentity( );
                int nIndex = 1;

                suspiciousIdentity.setId( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );
                suspiciousIdentity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                if ( lock.getLockEndDate( ) != null )
                {
                    lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                    lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                    lock.setAuthorName( daoUtil.getString( nIndex++ ) );
                }
                else
                {
                    lock.setLocked( false );
                }
                suspiciousIdentityList.add( suspiciousIdentity );
            }

            return suspiciousIdentityList;
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
        List<SuspiciousIdentity> suspiciousIdentityList = new ArrayList<>( );

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
                    suspiciousIdentity.setIdDuplicateRule( daoUtil.getInt( nIndex++ ) );

                    final SuspiciousIdentityLock lock = new SuspiciousIdentityLock( );
                    lock.setLockEndDate( daoUtil.getTimestamp( nIndex++ ) );
                    if ( lock.getLockEndDate( ) != null )
                    {
                        lock.setLocked( daoUtil.getBoolean( nIndex++ ) );
                        lock.setAuthorType( daoUtil.getString( nIndex++ ) );
                        lock.setAuthorName( daoUtil.getString( nIndex++ ) );
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
            daoUtil.executeQuery( );
        }
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
}
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
package fr.paris.lutece.plugins.identitystore.business.identity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.UpdatedIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchUpdatedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for Identity objects
 */
public final class IdentityDAO implements IIdentityDAO
{
    // Constants
    private static final String COLUMNS = "a.id_identity, a.connection_id, a.customer_id, a.is_deleted, a.is_merged, a.date_create, a.last_update_date, a.date_merge, a.is_mon_paris_active, a.expiration_date, a.id_master_identity, a.date_delete";
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active, expiration_date  FROM identitystore_identity WHERE id_identity = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity (  connection_id, customer_id, date_create, last_update_date, is_mon_paris_active, expiration_date ) VALUES ( ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity WHERE id_identity = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity SET connection_id = ?, customer_id = ?, last_update_date = ?, is_mon_paris_active = ? WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECTALL_FULL = "SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active, expiration_date FROM identitystore_identity";
    private static final String SQL_QUERY_SELECT_BY_CONNECTION_ID = "SELECT " + COLUMNS
            + " FROM identitystore_identity a WHERE lower(a.connection_id) = lower(?)";
    private static final String SQL_QUERY_SELECT_BY_CUSTOMER_ID = "SELECT " + COLUMNS + " FROM identitystore_identity a WHERE a.customer_id = ?";
    private static final String SQL_QUERY_SELECT_NOT_MERGED_BY_CUSTOMER_ID = "WITH RECURSIVE identity_tree AS ("
            + "    SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active, expiration_date, date_delete"
            + "    FROM identitystore_identity" + "    WHERE customer_id = ?" + "    UNION ALL"
            + "    SELECT id.id_identity, id.connection_id, id.customer_id, id.is_deleted, id.is_merged, id.id_master_identity, id.date_create, id.last_update_date, id.date_merge, id.is_mon_paris_active, id.expiration_date, id.date_delete"
            + "    FROM identitystore_identity id" + "        INNER JOIN identity_tree mtree ON mtree.id_master_identity = id.id_identity" + " )" + " select "
            + COLUMNS + " from identity_tree a where a.is_merged = 0;";

    private static final String SQL_QUERY_SELECT_NOT_MERGED_BY_CONNECTION_ID = "WITH RECURSIVE identity_tree AS ("
            + "    SELECT id_identity, connection_id, customer_id, is_deleted, is_merged, id_master_identity, date_create, last_update_date, date_merge, is_mon_paris_active, expiration_date, date_delete"
            + "    FROM identitystore_identity" + "    WHERE lower(connection_id) = lower(?)" + "    UNION ALL"
            + "    SELECT id.id_identity, id.connection_id, id.customer_id, id.is_deleted, id.is_merged, id.id_master_identity, id.date_create, id.last_update_date, id.date_merge, id.is_mon_paris_active, id.expiration_date, id.date_delete"
            + "    FROM identitystore_identity id" + "        INNER JOIN identity_tree mtree ON mtree.id_master_identity = id.id_identity" + " )" + " select "
            + COLUMNS + " from identity_tree a where a.is_merged = 0;";

    private static final String SQL_QUERY_SELECT_ID_BY_CUSTOMER_ID = "SELECT id_identity, is_deleted, is_merged FROM identitystore_identity WHERE customer_id = ?";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTES_FOR_API_SEARCH = " SELECT " + COLUMNS + " FROM identitystore_identity a ${join_clause} LIMIT ${limit}";
    private static final String SQL_QUERY_WITH_CLAUSE_FOR_API_SEARCH = "WITH ${with_clause} ";
    private static final String SQL_QUERY_JOIN_CLAUSE_FOR_API_SEARCH = "JOIN ${tmp_table_name} on ${tmp_table_name}.id_identity = a.id_identity ";
    private static final String SQL_QUERY_TMP_TABLE_FOR_API_SEARCH = " AS (SELECT ${distinct} b.id_identity AS id_identity FROM identitystore_identity_attribute b JOIN identitystore_ref_attribute c ON b.id_attribute = c.id_attribute AND ${filter})";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE_FOR_API_SEARCH = "c.key_name IN (${key_name_list}) AND LOWER(b.attribute_value) = '${value}'";
    private static final String SQL_QUERY_FILTER_NORMALIZED_ATTRIBUTE_FOR_API_SEARCH = "c.key_name IN (${key_name_list}) AND TRANSLATE(REPLACE(REPLACE(LOWER(b.attribute_value), 'œ', 'oe'), 'æ', 'ae'), 'àâäéèêëîïôöùûüÿçñ', 'aaaeeeeiioouuuycn') = '${value}'";
    private static final String SQL_QUERY_SOFT_DELETE = "UPDATE identitystore_identity SET  date_delete = now( ), is_mon_paris_active = 0, expiration_date=now( ), last_update_date=now( )  WHERE customer_id = ?";
    private static final String SQL_QUERY_MERGE = "UPDATE identitystore_identity SET is_merged = 1, date_merge = now(), last_update_date = now(), id_master_identity = ? WHERE id_identity = ?";
    private static final String SQL_QUERY_CANCEL_MERGE = "UPDATE identitystore_identity SET is_merged = 0, date_merge = null, last_update_date = now(), id_master_identity = null WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECT_BY_ATTRIBUTE_EXISTING = "SELECT a.customer_id FROM identitystore_identity a"
            + " JOIN identitystore_identity_attribute b ON a.id_identity = b.id_identity"
            + " WHERE b.id_attribute IN (${id_attribute_list}) AND (${not_merged}) AND (${not_suspicious})"
            + " GROUP BY a.id_identity HAVING COUNT (b.id_attribute) >= ${count}";
    private static final String SQL_QUERY_FILTER_NOT_MERGED = "a.is_merged = 0 AND a.date_merge IS NULL";
    private static final String SQL_QUERY_FILTER_NOT_SUSPICIOUS = "NOT EXISTS (SELECT c.id_suspicious_identity FROM identitystore_quality_suspicious_identity c WHERE c.customer_id = a.customer_id)";
    private static final String SQL_QUERY_FILTER_LOWER_SUSPICIOUS = "NOT EXISTS (SELECT c.id_suspicious_identity FROM identitystore_quality_suspicious_identity c JOIN identitystore_duplicate_rule r on c.id_duplicate_rule = r.id_rule WHERE c.customer_id = a.customer_id AND r.priority <= ${rule_priority})";
    private static final String SQL_QUERY_INSERT_HISTORY = "INSERT INTO identitystore_identity_history (change_type, change_status, change_message, author_type, author_name, client_code, customer_id, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, to_json(?::json))";
    private static final String SQL_QUERY_UPSERT_HISTORY = "WITH tmp AS ( "
            + "    (SELECT ? AS change_type, ? AS change_status, ? AS change_message, ? AS author_type, ? AS author_name, ? AS client_code, ? AS customer_id, to_json(?::json) AS metadata) "
            + ") "
            + "INSERT INTO identitystore_identity_history (change_type, change_status, change_message, author_type, author_name, client_code, customer_id, metadata) "
            + "SELECT change_type, change_status, change_message, author_type, author_name, client_code, customer_id, metadata FROM TMP " + "WHERE NOT EXISTS( "
            + "    SELECT master.id_history FROM TMP "
            + "    JOIN identitystore_identity_history master ON master.change_type = tmp.change_type AND  master.customer_id = tmp.customer_id AND master.author_name = tmp.author_name "
            + "    WHERE date_trunc('day', master.modification_date) = date_trunc('day', CURRENT_TIMESTAMP)) ";
    private static final String SQL_QUERY_SELECT_IDENTITY_HISTORY = "SELECT change_type, change_status, change_message, author_type, author_name, client_code, customer_id, modification_date, metadata::text FROM identitystore_identity_history WHERE customer_id = ?  ORDER BY modification_date DESC";
    private static final String SQL_QUERY_SEARCH_IDENTITY_HISTORY = "SELECT change_type, change_status, change_message, author_type, author_name, client_code, customer_id, modification_date, metadata::text FROM identitystore_identity_history WHERE ${client_code} AND ${customer_id} AND ${author_name} AND ${change_type} AND ${nbDaysFrom} AND ${metadata} AND ${change_status} AND ${author_type} AND ${modification_date} ORDER BY modification_date DESC LIMIT ${limit}";

    private static final String SQL_QUERY_SELECT_UPDATED_IDENTITIES = "SELECT DISTINCT i.customer_id, i.last_update_date FROM identitystore_identity i JOIN identitystore_identity_history ih ON i.customer_id = ih.customer_id JOIN identitystore_identity_attribute_history iah ON i.id_identity = iah.id_identity WHERE 1=1";
    private static final String SQL_QUERY_SELECT_UPDATED_IDENTITIES_FROM_IDS = "SELECT i.customer_id, i.last_update_date FROM identitystore_identity i WHERE id_identity IN (${identity_id_list}) ORDER BY i.last_update_date DESC";
    private static final String SQL_QUERY_SELECT_UPDATED_IDENTITY_IDS = "SELECT DISTINCT i.id_identity, i.last_update_date FROM identitystore_identity i JOIN identitystore_identity_history ih ON i.customer_id = ih.customer_id JOIN identitystore_identity_attribute_history iah ON i.id_identity = iah.id_identity WHERE 1=1";
    private static final String SQL_QUERY_FILTER_LAST_UPDATE = "i.last_update_date > (NOW() - INTERVAL '${days}' DAY)";
    private static final String SQL_QUERY_FILTER_IDENTITY_MODIFICATION_DATE = "ih.modification_date > (NOW() - INTERVAL '${days}' DAY)";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE_MODIFICATION_DATE = "iah.modification_date > (NOW() - INTERVAL '${days}' DAY)";
    private static final String SQL_QUERY_FILTER_IDENTITY_CHANGE_TYPE = "ih.change_type IN (${identity_change_type_list})";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE_CHANGE_TYPE = "iah.change_type IN (${attribute_change_type_list})";
    private static final String SQL_QUERY_FILTER_ATTRIBUTE_KEY = "iah.attribute_key = '${attribute_key}'";

    private static final String SQL_QUERY_REFRESH_LAST_UPDATE_DATE = "UPDATE identitystore_identity SET last_update_date = now() WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECT_EXPIRED_NOT_MERGED_AND_NOT_CONNECTED = "SELECT " + COLUMNS
            + " FROM identitystore_identity a WHERE a.expiration_date < NOW() AND a.is_merged = 0 AND a.is_mon_paris_active = 0 LIMIT ?";

    private static final String SQL_QUERY_SELECT_NOT_MERGED_NOT_CONNECTED_WITH_UNCERTIFIED_ATTRIBUTE =
            "SELECT a.customer_id FROM identitystore_identity a"
            + " JOIN identitystore_identity_attribute b ON a.id_identity = b.id_identity"
            + " JOIN identitystore_ref_attribute c ON c.id_attribute = b.id_attribute"
            + " JOIN identitystore_identity_attribute_certificate d ON b.id_certification = d.id_attribute_certificate"
            + " WHERE c.key_name = ? AND a.is_merged = 0 AND a.is_mon_paris_active = 0 AND d.certifier_code = ?"
            + " LIMIT ?";

    private static final String SQL_QUERY_SELECT_MERGED_TO = "SELECT " + COLUMNS
            + " FROM identitystore_identity a WHERE a.is_merged = 1 AND a.id_master_identity = ?";
    private static final String SQL_QUERY_DELETE_ALL_ATTRIBUTE_HISTORY = "DELETE from identitystore_identity_attribute_history WHERE id_identity = ?";
    private static final String SQL_QUERY_SELECT_LAST_UPDATE_DATE_FROM_CUID = "SELECT last_update_date FROM identitystore_identity WHERE customer_id = ?";
    private static final String SQL_QUERY_SELECT_COUNT_IDENTITIES = "SELECT COUNT(*) FROM identitystore_identity";
    private static final String SQL_QUERY_SELECT_COUNT_DELETED_IDENTITIES = "SELECT COUNT(*) FROM identitystore_identity WHERE is_deleted = ?";
    private static final String SQL_QUERY_SELECT_COUNT_MERGED_IDENTITIES = "SELECT COUNT(*) FROM identitystore_identity WHERE is_merged = ?";
    private static final String SQL_QUERY_SELECT_COUNT_MONPARIS_ACTIVE_IDENTITIES = "SELECT COUNT(*) FROM identitystore_identity WHERE is_mon_paris_active = ?";
    private static final String SQL_QUERY_SELECT_COUNT_ATTRIBUTES_BY_IDENTITY = "SELECT v.nbattr, count(v.id_identity) as identities FROM (SELECT id_identity , count(id_identity) as nbattr FROM identitystore_identity_attribute GROUP BY id_identity) as v GROUP BY v.nbattr ORDER BY v.nbattr";
    private static final String SQL_QUERY_SELECT_COUNT_IDENTITIES_NO_ATTRIBUTES_NOT_MERGED = "SELECT count(*) FROM identitystore_identity i LEFT OUTER JOIN identitystore_identity_attribute a ON a.id_identity = i.id_identity WHERE is_merged = 0 AND a.id_identity is null";
    private static final String SQL_QUERY_SELECT_COUNT_INDEX_ELIGIBLE_IDENTITIES = "SELECT count(i.id_identity) FROM identitystore_identity i WHERE is_deleted = 0 AND is_merged = 0 AND exists(SELECT a.id_attribute FROM identitystore_identity_attribute a WHERE i.id_identity = a.id_identity)";
    private static final String SQL_QUERY_SELECT_COUNT_INDEX_NOT_ELIGIBLE_IDENTITIES = "SELECT count(i.id_identity) FROM identitystore_identity i WHERE is_deleted = 1   OR is_merged = 1   OR not exists(SELECT a.id_attribute FROM identitystore_identity_attribute a WHERE i.id_identity = a.id_identity)";
    private static final String SQL_QUERY_SELECT_ACTIONS_ACTIVITIES = "SELECT change_type AS change_type_label, change_status , author_type, client_code , count(*) FROM identitystore_identity_history WHERE modification_date > NOW() - INTERVAL '${interval} DAY' GROUP BY change_type , change_status , author_type, client_code ORDER BY client_code, change_type, change_status";
    private static final String SQL_QUERY_SELECT_STATUS_LIST = "SELECT DISTINCT change_status FROM identitystore_identity_history";

    private final ObjectMapper objectMapper = new ObjectMapper( );

    /**
     * Generates a new customerId key using Java UUID
     *
     * @return The new customerID
     */
    public String newCustomerIdKey( )
    {
        return UUID.randomUUID( ).toString( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( final Identity identity, final int dataRetentionPeriodInMonth, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            final ZonedDateTime now = ZonedDateTime.now( ZoneId.systemDefault( ) );
            identity.setCreationDate( Timestamp.from( now.toInstant( ) ) );
            identity.setLastUpdateDate( Timestamp.from( now.toInstant( ) ) );
            identity.setExpirationDate( Timestamp.from( now.plusMonths( dataRetentionPeriodInMonth ).toInstant( ) ) );

            int nIndex = 1;
            identity.setCustomerId( newCustomerIdKey( ) );
            daoUtil.setString( nIndex++, identity.getConnectionId( ) );
            daoUtil.setString( nIndex++, identity.getCustomerId( ) );
            daoUtil.setTimestamp( nIndex++, identity.getCreationDate( ) );
            daoUtil.setTimestamp( nIndex++, identity.getLastUpdateDate( ) );
            daoUtil.setBoolean( nIndex++, identity.isMonParisActive( ) );
            daoUtil.setTimestamp( nIndex, identity.getExpirationDate( ) );

            daoUtil.executeUpdate( );

            if ( daoUtil.nextGeneratedKey( ) )
            {
                identity.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Identity load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {

                identity = new Identity( );

                int nIndex = 1;
                identity.setId( daoUtil.getInt( nIndex++ ) );
                identity.setConnectionId( daoUtil.getString( nIndex++ ) );
                identity.setCustomerId( daoUtil.getString( nIndex++ ) );
                identity.setDeleted( daoUtil.getBoolean( nIndex++ ) );
                identity.setMerged( daoUtil.getBoolean( nIndex++ ) );
                identity.setMasterIdentityId( daoUtil.getInt( nIndex++ ) );
                identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMergeDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMonParisActive( daoUtil.getBoolean( nIndex++ ) );
                identity.setExpirationDate( daoUtil.getTimestamp( nIndex ) );
            }

            if ( identity != null )
            {
                identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) );
            }

            return identity;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void hardDelete( int nKey, Plugin plugin )
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
    public void softDelete( String strCuid, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SOFT_DELETE, plugin ) )
        {
            daoUtil.setString( 1, strCuid );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void merge( Identity identity, Plugin plugin )
    {
        identity.setMerged( true );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_MERGE, plugin ) )
        {
            daoUtil.setInt( 1, identity.getMasterIdentityId( ) );
            daoUtil.setInt( 2, identity.getId( ) );
            daoUtil.executeUpdate( );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REFRESH_LAST_UPDATE_DATE, plugin ) )
        {
            daoUtil.setInt( 1, identity.getMasterIdentityId( ) );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void cancelMerge( Identity identity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CANCEL_MERGE, plugin ) )
        {
            daoUtil.setInt( 1, identity.getId( ) );
            daoUtil.executeUpdate( );
        }
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_REFRESH_LAST_UPDATE_DATE, plugin ) )
        {
            daoUtil.setInt( 1, identity.getMasterIdentityId( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( Identity identity, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            identity.setLastUpdateDate( new Timestamp( new Date( ).getTime( ) ) );

            daoUtil.setString( nIndex++, identity.getConnectionId( ) );
            daoUtil.setString( nIndex++, identity.getCustomerId( ) );
            daoUtil.setTimestamp( nIndex++, identity.getLastUpdateDate( ) );
            daoUtil.setBoolean( nIndex++, identity.isMonParisActive( ) );
            daoUtil.setInt( nIndex, identity.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<Identity> selectAll( Plugin plugin )
    {
        final List<Identity> identityList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_FULL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                int nIndex = 1;
                final Identity identity = new Identity( );
                identityList.add( identity );
                identity.setId( daoUtil.getInt( nIndex++ ) );
                identity.setConnectionId( daoUtil.getString( nIndex++ ) );
                identity.setCustomerId( daoUtil.getString( nIndex++ ) );
                identity.setDeleted( daoUtil.getBoolean( nIndex++ ) );
                identity.setMerged( daoUtil.getBoolean( nIndex++ ) );
                identity.setMasterIdentityId( daoUtil.getInt( nIndex++ ) );
                identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMergeDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMonParisActive( daoUtil.getBoolean( nIndex++ ) );
                identity.setExpirationDate( daoUtil.getTimestamp( nIndex ) );
            }

            return identityList;
        }
    }

    @Override
    public Identity selectByConnectionId( String strConnectionId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CONNECTION_ID, plugin ) )
        {
            daoUtil.setString( 1, strConnectionId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectByCustomerId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectNotMergedByCustomerId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public Identity selectNotMergedByConnectionId( String strCustomerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_BY_CONNECTION_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            Identity identity = null;

            if ( daoUtil.next( ) )
            {
                identity = this.getIdentityFromQuery( daoUtil );
            }

            return identity;
        }
    }

    @Override
    public int selectIdByCustomerId( final String strCustomerId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ID_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );
            daoUtil.executeQuery( );

            int nIdentityId = -1;

            if ( daoUtil.next( ) )
            {
                nIdentityId = daoUtil.getInt( 1 );
            }

            return nIdentityId;
        }
    }

    /**
     * return Identity object from select query
     *
     * @param daoUtil
     *            daoUtil initialized with select query
     * @return Identity load from result
     */
    private Identity getIdentityFromQuery( DAOUtil daoUtil )
    {
        Identity identity = new Identity( );

        int nIndex = 1;

        identity.setId( daoUtil.getInt( nIndex++ ) );
        identity.setConnectionId( daoUtil.getString( nIndex++ ) );
        identity.setCustomerId( daoUtil.getString( nIndex++ ) );
        identity.setDeleted( daoUtil.getBoolean( nIndex++ ) );
        identity.setMerged( daoUtil.getBoolean( nIndex++ ) );
        identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setMergeDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setMonParisActive( daoUtil.getBoolean( nIndex++ ) );
        identity.setExpirationDate( daoUtil.getTimestamp( nIndex++ ) );
        identity.setMasterIdentityId( daoUtil.getObject( nIndex++, Integer.class ) );
        identity.setDeleteDate( daoUtil.getTimestamp( nIndex ) );

        return identity;
    }

    /**
     * return Identity change object from select query
     *
     * @param daoUtil
     *            daoUtil initialized with select query
     * @return Identity change load from result
     */
    private IdentityChange getIdentityChangeFromQuery( final DAOUtil daoUtil ) throws JsonProcessingException
    {
        RequestAuthor author = new RequestAuthor( );
        IdentityChange identityChange = new IdentityChange( );

        int nIndex = 1;

        // change_type, change_status, change_message, author_type, author_name, client_code, customer_id "
        identityChange.setChangeType( IdentityChangeType.valueOf( daoUtil.getInt( nIndex++ ) ) );
        identityChange.setChangeStatus( daoUtil.getString( nIndex++ ) );
        identityChange.setChangeMessage( daoUtil.getString( nIndex++ ) );
        author.setType( AuthorType.valueOf( daoUtil.getString( nIndex++ ) ) );
        author.setName( daoUtil.getString( nIndex++ ) );
        identityChange.setClientCode( daoUtil.getString( nIndex++ ) );
        identityChange.setCustomerId( daoUtil.getString( nIndex++ ) );
        identityChange.setModificationDate( daoUtil.getTimestamp( nIndex++ ) );
        final String jsonMap = daoUtil.getString( nIndex );
        if ( StringUtils.isNotEmpty( jsonMap ) )
        {
            final Map<String, String> mapMetaData = objectMapper.readValue( jsonMap, new TypeReference<Map<String, String>>( )
            {
            } );
            identityChange.getMetadata( ).clear( );
            if ( mapMetaData != null && !mapMetaData.isEmpty( ) )
            {
                identityChange.getMetadata( ).putAll( mapMetaData );
            }
        }

        identityChange.setAuthor( author );

        return identityChange;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Identity> selectByAttributesValueForApiSearch( final List<SearchAttribute> searchAttributes, final int nMaxNbIdentityReturned, final Plugin plugin )
    {
        final List<Identity> listIdentities = new ArrayList<>( );
        final Map<String, String> withClauses = new HashMap<>( );

        if ( searchAttributes == null || searchAttributes.isEmpty( ) )
        {
            return listIdentities;
        }

        for ( final SearchAttribute attribute : searchAttributes )
        {
            if ( StringUtils.isBlank( attribute.getValue() ) )
            {
                continue;
            }

            final String filter;
            if ( attribute.getKey( ).equals( Constants.PARAM_FIRST_NAME ) || attribute.getKey( ).equals( Constants.PARAM_FAMILY_NAME )
                    ||  ( attribute.getOutputKeys( ) != null && !attribute.getOutputKeys( ).isEmpty( ) && ( attribute.getOutputKeys( ).contains( Constants.PARAM_FIRST_NAME ) || attribute.getOutputKeys( ).contains( Constants.PARAM_FAMILY_NAME ) ) ) )
            {
                filter = SQL_QUERY_FILTER_NORMALIZED_ATTRIBUTE_FOR_API_SEARCH.replace("${key_name_list}", "'" + String.join("', '", attribute.getOutputKeys( ) ) + "'" ).replace("${value}", this.normalizeValue( attribute.getValue( ) ) );
            }
            else
            {
                filter = SQL_QUERY_FILTER_ATTRIBUTE_FOR_API_SEARCH.replace("${key_name_list}", "'" + String.join("', '", attribute.getOutputKeys( ) ) + "'" ).replace("${value}", StringUtils.lowerCase( attribute.getValue( ) ) );
            }

            if(attribute.getKey( ).equals(Constants.PARAM_COMMON_EMAIL)
                    || attribute.getKey( ).equals( Constants.PARAM_COMMON_LASTNAME )
                    || attribute.getKey( ).equals(Constants.PARAM_COMMON_PHONE) )
            {
                withClauses.put(attribute.getKey(), SQL_QUERY_TMP_TABLE_FOR_API_SEARCH.replace("${filter}", filter )
                        .replace("${distinct}", "DISTINCT") );
            }
            else
            {
                withClauses.put(attribute.getKey(), SQL_QUERY_TMP_TABLE_FOR_API_SEARCH.replace("${filter}", filter )
                        .replace("${distinct}", "") );
            }
        }

        if ( withClauses.isEmpty( ) )
        {
            return listIdentities;
        }

        final String strSQL = SQL_QUERY_WITH_CLAUSE_FOR_API_SEARCH.replace("${with_clause}", withClauses.entrySet( ).stream( ).map( entry -> entry.getKey() + " " + entry.getValue( ) ).collect( Collectors.joining(", ") ) )
                + SQL_QUERY_SELECT_BY_ATTRIBUTES_FOR_API_SEARCH.replace( "${join_clause}", withClauses.keySet( ).stream( ).map( key -> SQL_QUERY_JOIN_CLAUSE_FOR_API_SEARCH.replace( "${tmp_table_name}", key ) ).collect(Collectors.joining( " " )) ).replace( "${limit}", String.valueOf( nMaxNbIdentityReturned ) );
        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Identity identity = getIdentityFromQuery( daoUtil );
                listIdentities.add( identity );
            }

            return listIdentities;
        }
    }

    private String normalizeValue( final String value )
    {
        return StringUtils.stripAccents( value.toLowerCase( ).replace( "œ", "oe" ).replace( "æ", "ae" ) );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<String> selectByAttributeExisting( final List<Integer> idAttributeList, final int nbFilledAttributes, final boolean notMerged,
            final boolean notSuspicious, final int rulePriority, final Plugin plugin )
    {
        final List<String> listCuids = new ArrayList<>( );
        if ( idAttributeList == null || idAttributeList.isEmpty( ) )
        {
            return listCuids;
        }
        String sql = SQL_QUERY_SELECT_BY_ATTRIBUTE_EXISTING
                .replace( "${id_attribute_list}", idAttributeList.stream( ).map( Object::toString ).collect( Collectors.joining( ", " ) ) )
                .replace( "${not_merged}", ( notMerged ? SQL_QUERY_FILTER_NOT_MERGED : "1=1" ) )
                .replace( "${not_suspicious}", ( notSuspicious ? SQL_QUERY_FILTER_NOT_SUSPICIOUS : SQL_QUERY_FILTER_LOWER_SUSPICIOUS.replace("${rule_priority}", String.valueOf( rulePriority ) ) ) )
                .replace( "${count}", String.valueOf( nbFilledAttributes ) );
        try ( final DAOUtil daoUtil = new DAOUtil( sql, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listCuids.add( daoUtil.getString( 1 ) );
            }
        }
        return listCuids;
    }

    @Override
    public void addChangeHistory( IdentityChange identityChange, Plugin plugin ) throws IdentityStoreException
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityChange.getChangeType( ).getValue( ) );
            daoUtil.setString( nIndex++, identityChange.getChangeStatus( ) );
            daoUtil.setString( nIndex++, identityChange.getChangeMessage( ) );
            daoUtil.setString( nIndex++, identityChange.getAuthor( ).getType( ).name( ) );
            daoUtil.setString( nIndex++, identityChange.getAuthor( ).getName( ) );
            daoUtil.setString( nIndex++, identityChange.getClientCode( ) );
            daoUtil.setString( nIndex++, identityChange.getCustomerId( ) );
            daoUtil.setString( nIndex, objectMapper.writeValueAsString( identityChange.getMetadata( ) ) );
            daoUtil.executeUpdate( );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public void addOrUpdateChangeHistory( IdentityChange identityChange, Plugin plugin ) throws IdentityStoreException
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPSERT_HISTORY, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityChange.getChangeType( ).getValue( ) );
            daoUtil.setString( nIndex++, identityChange.getChangeStatus( ) );
            daoUtil.setString( nIndex++, identityChange.getChangeMessage( ) );
            daoUtil.setString( nIndex++, identityChange.getAuthor( ).getType( ).name( ) );
            daoUtil.setString( nIndex++, identityChange.getAuthor( ).getName( ) );
            daoUtil.setString( nIndex++, identityChange.getClientCode( ) );
            daoUtil.setString( nIndex++, identityChange.getCustomerId( ) );
            daoUtil.setString( nIndex, objectMapper.writeValueAsString( identityChange.getMetadata( ) ) );
            daoUtil.executeUpdate( );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityChange> selectIdentityHistoryByCustomerId( String strCustomerId, Plugin plugin ) throws IdentityStoreException
    {
        List<IdentityChange> listIdentitieChanges = new ArrayList<>( );

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_IDENTITY_HISTORY, plugin ) )
        {
            daoUtil.setString( 1, strCustomerId );

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final IdentityChange identityChange = getIdentityChangeFromQuery( daoUtil );
                listIdentitieChanges.add( identityChange );
            }

            return listIdentitieChanges;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UpdatedIdentityDto> selectUpdated( final Integer days, final List<IdentityChangeType> identityChangeTypes,
            final List<SearchUpdatedAttribute> updatedAttributes, final Integer max, final Plugin plugin )
    {
        final List<UpdatedIdentityDto> list = new ArrayList<>( );
        final StringBuilder sqlBuilder = new StringBuilder( SQL_QUERY_SELECT_UPDATED_IDENTITIES );

        addUpdatedIdentitiesFilters( days, identityChangeTypes, updatedAttributes, max, sqlBuilder );

        try ( final DAOUtil daoUtil = new DAOUtil( sqlBuilder.toString( ), plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final UpdatedIdentityDto updatedIdentity = new UpdatedIdentityDto( );
                int nIndex = 1;
                updatedIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                updatedIdentity.setModificationDate( daoUtil.getTimestamp( nIndex ) );
                list.add( updatedIdentity );
            }
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> selectUpdatedIds( final Integer days, final List<IdentityChangeType> identityChangeTypes,
            final List<SearchUpdatedAttribute> updatedAttributes, final Integer max, final Plugin plugin )
    {
        final List<Integer> ids = new ArrayList<>( );
        final StringBuilder sqlBuilder = new StringBuilder( SQL_QUERY_SELECT_UPDATED_IDENTITY_IDS );

        addUpdatedIdentitiesFilters( days, identityChangeTypes, updatedAttributes, max, sqlBuilder );

        try ( final DAOUtil daoUtil = new DAOUtil( sqlBuilder.toString( ), plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                ids.add( daoUtil.getInt( 1 ) );
            }
        }
        return ids;
    }

    /**
     * Adds filters for the search updated identities request.
     */
    private void addUpdatedIdentitiesFilters( final Integer days, final List<IdentityChangeType> identityChangeTypes,
            final List<SearchUpdatedAttribute> updatedAttributes, final Integer max, final StringBuilder sqlBuilder )
    {
        if ( days != null )
        {
            // AND i.last_update_date > (NOW() - INTERVAL '100' DAY)
            sqlBuilder.append( " AND " ).append( SQL_QUERY_FILTER_LAST_UPDATE.replace( "${days}", days.toString( ) ) );
        }
        if ( !identityChangeTypes.isEmpty( ) )
        {
            if ( days != null )
            {
                // AND ih.modification_date > (NOW() - INTERVAL '100' DAY)
                sqlBuilder.append( " AND " ).append( SQL_QUERY_FILTER_IDENTITY_MODIFICATION_DATE.replace( "${days}", days.toString( ) ) );
            }
            // AND ih.change_type in (0, 1)
            sqlBuilder.append( " AND " ).append( SQL_QUERY_FILTER_IDENTITY_CHANGE_TYPE.replace( "${identity_change_type_list}",
                    identityChangeTypes.stream( ).map( changeType -> String.valueOf( changeType.getValue( ) ) ).collect( Collectors.joining( ", " ) ) ) );
        }
        if ( !updatedAttributes.isEmpty( ) )
        {
            if ( days != null )
            {
                // AND iah.modification_date > (NOW() - INTERVAL '100' DAY)
                sqlBuilder.append( " AND " ).append( SQL_QUERY_FILTER_ATTRIBUTE_MODIFICATION_DATE.replace( "${days}", days.toString( ) ) );
            }
            final List<String> fullAttributesFilters = new ArrayList<>( );
            for ( final SearchUpdatedAttribute attribute : updatedAttributes )
            {
                final List<String> attributeFilters = new ArrayList<>( 2 );
                if ( StringUtils.isNotBlank( attribute.getAttributeKey( ) ) )
                {
                    attributeFilters.add( SQL_QUERY_FILTER_ATTRIBUTE_KEY.replace( "${attribute_key}", attribute.getAttributeKey( ) ) );
                }
                if ( !attribute.getAttributeChangeTypes( ).isEmpty( ) )
                {
                    attributeFilters.add( SQL_QUERY_FILTER_ATTRIBUTE_CHANGE_TYPE.replace( "${attribute_change_type_list}", attribute.getAttributeChangeTypes( )
                            .stream( ).map( changeType -> String.valueOf( changeType.getValue( ) ) ).collect( Collectors.joining( ", " ) ) ) );
                }
                if ( !attributeFilters.isEmpty( ) )
                {
                    fullAttributesFilters.add( attributeFilters.stream( ).collect( Collectors.joining( " AND ", "(", ")" ) ) );
                }
            }
            if ( !fullAttributesFilters.isEmpty( ) )
            {
                // AND ( ( iah.change_type IN (0,1) AND iah.attribute_key = 'birthplace' ) OR ( iah.change_type IN (2) AND iah.attribute_key = 'mail' ) )
                sqlBuilder.append( " AND " ).append( fullAttributesFilters.stream( ).collect( Collectors.joining( " OR ", "(", ")" ) ) );
            }
        }
        sqlBuilder.append( " ORDER BY i.last_update_date DESC " );
        if ( max != null && max > 0 )
        {
            sqlBuilder.append( " LIMIT " ).append( max );
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<UpdatedIdentityDto> selectUpdatedFromIds( final List<Integer> identityIds, final Plugin plugin )
    {
        final List<UpdatedIdentityDto> list = new ArrayList<>( );
        final String sql = SQL_QUERY_SELECT_UPDATED_IDENTITIES_FROM_IDS.replace( "${identity_id_list}",
                identityIds.stream( ).map( Object::toString ).collect( Collectors.joining( ", " ) ) );

        try ( final DAOUtil daoUtil = new DAOUtil( sql, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final UpdatedIdentityDto updatedIdentity = new UpdatedIdentityDto( );
                int nIndex = 1;
                updatedIdentity.setCustomerId( daoUtil.getString( nIndex++ ) );
                updatedIdentity.setModificationDate( daoUtil.getTimestamp( nIndex ) );
                list.add( updatedIdentity );
            }
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IdentityChange> selectIdentityHistoryBySearchParameters( final String customerId, final String clientCode, final String authorName,
            final IdentityChangeType changeType, final String changeStatus, final String authorType, final Date modificationDate, final Map<String, String> metadata, final Integer nbDaysFrom,
            final Pair<Date, Date> modificationDateInterval, final Plugin plugin, final int nMaxNbIdentityReturned ) throws IdentityStoreException
    {
        final List<IdentityChange> identityChanges = new ArrayList<>( );
        // ${client_code} AND ${customer_id} AND ${author_name} AND ${change_type} AND ${modification_date} AND ${metadata} AND ${modification_date} AND
        // ${change_status} AND ${nbDaysFrom} AND ${author_type}
        String sql = SQL_QUERY_SEARCH_IDENTITY_HISTORY
                .replace( "${client_code}", ( StringUtils.isNotBlank( clientCode ) ? "LOWER(client_code) = '" + StringUtils.lowerCase(clientCode) + "'" : "1=1" ) )
                .replace( "${customer_id}", ( StringUtils.isNotBlank( customerId ) ? "LOWER(customer_id) = '" + StringUtils.lowerCase(customerId) + "'" : "1=1" ) )
                .replace( "${author_name}", ( StringUtils.isNotBlank( authorName ) ? "LOWER(author_name) = '" + StringUtils.lowerCase(authorName.toLowerCase( )) + "'" : "1=1" ) )
                .replace( "${change_type}", ( changeType != null ? "change_type = " + changeType.getValue( ) : "1=1" ) )
                .replace( "${change_status}", ( StringUtils.isNotBlank( changeStatus ) ? "change_status = '" + changeStatus + "'" : "1=1" ) )
                .replace( "${author_type}", ( StringUtils.isNotBlank( authorType ) ? "LOWER(author_type) = '" + StringUtils.lowerCase(authorType) + "'" : "1=1" ) )
                .replace( "${metadata}", ( metadata != null && !metadata.isEmpty( ) ? this.computeMetadaQuery( metadata ) : "1=1" ) )
                .replace( "${limit}", String.valueOf( nMaxNbIdentityReturned ) );
        final List<Date> sqlDateParameters = new ArrayList<>( );
        if(modificationDate != null )
        {
            sql = sql.replace( "${modification_date}", "modification_date >= '" + modificationDate +
                "' AND modification_date < '" + DateUtils.addDays(modificationDate, 1) + "'" );
        }
        else
        {
            sql = sql.replace( "${modification_date}",  "1=1" );
        }

        if ( nbDaysFrom != null && nbDaysFrom != 0 )
        {
            sql = sql.replace( "${nbDaysFrom}", "modification_date > now() - interval '" + nbDaysFrom + "' day" );
        }
        else
        {
            if ( modificationDateInterval != null && ( modificationDateInterval.getLeft( ) != null || modificationDateInterval.getRight( ) != null ) )
            {
                final Date dateStart = modificationDateInterval.getLeft( );
                final Date dateEnd = modificationDateInterval.getRight( );
                final List<String> dateStatements = new ArrayList<>( );
                if ( dateStart != null )
                {
                    dateStatements.add( "modification_date > ?" );
                    sqlDateParameters.add( dateStart );
                }
                if ( dateEnd != null )
                {
                    dateStatements.add( "modification_date < ?" );
                    sqlDateParameters.add( dateEnd );
                }
                sql = sql.replace( "${nbDaysFrom}", String.join( " AND ", dateStatements ) );
            }
            else
            {
                sql = sql.replace( "${nbDaysFrom}", "1=1" );
            }
        }

        try ( final DAOUtil daoUtil = new DAOUtil( sql, plugin ) )
        {
            for ( int index = 1; index <= sqlDateParameters.size( ); index++ )
            {
                daoUtil.setTimestamp( index, Timestamp.from( sqlDateParameters.get( index - 1 ).toInstant( ) ) );
            }
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final IdentityChange identityChange = getIdentityChangeFromQuery( daoUtil );
                identityChanges.add( identityChange );
            }
        }
        catch( final JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
        return identityChanges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identity> selectExpiredNotMergedAndNotConnectedIdentities( final int limit, final Plugin plugin )
    {
        final List<Identity> listIdentities = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRED_NOT_MERGED_AND_NOT_CONNECTED, plugin ) )
        {
            daoUtil.setInt( 1, limit );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listIdentities.add( getIdentityFromQuery( daoUtil ) );
            }
        }
        return listIdentities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> selectNotMergedNotConnectedWithNonCertifiedAttributeCustomerIds( final String attributeKey, final int limit, final Plugin plugin )
    {
        final List<String> listCuids = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NOT_MERGED_NOT_CONNECTED_WITH_UNCERTIFIED_ATTRIBUTE, plugin ) )
        {
            daoUtil.setString( 1, attributeKey );
            daoUtil.setString( 2, AppPropertiesService.getProperty("identitystore.identity.uncertify.processus", "DEC") );
            daoUtil.setInt( 3, limit );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listCuids.add( daoUtil.getString( 1 ) );
            }
        }
        return listCuids;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Identity> selectMergedIdentities( final int identityId, final Plugin plugin )
    {
        final List<Identity> listIdentities = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_MERGED_TO, plugin ) )
        {
            daoUtil.setInt( 1, identityId );
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                listIdentities.add( getIdentityFromQuery( daoUtil ) );
            }
        }
        return listIdentities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAttributeHistory( final int identityId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_ATTRIBUTE_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, identityId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp getIdentityLastUpdateDate( final String customerId, final Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LAST_UPDATE_DATE_FROM_CUID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getTimestamp( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountIdentities(Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_IDENTITIES, plugin ) )
        {
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountDeletedIdentities(boolean deleted, Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_DELETED_IDENTITIES, plugin ) )
        {
            daoUtil.setBoolean( 1, deleted );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountMergedIdentities(boolean merged, Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_MERGED_IDENTITIES, plugin ) )
        {
            daoUtil.setBoolean( 1, merged );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountActiveMonParisdentities(boolean monParisActive, Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_MONPARIS_ACTIVE_IDENTITIES, plugin ) )
        {
            daoUtil.setBoolean( 1, monParisActive );
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Integer> getCountAttributesByIdentities(Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_COUNT_ATTRIBUTES_BY_IDENTITY, plugin ) )
        {
            daoUtil.executeQuery( );
            Map<Integer, Integer> attributesByIdentities = new HashMap<>( );
            while ( daoUtil.next( ) )
            {
                attributesByIdentities.put(daoUtil.getInt( 1 ), daoUtil.getInt( 2 ) );
            }
            return attributesByIdentities;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountUnmergedIdentitiesWithoutAttributes(Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_IDENTITIES_NO_ATTRIBUTES_NOT_MERGED, plugin ) )
        {
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountIndexEligibleIdentities(Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_INDEX_ELIGIBLE_IDENTITIES, plugin ) )
        {
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getCountIndexNotEligibleIdentities(Plugin plugin)
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_COUNT_INDEX_NOT_ELIGIBLE_IDENTITIES, plugin ) )
        {
            daoUtil.executeQuery( );
            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IndicatorsActionsType> getActionsTypesDuringInterval(int interval, Plugin plugin)
    {

        String sql = SQL_QUERY_SELECT_ACTIONS_ACTIVITIES
                .replace( "${interval}", String.valueOf( interval ) );
        try ( final DAOUtil daoUtil = new DAOUtil( sql, plugin ) )
        {
            daoUtil.executeQuery( );
            List<IndicatorsActionsType> indicatorsList = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                indicatorsList.add(getIndicatorFromQuery(daoUtil));
            }
            return indicatorsList;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getHistoryStatusList( Plugin plugin )
    {
        try( final DAOUtil daoUtil = new DAOUtil(SQL_QUERY_SELECT_STATUS_LIST, plugin))
        {
            daoUtil.executeQuery( );
            List<String> statusList = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                statusList.add(daoUtil.getString( 1 ));
            }
            return statusList;
        }
    }

    public  IndicatorsActionsType getIndicatorFromQuery (DAOUtil daoUtil)
    {
        IndicatorsActionsType indicatorsActionsType = new IndicatorsActionsType( );
        int nIndex = 1;

        indicatorsActionsType.setChangeType(daoUtil.getInt( nIndex++ ) );
        indicatorsActionsType.setChangeStatus(daoUtil.getString(nIndex++));
        indicatorsActionsType.setAuthorType(daoUtil.getString(nIndex++));
        indicatorsActionsType.setClientCode(daoUtil.getString(nIndex++));
        indicatorsActionsType.setCountActions(daoUtil.getInt(nIndex));

        return indicatorsActionsType;
    }

    private String computeMetadaQuery( Map<String, String> metadata )
    {
        // where string_to_array(metadata ->> 'duplicate_rule_code',',') @> '{RG_GEN_SuspectDoublon_03}';
        return metadata.entrySet( ).stream( ).map( entry -> "string_to_array(metadata ->> '" + entry.getKey( ) + "',',') @>'{" + entry.getValue( ) + "}'" )
                .collect( Collectors.joining( " AND " ) );
    }

}

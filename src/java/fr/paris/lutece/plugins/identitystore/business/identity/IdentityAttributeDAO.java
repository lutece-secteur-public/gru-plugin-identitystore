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
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for IdentityAttribute objects
 */
public final class IdentityAttributeDAO implements IIdentityAttributeDAO
{
    // Constants
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity_attribute ( id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_client ) VALUES ( ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ?";
    private static final String SQL_QUERY_DELETE_ALL_ATTR = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity_attribute SET id_identity = ?, id_attribute = ?, attribute_value = ?, id_certification = ?, id_file = ?, lastupdate_date = CURRENT_TIMESTAMP, lastupdate_client = ? WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_COMMON_SELECT = "SELECT a.id_attribute as id_attribute, a.id_identity as id_identity, a.attribute_value as attribute_value, a.id_certification as id_certification, a.lastupdate_date as lastupdate_date, a.lastupdate_client as lastupdate_client,"
            + " c.certifier_code as certifier_code, c.certificate_date as certificate_date, c.expiration_date as expiration_date, "
            + " r.id_attribute as attribute_key_id, r.key_name as attribute_key_name, r.name as attribute_name, r.description as attribute_description, r.certifiable as certifiable, r.common_search_key as common_search_key, "
            + " r.key_type as attribute_key_type, r.key_weight as attribute_key_weight, r.mandatory_for_creation as mandatory_for_creation, r.pivot as pivot, r.validation_error_message as validation_error_message, r.validation_regex as validation_regex "
            + "FROM identitystore_identity_attribute a "
            + "    LEFT JOIN identitystore_identity_attribute_certificate c ON c.id_attribute_certificate = a.id_certification "
            + "    LEFT JOIN identitystore_ref_attribute r ON r.id_attribute = a.id_attribute ";
    private static final String SQL_QUERY_SELECTALL = SQL_COMMON_SELECT + " WHERE a.id_identity = ? ORDER BY a.id_attribute";
    private static final String SQL_QUERY_SELECT_BY_LIST_IDENTITY = SQL_COMMON_SELECT + " WHERE a.id_identity IN (${list_identity})";

    // Historical
    private static final String SQL_QUERY_INSERT_HISTORY = "INSERT INTO identitystore_identity_attribute_history  "
            + "   (change_type, change_satus, change_message, author_type, author_name, client_code, id_identity, attribute_key, attribute_value, certification_process, certification_date, modification_date, metadata) "
            + "   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE_HISTORY = "SELECT id_history, change_type, change_satus, change_message, author_type, author_name, client_code, id_identity, attribute_key, attribute_value, certification_process, certification_date, modification_date, CAST( metadata AS VARCHAR ) FROM identitystore_identity_attribute_history WHERE id_identity = ? ORDER BY modification_date DESC";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE_HISTORY_BY_CUSTOMER_ID = "SELECT a.id_history, a.change_type, a.change_satus, a.change_message, a.author_type, a.author_name, a.client_code, a.id_identity, a.attribute_key, a.attribute_value, a.certification_process, a.certification_date, a.modification_date, CAST( a.metadata AS VARCHAR ) FROM identitystore_identity_attribute_history a JOIN identitystore_identity i ON a.id_identity = i.id_identity WHERE i.customer_id = ? ORDER BY a.modification_date DESC";
    private static final String SQL_QUERY_DELETE_ALL_HISTORY = "DELETE FROM identitystore_identity_attribute_history  WHERE id_identity = ?";

    private final ObjectMapper objectMapper = new ObjectMapper( );

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( IdentityAttribute identityAttribute, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
            daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
            daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
            daoUtil.setString( nIndex, identityAttribute.getLastUpdateClientCode( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        // FIXME Delete also the attribute history

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.setInt( 2, nAttributeId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( IdentityAttribute identityAttribute, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
            daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
            daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
            daoUtil.setString( nIndex++, identityAttribute.getLastUpdateClientCode( ) );
            daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
            daoUtil.setInt( nIndex, identityAttribute.getAttributeKey( ).getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, Plugin plugin )
    {
        final Map<String, IdentityAttribute> attributesMap = new LinkedHashMap<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final IdentityAttribute identityAttribute = this.getIdentityAttribute( daoUtil );
                attributesMap.put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );
            }

            return attributesMap;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityAttribute> selectAllAttributesByIdentityList( List<Identity> listIdentity, Plugin plugin )
    {
        final List<IdentityAttribute> listIdentityAttributes = new ArrayList<>( );

        if ( listIdentity == null || listIdentity.isEmpty( ) )
        {
            return listIdentityAttributes;
        }

        final String strSQL = SQL_QUERY_SELECT_BY_LIST_IDENTITY.replace( "${list_identity}",
                listIdentity.stream( ).map( i -> String.valueOf( i.getId( ) ) ).collect( Collectors.joining( "," ) ) );

        try ( final DAOUtil daoUtil = new DAOUtil( strSQL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIdentityAttributes.add( this.getIdentityAttribute( daoUtil ) );
            }

            return listIdentityAttributes;
        }
    }

    /**
     * Return true if the attribute certificate is expired or false if not
     * 
     * @param attribute
     * @return true if the identity attribute is not null and expired
     */
    private boolean isCertificateExpired( IdentityAttribute attribute )
    {
        return attribute.getCertificate( ) != null && attribute.getCertificate( ).getExpirationDate( ) != null
                && attribute.getCertificate( ).getExpirationDate( ).before( new Date( ) );
    }

    @Override
    public void deleteAllAttributes( int nIdentityId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeUpdate( );
        }

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_ATTR, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public synchronized void addAttributeChangeHistory( AttributeChange attributeChange, Plugin plugin ) throws IdentityStoreException
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeChange.getChangeType( ).getValue( ) );
            daoUtil.setString( nIndex++, attributeChange.getChangeSatus( ) );
            daoUtil.setString( nIndex++, attributeChange.getChangeMessage( ) );
            daoUtil.setString( nIndex++, attributeChange.getAuthorType( ).name( ) );
            daoUtil.setString( nIndex++, attributeChange.getAuthorName( ) );
            daoUtil.setString( nIndex++, attributeChange.getClientCode( ) );
            daoUtil.setInt( nIndex++, attributeChange.getIdIdentity( ) );
            daoUtil.setString( nIndex++, attributeChange.getAttributeKey( ) );
            daoUtil.setString( nIndex++, attributeChange.getAttributeValue( ) );
            daoUtil.setString( nIndex++, attributeChange.getCertificationProcessus( ) );
            daoUtil.setTimestamp( nIndex++, attributeChange.getCertificationDate( ) );
            daoUtil.setTimestamp( nIndex++, attributeChange.getModificationDate( ) );
            daoUtil.setObject( nIndex, objectMapper.writeValueAsString( attributeChange.getMetadata( ) ), Types.OTHER );

            daoUtil.executeUpdate( );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( int nIdentityId, Plugin plugin ) throws IdentityStoreException
    {
        final List<AttributeChange> listAttributeChange = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE_HISTORY, plugin ) )
        {
            daoUtil.setInt( 1, nIdentityId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listAttributeChange.add( this.getAttributeChange( daoUtil ) );
            }

            return listAttributeChange;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( String customerId, Plugin plugin ) throws IdentityStoreException
    {
        final List<AttributeChange> listAttributeChange = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE_HISTORY_BY_CUSTOMER_ID, plugin ) )
        {
            daoUtil.setString( 1, customerId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listAttributeChange.add( this.getAttributeChange( daoUtil ) );
            }

            return listAttributeChange;
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    public IdentityAttribute getIdentityAttribute( final DAOUtil daoUtil )
    {
        final IdentityAttribute identityAttribute = new IdentityAttribute( );
        identityAttribute.setIdIdentity( daoUtil.getInt( "id_identity" ) );
        identityAttribute.setValue( daoUtil.getString( "attribute_value" ) );
        identityAttribute.setLastUpdateDate( daoUtil.getTimestamp( "lastupdate_date" ) );
        identityAttribute.setLastUpdateClientCode( daoUtil.getString( "lastupdate_client" ) );

        final int nCertificateId = daoUtil.getInt( "id_certification" );
        if ( nCertificateId != 0 )
        {
            final AttributeCertificate certificate = new AttributeCertificate( );
            certificate.setId( nCertificateId );
            certificate.setCertifierCode( daoUtil.getString( "certifier_code" ) );
            certificate.setCertifierName( certificate.getCertifierCode( ) );
            certificate.setCertificateDate( daoUtil.getTimestamp( "certificate_date" ) );
            certificate.setExpirationDate( daoUtil.getTimestamp( "expiration_date" ) );
            identityAttribute.setCertificate( certificate );
        }

        final AttributeKey attributeKey = new AttributeKey( );
        identityAttribute.setAttributeKey( attributeKey );
        attributeKey.setId( daoUtil.getInt( "attribute_key_id" ) );
        attributeKey.setKeyName( daoUtil.getString( "attribute_key_name" ) );
        attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( "attribute_key_type" ) ) );
        attributeKey.setKeyWeight( daoUtil.getInt( "attribute_key_weight" ) );
        attributeKey.setName( daoUtil.getString( "attribute_name" ) );
        attributeKey.setDescription( daoUtil.getString( "attribute_description" ) );
        attributeKey.setCommonSearchKeyName( daoUtil.getString( "common_search_key" ) );
        attributeKey.setValidationErrorMessage( daoUtil.getString( "validation_error_message" ) );
        attributeKey.setValidationRegex( daoUtil.getString( "validation_regex" ) );
        attributeKey.setPivot( daoUtil.getBoolean( "pivot" ) );
        attributeKey.setCertifiable( daoUtil.getBoolean( "certifiable" ) );
        attributeKey.setMandatoryForCreation( daoUtil.getBoolean( "mandatory_for_creation" ) );
        return identityAttribute;
    }

    public AttributeChange getAttributeChange( final DAOUtil daoUtil ) throws JsonProcessingException
    {
        final AttributeChange attributeChange = new AttributeChange( );
        int nIndex = 1;
        attributeChange.setId( daoUtil.getInt( nIndex++ ) );
        attributeChange.setChangeType( AttributeChangeType.valueOf( daoUtil.getInt( nIndex++ ) ) );
        attributeChange.setChangeSatus( daoUtil.getString( nIndex++ ) );
        attributeChange.setChangeMessage( daoUtil.getString( nIndex++ ) );
        attributeChange.setAuthorType( AuthorType.valueOf( daoUtil.getString( nIndex++ ) ) );
        attributeChange.setAuthorName( daoUtil.getString( nIndex++ ) );
        attributeChange.setClientCode( daoUtil.getString( nIndex++ ) );
        attributeChange.setIdIdentity( daoUtil.getInt( nIndex++ ) );
        attributeChange.setAttributeKey( daoUtil.getString( nIndex++ ) );
        attributeChange.setAttributeValue( daoUtil.getString( nIndex++ ) );
        attributeChange.setCertificationProcessus( daoUtil.getString( nIndex++ ) );
        attributeChange.setCertificationDate( daoUtil.getTimestamp( nIndex++ ) );
        attributeChange.setModificationDate( daoUtil.getTimestamp( nIndex++ ) );
        final String jsonMap = daoUtil.getString( nIndex );
        if ( StringUtils.isNotEmpty( jsonMap ) )
        {
            final Map<String, String> mapMetaData = objectMapper.readValue( jsonMap, new TypeReference<Map<String, String>>( )
            {
            } );
            attributeChange.getMetadata( ).clear( );
            if ( mapMetaData != null && !mapMetaData.isEmpty( ) )
            {
                attributeChange.getMetadata( ).putAll( mapMetaData );
            }
        }
        return attributeChange;
    }
}

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
package fr.paris.lutece.plugins.identitystore.business.identity;

import fr.paris.lutece.plugins.identitystore.business.attribute.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides Data Access methods for IdentityAttribute objects
 */
public final class IdentityAttributeDAO implements IIdentityAttributeDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_date, lastupdate_application FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_identity_attribute ( id_identity, id_attribute, attribute_value, id_certification, id_file, lastupdate_application ) VALUES ( ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ? AND id_attribute = ?";
    private static final String SQL_QUERY_DELETE_ALL_ATTR = "DELETE FROM identitystore_identity_attribute WHERE id_identity = ?";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_identity_attribute SET id_identity = ?, id_attribute = ?, attribute_value = ?, id_certification = ?, id_file = ?, lastupdate_date = CURRENT_TIMESTAMP, lastupdate_application = ? WHERE id_identity = ? AND id_attribute = ? ";
    private static final String SQL_QUERY_SELECTALL = "SELECT a.id_attribute, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_application "
            + " FROM identitystore_identity_attribute a" + " WHERE a.id_identity = ? ORDER BY a.id_attribute";
    private static final String SQL_QUERY_SELECT_BY_CLIENT_APP_CODE = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_application "
            + " FROM identitystore_identity_attribute a , identitystore_attribute b, identitystore_attribute_right c, identitystore_client_application d "
            + " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute AND c.id_attribute = a.id_attribute AND d.code = ? AND c.id_client_app = d.id_client_app and c.readable = 1";
    private static final String SQL_QUERY_SELECT_BY_KEY_AND_CLIENT_APP_CODE = "SELECT a.id_attribute, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_application "
            + " FROM identitystore_identity_attribute a , identitystore_attribute b, identitystore_attribute_right c, identitystore_client_application d "
            + " WHERE a.id_identity = ? AND a.id_attribute = b.id_attribute AND c.id_attribute = a.id_attribute AND d.code = ? AND c.id_client_app = d.id_client_app and c.readable = 1 and b.key_name = ?";
    private static final String SQL_QUERY_SELECT_BY_LIST_IDENTITY_AND_LIST_KEY_AND_CLIENT_APP_CODE = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.id_identity, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_application"
            + " FROM identitystore_identity_attribute a, identitystore_attribute b"
            + " WHERE a.id_attribute = b.id_attribute AND a.id_identity IN (${list_identity}) ${filter_attribute_key_names}";

    private static final String SQL_QUERY_SELECT_BY_LIST_IDENTITY = "SELECT a.id_attribute, b.name, b.key_name, b.description, b.key_type, a.id_identity, a.attribute_value, a.id_certification, a.id_file, a.lastupdate_date, a.lastupdate_application"
            + " FROM identitystore_identity_attribute a, identitystore_attribute b"
            + " WHERE a.id_attribute = b.id_attribute AND a.id_identity IN (${list_identity})";
    private static final String SQL_FILTER_ATTRIBUTE_KEY_NAMES = "AND b.key_name IN (${list_attribute_key_names})";
    // Historical
    private static final String SQL_QUERY_INSERT_HISTORY = "INSERT INTO identitystore_history_identity_attribute "
            + "   (change_type, change_satus, change_message, author_type, author_name, client_code, id_identity, attribute_key, attribute_value, certification_process, certification_date, modification_date) "
            + "   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_QUERY_SELECT_ATTRIBUTE_HISTORY = "SELECT id_history," + "       change_type, " + "       change_satus, "
            + "       change_message, " + "       author_type, " + "       author_name, " + "       client_code, " + "       id_identity, "
            + "       attribute_key, " + "       attribute_value, " + "       certification_process, " + "       certification_date, "
            + "       modification_date " + "FROM identitystore_history_identity_attribute " + "WHERE attribute_key = ? " + "  AND id_identity = ? "
            + "ORDER BY modification_date DESC";
    private static final String SQL_QUERY_GRU_CERTIFIER_ID = "SELECT id_history FROM identitystore_history_identity_attribute WHERE certifier_name = ? AND identity_connection_id = ? ORDER BY modification_date DESC LIMIT 1";
    private static final String SQL_QUERY_DELETE_ALL_HISTORY = "DELETE FROM identitystore_history_identity_attribute WHERE id_identity = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
        daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
        daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
        daoUtil.setString( nIndex++, identityAttribute.getLastUpdateApplicationCode( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityAttribute load( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setInt( 2, nAttributeId );
        daoUtil.executeQuery( );

        IdentityAttribute identityAttribute = null;
        int nAttributeKey = -1;

        if ( daoUtil.next( ) )
        {
            identityAttribute = new IdentityAttribute( );

            int nIndex = 1;

            identityAttribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
            nAttributeKey = daoUtil.getInt( nIndex++ );
            identityAttribute.setValue( daoUtil.getString( nIndex++ ) );
            identityAttribute.setIdCertificate( daoUtil.getInt( nIndex++ ) );

            int nIdFile = daoUtil.getInt( nIndex++ );

            if ( nIdFile > 0 )
            {
                identityAttribute.setFile( FileHome.findByPrimaryKey( nIdFile ) );
            }

            identityAttribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            identityAttribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );

        }
        daoUtil.free( );

        if ( identityAttribute != null )
        {
            identityAttribute.setAttributeKey( AttributeKeyHome.findByPrimaryKey( nAttributeKey ) );
        }

        return identityAttribute;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nIdentityId, int nAttributeId, Plugin plugin )
    {
        // FIXME Delete also the attribute history

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setInt( 2, nAttributeId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( IdentityAttribute identityAttribute, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getAttributeKey( ).getId( ) );
        daoUtil.setString( nIndex++, identityAttribute.getValue( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdCertificate( ) );
        daoUtil.setInt( nIndex++, ( identityAttribute.getFile( ) != null ) ? identityAttribute.getFile( ).getIdFile( ) : 0 );
        daoUtil.setString( nIndex++, identityAttribute.getLastUpdateApplicationCode( ) );
        daoUtil.setInt( nIndex++, identityAttribute.getIdIdentity( ) );
        daoUtil.setInt( nIndex, identityAttribute.getAttributeKey( ).getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, Plugin plugin )
    {
        Map<String, IdentityAttribute> attributesMap = new LinkedHashMap<String, IdentityAttribute>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute identityAttribute = new IdentityAttribute( );
            int nIndex = 1;

            AttributeKey attribute = AttributeKeyHome.findByPrimaryKey( daoUtil.getInt( nIndex++ ) );

            identityAttribute.setAttributeKey( attribute );
            identityAttribute.setIdIdentity( nIdentityId );
            identityAttribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            identityAttribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            identityAttribute.setFile( attrFile );

            identityAttribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            identityAttribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );

            attributesMap.put( attribute.getKeyName( ), identityAttribute );
        }
        daoUtil.free( );

        for ( IdentityAttribute attribute : attributesMap.values( ) )
        {
            if ( attribute.getCertificate( ) != null )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                if ( isCertificateExpired( attribute ) )
                {
                    attribute.setCertificate( null );
                }
            }
            if ( attribute.getFile( ) != null )
            {
                attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
            }
        }

        return attributesMap;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, IdentityAttribute> selectAttributes( int nIdentityId, String strApplicationCode, Plugin plugin )
    {
        Map<String, IdentityAttribute> attributesMap = new HashMap<String, IdentityAttribute>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CLIENT_APP_CODE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setString( 2, strApplicationCode );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute attribute = new IdentityAttribute( );
            AttributeKey attributeKey = new AttributeKey( );

            int nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

            attribute.setAttributeKey( attributeKey );
            attribute.setIdIdentity( nIdentityId );
            attribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            attribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            attribute.setFile( attrFile );

            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );
            attributesMap.put( attributeKey.getKeyName( ), attribute );
        }
        daoUtil.free( );

        for ( IdentityAttribute attribute : attributesMap.values( ) )
        {
            if ( attribute.getCertificate( ) != null )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                if ( isCertificateExpired( attribute ) )
                {
                    attribute.setCertificate( null );
                }
            }
            if ( attribute.getFile( ) != null )
            {
                attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
            }
        }

        return attributesMap;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityAttribute> selectAttributesByIdentityList( List<Identity> listIdentity, List<String> listAttributeKeyNames, String strApplicationCode,
            Plugin plugin )
    {
        List<IdentityAttribute> listIdentityAttributes = new ArrayList<>( );

        if ( listIdentity == null || listIdentity.isEmpty( ) )
        {
            return listIdentityAttributes;
        }

        List<String> listIn = new ArrayList<>( );

        for ( int i = 0; i < listIdentity.size( ); i++ )
        {
            listIn.add( "?" );
        }

        String strSQL = SQL_QUERY_SELECT_BY_LIST_IDENTITY_AND_LIST_KEY_AND_CLIENT_APP_CODE.replace( "${list_identity}", String.join( ", ", listIn ) );

        String strFilterAttributeKeyNames = "";
        if ( listAttributeKeyNames != null && !listAttributeKeyNames.isEmpty( ) )
        {
            listIn = new ArrayList<>( );

            for ( int i = 0; i < listAttributeKeyNames.size( ); i++ )
            {
                listIn.add( "?" );
            }

            strFilterAttributeKeyNames = SQL_FILTER_ATTRIBUTE_KEY_NAMES.replace( "${list_attribute_key_names}", String.join( ", ", listIn ) );
        }

        strSQL = strSQL.replace( "${filter_attribute_key_names}", strFilterAttributeKeyNames );

        DAOUtil daoUtil = new DAOUtil( strSQL, plugin );
        int nIndex = 1;

        for ( Identity identity : listIdentity )
        {
            daoUtil.setInt( nIndex++, identity.getId( ) );
        }

        if ( listAttributeKeyNames != null && !listAttributeKeyNames.isEmpty( ) )
        {
            for ( String strAttributeKeyName : listAttributeKeyNames )
            {
                daoUtil.setString( nIndex++, strAttributeKeyName );
            }
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute attribute = new IdentityAttribute( );
            AttributeKey attributeKey = new AttributeKey( );

            nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

            attribute.setAttributeKey( attributeKey );
            attribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
            attribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            attribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            attribute.setFile( attrFile );

            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );
            listIdentityAttributes.add( attribute );
        }
        daoUtil.free( );

        for ( IdentityAttribute attribute : listIdentityAttributes )
        {
            if ( attribute.getCertificate( ) != null )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                if ( isCertificateExpired( attribute ) )
                {
                    attribute.setCertificate( null );
                }
            }
            if ( attribute.getFile( ) != null )
            {
                attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
            }
        }

        return listIdentityAttributes;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityAttribute> selectAllAttributesByIdentityList( List<Identity> listIdentity, Plugin plugin )
    {
        List<IdentityAttribute> listIdentityAttributes = new ArrayList<>( );

        if ( listIdentity == null || listIdentity.isEmpty( ) )
        {
            return listIdentityAttributes;
        }

        List<String> listIn = new ArrayList<>( );

        for ( int i = 0; i < listIdentity.size( ); i++ )
        {
            listIn.add( "?" );
        }

        String strSQL = SQL_QUERY_SELECT_BY_LIST_IDENTITY.replace( "${list_identity}", String.join( ", ", listIn ) );

        DAOUtil daoUtil = new DAOUtil( strSQL, plugin );
        int nIndex = 1;

        for ( Identity identity : listIdentity )
        {
            daoUtil.setInt( nIndex++, identity.getId( ) );
        }

        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            IdentityAttribute attribute = new IdentityAttribute( );
            AttributeKey attributeKey = new AttributeKey( );

            nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

            attribute.setAttributeKey( attributeKey );
            attribute.setIdIdentity( daoUtil.getInt( nIndex++ ) );
            attribute.setValue( daoUtil.getString( nIndex++ ) );

            int nCertificateId = daoUtil.getInt( nIndex++ );
            AttributeCertificate certificate = null;
            if ( nCertificateId != 0 )
            {
                certificate = new AttributeCertificate( );
                certificate.setId( nCertificateId );
            }
            attribute.setCertificate( certificate );

            int nIdFile = daoUtil.getInt( nIndex++ );
            File attrFile = null;
            if ( nIdFile > 0 )
            {
                attrFile = new File( );
                attrFile.setIdFile( nIdFile );
            }
            attribute.setFile( attrFile );

            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );
            listIdentityAttributes.add( attribute );
        }
        daoUtil.free( );

        for ( IdentityAttribute attribute : listIdentityAttributes )
        {
            if ( attribute.getCertificate( ) != null )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( attribute.getCertificate( ).getId( ) ) );

                if ( isCertificateExpired( attribute ) )
                {
                    attribute.setCertificate( null );
                }
            }
            if ( attribute.getFile( ) != null )
            {
                attribute.setFile( FileHome.findByPrimaryKey( attribute.getFile( ).getIdFile( ) ) );
            }
        }

        return listIdentityAttributes;
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

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectIdentityAttributesReferenceList( Plugin plugin )
    {
        ReferenceList identityAttributeList = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            identityAttributeList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return identityAttributeList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityAttribute selectAttribute( int nIdentityId, String strAttributeKey, String strApplicationCode, Plugin plugin )
    {
        IdentityAttribute attribute = null;
        int nAttrKey = -1, nCertificateId = -1, nIdFile = -1;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_KEY_AND_CLIENT_APP_CODE, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.setString( 2, strApplicationCode );
        daoUtil.setString( 3, strAttributeKey );
        daoUtil.executeQuery( );

        if ( daoUtil.next( ) )
        {
            attribute = new IdentityAttribute( );

            int nIndex = 1;
            nAttrKey = daoUtil.getInt( nIndex++ );
            attribute.setValue( daoUtil.getString( nIndex++ ) );
            nCertificateId = daoUtil.getInt( nIndex++ );
            nIdFile = daoUtil.getInt( nIndex++ );
            attribute.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );
            attribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );
            attribute.setIdIdentity( nIdentityId );
        }
        daoUtil.free( );

        if ( attribute != null )
        {
            attribute.setAttributeKey( AttributeKeyHome.findByPrimaryKey( nAttrKey ) );
            if ( nCertificateId > 0 )
            {
                attribute.setCertificate( AttributeCertificateHome.findByPrimaryKey( nCertificateId ) );
                if ( isCertificateExpired( attribute ) )
                {
                    attribute.setCertificate( null );
                }
            }
            if ( nIdFile > 0 )
            {
                attribute.setFile( FileHome.findByPrimaryKey( nIdFile ) );
            }
        }

        return attribute;
    }

    @Override
    public void deleteAllAttributes( int nIdentityId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_HISTORY, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeUpdate( );
        daoUtil.free( );

        daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_ATTR, plugin );
        daoUtil.setInt( 1, nIdentityId );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public synchronized void addAttributeChangeHistory( AttributeChange attributeChange, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_HISTORY, Statement.RETURN_GENERATED_KEYS, plugin );
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

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    @Override
    public List<AttributeChange> getAttributeChangeHistory( int nIdentityId, String strAttributeKey, Plugin plugin )
    {
        final List<AttributeChange> listAttributeChange = new ArrayList<>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ATTRIBUTE_HISTORY, plugin );
        daoUtil.setString( 1, strAttributeKey );
        daoUtil.setInt( 2, nIdentityId );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AttributeChange attributeChange = new AttributeChange( );
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
            listAttributeChange.add( attributeChange );
        }

        daoUtil.free( );

        return listAttributeChange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLastIdHistory( String strConnectionId, String strCertifierCode, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_GRU_CERTIFIER_ID, plugin );
        daoUtil.setString( 1, strCertifierCode );
        daoUtil.setString( 2, strConnectionId );
        daoUtil.executeQuery( );
        int nIdHistory = -1;
        if ( daoUtil.next( ) )
        {
            nIdHistory = daoUtil.getInt( 1 );
        }
        daoUtil.free( );

        return nIdHistory;
    }

}

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
package fr.paris.lutece.plugins.identitystore.business.attribute;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for AttributeKey objects
 */
public final class AttributeKeyDAO implements IAttributeKeyDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_attribute ) FROM identitystore_ref_attribute";
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute, name, key_name, common_search_key, description, key_type, certifiable, pivot, key_weight, mandatory_for_creation, validation_regex, validation_error_message, validation_error_message_key FROM identitystore_ref_attribute WHERE id_attribute = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_ref_attribute ( id_attribute, name, key_name, common_search_key, description, key_type, certifiable, pivot, key_weight, validation_regex, validation_error_message, validation_error_message_key, creation_date, last_update_date, author_name ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_INSERT_VALUE = "INSERT INTO identitystore_ref_attribute_values(id_attribute, value, label) VALUES (?,?,?)";
    private static final String SQL_QUERY_DELETE_VALUES = "DELETE FROM identitystore_ref_attribute_values WHERE id_attribute = ?";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_ref_attribute WHERE id_attribute = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_ref_attribute SET id_attribute = ?, name = ?, key_name = ?, common_search_key = ?, description = ?, key_type = ?, certifiable = ?, pivot = ?, key_weight = ?, mandatory_for_creation = ?, validation_regex = ?, validation_error_message = ?, validation_error_message_key, last_update_date = ?, author_name = ? = ? WHERE id_attribute = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute, name, key_name, common_search_key, description, key_type, certifiable, pivot, key_weight, mandatory_for_creation, validation_regex, validation_error_message, validation_error_message_key FROM identitystore_ref_attribute";
    private static final String SQL_QUERY_SELECTALL_KEYS = "SELECT key_name FROM identitystore_ref_attribute";
    private static final String SQL_QUERY_SELECT_BY_KEY = "SELECT id_attribute, name, key_name, common_search_key, description, key_type, certifiable, pivot, key_weight, mandatory_for_creation, validation_regex, validation_error_message, validation_error_message_key FROM identitystore_ref_attribute WHERE key_name = ?";
    private static final String SQL_QUERY_SELECT_NB_ATTRIBUTE_ID_USED = "SELECT count(*) FROM identitystore_ref_attribute WHERE id_attribute = ? AND ( EXISTS( SELECT id_attribute FROM identitystore_service_contract_attribute_right WHERE id_attribute = ? ) OR EXISTS( SELECT id_attribute FROM identitystore_identity_attribute WHERE id_attribute = ? ) OR EXISTS( SELECT id_attribute FROM identitystore_identity_attribute_history  WHERE attribute_key IN  ( SELECT key_name FROM identitystore_ref_attribute WHERE id_attribute = ? ) ) )";
    private static final String SQL_QUERY_SELECT_LEVEL_MAX = "WITH attributes AS ( SELECT ia.key_name, ia.key_weight, max(cast(ircl.level AS NUMERIC)) as max_level FROM identitystore_ref_attribute ia JOIN identitystore_ref_certification_attribute_level iracl ON ia.id_attribute = iracl.id_attribute JOIN identitystore_ref_certification_level ircl ON iracl.id_ref_certification_level = ircl.id_ref_certification_level WHERE ia.key_weight != 0 GROUP BY ia.key_name, ia.key_weight ) SELECT SUM(attributes.max_level * attributes.key_weight) FROM attributes";
    private static final String SQL_QUERY_SELECTALL_MANDATORY = "SELECT id_attribute, name, key_name, common_search_key, description, key_type, certifiable, pivot, key_weight, mandatory_for_creation, validation_regex, validation_error_message, validation_error_message_key FROM identitystore_ref_attribute WHERE mandatory_for_creation = 1";
    private static final String SQL_QUERY_SELECTALL_ATTRIBUTE_VALUES = "SELECT id_attribute, value, label FROM identitystore_ref_attribute_values WHERE id_attribute = ?";

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            The Plugin
     * @return The new primary key
     */
    private synchronized int newPrimaryKey( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin ) )
        {
            daoUtil.executeQuery( );

            int nKey = 1;

            if ( daoUtil.next( ) )
            {
                nKey = daoUtil.getInt( 1 ) + 1;
            }

            return nKey;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeKey attributeKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            attributeKey.setId( newPrimaryKey( plugin ) );

            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeKey.getId( ) );
            daoUtil.setString( nIndex++, attributeKey.getName( ) );
            daoUtil.setString( nIndex++, attributeKey.getKeyName( ) );
            daoUtil.setString( nIndex++, attributeKey.getCommonSearchKeyName( ) );
            daoUtil.setString( nIndex++, attributeKey.getDescription( ) );
            daoUtil.setInt( nIndex++, attributeKey.getKeyType( ).getId( ) );
            daoUtil.setBoolean( nIndex++, attributeKey.getCertifiable( ) );
            daoUtil.setBoolean( nIndex++, attributeKey.getPivot( ) );
            daoUtil.setInt( nIndex++, attributeKey.getKeyWeight( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationRegex( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationErrorMessage( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationErrorMessageKey( ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setString( nIndex,  attributeKey.getAuthorName( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeKey load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            AttributeKey attributeKey = null;

            if ( daoUtil.next( ) )
            {
                attributeKey = new AttributeKey( );

                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setCommonSearchKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
                attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setPivot( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setKeyWeight( daoUtil.getInt( nIndex++ ) );
                attributeKey.setMandatoryForCreation( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setValidationRegex( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessage( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessageKey( daoUtil.getString( nIndex ) );
            }

            return attributeKey;
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
    public void store( AttributeKey attributeKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeKey.getId( ) );
            daoUtil.setString( nIndex++, attributeKey.getName( ) );
            daoUtil.setString( nIndex++, attributeKey.getKeyName( ) );
            daoUtil.setString( nIndex++, attributeKey.getCommonSearchKeyName( ) );
            daoUtil.setString( nIndex++, attributeKey.getDescription( ) );
            daoUtil.setInt( nIndex++, attributeKey.getKeyType( ).getId( ) );
            daoUtil.setBoolean( nIndex++, attributeKey.getCertifiable( ) );
            daoUtil.setBoolean( nIndex++, attributeKey.getPivot( ) );
            daoUtil.setInt( nIndex++, attributeKey.getKeyWeight( ) );
            daoUtil.setBoolean( nIndex++, attributeKey.isMandatoryForCreation( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationRegex( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationErrorMessage( ) );
            daoUtil.setString( nIndex++, attributeKey.getValidationErrorMessageKey( ) );
            daoUtil.setTimestamp( nIndex++, new Timestamp( new java.util.Date( ).getTime( ) ) );
            daoUtil.setString( nIndex++, attributeKey.getAuthorName( ) );
            daoUtil.setInt( nIndex, attributeKey.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeKey> selectAttributeKeysList( Plugin plugin )
    {

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );
            final List<AttributeKey> attributeKeyList = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                final AttributeKey attributeKey = new AttributeKey( );
                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setCommonSearchKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
                attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setPivot( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setKeyWeight( daoUtil.getInt( nIndex++ ) );
                attributeKey.setMandatoryForCreation( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setValidationRegex( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessage( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessageKey( daoUtil.getString( nIndex ) );

                attributeKeyList.add( attributeKey );
            }

            return attributeKeyList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<String> selectAttributeKeysNamesList( Plugin plugin )
    {

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_KEYS, plugin ) )
        {
            daoUtil.executeQuery( );
            final List<String> attributeNamesList = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                attributeNamesList.add(daoUtil.getString( 1 ) );
            }

            return attributeNamesList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeKeysReferenceList( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );
            final ReferenceList attributeKeyList = new ReferenceList( );
            while ( daoUtil.next( ) )
            {
                attributeKeyList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return attributeKeyList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeKey selectByKey( String strKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_KEY, plugin ) )
        {
            daoUtil.setString( 1, strKey );
            daoUtil.executeQuery( );

            AttributeKey attributeKey = null;

            if ( daoUtil.next( ) )
            {
                attributeKey = new AttributeKey( );

                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setCommonSearchKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
                attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setPivot( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setKeyWeight( daoUtil.getInt( nIndex++ ) );
                attributeKey.setMandatoryForCreation( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setValidationRegex( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessage( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessageKey( daoUtil.getString( nIndex ) );
            }

            return attributeKey;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkAttributeId( int nAttributeId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NB_ATTRIBUTE_ID_USED, plugin ) )
        {
            daoUtil.setInt( 1, nAttributeId );
            daoUtil.setInt( 2, nAttributeId );
            daoUtil.setInt( 3, nAttributeId );
            daoUtil.setInt( 4, nAttributeId );
            daoUtil.executeQuery( );

            int nCount = 0;

            if ( daoUtil.next( ) )
            {
                nCount = daoUtil.getInt( 1 );
            }

            return !( nCount == 0 );
        }
    }

    @Override
    public Integer selectQualityBaseFactor( Plugin plugin )
    {

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LEVEL_MAX, plugin ) )
        {
            int base = 0;
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                base = daoUtil.getInt( 1 );
            }

            return base;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AttributeKey> selectMandatoryForCreationAttributeKeyList( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_MANDATORY, plugin ) )
        {
            daoUtil.executeQuery( );
            final List<AttributeKey> attributeKeyList = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                final AttributeKey attributeKey = new AttributeKey( );
                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setCommonSearchKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
                attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setPivot( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setKeyWeight( daoUtil.getInt( nIndex++ ) );
                attributeKey.setMandatoryForCreation( daoUtil.getBoolean( nIndex++ ) );
                attributeKey.setValidationRegex( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessage( daoUtil.getString( nIndex++ ) );
                attributeKey.setValidationErrorMessageKey( daoUtil.getString( nIndex ) );

                attributeKeyList.add( attributeKey );
            }

            return attributeKeyList;
        }
    }

    @Override
    public List<AttributeValue> loadAttributeValues( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ATTRIBUTE_VALUES, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            final List<AttributeValue> attributeValues = new ArrayList<>( );
            while ( daoUtil.next( ) )
            {
                final AttributeValue attributeValue = new AttributeValue( );
                int nIndex = 1;

                attributeValue.setAttributeId( daoUtil.getInt( nIndex++ ) );
                attributeValue.setValue( daoUtil.getString( nIndex++ ) );
                attributeValue.setLabel( daoUtil.getString( nIndex++ ) );
                attributeValues.add( attributeValue );
            }

            return attributeValues;
        }
    }

    @Override
    public void insertAttributeValues( int nKey, AttributeValue attributeValue, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_VALUE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.setString( 2, attributeValue.getValue( ) );
            daoUtil.setString( 3, attributeValue.getLabel( ) );

            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void removeAttributeValues( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_VALUES, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }
}

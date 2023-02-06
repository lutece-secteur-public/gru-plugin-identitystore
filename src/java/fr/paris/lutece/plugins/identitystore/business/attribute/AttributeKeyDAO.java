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
package fr.paris.lutece.plugins.identitystore.business.attribute;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for AttributeKey objects
 */
public final class AttributeKeyDAO implements IAttributeKeyDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_attribute ) FROM identitystore_attribute";
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute, name, key_name, description, key_type, certifiable, pivot FROM identitystore_attribute WHERE id_attribute = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attribute ( id_attribute, name, key_name, description, key_type, certifiable, pivot ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_attribute WHERE id_attribute = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attribute SET id_attribute = ?, name = ?, key_name = ?, description = ?, key_type = ?, certifiable = ?, pivot = ? WHERE id_attribute = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute, name, key_name, description, key_type, certifiable, pivot FROM identitystore_attribute";
    private static final String SQL_QUERY_SELECT_BY_KEY = "SELECT id_attribute, name, key_name, description, key_type, certifiable, pivot FROM identitystore_attribute WHERE key_name = ?";
    private static final String SQL_QUERY_SELECT_NB_ATTRIBUTE_ID_USED = "SELECT count(*) FROM identitystore_attribute WHERE id_attribute = ? AND ( EXISTS( SELECT id_attribute FROM identitystore_attribute_right WHERE id_attribute = ? ) OR EXISTS( SELECT id_attribute FROM identitystore_identity_attribute WHERE id_attribute = ? ) OR EXISTS( SELECT id_attribute FROM identitystore_history_identity_attribute WHERE attribute_key IN  ( SELECT key_name FROM identitystore_attribute WHERE id_attribute = ? ) ) )";

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            The Plugin
     * @return The new primary key
     */
    private synchronized int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery( );

        int nKey = 1;

        if ( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeKey attributeKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        attributeKey.setId( newPrimaryKey( plugin ) );

        int nIndex = 1;

        daoUtil.setInt( nIndex++, attributeKey.getId( ) );
        daoUtil.setString( nIndex++, attributeKey.getName( ) );
        daoUtil.setString( nIndex++, attributeKey.getKeyName( ) );
        daoUtil.setString( nIndex++, attributeKey.getDescription( ) );
        daoUtil.setInt( nIndex++, attributeKey.getKeyType( ).getId( ) );
        daoUtil.setBoolean( nIndex++, attributeKey.getCertifiable( ) );
        daoUtil.setBoolean( nIndex, attributeKey.getPivot( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeKey load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
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
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
            attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
            attributeKey.setPivot( daoUtil.getBoolean( nIndex ) );
        }

        daoUtil.free( );

        return attributeKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( AttributeKey attributeKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, attributeKey.getId( ) );
        daoUtil.setString( nIndex++, attributeKey.getName( ) );
        daoUtil.setString( nIndex++, attributeKey.getKeyName( ) );
        daoUtil.setString( nIndex++, attributeKey.getDescription( ) );
        daoUtil.setInt( nIndex++, attributeKey.getKeyType( ).getId( ) );
        daoUtil.setInt( nIndex++, attributeKey.getId( ) );
        daoUtil.setBoolean( nIndex++, attributeKey.getCertifiable( ) );
        daoUtil.setBoolean( nIndex, attributeKey.getPivot( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeKey> selectAttributeKeysList( Plugin plugin )
    {
        List<AttributeKey> attributeKeyList = new ArrayList<>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AttributeKey attributeKey = new AttributeKey( );
            int nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
            attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
            attributeKey.setPivot( daoUtil.getBoolean( nIndex ) );

            attributeKeyList.add( attributeKey );
        }

        daoUtil.free( );

        return attributeKeyList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeKeysReferenceList( Plugin plugin )
    {
        ReferenceList attributeKeyList = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            attributeKeyList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return attributeKeyList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AttributeKey selectByKey( String strKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_KEY, plugin );
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
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );
            attributeKey.setCertifiable( daoUtil.getBoolean( nIndex++ ) );
            attributeKey.setPivot( daoUtil.getBoolean( nIndex ) );
        }

        daoUtil.free( );

        return attributeKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkAttributeId( int nAttributeId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_NB_ATTRIBUTE_ID_USED, plugin );
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

        daoUtil.free( );

        return !( nCount == 0 );
    }

}

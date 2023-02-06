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
package fr.paris.lutece.plugins.identitystore.business.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides rights Access methods for Attribute objects
 */
public final class AttributeRightDAO implements IAttributeRightDAO
{
    // Constants
    private static final String SQL_QUERY_DELETE_ALL_BY_CLIENT = "DELETE FROM identitystore_attribute_right WHERE id_service_contract = ? ";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attribute_right ( id_attribute, id_service_contract, searchable, readable, writable ) VALUES ( ?, ?, ?, ?, ? ) ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attribute_right SET readable = ? , writable = ?, searchable = ? WHERE id_attribute = ? AND id_service_contract = ?";
    private static final String SQL_QUERY_SELECT_ALL_BY_CONTRACT = "SELECT a.id_attribute, a.name, a.key_name, a.description, a.key_type, b.searchable, b.readable, b.writable FROM identitystore_attribute a LEFT JOIN  identitystore_attribute_right b ON  a.id_attribute = b.id_attribute AND id_service_contract = ? ";
    private static final String SQL_QUERY_SELECT_ALL_RIGHTS = "SELECT a.key_name, c.name, b.searchable, b.readable, b.writable FROM identitystore_attribute a JOIN identitystore_attribute_right b ON a.id_attribute = b.id_attribute JOIN identitystore_service_contract c ON b.id_service_contract = c.id_service_contact ORDER BY a.key_name, c.name";
    private static final int CONST_INT_TRUE = 1;
    private static final int CONST_INT_FALSE = 0;

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeRight attributeRight, int serviceContractId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );

        int nIndex = 1;
        daoUtil.setInt( nIndex++, attributeRight.getAttributeKey( ).getId( ) );
        daoUtil.setInt( nIndex++, serviceContractId );
        daoUtil.setInt( nIndex++, attributeRight.isSearchable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );
        daoUtil.setInt( nIndex++, attributeRight.isReadable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );
        daoUtil.setInt( nIndex++, attributeRight.isWritable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( AttributeRight attributeRight, int serviceContractId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, attributeRight.isReadable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );
        daoUtil.setInt( nIndex++, attributeRight.isWritable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );
        daoUtil.setInt( nIndex++, attributeRight.isSearchable( ) ? CONST_INT_TRUE : CONST_INT_FALSE );
        daoUtil.setInt( nIndex++, attributeRight.getAttributeKey( ).getId( ) );
        daoUtil.setInt( nIndex++, serviceContractId );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeRight> selectAttributeRights( ServiceContract serviceContract, Plugin plugin )
    {
        List<AttributeRight> lstAttributeRights = new ArrayList<AttributeRight>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_BY_CONTRACT, plugin );
        daoUtil.setInt( 1, serviceContract.getId( ) );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AttributeRight attributeRight = new AttributeRight( );
            AttributeKey attributeKey = new AttributeKey( );

            int nIndex = 1;

            attributeKey.setId( daoUtil.getInt( nIndex++ ) );
            attributeKey.setName( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

            attributeRight.setAttributeKey( attributeKey );
            attributeRight.setSearchable( daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE );
            attributeRight.setReadable( daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE );
            attributeRight.setWritable( daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE );

            lstAttributeRights.add( attributeRight );
        }

        daoUtil.free( );

        return lstAttributeRights;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeAttributeRights( ServiceContract serviceContract, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_BY_CLIENT, plugin );
        daoUtil.setInt( 1, serviceContract.getId( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AttributeApplicationsRight> getAttributeApplicationsRight( Plugin plugin )
    {
        Map<String, AttributeApplicationsRight> mapApplicationsRight = new HashMap<String, AttributeApplicationsRight>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL_RIGHTS, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            int nIndex = 1;

            String strAttributKey = daoUtil.getString( nIndex++ );
            String strApplicationCode = daoUtil.getString( nIndex++ );
            boolean bHasRead = daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE;
            boolean bHasWrite = daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE;
            boolean bHasCertif = daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE;
            boolean bHasSearch = daoUtil.getInt( nIndex++ ) == CONST_INT_TRUE;

            AttributeApplicationsRight attributeApplicationsRight;
            if ( mapApplicationsRight.containsKey( strAttributKey ) )
            {
                attributeApplicationsRight = mapApplicationsRight.get( strAttributKey );
            }
            else
            {
                attributeApplicationsRight = new AttributeApplicationsRight( );
                attributeApplicationsRight.setAttributeKey( strAttributKey );
            }

            if ( bHasRead )
            {
                attributeApplicationsRight.addReadApplication( strApplicationCode );
            }
            if ( bHasWrite )
            {
                attributeApplicationsRight.addWriteApplication( strApplicationCode );
            }
            if ( bHasCertif )
            {
                attributeApplicationsRight.addCertifApplication( strApplicationCode );
            }
            if ( bHasSearch )
            {
                attributeApplicationsRight.addSearchApplication( strApplicationCode );
            }

            mapApplicationsRight.put( strAttributKey, attributeApplicationsRight );
        }

        daoUtil.free( );

        return mapApplicationsRight;
    }
}

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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for AttributeCertification objects
 */
public final class AttributeCertificationDAO implements IAttributeCertificationDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute, id_ref_attribute_certification_processus, id_service_contract  FROM identitystore_attribute_certification WHERE id_attribute_certification = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attribute_certification ( id_attribute, id_ref_attribute_certification_processus, id_service_contract ) VALUES ( ?,?,? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_attribute_certification WHERE id_service_contract = ? ";
    private static final String SQL_QUERY_DELETE_WITH_SERVICE_CONTRACT_ID = "DELETE FROM identitystore_attribute_certification WHERE id_service_contract = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attribute_certification SET id_attribute_certification = ?,  WHERE id_attribute_certification = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute_certification,  FROM identitystore_attribute_certification";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_attribute_certification FROM identitystore_attribute_certification";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT id_attribute_certification, FROM identitystore_attribute_certification WHERE id_attribute_certification IN (  ";

    private static final String SQL_QUERY_SELECTALL_BY_SERVICE_CONTRACT = "SELECT a.id_attribute, a.name, a.key_name, a.description, a.key_type"
            + " FROM identitystore_attribute a"
            + " LEFT JOIN  identitystore_attribute_certification b ON  a.id_attribute = b.id_attribute AND id_service_contract = ?"
            + " LEFT JOIN  identitystore_ref_attribute_certification_processus c ON  c.id_ref_attribute_certification_processus = b.id_ref_attribute_certification_processus";

    private static final String SQL_QUERY_SELECTALL_BY_ATTRIBUTE_CERTIF = "SELECT b.id_ref_attribute_certification_processus, b.label, b.code FROM identitystore_attribute_certification a"
            + " JOIN identitystore_ref_attribute_certification_processus b ON a.id_ref_attribute_certification_processus = b.id_ref_attribute_certification_processus"
            + " WHERE a.id_service_contract = ? AND a.id_attribute = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeCertification attributeCertification, int serviceContractId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            for ( RefAttributeCertificationProcessus processus : attributeCertification.getRefAttributeCertificationProcessus( ) )
            {
                int nIndex = 1;
                daoUtil.setInt( nIndex++, attributeCertification.getAttributeKey( ).getId( ) );
                daoUtil.setInt( nIndex++, processus.getId( ) );
                daoUtil.setInt( nIndex++, serviceContractId );
                daoUtil.executeUpdate( );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<AttributeCertification> load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            AttributeCertification attributeCertification = null;

            if ( daoUtil.next( ) )
            {
                attributeCertification = new AttributeCertification( );
                int nIndex = 1;

                attributeCertification.setId( daoUtil.getInt( nIndex++ ) );
            }

            return Optional.ofNullable( attributeCertification );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteFromServiceContract( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_WITH_SERVICE_CONTRACT_ID, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( AttributeCertification attributeCertification, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeCertification.getId( ) );
            daoUtil.setInt( nIndex, attributeCertification.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeCertification> selectAttributeCertificationsList( Plugin plugin )
    {
        List<AttributeCertification> attributeCertificationList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                AttributeCertification attributeCertification = new AttributeCertification( );
                int nIndex = 1;

                attributeCertification.setId( daoUtil.getInt( nIndex++ ) );

                attributeCertificationList.add( attributeCertification );
            }

            return attributeCertificationList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdAttributeCertificationsList( Plugin plugin )
    {
        List<Integer> attributeCertificationList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                attributeCertificationList.add( daoUtil.getInt( 1 ) );
            }

            return attributeCertificationList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeCertificationsReferenceList( Plugin plugin )
    {
        ReferenceList attributeCertificationList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                attributeCertificationList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return attributeCertificationList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeCertification> selectAttributeCertificationsListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<AttributeCertification> attributeCertificationList = new ArrayList<>( );

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
                    AttributeCertification attributeCertification = new AttributeCertification( );
                    int nIndex = 1;

                    attributeCertification.setId( daoUtil.getInt( nIndex++ ) );

                    attributeCertificationList.add( attributeCertification );
                }

                daoUtil.free( );

            }
        }
        return attributeCertificationList;

    }

    @Override
    public List<AttributeCertification> selectAttributeCertificationListByServiceContract( Plugin plugin, ServiceContract servicecontract )
    {
        List<AttributeCertification> attributeCertifications = new ArrayList<>( );

        final DAOUtil attributesDaoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_SERVICE_CONTRACT, plugin );
        attributesDaoUtil.setInt( 1, servicecontract.getId( ) );
        attributesDaoUtil.executeQuery( );

        while ( attributesDaoUtil.next( ) )
        {
            AttributeCertification attributeCertification = new AttributeCertification( );
            AttributeKey attributeKey = new AttributeKey( );

            int nIndex = 1;

            attributeKey.setId( attributesDaoUtil.getInt( nIndex++ ) );
            attributeKey.setName( attributesDaoUtil.getString( nIndex++ ) );
            attributeKey.setKeyName( attributesDaoUtil.getString( nIndex++ ) );
            attributeKey.setDescription( attributesDaoUtil.getString( nIndex++ ) );
            attributeKey.setKeyType( KeyType.valueOf( attributesDaoUtil.getInt( nIndex++ ) ) );

            attributeCertification.setAttributeKey( attributeKey );

            attributeCertifications.add( attributeCertification );
        }

        attributesDaoUtil.free( );

        for ( final AttributeCertification attributeCertification : attributeCertifications )
        {

            final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_ATTRIBUTE_CERTIF, plugin );
            daoUtil.setInt( 1, servicecontract.getId( ) );
            daoUtil.setInt( 2, attributeCertification.getAttributeKey( ).getId( ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final RefAttributeCertificationProcessus refAttributeCertificationProcessus = new RefAttributeCertificationProcessus( );

                int nIndex = 1;

                refAttributeCertificationProcessus.setId( daoUtil.getInt( nIndex++ ) );
                refAttributeCertificationProcessus.setLabel( daoUtil.getString( nIndex++ ) );
                refAttributeCertificationProcessus.setCode( daoUtil.getString( nIndex++ ) );

                attributeCertification.getRefAttributeCertificationProcessus( ).add( refAttributeCertificationProcessus );
            }

            daoUtil.free( );
        }

        return attributeCertifications;
    }
}

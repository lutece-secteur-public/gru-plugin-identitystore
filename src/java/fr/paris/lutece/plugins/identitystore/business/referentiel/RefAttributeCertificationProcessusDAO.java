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
package fr.paris.lutece.plugins.identitystore.business.referentiel;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for RefAttributeCertificationProcessus objects
 */
public final class RefAttributeCertificationProcessusDAO implements IRefAttributeCertificationProcessusDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_ref_attribute_certification_processus, label, code FROM identitystore_ref_attribute_certification_processus WHERE id_ref_attribute_certification_processus = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_ref_attribute_certification_processus ( label, code ) VALUES ( ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_ref_attribute_certification_processus WHERE id_ref_attribute_certification_processus = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_ref_attribute_certification_processus SET label = ?, code = ?  WHERE id_ref_attribute_certification_processus = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_ref_attribute_certification_processus, label, code  FROM identitystore_ref_attribute_certification_processus";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_ref_attribute_certification_processus FROM identitystore_ref_attribute_certification_processus";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT id_ref_attribute_certification_processus, label, code FROM identitystore_ref_attribute_certification_processus WHERE id_ref_attribute_certification_processus IN (  ";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( RefAttributeCertificationProcessus refAttributeCertificationProcessus, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, refAttributeCertificationProcessus.getLabel( ) );
            daoUtil.setString( nIndex++, refAttributeCertificationProcessus.getCode( ) );
            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                refAttributeCertificationProcessus.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public RefAttributeCertificationProcessus load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            RefAttributeCertificationProcessus refAttributeCertificationProcessus = null;

            if ( daoUtil.next( ) )
            {
                refAttributeCertificationProcessus = new RefAttributeCertificationProcessus( );
                int nIndex = 1;

                refAttributeCertificationProcessus.setId( daoUtil.getInt( nIndex++ ) );
                refAttributeCertificationProcessus.setLabel( daoUtil.getString( nIndex++ ) );
                refAttributeCertificationProcessus.setCode( daoUtil.getString( nIndex++ ) );
            }

            return refAttributeCertificationProcessus;
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
    public void store( RefAttributeCertificationProcessus refAttributeCertificationProcessus, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setString( nIndex++, refAttributeCertificationProcessus.getLabel( ) );
            daoUtil.setString( nIndex++, refAttributeCertificationProcessus.getCode( ) );
            daoUtil.setInt( nIndex, refAttributeCertificationProcessus.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefAttributeCertificationProcessus> selectRefAttributeCertificationProcessussList( Plugin plugin )
    {
        List<RefAttributeCertificationProcessus> refAttributeCertificationProcessusList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                RefAttributeCertificationProcessus refAttributeCertificationProcessus = new RefAttributeCertificationProcessus( );
                int nIndex = 1;

                refAttributeCertificationProcessus.setId( daoUtil.getInt( nIndex++ ) );
                refAttributeCertificationProcessus.setLabel( daoUtil.getString( nIndex++ ) );
                refAttributeCertificationProcessus.setCode( daoUtil.getString( nIndex++ ) );

                refAttributeCertificationProcessusList.add( refAttributeCertificationProcessus );
            }

            return refAttributeCertificationProcessusList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdRefAttributeCertificationProcessussList( Plugin plugin )
    {
        List<Integer> refAttributeCertificationProcessusList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refAttributeCertificationProcessusList.add( daoUtil.getInt( 1 ) );
            }

            return refAttributeCertificationProcessusList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectRefAttributeCertificationProcessussReferenceList( Plugin plugin )
    {
        ReferenceList refAttributeCertificationProcessusList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refAttributeCertificationProcessusList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return refAttributeCertificationProcessusList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefAttributeCertificationProcessus> selectRefAttributeCertificationProcessussListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<RefAttributeCertificationProcessus> refAttributeCertificationProcessusList = new ArrayList<>( );

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
                    RefAttributeCertificationProcessus refAttributeCertificationProcessus = new RefAttributeCertificationProcessus( );
                    int nIndex = 1;

                    refAttributeCertificationProcessus.setId( daoUtil.getInt( nIndex++ ) );
                    refAttributeCertificationProcessus.setLabel( daoUtil.getString( nIndex++ ) );
                    refAttributeCertificationProcessus.setCode( daoUtil.getString( nIndex++ ) );

                    refAttributeCertificationProcessusList.add( refAttributeCertificationProcessus );
                }

                daoUtil.free( );

            }
        }
        return refAttributeCertificationProcessusList;

    }
}

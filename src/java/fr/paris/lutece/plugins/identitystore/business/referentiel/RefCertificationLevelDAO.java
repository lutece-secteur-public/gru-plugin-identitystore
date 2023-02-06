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
 * This class provides Data Access methods for RefCertificationLevel objects
 */
public final class RefCertificationLevelDAO implements IRefCertificationLevelDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_ref_certification_level, name, description, level FROM identitystore_ref_certification_level WHERE id_ref_certification_level = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_ref_certification_level ( name, description, level ) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_ref_certification_level WHERE id_ref_certification_level = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_ref_certification_level SET name = ?, description = ?, level = ? WHERE id_ref_certification_level = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_ref_certification_level, name, description, level FROM identitystore_ref_certification_level";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_ref_certification_level FROM identitystore_ref_certification_level";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT id_ref_certification_level, name, description, level FROM identitystore_ref_certification_level WHERE id_ref_certification_level IN (  ";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( RefCertificationLevel refCertificationLevel, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, refCertificationLevel.getName( ) );
            daoUtil.setString( nIndex++, refCertificationLevel.getDescription( ) );
            daoUtil.setString( nIndex++, refCertificationLevel.getLevel( ) );

            daoUtil.executeUpdate( );
            if ( daoUtil.nextGeneratedKey( ) )
            {
                refCertificationLevel.setId( daoUtil.getGeneratedKeyInt( 1 ) );
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public RefCertificationLevel load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            RefCertificationLevel refCertificationLevel = null;

            if ( daoUtil.next( ) )
            {
                refCertificationLevel = new RefCertificationLevel( );
                int nIndex = 1;

                refCertificationLevel.setId( daoUtil.getInt( nIndex++ ) );
                refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setLevel( daoUtil.getString( nIndex ) );
            }

            return refCertificationLevel;
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
    public void store( RefCertificationLevel refCertificationLevel, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            // daoUtil.setInt( nIndex++ , refCertificationLevel.getId( ) );
            daoUtil.setString( nIndex++, refCertificationLevel.getName( ) );
            daoUtil.setString( nIndex++, refCertificationLevel.getDescription( ) );
            daoUtil.setString( nIndex++, refCertificationLevel.getLevel( ) );
            daoUtil.setInt( nIndex, refCertificationLevel.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefCertificationLevel> selectRefCertificationLevelsList( Plugin plugin )
    {
        List<RefCertificationLevel> refCertificationLevelList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                RefCertificationLevel refCertificationLevel = new RefCertificationLevel( );
                int nIndex = 1;

                refCertificationLevel.setId( daoUtil.getInt( nIndex++ ) );
                refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setLevel( daoUtil.getString( nIndex ) );

                refCertificationLevelList.add( refCertificationLevel );
            }

            return refCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdRefCertificationLevelsList( Plugin plugin )
    {
        List<Integer> refCertificationLevelList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refCertificationLevelList.add( daoUtil.getInt( 1 ) );
            }

            return refCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectRefCertificationLevelsReferenceList( Plugin plugin )
    {
        ReferenceList refCertificationLevelList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refCertificationLevelList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return refCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefCertificationLevel> selectRefCertificationLevelsListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<RefCertificationLevel> refCertificationLevelList = new ArrayList<>( );

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
                    RefCertificationLevel refCertificationLevel = new RefCertificationLevel( );
                    int nIndex = 1;

                    refCertificationLevel.setId( daoUtil.getInt( nIndex++ ) );
                    refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                    refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                    refCertificationLevel.setLevel( daoUtil.getString( nIndex ) );

                    refCertificationLevelList.add( refCertificationLevel );
                }

                daoUtil.free( );

            }
        }
        return refCertificationLevelList;

    }
}

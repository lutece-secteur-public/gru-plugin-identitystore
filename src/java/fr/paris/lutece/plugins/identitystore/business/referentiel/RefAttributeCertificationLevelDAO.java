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
package fr.paris.lutece.plugins.identitystore.business.referentiel;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for RefAttributeCertificationLevel objects
 */
public final class RefAttributeCertificationLevelDAO implements IRefAttributeCertificationLevelDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute, id_ref_attribute_certification_processus, id_ref_certification_level FROM identitystore_ref_certification_attribute_level WHERE id_attribute_certification_level = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_ref_certification_attribute_level ( id_attribute, id_ref_attribute_certification_processus, id_ref_certification_level ) VALUES ( ?,?,? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_ref_certification_attribute_level WHERE id_attribute = ? AND id_ref_attribute_certification_processus = ? AND id_ref_certification_level = ?";
    private static final String SQL_QUERY_DELETE_WITH_PROCESS_ID = "DELETE FROM identitystore_ref_certification_attribute_level WHERE id_ref_attribute_certification_processus = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_ref_certification_attribute_level SET id_attribute_certification_level = ?  WHERE id_attribute = ? AND id_ref_attribute_certification_processus = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute, id_ref_attribute_certification_processus, id_ref_certification_level FROM identitystore_ref_certification_attribute_level";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_attribute, id_ref_attribute_certification_processus, id_ref_certification_level FROM identitystore_ref_certification_attribute_level";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT id_attribute, id_ref_attribute_certification_processus, id_ref_certification_level FROM identitystore_ref_certification_attribute_level WHERE id_ref_attribute_certification_processus IN (  ";

    private static final String SQL_QUERY_SELECTALL_BY_ATTRIBUTE = "SELECT a.id_ref_attribute_certification_processus, a.label, a.code, c.name, c.description, c.level, c.id_ref_certification_level"
            + " FROM identitystore_ref_certification_processus a"
            + " JOIN  identitystore_ref_certification_attribute_level b ON  a.id_ref_attribute_certification_processus = b.id_ref_attribute_certification_processus AND id_attribute = ?"
            + " JOIN  identitystore_ref_certification_level c ON  c.id_ref_certification_level = b.id_ref_certification_level";
    private static final String SQL_QUERY_SELECTALL_BY_REF_PROCESSUS = "SELECT a.id_attribute, a.name, a.key_name, a.description, a.key_type, c.name, c.description, c.level, c.id_ref_certification_level"
            + " FROM identitystore_ref_attribute a"
            + " LEFT JOIN  identitystore_ref_certification_attribute_level b ON  a.id_attribute = b.id_attribute AND id_ref_attribute_certification_processus = ?"
            + " LEFT JOIN  identitystore_ref_certification_level c ON  c.id_ref_certification_level = b.id_ref_certification_level";

    private static final String SQL_QUERY_SELECT_BY_PROCESSUS_AND_KEY_NAME = "SELECT b.id_attribute, b.name, b.key_name, b.description, b.key_type, c.id_ref_attribute_certification_processus, c.code, c.label, d.name, d.description, d.level, d.id_ref_certification_level "
            + " FROM identitystore_ref_certification_attribute_level a " + " JOIN identitystore_ref_attribute b ON a.id_attribute = b.id_attribute "
            + " JOIN identitystore_ref_certification_processus c ON a.id_ref_attribute_certification_processus = c.id_ref_attribute_certification_processus "
            + " JOIN identitystore_ref_certification_level d on a.id_ref_certification_level = d.id_ref_certification_level "
            + " WHERE c.code = ? AND b.key_name = ?";

    private static final String SQL_QUERY_SELECT_LEVEL_BY_ATTRIBUTE_AND_CODE = "SELECT d.level "
            + " FROM identitystore_ref_certification_attribute_level a " + " JOIN identitystore_ref_attribute b ON a.id_attribute = b.id_attribute "
            + " JOIN identitystore_ref_certification_processus c ON a.id_ref_attribute_certification_processus = c.id_ref_attribute_certification_processus "
            + " JOIN identitystore_ref_certification_level d on a.id_ref_certification_level = d.id_ref_certification_level "
            + " WHERE c.code = ? AND b.key_name = ?";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( RefAttributeCertificationLevel refAttributeCertificationLevel, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, refAttributeCertificationLevel.getAttributeKey( ).getId( ) );
            daoUtil.setInt( nIndex++, refAttributeCertificationLevel.getRefAttributeCertificationProcessus( ).getId( ) );
            daoUtil.setInt( nIndex, refAttributeCertificationLevel.getRefCertificationLevel( ).getId( ) );
            daoUtil.executeUpdate( );
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<RefAttributeCertificationLevel> load( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            RefAttributeCertificationLevel refAttributeCertificationLevel = null;

            if ( daoUtil.next( ) )
            {
                refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                int nIndex = 1;

                refAttributeCertificationLevel.setId( daoUtil.getInt( nIndex ) );
            }

            return Optional.ofNullable( refAttributeCertificationLevel );
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
    public void deleteFromProcessus( int nKey, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_WITH_PROCESS_ID, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( RefAttributeCertificationLevel refAttributeCertificationLevel, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, refAttributeCertificationLevel.getId( ) );
            daoUtil.setInt( nIndex, refAttributeCertificationLevel.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefAttributeCertificationLevel> selectRefAttributeCertificationLevelsList( Plugin plugin )
    {
        List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                int nIndex = 1;

                refAttributeCertificationLevel.setAttributeKey( AttributeKeyHome.findByPrimaryKey( daoUtil.getInt( nIndex++ ), false ) );
                refAttributeCertificationLevel
                        .setRefAttributeCertificationProcessus( RefAttributeCertificationProcessusHome.findByPrimaryKey( daoUtil.getInt( nIndex++ ) ) );
                refAttributeCertificationLevel.setRefCertificationLevel( RefCertificationLevelHome.findByPrimaryKey( daoUtil.getInt( nIndex ) ) );

                refAttributeCertificationLevelList.add( refAttributeCertificationLevel );
            }

            return refAttributeCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdRefAttributeCertificationLevelsList( Plugin plugin )
    {
        List<Integer> refAttributeCertificationLevelList = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refAttributeCertificationLevelList.add( daoUtil.getInt( 1 ) );
            }

            return refAttributeCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectRefAttributeCertificationLevelsReferenceList( Plugin plugin )
    {
        ReferenceList refAttributeCertificationLevelList = new ReferenceList( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                refAttributeCertificationLevelList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return refAttributeCertificationLevelList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<RefAttributeCertificationLevel> selectRefAttributeCertificationLevelsListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>( );

        StringBuilder builder = new StringBuilder( );

        if ( !listIds.isEmpty( ) )
        {
            for ( int i = 0; i < listIds.size( ); i++ )
            {
                builder.append( "?," );
            }

            String placeHolders = builder.deleteCharAt( builder.length( ) - 1 ).toString( );
            String stmt = SQL_QUERY_SELECTALL_BY_IDS + placeHolders + ")";

            try ( final DAOUtil daoUtil = new DAOUtil( stmt, plugin ) )
            {
                int index = 1;
                for ( Integer n : listIds )
                {
                    daoUtil.setInt( index++, n );
                }

                daoUtil.executeQuery( );
                while ( daoUtil.next( ) )
                {
                    RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                    int nIndex = 1;

                    refAttributeCertificationLevel.setId( daoUtil.getInt( nIndex ) );

                    refAttributeCertificationLevelList.add( refAttributeCertificationLevel );
                }
            }
        }
        return refAttributeCertificationLevelList;

    }

    @Override
    public List<RefAttributeCertificationLevel> selectRefAttributeLevelByProcessus( Plugin plugin,
            RefAttributeCertificationProcessus refattributecertificationprocessus )
    {
        final List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>( );

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_REF_PROCESSUS, plugin ) )
        {
            daoUtil.setInt( 1, refattributecertificationprocessus.getId( ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                RefCertificationLevel refCertificationLevel = new RefCertificationLevel( );
                AttributeKey attributeKey = new AttributeKey( );

                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

                refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setLevel( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setId( daoUtil.getInt( nIndex ) );

                refAttributeCertificationLevel.setRefAttributeCertificationProcessus( refattributecertificationprocessus );
                refAttributeCertificationLevel.setRefCertificationLevel( refCertificationLevel );
                refAttributeCertificationLevel.setAttributeKey( attributeKey );

                refAttributeCertificationLevelList.add( refAttributeCertificationLevel );
            }

            return refAttributeCertificationLevelList;
        }
    }

    @Override
    public List<RefAttributeCertificationLevel> selectRefAttributeLevelByAttribute( Plugin plugin, AttributeKey attributeKey )
    {
        final List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>( );

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_ATTRIBUTE, plugin ) )
        {
            daoUtil.setInt( 1, attributeKey.getId( ) );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                RefCertificationLevel refCertificationLevel = new RefCertificationLevel( );
                RefAttributeCertificationProcessus processus = new RefAttributeCertificationProcessus( );

                int nIndex = 1;

                processus.setId( daoUtil.getInt( nIndex++ ) );
                processus.setLabel( daoUtil.getString( nIndex++ ) );
                processus.setCode( daoUtil.getString( nIndex++ ) );

                refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setLevel( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setId( daoUtil.getInt( nIndex ) );

                refAttributeCertificationLevel.setAttributeKey( attributeKey );
                refAttributeCertificationLevel.setRefCertificationLevel( refCertificationLevel );
                refAttributeCertificationLevel.setRefAttributeCertificationProcessus( processus );
                processus.setLevel( refAttributeCertificationLevel );

                refAttributeCertificationLevelList.add( refAttributeCertificationLevel );
            }

            return refAttributeCertificationLevelList;
        }
    }

    @Override
    public RefAttributeCertificationLevel findByProcessusAndAttributeKeyName( String processusCode, String attributeKeyName, Plugin plugin )
    {

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_PROCESSUS_AND_KEY_NAME, plugin ) )
        {
            daoUtil.setString( 1, processusCode );
            daoUtil.setString( 2, attributeKeyName );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                final RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
                final RefCertificationLevel refCertificationLevel = new RefCertificationLevel( );
                final AttributeKey attributeKey = new AttributeKey( );
                final RefAttributeCertificationProcessus refattributecertificationprocessus = new RefAttributeCertificationProcessus( );

                int nIndex = 1;

                attributeKey.setId( daoUtil.getInt( nIndex++ ) );
                attributeKey.setName( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyName( daoUtil.getString( nIndex++ ) );
                attributeKey.setDescription( daoUtil.getString( nIndex++ ) );
                attributeKey.setKeyType( KeyType.valueOf( daoUtil.getInt( nIndex++ ) ) );

                refattributecertificationprocessus.setId( daoUtil.getInt( nIndex++ ) );
                refattributecertificationprocessus.setCode( daoUtil.getString( nIndex++ ) );
                refattributecertificationprocessus.setLabel( daoUtil.getString( nIndex++ ) );

                refCertificationLevel.setName( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setDescription( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setLevel( daoUtil.getString( nIndex++ ) );
                refCertificationLevel.setId( daoUtil.getInt( nIndex ) );

                refAttributeCertificationLevel.setRefAttributeCertificationProcessus( refattributecertificationprocessus );
                refAttributeCertificationLevel.setRefCertificationLevel( refCertificationLevel );
                refAttributeCertificationLevel.setAttributeKey( attributeKey );
                return refAttributeCertificationLevel;

            }

            return null;
        }
    }

    @Override
    public Integer findLevelByProcessusAndAttributeKeyName( String processusCode, String attributeKeyName, Plugin plugin )
    {

        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_LEVEL_BY_ATTRIBUTE_AND_CODE, plugin ) )
        {
            daoUtil.setString( 1, processusCode );
            daoUtil.setString( 2, attributeKeyName );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                int nIndex = 1;
                return daoUtil.getInt( nIndex);
            }

            return null;
        }
    }
}

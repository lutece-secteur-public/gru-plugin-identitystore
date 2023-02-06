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
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class provides Data Access methods for AttributeRequirement objects
 */
public final class AttributeRequirementDAO implements IAttributeRequirementDAO
{
    // Constants
    private static final String SQL_QUERY_SELECT = "SELECT id_attribute, id_ref_certification_level, id_service_contract FROM identitystore_attribute_requirement WHERE id_attribute_requirement = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_attribute_requirement ( id_attribute, id_ref_certification_level, id_service_contract ) VALUES ( ?,?,? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_attribute_requirement WHERE id_attribute_requirement = ? ";
    private static final String SQL_QUERY_DELETE_WITH_SERVICE_CONTRACT_ID = "DELETE FROM identitystore_attribute_requirement WHERE id_service_contract = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_attribute_requirement SET id_attribute_requirement = ?,  WHERE id_attribute_requirement = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_attribute_requirement,  FROM identitystore_attribute_requirement";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_attribute_requirement FROM identitystore_attribute_requirement";
    private static final String SQL_QUERY_SELECTALL_BY_IDS = "SELECT id_attribute_requirement,  FROM identitystore_attribute_requirement WHERE id_attribute_requirement IN (  ";
    private static final String SQL_QUERY_SELECTALL_BY_SERVICE_CONTRACT = "SELECT a.id_attribute, a.name, a.key_name, a.description, a.key_type, c.name, c.description, c.level, c.id_ref_certification_level"
            + " FROM identitystore_attribute a"
            + " LEFT JOIN  identitystore_attribute_requirement b ON  a.id_attribute = b.id_attribute AND id_service_contract = ?"
            + " LEFT JOIN  identitystore_ref_certification_level c ON  c.id_ref_certification_level = b.id_ref_certification_level";

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( AttributeRequirement attributeRequirement, int serviceContractId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, Statement.RETURN_GENERATED_KEYS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, attributeRequirement.getAttributeKey( ).getId( ) );
            daoUtil.setInt( nIndex++, attributeRequirement.getRefCertificationLevel( ).getId( ) );
            daoUtil.setInt( nIndex++, serviceContractId );
            daoUtil.executeUpdate( );
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Optional<AttributeRequirement> load( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );
            AttributeRequirement attributeRequirement = null;

            if ( daoUtil.next( ) )
            {
                attributeRequirement = new AttributeRequirement( );
                int nIndex = 1;

                attributeRequirement.setId( daoUtil.getInt( nIndex++ ) );
            }

            return Optional.ofNullable( attributeRequirement );
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
    public void store( AttributeRequirement attributeRequirement, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin ) )
        {
            int nIndex = 1;

            daoUtil.setInt( nIndex++, attributeRequirement.getId( ) );
            daoUtil.setInt( nIndex, attributeRequirement.getId( ) );

            daoUtil.executeUpdate( );
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeRequirement> selectAttributeRequirementsList( Plugin plugin )
    {
        List<AttributeRequirement> attributeRequirementList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                AttributeRequirement attributeRequirement = new AttributeRequirement( );
                int nIndex = 1;

                attributeRequirement.setId( daoUtil.getInt( nIndex++ ) );

                attributeRequirementList.add( attributeRequirement );
            }

            return attributeRequirementList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdAttributeRequirementsList( Plugin plugin )
    {
        List<Integer> attributeRequirementList = new ArrayList<>( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                attributeRequirementList.add( daoUtil.getInt( 1 ) );
            }

            return attributeRequirementList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectAttributeRequirementsReferenceList( Plugin plugin )
    {
        ReferenceList attributeRequirementList = new ReferenceList( );
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                attributeRequirementList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
            }

            return attributeRequirementList;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<AttributeRequirement> selectAttributeRequirementsListByIds( Plugin plugin, List<Integer> listIds )
    {
        List<AttributeRequirement> attributeRequirementList = new ArrayList<>( );

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
                    AttributeRequirement attributeRequirement = new AttributeRequirement( );
                    int nIndex = 1;

                    attributeRequirement.setId( daoUtil.getInt( nIndex++ ) );

                    attributeRequirementList.add( attributeRequirement );
                }

                daoUtil.free( );

            }
        }
        return attributeRequirementList;

    }

    @Override
    public List<AttributeRequirement> selectAttributeRequirementsListByServiceContract( Plugin plugin, ServiceContract servicecontract )
    {
        List<AttributeRequirement> attributeRequirements = new ArrayList<>( );

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_BY_SERVICE_CONTRACT, plugin );
        daoUtil.setInt( 1, servicecontract.getId( ) );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            AttributeRequirement attributeRequirement = new AttributeRequirement( );
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
            refCertificationLevel.setId( daoUtil.getInt( nIndex++ ) );

            attributeRequirement.setRefCertificationLevel( refCertificationLevel );
            attributeRequirement.setAttributeKey( attributeKey );

            attributeRequirements.add( attributeRequirement );
        }

        daoUtil.free( );

        return attributeRequirements;
    }
}

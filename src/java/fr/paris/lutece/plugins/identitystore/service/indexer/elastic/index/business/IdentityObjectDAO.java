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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides Data Access methods for Identity objects
 */
public final class IdentityObjectDAO implements IIdentityObjectDAO
{
    // Constants
    private static final String SQL_QUERY_SELECTALL_CUSTOMER_IDS_FOR_INDEX = "SELECT customer_id FROM identitystore_identity identity "
            + "WHERE is_deleted = 0 AND is_merged = 0 AND exists( " + "    select id_attribute " + "    from identitystore_identity_attribute attribute "
            + "    where identity.id_identity = attribute.id_identity " + ")";
    private static final String SQL_QUERY_LOAD_IDENTITY = "SELECT "
            + "    identity.connection_id, identity.customer_id, identity.date_create, identity.last_update_date, "
            + "    attributeKey.name, attributeKey.key_name, attributeKey.key_type, attributeKey.key_weight, attributeKey.description, attributeKey.pivot, "
            + "    attribute.attribute_value, attribute.lastupdate_application, "
            + "    certificate.certifier_code, certificate.certifier_code, certificate.certificate_date, certificate.certificate_level, certificate.expiration_date "
            + " FROM identitystore_identity identity"
            + "    LEFT JOIN identitystore_identity_attribute attribute ON identity.id_identity = attribute.id_identity "
            + "    LEFT JOIN identitystore_attribute attributeKey ON attribute.id_attribute = attributeKey.id_attribute "
            + "    LEFT JOIN identitystore_attribute_certificate certificate on attribute.id_certification = certificate.id_attribute_certificate "
            + " WHERE identity.customer_id = ? ";

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityObject loadFull( String customerId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_LOAD_IDENTITY, plugin );
        daoUtil.setString( 1, customerId );
        daoUtil.executeQuery( );

        IdentityObject identity = null;

        if ( daoUtil.next( ) )
        {
            identity = new IdentityObject( );

            int nIndex = 1;

            identity.setConnectionId( daoUtil.getString( nIndex++ ) );
            identity.setCustomerId( daoUtil.getString( nIndex++ ) );
            identity.setCreationDate( daoUtil.getTimestamp( nIndex++ ) );
            identity.setLastUpdateDate( daoUtil.getTimestamp( nIndex++ ) );

            final AttributeObject firstAttribute = getAttributeObject( daoUtil, 5 );
            identity.getAttributes( ).put( firstAttribute.getKey( ), firstAttribute );

            while ( daoUtil.next( ) )
            {
                final AttributeObject attribute = getAttributeObject( daoUtil, 5 );
                identity.getAttributes( ).put( attribute.getKey( ), attribute );
            }

        }

        daoUtil.free( );

        return identity;
    }

    private AttributeObject getAttributeObject( DAOUtil daoUtil, int nIndex )
    {
        final AttributeObject attribute = new AttributeObject( );
        attribute.setName( daoUtil.getString( nIndex++ ) );
        attribute.setKey( daoUtil.getString( nIndex++ ) );
        attribute.setType( daoUtil.getString( nIndex++ ) );
        attribute.setWeight( daoUtil.getInt( nIndex++ ) );
        attribute.setDescription( daoUtil.getString( nIndex++ ) );
        attribute.setPivot( daoUtil.getBoolean( nIndex++ ) );
        attribute.setValue( daoUtil.getString( nIndex++ ) );
        attribute.setLastUpdateApplicationCode( daoUtil.getString( nIndex++ ) );
        attribute.setCertifierCode( daoUtil.getString( nIndex++ ) );
        attribute.setCertifierName( daoUtil.getString( nIndex++ ) );
        attribute.setCertificateDate( daoUtil.getTimestamp( nIndex++ ) );
        attribute.setCertificateLevel( daoUtil.getInt( nIndex++ ) );
        attribute.setCertificateExpirationDate( daoUtil.getTimestamp( nIndex++ ) );
        return attribute;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<String> selectEligibleCustomerIdsListForIndex( Plugin plugin )
    {
        final List<String> listIds = new ArrayList<>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CUSTOMER_IDS_FOR_INDEX, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            String identity = daoUtil.getString( 1 );
            listIds.add( identity );
        }

        daoUtil.free( );

        return listIds;
    }
}

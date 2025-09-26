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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides Data Access methods for Identity objects
 */
public final class IdentityObjectDAO implements IIdentityObjectDAO
{
    // Constants
    private static final String LIST_IDS = "%{list_ids}";
    private static final String SQL_QUERY_GET_ELIGIBLE_ID_FOR_INDEX = "SELECT i.id_identity FROM identitystore_identity i WHERE is_deleted = 0 AND is_merged = 0 AND exists(SELECT a.id_attribute FROM identitystore_identity_attribute a WHERE i.id_identity = a.id_identity)";
    private static final String SQL_QUERY_LOAD_IDENTITY_BY_CUSTOMER_ID = "SELECT "
            + "    identity.connection_id, identity.customer_id, identity.date_create, identity.last_update_date, identity.expiration_date, identity.date_delete, identity.is_mon_paris_active,"
            + "    attributeKey.name, attributeKey.key_name, attributeKey.key_type, attributeKey.description, attributeKey.pivot, "
            + "    attribute.attribute_value, attribute.lastupdate_client, "
            + "    certificate.certifier_code, certificate.certifier_code, certificate.certificate_date, certificate.expiration_date "
            + " FROM identitystore_identity identity"
            + "    LEFT JOIN identitystore_identity_attribute attribute ON identity.id_identity = attribute.id_identity "
            + "    LEFT JOIN identitystore_ref_attribute attributeKey ON attribute.id_attribute = attributeKey.id_attribute "
            + "    LEFT JOIN identitystore_identity_attribute_certificate certificate on attribute.id_certification = certificate.id_attribute_certificate "
            + " WHERE identity.customer_id = ? ";
    private static final String SQL_QUERY_SELECT_IDENTITIES_BY_IDS = "SELECT "
            + "    identity.connection_id as connection_id, identity.customer_id as customer_id, identity.date_create as date_create, identity.last_update_date as last_update_date, identity.expiration_date as expiration_date, identity.date_delete as date_delete, identity.is_mon_paris_active as is_mon_paris_active, "
            + "    attribute_key.name as  attribute_key_name,  attribute_key.key_name as  attribute_key_key_name,  attribute_key.key_type as  attribute_key_key_type,  attribute_key.description as  attribute_key_description,  attribute_key.pivot as  attribute_key_pivot, "
            + "    attribute.attribute_value as  attribute_attribute_value,  attribute.lastupdate_client as  attribute_lastupdate_client, "
            + "    certificate.certifier_code as  certificate_certifier_code,  certificate.certifier_code as  certificate_certifier_code,  certificate.certificate_date as  certificate_certificate_date,  certificate.expiration_date as  certificate_expiration_date "
            + "FROM identitystore_identity identity "
            + "         LEFT JOIN identitystore_identity_attribute attribute ON identity.id_identity = attribute.id_identity "
            + "         LEFT JOIN identitystore_ref_attribute attribute_key ON attribute.id_attribute = attribute_key.id_attribute "
            + "         LEFT JOIN identitystore_identity_attribute_certificate certificate on attribute.id_certification = certificate.id_attribute_certificate "
            + "WHERE identity.id_identity IN ( " + LIST_IDS + " );";

    /**
     * {@inheritDoc }
     */
    @Override
    public IdentityObject loadFull( String customerId, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_LOAD_IDENTITY_BY_CUSTOMER_ID, plugin ) )
        {
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
                identity.setExpirationDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setDeleteDate( daoUtil.getTimestamp( nIndex++ ) );
                identity.setMonParisActive( daoUtil.getBoolean( nIndex++ ) );

                final AttributeObject firstAttribute = getAttributeObject( daoUtil, nIndex );
                identity.getAttributes( ).put( firstAttribute.getKey( ), firstAttribute );

                while ( daoUtil.next( ) )
                {
                    final AttributeObject attribute = getAttributeObject( daoUtil, nIndex );
                    identity.getAttributes( ).put( attribute.getKey( ), attribute );
                }

            }

            return identity;
        }
    }

    private AttributeObject getAttributeObject( DAOUtil daoUtil, int nIndex )
    {
        final AttributeObject attribute = new AttributeObject( );
        attribute.setName( daoUtil.getString( nIndex++ ) );
        attribute.setKey( daoUtil.getString( nIndex++ ) );
        attribute.setType( daoUtil.getString( nIndex++ ) );
        attribute.setDescription( daoUtil.getString( nIndex++ ) );
        attribute.setPivot( daoUtil.getBoolean( nIndex++ ) );
        attribute.setValue( daoUtil.getString( nIndex++ ) );
        attribute.setLastUpdateClientCode( daoUtil.getString( nIndex++ ) );
        attribute.setCertifierCode( daoUtil.getString( nIndex++ ) );
        attribute.setCertifierName( daoUtil.getString( nIndex++ ) );
        attribute.setCertificateDate( daoUtil.getTimestamp( nIndex++ ) );
        attribute.setCertificateExpirationDate( daoUtil.getTimestamp( nIndex ) );
        return attribute;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> getEligibleIdListForIndex( Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_GET_ELIGIBLE_ID_FOR_INDEX, plugin ) )
        {
            final List<Integer> eligibleIdListForIndex = new ArrayList<>( );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                eligibleIdListForIndex.add( daoUtil.getInt( 1 ) );
            }

            return eligibleIdListForIndex;
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<IdentityObject> loadEligibleIdentitiesForIndex( final List<Integer> idList, Plugin plugin )
    {
        final List<IdentityObject> identityObjects = new ArrayList<>( );
        final String query = SQL_QUERY_SELECT_IDENTITIES_BY_IDS.replace( LIST_IDS,
                idList.stream( ).map( String::valueOf ).collect( Collectors.joining( "," ) ) );
        try ( final DAOUtil daoUtil = new DAOUtil( query, plugin ) )
        {
            daoUtil.executeQuery( );
            while ( daoUtil.next( ) )
            {
                final String customerId = daoUtil.getString( "customer_id" );
                final Optional<IdentityObject> first = identityObjects.stream( )
                        .filter( identityObject -> identityObject.getCustomerId( ).equals( customerId ) ).findFirst( );

                IdentityObject identity;
                if ( first.isPresent( ) )
                {
                    identity = first.get( );
                }
                else
                {
                    identity = new IdentityObject( );
                    identity.setConnectionId( daoUtil.getString( "connection_id" ) );
                    identity.setCustomerId( customerId );
                    identity.setCreationDate( daoUtil.getTimestamp( "date_create" ) );
                    identity.setLastUpdateDate( daoUtil.getTimestamp( "last_update_date" ) );
                    identity.setExpirationDate( daoUtil.getTimestamp( "expiration_date" ) );
                    identity.setDeleteDate( daoUtil.getTimestamp( "date_delete" ) );
                    identity.setMonParisActive( daoUtil.getBoolean( "is_mon_paris_active" ) );
                    identityObjects.add( identity );
                }

                final AttributeObject attribute = new AttributeObject( );
                attribute.setName( daoUtil.getString( "attribute_key_name" ) );
                attribute.setKey( daoUtil.getString( "attribute_key_key_name" ) );
                attribute.setType( daoUtil.getString( "attribute_key_key_type" ) );
                attribute.setDescription( daoUtil.getString( "attribute_key_description" ) );
                attribute.setPivot( daoUtil.getBoolean( "attribute_key_pivot" ) );
                attribute.setValue( daoUtil.getString( "attribute_attribute_value" ) );
                attribute.setLastUpdateClientCode( daoUtil.getString( "attribute_lastupdate_client" ) );
                attribute.setCertifierCode( daoUtil.getString( "certificate_certifier_code" ) );
                attribute.setCertifierName( daoUtil.getString( "certificate_certifier_code" ) );
                attribute.setCertificateDate( daoUtil.getTimestamp( "certificate_certificate_date" ) );
                attribute.setCertificateExpirationDate( daoUtil.getTimestamp( "certificate_expiration_date" ) );
                identity.getAttributes( ).put( attribute.getKey( ), attribute );
            }
        }
        return identityObjects;
    }
}

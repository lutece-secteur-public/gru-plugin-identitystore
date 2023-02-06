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
package fr.paris.lutece.plugins.identitystore.service.indexer.search;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service.IIdentitySearcher;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticSearchIdentityService implements ISearchIdentityService
{
    private IIdentitySearcher _identityIndexer;

    /**
     * private constructor
     */
    private ElasticSearchIdentityService( IIdentitySearcher _identityIndexer )
    {
        this._identityIndexer = _identityIndexer;
    }

    /**
     * {@inheritDoc }
     */
    public List<IdentityDto> getIdentities( Map<String, List<String>> mapAttributeValues, List<String> listAttributeKeyNames, String strClientApplicationCode )
    {
        return IdentityStoreService.getIdentities( mapAttributeValues, listAttributeKeyNames, strClientApplicationCode );
    }

    /**
     * {@inheritDoc }
     */
    public List<QualifiedIdentity> getQualifiedIdentities( final List<SearchAttributeDto> attributes )
    {
        final List<SearchAttribute> searchAttributes = attributes.stream( ).map( dto -> new SearchAttribute( dto.getKey( ), dto.getValue( ), dto.isStrict( ) ) )
                .collect( Collectors.toList( ) );
        final Response search = _identityIndexer.search( searchAttributes );
        final List<QualifiedIdentity> identities = new ArrayList<>( );
        if ( search != null )
        {
            search.getResult( ).getHits( ).forEach( hit -> {
                final QualifiedIdentity identity = new QualifiedIdentity( );
                final IdentityObject source = hit.getSource( );
                identity.setConnectionId( source.getConnectionId( ) );
                identity.setCustomerId( source.getCustomerId( ) );
                // TODO manque creation date et last update date
                identity.setQuality( hit.getScore( ) );
                source.getAttributes( ).forEach( ( s, attributeObject ) -> {
                    final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute attribute = new fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute( );
                    attribute.setKey( s );
                    attribute.setValue( attributeObject.getValue( ) );
                    attribute.setType( attributeObject.getType( ) );
                    attribute.setCertifier( attributeObject.getCertifierCode( ) );
                    attribute.setCertificationDate( attributeObject.getCertificateDate( ) );
                    attribute.setCertificationLevel( attribute.getCertificationLevel( ) );
                    identity.getAttributes( ).add( attribute );
                } );
                identities.add( identity );
            } );
        }
        return identities;
    }

    /**
     * {@inheritDoc }
     */
    public void checkSearchAttributes( Map<String, List<String>> mapAttributeValues, int nServiceContractId ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkSearchAttributes( mapAttributeValues, nServiceContractId );
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.search;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service.IIdentitySearcher;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ConsolidateDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ExpirationDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.MergeDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ElasticSearchIdentityService implements ISearchIdentityService
{
    private final IIdentitySearcher _identitySearcher;

    /**
     * private constructor
     */
    private ElasticSearchIdentityService( IIdentitySearcher _identitySearcher )
    {
        this._identitySearcher = _identitySearcher;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( final List<SearchAttribute> attributes,
            final List<List<SearchAttribute>> specialTreatmentAttributes, final Integer nbEqualAttributes, final Integer nbMissingAttributes, final int max,
            final boolean connected, final List<String> attributesFilter ) throws IdentityStoreException
    {
        final List<SearchAttribute> searchAttributes = this.computeOutputKeys( attributes );
        final List<List<SearchAttribute>> specialAttributes = specialTreatmentAttributes == null ? null
                : specialTreatmentAttributes.stream( ).map( this::computeOutputKeys ).collect( Collectors.toList( ) );

        final Response search = _identitySearcher.multiSearch( searchAttributes, specialAttributes, nbEqualAttributes, nbMissingAttributes, max, connected,
                attributesFilter );

        return new QualifiedIdentitySearchResult( this.getEntities( search ), search.getMetadata( ) );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( List<SearchAttribute> attributes, int max, boolean connected,
            final List<String> attributesFilter ) throws IdentityStoreException
    {
        final List<SearchAttribute> searchAttributes = this.computeOutputKeys( attributes );

        final Response search = _identitySearcher.search( searchAttributes, max, connected, attributesFilter );

        return new QualifiedIdentitySearchResult( this.getEntities( search ), search.getMetadata( ) );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( String customerId, final List<String> attributesFilter ) throws IdentityStoreException
    {
        final Response search = _identitySearcher.search( customerId, attributesFilter );

        return new QualifiedIdentitySearchResult( this.getEntities( search ), search.getMetadata( ) );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentitiesByConnectionId( String connectionId, List<String> attributesFilter ) throws IdentityStoreException
    {
        final Response search = _identitySearcher.searchByConnectionId( connectionId, attributesFilter );

        return new QualifiedIdentitySearchResult( this.getEntities( search ), search.getMetadata( ) );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( List<String> customerIds, final List<String> attributesFilter ) throws IdentityStoreException
    {
        final Response search = _identitySearcher.search( customerIds, attributesFilter );

        return new QualifiedIdentitySearchResult( this.getEntities( search ), search.getMetadata( ) );
    }

    private List<IdentityDto> getEntities( Response search )
    {
        final List<IdentityDto> identities = new ArrayList<>( );

        if ( search != null )
        {
            search.getResult( ).getHits( ).forEach( hit -> {
                identities.add( this.toQualifiedIdentity( hit.getSource( ) ) );
            } );
        }
        return identities;
    }

    private IdentityDto toQualifiedIdentity( final IdentityObject identityObject )
    {
        final IdentityDto identity = new IdentityDto( );
        identity.setConnectionId( identityObject.getConnectionId( ) );
        identity.setCustomerId( identityObject.getCustomerId( ) );
        identity.setCreationDate( identityObject.getCreationDate( ) );
        identity.setLastUpdateDate( identityObject.getLastUpdateDate( ) );
        if( identityObject.getExpirationDate( ) != null )
        {
            identity.setExpiration( new ExpirationDefinition());
            identity.getExpiration().setExpirationDate(identityObject.getExpirationDate());
            identity.getExpiration().setDeleted(false);
            identity.getExpiration().setDeleteDate(null);
        }
        Identity identityDetails = IdentityHome.findByCustomerId( identity.getCustomerId( ) );
        if( identityDetails != null && identityDetails.isMerged( ) ) {
            identity.setMerge( new MergeDefinition( ) );
            identity.getMerge().setMasterCustomerId( identityDetails.getMasterIdentityId().toString( ) );
            identity.getMerge().setMerged( true );
            identity.getMerge().setMergeDate( identityDetails.getMergeDate( ));
        }
        List<Identity> mergedIdentities = IdentityHome.findMergedIdentities(identityDetails.getId());
        if( mergedIdentities != null && !mergedIdentities.isEmpty() )
        {
            identity.setConsolidate(new ConsolidateDefinition());
            List<IdentityDto> mergedIdentitiesDto = new ArrayList<>( );
            for (Identity megedIdentity : mergedIdentities)
            {
                mergedIdentitiesDto.add(DtoConverter.convertIdentityToDto( megedIdentity ));
            }
            identity.getConsolidate().setMergedIdentities(mergedIdentitiesDto);
        }
        identity.setMonParisActive( identityObject.isMonParisActive( ) );
        for ( final Map.Entry<String, AttributeObject> entry : identityObject.getAttributes( ).entrySet( ) )
        {
            final String s = entry.getKey( );
            AttributeObject attributeObject = entry.getValue( );
            final AttributeDto attribute = new AttributeDto( );
            attribute.setKey( s );
            attribute.setValue( attributeObject.getValue( ) );
            attribute.setType( attributeObject.getType( ) );
            attribute.setCertifier( attributeObject.getCertifierCode( ) );
            attribute.setCertificationDate( attributeObject.getCertificateDate( ) );
            attribute.setCertificationLevel( AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( attribute.getCertifier( ), s ) );
            identity.getAttributes( ).add( attribute );
        }
        return identity;
    }
}

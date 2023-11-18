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
package fr.paris.lutece.plugins.identitystore.service.search;

import com.google.common.collect.Lists;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DatabaseSearchIdentityService implements ISearchIdentityService
{

    /**
     * private constructor
     */
    private DatabaseSearchIdentityService( )
    {
    }

    /**
     * {@inheritDoc }
     */
    public QualifiedIdentitySearchResult getQualifiedIdentities( final List<SearchAttribute> attributes, final int max, final boolean connected )
    {
        final Map<String, List<String>> mapAttributeValues = attributes.stream( )
                .collect( Collectors.toMap( SearchAttribute::getKey, searchAttribute -> Lists.newArrayList( searchAttribute.getValue( ) ) ) );
        try
        {
            final List<Identity> listIdentity = IdentityHome.findByAttributesValueForApiSearch( mapAttributeValues, max );
            if ( listIdentity != null && !listIdentity.isEmpty( ) )
            {
                return new QualifiedIdentitySearchResult( this.getEntities( listIdentity ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "An error occurred during database search: ", e );
        }
        return new QualifiedIdentitySearchResult( );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( final String customerId ) throws IdentityStoreException
    {
        try
        {
            final Identity identity = IdentityHome.findByCustomerId( customerId );
            if ( identity != null )
            {
                return new QualifiedIdentitySearchResult( this.getEntities( Collections.singletonList( identity ) ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "An error occurred during database search: ", e );
        }
        return new QualifiedIdentitySearchResult( );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( List<String> customerIds ) throws IdentityStoreException
    {
        // not to be used
        return new QualifiedIdentitySearchResult( );
    }

    @Override
    public QualifiedIdentitySearchResult getQualifiedIdentities( final List<SearchAttribute> attributes,
            final List<List<SearchAttribute>> specialTreatmentAttributes, final Integer nbEqualAttributes, final Integer nbMissingAttributes, int max,
            boolean connected ) throws IdentityStoreException
    {
        if ( CollectionUtils.isNotEmpty( specialTreatmentAttributes ) || ( nbMissingAttributes != null && nbMissingAttributes != 0 ) )
        {
            throw new IdentityStoreException( "Cannot perform a complex search on database" );
        }
        try
        {
            final Map<String, List<String>> mapAttributeValues = attributes.stream( )
                    .collect( Collectors.toMap( SearchAttribute::getKey, searchAttribute -> Lists.newArrayList( searchAttribute.getValue( ) ) ) );
            final List<Identity> listIdentity = IdentityHome.findByAttributesValueForApiSearch( mapAttributeValues, max );
            if ( listIdentity != null && !listIdentity.isEmpty( ) )
            {
                return new QualifiedIdentitySearchResult( this.getEntities( listIdentity ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "An error occurred during database search: ", e );
        }
        return new QualifiedIdentitySearchResult( );
    }

    /**
     * Populates identities with their attributes fetched from database, and convert them to DTO.
     * 
     * @param identityList
     *            the identies to populate
     * @return the DTO list
     * @throws IdentityStoreException
     */
    private List<IdentityDto> getEntities( final List<Identity> identityList ) throws IdentityStoreException
    {
        final List<IdentityDto> qualifiedIdentities = new ArrayList<>( identityList.size( ) );
        final List<IdentityAttribute> listIdentityAttribute = IdentityAttributeHome.getAttributesByIdentityListFullAttributes( identityList );
        for ( final Identity identity : identityList )
        {
            for ( final IdentityAttribute identityAttribute : listIdentityAttribute )
            {
                if ( identity.getId( ) == identityAttribute.getIdIdentity( ) )
                {
                    Map<String, IdentityAttribute> mapIdentityAttributes = identity.getAttributes( );
                    if ( mapIdentityAttributes == null )
                    {
                        mapIdentityAttributes = new HashMap<>( );
                    }
                    mapIdentityAttributes.put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );
                    identity.setAttributes( mapIdentityAttributes );
                }
            }
            qualifiedIdentities.add( DtoConverter.convertIdentityToDto( identity ) );
        }
        return qualifiedIdentities;
    }
}

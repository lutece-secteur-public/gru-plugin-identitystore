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
package fr.paris.lutece.plugins.identitystore.service.duplicate;

import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DuplicateService implements IDuplicateService
{
    /**
     * Identity Search service
     */
    protected ISearchIdentityService _searchIdentityService;

    /**
     * Prefix of the group of rules
     */
    protected String _prefix;

    public DuplicateService( ISearchIdentityService _searchIdentityService, String _prefix )
    {
        this._searchIdentityService = _searchIdentityService;
        this._prefix = _prefix;
    }

    /**
     * Performs a search request on the given {@link ISearchIdentityService}. <br>
     * The rules are defined in properties file with :
     * <ul>
     * <li>a prefix specified in {@link DuplicateService} constructor, which defines the group of rules to load</li>
     * <li>a mode of checking which can be strict or fuzzy (if not specified, default mode is strict)</li>
     * <li>an id which is used to specify several rules to process, and their order</li>
     * </ul>
     * E.g: For creation action: {prefix}.{id}={attribute_key_1},{attribute_key_2},{attribute_key_3}:fuzzy,{attribute_key_4}
     * <ul>
     * <li>identitystore.identity.duplicates.import.suspicion.0=family_name,first_name,birthdate</li>
     * <li>identitystore.identity.duplicates.import.suspicion.1=family_name:fuzzy,first_name:fuzzy,birthdate</li>
     * </ul>
     *
     * @param attributeValues
     *            a {@link Map} of attribute key and attribute value
     * @return a {@link DuplicateDto} that contains the result of the search request
     */
    @Override
    public DuplicateDto findDuplicates( Map<String, String> attributeValues )
    {
        final List<String> keys = AppPropertiesService.getKeys( _prefix );

        if ( CollectionUtils.isNotEmpty( keys ) )
        {
            final List<String> sortedRules = keys.stream( )
                    .sorted( ( o1, o2 ) -> StringUtils.compare( StringUtils.substringAfterLast( o1, "." ), StringUtils.substringAfterLast( o2, "." ) ) )
                    .collect( Collectors.toList( ) );
            for ( final String rule : sortedRules )
            {
                final String property = AppPropertiesService.getProperty( rule );
                final List<SearchAttributeDto> searchAttributes = this.mapAttributes( property, attributeValues );

                final List<QualifiedIdentity> resultIdentities = _searchIdentityService.getQualifiedIdentities( searchAttributes ).stream( )
                        .filter( qualifiedIdentity -> !qualifiedIdentity.isMerged( ) ).collect( Collectors.toList( ) );
                if ( CollectionUtils.isNotEmpty( resultIdentities ) )
                {
                    final DuplicateDto duplicateDto = new DuplicateDto( );
                    duplicateDto.setMessage( "Une ou plusieurs identités existent avec cette règle : " + property );
                    duplicateDto.setIdentities( resultIdentities );
                    return duplicateDto;
                }
            }
        }

        return null;
    }

    private List<SearchAttributeDto> mapAttributes( final String property, final Map<String, String> attributeValues )
    {
        final List<SearchAttributeDto> attributeDtoList = new ArrayList<>( );
        Arrays.asList( StringUtils.split( property, "," ) ).forEach( attributeKey -> {
            final String [ ] attributeAndMode = StringUtils.split( attributeKey, ":" );
            final String key = attributeAndMode [0];
            final boolean fuzzy = attributeAndMode.length == 2 && StringUtils.equals( attributeAndMode [1], "fuzzy" );
            final String value = attributeValues.get( key );
            if ( StringUtils.isNotEmpty( value ) )
            {
                attributeDtoList.add( new SearchAttributeDto( key, value, !fuzzy ) );
            }
        } );
        return attributeDtoList;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.search;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface ISearchIdentityService
{
    /**
     * returns a list of qualified identities from combination of attributes
     *
     * @param max
     *            the maximum number of results that must be returned, if set to 0 or null, must be ignored by implementation.
     * @param attributes
     *            list of values to search for some attributes with strict or fuzzy mode
     * @param specialTreatmentAttributes
     *            defines how each attribute should be searched
     * @param nbEqualAttributes
     *            the number of attributes that must be equal in the response
     * @param nbMissingAttributes
     *            defines the maximum number of attributes that can be absent in the response (vs the request)
     * @param connected
     *            defines if the identity should be connected or not
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a list of identities satisfying the criteria of the {@link SearchAttribute} list
     */
    QualifiedIdentitySearchResult getQualifiedIdentities( final List<SearchAttribute> attributes, final List<List<SearchAttribute>> specialTreatmentAttributes,
            final Integer nbEqualAttributes, final Integer nbMissingAttributes, final int max, final boolean connected, final List<String> attributesFilter )
            throws IdentityStoreException;

    /**
     * returns a list of qualified identities from combination of attributes
     *
     * @param max
     *            the maximum number of results that must be returned, if set to 0 or null, must be ignored by implementation.
     * @param attributes
     *            list of values to search for some attributes with strict or fuzzy mode
     * @param connected
     *            defines if the identity should be connected or not
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a list of identities satisfying the criteria of the {@link SearchAttribute} list
     */
    QualifiedIdentitySearchResult getQualifiedIdentities( final List<SearchAttribute> attributes, final int max, final boolean connected,
            final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * returns a list of qualified identities from its customer ID
     *
     * @param customerId
     *            the customer ID (CUID)
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a list of identities satisfying the criteria of the {@link SearchAttribute} list
     */
    QualifiedIdentitySearchResult getQualifiedIdentities( final String customerId, final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * returns a list of qualified identities from its customer ID
     *
     * @param connectionId
     *            the connection ID (GUID)
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a list of identities satisfying the criteria of the {@link SearchAttribute} list
     */
    QualifiedIdentitySearchResult getQualifiedIdentitiesByConnectionId( final String connectionId, final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * returns a list of qualified identities from its customer ID
     *
     * @param customerIds
     *            the customer IDs
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a list of identities satisfying the criteria of the {@link SearchAttribute} list
     */
    QualifiedIdentitySearchResult getQualifiedIdentities( final List<String> customerIds, final List<String> attributesFilter ) throws IdentityStoreException;

    default List<SearchAttribute> computeOutputKeys( final List<SearchAttribute> attributes )
    {
        final List<SearchAttribute> searchAttributes = new ArrayList<>( );
        if ( CollectionUtils.isNotEmpty( attributes ) )
        {
            for ( final SearchAttribute dto : attributes )
            {
                AttributeKey refKey = null;
                try
                {
                    refKey = IdentityAttributeService.instance( ).getAttributeKey( dto.getKey( ) );
                }
                catch( IdentityAttributeNotFoundException e )
                {
                    // do nothing, we want to identify if the key exists
                }

                if ( refKey != null )
                {
                    dto.setOutputKeys( Collections.singletonList( dto.getKey( ) ) );
                }
                else
                {
                    // In this case we have a common search key in the request, so map it
                    final List<AttributeKey> commonAttributeKeys = IdentityAttributeService.instance( ).getCommonAttributeKeys( dto.getKey( ) );
                    final List<String> commonAttributeKeyNames = commonAttributeKeys.stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toList( ) );
                    dto.setOutputKeys( commonAttributeKeyNames );
                }
                searchAttributes.add( dto );
            }
        }
        return searchAttributes;
    }
}

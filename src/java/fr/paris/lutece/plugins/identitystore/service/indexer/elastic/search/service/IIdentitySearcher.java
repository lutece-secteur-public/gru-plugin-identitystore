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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Result;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Shard;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Total;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface IIdentitySearcher
{
    /**
     * Performs a multi search in elastic search (_msearch) based on a list of attributes
     * 
     * @param attributes
     *            the list of requested attribute keys and values
     * @param specialTreatmentAttributes
     *            the list of attribute treatment
     * @param nbEqualAttributes
     *            the number of attributes that must be equal amongst the requested attributes
     * @param nbMissingAttributes
     *            the numer of attributes that must be not present amongst the requested attributes
     * @param max
     *            the maximum of hits for each request
     * @param connected
     *            if true the hits must contain only connected identities
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a {@link Response}
     * @throws IdentityStoreException
     *             in case of error
     */
    Response multiSearch( final List<SearchAttribute> attributes, final List<List<SearchAttribute>> specialTreatmentAttributes, final Integer nbEqualAttributes,
            final Integer nbMissingAttributes, int max, boolean connected, final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * Performs a search in elastic search (_search) based on a list of attributes
     * 
     * @param attributes
     *            the list of requested attribute keys and values
     * @param max
     *            the maximum of hits for each request
     * @param connected
     *            if true the hits must contain only connected identities
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a {@link Response}
     * @throws IdentityStoreException
     */
    Response search( final List<SearchAttribute> attributes, final int max, final boolean connected, final List<String> attributesFilter )
            throws IdentityStoreException;

    /**
     * Performs a search in elastic search (_search) based on customer ID
     * 
     * @param customerId
     *            the customer ID to find (CUID)
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a {@link Response}
     * @throws IdentityStoreException
     */
    Response search( final String customerId, final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * Performs a search in elastic search (_search) based on customer ID
     *
     * @param connectionId
     *            the connection ID to find (GUID)
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a {@link Response}
     * @throws IdentityStoreException
     */
    Response searchByConnectionId( final String connectionId, final List<String> attributesFilter ) throws IdentityStoreException;

    /**
     * Performs a multi search in elastic search (_msearch) based on a list of customer IDs
     * 
     * @param customerIds
     *            the list of customer IDs to find
     * @param attributesFilter
     *            the list of attributes that must be included in the response
     * @return a {@link Response}
     * @throws IdentityStoreException
     */
    Response search( final List<String> customerIds, final List<String> attributesFilter ) throws IdentityStoreException;

    default Response emptyResponse( )
    {
        final Response response = new Response( );
        final Result result = new Result( );
        result.setHits( new ArrayList<>( ) );
        result.setMaxScore( BigDecimal.ZERO );
        final Total total = new Total( );
        total.setValue( 0 );
        total.setRelation( "" );
        result.setTotal( total );
        response.setResult( result );
        final Shard shards = new Shard( );
        shards.setFailed( 0 );
        shards.setTotal( 0 );
        shards.setSkipped( 0 );
        shards.setSuccessful( 0 );
        response.setShards( shards );
        response.setTook( "" );
        response.setTimedOut( false );
        response.setStatus( 0 );
        response.setMetadata( new HashMap<>( ) );
        return response;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.BasicSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.InnerSearchRequest;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import org.apache.log4j.Logger;

import java.util.List;

public class IdentitySearcher implements IIdentitySearcher
{

    private static Logger logger = Logger.getLogger( IdentitySearcher.class );
    private static String INDEX = "identities";
    private ElasticClient _elasticClient;

    public IdentitySearcher( String strServerUrl, String strLogin, String strPassword )
    {
        this._elasticClient = new ElasticClient( strServerUrl, strLogin, strPassword );
    }

    public IdentitySearcher( String strServerUrl )
    {
        this._elasticClient = new ElasticClient( strServerUrl );
    }

    @Override
    public Response search( final List<SearchAttribute> attributes )
    {
        try
        {
            final BasicSearchRequest request = new BasicSearchRequest( attributes );
            final InnerSearchRequest initialRequest = request.body( );
            final String response = this._elasticClient.search( INDEX, initialRequest );
            final Response innerResponse = new ObjectMapper( ).readValue( response, Response.class );
            final int size = initialRequest.getSize( );
            int offset = initialRequest.getFrom( );
            int total = innerResponse.getResult( ).getTotal( ).getValue( );
            while ( offset < total )
            {
                offset += size;
                initialRequest.setFrom( offset );
                final String subResponse = this._elasticClient.search( INDEX, initialRequest );
                final Response pagedResponse = new ObjectMapper( ).readValue( subResponse, Response.class );
                innerResponse.getResult( ).getHits( ).addAll( pagedResponse.getResult( ).getHits( ) );
                if ( pagedResponse.getResult( ).getMaxScore( ).compareTo( innerResponse.getResult( ).getMaxScore( ) ) > 0 )
                {
                    innerResponse.getResult( ).setMaxScore( pagedResponse.getResult( ).getMaxScore( ) );
                }
            }
            return innerResponse;
        }
        catch( ElasticClientException | JsonProcessingException e )
        {
            logger.error( "Failed to index", e );
        }
        return null;
    }
}

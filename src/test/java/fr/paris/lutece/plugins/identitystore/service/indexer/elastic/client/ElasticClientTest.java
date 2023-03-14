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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.IdentityStoreBDDAndESTestCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ElasticClientTest extends IdentityStoreBDDAndESTestCase
{
    private final static String INDEX = "identities-index";
    private final static String ALIAS = "identities-alias";
    private final static String DOCUMENT_ID = "azy-cde-152";
    private final static String DOCUMENT_V1 = "{\"data\":\"document content v1\"}";
    private final static String DOCUMENT_V2 = "{\"data\":\"document content v2\"}";

    public void testElasticClientInterface( ) throws ElasticClientException, JsonProcessingException
    {
        final ElasticClient elasticClient = new ElasticClient( "http://" + elasticsearchContainer.getHttpHostAddress( ) );

        System.out.println( "----- Create Index with mappings -----" );
        final InputStream inputStream = this.getClass( ).getClassLoader( )
                .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
        final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                .collect( Collectors.joining( "\n" ) );
        elasticClient.createMappings( INDEX, mappings );
        assertTrue( elasticClient.isExists( INDEX ) );

        System.out.println( "----- Create alias on index -----" );
        elasticClient.createAlias( INDEX, ALIAS );
        assertTrue( elasticClient.isExists( ALIAS ) );

        System.out.println( "----- Create Document -----" );
        elasticClient.create( ALIAS, DOCUMENT_ID, DOCUMENT_V1 );
        final String getV1 = getSource( elasticClient.get( ALIAS, DOCUMENT_ID ) );
        assertEquals( DOCUMENT_V1, getV1 );

        System.out.println( "----- Update Document -----" );
        elasticClient.update( ALIAS, DOCUMENT_ID, DOCUMENT_V2 );
        final String getV2 = getSource( elasticClient.get( ALIAS, DOCUMENT_ID ) );
        assertEquals( DOCUMENT_V2, getV2 );

        System.out.println( "----- Delete Document -----" );
        elasticClient.deleteDocument( ALIAS, DOCUMENT_ID );
        String noDocument = "";
        try
        {
            noDocument = elasticClient.get( ALIAS, DOCUMENT_ID );
        }
        catch( ElasticClientException e )
        {
            System.out.println( "no document found with id " + DOCUMENT_ID );
        }
        assertEquals( "", noDocument );

        System.out.println( "----- Delete Alias -----" );
        elasticClient.deleteAlias( INDEX, ALIAS );
        assertFalse( elasticClient.isExists( ALIAS ) );
        assertTrue( elasticClient.isExists( INDEX ) );

        System.out.println( "----- Delete Index -----" );
        elasticClient.deleteIndex( INDEX );
        assertFalse( elasticClient.isExists( INDEX ) );
    }

    private String getSource( final String document ) throws JsonProcessingException
    {
        final ObjectMapper mapper = new ObjectMapper( );
        final JsonNode node = mapper.readTree( document );

        if ( node != null )
        {
            final Iterator<Map.Entry<String, JsonNode>> iterator = node.fields( );
            while ( iterator != null && iterator.hasNext( ) )
            {
                final Map.Entry<String, JsonNode> next = iterator.next( );
                if ( Objects.equals( next.getKey( ), "_source" ) )
                {
                    return next.getValue( ).toString( );
                }
            }
        }
        return "";
    }
}

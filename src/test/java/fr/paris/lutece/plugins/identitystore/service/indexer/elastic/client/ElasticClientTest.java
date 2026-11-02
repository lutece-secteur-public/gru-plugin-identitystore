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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import fr.paris.lutece.plugins.identitystore.AbstractIdentityStoreTestCase;
import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.util.TestEnvironmentUtils;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ElasticClientTest extends AbstractIdentityStoreTestCase
{
    private static final String IDENTITIES_ALIAS = "identities-alias";
    private static final String IDENTITIES_INDEX = "identities-index";
    protected ElasticsearchContainer elasticsearchContainer;
    protected String esUrl;
    protected ElasticClient elasticClient;

    /**
     * Runs the test only when a Docker environment is available, as it requires an Elasticsearch container
     */
    @Override
    public void runBare( ) throws Throwable
    {
        if ( !TestEnvironmentUtils.isDockerAvailable( ) )
        {
            TestEnvironmentUtils.printSkipped( this.getClass( ), this.getName( ), "Docker environment" );
            return;
        }
        super.runBare( );
    }

    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        this.startContainers( );
        this.initConnection( );
    }

    @Override
    protected void tearDown( ) throws Exception
    {
        super.tearDown( );
        this.shutDownContainers( );
    }

    protected void startContainers( )
    {
        elasticsearchContainer = new ElasticsearchContainer(
                "docker.elastic.co/elasticsearch/elasticsearch:".concat( IdentityStoreTestContext.ELASTICSEARCH_VERSION ) )
                        .withEnv( "xpack.security.enabled", "false" ).withNetworkAliases( "localhost" ).withReuse( false );
        elasticsearchContainer.start( );
        this.esUrl = "http://" + elasticsearchContainer.getHttpHostAddress( );
    }

    protected void shutDownContainers( )
    {
        if ( elasticsearchContainer != null && elasticsearchContainer.isRunning( ) )
        {
            elasticsearchContainer.stop( );
            elasticsearchContainer = null;
        }
    }

    @Override
    protected void preInitApplication( ) throws IOException
    {

    }

    private void initConnection( )
    {
        this.elasticClient = new ElasticClient( this.esUrl );
    }

    public void testClient( ) throws ElasticClientException
    {
        final InputStream inputStream = this.getClass( ).getClassLoader( )
                .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
        final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                .collect( Collectors.joining( "\n" ) );
        this.elasticClient.createMappings( IDENTITIES_INDEX, mappings );

        this.elasticClient.updateSettings( IDENTITIES_INDEX, "{ \"index.blocks.write\": true }" );
        this.elasticClient.updateSettings( IDENTITIES_INDEX, "{ \"index.blocks.write\": false }" );

        // this.elasticClient.addAliasOnIndex(IDENTITIES_INDEX, IDENTITIES_ALIAS);

        final String alias = this.elasticClient.getAlias( IDENTITIES_ALIAS );
        assertNotNull( alias );

        final boolean totoExists = this.elasticClient.aliasExists( "toto" );
        assertFalse( "Alias toto ne devrait pas exister", totoExists );
        final boolean aliasExists = this.elasticClient.aliasExists( IDENTITIES_ALIAS );
        assertTrue( "Alias " + IDENTITIES_ALIAS + " devrait exister", aliasExists );

    }
}

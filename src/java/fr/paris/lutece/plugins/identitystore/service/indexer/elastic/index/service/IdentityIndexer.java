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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class IdentityIndexer implements IIdentityIndexer
{

    private static Logger logger = Logger.getLogger( IdentityIndexer.class );
    private static String INDEX = "identities";
    private ElasticClient _elasticClient;

    public IdentityIndexer( String strServerUrl, String strLogin, String strPassword )
    {
        this._elasticClient = new ElasticClient( strServerUrl, strLogin, strPassword );
    }

    public IdentityIndexer( String strServerUrl )
    {
        this._elasticClient = new ElasticClient( strServerUrl );
    }

    @Override
    public void create( IdentityObject identity )
    {
        try
        {
            if ( !this._elasticClient.isExists( INDEX ) )
            {
                final InputStream inputStream = this.getClass( ).getClassLoader( )
                        .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
                final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                        .collect( Collectors.joining( "\n" ) );
                this._elasticClient.createMappings( INDEX, mappings );
            }

            final String response = this._elasticClient.create( INDEX, identity.getCustomerId( ), identity );
            logger.info( "Indexed document: " + response );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.CREATE );
            logger.error( "Failed to index", e );
        }
    }

    @Override
    public void update( IdentityObject identity )
    {
        try
        {
            final String response = this._elasticClient.update( INDEX, identity.getCustomerId( ), identity );
            logger.info( "Indexed document: " + response );
        }
        catch( ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.UPDATE );
            logger.error( "Failed to index ", e );
        }
    }

    @Override
    public void delete( String documentId )
    {
        try
        {
            final String response = this._elasticClient.deleteDocument( INDEX, documentId );
            logger.info( "Removed document: " + response );
        }
        catch( ElasticClientException e )
        {
            this.handleError( documentId, IndexActionType.DELETE );
            logger.error( "Failed to remove document ", e );
        }
    }

    @Override
    public void fullIndex( List<IdentityObject> identities )
    {
        try
        {
            if ( this._elasticClient.isExists( INDEX ) )
            {
                this._elasticClient.deleteIndex( INDEX );
            }
            final InputStream inputStream = this.getClass( ).getClassLoader( )
                    .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
            final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                    .collect( Collectors.joining( "\n" ) );
            this._elasticClient.createMappings( INDEX, mappings );
            // TODO bulk request pour optimiser
            identities.forEach( identityObject -> this.create( identityObject ) );
        }
        catch( ElasticClientException e )
        {
            logger.error( "Failed to reindex", e );
        }
    }

    public boolean isAlive( )
    {
        return _elasticClient.isAlive( );
    }

    private void handleError( String documentId, IndexActionType actionType )
    {
        final IndexAction indexAction = new IndexAction( actionType, documentId );
        IndexActionHome.create( indexAction );
    }
}

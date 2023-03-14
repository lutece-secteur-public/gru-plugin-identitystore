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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdentityIndexer implements IIdentityIndexer
{

    private static Logger logger = Logger.getLogger( IdentityIndexer.class );
    private ElasticClient _elasticClient;

    public IdentityIndexer( final String strServerUrl, final String strLogin, final String strPassword )
    {
        this._elasticClient = new ElasticClient( strServerUrl, strLogin, strPassword );
    }

    public IdentityIndexer( final String strServerUrl )
    {
        this._elasticClient = new ElasticClient( strServerUrl );
    }

    @Override
    public void create( final IdentityObject identity, final String index )
    {
        try
        {
            if ( !this._elasticClient.isExists( IIdentityIndexer.CURRENT_INDEX_ALIAS ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.initIndex( newIndex );
                this.createOrUpdateAlias( "", newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
            }

            final String response = this._elasticClient.create( IIdentityIndexer.CURRENT_INDEX_ALIAS, identity.getCustomerId( ), identity );
            logger.info( "Indexed document: " + response );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.CREATE );
            logger.error( "Failed to index", e );
        }
    }

    @Override
    public void bulk( final List<BulkAction> bulkActions, final String index )
    {
        try
        {
            final String response = this._elasticClient.indexByBulk( index, bulkActions );
            logger.info( "Indexed document: " + response );
        }
        catch( final ElasticClientException e )
        {
            logger.error( "Failed to bulk index ", e );
        }
    }

    @Override
    public void update( final IdentityObject identity, final String index )
    {
        try
        {
            if ( !this._elasticClient.isExists( IIdentityIndexer.CURRENT_INDEX_ALIAS ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.initIndex( newIndex );
                this.createOrUpdateAlias( "", newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
            }

            final String response = this._elasticClient.update( IIdentityIndexer.CURRENT_INDEX_ALIAS, identity.getCustomerId( ), identity );
            logger.info( "Indexed document: " + response );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.UPDATE );
            logger.error( "Failed to index ", e );
        }
    }

    @Override
    public void delete( final String documentId, final String index )
    {
        try
        {
            final String response = this._elasticClient.deleteDocument( IIdentityIndexer.CURRENT_INDEX_ALIAS, documentId );
            logger.info( "Removed document: " + response );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( documentId, IndexActionType.DELETE );
            logger.error( "Failed to remove document ", e );
        }
    }

    @Override
    public void initIndex( final String index ) throws ElasticClientException
    {
        final InputStream inputStream = this.getClass( ).getClassLoader( )
                .getResourceAsStream( "fr/paris/lutece/plugins/identitystore/service/indexer/elastic/index/model/internal/mappings.json" );
        final String mappings = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) ).lines( )
                .collect( Collectors.joining( "\n" ) );
        this._elasticClient.createMappings( index, mappings );
    }

    @Override
    public void deleteIndex( final String index ) throws ElasticClientException
    {
        this._elasticClient.deleteIndex( index );
    }

    @Override
    public void makeIndexReadOnly( final String index ) throws ElasticClientException
    {
        final String settings = "{ \"index.blocks.write\": true }";
        this._elasticClient.updateSettings( index, settings );
    }

    @Override
    public void createOrUpdateAlias( final String oldIndex, final String newIndex, final String alias )
    {
        boolean aliasExists = false;
        try
        {
            aliasExists = this._elasticClient.getAlias( alias ) != null;
        }
        catch( final ElasticClientException e )
        {
            aliasExists = false;
        }

        try
        {
            if ( aliasExists )
            {
                if ( StringUtils.isNotEmpty( oldIndex ) )
                {
                    this._elasticClient.deleteAlias( oldIndex, alias );
                }
                else
                {
                    this._elasticClient.deleteAlias( "*", alias );
                }
            }

            if ( StringUtils.isNotEmpty( newIndex ) )
            {
                this._elasticClient.createAlias( newIndex, alias );
            }
        }
        catch( ElasticClientException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public String getIndexBehindAlias( final String alias )
    {
        try
        {
            final String response = this._elasticClient.getAlias( alias );
            final ObjectMapper mapper = new ObjectMapper( );
            final JsonNode node = mapper.readTree( response );

            if ( node != null )
            {
                final Iterator<String> iterator = node.fieldNames( );
                if ( iterator != null && iterator.hasNext( ) )
                {
                    return iterator.next( );
                }
            }
        }
        catch( ElasticClientException | JsonProcessingException e )
        {
            return null;
        }
        return null;
    }

    @Override
    public boolean isAlive( )
    {
        return _elasticClient.isAlive( );
    }

    @Override
    public boolean indexExists( final String index )
    {
        try
        {
            return _elasticClient.isExists( index );
        }
        catch( ElasticClientException e )
        {
            return false;
        }
    }

    private void handleError( final String documentId, final IndexActionType actionType )
    {
        final IndexAction indexAction = new IndexAction( actionType, documentId );
        IndexActionHome.create( indexAction );
    }
}

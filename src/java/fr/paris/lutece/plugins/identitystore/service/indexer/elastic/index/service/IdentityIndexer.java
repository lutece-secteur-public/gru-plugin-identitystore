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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias.AliasAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias.AliasActions;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

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
    private final String CURRENT_INDEX_ALIAS = AppPropertiesService.getProperty( "identitystore.elastic.client.identities.alias", "identities-alias" );

    private final static ObjectMapper _mapper = new ObjectMapper( ).disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    private final ElasticClient _elasticClient;

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
            if ( !this._elasticClient.isExists( CURRENT_INDEX_ALIAS ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.initIndex( newIndex );
                this.addAliasOnIndex( newIndex, CURRENT_INDEX_ALIAS );
            }

            this._elasticClient.create( CURRENT_INDEX_ALIAS, identity.getCustomerId( ), identity );
            AppLogService.debug( "Indexed document: " + identity.getCustomerId( ) );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.CREATE );
            AppLogService.error( "Failed to index (creation) identity " + identity.getCustomerId( ), e );
        }
    }

    @Override
    public boolean bulk( final List<BulkAction> bulkActions, final String index ) throws IdentityStoreException
    {
        try
        {
            this._elasticClient.indexByBulk( index, bulkActions );
            return true;
        }
        catch( final ElasticClientException e )
        {
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    @Override
    public void update( final IdentityObject identity, final String index )
    {
        try
        {
            if ( !this._elasticClient.isExists( CURRENT_INDEX_ALIAS ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.initIndex( newIndex );
                this.addAliasOnIndex( newIndex, CURRENT_INDEX_ALIAS );
            }

            this._elasticClient.update( CURRENT_INDEX_ALIAS, identity.getCustomerId( ), identity );
            AppLogService.debug( "Indexed document: " + identity.getCustomerId( ) );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( identity.getCustomerId( ), IndexActionType.UPDATE );
            AppLogService.error( "Failed to index (update) identity " + identity.getCustomerId( ), e );
        }
    }

    @Override
    public void delete( final String documentId, final String index )
    {
        try
        {
            this._elasticClient.deleteDocument( CURRENT_INDEX_ALIAS, documentId );
            AppLogService.debug( "Removed identity : " + documentId );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( documentId, IndexActionType.DELETE );
            AppLogService.error( "Failed to remove identity " + documentId, e );
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
    public void removeIndexReadOnly( final String index ) throws ElasticClientException
    {
        final String settings = "{ \"index.blocks.write\": false }";
        this._elasticClient.updateSettings( index, settings );
    }

    @Override
    public void addAliasOnIndex( final String newIndex, final String alias )
    {
        try
        {
            final AliasActions actions = new AliasActions( );
            final AliasAction remove = new AliasAction( );
            remove.setName( "remove" );
            remove.setAlias( alias );
            remove.setIndex( "*" );
            actions.addAction( remove );

            final AliasAction add = new AliasAction( );
            add.setName( "add" );
            add.setAlias( alias );
            add.setIndex( newIndex );
            actions.addAction( add );

            this._elasticClient.addAliasOnIndex( actions );
        }
        catch( final ElasticClientException e )
        {
            AppLogService.error( "Unexpected error occurred while managing ES alias.", e );
            throw new AppException( "Unexpected error occurred while managing ES alias.", e );
        }
    }

    @Override
    public String getIndexBehindAlias( final String alias )
    {
        try
        {
            final String response = this._elasticClient.getAlias( alias );
            final JsonNode node = _mapper.readTree( response );

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
            AppLogService.error( "Failed to get index behind alias", e );
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
        catch( final ElasticClientException e )
        {
            AppLogService.error( "Failed to check index existence", e );
            return false;
        }
    }

    @Override
    public boolean isIndexWriteable( String index )
    {
        return _elasticClient.isWriteable( index );
    }

    @Override
    public boolean aliasExists( String index )
    {
        return false;
    }

    private void handleError( final String documentId, final IndexActionType actionType )
    {
        final IndexAction indexAction = new IndexAction( actionType, documentId );
        IndexActionHome.create( indexAction );
    }
}

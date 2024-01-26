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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityObjectHome;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FullIndexTask extends AbstractIndexTask
{
    private final int BATCH_SIZE = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.batch.size", 1000 );
    private final int MAX_RETRY = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.retry.max", 500 );
    private final boolean ACTIVE = AppPropertiesService.getPropertyBoolean( "identitystore.task.reindex.active", false );
    private final String ELASTIC_URL = AppPropertiesService.getProperty( "elasticsearch.url" );
    private final String ELASTIC_USER = AppPropertiesService.getProperty( "elasticsearch.user", "" );
    private final String ELASTIC_PWD = AppPropertiesService.getProperty( "elasticsearch.pwd", "" );

    public FullIndexTask( )
    {
    }

    public void run( )
    {
        if ( ACTIVE )
        {
            AppLogService.info( "Full index task is active." );
            this.doJob( );
        }
        else
        {
            AppLogService.info( "Full index task is not active." );
        }
    }

    public void doJob( )
    {
        final StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        this.init( );
        this.getStatus( ).log( "Starting identities full reindex at " + DateFormatUtils.format( stopWatch.getStartTime( ), "dd-MM-yyyy'T'HH:mm:ss" ) );
        final IIdentityIndexer identityIndexer = this.createIdentityIndexer( );
        if ( identityIndexer.isAlive( ) )
        {
            final String newIndex = "identities-" + UUID.randomUUID( );
            this.getStatus( ).log( "ES available :: indexing" );
            try
            {
                this.getStatus( ).log( "Creating new index : " + newIndex );
                identityIndexer.initIndex( newIndex );

                if ( identityIndexer.indexExists( IIdentityIndexer.CURRENT_INDEX_ALIAS ) )
                {
                    this.getStatus( ).log( "Set current index READ-ONLY" );
                    identityIndexer.makeIndexReadOnly( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }
                else
                {
                    identityIndexer.createOrUpdateAlias( "", newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }

                final List<Integer> identityIdsList = new ArrayList<>( IdentityObjectHome.getEligibleIdListForIndex( ) );
                this.getStatus( ).setNbTotalIdentities( identityIdsList.size( ) );
                this.getStatus( ).log( "NB identities to be indexed : " + this.getStatus( ).getNbTotalIdentities( ) );
                this.getStatus( ).log( "Size of indexing batches : " + BATCH_SIZE );
                final Batch<Integer> batch = Batch.ofSize( identityIdsList, BATCH_SIZE );
                this.getStatus( ).log( "NB of indexing batches : " + batch.size( ) );
                final AtomicInteger failedCalls = new AtomicInteger( 0 );
                batch.stream( ).parallel( ).forEach( identityIdList -> {
                    boolean processed = this.process( identityIdList, newIndex );
                    while ( !processed )
                    {
                        final int nbRetry = failedCalls.getAndIncrement( );
                        this.getStatus( ).log( "Retry nb " + nbRetry );
                        if ( nbRetry > MAX_RETRY )
                        {
                            this.getStatus( ).log( "The number of retries exceeds the configured value of " + MAX_RETRY + ", interrupting.." );
                            break;
                        }
                        try
                        {
                            Thread.sleep( 1000 );
                        }
                        catch( InterruptedException e )
                        {
                            this.getStatus( ).log( "Could thread sleep.. + " + e.getMessage( ) );
                        }
                        processed = this.process( identityIdList, newIndex );
                    }
                } );
                this.getStatus( ).log( "All batches processed, now switch alias to publish new index.." );
                final String oldIndex = identityIndexer.getIndexBehindAlias( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                if ( !StringUtils.equals( oldIndex, newIndex ) )
                {
                    this.getStatus( ).log( "Old index id: " + oldIndex );
                    identityIndexer.createOrUpdateAlias( oldIndex, newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                    if ( oldIndex != null )
                    {
                        this.getStatus( ).log( "Delete old index : " + oldIndex );
                        identityIndexer.deleteIndex( oldIndex );
                    }
                }
            }
            catch( final ElasticClientException e )
            {
                this.getStatus( ).log( "Failed to reindex " + e.getMessage( ) );
            }
            // Index pending identities
            new MissingIndexTask( ).run( );
        }
        else
        {
            this.getStatus( ).log( "[ERROR] ES not available" );
        }
        stopWatch.stop( );
        final String duration = DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );

        if ( this.getStatus( ).getNbTotalIdentities( ) > 0 )
        {
            this.getStatus( ).log( "Re-indexed  " + this.getStatus( ).getNbTotalIdentities( ) + " identities in " + duration );
        }
        else
        {
            this.getStatus( ).log( "Re-indexed failed" );
        }
        this.close( );
    }

    private boolean process( final List<Integer> identityIdList, final String newIndex )
    {
        final List<IdentityObject> identityObjects = IdentityObjectHome.loadEligibleIdentitiesForIndex( identityIdList );
        final List<BulkAction> actions = identityObjects.stream( )
                .map( identityObject -> new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.INDEX ) )
                .collect( Collectors.toList( ) );
        final boolean bulked = this.createIdentityIndexer( ).bulk( actions, newIndex );
        if ( bulked )
        {
            this.getStatus( ).incrementCurrentNbIndexedIdentities( identityObjects.size( ) );
        }
        return bulked;
    }

    public IIdentityIndexer createIdentityIndexer( )
    {
        if ( StringUtils.isAnyBlank( ELASTIC_USER, ELASTIC_PWD ) )
        {
            AppLogService.debug( "Creating elastic connection without authentification" );
            return new IdentityIndexer( ELASTIC_URL );
        }
        else
        {
            AppLogService.debug( "Creating elastic connection with authentification" );
            return new IdentityIndexer( ELASTIC_URL, ELASTIC_USER, ELASTIC_PWD );
        }
    }
}

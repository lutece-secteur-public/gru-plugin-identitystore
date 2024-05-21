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
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityObjectHome;
import fr.paris.lutece.plugins.identitystore.service.network.DelayedNetworkService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FullIndexTask extends AbstractIndexTask implements UsingElasticConnection
{
    private final String CURRENT_INDEX_ALIAS = AppPropertiesService.getProperty( "identitystore.elastic.client.identities.alias", "identities-alias" );
    private final int BATCH_SIZE = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.batch.size", 1000 );
    private final boolean ACTIVE = AppPropertiesService.getPropertyBoolean( "identitystore.task.reindex.active", false );

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
        final List<Integer> identityIdsList = new ArrayList<>( IdentityObjectHome.getEligibleIdListForIndex( ) );
        if ( !identityIdsList.isEmpty( ) )
        {
            this.debug( "Starting identities full reindex at " + DateFormatUtils.format( stopWatch.getStartTime( ), "dd-MM-yyyy'T'HH:mm:ss" ) );
            final IIdentityIndexer identityIndexer = this.createIdentityIndexer( );
            if ( identityIndexer.isAlive( ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.debug( "ES available :: indexing" );
                try
                {
                    this.debug( "Creating new index : " + newIndex );
                    identityIndexer.initIndex( newIndex );

                    if ( identityIndexer.indexExists( CURRENT_INDEX_ALIAS ) )
                    {
                        this.debug( "Set current index READ-ONLY" );
                        identityIndexer.makeIndexReadOnly( CURRENT_INDEX_ALIAS );
                    }
                    else
                    {
                        this.debug( "Create alias" );
                        identityIndexer.addAliasOnIndex( newIndex, CURRENT_INDEX_ALIAS );
                    }

                    this.getStatus( ).setNbTotalIdentities( identityIdsList.size( ) );
                    this.debug( "NB identities to be indexed : " + this.getStatus( ).getNbTotalIdentities( ) );
                    this.debug( "Size of indexing batches : " + BATCH_SIZE );
                    final Batch<Integer> batch = Batch.ofSize( identityIdsList, BATCH_SIZE );
                    this.debug( "NB of indexing batches : " + batch.size( ) );
                    batch.stream( ).parallel( ).forEach( identityIdList -> {
                        this.process( identityIdList, newIndex );
                    } );
                    this.debug( "All batches processed, now switch alias to publish new index.." );
                    final String oldIndex = identityIndexer.getIndexBehindAlias( CURRENT_INDEX_ALIAS );
                    if ( !StringUtils.equals( oldIndex, newIndex ) )
                    {
                        this.debug( "Old index id: " + oldIndex );
                        identityIndexer.addAliasOnIndex( newIndex, CURRENT_INDEX_ALIAS );
                        if ( oldIndex != null )
                        {
                            this.debug( "Delete old index : " + oldIndex );
                            identityIndexer.removeIndexReadOnly( oldIndex );
                            identityIndexer.deleteIndex( oldIndex );
                        }
                    }
                }
                catch( final ElasticClientException e )
                {
                    this.debug( "Failed to reindex " + e.getMessage( ) );
                    final String oldIndex = identityIndexer.getIndexBehindAlias( CURRENT_INDEX_ALIAS );
                    this.rollbackIndexCreation( oldIndex, newIndex, identityIndexer );
                }
            }
            else
            {
                this.debug( "[ERROR] ES not available" );
            }
            stopWatch.stop( );
            final String duration = DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );
            this.debug( "Re-indexed  " + this.getStatus( ).getCurrentNbIndexedIdentities( ) + " identities in " + duration );
        }
        else
        {
            stopWatch.stop( );
            this.debug( "No index in database" );
        }
        this.close( );
    }

    private void process( final List<Integer> identityIdList, final String newIndex )
    {
        final List<IdentityObject> identityObjects = IdentityObjectHome.loadEligibleIdentitiesForIndex( identityIdList );
        final List<BulkAction> actions = identityObjects.stream( )
                .map( identityObject -> new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.INDEX ) )
                .collect( Collectors.toList( ) );

        boolean bulked = false;
        try
        {
            bulked = new DelayedNetworkService<Boolean>().call( ( ) -> this.createIdentityIndexer( ).bulk( actions, newIndex ), "Index identities by bulk", this);
        }
        catch ( final IdentityStoreException e )
        {
            AppLogService.error("An error occurred while bulking: " + e.getMessage( ) );
        }

        if ( bulked )
        {
            this.getStatus( ).incrementCurrentNbIndexedIdentities( identityObjects.size( ) );
        }
    }

    public void rollbackIndexCreation( String oldIndex, String newIndex, IIdentityIndexer identityIndexer )
    {
        try
        {
            identityIndexer.deleteIndex( newIndex );
            identityIndexer.removeIndexReadOnly( oldIndex );
        }
        catch( ElasticClientException e )
        {
            this.debug( "Failed to rollback " + e.getMessage( ) );
        }
        finally {
            this.close();
        }
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityObjectHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.utils.Batch;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FullIndexTask extends Daemon
{
    private IIdentityIndexer _identityIndexer = SpringContextService.getBean( IIdentityIndexer.NAME );

    @Override
    public void run( )
    {
        final StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        final int batchSize = AppPropertiesService.getPropertyInt( "task.reindex.batch.size", 1000 );
        final List<String> customerIdsList = new ArrayList<>( );
        if ( _identityIndexer.isAlive( ) )
        {
            final String newIndex = "identities-" + UUID.randomUUID( );
            AppLogService.info( "ES available :: indexing" );
            try
            {
                AppLogService.info( "Creating new index : " + newIndex );
                this._identityIndexer.initIndex( newIndex );

                if ( this._identityIndexer.indexExists( IIdentityIndexer.CURRENT_INDEX_ALIAS ) )
                {
                    AppLogService.info( "Set current index READ-ONLY" );
                    this._identityIndexer.makeIndexReadOnly( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }
                else
                {
                    this._identityIndexer.createOrUpdateAlias( "", newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                }

                final List<String> eligibleCustomerIdsListForIndex = IdentityObjectHome.getEligibleCustomerIdsListForIndex( );
                customerIdsList.addAll( eligibleCustomerIdsListForIndex );
                AppLogService.info( "NB identies to be indexed : " + customerIdsList.size( ) );
                AppLogService.info( "Size of indexing batches : " + batchSize );
                final Batch<String> batch = Batch.ofSize( customerIdsList, batchSize );
                AppLogService.info( "NB of indexing batches : " + batch.size( ) );
                final AtomicInteger batchCounter = new AtomicInteger( );
                batch.stream( ).parallel( ).forEach( customerIds -> {
                    AppLogService.info( "Processing batch : " + batchCounter.incrementAndGet( ) );
                    final List<BulkAction> actions = customerIds.stream( ).map( IdentityObjectHome::findByCustomerId )
                            .map( identityObject -> new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.INDEX ) )
                            .collect( Collectors.toList( ) );
                    _identityIndexer.bulk( actions, newIndex );
                } );
                AppLogService.info( "All batches processed, now switch alias to publish new index.." );
                final String oldIndex = this._identityIndexer.getIndexBehindAlias( IIdentityIndexer.CURRENT_INDEX_ALIAS );
                if ( !StringUtils.equals( oldIndex, newIndex ) )
                {
                    AppLogService.info( "Old index id: " + oldIndex );
                    this._identityIndexer.createOrUpdateAlias( oldIndex, newIndex, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                    if ( oldIndex != null )
                    {
                        AppLogService.info( "Delete old index : " + oldIndex );
                        this._identityIndexer.deleteIndex( oldIndex );
                    }
                }
            }
            catch( final ElasticClientException e )
            {
                AppLogService.info( "Failed to reindex " + e.getMessage( ) );
            }
        }
        else
        {
            AppLogService.info( "[ERROR] ES not available" );
        }
        stopWatch.stop( );
        final String duration = DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );

        if ( CollectionUtils.isNotEmpty( customerIdsList ) )
        {
            AppLogService.info( "Re-indexed  " + customerIdsList.size( ) + " identities in " + duration );
        }
        else
        {
            AppLogService.info( "Re-indexed failed" );
        }
    }
}

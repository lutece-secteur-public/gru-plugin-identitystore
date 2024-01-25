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

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityObjectHome;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

public class MissingIndexTask extends Daemon
{
    private final IIdentityIndexer _identityIndexer = SpringContextService.getBean( "identitystore.elasticsearch.identityIndexer" );

    @Override
    public void run( )
    {

        final StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        final int batchSize = AppPropertiesService.getPropertyInt( "task.missingindex.batch.size", 1000 );
        final List<BulkAction> bulkActions = new ArrayList<>( );
        if ( _identityIndexer.isAlive( ) )
        {
            AppLogService.debug( "ES available :: indexing" );
            final List<IndexAction> indexActions = IndexActionHome.selectAll( );
            for ( final IndexAction indexAction : indexActions )
            {
                final IdentityObject identityObject = IdentityObjectHome.findByCustomerId( indexAction.getCustomerId( ) );
                switch( indexAction.getActionType( ) )
                {
                    case CREATE:
                    case UPDATE:
                        bulkActions.add( new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.INDEX ) );
                        break;
                    case DELETE:
                        bulkActions.add( new BulkAction( identityObject.getCustomerId( ), identityObject, BulkActionType.DELETE ) );
                        break;
                    default:
                        break;
                }
            }
            AppLogService.debug( "NB identies to be indexed : " + bulkActions.size( ) );
            AppLogService.debug( "Size of indexing batches : " + batchSize );
            final Batch<BulkAction> batch = Batch.ofSize( bulkActions, batchSize );
            AppLogService.debug( "NB of indexing batches : " + batch.size( ) );
            int batchCounter = 0;
            for ( final List<BulkAction> batchActions : batch )
            {
                AppLogService.debug( "Processing batch : " + ++batchCounter );
                // TODO handle bulk error
                this._identityIndexer.bulk( batchActions, IIdentityIndexer.CURRENT_INDEX_ALIAS );
            }

            // Clean processed actions
            AppLogService.debug( "Indexing over, clean processed actions in database " );
            indexActions.forEach( IndexActionHome::delete );
        }
        else
        {
            AppLogService.error( "[ERROR] ES not available" );
        }
        stopWatch.stop( );
        final String duration = DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );

        if ( CollectionUtils.isNotEmpty( bulkActions ) )
        {
            AppLogService.debug( "Indexed  " + bulkActions.size( ) + " identities in " + duration );
        }
        else
        {
            AppLogService.debug( "No missing index to process" );
        }
    }
}

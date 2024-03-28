package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryService
{
    protected final int MAX_RETRY = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.retry.max", 500 );
    protected final int TEMPO_RETRY = AppPropertiesService.getPropertyInt( "identitystore.task.reindex.retry.wait", 100 );
    private final String ELASTIC_URL = AppPropertiesService.getProperty( "elasticsearch.url" );
    private final String ELASTIC_USER = AppPropertiesService.getProperty( "elasticsearch.user", "" );
    private final String ELASTIC_PWD = AppPropertiesService.getProperty( "elasticsearch.pwd", "" );

    public boolean callBulkWithRetry( final List<BulkAction> bulkActions, final String index )
    {
        final IIdentityIndexer identityIndexer = this.createIdentityIndexer( );
        final AtomicInteger failedCalls = new AtomicInteger( 0 );
        boolean processed = identityIndexer.bulk( bulkActions, IIdentityIndexer.CURRENT_INDEX_ALIAS );
        while ( !processed )
        {
            final int nbRetry = failedCalls.getAndIncrement( );
            AppLogService.error( "Retry nb " + nbRetry );
            if ( nbRetry > MAX_RETRY )
            {
                AppLogService.error( "The number of retries exceeds the configured value of " + MAX_RETRY + ", interrupting.." );
                return false;
            }
            try
            {
                Thread.sleep( TEMPO_RETRY );
            }
            catch( InterruptedException e )
            {
                AppLogService.error( "Could thread sleep.. + " + e.getMessage( ) );
            }
            processed = identityIndexer.bulk( bulkActions, IIdentityIndexer.CURRENT_INDEX_ALIAS );
        }
        return true;
    }

    private IIdentityIndexer createIdentityIndexer( )
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

package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityIndexer;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

public interface UsingElasticConnection {
    String ELASTIC_URL = AppPropertiesService.getProperty( "elasticsearch.url" );
    String ELASTIC_USER = AppPropertiesService.getProperty( "elasticsearch.user", "" );
    String ELASTIC_PWD = AppPropertiesService.getProperty( "elasticsearch.pwd", "" );

    default IIdentityIndexer createIdentityIndexer()
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

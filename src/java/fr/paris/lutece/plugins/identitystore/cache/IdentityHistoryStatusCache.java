package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.List;

public class IdentityHistoryStatusCache extends AbstractCacheableService
{
    public static final String SERVICE_NAME = "IdentityHistoryCache";

    public IdentityHistoryStatusCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init Identity History status cache" );
        this.resetCache( );
    }

    public void put(List<String> statusList, String statusName)
    {
        if ( this.getKeys( ).contains( statusName ) )
        {
            this.removeKey( statusName );
        }
        this.putInCache( statusName, statusList );
        AppLogService.debug( "History status added to cache: " + statusName );
    }

    public void remove(String statusName)
    {
        if ( this.getKeys( ).contains( statusName ) )
        {
            this.removeKey( statusName );
        }

        AppLogService.debug( "History status removed from cache: " + statusName );
    }

    public List<String> get(String statusName)
    {
        List<String> statusList = (List<String>) this.getFromCache( statusName );
        if ( statusList == null )
        {
            statusList = IdentityHome.getHistoryStatusList();;
            this.put( statusList, statusName );
        }
        return statusList;
    }

    public List<String> getStatusList(String statusName)
    {
        return this.get(statusName);
    }

    @Override
    public String getName()
    {
        return SERVICE_NAME;
    }
}

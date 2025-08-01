package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

public class SuspicionActionHome {
    private static final ISuspicionActionDao _dao = SpringContextService.getBean(ISuspicionActionDao.BEAN_NAME);
    private static final Plugin _plugin = PluginService.getPlugin(IdentityStorePlugin.PLUGIN_NAME);

    private SuspicionActionHome( )
    {
    }

    public static SuspicionAction create(final SuspicionAction suspicionAction)
    {
        _dao.insert( suspicionAction, _plugin );

        return suspicionAction;
    }

    public static void delete( final List<Integer> ids)
    {
        _dao.delete( ids, _plugin );
    }

    public static List<SuspicionAction> selectWithLimit( final int limit )
    {
        return _dao.select( limit, _plugin );
    }

    public static List<SuspicionAction> selectAll( )
    {
        return _dao.selectAll( _plugin );
    }
}

package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.List;

public interface ISuspicionActionDao {
    String BEAN_NAME = "identitystore.suspicionActionDAO";

    void insert( final SuspicionAction suspicionAction, final Plugin plugin );

    void delete( final List<Integer> ids, final Plugin plugin );

    List<SuspicionAction> select( int limit, final Plugin plugin );

    List<SuspicionAction> selectAll( Plugin plugin );
}

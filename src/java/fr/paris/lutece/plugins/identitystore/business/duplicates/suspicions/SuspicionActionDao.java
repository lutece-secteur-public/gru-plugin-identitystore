package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SuspicionActionDao implements ISuspicionActionDao {

    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_suspicion_action ( customer_id, action_type, date ) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_suspicion_action WHERE id_suspicion_action IN ";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_suspicion_action, customer_id, action_type, date FROM identitystore_suspicion_action ORDER BY date asc";
    private static final String SQL_QUERY_SELECTALL_WITH_LIMIT = SQL_QUERY_SELECTALL + " LIMIT ?";

    @Override
    public void insert(final SuspicionAction suspicionAction, final Plugin plugin) {
        try ( final DAOUtil daoUtil = new DAOUtil(SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, suspicionAction.getCustomerId( ) );
            daoUtil.setString( nIndex++, suspicionAction.getActionType( ) );
            daoUtil.setTimestamp( nIndex, new Timestamp(new Date( ).getTime() ));
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void delete(final List<Integer> ids, final Plugin plugin) {
        final String sqlQueryDelete = SQL_QUERY_DELETE + " (" + ids.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(",")) + ")" ;
        try (final DAOUtil daoUtil = new DAOUtil(sqlQueryDelete, plugin ) )
        {
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<SuspicionAction> select(final int limit, final Plugin plugin) {
        final List<SuspicionAction> actions = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_WITH_LIMIT, plugin ) )
        {
            daoUtil.setInt( 1, limit );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final SuspicionAction action = new SuspicionAction( );
                int nIndex = 1;

                action.setId( daoUtil.getInt( nIndex++ ) );
                action.setCustomerId( daoUtil.getString( nIndex++ ) );
                action.setActionType(daoUtil.getString( nIndex++ ));
                action.setDate( daoUtil.getDate( nIndex ) );
                actions.add( action );
            }

            return actions;
        }
    }

    @Override
    public List<SuspicionAction> selectAll(final Plugin plugin) {
        final List<SuspicionAction> actions = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final SuspicionAction action = new SuspicionAction( );
                int nIndex = 1;

                action.setId( daoUtil.getInt( nIndex++ ) );
                action.setCustomerId( daoUtil.getString( nIndex++ ) );
                action.setActionType(daoUtil.getString( nIndex++ ));
                action.setDate( daoUtil.getDate( nIndex ) );
                actions.add( action );
            }

            return actions;
        }
    }
}

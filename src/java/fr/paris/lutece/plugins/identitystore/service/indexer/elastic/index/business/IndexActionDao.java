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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.sql.DAOUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IndexActionDao implements IIndexActionDao
{

    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_index_action ( customer_id, action_type, date_index ) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_index_action WHERE id_index_action IN ";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_index_action, customer_id, action_type, date_index FROM identitystore_index_action ORDER BY date_index asc";
    private static final String SQL_QUERY_SELECTALL_WITH_LIMIT = SQL_QUERY_SELECTALL + " LIMIT ?";
    private static final String SQL_QUERY_SELECTALL_WITH_CUID_AND_TYPE = "SELECT id_index_action, customer_id, action_type, date_index FROM identitystore_index_action WHERE customer_id = ? AND action_type = ? ORDER BY date_index asc";

    @Override
    public void insert( IndexAction indexAction, Plugin plugin )
    {
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setString( nIndex++, indexAction.getCustomerId( ) );
            daoUtil.setString( nIndex++, indexAction.getActionType( ).name( ) );
            daoUtil.setTimestamp( nIndex, new Timestamp( new Date( ).getTime( ) ) );
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public void delete( final List<Integer> ids, Plugin plugin )
    {
        final String sqlQueryDelete = SQL_QUERY_DELETE + " (" + ids.stream().filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(",")) + ")" ;
        try (final DAOUtil daoUtil = new DAOUtil(sqlQueryDelete, plugin ) )
        {
            daoUtil.executeUpdate( );
        }
    }

    @Override
    public List<IndexAction> select( int limit, Plugin plugin )
    {
        final List<IndexAction> actions = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_WITH_LIMIT, plugin ) )
        {
            daoUtil.setInt( 1, limit );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final IndexAction indexAction = new IndexAction( );
                int nIndex = 1;

                indexAction.setId( daoUtil.getInt( nIndex++ ) );
                indexAction.setCustomerId( daoUtil.getString( nIndex++ ) );
                indexAction.setActionType( IndexActionType.valueOf( daoUtil.getString( nIndex++ ) ) );
                indexAction.setDateIndex( daoUtil.getDate( nIndex ) );
                actions.add( indexAction );
            }

            return actions;
        }
    }

    @Override
    public List<IndexAction> selectAll( Plugin plugin )
    {
        final List<IndexAction> actions = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final IndexAction indexAction = new IndexAction( );
                int nIndex = 1;

                indexAction.setId( daoUtil.getInt( nIndex++ ) );
                indexAction.setCustomerId( daoUtil.getString( nIndex++ ) );
                indexAction.setActionType( IndexActionType.valueOf( daoUtil.getString( nIndex++ ) ) );
                indexAction.setDateIndex( daoUtil.getDate( nIndex ) );
                actions.add( indexAction );
            }

            return actions;
        }
    }

    @Override
    public List<IndexAction> selectWithCuidAndActionType(final String cuid, final String type, final Plugin plugin ) {
        final List<IndexAction> actions = new ArrayList<>( );
        try ( final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_WITH_CUID_AND_TYPE, plugin ) )
        {
            int index = 1;
            daoUtil.setString( index++, cuid);
            daoUtil.setString( index++, type);
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                final IndexAction indexAction = new IndexAction( );
                int nIndex = 1;

                indexAction.setId( daoUtil.getInt( nIndex++ ) );
                indexAction.setCustomerId( daoUtil.getString( nIndex++ ) );
                indexAction.setActionType( IndexActionType.valueOf( daoUtil.getString( nIndex++ ) ) );
                indexAction.setDateIndex( daoUtil.getDate( nIndex ) );
                actions.add( indexAction );
            }

            return actions;
        }
    }

}

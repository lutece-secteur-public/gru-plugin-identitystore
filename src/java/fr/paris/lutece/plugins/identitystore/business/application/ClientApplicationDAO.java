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
package fr.paris.lutece.plugins.identitystore.business.application;

import java.util.ArrayList;
import java.util.List;

import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.sql.DAOUtil;

/**
 * This class provides Data Access methods for ClientApplication objects
 */
public final class ClientApplicationDAO implements IClientApplicationDAO
{
    // Constants
    private static final String SQL_QUERY_NEW_PK = "SELECT max( id_client_app ) FROM identitystore_client_application";
    private static final String SQL_QUERY_SELECT = "SELECT id_client_app, name, code  FROM identitystore_client_application WHERE id_client_app = ?";
    private static final String SQL_QUERY_SELECT_BY_CODE = "SELECT id_client_app, name, code FROM identitystore_client_application WHERE code = ?";
    private static final String SQL_QUERY_INSERT = "INSERT INTO identitystore_client_application ( id_client_app, name, code) VALUES ( ?, ?, ? ) ";
    private static final String SQL_QUERY_DELETE = "DELETE FROM identitystore_client_application WHERE id_client_app = ? ";
    private static final String SQL_QUERY_UPDATE = "UPDATE identitystore_client_application SET id_client_app = ?, name = ?, code = ? WHERE id_client_app = ?";
    private static final String SQL_QUERY_SELECTALL = "SELECT id_client_app, name, code FROM identitystore_client_application";
    private static final String SQL_QUERY_SELECTALL_ID = "SELECT id_client_app FROM identitystore_client_application";
    private static final String SQL_QUERY_SELECTALL_CERTIFICATE_APP = "SELECT certifier_code FROM identitystore_client_application_certifiers WHERE id_client_app = ? ";
    private static final String SQL_QUERY_SELECTALL_APP_CERTIFIER = "SELECT ica.id_client_app, ica.name, ica.code FROM identitystore_client_application ica JOIN identitystore_client_application_certifiers icac ON ica.id_client_app=icac.id_client_app WHERE icac.certifier_code = ? ORDER BY ica.id_client_app";
    private static final String SQL_QUERY_ADD_CERTIFICATE_APP = "INSERT INTO identitystore_client_application_certifiers (id_client_app, certifier_code) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE_CERTIFICATE_APP = "DELETE FROM identitystore_client_application_certifiers WHERE id_client_app = ? AND certifier_code = ?";
    private static final String SQL_QUERY_DELETE_ALL_CERTIFICATE_APP = "DELETE FROM identitystore_client_application_certifiers WHERE id_client_app = ? ";
    private static final String SQL_QUERY_SELECT_BY_CONTRACT_ID = "SELECT a.id_client_app, a.name, a.code" + " FROM identitystore_client_application a"
            + " JOIN identitystore_service_contract b ON a.id_client_app = b.id_client_app" + " WHERE b.id_service_contract = ?";

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            The Plugin
     * @return The new primary key
     */
    private synchronized int newPrimaryKey( Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PK, plugin );
        daoUtil.executeQuery( );

        int nKey = 1;

        if ( daoUtil.next( ) )
        {
            nKey = daoUtil.getInt( 1 ) + 1;
        }

        daoUtil.free( );

        return nKey;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert( ClientApplication clientApplication, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        clientApplication.setId( newPrimaryKey( plugin ) );

        int nIndex = 1;

        daoUtil.setInt( nIndex++, clientApplication.getId( ) );
        daoUtil.setString( nIndex++, clientApplication.getName( ) );
        daoUtil.setString( nIndex++, clientApplication.getCode( ) );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ClientApplication load( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery( );

        ClientApplication clientApplication = null;

        if ( daoUtil.next( ) )
        {
            clientApplication = new ClientApplication( );

            int nIndex = 1;

            clientApplication.setId( daoUtil.getInt( nIndex++ ) );
            clientApplication.setName( daoUtil.getString( nIndex++ ) );
            clientApplication.setCode( daoUtil.getString( nIndex++ ) );

        }

        daoUtil.free( );

        return clientApplication;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ClientApplication selectByContractId( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CONTRACT_ID, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery( );

        ClientApplication clientApplication = null;

        if ( daoUtil.next( ) )
        {
            clientApplication = new ClientApplication( );

            int nIndex = 1;

            clientApplication.setId( daoUtil.getInt( nIndex++ ) );
            clientApplication.setName( daoUtil.getString( nIndex++ ) );
            clientApplication.setCode( daoUtil.getString( nIndex++ ) );

        }

        daoUtil.free( );

        return clientApplication;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void store( ClientApplication clientApplication, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE, plugin );
        int nIndex = 1;

        daoUtil.setInt( nIndex++, clientApplication.getId( ) );
        daoUtil.setString( nIndex++, clientApplication.getName( ) );
        daoUtil.setString( nIndex++, clientApplication.getCode( ) );

        daoUtil.setInt( nIndex, clientApplication.getId( ) );

        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<ClientApplication> selectClientApplicationList( Plugin plugin )
    {
        final List<ClientApplication> clientApplicationList = new ArrayList<>( );
        final DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            final ClientApplication clientApplication = new ClientApplication( );
            int nIndex = 1;

            clientApplication.setId( daoUtil.getInt( nIndex++ ) );
            clientApplication.setName( daoUtil.getString( nIndex++ ) );
            clientApplication.setCode( daoUtil.getString( nIndex++ ) );

            clientApplicationList.add( clientApplication );
        }

        daoUtil.free( );

        return clientApplicationList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<Integer> selectIdClientApplicationList( Plugin plugin )
    {
        List<Integer> clientApplicationList = new ArrayList<Integer>( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_ID, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            clientApplicationList.add( daoUtil.getInt( 1 ) );
        }

        daoUtil.free( );

        return clientApplicationList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ReferenceList selectClientApplicationReferenceList( Plugin plugin )
    {
        ReferenceList clientApplicationList = new ReferenceList( );
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL, plugin );
        daoUtil.executeQuery( );

        while ( daoUtil.next( ) )
        {
            clientApplicationList.addItem( daoUtil.getInt( 1 ), daoUtil.getString( 2 ) );
        }

        daoUtil.free( );

        return clientApplicationList;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ClientApplication selectByCode( String strCode, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_CODE, plugin );
        daoUtil.setString( 1, strCode );
        daoUtil.executeQuery( );

        ClientApplication clientApplication = null;

        if ( daoUtil.next( ) )
        {
            clientApplication = new ClientApplication( );

            int nIndex = 1;

            clientApplication.setId( daoUtil.getInt( nIndex++ ) );
            clientApplication.setName( daoUtil.getString( nIndex++ ) );
            clientApplication.setCode( daoUtil.getString( nIndex++ ) );

        }

        daoUtil.free( );

        return clientApplication;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractCertifier> getCertifiers( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_CERTIFICATE_APP, plugin );
        daoUtil.setInt( 1, nKey );
        daoUtil.executeQuery( );

        List<AbstractCertifier> listCertifiers = new ArrayList<AbstractCertifier>( );

        while ( daoUtil.next( ) )
        {
            String strCertifCode = daoUtil.getString( 1 );
            try
            {
                AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( strCertifCode );
                listCertifiers.add( certifier );
            }
            catch( CertifierNotFoundException e )
            {
                // nothing
            }
        }

        daoUtil.free( );

        return listCertifiers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientApplication> getClientApplications( String strCertifier, Plugin plugin )
    {

        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECTALL_APP_CERTIFIER, plugin );
        daoUtil.setString( 1, strCertifier );
        daoUtil.executeQuery( );

        List<ClientApplication> listClientApplications = new ArrayList<ClientApplication>( );
        ClientApplication clientApplication = null;

        while ( daoUtil.next( ) )
        {
            clientApplication = new ClientApplication( );

            int nIndex = 1;

            clientApplication.setId( daoUtil.getInt( nIndex++ ) );
            clientApplication.setName( daoUtil.getString( nIndex++ ) );
            clientApplication.setCode( daoUtil.getString( nIndex++ ) );

            listClientApplications.add( clientApplication );
        }

        daoUtil.free( );

        return listClientApplications;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCertifier( int nKey, String strCertifier, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_ADD_CERTIFICATE_APP, plugin );

        int nIndex = 1;

        daoUtil.setInt( nIndex++, nKey );
        daoUtil.setString( nIndex++, strCertifier );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteCertifier( int nKey, String strCertifier, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_CERTIFICATE_APP, plugin );

        int nIndex = 1;

        daoUtil.setInt( nIndex++, nKey );
        daoUtil.setString( nIndex++, strCertifier );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanCertifiers( int nKey, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE_ALL_CERTIFICATE_APP, plugin );

        daoUtil.setInt( 1, nKey );
        daoUtil.executeUpdate( );
        daoUtil.free( );
    }
}

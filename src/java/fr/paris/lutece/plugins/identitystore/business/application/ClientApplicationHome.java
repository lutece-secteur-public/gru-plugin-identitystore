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
package fr.paris.lutece.plugins.identitystore.business.application;

import fr.paris.lutece.plugins.identitystore.business.contract.IServiceContractDAO;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for IdentityAttribute objects
 */
public final class ClientApplicationHome
{
    // Static variable pointed at the DAO instance
    private static final IClientApplicationDAO _daoClientApplication = SpringContextService.getBean( IClientApplicationDAO.BEAN_NAME );
    private static final IServiceContractDAO _daoServiceContract = SpringContextService.getBean( IServiceContractDAO.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private ClientApplicationHome( )
    {
    }

    /**
     * Create an instance of the identityAttribute class
     *
     * @param clientApplication
     *            The instance of the clientApplication which contains the informations to store
     * @return The instance of identityAttribute which has been created with its primary key.
     */
    public static ClientApplication create( ClientApplication clientApplication )
    {
        _daoClientApplication.insert( clientApplication, _plugin );

        return clientApplication;
    }

    /**
     * Update of the clientApplication which is specified in parameter
     *
     * @param clientApplication
     *            The instance of the clientApplication which contains the data to store
     * @return The instance of the clientApplication which has been updated
     */
    public static ClientApplication update( ClientApplication clientApplication )
    {
        _daoClientApplication.store( clientApplication, _plugin );

        return clientApplication;
    }

    /**
     * Remove the clientApplication whose identifier is specified in parameter
     *
     * @param clientApplication
     *            The client application
     */
    public static void remove( ClientApplication clientApplication )
    {
        _daoClientApplication.delete( clientApplication.getId( ), _plugin );
    }

    /**
     * Remove the clientApplication whose identifier is specified in parameter
     *
     * @param clientApplication
     *            The client application
     */
    public static void removeContracts( ClientApplication clientApplication )
    {
        List<ServiceContract> serviceContracts = selectServiceContracts( clientApplication.getId( ) );
        for ( ServiceContract serviceContract : serviceContracts )
        {
            ServiceContractHome.remove( serviceContract.getId( ) );
        }
    }

    /**
     * find service contracts of a client app
     *
     * @param clientApplicationId
     * @return the service contract list
     */
    public static List<ServiceContract> selectServiceContracts( final int clientApplicationId )
    {
        return _daoServiceContract.loadFromClientApplication( clientApplicationId, _plugin );
    }

    /**
     * select active service contract for a client code
     * 
     * @param clientCode
     * @return the service contract list
     */
    public static List<ServiceContract> selectActiveServiceContract( String clientCode )
    {
        return _daoServiceContract.selectActiveServiceContract( clientCode, _plugin );
    }

    /**
     * Returns an instance of a clientApplication whose identifier is specified in parameter
     *
     * @param nIdclientApplication
     *            The clientApplication ID
     * @return an instance of clientApplication
     */
    public static ClientApplication findByPrimaryKey( int nIdclientApplication )
    {
        return _daoClientApplication.load( nIdclientApplication, _plugin );
    }

    /**
     * Returns an instance of a clientApplication whose code is specified in parameter
     *
     * @param strClientCode
     *            code of the clientApplication
     * @return an instance of clientApplication
     */
    public static ClientApplication findByCode( String strClientCode )
    {
        return _daoClientApplication.selectByClientCode( strClientCode, _plugin );
    }

    /**
     * Returns list of clientApplication
     *
     * @return list of clientApplication
     */
    public static List<ClientApplication> selectApplicationList( )
    {
        return _daoClientApplication.selectClientApplicationList( _plugin );
    }

    /**
     * Returns list of clientApplication
     *
     * @return list of clientApplication
     */
    public static ClientApplication getParentApplication( ServiceContract serviceContract )
    {
        return _daoClientApplication.selectByContractId( serviceContract.getId( ), _plugin );
    }

    public static List<ClientApplication> findByApplicationCode( String strApplicationCode )
    {
        return _daoClientApplication.selectByApplicationCode( strApplicationCode, _plugin );
    }
}

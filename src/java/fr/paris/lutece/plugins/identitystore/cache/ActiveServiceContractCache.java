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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class ActiveServiceContractCache extends AbstractCacheableService
{

    public static final String SERVICE_NAME = "ActiveServiceContractCache";

    public ActiveServiceContractCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init service contract cache" );
        this.resetCache( );
        final List<ClientApplication> clientApplications = ClientApplicationHome.selectApplicationList( );
        clientApplications.forEach( clientApplication -> {
            try
            {
                final ServiceContract activeServiceContract = this.getActiveServiceContractFromDatabase( clientApplication.getClientCode( ) );
                this.put( clientApplication.getClientCode( ), activeServiceContract );
            }
            catch( final ResourceNotFoundException e )
            {
                AppLogService.debug( e.getMessage( ) );
            }
        } );
    }

    public void put( final String clientCode, final ServiceContract serviceContract )
    {
        if ( this.getKeys( ).contains( clientCode ) )
        {
            this.removeKey( clientCode );
        }
        this.putInCache( clientCode, serviceContract );
        AppLogService.debug( "An active service contract has been added for client application with code : " + clientCode );
    }

    /**
     * Deletes a {@link ServiceContract} by its id in the database
     * 
     * @param id
     */
    public void deleteById( final Integer id )
    {
        this.getKeys( ).forEach( key -> {
            try
            {
                ServiceContract contract = this.get( key );
                if ( Objects.equals( contract.getId( ), id ) )
                {
                    this.removeKey( key );
                }
            }
            catch( final ResourceNotFoundException e )
            {
                AppLogService.error( "Cannot delete service contract with id" + id + " : {}", e );
            }
        } );
    }

    public ServiceContract get( final String clientCode ) throws ResourceNotFoundException
    {
        ServiceContract serviceContract = (ServiceContract) this.getFromCache( clientCode );
        if ( serviceContract == null )
        {
            serviceContract = this.getActiveServiceContractFromDatabase( clientCode );
            this.put( clientCode, serviceContract );
        }
        return serviceContract;
    }

    private ServiceContract getActiveServiceContractFromDatabase( final String clientCode ) throws ResourceNotFoundException
    {
        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectActiveServiceContract( clientCode );
        if ( CollectionUtils.isEmpty( serviceContracts ) )
        {
            throw new ResourceNotFoundException( "No contract service found for client application with code " + clientCode,
                    Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_NOT_FOUND );
        }
        else
            if ( CollectionUtils.size( serviceContracts ) > 1 )
            {
                throw new ResourceNotFoundException(
                        "There is more than one active service contract for the application with code " + clientCode + ". There must be only one",
                        Constants.PROPERTY_REST_ERROR_MULTIPLE_ACTIVE_SERVICE_CONTRACTS );
            }

        final ServiceContract serviceContract = serviceContracts.get( 0 );
        serviceContract.setAttributeRights( ServiceContractHome.selectApplicationRights( serviceContract ) );
        serviceContract.setAttributeCertifications( ServiceContractHome.selectAttributeCertifications( serviceContract ) );
        serviceContract.setAttributeRequirements( ServiceContractHome.selectAttributeRequirements( serviceContract ) );

        return serviceContract;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

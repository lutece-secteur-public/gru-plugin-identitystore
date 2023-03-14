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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Objects;

public class ActiveServiceContractCache extends AbstractCacheableService
{

    private static Logger _logger = Logger.getLogger( ActiveServiceContractCache.class );

    public static final String SERVICE_NAME = "ActiveServiceContractCache";

    public ActiveServiceContractCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        _logger.info( "Init service contract cache" );
        this.resetCache( );
        final List<ClientApplication> clientApplications = ClientApplicationHome.selectApplicationList( );
        clientApplications.forEach( clientApplication -> {
            try
            {
                final ServiceContract activeServiceContract = this.getActiveServiceContractFromDatabase( clientApplication.getCode( ) );
                this.put( clientApplication.getCode( ), activeServiceContract );
            }
            catch( ServiceContractNotFoundException e )
            {
                _logger.warn( "An error occurred during service contract cache refreshing : {} ", e );
            }
        } );
    }

    public void put( final String applicationCode, final ServiceContract serviceContract )
    {
        if ( this.getKeys( ).contains( applicationCode ) )
        {
            this.removeKey( applicationCode );
        }
        this.putInCache( applicationCode, serviceContract );
        _logger.info( "An active service contract has been added for client application with code : " + applicationCode );
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
            catch( ServiceContractNotFoundException e )
            {
                _logger.error( "Cannot delete service contract with id" + id + " : {}", e );
            }
        } );
    }

    public ServiceContract get( final String applicationCode ) throws ServiceContractNotFoundException
    {
        ServiceContract serviceContract = (ServiceContract) this.getFromCache( applicationCode );
        if ( serviceContract == null )
        {
            serviceContract = this.getActiveServiceContractFromDatabase( applicationCode );
            this.put( applicationCode, serviceContract );
        }
        return serviceContract;
    }

    private ServiceContract getActiveServiceContractFromDatabase( final String applicationCode ) throws ServiceContractNotFoundException
    {
        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectActiveServiceContract( applicationCode );
        if ( CollectionUtils.isEmpty( serviceContracts ) )
        {
            throw new ServiceContractNotFoundException( "No contract service found for client application with code " + applicationCode );
        }
        else
            if ( CollectionUtils.size( serviceContracts ) > 1 )
            {
                throw new ServiceContractNotFoundException(
                        "There is more than one active service contract for the application with code " + applicationCode + ". There shall be one only." );
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

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
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClientApplicationCache extends AbstractCacheableService
{

    public static final String SERVICE_NAME = "ClientApplicationCache";

    public ClientApplicationCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init service ClientApplicationCache" );
        this.resetCache( );
    }

    /**
     * put the client code list corresponding to an app code in cache
     * 
     * @param clientAppCode
     * @param clientCodes
     */
    public void put( final String clientAppCode, final List<String> clientCodes )
    {
        if ( this.getKeys( ).contains( clientAppCode ) )
        {
            this.removeKey( clientAppCode );
        }
        this.putInCache( clientAppCode, clientCodes );
        AppLogService.debug( "Client application added to cache, with code : " + clientAppCode );
    }

    /**
     * delete  the client code list cache corresponding to an app code 
     * 
     * @param clientAppCode
     * @param clientCodes
     */
    public void remove( final String clientAppCode )
    {
        if ( this.getKeys( ).contains( clientAppCode ) )
        {
            this.removeKey( clientAppCode );
        }
    }
    
    /**
     * 
     * get client codes from cache or from DB
     * 
     * @param clientAppCode
     * @return the list
     * @throws ClientAuthorizationException
     */
    @SuppressWarnings( "unchecked" )
    public List<String> getClientCodes( final String clientAppCode )
    {
        List<String> clientCodeList = (List<String>) this.getFromCache( clientAppCode );
        if ( clientCodeList == null )
        {
            final List<ClientApplication> clientApplicationList = ClientApplicationHome.findByApplicationCode( clientAppCode );
            clientCodeList = clientApplicationList.stream ( ).map( ClientApplication::getClientCode ).collect( Collectors.toList() );
            this.put( clientAppCode, clientCodeList );
        }
        return clientCodeList;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

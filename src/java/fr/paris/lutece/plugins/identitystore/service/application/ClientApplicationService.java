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
package fr.paris.lutece.plugins.identitystore.service.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.util.sql.TransactionManager;

public class ClientApplicationService
{
    private static ClientApplicationService _instance;

    public static ClientApplicationService instance( )
    {
        if ( _instance == null )
        {
            _instance = new ClientApplicationService( );
        }
        return _instance;
    }

    public ClientApplication create( final ClientApplicationDto clientApplicationDto, String authorName ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            final ClientApplication client = DtoConverter.convertDtoToClient( clientApplicationDto );
            client.setAuthorName( authorName );
            final ClientApplication createdClientApplication = ClientApplicationHome.create( client );
            TransactionManager.commitTransaction( null );
            return createdClientApplication;
        }
        catch( final Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e, Constants.PROPERTY_REST_ERROR_DURING_TREATMENT );
        }
    }

    public ClientApplication update( final ClientApplicationDto clientApplicationDto, String authorName ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            final ClientApplication client = DtoConverter.convertDtoToClient( clientApplicationDto );
            client.setAuthorName( authorName );
            final ClientApplication updatedClientApp = ClientApplicationHome.update( client );
            TransactionManager.commitTransaction( null );
            return updatedClientApp;
        }
        catch( final Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e, Constants.PROPERTY_REST_ERROR_DURING_TREATMENT );
        }
    }
}

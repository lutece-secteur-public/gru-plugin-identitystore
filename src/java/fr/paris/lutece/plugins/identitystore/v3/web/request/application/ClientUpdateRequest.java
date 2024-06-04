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
package fr.paris.lutece.plugins.identitystore.v3.web.request.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.service.application.ClientApplicationService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class ClientUpdateRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final ClientApplicationDto _clientApplicationDto;

    /**
     * Constructor of IdentityStoreCreateRequest
     *
     * @param clientApplicationDto
     *            the dto of client's change
     */
    public ClientUpdateRequest( final ClientApplicationDto clientApplicationDto, final String strClientCode, final String strAppCode, final String authorName,
            final String authorType ) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._clientApplicationDto = clientApplicationDto;
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException
    {
        if (_clientApplicationDto != null && _clientApplicationDto.getClientCode() != null) {
            final ClientApplication existingClientApp = ClientApplicationHome.findByCode(_clientApplicationDto.getClientCode());
            if (existingClientApp == null) {
                throw new ResourceNotFoundException("No client could be found with the code " + _clientApplicationDto.getClientCode(),
                                                    Constants.PROPERTY_REST_ERROR_NO_CLIENT_FOUND);
            }
            _clientApplicationDto.setId(existingClientApp.getId());
        }
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkClientApplicationDto( _clientApplicationDto );
    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        // do nothing
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        // do nothing
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        // do nothing
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        // do nothing
    }

    @Override
    protected ClientChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final ClientChangeResponse response = new ClientChangeResponse( );
        final ClientApplication updatedClientApp = ClientApplicationService.instance( ).update( _clientApplicationDto );

        response.setClientApplication( DtoConverter.convertClientToDto( updatedClientApp ) );
        response.setStatus( ResponseStatusFactory.success( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );

        return response;
    }

}

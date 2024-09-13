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
package fr.paris.lutece.plugins.identitystore.v3.web.request.identity;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityAttributeValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityDuplicateValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class IdentityStoreImportRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final IdentityChangeRequest _identityChangeRequest;

    private ServiceContract serviceContract;
    private String strictDuplicateCustomerId;

    /**
     * Constructor of IdentityStoreImportRequest
     *
     * @param identityChangeRequest
     *            the dto of identity's change
     */
    public IdentityStoreImportRequest( final IdentityChangeRequest identityChangeRequest, final String strClientCode, final String strAppCode,
            final String authorName, final String authorType ) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException
    {
        serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeRequest, false );

    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        ServiceContractService.instance( ).validateImportAuthorization( _identityChangeRequest, serviceContract );
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        // do nothing, it will be done in subsequent request (create or update)
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        // do nothing, it will be done in subsequent request (create or update)
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        strictDuplicateCustomerId = IdentityDuplicateValidator.instance( ).checkDuplicateExistenceForImport( _identityChangeRequest );
    }

    @Override
    protected IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        if ( strictDuplicateCustomerId == null )
        {
            return (IdentityChangeResponse) new IdentityStoreCreateRequest( _identityChangeRequest, _strClientCode, _strAppCode, _author.getName( ),
                    _author.getType( ).name( ) ).doRequest( );
        }
        else
        {
            return (IdentityChangeResponse) new IdentityStoreUpdateRequest( strictDuplicateCustomerId, _identityChangeRequest, _strClientCode, _strAppCode,
                    _author.getName( ), _author.getType( ).name( ) ).doRequest( );
        }
    }
}

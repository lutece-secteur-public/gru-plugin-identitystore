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
package fr.paris.lutece.plugins.identitystore.v3.web.request;

import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class IdentityStoreMergeRequest extends AbstractIdentityStoreRequest
{
    protected static final String ERROR_JSON_MAPPING = "Error while translate object to json";

    private final IdentityMergeRequest _identityMergeRequest;

    /**
     * Constructor of IdentityStoreCreateRequest
     *
     * @param identityMergeRequest
     *            the dto of identity's merge
     */
    public IdentityStoreMergeRequest( IdentityMergeRequest identityMergeRequest, String strClientAppCode )
    {
        super( strClientAppCode );
        this._identityMergeRequest = identityMergeRequest;
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        // Vérification de la consistence des paramètres
        IdentityRequestValidator.instance( ).checkMergeRequest( _identityMergeRequest );
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientAppCode );
    }

    @Override
    public IdentityMergeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final IdentityMergeResponse response = ServiceContractService.instance( ).validateIdentityMerge( _identityMergeRequest, _strClientAppCode );

        if ( !IdentityMergeStatus.FAILURE.equals( response.getStatus( ) ) )
        {
            IdentityService.instance( ).merge( _identityMergeRequest, _strClientAppCode, response );
        }

        return response;
    }

}

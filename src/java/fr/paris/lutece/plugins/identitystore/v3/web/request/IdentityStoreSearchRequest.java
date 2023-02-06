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
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;

import java.util.List;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreSearchRequest extends AbstractIdentityStoreRequest
{
    protected static final String ERROR_JSON_MAPPING = "Error while translate object to json";
    private final IdentitySearchRequest _identitySearchRequest;

    /**
     * Constructor of IdentityStoreSearchRequest
     * 
     * @param identitySearchRequest
     */
    public IdentityStoreSearchRequest( IdentitySearchRequest identitySearchRequest, String strClientAppCode )
    {
        super( strClientAppCode );
        this._identitySearchRequest = identitySearchRequest;
    }

    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        // Vérification de la consistence des paramètres
        IdentityRequestValidator.instance( ).checkIdentitySearch( _identitySearchRequest );
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientAppCode );
    }

    /**
     * get the identities
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    public IdentitySearchResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final IdentitySearchResponse response = ServiceContractService.instance( ).validateIdentitySearch( _identitySearchRequest, _strClientAppCode );

        if ( !IdentitySearchStatusType.FAILURE.equals( response.getStatus( ) ) )
        {
            IdentityService.instance( ).search( _identitySearchRequest, response, _strClientAppCode );
        }

        // TODO calculer qualité, coverage, scoring
        response.setStatus( IdentitySearchStatusType.SUCCESS );
        return response;
    }
}

/*
 * Copyright (c) 2002-2017, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.web.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.rs.IdentityRequestValidator;
import fr.paris.lutece.portal.service.util.AppException;

/**
 *
 */
public class IdentityStoreAppRightsRequest extends IdentityStoreRequest
{

    private String _strClientAppCode;
    private ObjectMapper _objectMapper;

    /**
     * @param strClientAppCode
     * @param objectMapper
     */
    public IdentityStoreAppRightsRequest( String strClientAppCode, ObjectMapper objectMapper )
    {
        super( );
        this._strClientAppCode = strClientAppCode;
        this._objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validRequest( ) throws AppException
    {
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientAppCode );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String doSpecificRequest( ) throws AppException
    {
        try
        {
            return _objectMapper.writeValueAsString( IdentityStoreService.getApplicationRights( _strClientAppCode ) );
        }
        catch( JsonProcessingException e )
        {
            throw new AppException( ERROR_JSON_MAPPING, e );
        }
    }

}

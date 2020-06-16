/*
 * Copyright (c) 2002-2020, Mairie de Paris
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
package fr.paris.lutece.plugins.identitystore.v2.web.request;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.business.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.Identity;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;
import java.util.List;
import java.util.Map;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreSearchRequest extends IdentityStoreRequest
{
    private final Map<String, List<String>> _mapAttributeValues;
    private final List<String> _listAttributeKeyNames;
    private final String _strClientAppCode;
    private final ObjectMapper _objectMapper;

    /**
     * Constructor of IdentityStoreSearchRequest
     * 
     * @param mapAttributeValues
     *            the map that associates list of values for come attributes
     * @param listAttributeKey
     *            the list of attributes to retrieve in identities
     * @param strClientAppCode
     *            the applicationCode
     * @param objectMapper
     *            for json transformation
     */
    public IdentityStoreSearchRequest( Map<String, List<String>> mapAttributeValues, List<String> listAttributeKeyNames, String strClientAppCode, ObjectMapper objectMapper )
    {
        super( );
        this._mapAttributeValues = mapAttributeValues;
        this._listAttributeKeyNames = listAttributeKeyNames;
        this._strClientAppCode = strClientAppCode;
        this._objectMapper = objectMapper;
    }

    /**
     * Valid the get request
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected void validRequest( ) throws AppException
    {
        IdentityRequestValidator.instance( ).checkSearchAttributes( _mapAttributeValues, _strClientAppCode);
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientAppCode );
    }

    /**
     * get the identities
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected String doSpecificRequest( ) throws AppException
    {
        List<IdentityDto> listIdentityDto = IdentityStoreService.getIdentities(_mapAttributeValues, _listAttributeKeyNames, _strClientAppCode );

        try
        {
            return _objectMapper.writeValueAsString( listIdentityDto );
        }
        catch( JsonProcessingException e )
        {
            throw new AppException( ERROR_JSON_MAPPING, e );
        }
    }
}

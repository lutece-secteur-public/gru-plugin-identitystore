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
package fr.paris.lutece.plugins.identitystore.v2.web.request;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreSearchRequest extends IdentityStoreRequest
{
    private ISearchIdentityService _searchIdentityService = SpringContextService.getBean( "identitystore.searchIdentityService" );
    private final Map<String, List<String>> _mapAttributeValues;
    private final List<String> _listAttributeKeyNames;
    private final String _strClientAppCode;
    private final int _nServiceContractId;
    private final ObjectMapper _objectMapper;

    /**
     * Constructor of IdentityStoreSearchRequest
     * 
     * @param mapAttributeValues
     *            the map that associates list of values to search for some attributes
     * @param listAttributeKeyNames
     *            the list of attributes to retrieve in identities
     * @param nServiceContractId
     *            the service contract id
     * @param objectMapper
     *            for json transformation
     */
    public IdentityStoreSearchRequest( Map<String, List<String>> mapAttributeValues, List<String> listAttributeKeyNames, int nServiceContractId,
            String strClientAppCode, ObjectMapper objectMapper )
    {
        super( );
        this._mapAttributeValues = mapAttributeValues;
        this._listAttributeKeyNames = listAttributeKeyNames;
        this._nServiceContractId = nServiceContractId;
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
    protected void validRequest( ) throws IdentityStoreException
    {
        _searchIdentityService.checkSearchAttributes( _mapAttributeValues, _nServiceContractId );
        IdentityRequestValidator.instance( ).checkClientApplication( _strClientAppCode );
    }

    /**
     * get the identities
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected String doSpecificRequest( ) throws IdentityStoreException
    {
        List<IdentityDto> listIdentityDto = _searchIdentityService.getIdentities( _mapAttributeValues, _listAttributeKeyNames, _strClientAppCode );

        try
        {
            return _objectMapper.writeValueAsString( listIdentityDto );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( ERROR_JSON_MAPPING, e );
        }
    }
}

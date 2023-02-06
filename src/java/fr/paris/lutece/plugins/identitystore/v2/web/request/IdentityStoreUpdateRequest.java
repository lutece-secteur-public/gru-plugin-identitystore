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

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.file.File;

/**
 * This class represents an update request for IdentityStoreRestServive
 *
 */
public class IdentityStoreUpdateRequest extends IdentityStoreRequest
{

    private IdentityChangeDto _identityChangeDto;
    private Map<String, File> _mapAttachedFiles;
    private ObjectMapper _objectMapper;

    /**
     * Constructor of IdentityStoreUpdateRequest
     * 
     * @param identityChangeDto
     *            the dto of identity's change
     * @param mapAttachedFiles
     *            list of attached files for attributes
     * @param objectMapper
     *            for json transformation
     */
    public IdentityStoreUpdateRequest( IdentityChangeDto identityChangeDto, Map<String, File> mapAttachedFiles, ObjectMapper objectMapper )
    {
        super( );
        this._identityChangeDto = identityChangeDto;
        this._mapAttachedFiles = mapAttachedFiles;
        this._objectMapper = objectMapper;
    }

    /**
     * Valid the update request
     * 
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    protected void validRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeDto );
        IdentityRequestValidator.instance( ).checkIdentity( _identityChangeDto.getIdentity( ).getConnectionId( ),
                _identityChangeDto.getIdentity( ).getCustomerId( ) );
        IdentityRequestValidator.instance( ).checkClientApplication( _identityChangeDto.getAuthor( ).getApplicationCode( ) );
        // TODO change to pass the real service contract id
        IdentityRequestValidator.instance( ).checkAttributes( _identityChangeDto.getIdentity( ), 0, _mapAttachedFiles );
        IdentityRequestValidator.instance( ).checkCertification( _identityChangeDto.getIdentity( ), 0 );
    }

    /**
     * update the identity
     * 
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    protected String doSpecificRequest( ) throws IdentityStoreException
    {
        IdentityDto identityDto = IdentityStoreService.updateIdentity( _identityChangeDto, _mapAttachedFiles );

        try
        {
            return _objectMapper.writeValueAsString( identityDto );
        }
        catch( JsonProcessingException e )
        {
            throw new IdentityStoreException( ERROR_JSON_MAPPING, e );
        }
    }

}

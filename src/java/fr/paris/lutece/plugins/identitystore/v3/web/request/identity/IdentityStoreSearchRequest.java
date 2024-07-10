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

import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreSearchRequest extends AbstractIdentityStoreRequest
{
    private final IdentitySearchRequest _identitySearchRequest;

    public IdentityStoreSearchRequest( IdentitySearchRequest identitySearchRequest, String strClientAppCode, String authorName, String authorType )
            throws IdentityStoreException
    {
        super( strClientAppCode, authorName, authorType );
        this._identitySearchRequest = identitySearchRequest;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        // Vérification de la consistence des paramètres
        IdentityRequestValidator.instance( ).checkIdentitySearch( _identitySearchRequest );
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
        final boolean guidSearch = StringUtils.isNotEmpty( _identitySearchRequest.getConnectionId( ) );
        final IdentitySearchResponse response = ServiceContractService.instance( ).validateIdentitySearch( _identitySearchRequest, _strClientCode,
                !guidSearch );

        if ( !ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            if ( guidSearch )
            {
                IdentityService.instance( ).search( StringUtils.EMPTY, _identitySearchRequest.getConnectionId( ), response, _strClientCode, _author );
            }
            else
            {
                // data content checks
                final List<AttributeStatus> formatStatuses = IdentityAttributeFormatterService.instance( )
                        .formatIdentitySearchRequestAttributeValues( _identitySearchRequest );
                response.getStatus( ).getAttributeStatuses( ).addAll( formatStatuses );
                IdentityService.instance( ).search( _identitySearchRequest, _author, response, _strClientCode );
            }
        }

        return response;
    }
}

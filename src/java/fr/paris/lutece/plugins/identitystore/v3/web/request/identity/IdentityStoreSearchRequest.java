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
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentitySearchRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreSearchRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final IdentitySearchRequest _identitySearchRequest;
    private final List<AttributeStatus> formatStatuses;
    private ServiceContract serviceContract;

    public IdentityStoreSearchRequest( final IdentitySearchRequest identitySearchRequest, final String strClientCode, final String strAppCode,
            final String authorName, final String authorType ) throws RequestFormatException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._identitySearchRequest = identitySearchRequest;
        this.formatStatuses = new ArrayList<>( );
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException
    {
        serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        // Vérification de la consistence des paramètres
        IdentityRequestValidator.instance( ).checkIdentitySearch( _identitySearchRequest );
        IdentitySearchRequestValidator.instance( ).checkRequiredAttributes( _identitySearchRequest );
    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        final boolean guidSearch = StringUtils.isNotEmpty( _identitySearchRequest.getConnectionId( ) );
        if ( guidSearch )
        {
            ServiceContractService.instance( ).validateGetAuthorization( serviceContract );
        }
        else
        {
            ServiceContractService.instance( ).validateSearchAuthorization( _identitySearchRequest, serviceContract );
        }
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        // do nothing because GET request does not create or update any resource
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        formatStatuses.addAll( IdentityAttributeFormatterService.instance( ).formatIdentitySearchRequestAttributeValues( _identitySearchRequest ) );
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        // do nothing because GET request does not create or update any resource
    }

    /**
     * get the identities
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    protected IdentitySearchResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final boolean guidSearch = StringUtils.isNotEmpty( _identitySearchRequest.getConnectionId( ) );
        final IdentitySearchResponse response = new IdentitySearchResponse( );
        if ( guidSearch )
        {
            final IdentityDto identityDto = IdentityService.instance().search(StringUtils.EMPTY, _identitySearchRequest.getConnectionId(), serviceContract, _author);
            response.getIdentities( ).add(identityDto);

            final IdentitySearchMessage alert =
                    IdentityValidator.instance().verifyIfConsolidatedIdentitySearchResult(identityDto, null, _identitySearchRequest.getConnectionId());
            if (alert != null) {
                response.getAlerts().add(alert);
            }
        }
        else
        {
            response.getIdentities( ).addAll( IdentityService.instance( ).search( _identitySearchRequest, _author, serviceContract ) );
        }
        response.getStatus( ).getAttributeStatuses( ).addAll( formatStatuses );
        response.setStatus( ResponseStatusFactory.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );

        return response;
    }
}

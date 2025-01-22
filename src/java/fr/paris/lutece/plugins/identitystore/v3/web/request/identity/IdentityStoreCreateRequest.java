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
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeValidationService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.datastore.DataEntity;
import fr.paris.lutece.portal.business.datastore.DataEntityHome;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class IdentityStoreCreateRequest extends AbstractIdentityStoreRequest
{

    private final IdentityChangeRequest _identityChangeRequest;


    /**
     * Constructor of IdentityStoreCreateRequest
     * 
     * @param identityChangeRequest
     *            the dto of identity's change
     */
    public IdentityStoreCreateRequest( IdentityChangeRequest identityChangeRequest, String strClientAppCode, String authorName, String authorType )
            throws IdentityStoreException
    {
        super( strClientAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeRequest, false );
    }

    @Override
    public IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        // quality checks
        final IdentityChangeResponse response = ServiceContractService.instance( ).validateIdentityChange( _identityChangeRequest, _strClientCode );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        // data content checks
        final List<AttributeStatus> formatStatuses = IdentityAttributeFormatterService.instance( )
                .formatIdentityChangeRequestAttributeValues( _identityChangeRequest );

        IdentityAttributeValidationService.instance( ).validateIdentityAttributeValues( _identityChangeRequest.getIdentity( ), response );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        // Integrity checks
        IdentityAttributeValidationService.instance( ).validatePivotAttributesIntegrity( null, _strClientCode, _identityChangeRequest.getIdentity( ), true,
                response );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        // perform create
        IdentityService.instance( ).create( _identityChangeRequest, _author, _strClientCode, response );

        // if request is accepted and treatment successful, add the formatting statuses
        if ( ResponseStatusFactory.success( ).equals( response.getStatus( ) ) || ResponseStatusFactory.incompleteSuccess( ).equals( response.getStatus( ) ) )
        {
            response.getStatus( ).getAttributeStatuses( ).addAll( formatStatuses );
        }

        return response;
    }

}

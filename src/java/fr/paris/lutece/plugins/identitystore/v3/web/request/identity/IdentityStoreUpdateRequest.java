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

import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeValidationService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.business.datastore.DataEntity;
import fr.paris.lutece.portal.business.datastore.DataEntityHome;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

/**
 * This class represents an update request for IdentityStoreRestServive
 */
public class IdentityStoreUpdateRequest extends AbstractIdentityStoreRequest
{

    private final IdentityChangeRequest _identityChangeRequest;
    private final String _strCustomerId;
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );
    private static final String PROPERTY_EMAIL_FORBIDDEN_DOMAINS = AppPropertiesService.getProperty("identitystore.identity.attribute.email.forbidden_domains");

    /**
     * Constructor of IdentityStoreUpdateRequest
     *
     * @param identityChangeRequest
     *            the dto of identity's change
     */
    public IdentityStoreUpdateRequest( String _strCustomerId, IdentityChangeRequest identityChangeRequest, String strClientAppCode, String authorName,
            String authorType ) throws IdentityStoreException
    {
        super( strClientAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
        this._strCustomerId = _strCustomerId;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeRequest, true );
        IdentityRequestValidator.instance( ).checkIdentityForUpdate( _identityChangeRequest.getIdentity( ).getConnectionId( ), _strCustomerId );
    }

    /**
     * update the identity
     *
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    public IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        // Quality checks
        final IdentityChangeResponse response = ServiceContractService.instance( ).validateIdentityChange( _identityChangeRequest, _strClientCode );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        // Data content checks
        final List<AttributeStatus> formatStatuses = IdentityAttributeFormatterService.instance( )
                .formatIdentityChangeRequestAttributeValues( _identityChangeRequest );

        IdentityAttributeValidationService.instance( ).validateIdentityAttributeValues( _identityChangeRequest.getIdentity( ), response );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        // Integrity checks
        final IdentityDto existingIdentityDto = _identityDtoCache.getByCustomerId( _strCustomerId,
                ServiceContractService.instance( ).getActiveServiceContract( _strClientCode ) );

        if ( existingIdentityDto == null )
        {
            // updated identity dto should exist
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_NOT_FOUND ).setMessage( "Identity not found" ) );
            return response;
        }

        IdentityAttributeValidationService.instance( ).validatePivotAttributesIntegrity( existingIdentityDto, _strClientCode,
                _identityChangeRequest.getIdentity( ), true, response );
        if ( ResponseStatusFactory.failure( ).equals( response.getStatus( ) ) )
        {
            return response;
        }

        DataEntity dataEntity = DataEntityHome.findByPrimaryKey(PROPERTY_EMAIL_FORBIDDEN_DOMAINS);
        List<String> listForbiddenDomains = new ArrayList<>(Arrays.asList(dataEntity.getValue().split(";")));
        for ( AttributeDto attributeDto : _identityChangeRequest.getIdentity().getAttributes())
        {
            if(StringUtils.equals(attributeDto.getKey(),Constants.PARAM_EMAIL) ||
                    ( StringUtils.equals(attributeDto.getKey(),Constants.PARAM_LOGIN) && attributeDto.getValue().contains("@") ) )
            {
                boolean forbiddenDomain = false;
                for(String domain : listForbiddenDomains)
                {
                    if(attributeDto.getValue().contains(domain))
                    {
                        forbiddenDomain = true;
                        break;
                    }
                }
                if(forbiddenDomain)
                {
                    response.setStatus(
                            ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_FORBIDDEN_EMAIL_DOMAIN ).setMessage( "Forbidden domain" ) );
                    return response;
                }
            }
        }

        // perform update
        IdentityService.instance( ).update( _strCustomerId, _identityChangeRequest, _author, _strClientCode, response );

        // if request is accepted and treatment successful, add the formatting statuses
        if ( ResponseStatusFactory.success( ).equals( response.getStatus( ) ) || ResponseStatusFactory.incompleteSuccess( ).equals( response.getStatus( ) ) )
        {
            response.getStatus( ).getAttributeStatuses( ).addAll( formatStatuses );
        }

        return response;
    }

}

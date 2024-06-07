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
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeGeocodesAdjustmentService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityAttributeValidator;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityDuplicateValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents an update request for IdentityStoreRestServive
 */
public class IdentityStoreUpdateRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );

    private final IdentityChangeRequest _identityChangeRequest;
    private final String _strCustomerId;
    private final List<AttributeStatus> formatStatuses;

    private ServiceContract serviceContract;
    private IdentityDto existingIdentityToUpdate;

    /**
     * Constructor of IdentityStoreUpdateRequest
     *
     * @param identityChangeRequest
     *            the dto of identity's change
     */
    public IdentityStoreUpdateRequest( final String _strCustomerId, final IdentityChangeRequest identityChangeRequest, final String strClientCode,
            final String strAppCode, final String authorName, final String authorType ) throws RequestFormatException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
        this._strCustomerId = _strCustomerId;
        this.formatStatuses = new ArrayList<>( );
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException
    {
        if (_strCustomerId != null) {
            serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
            existingIdentityToUpdate = _identityDtoCache.getByCustomerId( _strCustomerId, serviceContract );
            if ( existingIdentityToUpdate == null )
            {
                throw new ResourceNotFoundException( "No matching identity could be found", Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY );
            }
        }
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeRequest, true );
        IdentityRequestValidator.instance( ).checkIdentityForUpdate( _identityChangeRequest.getIdentity( ).getConnectionId( ), _strCustomerId );
        IdentityAttributeValidator.instance( ).checkAttributeExistence( _identityChangeRequest.getIdentity( ) );
        IdentityAttributeValidator.instance( ).validatePivotAttributesIntegrity( existingIdentityToUpdate, _identityChangeRequest.getIdentity( ), true );
    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        ServiceContractService.instance( ).validateUpdateAuthorization( _identityChangeRequest, existingIdentityToUpdate, serviceContract );
        // If identity is connected and service contract doesn't allow unrestricted update, do a bunch of additionnal checks
        if ( existingIdentityToUpdate.getMonParisActive( ) && !serviceContract.getAuthorizedAccountUpdate( ) )
        {
            IdentityAttributeValidator.instance( ).checkConnectedIdentityUpdate( _identityChangeRequest.getIdentity( ).getAttributes( ),
                    existingIdentityToUpdate );
        }
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        IdentityValidator.instance( ).checkIdentityLastUpdateDate( existingIdentityToUpdate, _identityChangeRequest.getIdentity( ).getLastUpdateDate( ) );
        IdentityValidator.instance( ).checkIdentityMergedStatusForUpdate( existingIdentityToUpdate );
        IdentityValidator.instance( ).checkIdentityDeletedStatusForUpdate( existingIdentityToUpdate );
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        formatStatuses.addAll( IdentityAttributeFormatterService.instance( ).formatIdentityChangeRequestAttributeValues( _identityChangeRequest ) );
        formatStatuses
                .addAll( IdentityAttributeGeocodesAdjustmentService.instance( ).adjustGeocodesAttributes( _identityChangeRequest, existingIdentityToUpdate ) );
        IdentityAttributeValidator.instance( ).validateIdentityAttributeValues( _identityChangeRequest.getIdentity( ) );
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        IdentityDuplicateValidator.instance( ).checkConnectionIdUniquenessForUpdate( _identityChangeRequest, existingIdentityToUpdate );
        IdentityDuplicateValidator.instance( ).checkDuplicateExistenceForUpdate( _identityChangeRequest, existingIdentityToUpdate );
    }

    /**
     * update the identity
     *
     * @throws IdentityStoreException
     *             if there is an exception during the treatment
     */
    @Override
    protected IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final IdentityChangeResponse response = new IdentityChangeResponse( );

        // perform update
        final Pair<Identity, List<AttributeStatus>> result = IdentityService.instance( ).update( _strCustomerId, _identityChangeRequest, _author,
                serviceContract, formatStatuses );

        final Identity updatedIdentity = result.getKey( );
        final List<AttributeStatus> attrStatusList = result.getValue( );
        attrStatusList.addAll( formatStatuses );

        final boolean allAttributesCreatedOrUpdated = attrStatusList.stream( ).map( AttributeStatus::getStatus )
                .allMatch( status -> status.getType( ) == AttributeChangeStatusType.SUCCESS );
        final ResponseStatus status = allAttributesCreatedOrUpdated ? ResponseStatusFactory.success( ) : ResponseStatusFactory.incompleteSuccess( );

        final String msgKey;
        if ( Collections.disjoint( AttributeChangeStatus.getSuccessStatuses( ),
                attrStatusList.stream( ).map( AttributeStatus::getStatus ).collect( Collectors.toList( ) ) ) )
        {
            // If there was no attribute change, send back a specific message key
            msgKey = Constants.PROPERTY_REST_INFO_NO_ATTRIBUTE_CHANGE;
        }
        else
        {
            msgKey = Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION;
        }

        response.setStatus( status.setAttributeStatuses( attrStatusList ).setMessageKey( msgKey ) );
        response.setCustomerId( updatedIdentity.getCustomerId( ) );
        response.setConnectionId( updatedIdentity.getConnectionId( ) );
        response.setCreationDate( updatedIdentity.getCreationDate( ) );
        response.setLastUpdateDate( updatedIdentity.getLastUpdateDate( ) );

        return response;
    }

}

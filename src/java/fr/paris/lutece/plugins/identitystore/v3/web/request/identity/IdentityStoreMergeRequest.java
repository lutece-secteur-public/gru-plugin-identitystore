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
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityAttributeValidator;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a create request for IdentityStoreRestServive
 */
public class IdentityStoreMergeRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );

    private final IdentityMergeRequest _identityMergeRequest;
    private final List<AttributeStatus> formatStatuses;

    private ServiceContract serviceContract;
    private IdentityDto primaryIdentity;
    private IdentityDto secondaryIdentity;

    /**
     * Constructor of IdentityStoreCreateRequest
     *
     * @param identityMergeRequest
     *            the dto of identity's merge
     */
    public IdentityStoreMergeRequest( final IdentityMergeRequest identityMergeRequest, final String strClientCode, final String strAppCode,
            final String authorName, final String authorType ) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        if ( identityMergeRequest == null )
        {
            throw new RequestFormatException( "Provided Identity Merge request is null", Constants.PROPERTY_REST_ERROR_MERGE_REQUEST_NULL );
        }
        this._identityMergeRequest = identityMergeRequest;
        this.formatStatuses = new ArrayList<>( );
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException, ClientAuthorizationException {
        serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
        primaryIdentity = _identityDtoCache.getByCustomerId( _identityMergeRequest.getPrimaryCuid( ), serviceContract );
        if ( primaryIdentity == null )
        {
            throw new ResourceNotFoundException( "Could not find primary identity", Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_NOT_FOUND );
        }
        secondaryIdentity = _identityDtoCache.getByCustomerId( _identityMergeRequest.getSecondaryCuid( ), serviceContract );
        if ( secondaryIdentity == null )
        {
            throw new ResourceNotFoundException( "Could not find secondary identity", Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_NOT_FOUND );
        }
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkMergeRequest( _identityMergeRequest );
        if ( _identityMergeRequest.getIdentity( ) != null && CollectionUtils.isNotEmpty( _identityMergeRequest.getIdentity( ).getAttributes( ) ) )
        {
            IdentityAttributeValidator.instance( ).checkAttributeExistence( _identityMergeRequest.getIdentity( ) );
            IdentityAttributeValidator.instance( ).validatePivotAttributesIntegrity( primaryIdentity, _identityMergeRequest.getIdentity( ), true );
        }
    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        ServiceContractService.instance( ).validateMergeAuthorization( _identityMergeRequest, serviceContract );
        if ( _identityMergeRequest.getIdentity( ) != null && CollectionUtils.isNotEmpty( _identityMergeRequest.getIdentity( ).getAttributes( ) ) )
        {
            // If identity is connected and service contract doesn't allow unrestricted update, do a bunch of additionnal checks
            if ( primaryIdentity.getMonParisActive( ) && !serviceContract.getAuthorizedAccountUpdate( ) )
            {
                IdentityAttributeValidator.instance( ).checkConnectedIdentityUpdate( _identityMergeRequest.getIdentity( ).getAttributes( ), primaryIdentity );
            }
        }

    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        IdentityValidator.instance( ).checkIdentityLastUpdateDate( primaryIdentity, _identityMergeRequest.getPrimaryLastUpdateDate( ) );
        IdentityValidator.instance( ).checkIdentityLastUpdateDate( secondaryIdentity, _identityMergeRequest.getSecondaryLastUpdateDate( ) );

        IdentityValidator.instance( ).checkIdentityMergedStatusForUpdate( primaryIdentity );
        IdentityValidator.instance( ).checkIdentityMergedStatusForUpdate( secondaryIdentity );

        IdentityValidator.instance( ).checkIdentityDeletedStatusForUpdate( primaryIdentity );
        IdentityValidator.instance( ).checkIdentityDeletedStatusForUpdate( secondaryIdentity );
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        if ( _identityMergeRequest.getIdentity( ) != null && CollectionUtils.isNotEmpty( _identityMergeRequest.getIdentity( ).getAttributes( ) ) )
        {
            formatStatuses.addAll( IdentityAttributeFormatterService.instance( ).formatIdentityMergeRequestAttributeValues( _identityMergeRequest ) );
            IdentityAttributeValidator.instance( ).validateIdentityAttributeValues( _identityMergeRequest.getIdentity( ) );
        }

    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        // do nothing
    }

    @Override
    protected IdentityMergeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        final IdentityMergeResponse response = new IdentityMergeResponse( );

        final Pair<Identity, List<AttributeStatus>> result = IdentityService.instance().merge(DtoConverter.convertDtoToIdentity(primaryIdentity),
                                                                                              DtoConverter.convertDtoToIdentity(secondaryIdentity),
                                                                                              _identityMergeRequest.getIdentity(),
                                                                                              _identityMergeRequest.getDuplicateRuleCode(), _author,
                                                                                              _strClientCode, _identityMergeRequest.isResetIsmonparisactiveForSecondaryIdentity( ), 
                                                                                              formatStatuses);
        final Identity updatedPrimaryIdentity = result.getKey( );
        final List<AttributeStatus> attrStatuses = result.getValue( );
        attrStatuses.addAll( formatStatuses );

        final boolean allAttributesCreatedOrUpdated = attrStatuses.stream( ).map( AttributeStatus::getStatus )
                .allMatch( status -> status.getType( ) == AttributeChangeStatusType.SUCCESS );
        final ResponseStatus status = allAttributesCreatedOrUpdated ? ResponseStatusFactory.success( ) : ResponseStatusFactory.incompleteSuccess( );

        final String msgKey;
        if ( Collections.disjoint( AttributeChangeStatus.getSuccessStatuses( ),
                attrStatuses.stream( ).map( AttributeStatus::getStatus ).collect( Collectors.toList( ) ) ) )
        {
            // If there was no attribute change, send back a specific message key
            msgKey = Constants.PROPERTY_REST_INFO_NO_ATTRIBUTE_CHANGE;
        }
        else
        {
            msgKey = Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION;
        }

        response.setStatus( status.setAttributeStatuses( attrStatuses ).setMessageKey( msgKey ) );
        response.setCustomerId( updatedPrimaryIdentity.getCustomerId( ) );
        response.setConnectionId( updatedPrimaryIdentity.getConnectionId( ) );
        response.setLastUpdateDate( updatedPrimaryIdentity.getLastUpdateDate( ) );

        return response;
    }

}

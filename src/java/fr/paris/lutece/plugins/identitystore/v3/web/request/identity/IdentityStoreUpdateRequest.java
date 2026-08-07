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

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeGeocodesAdjustmentService;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityAttributeValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityDuplicateValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
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
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents an update request for IdentityStoreRestServive
 */
public class IdentityStoreUpdateRequest extends AbstractIdentityStoreAppCodeRequest
{
    private static final String PROPERTY_CHECK_LOGIN_UNIQUENESS = "identitystore.identity.check_login_uniqueness";
    private static final String KEEP_CERTIFICATION_DATE_WHEN_NO_CHANGE = "identitystore.identity.update.keep.certification.date.when.no.changes";

    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );
    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance();

    private final boolean controlsOnly;
    private final IdentityChangeRequest _identityChangeRequest;
    private final String _strCustomerId;
    private final List<AttributeStatus> formatStatuses;
    private final List<AttributeStatus> noUpdateStatuses;

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
        this(_strCustomerId, identityChangeRequest, strClientCode, strAppCode, authorName, authorType, false );
    }

    public IdentityStoreUpdateRequest( final String _strCustomerId, final IdentityChangeRequest identityChangeRequest, final String strClientCode,
                                       final String strAppCode, final String authorName, final String authorType, final boolean controlsOnly ) throws RequestFormatException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
        this._strCustomerId = _strCustomerId;
        this.controlsOnly = controlsOnly;
        this.formatStatuses = new ArrayList<>( );
        this.noUpdateStatuses = new ArrayList<>( );
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException, ClientAuthorizationException {
        if (_strCustomerId != null) {
            serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
            existingIdentityToUpdate = _identityDtoCache.getIdentityByCustomerId( _strCustomerId, serviceContract );
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
        // If the identity is connected and the service contract doesn't allow unrestricted update, do a bunch of additional checks
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
        IdentityValidator.instance( ).checkIdentityMergedStatusForUpdate( existingIdentityToUpdate, serviceContract );
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
        if ( AppPropertiesService.getPropertyBoolean( PROPERTY_CHECK_LOGIN_UNIQUENESS, false ) )
        {
            IdentityDuplicateValidator.instance( ).checkLoginUniquenessForCreate( _identityChangeRequest );
        }
    }

    /**
     * Method that compares the request identity and the existing identity to update, to see if there are any changes to apply.
     * If there is nothing to update, this method will also compute the AttributeStatus for the attributes that won't be created/updated.
     * This should only be called in the <code>doSpecificRequest</code> method.
     * @return <code>true</code> if there is no changes to apply, <code>false</code> otherwise.
     */
    private boolean nothingToUpdate( )
    {
        final IdentityDto requestIdentity = _identityChangeRequest.getIdentity( );

        // Connection ID is different, and the request one is not empty : update
        if ( !Strings.CI.equals( existingIdentityToUpdate.getConnectionId( ), requestIdentity.getConnectionId( ) )
             && StringUtils.isNotEmpty( requestIdentity.getConnectionId( ) ) )
        {
            return false;
        }

        // MonParis flag is different, and the request one is not empty : update
        if ( requestIdentity.getMonParisActive( ) != null && requestIdentity.getMonParisActive( ) != existingIdentityToUpdate.isMonParisActive( ) )
        {
            return false;
        }

        // Attributes
        /* Separate attributes to create and to update */
        final Map<Boolean, List<AttributeDto>> sortedAttributes = requestIdentity.getAttributes().stream().collect(Collectors.partitioningBy(
                attr -> existingIdentityToUpdate.getAttributes().stream().anyMatch(existingAttr -> existingAttr.getKey().equals(attr.getKey()))));
        final List<AttributeDto> existingWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( true ) ) ? sortedAttributes.get( true ) : List.of( );
        final List<AttributeDto> newWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( false ) ) ? sortedAttributes.get( false ) : List.of( );

        // Attributes to CREATE
        for ( final AttributeDto attributeToCreate : newWritableAttributes )
        {
            // If there is at least one attribute to create with non-empty value -> update
            if ( StringUtils.isNotBlank( attributeToCreate.getValue( ) ) )
            {
                return false;
            }
            else
            {
                noUpdateStatuses.add( buildAttributeStatus( attributeToCreate.getKey( ), AttributeChangeStatus.NOT_CREATED,
                                                            Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_CREATED ) );
            }
        }

        // Attributes to UPDATE
        for ( final AttributeDto attributeToUpdate : existingWritableAttributes )
        {
            final AttributeDto existingAttribute =
                    existingIdentityToUpdate.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attributeToUpdate.getKey( ) ) ).findFirst( ).orElse( null );
            // Existing attribute shouldn't be null, but we check it anyway
            if ( existingAttribute != null )
            {
                int attributeToUpdateLevelInt =
                        _attributeCertificationDefinitionService.getLevelAsInteger( attributeToUpdate.getCertifier( ), attributeToUpdate.getKey( ) );
                int existingAttributeLevelInt =
                        _attributeCertificationDefinitionService.getLevelAsInteger( existingAttribute.getCertifier( ), existingAttribute.getKey( ) );

                // if the attribute already exists with the same value and the same certification level
                if ( attributeToUpdateLevelInt == existingAttributeLevelInt && Objects.equals( attributeToUpdate.getValue( ), existingAttribute.getValue( ) ) )
                {
                    final boolean keepCertificationDateWhenNoChange = AppPropertiesService.getPropertyBoolean( KEEP_CERTIFICATION_DATE_WHEN_NO_CHANGE, true );
                    // If the property to keep the certification date when no change is set to true, or if the new certification date is equal or before the
                    // existing one : no update
                    if ( keepCertificationDateWhenNoChange ||
                         attributeToUpdate.getCertificationDate( ).equals( existingAttribute.getCertificationDate( ) ) ||
                         attributeToUpdate.getCertificationDate( ).before( existingAttribute.getCertificationDate( ) ) )
                    {
                        noUpdateStatuses.add( buildAttributeStatus( attributeToUpdate.getKey( ), AttributeChangeStatus.NOT_UPDATED,
                                                                    Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_UPDATED ) );
                    }
                    // Otherwise : update
                    else
                    {
                        return false;
                    }
                }
                // request certification level is greater or equal to the existing one
                else if ( attributeToUpdateLevelInt >= existingAttributeLevelInt )
                {
                    // request value is empty = attribute delete requested
                    if ( StringUtils.isBlank( attributeToUpdate.getValue( ) ) )
                    {
                        final Optional<AttributeRight> right = serviceContract.getAttributeRights().stream()
                                .filter( ar -> ar.getAttributeKey( ).getKeyName( ).equals( attributeToUpdate.getKey( ) ) ).findAny( );
                        // attribute is not mandatory : deletion
                        if ( right.isEmpty( ) || !right.get( ).isMandatory( ) )
                        {
                            return false;
                        }
                        // attribute is mandatory : no deletion
                        else
                        {
                            noUpdateStatuses.add( buildAttributeStatus( attributeToUpdate.getKey( ), AttributeChangeStatus.NOT_REMOVED,
                                                                        Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_REMOVED ) );
                        }
                    }
                    // request value not empty : update
                    else
                    {
                        return false;
                    }
                }
                // request certification level is lesser than the existing one : no update
                else
                {
                    noUpdateStatuses.add( buildAttributeStatus( attributeToUpdate.getKey( ), AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL,
                                                                Constants.PROPERTY_ATTRIBUTE_STATUS_INSUFFICIENT_CERTIFICATION_LEVEL ) );
                }
            }
        }

        // If this is reached, it means there is nothing to update
        return true;
    }

    /**
     * Create a returns a new instance of {@link AttributeStatus}.
     * @param attrKey - the attribute key
     * @param status - the status
     * @param messageKey - the message key
     * @return {@link AttributeStatus}
     */
    private AttributeStatus buildAttributeStatus( final String attrKey, final AttributeChangeStatus status, final String messageKey )
    {
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attrKey );
        attributeStatus.setStatus( status );
        attributeStatus.setMessageKey( messageKey );
        return attributeStatus;
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
        if (controlsOnly) {
            // if we are here, it means that all the controls were successful.
            response.setStatus( ResponseStatusFactory.success( ).setAttributeStatuses( formatStatuses ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            return response;
        }
        if ( nothingToUpdate( ) )
        {
            // If the request doesn't contain any changes to apply to the existing identity, return a specific success response
            final List<AttributeStatus> attrStatuses = Stream.concat( formatStatuses.stream( ), noUpdateStatuses.stream( ) ).collect( Collectors.toList( ) );
            response.setStatus( ResponseStatusFactory.incompleteSuccess().setAttributeStatuses( attrStatuses ).setMessageKey( Constants.PROPERTY_REST_INFO_NOTHING_TO_UPDATE ) );
            return response;
        }

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

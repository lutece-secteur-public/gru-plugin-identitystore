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
package fr.paris.lutece.plugins.identitystore.service.identity;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.ExcludedIdentities;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentity;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.geocodes.GeocodesService;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.service.search.ISearchIdentityService;
import fr.paris.lutece.plugins.identitystore.service.user.InternalUserService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.QualityDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.security.AccessLogService;
import fr.paris.lutece.portal.service.security.AccessLoggerConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.http.SecurityUtil;
import fr.paris.lutece.util.sql.TransactionManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityService
{
    // Conf
    private static final String PIVOT_CERTIF_LEVEL_THRESHOLD = "identitystore.identity.attribute.update.pivot.certif.level.threshold";

    // EVENTS FOR ACCESS LOGGING
    public static final String CREATE_IDENTITY_EVENT_CODE = "CREATE_IDENTITY";
    public static final String UPDATE_IDENTITY_EVENT_CODE = "UPDATE_IDENTITY";
    public static final String DECERTIFY_IDENTITY_EVENT_CODE = "DECERTIFY_IDENTITY";
    public static final String GET_IDENTITY_EVENT_CODE = "GET_IDENTITY";
    public static final String SEARCH_IDENTITY_EVENT_CODE = "SEARCH_IDENTITY";
    public static final String DELETE_IDENTITY_EVENT_CODE = "DELETE_IDENTITY";
    public static final String CONSOLIDATE_IDENTITY_EVENT_CODE = "CONSOLIDATE_IDENTITY";
    public static final String MERGE_IDENTITY_EVENT_CODE = "MERGE_IDENTITY";
    public static final String CANCEL_MERGE_IDENTITY_EVENT_CODE = "CANCEL_MERGE_IDENTITY";
    public static final String CANCEL_CONSOLIDATE_IDENTITY_EVENT_CODE = "CANCEL_CONSOLIDATE_IDENTITY";
    public static final String SPECIFIC_ORIGIN = "BO";

    // PROPERTIES
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION = "identitystore.identity.duplicates.import.rules.suspicion";
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_STRICT = "identitystore.identity.duplicates.import.rules.strict";
    private static final String PROPERTY_DUPLICATES_CREATION_RULES = "identitystore.identity.duplicates.creation.rules";
    private static final String PROPERTY_DUPLICATES_UPDATE_RULES = "identitystore.identity.duplicates.update.rules";
    private static final String PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED = "identitystore.identity.duplicates.check.database";

    // SERVICES
    private final IdentityStoreNotifyListenerService _identityStoreNotifyListenerService = IdentityStoreNotifyListenerService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );
    private final IdentityAttributeService _identityAttributeService = IdentityAttributeService.instance( );
    private final InternalUserService _internalUserService = InternalUserService.getInstance( );
    private final IDuplicateService _duplicateServiceDatabase = SpringContextService.getBean( "identitystore.duplicateService.database" );
    private final IDuplicateService _duplicateServiceElasticSearch = SpringContextService.getBean( "identitystore.duplicateService.elasticsearch" );
    private final ISearchIdentityService _elasticSearchIdentityService = SpringContextService.getBean( "identitystore.searchIdentityService.elasticsearch" );

    // CACHE
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );

    private static IdentityService _instance;

    public static IdentityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityService( );
        }
        return _instance;
    }

    /**
     * Creates a new {@link Identity} according to the given {@link IdentityChangeRequest}
     *
     * @param request
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param author
     *            the author of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the created {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity create( final IdentityChangeRequest request, final RequestAuthor author, final String clientCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        if ( !_serviceContractService.canCreateIdentity( clientCode ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to create an identity." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_CREATE_UNAUTHORIZED ) );
            return null;
        }

        if ( StringUtils.isNotEmpty( request.getIdentity( ).getCustomerId( ) ) )
        {
            throw new IdentityStoreException( "You cannot specify a CUID when requesting for a creation" );
        }

        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) ) && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            throw new IdentityStoreException( "You cannot specify a GUID when requesting for a creation" );
        }

        // check if all mandatory attributes are present
        final List<String> mandatoryAttributes = _serviceContractService.getMandatoryAttributes( clientCode,
                AttributeKeyHome.getMandatoryForCreationAttributeKeyList( ) );
        if ( CollectionUtils.isNotEmpty( mandatoryAttributes ) )
        {
            final Set<String> providedKeySet = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                    .map( AttributeDto::getKey ).collect( Collectors.toSet( ) );
            if ( !providedKeySet.containsAll( mandatoryAttributes ) )
            {
                response.setStatus( ResponseStatusFactory.failure( ).setMessage( "All mandatory attributes must be provided : " + mandatoryAttributes )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_MISSING_MANDATORY_ATTRIBUTES ) );
                return null;
            }
        }

        // check if can set "mon_paris_active" flag to true
        if ( request.getIdentity( ).getMonParisActive( ) != null && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            throw new IdentityStoreException( "You cannot set the 'mon_paris_active' flag when requesting for a creation" );
        }

        // check if GUID is already in use
        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) )
                && IdentityHome.findByConnectionId( request.getIdentity( ).getConnectionId( ) ) != null )
        {
            throw new IdentityStoreException( "GUID is already in use." );
        }

        final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue, ( a, b ) -> a ) );
        final DuplicateSearchResponse duplicateSearchResponse = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_CREATION_RULES, "" );
        if ( duplicateSearchResponse != null && CollectionUtils.isNotEmpty( duplicateSearchResponse.getIdentities( ) ) )
        {
            response.setStatus( ResponseStatusFactory.conflict( ).setMessage( duplicateSearchResponse.getStatus( ).getMessage( ) )
                    .setMessageKey( duplicateSearchResponse.getStatus( ).getMessageKey( ) ) );
            return null;
        }

        final Identity identity = new Identity( );
        TransactionManager.beginTransaction( null );
        try
        {
            identity.setMonParisActive( request.getIdentity( ).isMonParisActive( ) );
            if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) ) )
            {
                identity.setConnectionId( request.getIdentity( ).getConnectionId( ) );
            }
            IdentityHome.create( identity, _serviceContractService.getDataRetentionPeriodInMonths( clientCode ) );

            final List<AttributeDto> attributesToCreate = request.getIdentity( ).getAttributes( );
            final List<AttributeStatus> attrStatusList = GeocodesService.processCountryAndCityForCreate( identity, attributesToCreate, clientCode );
            for ( final AttributeDto attributeDto : attributesToCreate )
            {
                // TODO vérifier que la clef d'attribut existe dans le référentiel
                final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( attributeDto, identity, clientCode );
                attrStatusList.add( attributeStatus );
            }

            response.setCustomerId( identity.getCustomerId( ) );
            response.setCreationDate( identity.getCreationDate( ) );
            final boolean incompleteCreation = attrStatusList.stream( ).anyMatch( s -> s.getStatus( ).equals( AttributeChangeStatus.NOT_CREATED ) );
            final ResponseStatus status = incompleteCreation ? ResponseStatusFactory.incompleteSuccess( ) : ResponseStatusFactory.success( );
            response.setStatus( status.setAttributeStatuses( attrStatusList ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            TransactionManager.commitTransaction( null );

            /* Historique des modifications */
            for ( AttributeStatus attributeStatus : attrStatusList )
            {
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( AttributeChangeType.CREATE, identity, attributeStatus, author, clientCode );
            }

            /* Indexation et historique */
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.CREATE, identity, response.getStatus( ).getType( ).name( ),
                    response.getStatus( ).getMessage( ), author, clientCode, new HashMap<>( ) );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_CREATE, CREATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( identity.getCustomerId( ) ), SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( e.getMessage( ) ).setMessageKey( Constants.PROPERTY_REST_ERROR_DURING_TREATMENT ) );
            TransactionManager.rollBack( null );
        }

        return identity;
    }

    /**
     * Updates an existing {@link Identity} according to the given {@link IdentityChangeRequest} and following the given rules: <br>
     * <ul>
     * <li>The {@link Identity} must exist in te database. If not, NOT_FOUND status is returned in the execution response</li>
     * <li>The {@link Identity} must not be merged or deleted. In case of merged/deleted identity, the update is not performed and the customer ID of the
     * primary identity is returned in the execution response with a CONFLICT status</li>
     * <li>If the {@link Identity} can be updated, its {@link IdentityAttribute} list is updated following the given rule:
     * <ul>
     * <li>If the {@link IdentityAttribute} exists, it is updated if the value is different, and if the process level given in the request is higher than the
     * existing one. If the value cannot be updated, the NOT_UPDATED status, associated with the attribute key, is returned in the execution response.</li>
     * <li>If the {@link IdentityAttribute} does not exist, it is created. The CREATED status, associated with the attribute key, is returned in the execution
     * response.</li>
     * <li>CUID and GUID attributes cannot be modified.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param customerId
     *            the id of the updated {@link Identity}
     * @param request
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param author
     *            the author of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the updated {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity update( final String customerId, final IdentityChangeRequest request, final RequestAuthor author, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {
        if ( !_serviceContractService.canUpdateIdentity( clientCode ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to update an identity." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_UPDATE_UNAUTHORIZED ) );
            response.setCustomerId( customerId );
            return null;
        }

        final Identity identity = IdentityHome.findByCustomerId( customerId );

        // check if identity exists
        if ( identity == null )
        {
            response.setStatus( ResponseStatusFactory.notFound( ).setMessage( "No matching identity could be found" )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY ) );
            return null;
        }

        // check if identity hasn't been updated between when the user retrieved the identity, and this request
        if ( !Objects.equals( identity.getLastUpdateDate( ), request.getIdentity( ).getLastUpdateDate( ) ) )
        {
            response.setStatus(
                    ResponseStatusFactory.conflict( ).setMessage( "This identity has been updated recently, please load the latest data before updating." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_UPDATE_CONFLICT ) );
            response.setCustomerId( identity.getCustomerId( ) );
            return identity;
        }

        // check if identity is not merged
        if ( identity.isMerged( ) )
        {
            final Identity masterIdentity = IdentityHome.findMasterIdentityByCustomerId( request.getIdentity( ).getCustomerId( ) );
            response.setStatus(
                    ResponseStatusFactory.conflict( ).setMessage( "Cannot update a merged Identity. Master identity customerId is provided in the response." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_FORBIDDEN_UPDATE_ON_MERGED_IDENTITY ) );
            response.setCustomerId( masterIdentity.getCustomerId( ) );
            return identity;
        }

        // check if identity is active
        if ( identity.isDeleted( ) )
        {
            response.setStatus( ResponseStatusFactory.conflict( ).setMessage( "Cannot update a deleted Identity." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_FORBIDDEN_UPDATE_ON_DELETED_IDENTITY ) );
            response.setCustomerId( identity.getCustomerId( ) );
            return identity;
        }

        // check if the service contract allows the update of "mon_paris_active" flag
        if ( request.getIdentity( ).getMonParisActive( ) != null && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            response.setStatus(
                    ResponseStatusFactory.conflict( ).setMessage( "The client application is not authorized to update the 'mon_paris_active' flag." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_FORBIDDEN_MON_PARIS_ACTIVE_UPDATE ) );
            response.setCustomerId( identity.getCustomerId( ) );
            return null;
        }

        // check if update would create duplicates
        if ( doesRequestContainsAttributeValueChangesImpactingRules( request, identity, PROPERTY_DUPLICATES_UPDATE_RULES ) )
        {
            // collect all non blank attributes from request
            final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                    .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue, ( a, b ) -> a ) );
            // add other existing identity attributes
            identity.getAttributes( ).forEach( ( key, attr ) -> attributes.putIfAbsent( key, attr.getValue( ) ) );
            // remove attributes that have blank values in the request
            request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isBlank( a.getValue( ) ) )
                    .forEach( a -> attributes.remove( a.getKey( ) ) );

            // search for potential duplicates with those attributes
            final DuplicateSearchResponse duplicateSearchResponse = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_UPDATE_RULES, customerId );
            if ( duplicateSearchResponse != null && !duplicateSearchResponse.getIdentities( ).isEmpty( ) )
            {
                response.setStatus( ResponseStatusFactory.conflict( ).setMessage( duplicateSearchResponse.getStatus( ).getMessage( ) )
                        .setMessageKey( duplicateSearchResponse.getStatus( ).getMessageKey( ) ) );
                return null;
            }
        }

        // If GUID is updated, check if the new GUID does not exist in database
        TransactionManager.beginTransaction( null );
        try
        {
            if ( _serviceContractService.canModifyConnectedIdentity( clientCode )
                    && !StringUtils.equalsIgnoreCase( identity.getConnectionId( ), request.getIdentity( ).getConnectionId( ) )
                    && request.getIdentity( ).getConnectionId( ) != null )
            {
                final Identity byConnectionId = IdentityHome.findByConnectionId( request.getIdentity( ).getConnectionId( ) );
                if ( byConnectionId != null )
                {
                    response.setStatus( ResponseStatusFactory.conflict( )
                            .setMessage(
                                    "An identity already exists with the given connection ID. The customer ID of that identity is provided in the response." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_CONFLICT_CONNECTION_ID ) );
                    response.setCustomerId( byConnectionId.getCustomerId( ) );
                    TransactionManager.rollBack( null );
                    return null;
                }
                else
                {
                    identity.setConnectionId( request.getIdentity( ).getConnectionId( ) );
                    IdentityHome.update( identity );
                }
            }

            // => process update :

            final List<AttributeStatus> attrStatusList = this.updateIdentity( request.getIdentity( ), clientCode, response, identity );
            if ( ResponseStatusFactory.unauthorized( ).equals( response.getStatus( ) ) )
            {
                response.setCustomerId( identity.getCustomerId( ) );
                TransactionManager.rollBack( null );
                return null;
            }

            response.setCustomerId( identity.getCustomerId( ) );
            response.setConnectionId( identity.getConnectionId( ) );
            response.setCreationDate( identity.getCreationDate( ) );
            response.setLastUpdateDate( identity.getLastUpdateDate( ) );

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
            TransactionManager.commitTransaction( null );

            /* Historique des modifications */
            for ( final AttributeStatus attributeStatus : attrStatusList )
            {
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( AttributeChangeType.UPDATE, identity, attributeStatus, author, clientCode );
            }

            /* Indexation et historique */
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.UPDATE, identity, response.getStatus( ).getType( ).name( ),
                    response.getStatus( ).getMessage( ), author, clientCode, new HashMap<>( ) );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, UPDATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( identity.getCustomerId( ) ), SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( e.getMessage( ) ).setMessageKey( Constants.PROPERTY_REST_ERROR_DURING_TREATMENT ) );
            TransactionManager.rollBack( null );
        }

        return identity;
    }

    /**
     * Returns <code>true</code> if the request aims to add new attributes, remove existing attributes, or modify existing attribute's value, of attributes
     * checked by the duplicate rules in parameter.<br/>
     * Returns <code>false</code> otherwise.
     * 
     * @param request
     *            the request
     * @param identity
     *            the identity
     */
    private boolean doesRequestContainsAttributeValueChangesImpactingRules( final IdentityChangeRequest request, final Identity identity,
            final String duplicateRulesProperty )
    {
        final Set<String> checkedAttributeKeys = Arrays.stream( AppPropertiesService.getProperty( duplicateRulesProperty ).split( "," ) )
                .map( DuplicateRuleHome::findByCode ).flatMap( rule -> rule.getCheckedAttributes( ).stream( ) ).map( AttributeKey::getKeyName )
                .collect( Collectors.toSet( ) );
        return request.getIdentity( ).getAttributes( ).stream( ).filter( a -> checkedAttributeKeys.contains( a.getKey( ) ) ).anyMatch( a -> {
            if ( StringUtils.isNotBlank( a.getValue( ) ) )
            {
                return !identity.getAttributes( ).containsKey( a.getKey( ) )
                        || !Objects.equals( identity.getAttributes( ).get( a.getKey( ) ).getValue( ), a.getValue( ) );
            }
            else
            {
                return identity.getAttributes( ).containsKey( a.getKey( ) );
            }
        } );
    }

    /**
     * Merges two existing {@link Identity} specified in the given {@link IdentityMergeRequest} and following the given rules: <br>
     * <ul>
     * <li>Both {@link Identity} must exist and not be merged or deleted in te database. If not, FAILURE status is returned in the execution response</li>
     * <li>The {@link IdentityAttribute}(s) of the secondary {@link Identity} are processed, according to the list of keys specified in the
     * {@link IdentityMergeRequest}.
     * <ul>
     * <li>If the {@link IdentityAttribute} is not present in the primary {@link Identity}, it is created.</li>
     * <li>If the {@link IdentityAttribute} is present in the primary {@link Identity}, it is updated if the certification process is higher and the value is
     * different.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param request
     *            the {@link IdentityMergeRequest} holding the parameters of the request
     * @param author
     *            the author of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityMergeResponse} holding the status of the execution of the request
     * @return the merged {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    // TODO: récupérer la plus haute date d'expiration des deux identités
    public Identity merge( final IdentityMergeRequest request, final RequestAuthor author, final String clientCode, final IdentityMergeResponse response )
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( request.getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "Could not find primary identity with customer_id " + request.getPrimaryCuid( ) )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_NOT_FOUND ) );
            return null;
        }

        if ( primaryIdentity.isDeleted( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Primary identity found with customer_id " + request.getPrimaryCuid( ) + " is deleted" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_DELETED ) );
            return null;
        }

        if ( primaryIdentity.isMerged( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Primary identity found with customer_id " + request.getPrimaryCuid( ) + " is merged" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_MERGED ) );
            return null;
        }

        if ( !Objects.equals( primaryIdentity.getLastUpdateDate( ), request.getPrimaryLastUpdateDate( ) ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "The primary identity has been updated recently, please load the latest data before merging." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_UPDATE_CONFLICT ) );
            return null;
        }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( request.getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Could not find secondary identity with customer_id " + request.getSecondaryCuid( ) )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_NOT_FOUND ) );
            return null;
        }

        if ( secondaryIdentity.isDeleted( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is deleted" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_DELETED ) );
            return null;
        }

        if ( secondaryIdentity.isMerged( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is merged" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_MERGED ) );
            return null;
        }

        if ( !Objects.equals( secondaryIdentity.getLastUpdateDate( ), request.getSecondaryLastUpdateDate( ) ) )
        {
            response.setStatus( ResponseStatusFactory.failure( )
                    .setMessage( "The secondary identity has been updated recently, please load the latest data before merging." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_UPDATE_CONFLICT ) );
            return null;
        }

        TransactionManager.beginTransaction( null );
        try
        {
            final List<AttributeStatus> attrStatusList = new ArrayList<>( );
            if ( request.getIdentity( ) != null )
            {
                attrStatusList.addAll( this.updateIdentity( request.getIdentity( ), clientCode, response, primaryIdentity ) );
                if ( ResponseStatusFactory.unauthorized( ).equals( response.getStatus( ) ) )
                {
                    response.setCustomerId( primaryIdentity.getCustomerId( ) );
                    TransactionManager.rollBack( null );
                    return null;
                }
            }

            /* Tag de l'identité secondaire */
            secondaryIdentity.setMerged( true );
            secondaryIdentity.setMasterIdentityId( primaryIdentity.getId( ) );
            IdentityHome.merge( secondaryIdentity );
            IdentityAttributeHome.removeAllAttributes( secondaryIdentity.getId( ) );

            response.setCustomerId( primaryIdentity.getCustomerId( ) );
            response.setConnectionId( primaryIdentity.getConnectionId( ) );
            response.setLastUpdateDate( primaryIdentity.getLastUpdateDate( ) );

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
            TransactionManager.commitTransaction( null );

            /* Historique des modifications */
            for ( AttributeStatus attributeStatus : attrStatusList )
            {
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( AttributeChangeType.MERGE, primaryIdentity, attributeStatus, author,
                        clientCode );
            }

            /* Indexation */
            final Map<String, String> primaryMetadata = new HashMap<>( );
            primaryMetadata.put( Constants.METADATA_MERGED_MASTER_IDENTITY_CUID, primaryIdentity.getCustomerId( ) );
            primaryMetadata.put( Constants.METADATA_DUPLICATE_RULE_CODE, request.getDuplicateRuleCode( ) );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.MERGED, secondaryIdentity,
                    response.getStatus( ).getType( ).name( ), response.getStatus( ).getType( ).name( ), author, clientCode, primaryMetadata );

            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, CONSOLIDATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( primaryIdentity.getCustomerId( ) ),
                    SPECIFIC_ORIGIN );

            final Map<String, String> secondaryMetadata = new HashMap<>( );
            secondaryMetadata.put( Constants.METADATA_MERGED_CHILD_IDENTITY_CUID, secondaryIdentity.getCustomerId( ) );
            secondaryMetadata.put( Constants.METADATA_DUPLICATE_RULE_CODE, request.getDuplicateRuleCode( ) );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.CONSOLIDATED, primaryIdentity,
                    response.getStatus( ).getType( ).name( ), response.getStatus( ).getType( ).name( ), author, clientCode, secondaryMetadata );

            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, MERGE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( secondaryIdentity.getCustomerId( ) ),
                    SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( e.getMessage( ) ).setMessageKey( Constants.PROPERTY_REST_ERROR_DURING_TREATMENT ) );
            TransactionManager.rollBack( null );
        }

        return primaryIdentity;
    }

    /**
     * Detach a merged {@link Identity} from its master {@link Identity}
     * 
     * @param request
     *            the unmerge request
     * @param author
     *            the author of the request
     * @param clientCode
     *            the client code of the calling application
     * @param response
     *            the status of the execution
     */
    public void cancelMerge( final IdentityMergeRequest request, final RequestAuthor author, final String clientCode, final IdentityMergeResponse response )
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( request.getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "Could not find primary identity with customer_id " + request.getPrimaryCuid( ) )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_NOT_FOUND ) );
            return;
        }

        if ( !Objects.equals( primaryIdentity.getLastUpdateDate( ), request.getPrimaryLastUpdateDate( ) ) )
        {
            response.setStatus( ResponseStatusFactory.failure( )
                    .setMessage( "The primary identity has been updated recently, please load the latest data before canceling merge." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_PRIMARY_IDENTITY_UPDATE_CONFLICT ) );
            return;
        }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( request.getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Could not find secondary identity with customer_id " + request.getSecondaryCuid( ) )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_NOT_FOUND ) );
            return;
        }

        if ( !secondaryIdentity.isMerged( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( ) + " is not merged" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_NOT_MERGED ) );
            return;
        }

        if ( !Objects.equals( secondaryIdentity.getMasterIdentityId( ), primaryIdentity.getId( ) ) )
        {
            response.setStatus( ResponseStatusFactory.failure( )
                    .setMessage( "Secondary identity found with customer_id " + request.getSecondaryCuid( )
                            + " is not merged to Primary identity found with customer ID " + request.getPrimaryCuid( ) )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITIES_NOT_MERGED_TOGETHER ) );
            return;
        }

        // TODO il n'y a pas d'API permettant de récupérer une identité merged, on renvoie systématiquement le master => impossible d'unmerge en passant ce test
        // if ( !Objects.equals( secondaryIdentity.getLastUpdateDate( ), request.getSecondaryLastUpdateDate( ) ) )
        // {
        // response.setStatus( ResponseStatusFactory.failure( )
        // .setMessage( "The secondary identity has been updated recently, please load the latest data before canceling merge." )
        // .setMessageKey( Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_UPDATE_CONFLICT ) );
        // return;
        // }

        TransactionManager.beginTransaction( null );
        try
        {
            /* Tag de l'identité secondaire */
            IdentityHome.cancelMerge( secondaryIdentity );
            response.setStatus( ResponseStatusFactory.success( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            TransactionManager.commitTransaction( null );

            /* Indexation */
            final Map<String, String> secondaryMetadata = new HashMap<>( );
            secondaryMetadata.put( Constants.METADATA_UNMERGED_MASTER_CUID, primaryIdentity.getCustomerId( ) );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.MERGE_CANCELLED, secondaryIdentity,
                    response.getStatus( ).getType( ).name( ), response.getStatus( ).getType( ).name( ), author, clientCode, secondaryMetadata );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, CANCEL_MERGE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( secondaryIdentity.getCustomerId( ) ),
                    SPECIFIC_ORIGIN );

            final Map<String, String> primaryMetadata = new HashMap<>( );
            primaryMetadata.put( Constants.METADATA_UNMERGED_CHILD_CUID, secondaryIdentity.getCustomerId( ) );
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.CONSOLIDATION_CANCELLED, primaryIdentity,
                    response.getStatus( ).getType( ).name( ), response.getStatus( ).getType( ).name( ), author, clientCode, primaryMetadata );
            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, CANCEL_CONSOLIDATE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( primaryIdentity.getCustomerId( ) ),
                    SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( e.getMessage( ) ).setMessageKey( Constants.PROPERTY_REST_ERROR_DURING_TREATMENT ) );
            TransactionManager.rollBack( null );
        }
    }

    /**
     * Imports an {@link Identity} according to the given {@link IdentityChangeRequest}
     *
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param author
     *            the author of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the imported {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity importIdentity( final IdentityChangeRequest identityChangeRequest, final RequestAuthor author, final String clientCode,
            final IdentityChangeResponse response ) throws IdentityStoreException
    {
        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue ) );

        final DuplicateSearchResponse certitudeDuplicates = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_STRICT, "" );
        if ( certitudeDuplicates != null && CollectionUtils.isNotEmpty( certitudeDuplicates.getIdentities( ) ) )
        {
            if ( certitudeDuplicates.getIdentities( ).size( ) == 1 )
            {
                final IdentityDto strictDuplicate = certitudeDuplicates.getIdentities( ).get( 0 );
                identityChangeRequest.getIdentity( ).setLastUpdateDate( strictDuplicate.getLastUpdateDate( ) );
                return this.update( strictDuplicate.getCustomerId( ), identityChangeRequest, author, clientCode, response );
            }
            else
            {
                response.setStatus( ResponseStatusFactory.conflict( ).setMessage( certitudeDuplicates.getStatus( ).getMessage( ) )
                        .setMessageKey( certitudeDuplicates.getStatus( ).getMessageKey( ) ) );
            }
        }
        else
        {
            final DuplicateSearchResponse suspicionDuplicates = this.checkDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION, "" );
            if ( suspicionDuplicates != null && CollectionUtils.isNotEmpty( suspicionDuplicates.getIdentities( ) ) )
            {
                response.setStatus( ResponseStatusFactory.conflict( ).setMessage( suspicionDuplicates.getStatus( ).getMessage( ) )
                        .setMessageKey( suspicionDuplicates.getStatus( ).getMessageKey( ) ) );
            }
            else
            {
                return this.create( identityChangeRequest, author, clientCode, response );
            }
        }

        return null;
    }

    public IdentityDto getQualifiedIdentity( final String customerId ) throws IdentityStoreException
    {
        final Identity identity = IdentityHome.findByCustomerId( customerId );
        final IdentityDto qualifiedIdentity = DtoConverter.convertIdentityToDto( identity );
        IdentityQualityService.instance( ).computeQuality( qualifiedIdentity );
        return qualifiedIdentity;
    }

    /**
     * Perform an identity research over a list of attributes (key and values) specified in the {@link IdentitySearchRequest}
     *
     * @param request
     *            the {@link IdentitySearchRequest} holding the parameters of the research
     * @param author
     *            the author of the request
     * @param response
     *            the {@link IdentitySearchResponse} holding the status of the execution status and the results of the request
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @throws ServiceContractNotFoundException
     *             in case of {@link ServiceContract} management error
     * @throws IdentityAttributeNotFoundException
     *             in case of {@link AttributeKey} management error
     */
    public void search( final IdentitySearchRequest request, final RequestAuthor author, final IdentitySearchResponse response, final String clientCode )
            throws IdentityStoreException
    {
        AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, SEARCH_IDENTITY_EVENT_CODE,
                _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( request.toString( ) ), SPECIFIC_ORIGIN );
        final List<SearchAttribute> providedAttributes = request.getSearch( ).getAttributes( );
        final Set<String> providedKeys = commonKeytoKey( providedAttributes.stream( ).map( SearchAttribute::getKey ).collect( Collectors.toSet( ) ) );

        final List<IdentitySearchRule> searchRules = IdentitySearchRuleHome.findAll( );
        boolean hasRequirements = searchRules.isEmpty();
        final Iterator<IdentitySearchRule> iterator = searchRules.iterator( );
        while ( !hasRequirements && iterator.hasNext( ) )
        {
            final IdentitySearchRule searchRule = iterator.next( );
            final Set<String> requiredKeys = searchRule.getAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) );
            if ( searchRule.getType( ) == SearchRuleType.AND )
            {
                if ( providedKeys.containsAll( requiredKeys ) )
                {
                    hasRequirements = true;
                }
            }
            else
                if ( searchRule.getType( ) == SearchRuleType.OR )
                {
                    if ( providedKeys.stream( ).anyMatch( requiredKeys::contains ) )
                    {
                        hasRequirements = true;
                    }
                }
        }

        if ( !hasRequirements )
        {
            final StringBuilder sb = new StringBuilder( );
            final Iterator<IdentitySearchRule> ruleIt = searchRules.iterator( );
            while ( ruleIt.hasNext( ) )
            {
                final IdentitySearchRule rule = ruleIt.next( );
                sb.append( "( " );
                final Iterator<AttributeKey> attrIt = rule.getAttributes( ).iterator( );
                while ( attrIt.hasNext( ) )
                {
                    final AttributeKey attr = attrIt.next( );
                    sb.append( attr.getKeyName( ) ).append( " " );
                    if ( attrIt.hasNext( ) )
                    {
                        sb.append( rule.getType( ).name( ) ).append( " " );
                    }
                }
                sb.append( ")" );
                if ( ruleIt.hasNext( ) )
                {
                    sb.append( " OR " );
                }
            }
            final IdentitySearchMessage alert = new IdentitySearchMessage( );
            alert.setAttributeName( sb.toString( ) );
            alert.setMessage( "Please provide those required attributes to be able to search identities." );
            response.getAlerts( ).add( alert );
            response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_MISSING_MANDATORY_ATTRIBUTES ) );
            return;
        }

        final QualifiedIdentitySearchResult result = _elasticSearchIdentityService.getQualifiedIdentities( providedAttributes, request.getMax( ),
                request.isConnected( ), Collections.emptyList( ) );
        if ( CollectionUtils.isNotEmpty( result.getQualifiedIdentities( ) ) )
        {
            final List<IdentityDto> filteredIdentities = this.getEnrichedIdentities( request.getSearch( ).getAttributes( ), clientCode,
                    result.getQualifiedIdentities( ) );
            response.setIdentities( filteredIdentities );
            if ( CollectionUtils.isNotEmpty( response.getIdentities( ) ) )
            {
                response.setStatus( ResponseStatusFactory.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
                for ( final IdentityDto identity : response.getIdentities( ) )
                {
                    AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, SEARCH_IDENTITY_EVENT_CODE,
                            _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( identity.getCustomerId( ) ),
                            SPECIFIC_ORIGIN );
                    if ( author.getType( ).equals( AuthorType.agent ) )
                    {
                        /* Indexation et historique */
                        _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.READ,
                                DtoConverter.convertDtoToIdentity( identity ), response.getStatus( ).getType( ).name( ), response.getStatus( ).getMessage( ),
                                author, clientCode, new HashMap<>( ) );
                    }
                }
            }
            else
            {
                response.setStatus( ResponseStatusFactory.noResult( ).setMessageKey( Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND ) );
            }
        }
        else
        {
            response.setStatus( ResponseStatusFactory.noResult( ).setMessageKey( Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND ) );
        }
    }

    /***
     * Check if the attributes are commonKeys, in that case it change them to the attributeKeys it refer
     *
     * @param providedAttributes
     * @return the list of keys
     */
    private Set<String> commonKeytoKey( Set<String> providedAttributes )
    {
        Set<String> returnKeys = new HashSet<>( );

        for ( String attribute : providedAttributes )
        {
            List<AttributeKey> keys = IdentityAttributeService.instance( ).getCommonAttributeKeys( attribute );
            if ( keys != null && !keys.isEmpty( ) )
            {
                for ( AttributeKey key : keys )
                {
                    returnKeys.add( key.getKeyName( ) );
                }
            }
            else
            {
                returnKeys.add( attribute );
            }
        }
        return returnKeys;
    }

    /**
     * Perform an identity research by customer or connection ID.
     *
     * @param customerId
     * @param connectionId
     * @param response
     * @param clientCode
     * @param author
     *            the author of the request
     * @throws IdentityAttributeNotFoundException
     * @throws ServiceContractNotFoundException
     */
    public void search( final String customerId, final String connectionId, final IdentitySearchResponse response, final String clientCode,
            final RequestAuthor author ) throws IdentityStoreException
    {
        AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, GET_IDENTITY_EVENT_CODE, _internalUserService.getApiUser( clientCode ),
                SecurityUtil.logForgingProtect( StringUtils.isNotBlank( customerId ) ? customerId : connectionId ), SPECIFIC_ORIGIN );

        final ServiceContract serviceContract = _serviceContractService.getActiveServiceContract( clientCode );
        if ( serviceContract == null )
        {
            throw new ServiceContractNotFoundException( "No active service contract could be found for clientCode = " + clientCode );
        }
        final IdentityDto identityDto = StringUtils.isNotBlank( customerId ) ? _identityDtoCache.getByCustomerId( customerId, serviceContract )
                : _identityDtoCache.getByConnectionId( connectionId, serviceContract );
        if ( identityDto == null )
        {
            // #345 : If the identity doesn't exist, make an extra search in the history (only for CUID search).
            // If there is a record, it means the identity has been deleted => send back a specific message
            if ( StringUtils.isNotBlank( customerId ) && !IdentityHome.findHistoryByCustomerId( customerId ).isEmpty( ) )
            {
                response.setStatus( ResponseStatusFactory.notFound( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_DELETED ) );
            }
            else
            {
                response.setStatus( ResponseStatusFactory.notFound( ).setMessageKey( Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND ) );
            }
        }
        else
        {
            response.setIdentities( Collections.singletonList( identityDto ) );
            response.setStatus( ResponseStatusFactory.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            // #27998 : Dans le cas d'une interrogation sur un CUID/GUID rapproché, ajouter une ligne dans le bloc "Alerte" dans la réponse de l'identité consolidée
            if ((StringUtils.isNotBlank(customerId) && !identityDto.getCustomerId().equals(customerId)) ||
                (StringUtils.isNotBlank(connectionId) && !identityDto.getConnectionId().equals(connectionId))) {
                final IdentitySearchMessage alert = new IdentitySearchMessage();
                alert.setMessage("Le CUID ou GUID demandé correspond à une identité rapprochée. Cette réponse contient l'identité consilidée.");
                response.getAlerts().add(alert);
            }
            if ( author != null )
            {
                AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_READ, SEARCH_IDENTITY_EVENT_CODE,
                        _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( identityDto.getCustomerId( ) ),
                        SPECIFIC_ORIGIN );
            }
            if ( author != null && author.getType( ).equals( AuthorType.agent ) )
            {
                /* Indexation et historique */
                _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.READ, DtoConverter.convertDtoToIdentity( identityDto ),
                        response.getStatus( ).getType( ).name( ), response.getStatus( ).getMessage( ), author, clientCode, new HashMap<>( ) );
            }
        }
    }

    /**
     * Performs a search of a list of {@link IdentityDto}, providing a list of customer ids
     * 
     * @param customerIds
     *            the customer ids to search for
     * @return a list of {@link IdentityDto}
     */
    public List<IdentityDto> search( final List<String> customerIds, final List<String> attributes )
    {
        try
        {
            final QualifiedIdentitySearchResult result = _elasticSearchIdentityService.getQualifiedIdentities( customerIds, attributes );
            if ( result != null && CollectionUtils.isNotEmpty( result.getQualifiedIdentities( ) ) )
            {
                result.getQualifiedIdentities( ).forEach( identityDto -> {
                    IdentityQualityService.instance( ).computeQuality( identityDto );
                    identityDto.getQuality( ).setScoring( 1D );
                    identityDto.getQuality( ).setCoverage( 1 );
                } );
                return result.getQualifiedIdentities( );
            }
        }
        catch( final IdentityStoreException e )
        {
            // ignore this identity
        }
        return new ArrayList<>( );
    }

    /**
     * Performs a search of an {@link IdentityDto}, providing its customer id
     * 
     * @param customerId
     *            the customer id to search for
     * @return an {@link IdentityDto}
     */
    public IdentityDto search( final String customerId )
    {
        try
        {
            final QualifiedIdentitySearchResult result = _elasticSearchIdentityService.getQualifiedIdentities( customerId, Collections.emptyList( ) );
            if ( result != null && CollectionUtils.isNotEmpty( result.getQualifiedIdentities( ) ) )
            {
                final IdentityDto identityDto = result.getQualifiedIdentities( ).get( 0 );
                IdentityQualityService.instance( ).computeQuality( identityDto );
                identityDto.getQuality( ).setScoring( 1D );
                identityDto.getQuality( ).setCoverage( 1 );
                return identityDto;
            }
        }
        catch( final IdentityStoreException e )
        {
            // ignore this identity
        }
        return null;
    }

    /**
     * Filter a list of search results over {@link ServiceContract} defined for the given clientCode. Also complete identities with additional information
     * (quality, duplicates, ...).
     * 
     * @param searchAttributes
     *            la requête de recherche si existante
     * @param clientCode
     *            le code client du demandeur
     * @param identities
     *            la liste de résultats à traiter
     * @return the list of filtered and completed {@link IdentityDto}
     * @throws ServiceContractNotFoundException
     *             in case of error
     */
    private List<IdentityDto> getEnrichedIdentities( final List<SearchAttribute> searchAttributes, final String clientCode, final List<IdentityDto> identities )
            throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = _serviceContractService.getActiveServiceContract( clientCode );
        final Comparator<QualityDefinition> qualityComparator = Comparator.comparing( QualityDefinition::getScoring )
                .thenComparingDouble( QualityDefinition::getQuality ).reversed( );
        final Comparator<IdentityDto> identityComparator = Comparator.comparing( IdentityDto::getQuality, qualityComparator );
        return identities.stream( ).filter( IdentityDto::isNotMerged )
                .peek( identity -> IdentityQualityService.instance( ).enrich( searchAttributes, identity, serviceContract, null ) ).sorted( identityComparator )
                .collect( Collectors.toList( ) );
    }

    private List<AttributeStatus> updateIdentity( final IdentityDto requestIdentity, final String clientCode, final ChangeResponse response,
            final Identity identity ) throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );

        /* Récupération des attributs déja existants ou non */
        final Map<Boolean, List<AttributeDto>> sortedAttributes = requestIdentity.getAttributes( ).stream( )
                .collect( Collectors.partitioningBy( a -> identity.getAttributes( ).containsKey( a.getKey( ) ) ) );
        final List<AttributeDto> existingWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( true ) ) ? sortedAttributes.get( true )
                : new ArrayList<>( );
        final List<AttributeDto> newWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( false ) ) ? sortedAttributes.get( false )
                : new ArrayList<>( );

        // If identity is connected and service contract doesn't allow unrestricted update, do a bunch of checks
        if ( identity.isConnected( ) && !_serviceContractService.canModifyConnectedIdentity( clientCode ) )
        {
            this.connectedIdentityUpdateCheck( requestIdentity, identity, existingWritableAttributes, newWritableAttributes, response );
            if ( ResponseStatusFactory.unauthorized( ).equals( response.getStatus( ) ) )
            {
                return attrStatusList;
            }
        }

        /* Create or Update birth country and city */
        attrStatusList.addAll( GeocodesService.processCountryAndCityForUpdate( identity, newWritableAttributes, existingWritableAttributes, clientCode ) );

        /* Create new attributes */
        for ( final AttributeDto attributeToWrite : newWritableAttributes )
        {
            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( attributeToWrite, identity, clientCode );
            attrStatusList.add( attributeStatus );
        }

        /* Update existing attributes */
        for ( final AttributeDto attributeToUpdate : existingWritableAttributes )
        {
            final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( attributeToUpdate, identity, clientCode );
            attrStatusList.add( attributeStatus );
        }

        boolean monParisUpdated = false;
        if ( requestIdentity.getMonParisActive( ) != null && requestIdentity.getMonParisActive( ) != identity.isMonParisActive( ) )
        {
            monParisUpdated = true;
            identity.setMonParisActive( requestIdentity.isMonParisActive( ) );
        }

        if ( monParisUpdated || !Collections.disjoint( AttributeChangeStatus.getSuccessStatuses( ),
                attrStatusList.stream( ).map( AttributeStatus::getStatus ).collect( Collectors.toList( ) ) ) )
        {
            // If there was an update on the monParis flag or in the attributes, we update the identity
            IdentityHome.update( identity );
        }

        return attrStatusList;
    }

    /**
     * Makes a bunch of checks regarding the validity of this update request on this connected identity.
     * <ul>
     * <li>Authorise update on "PIVOT" attributes only</li>
     * <li>For new attributes, certification level must be > 100 (better than self-declare)</li>
     * <li>For existing attributes, certification level must be >= than the existing level</li>
     * <li>If one "PIVOT" attribute is certified at a certain level N (conf) :
     * <ul>
     * <li>All "PIVOT" attributes must be set</li>
     * <li>All "PIVOT" attributes must be certified with level greater or equal to N</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param requestIdentity
     *            the request
     * @param identity
     *            the identity
     * @param existingWritableAttributes
     *            existing attributes in identity from request
     * @param newWritableAttributes
     *            new attributes from request
     */
    private void connectedIdentityUpdateCheck( final IdentityDto requestIdentity, final Identity identity, final List<AttributeDto> existingWritableAttributes,
            final List<AttributeDto> newWritableAttributes, final ChangeResponse response )
    {
        // TODO refactor to use cache ?
        final Map<String, AttributeKey> allAttributesByKey = AttributeKeyHome.getAttributeKeysList( false ).stream( )
                .collect( Collectors.toMap( AttributeKey::getKeyName, a -> a ) );

        // - Authorise update on "PIVOT" attributes only
        final boolean requestOnNonPivot = requestIdentity.getAttributes( ).stream( ).map( a -> allAttributesByKey.get( a.getKey( ) ) )
                .anyMatch( a -> !a.getPivot( ) );
        if ( requestOnNonPivot )
        {
            response.setStatus( ResponseStatusFactory.unauthorized( ).setMessage( "Identity is connected, updating non 'pivot' attributes is forbidden." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_UPDATE_NON_PIVOT ) );
            return;
        }

        // - For new attributes, certification level must be > 100 (better than self-declare)
        final boolean newAttrSelfDeclare = newWritableAttributes.stream( )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) )
                .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) <= 100 );
        if ( newAttrSelfDeclare )
        {
            response.setStatus( ResponseStatusFactory.unauthorized( )
                    .setMessage( "Identity is connected, adding 'pivot' attributes with self-declarative certification level is forbidden." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_SELF_DECLARE ) );
            return;
        }

        // - For existing attributes, certification level must be >= than the existing level
        final boolean lesserWantedLvl = existingWritableAttributes.stream( )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) ).anyMatch( wantedCertif -> {
                    final int wantedLvl = Integer.parseInt( wantedCertif.getRefCertificationLevel( ).getLevel( ) );

                    final IdentityAttribute existingAttr = identity.getAttributes( ).get( wantedCertif.getAttributeKey( ).getKeyName( ) );
                    final RefAttributeCertificationLevel existingCertif = RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName(
                            existingAttr.getCertificate( ).getCertifierCode( ), existingAttr.getAttributeKey( ).getKeyName( ) );
                    final int existingLvl = Integer.parseInt( existingCertif.getRefCertificationLevel( ).getLevel( ) );

                    return wantedLvl < existingLvl;
                } );
        if ( lesserWantedLvl )
        {
            response.setStatus( ResponseStatusFactory.unauthorized( )
                    .setMessage( "Identity is connected, updating existing 'pivot' attributes with lesser certification level is forbidden." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_UPDATE_PIVOT_LESSER_CERTIFICATION ) );
            return;
        }

        // - If one "PIVOT" attribute is certified at a certain level N (conf), all "PIVOT" attributes must be set and certified with level >= N.
        final int threshold = AppPropertiesService.getPropertyInt( PIVOT_CERTIF_LEVEL_THRESHOLD, 400 );
        final boolean breakingThreshold = identity.getAttributes( ).values( ).stream( ).filter( a -> a.getAttributeKey( ).getPivot( ) )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertificate( ).getCertifierCode( ),
                        a.getAttributeKey( ).getKeyName( ) ) )
                .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) >= threshold )
                || requestIdentity.getAttributes( ).stream( )
                        .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) )
                        .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) >= threshold );
        if ( breakingThreshold )
        {
            // get all pivot attributes from database
            final List<String> pivotAttributeKeys = allAttributesByKey.values( ).stream( ).filter( AttributeKey::getPivot ).map( AttributeKey::getKeyName )
                    .collect( Collectors.toList( ) );

            // if any pivot is missing from request + existing -> unauthorized
            @SuppressWarnings( "unchecked" )
            final Collection<String> unionOfExistingAndRequestedPivotKeys = CollectionUtils.union(
                    requestIdentity.getAttributes( ).stream( ).map( AttributeDto::getKey ).collect( Collectors.toSet( ) ),
                    identity.getAttributes( ).values( ).stream( ).map( IdentityAttribute::getAttributeKey ).filter( AttributeKey::getPivot )
                            .map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) ) );
            if ( !CollectionUtils.isEqualCollection( pivotAttributeKeys, unionOfExistingAndRequestedPivotKeys ) )
            {
                response.setStatus( ResponseStatusFactory.unauthorized( )
                        .setMessage( "Identity is connected, and at least one 'pivot' attribute is, or has been requested to be, certified above level "
                                + threshold + ". In that case, all 'pivot' attributes must be set, and certified with level greater or equal to " + threshold
                                + "." )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_CERTIFICATION_UNDER_THRESHOLD ) );
                return;
            }

            // if any has level lesser than threshold -> unauthorized
            final boolean lesserThanThreshold = pivotAttributeKeys.stream( ).map( key -> {
                final AttributeDto requested = requestIdentity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( key ) ).findFirst( ).orElse( null );
                final IdentityAttribute existing = identity.getAttributes( ).get( key );
                int requestedLvl = 0;
                int existingLvl = 0;
                if ( requested != null )
                {
                    requestedLvl = Integer.parseInt( RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( requested.getCertifier( ), key )
                            .getRefCertificationLevel( ).getLevel( ) );
                }
                if ( existing != null )
                {
                    existingLvl = Integer.parseInt(
                            RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( existing.getCertificate( ).getCertifierCode( ), key )
                                    .getRefCertificationLevel( ).getLevel( ) );
                }
                return Math.max( requestedLvl, existingLvl );
            } ).anyMatch( lvl -> lvl < threshold );

            if ( lesserThanThreshold )
            {
                response.setStatus( ResponseStatusFactory.unauthorized( )
                        .setMessage( "Identity is connected, and at least one 'pivot' attribute is, or has been requested to be, certified above level "
                                + threshold + ". In that case, all 'pivot' attributes must be set, and certified with level greater or equal to " + threshold
                                + "." )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_CERTIFICATION_UNDER_THRESHOLD ) );
            }
        }
    }

    /**
     * Gets a list of qualified identities on which to search potential duplicates.<br/>
     * Returned identities must have all attributes checked by the provided rule, and must also not be already merged nor be tagged as suspicious.<br/>
     * The list is sorted by quality (higher quality identities first).
     *
     * @param rule
     *            the rule used to get matching identities
     * @param batchSize the size of the batches
     * @param includeSuspicions filter CUIDs, to include or not, the CUIDs that are identified as suspicions
     * @return the list of identities
     */
    public Batch<String> getCUIDsBatchForPotentialDuplicate(final DuplicateRule rule, final int batchSize, final boolean includeSuspicions )
    {
        final List<Integer> attributes = rule.getCheckedAttributes( ).stream( ).map( AttributeKey::getId ).collect( Collectors.toList( ) );
        final List<String> customerIdsList = IdentityHome.findByAttributeExisting( attributes, rule.getNbFilledAttributes( ), true, !includeSuspicions, rule.getPriority() );
        if ( customerIdsList.isEmpty( ) )
        {
            return Batch.ofSize( Collections.emptyList( ), 0 );
        }
        return Batch.ofSize( customerIdsList, batchSize );
    }

    /**
     * request a deletion of identity .
     *
     * @param customerId
     *            the customer ID
     * @param clientCode
     *            the client code
     * @param author
     *            the author of the request
     */
    public void deleteRequest( final String customerId, final String clientCode, final RequestAuthor author, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        if ( !_serviceContractService.canDeleteIdentity( clientCode ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to request the deletion of an identity." )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_DELETE_UNAUTHORIZED ) );
            response.setCustomerId( customerId );
            return;
        }

        // check identity
        Identity identity = IdentityHome.findByCustomerId( customerId );
        if ( identity == null )
        {
            response.setStatus(
                    ResponseStatusFactory.notFound( ).setMessage( "Identity not found." ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_NOT_FOUND ) );
            response.setCustomerId( customerId );
            return;
        }
        if ( identity.isDeleted( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "Identity allready in deleted state." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALREADY_DELETED ) );
            response.setCustomerId( customerId );

            return;
        }
        if ( identity.isMerged( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "Identity in merged state can not be deleted." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_FORBIDDEN_DELETE_ON_MERGED_IDENTITY ) );
            response.setCustomerId( customerId );
            return;
        }

        TransactionManager.beginTransaction( null );
        try
        {
            // expire identity (the deletion is managed by the dedicated Daemon)
            IdentityHome.softRemove( customerId );
            response.setStatus( ResponseStatusFactory.success( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            TransactionManager.commitTransaction( null );

            /* Notify listeners for indexation, history, ... */
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.DELETE, identity, response.getStatus( ).getType( ).name( ),
                    response.getStatus( ).getMessage( ), author, clientCode, new HashMap<>( ) );

            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_DELETE, DELETE_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, clientCode ), SecurityUtil.logForgingProtect( customerId ), SPECIFIC_ORIGIN );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
        }

    }

    /**
     * Check if duplicates exist for a set of attributes
     * 
     * @param attributes
     *            the set of attributes
     * @param ruleCodeProperty
     *            the properties that defines the list of rules to check
     * @return a {@link DuplicateSearchResponse} that holds the execution result
     * @throws IdentityStoreException
     *             in case of error
     */
    private DuplicateSearchResponse checkDuplicates( final Map<String, String> attributes, final String ruleCodeProperty, final String customerId )
            throws IdentityStoreException
    {
        final List<String> ruleCodes = Arrays.stream(AppPropertiesService.getProperty( ruleCodeProperty, "" ).split( "," )).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        if( !ruleCodes.isEmpty( ) )
        {
            final DuplicateSearchResponse esDuplicates = _duplicateServiceElasticSearch.findDuplicates( attributes, customerId, ruleCodes,
                    Collections.emptyList( ) );
            if ( esDuplicates != null )
            {
                return esDuplicates;
            }
            final boolean checkDatabase = AppPropertiesService.getPropertyBoolean( PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED, false );
            if ( checkDatabase )
            {
                return _duplicateServiceDatabase.findDuplicates( attributes, "", ruleCodes, Collections.emptyList( ) );
            }
        }
        return null;
    }

    /**
     * Dé-certification d'une identité.
     *
     * @param strCustomerId
     *            customer ID
     * @return the response
     * @see IdentityAttributeService#uncertifyAttribute
     */
    public IdentityChangeResponse uncertifyIdentity( final String strCustomerId, final String strClientCode, final RequestAuthor author )
    {
        final IdentityChangeResponse response = new IdentityChangeResponse( );

        final Identity identity = IdentityHome.findByCustomerId( strCustomerId );
        if ( identity == null )
        {
            response.setStatus(
                    ResponseStatusFactory.notFound( ).setMessage( "No identity found" ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_NOT_FOUND ) );
            return response;
        }

        TransactionManager.beginTransaction( null );
        try
        {
            final List<AttributeStatus> attrStatusList = new ArrayList<>( );
            for ( final IdentityAttribute attribute : identity.getAttributes( ).values( ) )
            {
                final AttributeStatus status = _identityAttributeService.uncertifyAttribute( attribute );
                attrStatusList.add( status );
            }

            // update identity to set lastupdate_date
            IdentityHome.update( identity );

            response.setLastUpdateDate( identity.getLastUpdateDate( ) );
            response.setStatus( ResponseStatusFactory.success( ).setAttributeStatuses( attrStatusList )
                    .setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
            TransactionManager.commitTransaction( null );

            /* Historique des modifications */
            for ( AttributeStatus attributeStatus : attrStatusList )
            {
                _identityStoreNotifyListenerService.notifyListenersAttributeChange( AttributeChangeType.UPDATE, identity, attributeStatus, author,
                        strClientCode );
            }

            /* Indexation et historique */
            _identityStoreNotifyListenerService.notifyListenersIdentityChange( IdentityChangeType.UPDATE, identity, response.getStatus( ).getType( ).name( ),
                    response.getStatus( ).getMessage( ), author, strClientCode, new HashMap<>( ) );

            AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, DECERTIFY_IDENTITY_EVENT_CODE,
                    _internalUserService.getApiUser( author, strClientCode ), SecurityUtil.logForgingProtect( strCustomerId ), SPECIFIC_ORIGIN );
        }
        catch( final Exception e )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setMessage( e.getMessage( ) ).setMessageKey( Constants.PROPERTY_REST_ERROR_DURING_TREATMENT ) );
            TransactionManager.rollBack( null );
        }

        return response;
    }

    /**
     * Delete the identity and all his children, including potential merged identities, EXCEPT identity history.<br/>
     * The purge consist of deleting, for the identity and all of its potential merged identities :
     * <ul>
     * <li>the {@link Identity} object</li>
     * <li>the {@link IdentityAttribute} objetcs</li>
     * <li>the IdentityAttributeHistory objects</li>
     * <li>the {@link SuspiciousIdentity} objects</li>
     * <li>the {@link ExcludedIdentities} objects</li>
     * </ul>
     * The identity's history is kept.
     * 
     * @param customerId
     *            the customerId of the identity to delete
     */
    public void delete( final String customerId )
    {
        final int identityId = IdentityHome.findIdByCustomerId( customerId );
        if ( identityId != -1 )
        {
            final List<Identity> mergedIdentities = IdentityHome.findMergedIdentities( identityId );
            TransactionManager.beginTransaction( null );
            try
            {
                // Delete eventual merged identities first
                for ( final Identity mergedIdentity : mergedIdentities )
                {
                    SuspiciousIdentityHome.remove( mergedIdentity.getCustomerId( ) );
                    SuspiciousIdentityHome.removeExcludedIdentities( mergedIdentity.getCustomerId( ) );
                    IdentityHome.deleteAttributeHistory( mergedIdentity.getId( ) );
                    IdentityHome.hardRemove( mergedIdentity.getId( ) );
                }
                // Delete the actual identity
                SuspiciousIdentityHome.remove( customerId );
                SuspiciousIdentityHome.removeExcludedIdentities( customerId );
                IdentityHome.deleteAttributeHistory( identityId );
                IdentityHome.hardRemove( identityId );

                TransactionManager.commitTransaction( null );
            }
            catch( final Exception e )
            {
                TransactionManager.rollBack( null );
                throw e;
            }
        }
    }

}

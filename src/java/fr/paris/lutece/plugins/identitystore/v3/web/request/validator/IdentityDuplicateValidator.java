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
package fr.paris.lutece.plugins.identitystore.v3.web.request.validator;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.service.duplicate.DuplicateRuleService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.DuplicateSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentitySearchResult;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.sql.TransactionManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityDuplicateValidator
{

    private static final String PROPERTY_DUPLICATES_CREATION_RULES = "identitystore.identity.duplicates.creation.rules";
    private static final String PROPERTY_DUPLICATES_UPDATE_RULES = "identitystore.identity.duplicates.update.rules";
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION = "identitystore.identity.duplicates.import.rules.suspicion";
    private static final String PROPERTY_DUPLICATES_IMPORT_RULES_STRICT = "identitystore.identity.duplicates.import.rules.strict";
    private static final String PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED = "identitystore.identity.duplicates.check.database";

    private static IdentityDuplicateValidator instance;

    private final IDuplicateService _duplicateServiceDatabase = SpringContextService.getBean( "identitystore.duplicateService.database" );
    private final IDuplicateService _duplicateServiceElasticSearch = SpringContextService.getBean( "identitystore.duplicateService.elasticsearch" );

    public static IdentityDuplicateValidator instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityDuplicateValidator( );
        }
        return instance;
    }

    private IdentityDuplicateValidator( )
    {
    }

    /**
     * Checks if GUID is already in use for create request
     * 
     * @param request
     *            the create request
     * @throws DuplicatesConsistencyException
     */
    public void checkConnectionIdUniquenessForCreate( final IdentityChangeRequest request ) throws DuplicatesConsistencyException
    {
        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) )
                && IdentityHome.findByConnectionId( request.getIdentity( ).getConnectionId( ) ) != null )
        {
            throw new DuplicatesConsistencyException( "GUID is already in use.", Constants.PROPERTY_REST_ERROR_IDENTITY_CREATE_GUID_ALREADY_EXISTS );
        }
    }

    /**
     * Checks if GUID is already in use for update request
     *
     * @param request
     *            the update request
     * @throws DuplicatesConsistencyException
     */
    public void checkConnectionIdUniquenessForUpdate( final IdentityChangeRequest request, final IdentityDto existingIdentityToUpdate )
            throws DuplicatesConsistencyException
    {
        if ( StringUtils.isNotEmpty( request.getIdentity( ).getConnectionId( ) )
                && !StringUtils.equals( existingIdentityToUpdate.getConnectionId( ), request.getIdentity( ).getConnectionId( ) ) )
        {
            final Identity byConnectionId = IdentityHome.findByConnectionId( request.getIdentity( ).getConnectionId( ) );
            if ( byConnectionId != null )
            {
                final DuplicatesConsistencyException exception = new DuplicatesConsistencyException(
                        "An identity already exists with the given connection ID. The customer ID of that identity is provided in the response.",
                        Constants.PROPERTY_REST_ERROR_CONFLICT_CONNECTION_ID, IdentityChangeResponse.class );
                ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( byConnectionId.getCustomerId( ) );
                throw exception;
            }
        }
    }

    /**
     * Checks if the provided create request will introduce duplicates
     * 
     * @param request
     *            the request
     * @throws DuplicatesConsistencyException
     */
    public void checkDuplicateExistenceForCreation( final IdentityChangeRequest request ) throws DuplicatesConsistencyException
    {
        final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue ) );
        this.checkDuplicates( attributes, PROPERTY_DUPLICATES_CREATION_RULES, "" );
    }

    /**
     * Checks if the provided update request will introduce duplicates
     *
     * @param request
     *            the request
     * @param existingIdentityToUpdate
     *            the identity to update
     * @throws DuplicatesConsistencyException
     */
    public void checkDuplicateExistenceForUpdate( final IdentityChangeRequest request, final IdentityDto existingIdentityToUpdate )
            throws DuplicatesConsistencyException
    {
        if ( doesRequestContainsAttributeValueChangesImpactingRules( request, existingIdentityToUpdate, PROPERTY_DUPLICATES_UPDATE_RULES ) )
        {
            // collect all non blank attributes from request
            final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                    .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue ) );
            // add other existing identity attributes
            existingIdentityToUpdate.getAttributes( ).forEach( exAttr -> attributes.putIfAbsent( exAttr.getKey( ), exAttr.getValue( ) ) );

            // remove attributes that have blank values in the request
            request.getIdentity( ).getAttributes( ).stream( ).filter( a -> StringUtils.isBlank( a.getValue( ) ) )
                    .forEach( a -> attributes.remove( a.getKey( ) ) );

            // search for potential duplicates with those attributes
            this.checkDuplicates( attributes, PROPERTY_DUPLICATES_UPDATE_RULES, existingIdentityToUpdate.getCustomerId( ) );
        }
    }

    /**
     * Checks if the provided import request will introduce duplicates
     *
     * @param request
     *            the import request
     * @return If a unique strict duplicate is found, its CUID is returned, <code>null</code> otherwise
     * @throws DuplicatesConsistencyException
     *             if there is more than one strict duplicate, or any approximated duplicate
     */
    public String checkDuplicateExistenceForImport( final IdentityChangeRequest request ) throws DuplicatesConsistencyException
    {
        final Map<String, String> attributes = request.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue ) );

        final Map<String, QualifiedIdentitySearchResult> certitudeDuplicates = this.findDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_STRICT, "" );
        if ( certitudeDuplicates.values().stream().anyMatch(r -> !r.getQualifiedIdentities().isEmpty()) )
        {
            final List<IdentityDto> duplicates = new ArrayList<>();
            certitudeDuplicates.values().stream().flatMap(r -> r.getQualifiedIdentities().stream()).forEach(identity -> {
                if ( duplicates.stream( ).noneMatch( existing -> Objects.equals( existing.getCustomerId( ), identity.getCustomerId( ) ) ) )
                {
                    duplicates.add( identity );
                }
            });
            if ( duplicates.size( ) == 1 )
            {
                final IdentityDto strictDuplicate = duplicates.get( 0 );
                request.getIdentity( ).setLastUpdateDate( strictDuplicate.getLastUpdateDate( ) );
                return strictDuplicate.getCustomerId( );
            }
            else
            {
                final List<String> matchingRuleCodes =
                        certitudeDuplicates.entrySet().stream().filter(e -> !e.getValue().getQualifiedIdentities().isEmpty()).map(Map.Entry::getKey)
                                  .collect(Collectors.toList());
                throw new DuplicatesConsistencyException("Potential duplicate(s) found with rule(s) : " + String.join( ",", matchingRuleCodes ),
                                                         Constants.PROPERTY_REST_INFO_POTENTIAL_DUPLICATE_FOUND );
            }
        }
        else
        {
            this.checkDuplicates( attributes, PROPERTY_DUPLICATES_IMPORT_RULES_SUSPICION, "" );
        }

        return null;
    }

    /**
     * Check if duplicates exist for reqAttr set of attributes
     *
     * @param attributes
     *            the set of attributes
     * @param ruleCodeProperty
     *            the properties that defines the list of rules to check
     * @return reqAttr {@link DuplicateSearchResponse} that holds the execution result
     * @throws DuplicatesConsistencyException
     *             in case of error
     */
    private void checkDuplicates( final Map<String, String> attributes, final String ruleCodeProperty, final String customerId )
            throws DuplicatesConsistencyException
    {
        try
        {
            final List<DuplicateRule> rules = new ArrayList<>();
            for( final String ruleCode : AppPropertiesService.getProperty(ruleCodeProperty).split(",")){
                final DuplicateRule rule = DuplicateRuleService.instance().get(ruleCode);
                DuplicateRuleValidator.instance().validateActive(rule);
                rules.add(rule);
            }

            this.checkDuplicates(_duplicateServiceElasticSearch, attributes, rules, customerId );
            if ( AppPropertiesService.getPropertyBoolean( PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED, false ) )
            {
                this.checkDuplicates(_duplicateServiceDatabase, attributes, rules, customerId );
            }
        }
        catch( final IdentityStoreException e )
        {
            if (e instanceof DuplicatesConsistencyException) {
                throw (DuplicatesConsistencyException) e;
            }
            throw new DuplicatesConsistencyException( "Error while searching for duplicates : " + e.getMessage(), Constants.PROPERTY_REST_ERROR_DUPLICATE_SEARCH );
        }
    }

    private void checkDuplicates(final IDuplicateService duplicateService, final Map<String, String> attributes, final List<DuplicateRule> rules, final String customerId )
            throws IdentityStoreException {
        final Map<String, QualifiedIdentitySearchResult> duplicates =
                duplicateService.findDuplicates(attributes, customerId, rules, Collections.emptyList( ));
        if ( duplicates.values().stream().anyMatch(r -> !r.getQualifiedIdentities().isEmpty()) )
        {
            final List<String> matchingRuleCodes =
                    duplicates.entrySet().stream().filter(e -> !e.getValue().getQualifiedIdentities().isEmpty()).map(Map.Entry::getKey)
                                .collect(Collectors.toList());
            throw new DuplicatesConsistencyException("Potential duplicate(s) found with rule(s) : " + String.join( ",", matchingRuleCodes ),
                                                     Constants.PROPERTY_REST_INFO_POTENTIAL_DUPLICATE_FOUND );
        }
    }

    private Map<String, QualifiedIdentitySearchResult> findDuplicates( final Map<String, String> attributes, final String ruleCodeProperty, final String customerId)
            throws DuplicatesConsistencyException {
        try
        {
            final List<DuplicateRule> rules = new ArrayList<>();
            for( final String ruleCode : AppPropertiesService.getProperty(ruleCodeProperty).split(",")){
                final DuplicateRule rule = DuplicateRuleService.instance().get(ruleCode);
                DuplicateRuleValidator.instance().validateActive(rule);
                rules.add(rule);
            }

            final Map<String, QualifiedIdentitySearchResult> esDuplicates = _duplicateServiceElasticSearch.findDuplicates(attributes, customerId, rules, Collections.emptyList());
            if ( (esDuplicates.isEmpty() || esDuplicates.values().stream().allMatch(r -> r.getQualifiedIdentities().isEmpty()))
                 && AppPropertiesService.getPropertyBoolean( PROPERTY_DUPLICATES_CHECK_DATABASE_ACTIVATED, false ) )
            {
                return _duplicateServiceDatabase.findDuplicates(attributes, customerId, rules, Collections.emptyList( ));
            }
            return esDuplicates;
        }
        catch( final IdentityStoreException e )
        {
            if (e instanceof DuplicatesConsistencyException) {
                throw (DuplicatesConsistencyException) e;
            }
            throw new DuplicatesConsistencyException( "Error while searching for duplicates : " + e.getMessage(), Constants.PROPERTY_REST_ERROR_DUPLICATE_SEARCH );
        }
    }

    /**
     * Returns <code>true</code> if the request aims to add new attributes, remove existing attributes, or modify existing attribute's value, of attributes
     * checked by the duplicate rules in parameter.<br/>
     * Returns <code>false</code> otherwise.
     *
     * @param request
     *            the request
     * @param existingIdentityToUpdate
     *            the identity
     */
    private boolean doesRequestContainsAttributeValueChangesImpactingRules( final IdentityChangeRequest request, final IdentityDto existingIdentityToUpdate,
            final String duplicateRulesProperty )
    {
        final Set<String> checkedAttributeKeys = Arrays.stream( AppPropertiesService.getProperty( duplicateRulesProperty ).split( "," ) )
                .map( DuplicateRuleHome::findByCode ).flatMap( rule -> rule.getCheckedAttributes( ).stream( ) ).map( AttributeKey::getKeyName )
                .collect( Collectors.toSet( ) );
        return request.getIdentity( ).getAttributes( ).stream( ).filter( reqAttr -> checkedAttributeKeys.contains( reqAttr.getKey( ) ) ).anyMatch( reqAttr -> {
            final AttributeDto existingAttr = existingIdentityToUpdate.getAttributes( ).stream( )
                    .filter( exAttr -> Objects.equals( reqAttr.getKey( ), exAttr.getKey( ) ) ).findFirst( ).orElse( null );
            if ( StringUtils.isNotBlank( reqAttr.getValue( ) ) )
            {
                return existingAttr == null || !Objects.equals( existingAttr.getValue( ), reqAttr.getValue( ) );
            }
            else
            {
                return existingAttr != null;
            }
        } );
    }

}

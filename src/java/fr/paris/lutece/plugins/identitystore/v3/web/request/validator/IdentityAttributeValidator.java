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

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidator
{
    private static final String PIVOT_CERTIF_LEVEL_THRESHOLD = "identitystore.identity.attribute.update.pivot.certif.level.threshold";

    private final IdentityAttributeValidationCache _cache = SpringContextService.getBean( "identitystore.identityAttributeValidationCache" );
    private final int pivotCertificationLevelThreshold = AppPropertiesService
            .getPropertyInt( "identitystore.identity.attribute.pivot.certification.level.threshold", 400 );
    private static IdentityAttributeValidator _instance;

    public static IdentityAttributeValidator instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeValidator( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    /**
     * Checks if the request identity contains valid attributes that exist in the referential
     *
     * @param identity
     *            the identity
     * @throws RequestFormatException
     */
    public void checkAttributeExistence( final IdentityDto identity ) throws RequestFormatException
    {
        this.checkAttributeExistence(identity.getAttributes().stream().map(AttributeDto::getKey).collect(Collectors.toList()));
    }

    /**
     * Checks if the request attribute keys correspond to valid attributes that exist in the referential
     *
     * @param attributeKeys
     *            the attribute keys
     * @throws RequestFormatException
     */
    public void checkAttributeExistence( final Collection<String> attributeKeys ) throws RequestFormatException
    {
        for ( final String attributeKey : attributeKeys )
        {
            try
            {
                IdentityAttributeService.instance( ).getAttributeKey( attributeKey );
            }
            catch( final ResourceNotFoundException e )
            {
                throw new RequestFormatException( "Attribute doesn't exist : " + attributeKey, Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY );
            }
        }
    }

    /**
     * Validates all attribute values stored in the provided identity, according to each attribute validation regex.
     *
     * @param identity
     *            the identity
     * @throws RequestContentFormattingException
     */
    public void validateIdentityAttributeValues( final IdentityDto identity ) throws RequestContentFormattingException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        if ( identity != null )
        {
            for ( final AttributeDto attribute : identity.getAttributes( ) )
            {
                if ( StringUtils.isNotBlank( attribute.getValue( ) ) )
                {
                    try
                    {
                        final Pattern validationPattern = _cache.get( attribute.getKey( ) );
                        if ( validationPattern != null )
                        {
                            if ( !validationPattern.matcher( attribute.getValue( ) ).matches( ) )
                            {
                                attrStatusList.add( this.buildAttributeValueValidationErrorStatus( attribute.getKey( ) ) );
                            }
                        }
                    }
                    catch( final ResourceNotFoundException e )
                    {
                        // If attribute doesn't exist, do nothing.
                    }
                }
            }
        }
        if ( !attrStatusList.isEmpty( ) )
        {
            final RequestContentFormattingException exception = new RequestContentFormattingException(
                    "Some attribute values are not passing validation. Please check in the attribute statuses for details.",
                    Constants.PROPERTY_REST_ERROR_FAIL_ATTRIBUTE_VALIDATION );
            exception.getResponse( ).getStatus( ).setAttributeStatuses( attrStatusList );
            throw exception;
        }
    }

    /**
     * Builds an attribute status for invalid value.
     *
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValueValidationErrorStatus( final String attrStrKey ) throws ResourceNotFoundException
    {
        final AttributeKey attributeKey = IdentityAttributeService.instance( ).getAttributeKey( attrStrKey );
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attrStrKey );
        attributeStatus.setStatus( AttributeChangeStatus.INVALID_VALUE );
        attributeStatus.setMessage( attributeKey.getValidationErrorMessage( ) );
        attributeStatus.setMessageKey( attributeKey.getValidationErrorMessageKey( ) );

        return attributeStatus;
    }

    /**
     * Validates the integrity of pivot attributes stored in the provided identity.<br/>
     * If a pivot attribute is present and certified with a process with a greater or equal level to the defined process in configuration, then ALL the pivot
     * attributes need to be present and certified with the same certification process.<br/>
     * If the birthcountry code attribute is set and doesn't correspond to FRANCE, then the birthplace code attribute is allowed to be missing.<br/>
     * If the above rules are not fullfiled, the response status is put to FAILURE.
     *
     * @param existingIdentityDto
     *            the existing identity in case of update, <code>null</code> otherwise
     * @param identity
     *            the identity
     * @param geocodesCheck
     *            if checks geocode or not
     * @throws RequestFormatException
     */
    public void validatePivotAttributesIntegrity( final IdentityDto existingIdentityDto, final IdentityDto identity, boolean geocodesCheck )
            throws RequestFormatException
    {
        // get pivot attributes
        final List<String> pivotKeys = IdentityAttributeService.instance( ).getPivotAttributeKeys( ).stream( ).map( AttributeKey::getKeyName )
                .collect( Collectors.toList( ) );

        // get attributes to update, and add certification levels
        final Map<String, AttributeDto> pivotUpdatedAttrs = identity.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                .peek( a -> a.setCertificationLevel( AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( a.getCertifier( ), a.getKey( ) ) ) )
                .collect( Collectors.toMap( AttributeDto::getKey, Function.identity( ) ) );

        // If the request does not contains at least one pivot attribute, we skip this validation rule
        if ( pivotUpdatedAttrs.isEmpty( ) )
        {
            return;
        }

        // in case of update, we get the existing attributes for the check
        final Map<String, AttributeDto> pivotExistingAttrs = new HashMap<>( );
        if ( existingIdentityDto != null )
        {
            pivotExistingAttrs.putAll( existingIdentityDto.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                    .collect( Collectors.toMap( AttributeDto::getKey, Function.identity( ) ) ) );
        }

        // *** Geocode checks ***

        // get birth date for Geocode checks
        final List<AttributeStatus> geocodeStatuses = new ArrayList<>( );
        Date birthdate = null;
        AttributeDto birthdateAttr = pivotUpdatedAttrs.get( Constants.PARAM_BIRTH_DATE );
        if ( birthdateAttr == null && existingIdentityDto != null )
        {
            birthdateAttr = pivotExistingAttrs.get( Constants.PARAM_BIRTH_DATE );
        }

        try
        {
            if ( birthdateAttr != null )
            {
                birthdate = DateUtils.parseDate( birthdateAttr.getValue( ), "dd/MM/yyyy" );
            }
        }
        catch( final ParseException e )
        {
            final AttributeStatus birthplaceCodeStatus = new AttributeStatus( );
            birthplaceCodeStatus.setKey( Constants.PARAM_BIRTH_DATE );
            birthplaceCodeStatus.setStatus( AttributeChangeStatus.INVALID_VALUE );
            birthplaceCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_UPDATED );
            geocodeStatuses.add( birthplaceCodeStatus );
        }

        if ( birthdate == null )
        {
            pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_DATE );

            // birthdate is mandatory for birthplace and birthcountry codes
            // we won't consider those values either (if present)
            pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
            pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_COUNTRY_CODE );

        }

        // Birthplace codes checks
        // If invalid, we consider them as missing
        if ( geocodesCheck && pivotUpdatedAttrs.containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) && birthdate != null )
        {
            final AttributeDto birthPlaceCodeAttr = pivotUpdatedAttrs.get( Constants.PARAM_BIRTH_PLACE_CODE );
            if ( StringUtils.isNotBlank( birthPlaceCodeAttr.getValue( ) ) )
            {
                final Optional<City> city = GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, birthPlaceCodeAttr.getValue( ) );
                if ( city == null || !city.isPresent( ) )
                {
                    pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
                    final AttributeStatus birthplaceCodeStatus = new AttributeStatus( );
                    birthplaceCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                    birthplaceCodeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    birthplaceCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                    geocodeStatuses.add( birthplaceCodeStatus );
                }
            }
        }

        if ( geocodesCheck && pivotUpdatedAttrs.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) && birthdate != null )
        {
            final AttributeDto birthcountryCodeAttr = pivotUpdatedAttrs.get( Constants.PARAM_BIRTH_COUNTRY_CODE );
            if ( StringUtils.isNotBlank( birthcountryCodeAttr.getValue( ) ) )
            {
                // TODO : use GeoCodesService.getInstance( ).getCountryByDateAndCode() when available ...
                final Optional<Country> country = GeoCodesService.getInstance( ).getCountryByCode( birthcountryCodeAttr.getValue( ) );
                if ( country == null || !country.isPresent( ) )
                {
                    pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_COUNTRY_CODE );
                    final AttributeStatus birthcountryCodeStatus = new AttributeStatus( );
                    birthcountryCodeStatus.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                    birthcountryCodeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    birthcountryCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                    geocodeStatuses.add( birthcountryCodeStatus );
                }
            }
        }

        // ** consolidate targeted list of pivot attributes with new or updated valid attributes, and existing attributes **

        final Map<String, AttributeDto> pivotTargetAttrs = new HashMap<>( );
        pivotTargetAttrs.putAll( pivotUpdatedAttrs );

        // in case of update, we include the existing attributes for the check
        if ( existingIdentityDto != null )
        {
            // Get the targeted pivot attributes from updated pivot attributes
            // and pivot attributes that exist and are not present in request.
            // If there is an existing attribute with a higher certification level, we ignore the request attribute
            pivotTargetAttrs.putAll( pivotExistingAttrs.keySet( ).stream( )
                    .filter( a -> ( !pivotUpdatedAttrs.containsKey( a )
                            || pivotUpdatedAttrs.get( a ).getCertificationLevel( ) < pivotExistingAttrs.get( a ).getCertificationLevel( ) ) )
                    .collect( Collectors.toMap( Function.identity( ), pivotExistingAttrs::get ) ) );
        }

        // if the birth country is not the main Geocode country, the birth place code is ignored
        if ( pivotTargetAttrs.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE )
                && !pivotTargetAttrs.get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( Constants.GEOCODE_MAIN_COUNTRY_CODE ) )
        {
            pivotKeys.remove( Constants.PARAM_BIRTH_PLACE_CODE );
            pivotUpdatedAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
            pivotTargetAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
        }

        // ** Main checks on target Attributes **

        // get highest certification level for pivot attributes (as reference)
        final AttributeDto highestCertifiedPivot = pivotTargetAttrs.values( ).stream( ).max( Comparator.comparing( AttributeDto::getCertificationLevel ) )
                .orElse( null );

        // if the highest certification level is below the treshold, we wont carry out the check
        if ( highestCertifiedPivot.getCertificationLevel( ) < pivotCertificationLevelThreshold )
        {
            return;
        }

        // check that we have the 6 pivot attributes (or 5 if birth country is not the geocode main country)
        if ( !( ( pivotTargetAttrs.size( ) == pivotKeys.size( ) ) || ( pivotTargetAttrs.size( ) >= pivotKeys.size( )
                && !Constants.GEOCODE_MAIN_COUNTRY_CODE.equals( pivotTargetAttrs.get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ) ) ) ) )
        {
            final RequestFormatException exception = new RequestFormatException(
                    "Above level " + pivotCertificationLevelThreshold + ", all pivot attributes must be present and have the same certification level.",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION );
            exception.getResponse( ).getStatus( ).setAttributeStatuses( geocodeStatuses );
            throw exception;
        }

        // we check that each pivot attribute has the same certification level
        if ( pivotTargetAttrs.values( ).stream( ).anyMatch( a -> !a.getCertifier( ).equals( highestCertifiedPivot.getCertifier( ) ) ) )
        {
            final RequestFormatException exception = new RequestFormatException(
                    "All pivot attributes must be set and certified with the '" + highestCertifiedPivot.getCertifier( ) + "' certifier",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION );
            exception.getResponse( ).getStatus( ).setAttributeStatuses( geocodeStatuses );
            throw exception;
        }

        // Check of empty values
        // (deletion of pivot attributes is not allowed above the pivotCertificationLevelThreshold)
        if ( pivotTargetAttrs.values( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getValue( ) ) ) )
        {
            final RequestFormatException exception = new RequestFormatException( "Deleting pivot attribute is forbidden for this identity.",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_FORBIDDEN_PIVOT_ATTRIBUTE_DELETION );
            exception.getResponse( ).getStatus( ).setAttributeStatuses( geocodeStatuses );
            throw exception;
        }

    }

    /**
     * Makes a bunch of checks regarding the validity of this update or merge request on this connected identity.
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
     * @param requestAttributes
     *            the attributes in the update or merge request
     * @param existingIdentityToUpdate
     *            the existing identity
     */
    public void checkConnectedIdentityUpdate( final List<AttributeDto> requestAttributes, final IdentityDto existingIdentityToUpdate )
            throws ClientAuthorizationException
    {
        final Map<String, AttributeDto> existingAttributes = existingIdentityToUpdate.getAttributes( ).stream( )
                .collect( Collectors.toMap( AttributeDto::getKey, Function.identity( ) ) );

        /* Récupération des attributs déja existants ou non */
        final Map<Boolean, List<AttributeDto>> sortedAttributes = requestAttributes.stream( )
                .collect( Collectors.partitioningBy( a -> existingAttributes.containsKey( a.getKey( ) ) ) );
        final List<AttributeDto> existingWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( true ) ) ? sortedAttributes.get( true )
                : new ArrayList<>( );
        final List<AttributeDto> newWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( false ) ) ? sortedAttributes.get( false )
                : new ArrayList<>( );

        final Map<String, AttributeKey> allAttributesByKey = IdentityAttributeService.instance( ).getAllAtributeKeys( ).stream( )
                .collect( Collectors.toMap( AttributeKey::getKeyName, Function.identity( ) ) );

        // - Authorize update on "PIVOT" attributes only
        final boolean requestOnNonPivot = requestAttributes.stream( ).map( a -> allAttributesByKey.get( a.getKey( ) ) ).anyMatch( a -> !a.getPivot( ) );
        if ( requestOnNonPivot )
        {
            throw new ClientAuthorizationException( "Identity is connected, updating non 'pivot' attributes is forbidden.",
                    Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_UPDATE_NON_PIVOT );
        }

        // - For new attributes, certification level must be > 100 (better than self-declare)
        final boolean newAttrSelfDeclare = newWritableAttributes.stream( )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) )
                .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) <= 100 );
        if ( newAttrSelfDeclare )
        {
            throw new ClientAuthorizationException( "Identity is connected, adding 'pivot' attributes with self-declarative certification level is forbidden.",
                    Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_SELF_DECLARE );
        }

        // - For existing attributes, certification level must be >= than the existing level
        final boolean lesserWantedLvl = existingWritableAttributes.stream( )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) ).anyMatch( wantedCertif -> {
                    final int wantedLvl = Integer.parseInt( wantedCertif.getRefCertificationLevel( ).getLevel( ) );
                    final AttributeDto existingAttr = existingAttributes.get( wantedCertif.getAttributeKey( ).getKeyName( ) );
                    final RefAttributeCertificationLevel existingCertif = RefAttributeCertificationLevelHome
                            .findByProcessusAndAttributeKeyName( existingAttr.getCertifier( ), existingAttr.getKey( ) );
                    final int existingLvl = Integer.parseInt( existingCertif.getRefCertificationLevel( ).getLevel( ) );

                    return wantedLvl < existingLvl;
                } );
        if ( lesserWantedLvl )
        {
            throw new ClientAuthorizationException( "Identity is connected, updating existing 'pivot' attributes with lesser certification level is forbidden.",
                    Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_UPDATE_PIVOT_LESSER_CERTIFICATION );
        }

        // - If one "PIVOT" attribute is certified at a certain level N (conf), all "PIVOT" attributes must be set and certified with level >= N.
        final int threshold = AppPropertiesService.getPropertyInt( PIVOT_CERTIF_LEVEL_THRESHOLD, 400 );
        final boolean breakingThreshold = existingIdentityToUpdate.getAttributes( ).stream( ).filter( a -> allAttributesByKey.get( a.getKey( ) ).getPivot( ) )
                .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) )
                .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) >= threshold )
                || requestAttributes.stream( )
                        .map( a -> RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( a.getCertifier( ), a.getKey( ) ) )
                        .anyMatch( c -> Integer.parseInt( c.getRefCertificationLevel( ).getLevel( ) ) >= threshold );
        if ( breakingThreshold )
        {
            // get all pivot attributes from database
            final List<String> pivotAttributeKeys = allAttributesByKey.values( ).stream( ).filter( AttributeKey::getPivot ).map( AttributeKey::getKeyName )
                    .collect( Collectors.toList( ) );

            // if any pivot is missing from request + existing -> unauthorized
            final Collection<String> unionOfExistingAndRequestedPivotKeys = CollectionUtils
                    .union( requestAttributes.stream( ).map( AttributeDto::getKey ).collect( Collectors.toSet( ) ), existingIdentityToUpdate.getAttributes( )
                            .stream( ).map( AttributeDto::getKey ).filter( key -> allAttributesByKey.get( key ).getPivot( ) ).collect( Collectors.toSet( ) ) );
            if ( !CollectionUtils.isEqualCollection( pivotAttributeKeys, unionOfExistingAndRequestedPivotKeys ) )
            {
                throw new ClientAuthorizationException(
                        "Identity is connected, and at least one 'pivot' attribute is, or has been requested to be, certified above level " + threshold
                                + ". In that case, all 'pivot' attributes must be set, and certified with level greater or equal to " + threshold + ".",
                        Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_CERTIFICATION_UNDER_THRESHOLD );
            }

            // if any has level lesser than threshold -> unauthorized
            final boolean lesserThanThreshold = pivotAttributeKeys.stream( ).map( key -> {
                final AttributeDto requested = requestAttributes.stream( ).filter( a -> a.getKey( ).equals( key ) ).findFirst( ).orElse( null );
                final AttributeDto existing = existingAttributes.get( key );
                int requestedLvl = 0;
                int existingLvl = 0;
                if ( requested != null )
                {
                    requestedLvl = Integer.parseInt( RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( requested.getCertifier( ), key )
                            .getRefCertificationLevel( ).getLevel( ) );
                }
                if ( existing != null )
                {
                    existingLvl = Integer.parseInt( RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( existing.getCertifier( ), key )
                            .getRefCertificationLevel( ).getLevel( ) );
                }
                return Math.max( requestedLvl, existingLvl );
            } ).anyMatch( lvl -> lvl < threshold );

            if ( lesserThanThreshold )
            {
                throw new ClientAuthorizationException(
                        "Identity is connected, and at least one 'pivot' attribute is, or has been requested to be, certified above level " + threshold
                                + ". In that case, all 'pivot' attributes must be set, and certified with level greater or equal to " + threshold + ".",
                        Constants.PROPERTY_REST_ERROR_CONNECTED_IDENTITY_FORBIDDEN_PIVOT_CERTIFICATION_UNDER_THRESHOLD );
            }
        }
    }

}

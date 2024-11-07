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
package fr.paris.lutece.plugins.identitystore.service.attribute;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidationService
{

    private final IdentityAttributeValidationCache _cache = SpringContextService.getBean( "identitystore.identityAttributeValidationCache" );
    private final int pivotCertificationLevelThreshold = AppPropertiesService
            .getPropertyInt( "identitystore.identity.attribute.pivot.certification.level.threshold", 400 );
    private static IdentityAttributeValidationService _instance;

    public static IdentityAttributeValidationService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeValidationService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    public boolean validateAttribute( final String key, final String value) throws IdentityAttributeNotFoundException {
        return _cache.get( key ).matcher( value ).matches( );
    }

    /**
     * Validates all attribute values stored in the provided identity, according to each attribute validation regex. Adds validation error statuses in the
     * response in case of invalid values, and put the status to FAILURE.
     *
     * @param identity
     *            the identity
     * @param response
     *            the response
     */
    public void validateIdentityAttributeValues( final IdentityDto identity, final ChangeResponse response ) throws IdentityAttributeNotFoundException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        if ( identity != null && CollectionUtils.isNotEmpty( identity.getAttributes( ) ) )
        {
            for ( final AttributeDto attribute : identity.getAttributes( ) )
            {
                if ( StringUtils.isNotBlank( attribute.getValue( ) ) )
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
            }
        }
        if ( !attrStatusList.isEmpty( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setAttributeStatuses( attrStatusList )
                    .setMessage( "Some attribute values are not passing validation. Please check in the attribute statuses for details." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_FAIL_ATTRIBUTE_VALIDATION ) );
        }
    }

    /**
     * Builds an attribute status for invalid value.
     *
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValueValidationErrorStatus( final String attrStrKey ) throws IdentityAttributeNotFoundException
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
     * @param cuid
     *            the cuid
     * @param identityRequest
     *            the identity request
     * @param response
     *            the response
     */
    public void validatePivotAttributesIntegrity( final IdentityDto existingIdentityDto, final String clientCode, final IdentityDto identity,
            boolean geocodesCheck, final ChangeResponse response ) throws IdentityStoreException
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
            response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION )
                    .setMessage( "Above level " + pivotCertificationLevelThreshold
                            + ", all pivot attributes must be present and have the same certification level." ) );
            response.getStatus( ).getAttributeStatuses( ).addAll( geocodeStatuses );
            return;
        }

        // we check that each pivot attribute has the same certification level
        if ( pivotTargetAttrs.values( ).stream( ).anyMatch( a -> !a.getCertifier( ).equals( highestCertifiedPivot.getCertifier( ) ) ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION )
                    .setMessage( "All pivot attributes must be set and certified with the '" + highestCertifiedPivot.getCertifier( ) + "' certifier" ) );
            response.getStatus( ).getAttributeStatuses( ).addAll( geocodeStatuses );
            return;
        }

        // Check of empty values
        // (deletion of pivot attributes is not allowed above the pivotCertificationLevelThreshold)

        if ( pivotTargetAttrs.values( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getValue( ) ) ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_FORBIDDEN_PIVOT_ATTRIBUTE_DELETION )
                    .setMessage( "Deleting pivot attribute is forbidden for this identity." ) );
            response.getStatus( ).getAttributeStatuses( ).addAll( geocodeStatuses );
        }

    }

}

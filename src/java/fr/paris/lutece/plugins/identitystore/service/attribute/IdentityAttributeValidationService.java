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

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeValidationCache;
import fr.paris.lutece.plugins.identitystore.cache.IdentityDtoCache;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class used to validate attribute values in requests
 */
public class IdentityAttributeValidationService
{
    private final IdentityDtoCache _identityDtoCache = SpringContextService.getBean( "identitystore.identityDtoCache" );
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
            _instance._identityDtoCache.refresh( );
        }
        return _instance;
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
        if ( identity != null )
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
    public void validatePivotAttributesIntegrity( final String cuid, final String clientCode, final IdentityDto identityRequest, final ChangeResponse response )
            throws IdentityStoreException
    {
        final List<String> pivotKeys = IdentityAttributeService.instance( ).getPivotAttributeKeys( ).stream( ).map( AttributeKey::getKeyName )
                .collect( Collectors.toList( ) );
        final Map<String, AttributeDto> pivotAttrs = identityRequest.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                .peek( a -> a.setCertificationLevel( AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( a.getCertifier( ), a.getKey( ) ) ) )
                .collect( Collectors.toMap( AttributeDto::getKey, Function.identity( ) ) );

        if ( pivotAttrs.isEmpty( ) )
        {
            // On ne fait le contrôle que si la requête contient au moins un attribut pivot
            return;
        }

        // Vérification des codes INSEE.
        // Si non vides et invalides, on considère qu'ils sont absents de la requète
        final List<AttributeStatus> inseeCodeStatuses = new ArrayList<>( );
        if ( pivotAttrs.containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) )
        {
            final AttributeDto birthPlaceCodeAttr = pivotAttrs.get( Constants.PARAM_BIRTH_PLACE_CODE );
            if ( StringUtils.isNotBlank( birthPlaceCodeAttr.getValue( ) ) )
            {
                final Optional<City> city = GeoCodesService.getInstance( ).getCityByDateAndCode( new Date( ), birthPlaceCodeAttr.getValue( ) );
                if ( city == null || !city.isPresent( ) )
                {
                    pivotAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
                    final AttributeStatus birthplaceCodeStatus = new AttributeStatus( );
                    birthplaceCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                    birthplaceCodeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    birthplaceCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                    inseeCodeStatuses.add( birthplaceCodeStatus );
                }
            }
        }
        if ( pivotAttrs.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
        {
            final AttributeDto birthcountryCodeAttr = pivotAttrs.get( Constants.PARAM_BIRTH_COUNTRY_CODE );
            if ( StringUtils.isNotBlank( birthcountryCodeAttr.getValue( ) ) )
            {
                final Optional<Country> country = GeoCodesService.getInstance( ).getCountryByCode( birthcountryCodeAttr.getValue( ) );
                if ( country == null || !country.isPresent( ) )
                {
                    pivotAttrs.remove( Constants.PARAM_BIRTH_COUNTRY_CODE );
                    final AttributeStatus birthcountryCodeStatus = new AttributeStatus( );
                    birthcountryCodeStatus.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                    birthcountryCodeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                    birthcountryCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                    inseeCodeStatuses.add( birthcountryCodeStatus );
                }
            }
        }

        if ( StringUtils.isNotBlank( cuid ) )
        {
            final IdentityDto existingIdentityDto = _identityDtoCache.getByCustomerId( cuid,
                    ServiceContractService.instance( ).getActiveServiceContract( clientCode ) );
            final List<AttributeDto> existingPivotAttrs = existingIdentityDto.getAttributes( ).stream( ).filter( a -> pivotKeys.contains( a.getKey( ) ) )
                    .collect( Collectors.toList( ) );
            for ( final AttributeDto existingPivotAttr : existingPivotAttrs )
            {
                // On prend en priorité l'attribut qui vient de la requête, sauf si l'attribut existant possède un niveau de certif + élevé
                final AttributeDto requestAttr = pivotAttrs.get( existingPivotAttr.getKey( ) );
                if ( requestAttr == null || requestAttr.getCertificationLevel( ) < existingPivotAttr.getCertificationLevel( ) )
                {
                    pivotAttrs.put( existingPivotAttr.getKey( ), existingPivotAttr );
                }
            }
        }

        if ( pivotAttrs.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE )
                && !pivotAttrs.get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( "99100" ) )
        {
            // Si on est sur un pays étranger, l'attribut "birthplace_code" n'est pas un pivot.
            pivotKeys.remove( Constants.PARAM_BIRTH_PLACE_CODE );
            pivotAttrs.remove( Constants.PARAM_BIRTH_PLACE_CODE );
        }
        final AttributeDto highestCertifiedPivot = pivotAttrs.values( ).stream( ).max( Comparator.comparing( AttributeDto::getCertificationLevel ) )
                .orElse( null );
        if ( highestCertifiedPivot != null && highestCertifiedPivot.getCertificationLevel( ) >= pivotCertificationLevelThreshold )
        {
            // Si le pivot avec la plus haute certification dépasse le niveau défini en properties, on exige que tous les pivots soient présents et avec la même
            // certification
            if ( pivotKeys.size( ) != pivotAttrs.size( )
                    || pivotAttrs.values( ).stream( ).anyMatch( a -> !a.getCertifier( ).equals( highestCertifiedPivot.getCertifier( ) ) ) )
            {
                response.setStatus( ResponseStatusFactory.failure( )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION )
                        .setMessage( "All pivot attributes must be set and certified with the '" + highestCertifiedPivot.getCertifier( ) + "' certifier" ) );
                response.getStatus( ).getAttributeStatuses( ).addAll( inseeCodeStatuses );
                return;
            }
            // On vérifie qu'aucun des attributs pivots envoyé n'a de valeur vide
            // Si c'est le cas, on rejete (suppression non autorisée)
            if ( pivotAttrs.values( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getValue( ) ) ) )
            {
                response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_IDENTITY_FORBIDDEN_PIVOT_ATTRIBUTE_DELETION )
                        .setMessage( "Deleting pivot attribute is forbidden for this identity." ) );
                response.getStatus( ).getAttributeStatuses( ).addAll( inseeCodeStatuses );
            }
        }
    }

}

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
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class IdentityAttributeGeocodesAdjustmentService
{

    private static final String FRANCE_COUNTRY_CODE = "99110";

    private static IdentityAttributeGeocodesAdjustmentService instance;

    public static IdentityAttributeGeocodesAdjustmentService instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityAttributeGeocodesAdjustmentService( );
        }
        return instance;
    }

    private IdentityAttributeGeocodesAdjustmentService( )
    {
    }

    public List<AttributeStatus> adjustGeocodesAttributes( final IdentityChangeRequest request )
    {
        return adjustGeocodesAttributes( request, null );
    }

    public List<AttributeStatus> adjustGeocodesAttributes( final IdentityChangeRequest request, final IdentityDto existingIdentityToUpdate )
    {
        final List<AttributeStatus> statuses = this.adjustCountry( request );
        final AttributeDto requestCountryCode = request.getIdentity( ).getAttributes( ).stream( )
                .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( ).orElse( null );
        final AttributeDto existingCountryCode = existingIdentityToUpdate == null ? null
                : existingIdentityToUpdate.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                        .orElse( null );
        final String countryCode = requestCountryCode != null ? requestCountryCode.getValue( )
                : ( existingCountryCode != null ? existingCountryCode.getValue( ) : null );

        // If no birthcountry, assume that the city is french
        if ( StringUtils.isBlank( countryCode ) || FRANCE_COUNTRY_CODE.equalsIgnoreCase( countryCode ) )
        {
            statuses.addAll( adjustFrenchCity( request ) );
        }
        else
        {
            statuses.addAll( adjustForeignCity( request ) );
        }

        if ( existingIdentityToUpdate != null )
        {
            // Check if there is a country change
            if ( existingCountryCode != null && requestCountryCode != null
                    && !Objects.equals( existingCountryCode.getValue( ), requestCountryCode.getValue( ) ) )
            {
                final AttributeDto existingBirthplace = existingIdentityToUpdate.getAttributes( ).stream( )
                        .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
                final AttributeDto requestBirthplace = request.getIdentity( ).getAttributes( ).stream( )
                        .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
                if ( existingBirthplace != null && requestBirthplace == null )
                {
                    // If there is an existing birthplace and none in the request, we add a blank birthplace to the request for it to be deleted
                    existingBirthplace.setValue( "" );
                    request.getIdentity( ).getAttributes( ).add( existingBirthplace );
                }
                if ( existingCountryCode.getValue( ).equals( "99110" ) )
                {
                    // Change from France to a foreign country
                    final AttributeDto existingBirthplaceCode = existingIdentityToUpdate.getAttributes( ).stream( )
                            .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( ).orElse( null );
                    final AttributeDto requestBirthplaceCode = request.getIdentity( ).getAttributes( ).stream( )
                            .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( ).orElse( null );
                    if ( existingBirthplaceCode != null )
                    {
                        // If there is an existing birthplace code, we tweak the request for it to be deleted
                        if ( requestBirthplaceCode != null )
                        {
                            request.getIdentity( ).getAttributes( ).remove( requestBirthplaceCode );
                        }
                        existingBirthplaceCode.setValue( "" );
                        request.getIdentity( ).getAttributes( ).add( existingBirthplaceCode );
                    }
                }
            }
        }

        return statuses;
    }

    private List<AttributeStatus> adjustCountry( final IdentityChangeRequest request )
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        AttributeDto sentCountryCode = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                .findFirst( ).orElse( null );
        AttributeDto sentCountryLabel = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) )
                .findFirst( ).orElse( null );
        // Country code was sent
        if ( sentCountryCode != null )
        {
            final Country country = GeoCodesService.getInstance( ).getCountryByCode( sentCountryCode.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code : discard attribute and notify with an AttributeStatus
                request.getIdentity( ).getAttributes( ).remove( sentCountryCode );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( sentCountryCode.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code - Adjust country label attribute if needed
                final String countryGeocodesLabel = country.getValue( );

                // If sent label is different than the Geocodes label, modify the request to override with the Geocodes value
                if ( sentCountryLabel != null && !sentCountryLabel.getValue( ).equals( countryGeocodesLabel ) )
                {
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( sentCountryLabel.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                    attrStatusList.add( attributeStatus );

                    request.getIdentity( ).getAttributes( ).remove( sentCountryLabel );
                    sentCountryLabel.setValue( countryGeocodesLabel );
                    request.getIdentity( ).getAttributes( ).add( sentCountryLabel );
                }
                // If no country label was sent, we add the attribute to the request with the Geocodes value, and same certifier as the sent code
                if ( sentCountryLabel == null )
                {
                    sentCountryLabel = new AttributeDto( );
                    sentCountryLabel.setKey( Constants.PARAM_BIRTH_COUNTRY );
                    sentCountryLabel.setValue( countryGeocodesLabel );
                    sentCountryLabel.setCertifier( sentCountryCode.getCertifier( ) );
                    sentCountryLabel.setCertificationDate( sentCountryCode.getCertificationDate( ) );
                    request.getIdentity( ).getAttributes( ).add( sentCountryLabel );
                }
            }
        }
        // No country code sent
        else
        {
            // Country label was sent
            if ( sentCountryLabel != null )
            {
                Date birthdate;
                try
                {
                    final AttributeDto sentBirthdate = request.getIdentity( ).getAttributes( ).stream( )
                            .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( ).orElse( null );
                    birthdate = sentBirthdate != null ? DateUtils.parseDate( sentBirthdate.getValue( ), "dd/MM/yyyy" ) : null;
                }
                catch( final ParseException e )
                {
                    birthdate = null;
                }

                final List<Country> countries = GeoCodesService.getInstance( ).getCountriesListByName( sentCountryLabel.getValue( ), birthdate );
                if ( CollectionUtils.isEmpty( countries ) )
                {
                    // Country doesn't exist in Geocodes for provided label : discard attribute and notify with an AttributeStatus
                    request.getIdentity( ).getAttributes( ).remove( sentCountryLabel );

                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( sentCountryLabel.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                    attrStatusList.add( attributeStatus );
                }
                else
                    if ( countries.size( ) > 1 )
                    {
                        // Multiple countries exist in Geocodes for provided label : discard attribute and notify with an AttributeStatus
                        request.getIdentity( ).getAttributes( ).remove( sentCountryLabel );

                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( sentCountryLabel.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // One country exists in Geocodes for provided label : add the code attribute to the request with the Geocodes code value, and same
                        // certifier as the sent label
                        final String countryGeocodesCode = countries.get( 0 ).getCode( );

                        sentCountryCode = new AttributeDto( );
                        sentCountryCode.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                        sentCountryCode.setValue( countryGeocodesCode );
                        sentCountryCode.setCertifier( sentCountryLabel.getCertifier( ) );
                        sentCountryCode.setCertificationDate( sentCountryLabel.getCertificationDate( ) );

                        request.getIdentity( ).getAttributes( ).add( sentCountryCode );
                    }
            }
        }
        return attrStatusList;
    }

    private List<AttributeStatus> adjustFrenchCity( final IdentityChangeRequest request )
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        AttributeDto sentCityCode = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) )
                .findFirst( ).orElse( null );
        AttributeDto sentCityLabel = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) )
                .findFirst( ).orElse( null );

        Date birthdate;
        try
        {
            final AttributeDto sentBirthdate = request.getIdentity( ).getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) )
                    .findFirst( ).orElse( null );
            birthdate = sentBirthdate != null ? DateUtils.parseDate( sentBirthdate.getValue( ), "dd/MM/yyyy" ) : null;
        }
        catch( final ParseException e )
        {
            birthdate = null;
        }

        // City code was sent
        if ( sentCityCode != null )
        {
            final City city = birthdate != null ? GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, sentCityCode.getValue( ) ).orElse( null )
                    : GeoCodesService.getInstance( ).getCityByCode( sentCityCode.getValue( ) ).orElse( null );
            if ( city == null && ( sentCityCode.getCertificationLevel( ) == null || sentCityCode.getCertificationLevel( ) < 600 ) )
            {
                // city doesn't exist in Geocodes for provided code, and code is not FC certified : discard attribute and notify with an AttributeStatus
                request.getIdentity( ).getAttributes( ).remove( sentCityCode );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( sentCityCode.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code, or provided code is FC certified - adjust city label attribute if needed
                final String cityGeocodesLabel = city != null ? city.getValue( ) : "commune inconnue";

                // If sent label is different than the Geocodes label, modify the request to override with the Geocodes value
                if ( sentCityLabel != null && !cityGeocodesLabel.equals( sentCityLabel.getValue( ) ) )
                {
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( sentCityLabel.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                    attrStatusList.add( attributeStatus );

                    request.getIdentity( ).getAttributes( ).remove( sentCityLabel );
                    sentCityLabel.setValue( cityGeocodesLabel );
                    request.getIdentity( ).getAttributes( ).add( sentCityLabel );
                }
                // If no country label was sent, we add the attribute to the request with the Geocodes value, and same certifier as the sent code
                if ( sentCityLabel == null )
                {
                    sentCityLabel = new AttributeDto( );
                    sentCityLabel.setKey( Constants.PARAM_BIRTH_COUNTRY );
                    sentCityLabel.setValue( cityGeocodesLabel );
                    sentCityLabel.setCertifier( sentCityCode.getCertifier( ) );
                    sentCityLabel.setCertificationDate( sentCityCode.getCertificationDate( ) );
                    request.getIdentity( ).getAttributes( ).add( sentCityLabel );
                }
            }
        }
        // No city code sent
        else
        {
            // City label was sent
            if ( sentCityLabel != null )
            {
                final List<City> cities = birthdate != null ? GeoCodesService.getInstance( ).getCitiesListByNameAndDate( sentCityLabel.getValue( ), birthdate )
                        : GeoCodesService.getInstance( ).getCitiesListByName( sentCityLabel.getValue( ) );
                if ( CollectionUtils.isEmpty( cities ) )
                {
                    // city doesn't exist in Geocodes for provided label : discard attribute and notify with an AttributeStatus
                    request.getIdentity( ).getAttributes( ).remove( sentCityLabel );

                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( sentCityLabel.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                    attrStatusList.add( attributeStatus );
                }
                else
                    if ( cities.size( ) > 1 )
                    {
                        // Multiple cities exist in Geocodes for provided label : discard attribute and notify with an AttributeStatus
                        request.getIdentity( ).getAttributes( ).remove( sentCityLabel );

                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( sentCityLabel.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // One city exists in Geocodes for provided label - add city code attribute to request
                        final String countryGeocodesCode = cities.get( 0 ).getCode( );

                        // create city code attribute
                        sentCityCode = new AttributeDto( );
                        sentCityCode.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                        sentCityCode.setValue( countryGeocodesCode );
                        sentCityCode.setCertifier( sentCityLabel.getCertifier( ) );
                        sentCityCode.setCertificationDate( sentCityLabel.getCertificationDate( ) );

                        request.getIdentity( ).getAttributes( ).add( sentCityCode );
                    }
            }
        }
        return attrStatusList;
    }

    private List<AttributeStatus> adjustForeignCity( final IdentityChangeRequest request )
    {
        {
            final List<AttributeStatus> attrStatusList = new ArrayList<>( );
            final AttributeDto sentCityCode = request.getIdentity( ).getAttributes( ).stream( )
                    .filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( ).orElse( null );
            if ( sentCityCode != null && StringUtils.isNotBlank( sentCityCode.getValue( ) ) )
            {
                // City code is not supported for foreign countries : if city code is sent for a foreign country, discard the attribute
                request.getIdentity( ).getAttributes( ).remove( sentCityCode );

                final AttributeStatus cityCodeStatus = new AttributeStatus( );
                cityCodeStatus.setStatus( AttributeChangeStatus.NOT_CREATED );
                cityCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_CREATED );
                cityCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                attrStatusList.add( cityCodeStatus );
            }

            // No adjuments are made on the city label for foreign country : if it was sent, it will be created like this

            return attrStatusList;
        }
    }

}

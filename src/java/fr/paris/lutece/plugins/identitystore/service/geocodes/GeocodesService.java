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
package fr.paris.lutece.plugins.identitystore.service.geocodes;

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.service.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeocodesService
{

    private final static IdentityAttributeService _identityAttributeService = IdentityAttributeService.instance( );

    public static List<AttributeStatus> processCountryAndCityForCreate( final Identity identity, final List<AttributeDto> attrToCreate,
            final String clientCode ) throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = processCountryForCreate( identity, attrToCreate, clientCode );
        final IdentityAttribute birthCountryCode = identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE );
        // Si l'identité n'a pas de pays de naissance renseigné, on traite la ville de naissance comme étant française
        if ( birthCountryCode == null || "99100".equalsIgnoreCase( birthCountryCode.getValue( ) ) )
        {
            attrStatusList.addAll( processFrenchCityForCreate( identity, attrToCreate, clientCode ) );
        }
        else
        {
            attrStatusList.addAll( processForeignCityForCreate( identity, attrToCreate, clientCode ) );
        }
        return attrStatusList;
    }

    public static List<AttributeStatus> processCountryAndCityForUpdate( final Identity identity, final List<AttributeDto> attrToCreate,
            final List<AttributeDto> attrToUpdate, final String clientCode ) throws IdentityStoreException
    {
        final String previousCountryCode = Optional.ofNullable(identity.getAttributes().get(Constants.PARAM_BIRTH_COUNTRY_CODE)).map(IdentityAttribute::getValue).orElse(null);
        final String newCountryCode = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( ).map(AttributeDto::getValue).orElse( null );

        final List<AttributeStatus> attrStatusList = processCountryForUpdate( identity, attrToCreate, attrToUpdate, clientCode );

        final AttributeStatus countryCodeStatus = attrStatusList.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );

        if ( countryCodeStatus != null && countryCodeStatus.getStatus( ) == AttributeChangeStatus.UPDATED && previousCountryCode != null
                && newCountryCode != null && !previousCountryCode.equals( newCountryCode ) )
        {
            // Si l'identité a changé de pays
            final IdentityAttribute existingBirthplace = identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE );
            final AttributeDto newBirthplace = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );

            if ( existingBirthplace != null && ( newBirthplace == null || newBirthplace.getValue().equals( existingBirthplace.getValue( ) ) ) )
            {
                // Suppression du libellé de commune de naissance
                IdentityAttributeHome.remove( identity.getId( ), existingBirthplace.getAttributeKey( ).getId( ) );
                identity.getAttributes( ).remove( Constants.PARAM_BIRTH_PLACE );
                final AttributeStatus removedBirthplace = new AttributeStatus( );
                removedBirthplace.setKey( Constants.PARAM_BIRTH_PLACE );
                removedBirthplace.setStatus( AttributeChangeStatus.REMOVED );
                removedBirthplace.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_REMOVED );
                attrStatusList.add( removedBirthplace );
            }
            if ( previousCountryCode.equals( "99100" ) )
            {
                // Passage de France à Etranger
                final IdentityAttribute existingBirthplaceCode = identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE );
                if ( existingBirthplaceCode != null )
                {
                    // Suppression du code commune de naissance
                    IdentityAttributeHome.remove( identity.getId( ), existingBirthplaceCode.getAttributeKey( ).getId( ) );
                    identity.getAttributes( ).remove( Constants.PARAM_BIRTH_PLACE_CODE );
                    final AttributeStatus removedBirthplaceCode = new AttributeStatus( );
                    removedBirthplaceCode.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                    removedBirthplaceCode.setStatus( AttributeChangeStatus.REMOVED );
                    removedBirthplaceCode.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_REMOVED );
                    attrStatusList.add( removedBirthplaceCode );
                }
            }
        }

        final IdentityAttribute birthCountryCode = identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE );

        // Si l'identité n'a pas de pays de naissance renseigné, on traite la ville de naissance comme étant française
        if ( birthCountryCode == null || "99100".equalsIgnoreCase( birthCountryCode.getValue( ) ) )
        {
            attrStatusList.addAll( processFrenchCityForUpdate( identity, attrToCreate, attrToUpdate, clientCode ) );
        }
        else
        {
            attrStatusList.addAll( processForeignCityForUpdate( identity, attrToCreate, attrToUpdate, clientCode ) );
        }
        return attrStatusList;
    }

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @throws IdentityAttributeNotFoundException
     */
    private static List<AttributeStatus> processCountryForUpdate( final Identity identity, final List<AttributeDto> attrToCreate,
            final List<AttributeDto> attrToUpdate, final String clientCode ) throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        final AttributeDto countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        AttributeDto countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }
        final AttributeDto countryCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToUpdate != null )
        {
            attrToUpdate.remove( countryCodeToUpdate );
        }
        AttributeDto countryLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToUpdate != null )
        {
            attrToUpdate.remove( countryLabelToUpdate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getInstance( ).getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                attrStatusList.add( codeStatus );

                // create country label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY ) )
                {
                    final AttributeChangeStatus labelStatus;
                    final String LabelStatusMessageKey;
                    if ( countryLabelToCreate == null || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) )
                    {
                        labelStatus = AttributeChangeStatus.CREATED;
                        LabelStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                    }
                    else
                    {
                        labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                        LabelStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                    }
                    if ( countryLabelToCreate == null )
                    {
                        countryLabelToCreate = new AttributeDto( );
                        countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                    }
                    countryLabelToCreate.setValue( countryGeocodesLabel );
                    countryLabelToCreate.setCertifier( countryCodeToCreate.getCertifier( ) );
                    countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    attributeStatus.setMessageKey( LabelStatusMessageKey );
                    attrStatusList.add( attributeStatus );
                }
                // update country label if attribute exists, and value or certif is different from existing
                else
                {
                    final IdentityAttribute existingBirthcountryAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY );
                    if ( !existingBirthcountryAttr.getValue( ).equals( countryGeocodesLabel )
                            || !existingBirthcountryAttr.getCertificate( ).getCertifierCode( ).equals( countryCodeToCreate.getCertifier( ) ) )
                    {
                        final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                        if ( countryLabelToUpdate == null )
                        {
                            countryLabelToUpdate = new AttributeDto( );
                            countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                        }
                        countryLabelToUpdate.setValue( countryGeocodesLabel );
                        countryLabelToUpdate.setCertifier( countryCodeToCreate.getCertifier( ) );
                        countryLabelToUpdate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                        }
                        attrStatusList.add( attributeStatus );
                    }
                }
            }
        }
        // Country code to UPDATE
        else
            if ( countryCodeToUpdate != null )
            {
                if ( StringUtils.isBlank( countryCodeToUpdate.getValue( ) ) )
                {
                    // Remove code & label attributes
                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToUpdate, identity, clientCode );
                    attrStatusList.add( codeStatus );
                    if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY ) != null )
                    {
                        if ( countryLabelToUpdate == null )
                        {
                            countryLabelToUpdate = new AttributeDto( );
                            countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                        }
                        countryLabelToUpdate.setValue( "" );
                        countryLabelToUpdate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                        countryLabelToUpdate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );
                        final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                        attrStatusList.add( labelStatus );
                    }
                }
                else
                {
                    final Country country = GeoCodesService.getInstance( ).getCountryByCode( countryCodeToUpdate.getValue( ) ).orElse( null );
                    if ( country == null )
                    {
                        // Country doesn't exist in Geocodes for provided code
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryCodeToUpdate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // Country exists in Geocodes for provided code
                        // update country code attribute
                        final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToUpdate, identity, clientCode );
                        attrStatusList.add( codeStatus );

                        // create country label attribute if it doesn't already exist in the identity
                        // Geocodes label value is used, regardless if a label is provided or not
                        final String countryGeocodesLabel = country.getValue( );
                        if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY ) )
                        {
                            final AttributeChangeStatus labelStatus;
                            final String LabelStatusMessageKey;
                            if ( countryLabelToCreate == null || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) )
                            {
                                labelStatus = AttributeChangeStatus.CREATED;
                                LabelStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                            }
                            else
                            {
                                labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                                LabelStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                            }
                            if ( countryLabelToCreate == null )
                            {
                                countryLabelToCreate = new AttributeDto( );
                                countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                            }
                            countryLabelToCreate.setValue( countryGeocodesLabel );
                            countryLabelToCreate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                            countryLabelToCreate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                            attributeStatus.setStatus( labelStatus );
                            attributeStatus.setMessageKey( LabelStatusMessageKey );
                            attrStatusList.add( attributeStatus );
                        }
                        // update country label if attribute exists, and value or certif is different from existing
                        else
                        {
                            final IdentityAttribute existingBirthCountryAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY );
                            if ( !existingBirthCountryAttr.getValue( ).equals( countryGeocodesLabel )
                                    || !existingBirthCountryAttr.getCertificate( ).getCertifierCode( ).equals( countryCodeToUpdate.getCertifier( ) ) )
                            {
                                final boolean override = ( countryLabelToUpdate != null && !countryLabelToUpdate.getValue( ).equals( countryGeocodesLabel ) );
                                if ( countryLabelToUpdate == null )
                                {
                                    countryLabelToUpdate = new AttributeDto( );
                                    countryLabelToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                                }
                                countryLabelToUpdate.setValue( countryGeocodesLabel );
                                countryLabelToUpdate.setCertifier( countryCodeToUpdate.getCertifier( ) );
                                countryLabelToUpdate.setCertificationDate( countryCodeToUpdate.getCertificationDate( ) );

                                final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                                if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                                {
                                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                                }
                                attrStatusList.add( attributeStatus );
                            }
                        }
                    }
                }
            }
            // No country code sent, checking if label was sent
            else
            {
                // get birth date
                Date birthdate;
                final IdentityAttribute birthdateAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_DATE );
                try
                {
                    if ( birthdateAttr == null )
                    {
                        final AttributeDto bdToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( )
                                .orElse( null );
                        birthdate = bdToCreate != null ? DateUtils.parseDate( bdToCreate.getValue( ), "dd/MM/yyyy" ) : null;
                    }
                    else
                    {
                        final AttributeDto bdToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( )
                                .orElse( null );
                        birthdate = DateUtils.parseDate( bdToUpdate != null ? bdToUpdate.getValue( ) : birthdateAttr.getValue( ), "dd/MM/yyyy" );
                    }
                }
                catch( final ParseException e )
                {
                    birthdate = null;
                }

                if ( countryLabelToCreate != null )
                {

                    final List<Country> countries = GeoCodesService.getInstance( ).getCountriesListByName( countryLabelToCreate.getValue( ), birthdate );
                    if ( CollectionUtils.isEmpty( countries ) )
                    {
                        // Country doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                        if ( countries.size( ) > 1 )
                        {
                            // Multiple countries exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                            attrStatusList.add( attributeStatus );
                        }
                        else
                        {
                            // One country exists in Geocodes for provided label
                            // Create country label attribute
                            final AttributeStatus labelStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                            attrStatusList.add( labelStatus );

                            // create country code attribute if it doesn't already exist in the identity
                            final String countryGeocodesCode = countries.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                            {
                                final AttributeDto codeToCreate = new AttributeDto( );
                                codeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                codeToCreate.setValue( countryGeocodesCode );
                                codeToCreate.setCertifier( countryLabelToCreate.getCertifier( ) );
                                codeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                attrStatusList.add( codeStatus );
                            }
                            // update country code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                {
                                    final AttributeDto codeToUpdate = new AttributeDto( );
                                    codeToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                    codeToUpdate.setValue( countryGeocodesCode );
                                    codeToUpdate.setCertifier( countryLabelToCreate.getCertifier( ) );
                                    codeToUpdate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                    attrStatusList.add( codeStatus );
                                }
                        }
                }
                else
                    if ( countryLabelToUpdate != null )
                    {
                        if ( StringUtils.isBlank( countryLabelToUpdate.getValue( ) ) )
                        {
                            // Attempt to remove attribute
                            final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                            attrStatusList.add( labelStatus );
                            if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ) != null )
                            {
                                final AttributeDto countryCodeToDelete = new AttributeDto( );
                                countryCodeToDelete.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                countryCodeToDelete.setValue( "" );
                                countryCodeToDelete.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                countryCodeToDelete.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );
                                final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( countryCodeToDelete, identity, clientCode );
                                attrStatusList.add( codeStatus );
                            }
                        }
                        else
                        {
                            final List<Country> countries = GeoCodesService.getInstance( ).getCountriesListByName( countryLabelToUpdate.getValue( ),
                                    birthdate );
                            if ( CollectionUtils.isEmpty( countries ) )
                            {
                                // Country doesn't exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                                attrStatusList.add( attributeStatus );
                            }
                            else
                            {
                                if ( countries.size( ) > 1 )
                                {
                                    // Multiple countries exist in Geocodes for provided label
                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( countryLabelToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                                    attrStatusList.add( attributeStatus );
                                }
                                else
                                {
                                    // One country exists in Geocodes for provided label
                                    // Update country label attribute
                                    final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( countryLabelToUpdate, identity, clientCode );
                                    attrStatusList.add( labelStatus );

                                    // create country code attribute if it doesn't already exist in the identity
                                    final String countryGeocodesCode = countries.get( 0 ).getCode( );
                                    if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) )
                                    {
                                        final AttributeDto codeToCreate = new AttributeDto( );
                                        codeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                        codeToCreate.setValue( countryGeocodesCode );
                                        codeToCreate.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                        codeToCreate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                        attrStatusList.add( codeStatus );
                                    }
                                    // update country code if attribute exists, and value is different from existing
                                    else
                                    {
                                        if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_COUNTRY_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                        {
                                            final AttributeDto codeToUpdate = new AttributeDto( );
                                            codeToUpdate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                                            codeToUpdate.setValue( countryGeocodesCode );
                                            codeToUpdate.setCertifier( countryLabelToUpdate.getCertifier( ) );
                                            codeToUpdate.setCertificationDate( countryLabelToUpdate.getCertificationDate( ) );

                                            final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                            attrStatusList.add( codeStatus );
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        return attrStatusList;
    }

    /**
     * Private methode used to process both "birthcountry_code" and "birthcountry" attributes during an create process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @throws IdentityAttributeNotFoundException
     */
    private static List<AttributeStatus> processCountryForCreate( final Identity identity, final List<AttributeDto> attrToCreate, final String clientCode )
            throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        AttributeDto countryCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).findFirst( )
                .orElse( null );
        if ( countryCodeToCreate != null )
        {
            attrToCreate.remove( countryCodeToCreate );
        }
        AttributeDto countryLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ).findFirst( )
                .orElse( null );
        if ( countryLabelToCreate != null )
        {
            attrToCreate.remove( countryLabelToCreate );
        }

        // Country code to CREATE
        if ( countryCodeToCreate != null )
        {
            final Country country = GeoCodesService.getInstance( ).getCountryByCode( countryCodeToCreate.getValue( ) ).orElse( null );
            if ( country == null )
            {
                // Country doesn't exist in Geocodes for provided code
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( countryCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // Country exists in Geocodes for provided code
                // create country code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                attrStatusList.add( codeStatus );

                // create country label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String countryGeocodesLabel = country.getValue( );
                final AttributeChangeStatus labelStatus;
                final String attrStatusMessageKey;
                if ( countryLabelToCreate == null || countryLabelToCreate.getValue( ).equals( countryGeocodesLabel ) )
                {
                    labelStatus = AttributeChangeStatus.CREATED;
                    attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                }
                else
                {
                    labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                }
                if ( countryLabelToCreate == null )
                {
                    countryLabelToCreate = new AttributeDto( );
                    countryLabelToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY );
                }
                countryLabelToCreate.setValue( countryGeocodesLabel );
                countryLabelToCreate.setCertifier( countryCodeToCreate.getCertifier( ) );
                countryLabelToCreate.setCertificationDate( countryCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                attributeStatus.setMessageKey( attrStatusMessageKey );
                attrStatusList.add( attributeStatus );
            }
        }
        // No country code sent, checking if label was sent
        else
        {
            if ( countryLabelToCreate != null )
            {
                Date birthdate;
                try
                {
                    final AttributeDto bdToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( )
                            .orElse( null );
                    birthdate = bdToCreate != null ? DateUtils.parseDate( bdToCreate.getValue( ), "dd/MM/yyyy" ) : null;
                }
                catch( final ParseException e )
                {
                    birthdate = null;
                }

                final List<Country> countries = GeoCodesService.getInstance( ).getCountriesListByName( countryLabelToCreate.getValue( ), birthdate );
                if ( CollectionUtils.isEmpty( countries ) )
                {
                    // Country doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                    attrStatusList.add( attributeStatus );
                }
                else
                    if ( countries.size( ) > 1 )
                    {
                        // Multiple countries exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( countryLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // One country exists in Geocodes for provided label
                        // Create country label attribute
                        final AttributeStatus labelStatus = _identityAttributeService.createAttribute( countryLabelToCreate, identity, clientCode );
                        attrStatusList.add( labelStatus );

                        // create country code attribute
                        final String countryGeocodesCode = countries.get( 0 ).getCode( );
                        countryCodeToCreate = new AttributeDto( );
                        countryCodeToCreate.setKey( Constants.PARAM_BIRTH_COUNTRY_CODE );
                        countryCodeToCreate.setValue( countryGeocodesCode );
                        countryCodeToCreate.setCertifier( countryLabelToCreate.getCertifier( ) );
                        countryCodeToCreate.setCertificationDate( countryLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( countryCodeToCreate, identity, clientCode );
                        attrStatusList.add( codeStatus );
                    }
            }
        }
        return attrStatusList;
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an create process of an identity when the birthcountry is
     * FRANCE.
     *
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @throws IdentityAttributeNotFoundException
     */
    private static List<AttributeStatus> processFrenchCityForCreate( final Identity identity, final List<AttributeDto> attrToCreate, final String clientCode )
            throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }

        Date birthdate;
        try
        {
            final AttributeDto bdToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( ).orElse( null );
            birthdate = bdToCreate != null ? DateUtils.parseDate( bdToCreate.getValue( ), "dd/MM/yyyy" ) : null;
        }
        catch( final ParseException e )
        {
            birthdate = null;
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = birthdate != null ? GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, cityCodeToCreate.getValue( ) ).orElse( null )
                    : GeoCodesService.getInstance( ).getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null && ( cityCodeToCreate.getCertificationLevel( ) == null || cityCodeToCreate.getCertificationLevel( ) < 600 ) )
            {
                // city doesn't exist in Geocodes for provided code, and code is not FC certified
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code, or provided code is FC certified
                // create city code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                attrStatusList.add( codeStatus );

                // create city label attribute
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city != null ? city.getValueMinComplete( ) : "commune inconnue";
                final AttributeChangeStatus labelStatus;
                final String attrStatusMessageKey;
                if ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                {
                    labelStatus = AttributeChangeStatus.CREATED;
                    attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                }
                else
                {
                    labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                    attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                }
                if ( cityLabelToCreate == null )
                {
                    cityLabelToCreate = new AttributeDto( );
                    cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                }
                cityLabelToCreate.setValue( cityGeocodesLabel );
                cityLabelToCreate.setCertifier( cityCodeToCreate.getCertifier( ) );
                cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                attributeStatus.setStatus( labelStatus );
                attributeStatus.setMessageKey( attrStatusMessageKey );
                attrStatusList.add( attributeStatus );
            }
        }
        // No city code sent, checking if label was sent
        else
        {
            if ( cityLabelToCreate != null )
            {
                final List<City> cities = birthdate != null
                        ? GeoCodesService.getInstance( ).getCitiesListByNameAndDate( cityLabelToCreate.getValue( ), birthdate )
                        : GeoCodesService.getInstance( ).getCitiesListByName( cityLabelToCreate.getValue( ) );
                if ( CollectionUtils.isEmpty( cities ) )
                {
                    // city doesn't exist in Geocodes for provided label
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                    attrStatusList.add( attributeStatus );
                }
                else
                    if ( cities.size( ) > 1 )
                    {
                        // Multiple cities exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // One city exists in Geocodes for provided label
                        // Create city label attribute
                        final AttributeStatus labelStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                        attrStatusList.add( labelStatus );

                        // create city code attribute
                        final String countryGeocodesCode = cities.get( 0 ).getCode( );
                        cityCodeToCreate = new AttributeDto( );
                        cityCodeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                        cityCodeToCreate.setValue( countryGeocodesCode );
                        cityCodeToCreate.setCertifier( cityLabelToCreate.getCertifier( ) );
                        cityCodeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                        attrStatusList.add( codeStatus );
                    }
            }
        }
        return attrStatusList;
    }

    /**
     * Private methode used to process both "birthplace_code" and "birthplace" attributes during an update process of an identity.
     *
     * @param identity
     * @param attrToCreate
     * @param attrToUpdate
     * @param clientCode
     * @throws IdentityAttributeNotFoundException
     */
    private static List<AttributeStatus> processFrenchCityForUpdate( final Identity identity, final List<AttributeDto> attrToCreate,
            final List<AttributeDto> attrToUpdate, final String clientCode ) throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        final AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
        }
        AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
        }
        final AttributeDto cityCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToUpdate != null )
        {
            attrToUpdate.remove( cityCodeToUpdate );
        }
        AttributeDto cityLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( ).orElse( null );
        if ( cityLabelToUpdate != null )
        {
            attrToUpdate.remove( cityLabelToUpdate );
        }

        Date birthdate;
        final IdentityAttribute birthdateAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_DATE );
        try
        {
            if ( birthdateAttr == null )
            {
                final AttributeDto bdToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( )
                        .orElse( null );
                birthdate = bdToCreate != null ? DateUtils.parseDate( bdToCreate.getValue( ), "dd/MM/yyyy" ) : null;
            }
            else
            {
                final AttributeDto bdToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( )
                        .orElse( null );
                birthdate = DateUtils.parseDate( bdToUpdate != null ? bdToUpdate.getValue( ) : birthdateAttr.getValue( ), "dd/MM/yyyy" );
            }
        }
        catch( final ParseException e )
        {
            birthdate = null;
        }

        // City code to CREATE
        if ( cityCodeToCreate != null )
        {
            final City city = birthdate != null ? GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, cityCodeToCreate.getValue( ) ).orElse( null )
                    : GeoCodesService.getInstance( ).getCityByCode( cityCodeToCreate.getValue( ) ).orElse( null );
            if ( city == null && ( cityCodeToCreate.getCertificationLevel( ) == null || cityCodeToCreate.getCertificationLevel( ) < 600 ) )
            {
                // city doesn't exist in Geocodes for provided code and provided code is not FC certified
                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( cityCodeToCreate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                attrStatusList.add( attributeStatus );
            }
            else
            {
                // city exists in Geocodes for provided code, or provided code is FC certified
                // create city code attribute
                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( cityCodeToCreate, identity, clientCode );
                attrStatusList.add( codeStatus );

                // create city label attribute if it doesn't already exist in the identity
                // Geocodes label value is used, regardless if a label is provided or not
                final String cityGeocodesLabel = city != null ? city.getValueMinComplete( ) : "commune inconnue";
                if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE ) )
                {
                    final AttributeChangeStatus labelStatus;
                    final String attrStatusMessageKey;
                    if ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                    {
                        labelStatus = AttributeChangeStatus.CREATED;
                        attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                    }
                    else
                    {
                        labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                        attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                    }
                    if ( cityLabelToCreate == null )
                    {
                        cityLabelToCreate = new AttributeDto( );
                        cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                    }
                    cityLabelToCreate.setValue( cityGeocodesLabel );
                    cityLabelToCreate.setCertifier( cityCodeToCreate.getCertifier( ) );
                    cityLabelToCreate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                    final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                    attributeStatus.setStatus( labelStatus );
                    attributeStatus.setMessageKey( attrStatusMessageKey );
                    attrStatusList.add( attributeStatus );
                }
                // update city label if attribute exists, and value or certif is different from existing
                else
                {
                    final IdentityAttribute existingBirthplaceAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE );
                    if ( !existingBirthplaceAttr.getValue( ).equals( cityGeocodesLabel )
                            || !existingBirthplaceAttr.getCertificate( ).getCertifierCode( ).equals( cityCodeToCreate.getCertifier( ) ) )
                    {
                        final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                        if ( cityLabelToUpdate == null )
                        {
                            cityLabelToUpdate = new AttributeDto( );
                            cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                        }
                        cityLabelToUpdate.setValue( cityGeocodesLabel );
                        cityLabelToUpdate.setCertifier( cityCodeToCreate.getCertifier( ) );
                        cityLabelToUpdate.setCertificationDate( cityCodeToCreate.getCertificationDate( ) );

                        final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                        if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                        {
                            attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                        }
                        attrStatusList.add( attributeStatus );
                    }
                }
            }
        }
        // city code to UPDATE
        else
            if ( cityCodeToUpdate != null )
            {
                if ( StringUtils.isBlank( cityCodeToUpdate.getValue( ) ) )
                {
                    // Remove code & label attributes
                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToUpdate, identity, clientCode );
                    attrStatusList.add( codeStatus );
                    if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE ) != null )
                    {
                        if ( cityLabelToUpdate == null )
                        {
                            cityLabelToUpdate = new AttributeDto( );
                            cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                        }
                        cityLabelToUpdate.setValue( "" );
                        cityLabelToUpdate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                        cityLabelToUpdate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );
                        final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                        attrStatusList.add( labelStatus );
                    }
                }
                else
                {
                    final City city = birthdate != null
                            ? GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, cityCodeToUpdate.getValue( ) ).orElse( null )
                            : GeoCodesService.getInstance( ).getCityByCode( cityCodeToUpdate.getValue( ) ).orElse( null );
                    if ( city == null && ( cityCodeToUpdate.getCertificationLevel( ) == null || cityCodeToUpdate.getCertificationLevel( ) < 600 ) )
                    {
                        // city doesn't exist in Geocodes for provided code and provided code is not FC certified
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityCodeToUpdate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_CODE );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_CODE );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                    {
                        // city exists in Geocodes for provided code or provided code is FC certified
                        // update city code attribute
                        final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToUpdate, identity, clientCode );
                        attrStatusList.add( codeStatus );

                        // create city label attribute if it doesn't already exist in the identity
                        // Geocodes label value is used, regardless if a label is provided or not
                        final String cityGeocodesLabel = city != null ? city.getValueMinComplete( ) : "commune inconnue";
                        if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE ) )
                        {
                            final AttributeChangeStatus labelStatus;
                            final String attrStatusMessageKey;
                            if ( cityLabelToCreate == null || cityLabelToCreate.getValue( ).equals( cityGeocodesLabel ) )
                            {
                                labelStatus = AttributeChangeStatus.CREATED;
                                attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED;
                            }
                            else
                            {
                                labelStatus = AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL;
                                attrStatusMessageKey = Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN;
                            }
                            if ( cityLabelToCreate == null )
                            {
                                cityLabelToCreate = new AttributeDto( );
                                cityLabelToCreate.setKey( Constants.PARAM_BIRTH_PLACE );
                            }
                            cityLabelToCreate.setValue( cityGeocodesLabel );
                            cityLabelToCreate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                            cityLabelToCreate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                            final AttributeStatus attributeStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                            attributeStatus.setStatus( labelStatus );
                            attributeStatus.setMessageKey( attrStatusMessageKey );
                            attrStatusList.add( attributeStatus );
                        }
                        // update city label if attribute exists, and value or certif is different from existing
                        else
                        {
                            final IdentityAttribute existingBirthplaceAttr = identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE );
                            if ( !existingBirthplaceAttr.getValue( ).equals( cityGeocodesLabel )
                                    || !existingBirthplaceAttr.getCertificate( ).getCertifierCode( ).equals( cityCodeToUpdate.getCertifier( ) ) )
                            {
                                final boolean override = ( cityLabelToUpdate != null && !cityLabelToUpdate.getValue( ).equals( cityGeocodesLabel ) );
                                if ( cityLabelToUpdate == null )
                                {
                                    cityLabelToUpdate = new AttributeDto( );
                                    cityLabelToUpdate.setKey( Constants.PARAM_BIRTH_PLACE );
                                }
                                cityLabelToUpdate.setValue( cityGeocodesLabel );
                                cityLabelToUpdate.setCertifier( cityCodeToUpdate.getCertifier( ) );
                                cityLabelToUpdate.setCertificationDate( cityCodeToUpdate.getCertificationDate( ) );

                                final AttributeStatus attributeStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                                if ( attributeStatus.getStatus( ).equals( AttributeChangeStatus.UPDATED ) && override )
                                {
                                    attributeStatus.setStatus( AttributeChangeStatus.OVERRIDDEN_GEOCODES_LABEL );
                                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_GEOCODES_LABEL_OVERRIDDEN );
                                }
                                attrStatusList.add( attributeStatus );
                            }
                        }
                    }
                }
            }
            // No city code sent, checking if label was sent
            else
            {
                if ( cityLabelToCreate != null )
                {
                    final List<City> cities = birthdate != null
                            ? GeoCodesService.getInstance( ).getCitiesListByNameAndDate( cityLabelToCreate.getValue( ), birthdate )
                            : GeoCodesService.getInstance( ).getCitiesListByName( cityLabelToCreate.getValue( ) );
                    if ( CollectionUtils.isEmpty( cities ) )
                    {
                        // city doesn't exist in Geocodes for provided label
                        final AttributeStatus attributeStatus = new AttributeStatus( );
                        attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                        attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                        attrStatusList.add( attributeStatus );
                    }
                    else
                        if ( cities.size( ) > 1 )
                        {
                            // Multiple cities exist in Geocodes for provided label
                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( cityLabelToCreate.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                            attrStatusList.add( attributeStatus );
                        }
                        else
                        {
                            // One city exists in Geocodes for provided label
                            // Create city label attribute
                            final AttributeStatus labelStatus = _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode );
                            attrStatusList.add( labelStatus );

                            // create city code attribute if it doesn't already exist in the identity
                            final String cityGeocodesCode = cities.get( 0 ).getCode( );
                            if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) )
                            {
                                final AttributeDto codeToCreate = new AttributeDto( );
                                codeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                codeToCreate.setValue( cityGeocodesCode );
                                codeToCreate.setCertifier( cityLabelToCreate.getCertifier( ) );
                                codeToCreate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                attrStatusList.add( codeStatus );
                            }
                            // update city code if attribute exists, and value is different from existing
                            else
                                if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ).getValue( ).equals( cityGeocodesCode ) )
                                {
                                    final AttributeDto codeToUpdate = new AttributeDto( );
                                    codeToUpdate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                    codeToUpdate.setValue( cityGeocodesCode );
                                    codeToUpdate.setCertifier( cityLabelToCreate.getCertifier( ) );
                                    codeToUpdate.setCertificationDate( cityLabelToCreate.getCertificationDate( ) );

                                    final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                    attrStatusList.add( codeStatus );
                                }
                        }
                }
                else
                    if ( cityLabelToUpdate != null )
                    {
                        if ( StringUtils.isBlank( cityLabelToUpdate.getValue( ) ) )
                        {
                            // Attempt to remove attribute
                            final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                            attrStatusList.add( labelStatus );
                            if ( identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ) != null )
                            {
                                final AttributeDto cityCodeToDelete = new AttributeDto( );
                                cityCodeToDelete.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                cityCodeToDelete.setValue( "" );
                                cityCodeToDelete.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                cityCodeToDelete.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );
                                final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( cityCodeToDelete, identity, clientCode );
                                attrStatusList.add( codeStatus );
                            }
                        }
                        else
                        {
                            final List<City> cities = birthdate != null
                                    ? GeoCodesService.getInstance( ).getCitiesListByNameAndDate( cityLabelToUpdate.getValue( ), birthdate )
                                    : GeoCodesService.getInstance( ).getCitiesListByName( cityLabelToUpdate.getValue( ) );
                            if ( CollectionUtils.isEmpty( cities ) )
                            {
                                // city doesn't exist in Geocodes for provided label
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.UNKNOWN_GEOCODES_LABEL );
                                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_UNKNOWN_GEOCODES_LABEL );
                                attrStatusList.add( attributeStatus );
                            }
                            else
                                if ( cities.size( ) > 1 )
                                {
                                    // Multiple cities exist in Geocodes for provided label
                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( cityLabelToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.MULTIPLE_GEOCODES_RESULTS_FOR_LABEL );
                                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_VALIDATION_ERROR_GEOCODES_LABEL_MULTIPLE_RESULTS );
                                    attrStatusList.add( attributeStatus );
                                }
                                else
                                {
                                    // One city exists in Geocodes for provided label
                                    // Update city label attribute
                                    final AttributeStatus labelStatus = _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode );
                                    attrStatusList.add( labelStatus );

                                    // create city code attribute if it doesn't already exist in the identity
                                    final String countryGeocodesCode = cities.get( 0 ).getCode( );
                                    if ( !identity.getAttributes( ).containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) )
                                    {
                                        final AttributeDto codeToCreate = new AttributeDto( );
                                        codeToCreate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                        codeToCreate.setValue( countryGeocodesCode );
                                        codeToCreate.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                        codeToCreate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                        final AttributeStatus codeStatus = _identityAttributeService.createAttribute( codeToCreate, identity, clientCode );
                                        attrStatusList.add( codeStatus );
                                    }
                                    // update city code if attribute exists, and value is different from existing
                                    else
                                        if ( !identity.getAttributes( ).get( Constants.PARAM_BIRTH_PLACE_CODE ).getValue( ).equals( countryGeocodesCode ) )
                                        {
                                            final AttributeDto codeToUpdate = new AttributeDto( );
                                            codeToUpdate.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                                            codeToUpdate.setValue( countryGeocodesCode );
                                            codeToUpdate.setCertifier( cityLabelToUpdate.getCertifier( ) );
                                            codeToUpdate.setCertificationDate( cityLabelToUpdate.getCertificationDate( ) );

                                            final AttributeStatus codeStatus = _identityAttributeService.updateAttribute( codeToUpdate, identity, clientCode );
                                            attrStatusList.add( codeStatus );
                                        }
                                }
                        }
                    }
            }
        return attrStatusList;
    }

    /**
     * Private method used to process both "birthplace_code" and "birthplace" attributes during an create process of an identity when the birthcountry is not
     * FRANCE.
     * 
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @throws IdentityStoreException
     */
    private static List<AttributeStatus> processForeignCityForCreate( final Identity identity, final List<AttributeDto> attrToCreate, final String clientCode )
            throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        final AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
            final AttributeStatus cityCodeStatus = new AttributeStatus( );
            cityCodeStatus.setStatus( AttributeChangeStatus.NOT_CREATED );
            cityCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_CREATED );
            cityCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
            attrStatusList.add( cityCodeStatus );
        }
        final AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( )
                .orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
            attrStatusList.add( _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode ) );
        }
        return attrStatusList;
    }

    /**
     * Private method used to process both "birthplace_code" and "birthplace" attributes during an update process of an identity when the birthcountry is not
     * FRANCE.
     * 
     * @param identity
     * @param attrToCreate
     * @param clientCode
     * @throws IdentityStoreException
     */
    private static List<AttributeStatus> processForeignCityForUpdate( final Identity identity, final List<AttributeDto> attrToCreate,
            final List<AttributeDto> attrToUpdate, final String clientCode ) throws IdentityStoreException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        final AttributeDto cityCodeToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        final AttributeDto cityCodeToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE_CODE ) ).findFirst( )
                .orElse( null );
        if ( cityCodeToCreate != null )
        {
            attrToCreate.remove( cityCodeToCreate );
            final AttributeStatus cityCodeStatus = new AttributeStatus( );
            cityCodeStatus.setStatus( AttributeChangeStatus.NOT_CREATED );
            cityCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_CREATED );
            cityCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
            attrStatusList.add( cityCodeStatus );
        }
        else
            if ( cityCodeToUpdate != null )
            {
                attrToUpdate.remove( cityCodeToUpdate );
                if ( StringUtils.isEmpty( cityCodeToUpdate.getValue( ) ) )
                {
                    attrStatusList.add( _identityAttributeService.updateAttribute( cityCodeToUpdate, identity, clientCode ) );
                }
                else
                {
                    final AttributeStatus cityCodeStatus = new AttributeStatus( );
                    cityCodeStatus.setStatus( AttributeChangeStatus.NOT_UPDATED );
                    cityCodeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_UPDATED );
                    cityCodeStatus.setKey( Constants.PARAM_BIRTH_PLACE_CODE );
                    attrStatusList.add( cityCodeStatus );
                }
            }
        final AttributeDto cityLabelToCreate = attrToCreate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( )
                .orElse( null );
        final AttributeDto cityLabelToUpdate = attrToUpdate.stream( ).filter( a -> a.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ).findFirst( )
                .orElse( null );
        if ( cityLabelToCreate != null )
        {
            attrToCreate.remove( cityLabelToCreate );
            attrStatusList.add( _identityAttributeService.createAttribute( cityLabelToCreate, identity, clientCode ) );
        }
        else
            if ( cityLabelToUpdate != null )
            {
                attrToUpdate.remove( cityLabelToUpdate );
                attrStatusList.add( _identityAttributeService.updateAttribute( cityLabelToUpdate, identity, clientCode ) );
            }
        return attrStatusList;
    }

    /**
     * This method is a temporary patch to prevent from creations and updates with labels only, which could lead to duplicates creation when duplicate rules are based only on INSEE codes.
     * @param identity the {@link IdentityDto} from which to extract the list of attributes
     * @return a {@link Map} of key:value representing the attributes of the identity
     */
    public static Map<String, String> getAttributeListWithLabelAndCodes(final IdentityDto identity ) {
        final Map<String, String> attributes = identity.getAttributes( ).stream( )
                .filter( a -> StringUtils.isNotBlank( a.getValue( ) ) )
                .collect( Collectors.toMap( AttributeDto::getKey, AttributeDto::getValue ) );

        final String strBirthdate = attributes.get(Constants.PARAM_BIRTH_DATE);
        Date birthdate = null;
        try {
            birthdate = strBirthdate != null ? DateUtils.parseDate( strBirthdate, "dd/MM/yyyy" ) : null;
        } catch ( final ParseException e ) {
            AppLogService.error( "Could not parse birthdate attribute value " + strBirthdate + " with format dd/MM/yyyy when trying to convert INSEE codes and labels before verifying possible duplicates at creation or update of an identity" );
        }

        // Country code but no label
        if( attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) && !attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY ) )
        {
            GeoCodesService.getInstance( ).getCountryByCode( attributes.get( Constants.PARAM_BIRTH_COUNTRY_CODE ) ).ifPresent( country -> attributes.put( Constants.PARAM_BIRTH_COUNTRY, country.getValue( ) ) );
        }

        // Country label but no code, only possible when birthdate exists
        if( !attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) && attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY ) && birthdate != null )
        {
            final List<Country> countries = GeoCodesService.getInstance( ).getCountriesListByName( attributes.get( Constants.PARAM_BIRTH_COUNTRY ), birthdate );
            if( countries != null && countries.size( ) == 1 )
            {
                attributes.put( Constants.PARAM_BIRTH_COUNTRY_CODE, countries.get( 0 ).getCode( ) );
            }
        }

        // Process cities only when birth country is FRANCE
        if( attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY ) && Objects.equals(attributes.get(Constants.PARAM_BIRTH_COUNTRY).toUpperCase(), "FRANCE")
                || attributes.containsKey( Constants.PARAM_BIRTH_COUNTRY_CODE ) && Objects.equals(attributes.get(Constants.PARAM_BIRTH_COUNTRY_CODE), "99100") )
        {
            // City code but no label
            if( attributes.containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) && !attributes.containsKey( Constants.PARAM_BIRTH_PLACE ) )
            {
                final City city = birthdate != null
                        ? GeoCodesService.getInstance( ).getCityByDateAndCode( birthdate, attributes.get( Constants.PARAM_BIRTH_PLACE_CODE ) ).orElse( null )
                        : GeoCodesService.getInstance( ).getCityByCode( attributes.get( Constants.PARAM_BIRTH_PLACE_CODE ) ).orElse( null );
                attributes.put( Constants.PARAM_BIRTH_PLACE, city != null ? city.getValueMinComplete( ) : "commune inconnue" );
            }

            // City label but no code
            if( !attributes.containsKey( Constants.PARAM_BIRTH_PLACE_CODE ) && attributes.containsKey( Constants.PARAM_BIRTH_PLACE ) )
            {
                final List<City> cities = birthdate != null
                        ? GeoCodesService.getInstance( ).getCitiesListByNameAndDate( attributes.get( Constants.PARAM_BIRTH_PLACE ), birthdate )
                        : GeoCodesService.getInstance( ).getCitiesListByName( attributes.get( Constants.PARAM_BIRTH_PLACE ) );
                if( cities != null && cities.size( ) == 1 )
                {
                    attributes.put( Constants.PARAM_BIRTH_PLACE_CODE, cities.get( 0 ).getCode( ) );
                }
            }
        }
        else
        {
            attributes.remove( Constants.PARAM_BIRTH_PLACE_CODE ); // In case of foreign country, the identity cannot have an INSEE city code which are reserved to FRANCE.
        }

        return attributes;
    }

}

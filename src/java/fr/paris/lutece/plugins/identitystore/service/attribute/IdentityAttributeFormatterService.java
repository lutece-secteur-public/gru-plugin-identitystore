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

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class used to format attribute values in requests
 */
public class IdentityAttributeFormatterService
{

    private static IdentityAttributeFormatterService _instance;
    private static final List<String> PHONE_ATTR_KEYS = Arrays.asList( "mobile_phone", "fixed_phone" );
    private static final List<String> DATE_ATTR_KEYS = Collections.singletonList( "birthdate" );
    private static final List<String> FIRSTNAME_ATTR_KEYS = Collections.singletonList( "first_name" );
    private static final List<String> UPPERCASE_ATTR_KEYS = Arrays.asList( "birthcountry", "family_name", "preferred_username" );
    private static final List<String> LOWERCASE_ATTR_KEYS = Arrays.asList( "login", "email" );

    public static IdentityAttributeFormatterService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeFormatterService( );
        }
        return _instance;
    }

    /**
     * Formats attribute values in the Identity contained in the provided request.
     * 
     * @see IdentityAttributeFormatterService#formatIdentityAttributeValues(IdentityDto)
     * @param request
     *            the identity change request
     */
    public List<AttributeStatus> formatIdentityChangeRequestAttributeValues( final IdentityChangeRequest request )
    {
        final IdentityDto identity = request.getIdentity( );
        final List<AttributeStatus> statuses = this.formatIdentityAttributeValues( identity );
        request.setIdentity( identity );
        return statuses;
    }

    /**
     * Formats attribute values in the Identity contained in the provided request.
     * 
     * @see IdentityAttributeFormatterService#formatIdentityAttributeValues(IdentityDto)
     * @param request
     *            the identity merge request
     */
    public List<AttributeStatus> formatIdentityMergeRequestAttributeValues( final IdentityMergeRequest request )
    {
        final List<AttributeStatus> statuses = new ArrayList<>( );
        final IdentityDto identity = request.getIdentity( );
        if ( identity != null )
        {
            statuses.addAll( this.formatIdentityAttributeValues( identity ) );
            request.setIdentity( identity );
        }
        return statuses;
    }

    /**
     * Formats attribute values in the provided search request.
     *
     * @see IdentityAttributeFormatterService#formatAttribute(String, String, List)
     * @param request
     *            the identity change request
     */
    public List<AttributeStatus> formatIdentitySearchRequestAttributeValues( final IdentitySearchRequest request )
    {
        final List<AttributeStatus> statuses = new ArrayList<>( );
        final SearchDto search = request.getSearch( );
        if ( search != null )
        {
            search.getAttributes( ).stream( ).filter( attributeDto -> StringUtils.isNotBlank( attributeDto.getValue( ) ) )
                    .forEach(attribute -> attribute.setValue( this.formatAttribute( attribute.getKey(), attribute.getValue(), statuses ) ));
        }
        return statuses;
    }

    /**
     * Formats all attributes stored in the provided identity :
     *
     * @param identity
     *            identity containing attributes to format
     * @see IdentityAttributeFormatterService#formatAttribute(String, String, List)
     * @return FORMATTED_VALUE statuses for attributes whose value has changed after the formatting.
     */
    private List<AttributeStatus> formatIdentityAttributeValues( final IdentityDto identity )
    {
        final List<AttributeStatus> statuses = new ArrayList<>( );
        identity.getAttributes( ).stream( ).filter( attributeDto -> StringUtils.isNotBlank( attributeDto.getValue( ) ) ).forEach( attribute -> {
            attribute.setValue( this.formatAttribute( attribute.getKey(), attribute.getValue(), statuses ) );
        } );
        return statuses;
    }

    /**
     * Formats the provided list of attributes :
     * <ul>
     * <li>Remove leading and trailing spaces</li>
     * <li>Replace all blank characters by an actual space</li>
     * <li>Replace space successions with a single space</li>
     * <li>For phone number attributes :
     * <ul>
     * <li>Remove all spaces, dots, dashes and parenthesis</li>
     * <li>Replace leading indicative part (0033 or +33) by a single zero</li>
     * </ul>
     * </li>
     * <li>For date attributes :
     * <ul>
     * <li>Put a leading zero in day and month parts if they contain only one character</li>
     * </ul>
     * </li>
     * <li>For first name attributes :
     * <ul>
     * <li>Replace comas (,) by a single whitespace</li>
     * <li>Force the first character of each group (space-separated) to be uppercase, the rest is forced to lowercase</li>
     * </ul>
     * </li>
     * <li>For country label, family name and prefered name attributes :
     * <ul>
     * <li>force to uppercase</li>
     * </ul>
     * </li>
     * <li>For login and email attributes :
     * <ul>
     * <li>force to lowercase</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param key the attribute key
     * @param value the attribute value
     * @param statuses the status list that must be filled with formatting results
     * @return FORMATTED_VALUE statuses for attributes whose value has changed after the formatting.
     */
    private String formatAttribute( final String key, final String value, final List<AttributeStatus> statuses )
    {
        // Suppression espaces avant et après, et uniformisation des espacements (tab, space, nbsp, successions d'espaces, ...) en les remplaçant tous par
        // un espace
        String formattedValue = value.trim( ).replaceAll( "\\s+", " " );

        if ( PHONE_ATTR_KEYS.contains( key ) )
        {
            formattedValue = formatPhoneValue( formattedValue );
        }
        if ( DATE_ATTR_KEYS.contains( key ) )
        {
            formattedValue = formatDateValue( formattedValue );
        }
        if ( FIRSTNAME_ATTR_KEYS.contains( key ) )
        {
            formattedValue = formatFirstnameValue( formattedValue );
        }
        if ( UPPERCASE_ATTR_KEYS.contains( key ) )
        {
            formattedValue = StringUtils.upperCase( formattedValue );
        }
        if ( LOWERCASE_ATTR_KEYS.contains( key ) )
        {
            formattedValue = StringUtils.lowerCase( formattedValue );
        }

        // Si la valeur a été modifiée, on renvoie un status
        if ( !formattedValue.equals( value ) )
        {
            statuses.add( buildAttributeValueFormattedStatus( key, value, formattedValue ) );
        }

        return formattedValue;
    }

    /**
     * Formats all attributes stored in the provided identity :
     * <ul>
     * <li>Remove leading and trailing spaces</li>
     * <li>Replace all blank characters by an actual space</li>
     * <li>Replace space successions with a single space</li>
     * <li>For phone number attributes :
     * <ul>
     * <li>Remove all spaces, dots, dashes and parenthesis</li>
     * <li>Replace leading indicative part (0033 or +33) by a single zero</li>
     * </ul>
     * </li>
     * <li>For date attributes :
     * <ul>
     * <li>Put a leading zero in day and month parts if they contain only one character</li>
     * </ul>
     * </li>
     * <li>For first name attributes :
     * <ul>
     * <li>Replace comas (,) by a single whitespace</li>
     * <li>Force the first character of each group (space-separated) to be uppercase, the rest is forced to lowercase</li>
     * </ul>
     * </li>
     * <li>For country label, family name and prefered name attributes :
     * <ul>
     * <li>force to uppercase</li>
     * </ul>
     * </li>
     * <li>For login and email attributes :
     * <ul>
     * <li>force to lowercase</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param identity
     *            identity containing attributes to format
     * @return FORMATTED_VALUE statuses for attributes whose value has changed after the formatting.
     */
    private List<AttributeStatus> formatSearchAttributeValues( final List<SearchAttribute> attributes )
    {
        final List<AttributeStatus> statuses = new ArrayList<>( );
        attributes.stream( ).filter( attributeDto -> StringUtils.isNotBlank( attributeDto.getValue( ) ) ).forEach( attribute -> {
            // Suppression espaces avant et après, et uniformisation des espacements (tab, space, nbsp, successions d'espaces, ...) en les remplaçant tous par
            // un espace
            String formattedValue = attribute.getValue( ).trim( ).replaceAll( "\\s+", " " );

            if ( PHONE_ATTR_KEYS.contains( attribute.getKey( ) ) )
            {
                formattedValue = formatPhoneValue( formattedValue );
            }
            if ( DATE_ATTR_KEYS.contains( attribute.getKey( ) ) )
            {
                formattedValue = formatDateValue( formattedValue );
            }
            if ( FIRSTNAME_ATTR_KEYS.contains( attribute.getKey( ) ) )
            {
                formattedValue = formatFirstnameValue( formattedValue );
            }
            if ( UPPERCASE_ATTR_KEYS.contains( attribute.getKey( ) ) )
            {
                formattedValue = StringUtils.upperCase( formattedValue );
            }
            if ( LOWERCASE_ATTR_KEYS.contains( attribute.getKey( ) ) )
            {
                formattedValue = StringUtils.lowerCase( formattedValue );
            }

            // Si la valeur a été modifiée, on renvoie un status
            if ( !formattedValue.equals( attribute.getValue( ) ) )
            {
                statuses.add( buildAttributeValueFormattedStatus( attribute.getKey( ), attribute.getValue( ), formattedValue ) );
            }
            attribute.setValue( formattedValue );
        } );
        return statuses;
    }

    /**
     * <ul>
     * <li>Remove all spaces, dots, dashes and parenthesis</li>
     * <li>Replace leading indicative part (0033 or +33) by a single zero</li>
     * </ul>
     * 
     * @param value
     *            the value to format
     * @return the formatted value
     */
    private String formatPhoneValue( final String value )
    {
        // Suppression des espaces, points, tirets, et parenthèses
        String formattedValue = value.replaceAll( "\\s", "" ).replace( ".", "" ).replace( "-", "" ).replace( "(", "" ).replace( ")", "" );
        // Remplacement de l'indicatif (0033 ou +33) par un 0
        formattedValue = formattedValue.replaceAll( "^(0{2}|\\+)3{2}", "0" );

        return formattedValue;
    }

    /**
     * Put a leading zero in day and month parts if they contain only one character
     * 
     * @param value
     *            the value to format
     * @return the formatted value
     */
    public String formatDateValue( final String value )
    {
        final StringBuilder sb = new StringBuilder( );
        final String [ ] splittedDate = value.split( "/" );
        if ( splittedDate.length == 3 )
        {
            final String day = splittedDate [0];
            if ( day.length( ) == 1 )
            {
                sb.append( "0" );
            }
            sb.append( day ).append( "/" );

            final String month = splittedDate [1];
            if ( month.length( ) == 1 )
            {
                sb.append( "0" );
            }
            sb.append( month ).append( "/" ).append( splittedDate [2] );

            return sb.toString( );
        }
        else
        {
            return value;
        }
    }

    /**
     * <ul>
     * <li>Replace comas (,) by a single whitespace</li>
     * <li>Force the first character of each group (space-separated) to be uppercase, the rest is forced to lowercase</li>
     * </ul>
     * 
     * @param value
     *            the value to format
     * @return the formatted value
     */
    private String formatFirstnameValue( final String value )
    {
        if ( StringUtils.isBlank( value ) )
        {
            return value;
        }
        return Arrays.stream( value.replace( ",", " " ).trim( ).split( " " ) ).filter( StringUtils::isNotBlank ).map( String::trim )
                .map( firstname -> firstname.substring( 0, 1 ).toUpperCase( ) + firstname.substring( 1 ).toLowerCase( ) ).collect( Collectors.joining( " " ) );
    }

    /**
     * Build attribute value formatted status
     * 
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    public AttributeStatus buildAttributeValueFormattedStatus( final String attrStrKey, final String oldValue, final String newValue )
    {
        final AttributeStatus status = new AttributeStatus( );
        status.setKey( attrStrKey );
        status.setStatus( AttributeChangeStatus.FORMATTED_VALUE );
        status.setMessage( "[" + oldValue + "] -> [" + newValue + "]" );
        status.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_FORMATTED_VALUE );
        return status;
    }

}

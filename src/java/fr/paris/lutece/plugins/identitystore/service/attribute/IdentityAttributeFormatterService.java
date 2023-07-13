/*
 * Copyright (c) 2002-2023, City of Paris
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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service class used to format attribute values in requests
 */
public class IdentityAttributeFormatterService
{

    private static IdentityAttributeFormatterService _instance;
    private static final List<String> PHONE_ATTR_KEYS = Arrays.asList( "mobile_phone", "fixed_phone" );
    private static final List<String> DATE_ATTR_KEYS = Collections.singletonList( "birthdate" );

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
     * @see IdentityAttributeFormatterService#formatIdentityAttributeValues(Identity)
     * @param request
     *            the identity change request
     */
    public List<AttributeStatus> formatIdentityChangeRequestAttributeValues( final IdentityChangeRequest request )
    {
        final Identity identity = request.getIdentity( );
        final List<AttributeStatus> statuses = this.formatIdentityAttributeValues( identity );
        request.setIdentity( identity );
        return statuses;
    }

    /**
     * Formats attribute values in the Identity contained in the provided request.
     * 
     * @see IdentityAttributeFormatterService#formatIdentityAttributeValues(Identity)
     * @param request
     *            the identity merge request
     */
    public List<AttributeStatus> formatIdentityMergeRequestAttributeValues( final IdentityMergeRequest request )
    {
        final Identity identity = request.getIdentity( );
        final List<AttributeStatus> statuses = this.formatIdentityAttributeValues( identity );
        request.setIdentity( identity );
        return statuses;
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
     * </ul>
     * 
     * @param identity
     *            identity containing attributes to format
     * @return FORMATTED_VALUE statuses for attributes whose value has changed after the formatting.
     */
    private List<AttributeStatus> formatIdentityAttributeValues( final Identity identity )
    {
        final List<CertifiedAttribute> formattedAttributes = new ArrayList<>( );
        final List<AttributeStatus> statuses = new ArrayList<>( );
        for ( final CertifiedAttribute attribute : identity.getAttributes( ) )
        {
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

            // Si la valeur a été modifiée, on renvoit un status
            if ( !formattedValue.equals( attribute.getValue( ) ) )
            {
                statuses.add( buildAttributeValueFormattedStatus( attribute.getKey( ), attribute.getValue( ), formattedValue ) );
            }
            attribute.setValue( formattedValue );
            formattedAttributes.add( attribute );
        }
        identity.setAttributes( formattedAttributes );
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
    private String formatDateValue( final String value )
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
            sb.append( month ).append( "/" );

            sb.append( splittedDate [2] );

            return sb.toString( );
        }
        else
        {
            return value;
        }
    }

    /**
     * Build attribute value formatted status
     * 
     * @param attrStrKey
     *            the attribute key
     * @return the status
     */
    private AttributeStatus buildAttributeValueFormattedStatus( final String attrStrKey, final String oldValue, final String newValue )
    {
        final AttributeStatus status = new AttributeStatus( );
        status.setKey( attrStrKey );
        status.setStatus( AttributeChangeStatus.FORMATTED_VALUE );
        status.setMessage( "[" + oldValue + "] -> [" + newValue + "]" );
        return status;
    }

}
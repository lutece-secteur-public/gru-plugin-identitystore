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
package fr.paris.lutece.plugins.identitystore.business.attribute;

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.util.ReferenceList;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * enum representing type of attribute
 *
 */
public enum KeyType
{
    STRING( 0, "string" ),
    NUMERIC( 1, "numeric" ),
    FILE( 2, "file" ),
    DATE( 3, "date" );

    private static final String MESSAGE_PREFIX = "identitystore.keytype.label.";
    private static Map<Integer, KeyType> _mapKeyType = new HashMap<Integer, KeyType>( );
    private int _nId;
    private String _strCode;

    /**
     * private constructor
     *
     * @param nId
     *            id
     * @param strCode
     *            code
     */
    KeyType( int nId, String strCode )
    {
        _nId = nId;
        _strCode = strCode;
    }

    static
    {
        for ( KeyType enumKeyType : EnumSet.allOf( KeyType.class ) )
        {
            _mapKeyType.put( enumKeyType._nId, enumKeyType );
        }
    }

    /**
     * returns id
     *
     * @return id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * returns code
     *
     * @return code
     */
    public String getCode( )
    {
        return _strCode;
    }

    /**
     * returns label
     *
     * @return label
     */
    public String getDefaultLabel( )
    {
        return getLocalizedMessage( Locale.getDefault( ) );
    }

    /**
     * returns KeyType enum for id
     *
     * @param nId
     *            id of KeyType
     * @return KeyType enum
     */
    public static KeyType valueOf( int nId )
    {
        return _mapKeyType.get( Integer.valueOf( nId ) );
    }

    /**
     * returns level label
     *
     * @param locale
     *            the locale used to retrieve the localized messages
     * @return the message
     */
    public String getLocalizedMessage( Locale locale )
    {
        return I18nService.getLocalizedString( MESSAGE_PREFIX + _strCode, locale );
    }

    /**
     * Builds a RefenrenceList object containing all the SupportLevel objects
     *
     * @param locale
     *            the locale used to retrieve the localized messages
     * @return the ReferenceList object
     */
    public static ReferenceList getReferenceList( Locale locale )
    {
        ReferenceList refListLevel = new ReferenceList( );

        for ( KeyType keyType : KeyType.values( ) )
        {
            refListLevel.addItem( keyType.getId( ), keyType.getLocalizedMessage( locale ) );
        }

        return refListLevel;
    }
}

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
package fr.paris.lutece.plugins.identitystore.business.geocodes;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * This is the business class for the object City
 */
public class City implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;

    @NotEmpty( message = "#i18n{geocodes.validation.city.CodeCountry.notEmpty}" )
    @Size( max = 10, message = "#i18n{geocodes.validation.city.CodeCountry.size}" )
    private String _strCodeCountry;

    @NotEmpty( message = "#i18n{geocodes.validation.city.Code.notEmpty}" )
    @Size( max = 10, message = "#i18n{geocodes.validation.city.Code.size}" )
    private String _strCode;

    @NotEmpty( message = "#i18n{geocodes.validation.city.Value.notEmpty}" )
    @Size( max = 255, message = "#i18n{geocodes.validation.city.Value.size}" )
    private String _strName;

    @Size( max = 10, message = "#i18n{geocodes.validation.city.CodeZone.size}" )
    private String _strCodeZone;

    /**
     * Returns the Id
     * 
     * @return The Id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the Id
     * 
     * @param nId
     *            The Id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the CodeCountry
     * 
     * @return The CodeCountry
     */
    public String getCodeCountry( )
    {
        return _strCodeCountry;
    }

    /**
     * Sets the CodeCountry
     * 
     * @param strCodeCountry
     *            The CodeCountry
     */
    public void setCodeCountry( String strCodeCountry )
    {
        _strCodeCountry = strCodeCountry;
    }

    /**
     * Returns the Code
     * 
     * @return The Code
     */
    public String getCode( )
    {
        return _strCode;
    }

    /**
     * Sets the Code
     * 
     * @param strCode
     *            The Code
     */
    public void setCode( String strCode )
    {
        _strCode = strCode;
    }

    /**
     * Returns the Value
     * 
     * @return The Value
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the Value
     * 
     * @param strName
     *            The Value
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the CodeZone
     * 
     * @return The CodeZone
     */
    public String getCodeZone( )
    {
        return _strCodeZone;
    }

    /**
     * Sets the CodeZone
     * 
     * @param strCodeZone
     *            The CodeZone
     */
    public void setCodeZone( String strCodeZone )
    {
        _strCodeZone = strCodeZone;
    }

}

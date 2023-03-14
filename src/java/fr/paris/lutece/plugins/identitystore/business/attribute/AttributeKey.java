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

import javax.validation.constraints.NotEmpty;

import java.io.Serializable;

import javax.validation.constraints.Size;

/**
 * This is the business class for the object AttributeKey
 */
public class AttributeKey implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;
    @NotEmpty( message = "#i18n{identitystore.validation.attributekey.Name.notEmpty}" )
    @Size( max = 100, message = "#i18n{identitystore.validation.attributekey.Name.size}" )
    private String _strName;
    @NotEmpty( message = "#i18n{identitystore.validation.attributekey.KeyName.notEmpty}" )
    @Size( max = 100, message = "#i18n{identitystore.validation.attributekey.KeyName.size}" )
    private String _strKeyName;
    private String _strDescription;
    private KeyType _keyType;
    private boolean _bCertifiable;
    private boolean _bPivot;

    private int _nKeyWeight;

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
     * Returns the Name
     *
     * @return The Name
     */
    public String getName( )
    {
        return _strName;
    }

    /**
     * Sets the Name
     *
     * @param strName
     *            The Name
     */
    public void setName( String strName )
    {
        _strName = strName;
    }

    /**
     * Returns the KeyName
     *
     * @return The KeyName
     */
    public String getKeyName( )
    {
        return _strKeyName;
    }

    /**
     * Sets the KeyName
     *
     * @param strKeyName
     *            The KeyName
     */
    public void setKeyName( String strKeyName )
    {
        _strKeyName = strKeyName;
    }

    /**
     * Returns the Description
     *
     * @return The Description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the Description
     *
     * @param strDescription
     *            The Description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the KeyType
     *
     * @return The KeyType
     */
    public KeyType getKeyType( )
    {
        return _keyType;
    }

    /**
     * Sets the KeyType
     *
     * @param keyType
     *            The KeyType
     */
    public void setKeyType( KeyType keyType )
    {
        _keyType = keyType;
    }

    /**
     * Returns the Certifiable
     *
     * @return The Certifiable
     */
    public boolean getCertifiable( )
    {
        return _bCertifiable;
    }

    /**
     * Sets the Certifiable
     *
     * @param bCertifiable
     *            The Certifiable
     */
    public void setCertifiable( boolean bCertifiable )
    {
        _bCertifiable = bCertifiable;
    }

    /**
     * Returns the Pivot
     *
     * @return The Pivot
     */
    public boolean getPivot( )
    {
        return _bPivot;
    }

    /**
     * Sets the Pivot
     *
     * @param bPivot
     *            The Pivot
     */
    public void setPivot( boolean bPivot )
    {
        _bPivot = bPivot;
    }

    /**
     * Returns the weight of the attribute for scoring computation
     * 
     * @return the weight
     */
    public int getKeyWeight( )
    {
        return _nKeyWeight;
    }

    /**
     * Sets the weight of the attribute for scoring computation
     * 
     * @param _nKeyWeight
     */
    public void setKeyWeight( int _nKeyWeight )
    {
        this._nKeyWeight = _nKeyWeight;
    }
}

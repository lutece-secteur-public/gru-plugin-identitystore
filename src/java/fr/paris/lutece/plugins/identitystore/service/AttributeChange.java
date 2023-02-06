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
package fr.paris.lutece.plugins.identitystore.service;

import java.sql.Timestamp;

/**
 * AttributeChange
 */
public class AttributeChange
{
    // Variables declarations
    private Timestamp _dateChange;
    private int _nIdentityId;
    private String _strAuthorId;
    private String _strAuthorApplication;
    private int _nAuthorType;
    private String _strIdentityConnectionId;
    private String _strCustomerId;
    private String _strChangedKey;
    private String _strOldValue;
    private String _strNewValue;
    private String _strCertifier;
    private AttributeChangeType _attributeChangeType;

    /**
     * Returns the DateChange
     *
     * @return The DateChange
     */
    public Timestamp getDateChange( )
    {
        if ( _dateChange != null )
        {
            return (Timestamp) _dateChange.clone( );
        }
        return null;
    }

    /**
     * Sets the DateChange
     *
     * @param dateChange
     *            The DateChange
     */
    public void setDateChange( Timestamp dateChange )
    {
        if ( dateChange != null )
        {
            _dateChange = (Timestamp) dateChange.clone( );
        }
        else
        {
            _dateChange = null;
        }
    }

    /**
     * Returns the AuthorId
     *
     * @return The AuthorId
     */
    public String getAuthorId( )
    {
        return _strAuthorId;
    }

    /**
     * Sets the AuthorId
     *
     * @param strAuthorId
     *            The AuthorId
     */
    public void setAuthorId( String strAuthorId )
    {
        _strAuthorId = strAuthorId;
    }

    /**
     * Returns the AuthorApplication
     *
     * @return The AuthorApplication
     */
    public String getAuthorApplication( )
    {
        return _strAuthorApplication;
    }

    /**
     * Sets the AuthorApplication
     *
     * @param strAuthorApplication
     *            The AuthorApplication
     */
    public void setAuthorApplication( String strAuthorApplication )
    {
        _strAuthorApplication = strAuthorApplication;
    }

    /**
     * Returns the AuthorType
     *
     * @return The AuthorType
     */
    public int getAuthorType( )
    {
        return _nAuthorType;
    }

    /**
     * Sets the AuthorType
     *
     * @param nAuthorType
     *            The AuthorType
     */
    public void setAuthorType( int nAuthorType )
    {
        _nAuthorType = nAuthorType;
    }

    /**
     * Returns the IdentityId
     *
     * @return The IdentityId
     */
    public String getIdentityConnectionId( )
    {
        return _strIdentityConnectionId;
    }

    /**
     * Sets the IdentityId
     *
     * @param strIdentityId
     *            The IdentityId
     */
    public void setIdentityConnectionId( String strIdentityId )
    {
        _strIdentityConnectionId = strIdentityId;
    }

    /**
     * Returns the CustomerId
     *
     * @return The CustomerId
     */
    public String getCustomerId( )
    {
        return _strCustomerId;
    }

    /**
     * Sets the CustomerId
     *
     * @param strCustomerId
     *            The CustomerId
     */
    public void setCustomerId( String strCustomerId )
    {
        _strCustomerId = strCustomerId;
    }

    /**
     * Returns the ChangedKey
     *
     * @return The ChangedKey
     */
    public String getChangedKey( )
    {
        return _strChangedKey;
    }

    /**
     * Sets the ChangedKey
     *
     * @param strChangedKey
     *            The ChangedKey
     */
    public void setChangedKey( String strChangedKey )
    {
        _strChangedKey = strChangedKey;
    }

    /**
     * Returns the OldValue
     *
     * @return The OldValue
     */
    public String getOldValue( )
    {
        return _strOldValue;
    }

    /**
     * Sets the OldValue
     *
     * @param strOldValue
     *            The OldValue
     */
    public void setOldValue( String strOldValue )
    {
        _strOldValue = strOldValue;
    }

    /**
     * Returns the NewValue
     *
     * @return The NewValue
     */
    public String getNewValue( )
    {
        return _strNewValue;
    }

    /**
     * Sets the NewValue
     *
     * @param strNewValue
     *            The NewValue
     */
    public void setNewValue( String strNewValue )
    {
        _strNewValue = strNewValue;
    }

    /**
     * Returns the Certifier
     *
     * @return The Certifier
     */
    public String getCertifier( )
    {
        return _strCertifier;
    }

    /**
     * Sets the Certifier
     *
     * @param strCertifier
     *            The Certifier
     */
    public void setCertifier( String strCertifier )
    {
        _strCertifier = strCertifier;
    }

    /**
     * Returns the type of the AttributeChange
     *
     * @return The AttributeChangeType
     */
    public AttributeChangeType getChangeType( )
    {
        return _attributeChangeType;
    }

    /**
     * Sets the type of the AttributeChange
     *
     * @param attributeChangeType
     *            The AttributeChangeType
     */
    public void setChangeType( AttributeChangeType attributeChangeType )
    {
        _attributeChangeType = attributeChangeType;
    }

    /**
     * @return the _nidentityId
     */
    public int getIdentityId( )
    {
        return _nIdentityId;
    }

    /**
     * @param nIdentityId
     *            the _nidentityId to set
     */
    public void setIdentityId( int nIdentityId )
    {
        this._nIdentityId = nIdentityId;
    }
}

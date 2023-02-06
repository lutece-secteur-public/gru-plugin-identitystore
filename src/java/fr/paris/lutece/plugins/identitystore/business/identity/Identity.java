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
package fr.paris.lutece.plugins.identitystore.business.identity;

import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Size;

/**
 * This is the business class for the object Identity
 */
public class Identity implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;
    @Size( max = 50, message = "#i18n{identitystore.validation.identity.ConnectionId.size}" )
    private String _strConnectionId;
    @Size( max = 50, message = "#i18n{identitystore.validation.identity.CustomerId.size}" )
    private String _strCustomerId;
    private Map<String, IdentityAttribute> _mapAttributes = new HashMap<>( );
    private boolean _bIsDeleted;
    private Timestamp _dateCreationDate;

    private Timestamp _dateLastUpdateDate;
    private boolean _bIsMerged;
    private Timestamp _dateMergeDate;
    private int _nMasterIdentityId;

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
     * Returns the ConnectionId
     *
     * @return The ConnectionId
     */
    public String getConnectionId( )
    {
        return _strConnectionId;
    }

    /**
     * Sets the ConnectionId
     *
     * @param strConnectionId
     *            The ConnectionId
     */
    public void setConnectionId( String strConnectionId )
    {
        _strConnectionId = strConnectionId;
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
     * @return the _mapAttributes
     */
    public Map<String, IdentityAttribute> getAttributes( )
    {
        return _mapAttributes;
    }

    /**
     * @param mapAttributes
     *            the mapAttributes to set
     */
    public void setAttributes( Map<String, IdentityAttribute> mapAttributes )
    {
        this._mapAttributes = mapAttributes;
    }

    /**
     * returns family name retrieve from attributes
     *
     * @return familyName
     */
    public String getFamilyName( )
    {
        String strFamilyName = StringUtils.EMPTY;

        if ( ( _mapAttributes != null )
                && ( _mapAttributes.get( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_PREFERRED_NAME ) ) != null ) )
        {
            strFamilyName = _mapAttributes.get( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_PREFERRED_NAME ) ).getValue( );
        }

        return strFamilyName;
    }

    /**
     * returns first name retrieve from attributes
     *
     * @return first name
     */
    public String getFirstName( )
    {
        String strFirstName = StringUtils.EMPTY;

        if ( ( _mapAttributes != null )
                && ( _mapAttributes.get( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_NAME_GIVEN ) ) != null ) )
        {
            strFirstName = _mapAttributes.get( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_NAME_GIVEN ) ).getValue( );
        }

        return strFirstName;
    }

    /**
     * 
     * @return the _bIsDeleted
     */
    public boolean isDeleted( )
    {
        return _bIsDeleted;
    }

    /**
     * set true if all the attibutes of identity are deleted
     * 
     * @param bIsDeleted
     *            if identity removed (softDelete)
     */
    public void setDeleted( boolean bIsDeleted )
    {
        _bIsDeleted = bIsDeleted;
    }

    public Timestamp getCreationDate( )
    {
        return _dateCreationDate;
    }

    public void setCreationDate( Timestamp creationDate )
    {
        this._dateCreationDate = creationDate;
    }

    public boolean isMerged( )
    {
        return _bIsMerged;
    }

    public void setMerged( boolean merged )
    {
        this._bIsMerged = merged;
    }

    public Timestamp getMergeDate( )
    {
        return _dateMergeDate;
    }

    public void setMergeDate( Timestamp mergeDate )
    {
        this._dateMergeDate = mergeDate;
    }

    public int getMasterIdentityId( )
    {
        return _nMasterIdentityId;
    }

    public void setMasterIdentityId( int _nMasterIdentityId )
    {
        this._nMasterIdentityId = _nMasterIdentityId;
    }

    public Timestamp getLastUpdateDate( )
    {
        return _dateLastUpdateDate;
    }

    public void setLastUpdateDate( Timestamp lastUpdateDate )
    {
        this._dateLastUpdateDate = lastUpdateDate;
    }
}

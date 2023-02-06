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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeStatusDto;
import fr.paris.lutece.portal.business.file.File;

import java.io.Serializable;

import java.sql.Timestamp;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * This is the business class for the object IdentityAttribute
 */
public class IdentityAttribute implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int _nIdIdentity;
    private AttributeKey _attributeKey;
    @NotBlank( message = "#i18n{identitystore.validation.identityattribute.AttributeValue.notEmpty}" )
    @Size( max = 255, message = "#i18n{identitystore.validation.identityattribute.AttributeValue.size}" )
    private String _strValue;
    private int _nIdCertificate;
    private File _file;
    private AttributeCertificate _certificate;
    private Timestamp _dateLastUpdate;
    private String _strLastUpdateApplicationCode;
    // for DTO conversion, not stored in DB
    private AttributeStatusDto _status;

    /**
     * Returns the IdIdentity
     *
     * @return The IdIdentity
     */
    public int getIdIdentity( )
    {
        return _nIdIdentity;
    }

    /**
     * Sets the IdIdentity
     *
     * @param nIdIdentity
     *            The IdIdentity
     */
    public void setIdIdentity( int nIdIdentity )
    {
        _nIdIdentity = nIdIdentity;
    }

    /**
     * Returns the AttributeKey
     *
     * @return The AttributeKey
     */
    public AttributeKey getAttributeKey( )
    {
        return _attributeKey;
    }

    /**
     * Sets the AttributeKey
     *
     * @param attributeKey
     *            The AttributeKey
     */
    public void setAttributeKey( AttributeKey attributeKey )
    {
        _attributeKey = attributeKey;
    }

    /**
     * Returns the value
     *
     * @return The value
     */
    public String getValue( )
    {
        return _strValue;
    }

    /**
     * Sets the value
     *
     * @param strValue
     *            The value
     */
    public void setValue( String strValue )
    {
        _strValue = strValue;
    }

    /**
     * Returns the IdCertification
     *
     * @return The IdCertification
     */
    public int getIdCertificate( )
    {
        return _nIdCertificate;
    }

    /**
     * Sets the IdCertification
     *
     * @param nIdCertificate
     *            The IdCertification
     */
    public void setIdCertificate( int nIdCertificate )
    {
        _nIdCertificate = nIdCertificate;
    }

    /**
     * @return the file
     */
    public File getFile( )
    {
        return _file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile( File file )
    {
        this._file = file;
    }

    /**
     * Returns the Certificate
     *
     * @return The Certificate
     */
    public AttributeCertificate getCertificate( )
    {
        return _certificate;
    }

    /**
     * Sets the Certificate
     *
     * @param certificate
     *            The certificate
     */
    public void setCertificate( AttributeCertificate certificate )
    {
        _certificate = certificate;
    }

    /**
     * @return the _dateLastUpdate
     */
    public Timestamp getLastUpdateDate( )
    {
        if ( _dateLastUpdate != null )
        {
            return (Timestamp) _dateLastUpdate.clone( );
        }
        return null;
    }

    /**
     * @param dateLastUpdate
     *            the _dateLastUpdate to set
     */
    public void setLastUpdateDate( Timestamp dateLastUpdate )
    {
        if ( dateLastUpdate != null )
        {
            _dateLastUpdate = (Timestamp) dateLastUpdate.clone( );
        }
        else
        {
            _dateLastUpdate = null;
        }
    }

    /**
     * @return the lastUpdateApplicationCode
     */
    public String getLastUpdateApplicationCode( )
    {
        return _strLastUpdateApplicationCode;
    }

    /**
     * @param strLastUpdateApplicationCode
     *            the lastUpdateApplicationCode to set
     */
    public void setLastUpdateApplicationCode( String strLastUpdateApplicationCode )
    {
        this._strLastUpdateApplicationCode = strLastUpdateApplicationCode;
    }

    /**
     * @return the sStatus for WS
     */
    public AttributeStatusDto getStatus( )
    {
        return _status;
    }

    /**
     * @param status
     *            the status to set for WS response
     */
    public void setStatus( AttributeStatusDto status )
    {
        this._status = status;
    }

}

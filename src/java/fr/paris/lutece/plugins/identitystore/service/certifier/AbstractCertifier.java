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
package fr.paris.lutece.plugins.identitystore.service.certifier;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;

/**
 *
 */
public abstract class AbstractCertifier
{
    public static final int NO_CERTIFICATE_EXPIRATION_DELAY = -1;

    private String _strCode;
    private String _strName;
    private String _strDescription;
    private int _nCertificateLevel;
    private String _strIconUrl;
    private int _nExpirationDelay;
    private List<String> _listCertifiableAttributes;
    private HashMap<String, IGenerateAutomaticCertifierAttribute> _mapGenerateAutomaticCertifierAttribute;

    /**
     * constructor with code for registration
     * 
     * @param strCode
     *            the code
     */
    public AbstractCertifier( String strCode )
    {
        super( );
        this._strCode = strCode;
        CertifierRegistry.instance( ).register( this );
    }

    /**
     * Get the certifier code
     * 
     * @return the certifier code
     */
    public String getCode( )
    {
        return _strCode;
    }

    /**
     * Get the certifier name
     * 
     * @return the certifier name
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
     * Returns the CertificateLevel
     * 
     * @return The CertificateLevel
     */
    public int getCertificateLevel( )
    {
        return _nCertificateLevel;
    }

    /**
     * Sets the CertificateLevel
     *
     * @param nCertificateLevel
     *            The CertificateLevel
     */
    public void setCertificateLevel( int nCertificateLevel )
    {
        _nCertificateLevel = nCertificateLevel;
    }

    /**
     * Get the Icon URL
     * 
     * @return The Icon URL
     */
    public String getIconUrl( )
    {
        return _strIconUrl;
    }

    /**
     * Sets the IconUrl
     *
     * @param strIconUrl
     *            The IconUrl
     */
    public void setIconUrl( String strIconUrl )
    {
        _strIconUrl = strIconUrl;
    }

    /**
     * Returns the Certification expiration delay have to return NO_CERTIFICATE_EXPIRATION_DELAY if no expiration
     * 
     * @return the certification expiration delay
     */
    public int getExpirationDelay( )
    {
        return _nExpirationDelay;
    }

    /**
     * Sets the ExpirationDelay
     *
     * @param nExpirationDelay
     *            The ExpirationDelay
     */
    public void setExpirationDelay( int nExpirationDelay )
    {
        _nExpirationDelay = nExpirationDelay;
    }

    /**
     * Get the certifiable attribute keys list
     * 
     * @return a list of certifiable attributes keys
     */
    public List<String> getCertifiableAttributesList( )
    {
        return _listCertifiableAttributes;
    }

    /**
     * Setter for Spring Context
     *
     * @param list
     *            The list
     */
    public void setCertifiableAttributesList( List<String> list )
    {
        _listCertifiableAttributes = list;
    }

    /**
     * generate an AttributeCertificate
     * 
     * @return AttributeCertificate
     */
    public final AttributeCertificate generateCertificate( )
    {

        AttributeCertificate certificate = new AttributeCertificate( );
        certificate.setCertificateDate( new Timestamp( new Date( ).getTime( ) ) );
        certificate.setCertificateLevel( _nCertificateLevel );
        certificate.setCertifierName( _strName );
        certificate.setCertifierCode( _strCode );

        if ( _nExpirationDelay != NO_CERTIFICATE_EXPIRATION_DELAY )
        {
            Calendar c = Calendar.getInstance( );
            c.setTime( new Date( ) );
            c.add( Calendar.DATE, _nExpirationDelay );
            certificate.setExpirationDate( new Timestamp( c.getTime( ).getTime( ) ) );
        }
        return certificate;
    }

    /**
     * Gets the generate automatic certifier attribute.
     *
     * @return the generate automatic certifier attribute
     */
    public HashMap<String, IGenerateAutomaticCertifierAttribute> getGenerateAutomaticCertifierAttribute( )
    {
        return _mapGenerateAutomaticCertifierAttribute;
    }

    /**
     * Sets the generate automatic certifier attribute.
     *
     * @param _mapGenerateAutomaticCertifierAttribute
     *            the map generate automatic certifier attribute
     */
    public void setGenerateAutomaticCertifierAttribute( HashMap<String, IGenerateAutomaticCertifierAttribute> _mapGenerateAutomaticCertifierAttribute )
    {
        this._mapGenerateAutomaticCertifierAttribute = _mapGenerateAutomaticCertifierAttribute;
    }
}

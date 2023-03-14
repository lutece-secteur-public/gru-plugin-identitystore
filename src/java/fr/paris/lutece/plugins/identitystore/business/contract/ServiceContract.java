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
package fr.paris.lutece.plugins.identitystore.business.contract;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the business class for the object ServiceContract
 */
public class ServiceContract implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;

    @NotEmpty( message = "#i18n{contractservice.validation.servicecontract.Name.notEmpty}" )
    @Size( max = 50, message = "#i18n{contractservice.validation.servicecontract.Name.size}" )
    private String _strName;

    @NotEmpty( message = "#i18n{contractservice.validation.servicecontract.OrganizationalEntity.notEmpty}" )
    @Size( max = 50, message = "#i18n{contractservice.validation.servicecontract.OrganizationalEntity.size}" )
    private String _strOrganizationalEntity;

    @NotEmpty( message = "#i18n{contractservice.validation.servicecontract.ResponsibleName.notEmpty}" )
    @Size( max = 50, message = "#i18n{contractservice.validation.servicecontract.ResponsibleName.size}" )
    private String _strResponsibleName;

    @NotEmpty( message = "#i18n{contractservice.validation.servicecontract.ContactName.notEmpty}" )
    @Size( max = 50, message = "#i18n{contractservice.validation.servicecontract.ContactName.size}" )
    private String _strContactName;

    @NotEmpty( message = "#i18n{contractservice.validation.servicecontract.ServiceType.notEmpty}" )
    @Size( max = 50, message = "#i18n{contractservice.validation.servicecontract.ServiceType.size}" )
    private String _strServiceType;

    private Date _dateStartingDate;
    private Date _dateEndingDate;

    private boolean _bAuthorizedMerge;

    private boolean _bAuthorizedAccountUpdate;

    private boolean _bAuthorizedDeletion;

    private boolean _bAuthorizedImport;

    private boolean _bAuthorizedExport;

    private boolean _bIsAuthorizedDeleteValue;

    private boolean _bIsAuthorizedDeleteCertificate;

    private List<AttributeRight> _listAttributeRights = new ArrayList<>( );

    private List<AttributeCertification> _listAttributeCertifications = new ArrayList<>( );

    private List<AttributeRequirement> _listAttributeRequirements = new ArrayList<>( );

    public boolean isActive( )
    {
        final Timestamp actualTimestamp = Timestamp.from( Instant.now( ) );
        return ( this.getStartingDate( ).before( actualTimestamp ) && ( this.getEndingDate( ) == null || this.getEndingDate( ).after( actualTimestamp ) ) );
    }

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
     * Returns the OrganizationalEntity
     * 
     * @return The OrganizationalEntity
     */
    public String getOrganizationalEntity( )
    {
        return _strOrganizationalEntity;
    }

    /**
     * Sets the OrganizationalEntity
     * 
     * @param strOrganizationalEntity
     *            The OrganizationalEntity
     */
    public void setOrganizationalEntity( String strOrganizationalEntity )
    {
        _strOrganizationalEntity = strOrganizationalEntity;
    }

    /**
     * Returns the ResponsibleName
     * 
     * @return The ResponsibleName
     */
    public String getResponsibleName( )
    {
        return _strResponsibleName;
    }

    /**
     * Sets the ResponsibleName
     * 
     * @param strResponsibleName
     *            The ResponsibleName
     */
    public void setResponsibleName( String strResponsibleName )
    {
        _strResponsibleName = strResponsibleName;
    }

    /**
     * Returns the ContactName
     * 
     * @return The ContactName
     */
    public String getContactName( )
    {
        return _strContactName;
    }

    /**
     * Sets the ContactName
     * 
     * @param strContactName
     *            The ContactName
     */
    public void setContactName( String strContactName )
    {
        _strContactName = strContactName;
    }

    /**
     * Returns the ServiceType
     * 
     * @return The ServiceType
     */
    public String getServiceType( )
    {
        return _strServiceType;
    }

    /**
     * Sets the ServiceType
     * 
     * @param strServiceType
     *            The ServiceType
     */
    public void setServiceType( String strServiceType )
    {
        _strServiceType = strServiceType;
    }

    public Date getStartingDate( )
    {
        return _dateStartingDate;
    }

    public void setStartingDate( Date _dateStartingDate )
    {
        this._dateStartingDate = _dateStartingDate;
    }

    public Date getEndingDate( )
    {
        return _dateEndingDate;
    }

    public void setEndingDate( Date _dateEndingDate )
    {
        this._dateEndingDate = _dateEndingDate;
    }

    /**
     * Returns the AuthorizedMerge
     * 
     * @return The AuthorizedMerge
     */
    public boolean getAuthorizedMerge( )
    {
        return _bAuthorizedMerge;
    }

    /**
     * Sets the AuthorizedMerge
     * 
     * @param bAuthorizedMerge
     *            The AuthorizedMerge
     */
    public void setAuthorizedMerge( boolean bAuthorizedMerge )
    {
        _bAuthorizedMerge = bAuthorizedMerge;
    }

    /**
     * Returns the AuthorizedAccountUpdate
     *
     * @return The AuthorizedAccountUpdate
     */
    public boolean getAuthorizedAccountUpdate( )
    {
        return _bAuthorizedAccountUpdate;
    }

    /**
     * Sets the AuthorizedMerge
     *
     * @param bAuthorizedAccountUpdate
     *            The AuthorizedAccountUpdate
     */
    public void setAuthorizedAccountUpdate( boolean bAuthorizedAccountUpdate )
    {
        _bAuthorizedAccountUpdate = bAuthorizedAccountUpdate;
    }

    /**
     * Returns the AuthorizedDeletion
     * 
     * @return The AuthorizedDeletion
     */
    public boolean getAuthorizedDeletion( )
    {
        return _bAuthorizedDeletion;
    }

    /**
     * Sets the AuthorizedDeletion
     * 
     * @param bAuthorizedDeletion
     *            The AuthorizedDeletion
     */
    public void setAuthorizedDeletion( boolean bAuthorizedDeletion )
    {
        _bAuthorizedDeletion = bAuthorizedDeletion;
    }

    /**
     * Returns the AuthorizedImport
     * 
     * @return The AuthorizedImport
     */
    public boolean getAuthorizedImport( )
    {
        return _bAuthorizedImport;
    }

    /**
     * Sets the AuthorizedImport
     * 
     * @param bAuthorizedImport
     *            The AuthorizedImport
     */
    public void setAuthorizedImport( boolean bAuthorizedImport )
    {
        _bAuthorizedImport = bAuthorizedImport;
    }

    /**
     * Returns the AuthorizedExport
     * 
     * @return The AuthorizedExport
     */
    public boolean getAuthorizedExport( )
    {
        return _bAuthorizedExport;
    }

    /**
     * Sets the AuthorizedExport
     * 
     * @param bAuthorizedExport
     *            The AuthorizedExport
     */
    public void setAuthorizedExport( boolean bAuthorizedExport )
    {
        _bAuthorizedExport = bAuthorizedExport;
    }

    public boolean getAuthorizedDeleteValue( )
    {
        return _bIsAuthorizedDeleteValue;
    }

    public void setAuthorizedDeleteValue( boolean bIsAuthorizedDeleteValue )
    {
        this._bIsAuthorizedDeleteValue = bIsAuthorizedDeleteValue;
    }

    public boolean getAuthorizedDeleteCertificate( )
    {
        return _bIsAuthorizedDeleteCertificate;
    }

    public void setAuthorizedDeleteCertificate( boolean bIsAuthorizedDeleteCertificate )
    {
        this._bIsAuthorizedDeleteCertificate = bIsAuthorizedDeleteCertificate;
    }

    public List<AttributeRight> getAttributeRights( )
    {
        return _listAttributeRights;
    }

    public void setAttributeRights( List<AttributeRight> attributeRights )
    {
        this._listAttributeRights = attributeRights;
    }

    public List<AttributeCertification> getAttributeCertifications( )
    {
        return _listAttributeCertifications;
    }

    public void setAttributeCertifications( List<AttributeCertification> attributeCertifications )
    {
        this._listAttributeCertifications = attributeCertifications;
    }

    public List<AttributeRequirement> getAttributeRequirements( )
    {
        return _listAttributeRequirements;
    }

    public void setAttributeRequirements( List<AttributeRequirement> attributeRequirements )
    {
        this._listAttributeRequirements = attributeRequirements;
    }
}

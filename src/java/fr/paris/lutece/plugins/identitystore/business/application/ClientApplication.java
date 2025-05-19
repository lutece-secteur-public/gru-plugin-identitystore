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
package fr.paris.lutece.plugins.identitystore.business.application;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * class representing Client application
 *
 */
public class ClientApplication implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Variables declarations
    private int _nId;
    @NotEmpty( message = "#i18n{identitystore.validation.clientapplication.Name.notEmpty}" )
    @Size( max = 255, message = "#i18n{identitystore.validation.clientapplication.Name.size}" )
    private String _strName;
    @NotEmpty( message = "#i18n{identitystore.validation.clientapplication.Code.notEmpty}" )
    @Size( max = 255, message = "#i18n{identitystore.validation.clientapplication.Code.size}" )
    private String _strClientCode;
    @NotEmpty( message = "#i18n{identitystore.validation.clientapplication.Code.notEmpty}" )
    @Size( max = 255, message = "#i18n{identitystore.validation.clientapplication.Code.size}" )
    private String _strApplicationCode;

    private List<ServiceContract> serviceContracts = new ArrayList<>( );

    @JsonIgnore
    private Date creationDate;
    @JsonIgnore
    private Date lastUpdateDate;
    @JsonIgnore
    private String _strAuthorName;

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
     * {@inheritDoc}
     *
     */
    public String getClientCode( )
    {
        return _strClientCode;
    }

    /**
     * Sets the Code
     *
     * @param strCode
     *            The Code
     */
    public void setClientCode( String strCode )
    {
        _strClientCode = strCode;
    }

    public String getApplicationCode( )
    {
        return _strApplicationCode;
    }

    public void setApplicationCode( String _strApplicationCode )
    {
        this._strApplicationCode = _strApplicationCode;
    }

    public List<ServiceContract> getServiceContracts( )
    {
        return serviceContracts;
    }

    public void setServiceContracts( List<ServiceContract> serviceContracts )
    {
        this.serviceContracts = serviceContracts;
    }

    public String getAuthorName()
    {
        return _strAuthorName;
    }

    public void setAuthorName(String _strAuthorName)
    {
        this._strAuthorName = _strAuthorName;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}

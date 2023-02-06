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

import java.util.ArrayList;
import java.util.List;

/**
 * DTO class for displaying AttributeKeyJspBean.VIEW_APP_RIGHT_ATTRIBUTES
 */
public class AttributeApplicationsRight
{
    private String _strAttributeKey;
    private final List<String> _listReadApplications = new ArrayList<String>( );
    private final List<String> _listWriteApplications = new ArrayList<String>( );
    private final List<String> _listCertifApplications = new ArrayList<String>( );
    private final List<String> _listSearchApplications = new ArrayList<String>( );

    /**
     * @return the strAttributeKey
     */
    public String getAttributeKey( )
    {
        return _strAttributeKey;
    }

    /**
     * @param strAttributeKey
     *            the strAttributeKey to set
     */
    public void setAttributeKey( String strAttributeKey )
    {
        this._strAttributeKey = strAttributeKey;
    }

    /**
     * @return the listReadApplications
     */
    public List<String> getReadApplications( )
    {
        return _listReadApplications;
    }

    /**
     * @param strApplicationCode
     *            an application code with read right
     */
    public void addReadApplication( String strApplicationCode )
    {
        this._listReadApplications.add( strApplicationCode );
    }

    /**
     * @return the listWriteApplications
     */
    public List<String> getWriteApplications( )
    {
        return _listWriteApplications;
    }

    /**
     * @param strApplicationCode
     *            an application code with write right
     */
    public void addWriteApplication( String strApplicationCode )
    {
        this._listWriteApplications.add( strApplicationCode );
    }

    /**
     * @return the listCertifApplications
     */
    public List<String> getCertifApplications( )
    {
        return _listCertifApplications;
    }

    /**
     * @param strApplicationCode
     *            an application code with certification right
     */
    public void addCertifApplication( String strApplicationCode )
    {
        this._listCertifApplications.add( strApplicationCode );
    }

    /**
     * @return the listSearchApplications
     */
    public List<String> getSearchApplications( )
    {
        return _listSearchApplications;
    }

    /**
     * @param strApplicationCode
     *            an application code with certification right
     */
    public void addSearchApplication( String strApplicationCode )
    {
        this._listSearchApplications.add( strApplicationCode );
    }
}

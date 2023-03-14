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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model;

import java.sql.Timestamp;

public class AttributeObject
{
    private String name;
    private String key;
    private String type;
    private Integer weight;
    private String value;
    private String description;
    private boolean pivot;
    private String certifierCode;
    private String certifierName;
    private Timestamp certificateDate;
    private Integer certificateLevel;
    private Timestamp certificateExpirationDate;
    private String lastUpdateApplicationCode;

    public AttributeObject( String name, String key, String type, Integer weight, String value, String description, boolean pivot, String certifierCode,
            String certifierName, Timestamp certificateDate, Integer certificateLevel, Timestamp certificateExpirationDate, String lastUpdateApplicationCode )
    {
        this.name = name;
        this.key = key;
        this.type = type;
        this.weight = weight;
        this.value = value;
        this.description = description;
        this.pivot = pivot;
        this.certifierCode = certifierCode;
        this.certifierName = certifierName;
        this.certificateDate = certificateDate;
        this.certificateLevel = certificateLevel;
        this.certificateExpirationDate = certificateExpirationDate;
        this.lastUpdateApplicationCode = lastUpdateApplicationCode;
    }

    public AttributeObject( )
    {

    }

    public String getName( )
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getKey( )
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getType( )
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public Integer getWeight( )
    {
        return weight;
    }

    public void setWeight( Integer weight )
    {
        this.weight = weight;
    }

    public String getValue( )
    {
        return value;
    }

    public void setValue( String value )
    {
        this.value = value;
    }

    public String getDescription( )
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    public boolean isPivot( )
    {
        return pivot;
    }

    public void setPivot( boolean pivot )
    {
        this.pivot = pivot;
    }

    public String getCertifierCode( )
    {
        return certifierCode;
    }

    public void setCertifierCode( String certifierCode )
    {
        this.certifierCode = certifierCode;
    }

    public String getCertifierName( )
    {
        return certifierName;
    }

    public void setCertifierName( String certifierName )
    {
        this.certifierName = certifierName;
    }

    public Timestamp getCertificateDate( )
    {
        return certificateDate;
    }

    public void setCertificateDate( Timestamp certificateDate )
    {
        this.certificateDate = certificateDate;
    }

    public Integer getCertificateLevel( )
    {
        return certificateLevel;
    }

    public void setCertificateLevel( Integer certificateLevel )
    {
        this.certificateLevel = certificateLevel;
    }

    public Timestamp getCertificateExpirationDate( )
    {
        return certificateExpirationDate;
    }

    public void setCertificateExpirationDate( Timestamp certificateExpirationDate )
    {
        this.certificateExpirationDate = certificateExpirationDate;
    }

    public String getLastUpdateApplicationCode( )
    {
        return lastUpdateApplicationCode;
    }

    public void setLastUpdateApplicationCode( String lastUpdateApplicationCode )
    {
        this.lastUpdateApplicationCode = lastUpdateApplicationCode;
    }
}

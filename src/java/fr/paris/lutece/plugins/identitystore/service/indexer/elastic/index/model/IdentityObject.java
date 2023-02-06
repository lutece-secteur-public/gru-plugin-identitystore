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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@JsonInclude( JsonInclude.Include.NON_NULL )
public class IdentityObject
{
    private String connectionId;
    private String customerId;
    private Timestamp creationDate;
    private Timestamp lastUpdateDate;
    private Map<String, AttributeObject> attributes = new HashMap( );

    public IdentityObject( String connectionId, String customerId, Timestamp creationDate, Timestamp lastUpdateDate, Map<String, AttributeObject> attributes )
    {
        this.connectionId = connectionId;
        this.customerId = customerId;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.attributes.putAll( attributes );
    }

    public IdentityObject( )
    {

    }

    public String getConnectionId( )
    {
        return connectionId;
    }

    public void setConnectionId( String connectionId )
    {
        this.connectionId = connectionId;
    }

    public String getCustomerId( )
    {
        return customerId;
    }

    public void setCustomerId( String customerId )
    {
        this.customerId = customerId;
    }

    public Timestamp getCreationDate( )
    {
        return creationDate;
    }

    public void setCreationDate( Timestamp creationDate )
    {
        this.creationDate = creationDate;
    }

    public Timestamp getLastUpdateDate( )
    {
        return lastUpdateDate;
    }

    public void setLastUpdateDate( Timestamp lastUpdateDate )
    {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Map<String, AttributeObject> getAttributes( )
    {
        return attributes;
    }

    public void setAttributes( Map<String, AttributeObject> attributes )
    {
        this.attributes = attributes;
    }
}

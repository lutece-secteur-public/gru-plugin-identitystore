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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.ArrayList;
import java.util.List;

public class AttributeKeyCache extends AbstractCacheableService
{
    public static final String SERVICE_NAME = "AttributeKeyCache";

    public AttributeKeyCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init AttributeKey cache" );
        this.resetCache( );
        AttributeKeyHome.getAttributeKeysList( true ).forEach( attributeKey -> this.put( attributeKey.getKeyName( ), attributeKey ) );
    }

    public void put( final String keyName, final AttributeKey attributeKey )
    {
        if ( this.getKeys( ).contains( keyName ) )
        {
            this.removeKey( keyName );
        }
        this.putInCache( keyName, attributeKey );
        AppLogService.debug( "AttributeKey added to cache: " + keyName );
    }

    public void remove( final String keyName )
    {
        if ( this.getKeys( ).contains( keyName ) )
        {
            this.removeKey( keyName );
        }

        AppLogService.debug( "AttributeKey removed from cache: " + keyName );
    }

    public List<AttributeKey> getAll( )
    {
        final List<AttributeKey> allAttributeKeys = new ArrayList<>( );
        if ( this.getKeys( ).isEmpty( ) )
        {
            this.refresh( );
        }
        for ( final String key : this.getKeys( ) )
        {
            try
            {
                allAttributeKeys.add( this.get( key ) );
            }
            catch( final ResourceNotFoundException e )
            {
                // this shouldn't happen : do nothing.
            }
        }
        // If cache is not activated, get from db
        if ( allAttributeKeys.isEmpty( ) )
        {
            allAttributeKeys.addAll( AttributeKeyHome.getAttributeKeysList( true ) );
        }
        return allAttributeKeys;
    }

    public AttributeKey get( final String keyName ) throws ResourceNotFoundException
    {
        AttributeKey attributeKey = (AttributeKey) this.getFromCache( keyName );
        if ( attributeKey == null )
        {
            attributeKey = this.getFromDatabase( keyName );
            this.put( keyName, attributeKey );
        }
        return attributeKey;
    }

    public AttributeKey getFromDatabase( final String keyName ) throws ResourceNotFoundException
    {
        final AttributeKey attributeKey = AttributeKeyHome.findByKey( keyName, true );
        if ( attributeKey == null )
        {
            throw new ResourceNotFoundException( "No attribute key could be found with key " + keyName, Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY );
        }
        return attributeKey;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

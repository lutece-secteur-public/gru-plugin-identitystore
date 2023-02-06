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
package fr.paris.lutece.plugins.identitystore.cache;

import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevelHome;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import org.apache.log4j.Logger;

import java.util.List;

public class RefAttributeCertificationDefinitionCache extends AbstractCacheableService
{

    private static Logger _logger = Logger.getLogger( RefAttributeCertificationDefinitionCache.class );

    public static final String SERVICE_NAME = "RefAttributeCertificationDefinitionCache";

    public RefAttributeCertificationDefinitionCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        _logger.info( "Init AttributeCertificationLevel cache" );
        this.resetCache( );
        final List<RefAttributeCertificationLevel> all = RefAttributeCertificationLevelHome.getRefAttributeCertificationLevelsList( );
        all.forEach( refAttributeCertificationLevel -> this.put( refAttributeCertificationLevel ) );
    }

    private String buildKey( final RefAttributeCertificationLevel refAttributeCertificationLevel )
    {
        return this.buildKey( refAttributeCertificationLevel.getRefAttributeCertificationProcessus( ).getCode( ),
                refAttributeCertificationLevel.getAttributeKey( ).getKeyName( ) );
    }

    private String buildKey( final String processusCode, final String attributeKeyName )
    {
        return processusCode + "::" + attributeKeyName;
    }

    public void put( final RefAttributeCertificationLevel refAttributeCertificationLevel )
    {
        final String key = this.buildKey( refAttributeCertificationLevel );
        if ( this.getKeys( ).contains( key ) )
        {
            this.removeKey( key );
        }
        this.putInCache( key, refAttributeCertificationLevel );
        _logger.info( "AttributeCertificationLevel added to cache: " + key );
    }

    public void remove( final RefAttributeCertificationLevel refAttributeCertificationLevel )
    {
        final String key = this.buildKey( refAttributeCertificationLevel );
        if ( this.getKeys( ).contains( key ) )
        {
            this.removeKey( key );
        }
        _logger.info( "AttributeCertificationLevel removed from cache: " + key );
    }

    public RefAttributeCertificationLevel get( final String processusCode, final String attributeKeyName )
    {
        final String key = this.buildKey( processusCode, attributeKeyName );
        RefAttributeCertificationLevel certificationLevel = (RefAttributeCertificationLevel) this.getFromCache( key );
        if ( certificationLevel == null )
        {
            certificationLevel = RefAttributeCertificationLevelHome.findByProcessusAndAttributeKeyName( processusCode, attributeKeyName );
            this.put( certificationLevel );
        }
        return certificationLevel;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

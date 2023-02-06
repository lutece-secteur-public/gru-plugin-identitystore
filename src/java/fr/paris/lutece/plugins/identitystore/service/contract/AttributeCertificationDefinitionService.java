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
package fr.paris.lutece.plugins.identitystore.service.contract;

import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessusHome;
import fr.paris.lutece.plugins.identitystore.cache.RefAttributeCertificationDefinitionCache;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

public class AttributeCertificationDefinitionService
{
    private RefAttributeCertificationDefinitionCache _cache = SpringContextService.getBean( "identitystore.refAttributeCertificationDefinitionCache" );

    private static AttributeCertificationDefinitionService _instance;

    public static AttributeCertificationDefinitionService instance( )
    {
        if ( _instance == null )
        {
            _instance = new AttributeCertificationDefinitionService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    private AttributeCertificationDefinitionService( )
    {
    }

    public RefAttributeCertificationLevel get( final String processusCode, final String attributeKeyName ) throws IdentityAttributeNotFoundException
    {
        return _cache.get( processusCode, attributeKeyName );
    }

    public void addRefAttributeCertificationLevels( final List<RefAttributeCertificationLevel> refAttributeCertificationLevelList )
    {
        refAttributeCertificationLevelList.forEach( level -> _cache.put( level ) );
        RefAttributeCertificationProcessusHome.addRefAttributeCertificationLevels( refAttributeCertificationLevelList );
    }

    /**
     * Removes {@link RefAttributeCertificationLevel} list from the given {@link RefAttributeCertificationProcessus}
     * 
     * @param processus
     */
    public void removeRefAttributeCertificationLevels( final RefAttributeCertificationProcessus processus )
    {
        RefAttributeCertificationProcessusHome.selectAttributeLevels( processus ).forEach( level -> _cache.remove( level ) );
        RefAttributeCertificationProcessusHome.removeProcessusLevels( processus.getId( ) );
    }
}

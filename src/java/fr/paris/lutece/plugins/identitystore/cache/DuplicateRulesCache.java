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

import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.util.Objects;

public class DuplicateRulesCache extends AbstractCacheableService
{
    public static final String SERVICE_NAME = "DuplicateRulesCache";

    public DuplicateRulesCache( )
    {
        this.initCache( );
    }

    public void refresh( )
    {
        AppLogService.debug( "Init AttributeKey cache" );
        this.resetCache( );
        DuplicateRuleHome.findAll( ).forEach( this::put );
    }

    public void put( final DuplicateRule rule )
    {
        if ( this.getKeys( ).contains( rule.getCode( ) ) )
        {
            this.removeKey( rule.getCode( ) );
        }
        this.putInCache( rule.getCode( ), rule );
        AppLogService.debug( "Duplicate rule added to cache: " + rule.getCode( ) );
    }

    public void remove( final String ruleCode )
    {
        if ( this.getKeys( ).contains( ruleCode ) )
        {
            this.removeKey( ruleCode );
        }

        AppLogService.debug( "Duplicate rule removed from cache: " + ruleCode );
    }

    /**
     * Deletes a {@link DuplicateRule} by its id in the database
     *
     * @param id
     */
    public void remove( final Integer id )
    {
        this.getKeys( ).forEach( key -> {
            try
            {
                final DuplicateRule duplicateRule = this.get( key );
                if ( Objects.equals( duplicateRule.getId( ), id ) )
                {
                    this.removeKey( key );
                }
            }
            catch( final ResourceNotFoundException e )
            {
                AppLogService.error( "Cannot delete duplicate rule with id" + id + " : {}", e );
            }
        } );
    }

    public DuplicateRule get( final String ruleCode ) throws ResourceNotFoundException
    {
        DuplicateRule duplicateRule = (DuplicateRule) this.getFromCache( ruleCode );
        if ( duplicateRule == null )
        {
            duplicateRule = this.getFromDatabase( ruleCode );
            this.put( duplicateRule );
        }
        return duplicateRule;
    }

    public DuplicateRule getFromDatabase( final String ruleCode ) throws ResourceNotFoundException {
        final DuplicateRule duplicateRule = DuplicateRuleHome.findByCode( ruleCode );
        if ( duplicateRule == null )
        {
            throw new ResourceNotFoundException("No duplicate rule could be found with code " + ruleCode, Constants.PROPERTY_REST_ERROR_UNKNOWN_DUPLICATE_RULE_CODE);
        }
        return duplicateRule;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.duplicate;

import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.cache.DuplicateRulesCache;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.DuplicateRuleSummaryDto;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DuplicateRuleService
{

    private final DuplicateRulesCache _cache = SpringContextService.getBean( "identitystore.duplicateRulesCache" );
    private static DuplicateRuleService _instance;

    public static DuplicateRuleService instance( )
    {
        if ( _instance == null )
        {
            _instance = new DuplicateRuleService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    private DuplicateRuleService( )
    {
    }

    /**
     *
     * @return
     * @throws ResourceNotFoundException
     */
    public List<DuplicateRule> findAll( ) throws ResourceNotFoundException
    {
        final List<String> allCodes = DuplicateRuleHome.findAllCodes( );
        final List<DuplicateRule> list = new ArrayList<>( );
        for ( final String code : allCodes )
        {
            final DuplicateRule duplicateRule = _cache.get( code );
            list.add( duplicateRule );
        }
        return list;
    }

    /**
     * Find all summaries of rules having priority or higher
     * 
     * @param priority
     *            the min priority
     * @return a list of rule summaries
     * @throws ResourceNotFoundException
     */
    public List<DuplicateRuleSummaryDto> findSummaries( final Integer priority ) throws ResourceNotFoundException
    {
        return this.findAll( ).stream( ).filter( rule -> priority == null || rule.getPriority( ) <= priority ).map( rule -> {
            final DuplicateRuleSummaryDto ruleDto = new DuplicateRuleSummaryDto( );
            ruleDto.setId( rule.getId( ) );
            ruleDto.setName( rule.getName( ) );
            ruleDto.setCode( rule.getCode( ) );
            ruleDto.setDescription( rule.getDescription( ) );
            ruleDto.setPriority( rule.getPriority( ) );
            ruleDto.setDaemonLastExecDate( rule.getDaemonLastExecDate( ) );
            ruleDto.setDuplicateCount( SuspiciousIdentityHome.countSuspiciousIdentity( rule.getId( ) ) );
            return ruleDto;
        } ).sorted( Comparator.comparing( DuplicateRuleSummaryDto::getPriority ) ).collect( Collectors.toList( ) );
    }

    /**
     * Get {@link DuplicateRule} from cache by its code.
     *
     * @param ruleCode
     *            the rule code
     * @return DuplicateRule
     * @throws ResourceNotFoundException
     *             if the rule is not found for the provided code
     */
    public DuplicateRule get( final String ruleCode ) throws ResourceNotFoundException
    {
        return _cache.get( ruleCode );
    }

    /**
     * Get {@link DuplicateRule} from cache by its code.
     *
     * @param ruleCode
     *            the rule code
     * @return {@link DuplicateRule}, or null if the rule is not found for the provided code
     */
    public DuplicateRule safeGet( final String ruleCode )
    {
        try
        {
            return _cache.get( ruleCode );
        }
        catch( final ResourceNotFoundException e )
        {
            return null;
        }
    }

    /**
     * Creates a new {@link DuplicateRule} and adds it to cache.
     * 
     * @param duplicateRule
     * @return
     */
    public DuplicateRule create( final DuplicateRule duplicateRule )
    {
        DuplicateRuleHome.create( duplicateRule );
        _cache.put( duplicateRule );
        return duplicateRule;
    }

    /**
     * Update an existing {@link DuplicateRule} (if possible).
     * 
     * @param duplicateRule
     * @return
     */
    public DuplicateRule update( final DuplicateRule duplicateRule )
    {
        DuplicateRuleHome.update( duplicateRule );
        _cache.put( duplicateRule );
        return duplicateRule;
    }

    /**
     * Deletes a {@link DuplicateRule} by its id in the database and cache
     * 
     * @param id
     */
    public void delete( final Integer id )
    {
        DuplicateRuleHome.delete( id );
        _cache.remove( id );
    }

}

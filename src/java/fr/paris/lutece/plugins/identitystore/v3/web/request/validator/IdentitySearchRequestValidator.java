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
package fr.paris.lutece.plugins.identitystore.v3.web.request.validator;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRule;
import fr.paris.lutece.plugins.identitystore.business.rules.search.IdentitySearchRuleHome;
import fr.paris.lutece.plugins.identitystore.business.rules.search.SearchRuleType;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentitySearchRequestValidator
{
    private static IdentitySearchRequestValidator instance;

    public static IdentitySearchRequestValidator instance( )
    {
        if ( instance == null )
        {
            instance = new IdentitySearchRequestValidator( );
        }
        return instance;
    }

    private IdentitySearchRequestValidator( )
    {
    }

    /**
     * Check if search request contains the mandatory attributes, according to the {@link IdentitySearchRule}.
     * 
     * @param request
     *            the search request
     * @throws RequestFormatException
     */
    public void checkRequiredAttributes( final IdentitySearchRequest request ) throws RequestFormatException
    {
        if (!StringUtils.isBlank(request.getConnectionId())) {
            return;
        }
        final List<SearchAttribute> providedAttributes = request.getSearch( ).getAttributes( );
        final Set<String> providedKeys = commonKeytoKey( providedAttributes.stream( ).map( SearchAttribute::getKey ).collect( Collectors.toSet( ) ) );

        boolean hasRequirements = false;
        final List<IdentitySearchRule> searchRules = IdentitySearchRuleHome.findAll( );
        final Iterator<IdentitySearchRule> iterator = searchRules.iterator( );
        while ( !hasRequirements && iterator.hasNext( ) )
        {
            final IdentitySearchRule searchRule = iterator.next( );
            final Set<String> requiredKeys = searchRule.getAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) );
            if ( searchRule.getType( ) == SearchRuleType.AND )
            {
                if ( providedKeys.containsAll( requiredKeys ) )
                {
                    hasRequirements = true;
                }
            }
            else
                if ( searchRule.getType( ) == SearchRuleType.OR )
                {
                    if ( providedKeys.stream( ).anyMatch( requiredKeys::contains ) )
                    {
                        hasRequirements = true;
                    }
                }
        }

        if ( !hasRequirements )
        {
            final StringBuilder sb = new StringBuilder( );
            final Iterator<IdentitySearchRule> ruleIt = searchRules.iterator( );
            while ( ruleIt.hasNext( ) )
            {
                final IdentitySearchRule rule = ruleIt.next( );
                sb.append( "( " );
                final Iterator<AttributeKey> attrIt = rule.getAttributes( ).iterator( );
                while ( attrIt.hasNext( ) )
                {
                    final AttributeKey attr = attrIt.next( );
                    sb.append( attr.getKeyName( ) ).append( " " );
                    if ( attrIt.hasNext( ) )
                    {
                        sb.append( rule.getType( ).name( ) ).append( " " );
                    }
                }
                sb.append( ")" );
                if ( ruleIt.hasNext( ) )
                {
                    sb.append( " OR " );
                }
            }
            final IdentitySearchMessage alert = new IdentitySearchMessage( );
            alert.setAttributeName( sb.toString( ) );
            alert.setMessage( "Please provide those required attributes to be able to search identities." );
            throw new RequestFormatException( "The request doesn't contain the mandatory attributes to perform a search",
                    Constants.PROPERTY_REST_ERROR_MISSING_MANDATORY_ATTRIBUTES, Collections.singletonList( alert ) );
        }
    }

    /***
     * Check if the attributes are commonKeys, in that case it change them to the attributeKeys it refer
     *
     * @param providedAttributes
     * @return the list of keys
     */
    private Set<String> commonKeytoKey( final Set<String> providedAttributes )
    {
        final Set<String> returnKeys = new HashSet<>( );

        for ( final String attribute : providedAttributes )
        {
            final List<AttributeKey> keys = IdentityAttributeService.instance().getCommonAttributeKeys(attribute);
            if ( keys != null && !keys.isEmpty( ) )
            {
                for ( final AttributeKey key : keys )
                {
                    returnKeys.add( key.getKeyName( ) );
                }
            }
            else
            {
                returnKeys.add( attribute );
            }
        }
        return returnKeys;
    }

}

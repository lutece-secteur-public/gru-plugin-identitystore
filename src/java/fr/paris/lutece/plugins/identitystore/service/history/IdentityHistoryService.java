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
package fr.paris.lutece.plugins.identitystore.service.history;

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.cache.IdentityHistoryStatusCache;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistory;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class IdentityHistoryService
{
    private static IdentityHistoryService _instance;

    private final IdentityHistoryStatusCache _identityHistoryCache = SpringContextService.getBean( "identitystore.identityHistoryCache" );
    private final String HISTORY_STATUS_LIST = "historyStatusList";

    public static IdentityHistoryService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityHistoryService( );
            _instance._identityHistoryCache.refresh();
        }
        return _instance;
    }

    public IdentityHistory get( final String customerId, final ServiceContract serviceContract ) throws IdentityStoreException
    {
        final Set<String> readableAttributeKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isReadable )
                .map( ar -> ar.getAttributeKey( ).getKeyName( ) ).collect( Collectors.toSet( ) );

        final List<IdentityChange> identityChangeList = IdentityHome.findHistoryByCustomerId( customerId );
        if ( !serviceContract.getAuthorizedAgentHistoryRead( ) )
        {
            identityChangeList.removeIf( identityChange -> Objects.equals( identityChange.getChangeType( ), IdentityChangeType.READ ) );
        }
        final List<AttributeChange> attributeChangeList = IdentityAttributeHome.getAttributeChangeHistory( customerId );
        if ( identityChangeList.isEmpty( ) && attributeChangeList.isEmpty( ) )
        {
            throw new ResourceNotFoundException( "No history found", Constants.PROPERTY_REST_ERROR_NO_HISTORY_FOUND );
        }

        return toHistory( customerId, identityChangeList, attributeChangeList, readableAttributeKeys );
    }

    public List<IdentityHistory> search( final IdentityHistorySearchRequest request, final ServiceContract serviceContract ) throws IdentityStoreException
    {
        final List<IdentityHistory> resultList = new ArrayList<>( );
        final Set<String> readableAttributeKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isReadable )
                .map( ar -> ar.getAttributeKey( ).getKeyName( ) ).collect( Collectors.toSet( ) );

        final List<IdentityChange> identityChangeList = IdentityHome.findHistoryBySearchParameters( request.getCustomerId( ), request.getClientCode( ),
                request.getAuthorName( ), request.getIdentityChangeType( ), request.getChangeStatus( ), null, null, request.getMetadata( ), request.getNbDaysFrom( ),
                Pair.of( request.getModificationDateIntervalStart( ), request.getModificationDateIntervalEnd( ) ), 0 );
        if ( !serviceContract.getAuthorizedAgentHistoryRead( ) )
        {
            identityChangeList.removeIf( identityChange -> Objects.equals( identityChange.getChangeType( ), IdentityChangeType.READ ) );
        }
        final Map<String, List<IdentityChange>> identityChangeMap = identityChangeList.stream( )
                .collect( Collectors.groupingBy( IdentityChange::getCustomerId ) );

        // TODO refactorer dans le cas d'une recherche par metadata la liste est beaucoup trop grande pour faire des appels successifs à la BDD.
        for ( final String customerId : identityChangeMap.keySet( ) )
        {
            final List<AttributeChange> attributeChangeList = IdentityAttributeHome.getAttributeChangeHistory( customerId );
            resultList.add( this.toHistory( customerId, identityChangeMap.get( customerId ), attributeChangeList, readableAttributeKeys ) );
        }

        if ( resultList.isEmpty( ) )
        {
            throw new ResourceNotFoundException( "No history found", Constants.PROPERTY_REST_ERROR_NO_HISTORY_FOUND );
        }

        return resultList;
    }

    private IdentityHistory toHistory( final String customerId, final List<IdentityChange> identityChangeList, final List<AttributeChange> attributeChangeList,
            final Set<String> readableAttributeKeys )
    {
        final IdentityHistory history = new IdentityHistory( );
        history.setCustomerId( customerId );
        history.getIdentityChanges( ).addAll( identityChangeList );
        attributeChangeList.stream( ).filter( ac -> readableAttributeKeys.contains( ac.getAttributeKey( ) ) )
                .collect( Collectors.groupingBy( AttributeChange::getAttributeKey ) ).forEach( ( key, attributeChanges ) -> {
                    final AttributeHistory attributeHistory = new AttributeHistory( );
                    attributeHistory.setAttributeKey( key );
                    attributeHistory.setAttributeChanges( attributeChanges );
                    history.getAttributeHistories( ).add( attributeHistory );
                } );
        return history;
    }

    public List<String> getStatusList()
    {
        return _identityHistoryCache.getStatusList(HISTORY_STATUS_LIST);
    }
}

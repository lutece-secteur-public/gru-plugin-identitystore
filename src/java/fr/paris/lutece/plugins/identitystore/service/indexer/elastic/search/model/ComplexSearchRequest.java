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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.Bool;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.BoolContainer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.Exists;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.ExistsContainer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.InnerSearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * An Elastic search bool request based on specific use cases for IdentityStore.
 */
public class ComplexSearchRequest extends ASearchRequest
{
    private final boolean connected;

    public ComplexSearchRequest( final List<SearchAttribute> attributes, final boolean connected, final List<String> attributesFilter )
    {
        super( attributesFilter );
        this.getSearchAttributes( ).addAll( attributes );
        this.connected = connected;
    }

    @Override
    public InnerSearchRequest innerBody( )
    {
        final InnerSearchRequest body = new InnerSearchRequest( );

        this.getSearchAttributes( ).forEach( searchAttribute -> this.addToRequestBody( searchAttribute, body ) );

        if ( this.isConnected( ) )
        {
            final Exists connectionId = new Exists( );
            connectionId.setField( "connectionId" );
            body.getQuery( ).getBool( ).getMust( ).add( new ExistsContainer( connectionId ) );
            final Exists login = new Exists( );
            login.setField( "attributes.login.value" );
            body.getQuery( ).getBool( ).getMust( ).add( new ExistsContainer( login ) );
        }

        return body;
    }

    private void addToRequestBody( final SearchAttribute searchAttribute, final InnerSearchRequest body )
    {
        if( searchAttribute.getOutputKeys( ).size( ) > 1 )
        {
            this.handleMultiAttributeSearch( searchAttribute, body );
        }
        else
        {
            this.handleSingleAttributeSearch( searchAttribute, body );
        }
    }

    private void handleMultiAttributeSearch( final SearchAttribute searchAttribute, final InnerSearchRequest body )
    {
        if( searchAttribute.getOutputKeys( ).stream().anyMatch( this::isSpecialAttributeKey ) )
        {
            final BoolContainer shouldContainer = new BoolContainer( new Bool( ) );
            for( final String key : searchAttribute.getOutputKeys( ) )
            {
                final BoolContainer attributeCondition = new BoolContainer( new Bool( ) );
                this.addBoolCondition( key, searchAttribute.getValue(), searchAttribute.getTreatmentType(), attributeCondition.getBool(), body );
                shouldContainer.getBool().getShould().add(attributeCondition);
            }
            switch( searchAttribute.getTreatmentType( ) )
            {
                case DIFFERENT:
                case ABSENT:
                    body.getQuery( ).getBool( ).getMustNot( ).add( shouldContainer );
                    break;
                case STRICT:
                case APPROXIMATED:
                    body.getQuery( ).getBool( ).getMust( ).add( shouldContainer );
                    break;
                default:
                    break;
            }
        }
        else
        {
            switch( searchAttribute.getTreatmentType( ) )
            {
                case DIFFERENT:
                    body.getQuery( ).getBool( ).getMustNot( ).add( body.createMultiMatch( searchAttribute ) );
                    break;
                case STRICT:
                case APPROXIMATED:
                    body.getQuery( ).getBool( ).getMust( ).add( body.createMultiMatch( searchAttribute ) );
                    break;
                case ABSENT:
                    body.getQuery( ).getBool( ).getMustNot( ).add( body.createExists( searchAttribute.getTreatmentType( ), searchAttribute.getKey( ) ) );
                default:
                    break;
            }
        }
    }

    private void handleSingleAttributeSearch( final SearchAttribute searchAttribute, final InnerSearchRequest body )
    {
        this.addBoolCondition( searchAttribute.getKey(), searchAttribute.getValue(), searchAttribute.getTreatmentType(), body.getQuery().getBool(), body );
    }

    private void addBoolCondition( final String key, final String value, final AttributeTreatmentType treatmentType, final Bool boolContainer, final InnerSearchRequest body )
    {
        switch( key )
        {
            case Constants.PARAM_FAMILY_NAME:
                switch( treatmentType )
                {
                    case STRICT:
                        boolContainer.getMust( ).add( body.createMatchPhrase( treatmentType, key, value, true ) );
                        break;
                    case APPROXIMATED:
                        final String multipleHyphenToOne = value.trim( ).replaceAll( "(-)\\1+", "$1" );
                        final String multipleSpacesToONe = multipleHyphenToOne.replaceAll( " +", " " );
                        final String trimmedHyphens = multipleSpacesToONe.replaceAll( " - ", "-" );
                        boolContainer.getMust( ).add( body.createMatch( treatmentType, trimmedHyphens, key ) );
                        break;
                    case DIFFERENT:
                        boolContainer.getMustNot( ).add( body.createMatchPhrase( treatmentType, key, value, false ) );
                        break;
                    case ABSENT:
                        boolContainer.getMustNot( ).add( body.createExists( treatmentType, key ) );
                    default:
                        break;
                }
                break;
            case Constants.PARAM_FIRST_NAME:
                switch( treatmentType )
                {
                    case STRICT:
                        boolContainer.getMust( ).add( body.createMatchPhrase( treatmentType, key, value, true ) );
                        break;
                    case APPROXIMATED:
                        final BoolContainer multiTokenApproximatedBoolQuery = body.createMultiTokenApproximatedBoolQuery( treatmentType, key, value.split( " " ) );
                        boolContainer.getMust( ).add(multiTokenApproximatedBoolQuery);
                        break;
                    case DIFFERENT:
                        final BoolContainer multiTokenDifferentBoolQuery = body.createMultiTokenDifferentBoolQuery( treatmentType, value, key, value.split( " " ) );
                        boolContainer.getMustNot( ).add( multiTokenDifferentBoolQuery );
                        break;
                    case ABSENT:
                        boolContainer.getMustNot( ).add( body.createExists( treatmentType, key ) );
                    default:
                        break;
                }
                break;
            case Constants.PARAM_PREFERRED_USERNAME:
                switch( treatmentType )
                {
                    case STRICT:
                        boolContainer.getMust( ).add( body.createMatchPhrase( treatmentType, key, value, true ) );
                        break;
                    case APPROXIMATED:
                        boolContainer.getMust( ).add( body.createMatch( treatmentType, value, key ) );
                        break;
                    case DIFFERENT:
                        boolContainer.getMustNot( ).add( body.createMatchPhrase( treatmentType, key, value, false ) );
                        break;
                    case ABSENT:
                        boolContainer.getMustNot( ).add( body.createExists( treatmentType, key ) );
                    default:
                        break;
                }
                break;
            default:
                switch( treatmentType )
                {
                    case DIFFERENT:
                        boolContainer.getMustNot( ).add( body.createMatch( treatmentType, value, key ) );
                        break;
                    case STRICT:
                    case APPROXIMATED:
                        boolContainer.getMust( ).add( body.createMatch( treatmentType, value, key ) );
                        break;
                    case ABSENT:
                        boolContainer.getMustNot( ).add( body.createExists( treatmentType, key ) );
                    default:
                        break;
                }
                break;
        }
    }

    private boolean isSpecialAttributeKey ( final String key )
    {
        return StringUtils.equalsAny( key, Constants.PARAM_FAMILY_NAME, Constants.PARAM_FIRST_NAME, Constants.PARAM_PREFERRED_USERNAME );
    }

    public boolean isConnected( )
    {
        return connected;
    }
}

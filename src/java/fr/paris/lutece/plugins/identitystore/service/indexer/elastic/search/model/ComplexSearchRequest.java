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

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.Exists;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.ExistsContainer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.InnerSearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;

import java.util.List;

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

        this.getSearchAttributes( ).forEach( searchAttribute -> {
            switch( searchAttribute.getKey( ) )
            {
                case Constants.PARAM_FAMILY_NAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            body.addMatchPhrase( searchAttribute, true );
                            break;
                        case APPROXIMATED:
                            final String multipleHyphenToOne = searchAttribute.getValue( ).trim( ).replaceAll( "(-)\\1+", "$1" );
                            final String multipleSpacesToONe = multipleHyphenToOne.replaceAll( " +", " " );
                            final String trimmedHyphens = multipleSpacesToONe.replaceAll( " - ", "-" );
                            searchAttribute.setValue( trimmedHyphens );
                            body.addMatch( searchAttribute, true );
                            break;
                        case DIFFERENT:
                            body.addMatchPhrase( searchAttribute, false );
                            break;
                        case ABSENT:
                            body.addExists( searchAttribute, false );
                        default:
                            break;
                    }
                    break;
                case Constants.PARAM_FIRST_NAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            body.addMatchPhrase( searchAttribute, true );
                            break;
                        case APPROXIMATED:
                            searchAttribute.setValue( searchAttribute.getValue( ).trim( ).replaceAll( " +", " " ).toLowerCase( ) );
                            final String [ ] splitSearchValue = searchAttribute.getValue( ).split( " " );
                            if ( splitSearchValue.length > 1 )
                            {
                                body.addSpanNear( searchAttribute, true );
                            }
                            else
                            {
                                body.addMatch( searchAttribute, true );
                            }
                            break;
                        case DIFFERENT:
                            body.addMatchPhrase( searchAttribute, false );
                            break;
                        case ABSENT:
                            body.addExists( searchAttribute, false );
                        default:
                            break;
                    }
                    break;
                case Constants.PARAM_PREFERRED_USERNAME:
                    switch( searchAttribute.getTreatmentType( ) )
                    {
                        case STRICT:
                            body.addMatchPhrase( searchAttribute, true );
                            break;
                        case APPROXIMATED:
                            body.addMatch( searchAttribute, true );
                            break;
                        case DIFFERENT:
                            body.addMatchPhrase( searchAttribute, false );
                            break;
                        case ABSENT:
                            body.addExists( searchAttribute, false );
                        default:
                            break;
                    }
                    break;
                default:
                    if ( searchAttribute.getOutputKeys( ).size( ) == 1 )
                    {
                        switch( searchAttribute.getTreatmentType( ) )
                        {
                            case DIFFERENT:
                                body.addMatch( searchAttribute, false );
                                break;
                            case STRICT:
                            case APPROXIMATED:
                                body.addMatch( searchAttribute, true );
                                break;
                            case ABSENT:
                                body.addExists( searchAttribute, false );
                            default:
                                break;
                        }
                    }
                    else
                    {
                        switch( searchAttribute.getTreatmentType( ) )
                        {
                            case DIFFERENT:
                                body.addMultiMatch( searchAttribute, false );
                                break;
                            case STRICT:
                            case APPROXIMATED:
                                body.addMultiMatch( searchAttribute, true );
                                break;
                            case ABSENT:
                                body.addExists( searchAttribute, false );
                            default:
                                break;
                        }
                    }
                    break;
            }
        } );

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

    public boolean isConnected( )
    {
        return connected;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicSearchRequest
{
    @SearchCriteria( keys = {
            "email_login", "email"
    } )
    protected SearchAttribute _strEmail;
    @SearchCriteria( keys = {
            "gender"
    } )
    protected SearchAttribute _strGender;
    @SearchCriteria( keys = {
            "family_name"
    } )
    protected SearchAttribute _strFamilyName;
    @SearchCriteria( keys = {
            "first_name"
    } )
    protected SearchAttribute _strFirstName;
    @SearchCriteria( keys = {
            "preferred_username"
    } )
    protected SearchAttribute _strPreferredUserName;
    @SearchCriteria( keys = {
            "birthdate"
    } )
    protected SearchAttribute _dateBirthDate;
    @SearchCriteria( keys = {
            "insee_birthplace_label"
    } )
    protected SearchAttribute _strBirthPlace;
    @SearchCriteria( keys = {
            "insee_birthcountry_label"
    } )
    protected SearchAttribute _strBirthCountry;
    @SearchCriteria( keys = {
            "mobile_phone", "fixed_phone"
    } )
    protected SearchAttribute _strPhone;

    protected List<SearchAttribute> _listExtraAttributeValues = new ArrayList<>( );

    public BasicSearchRequest( final List<SearchAttribute> attributes )
    {
        final List<String> annotatedKeys = new ArrayList<>( );
        Arrays.stream( this.getClass( ).getDeclaredFields( ) ).forEach( field -> {
            try
            {
                field.setAccessible( true );
                final SearchCriteria annotation = field.getAnnotation( SearchCriteria.class );
                if ( annotation != null )
                {
                    final List<String> fieldKeys = Arrays.asList( annotation.keys( ) );
                    annotatedKeys.addAll( fieldKeys );
                    final Optional<SearchAttribute> match = attributes.stream( ).filter( attribute -> fieldKeys.contains( attribute.getKey( ) ) ).findFirst( );
                    if ( match.isPresent( ) )
                    {
                        field.set( this, match.get( ) );
                    }
                }
            }
            catch( IllegalAccessException e )
            {
                throw new RuntimeException( e );
            }
        } );
        final List<SearchAttribute> extraAttributes = attributes.stream( ).filter( searchAttribute -> !annotatedKeys.contains( searchAttribute.getKey( ) ) )
                .collect( Collectors.toList( ) );
        this.getExtraAttributeValues( ).addAll( extraAttributes );
    }

    public InnerSearchRequest body( )
    {
        final InnerSearchRequest body = new InnerSearchRequest( );
        body.setFrom( 0 );
        body.setSize( 10 ); // TODO improve by setting size by property
        final Query query = new Query( );
        final Bool bool = new Bool( );
        final ArrayList<AbstractContainer> must = new ArrayList<>( );
        if ( _strEmail != null )
        {
            must.add( new MatchContainer( getMatch( _strEmail ) ) );
        }
        if ( _strGender != null )
        {
            must.add( new MatchContainer( getMatch( _strGender ) ) );
        }
        if ( _strFamilyName != null )
        {
            must.add( new MatchContainer( getMatch( _strFamilyName ) ) );
        }
        if ( _strFirstName != null )
        {
            if ( _strFirstName.isStrict( ) )
            {
                must.add( new MatchPhraseContainer( getMatchPhrase( _strFirstName ) ) );
            }
            else
            {
                must.add( new MatchContainer( getMatch( _strFirstName ) ) );
            }
        }
        if ( _strPreferredUserName != null )
        {
            must.add( new MatchContainer( getMatch( _strPreferredUserName ) ) );
        }
        if ( _dateBirthDate != null )
        {
            must.add( new MatchContainer( getMatch( _dateBirthDate ) ) );
        }
        if ( _strBirthPlace != null )
        {
            must.add( new MatchContainer( getMatch( _strBirthPlace ) ) );
        }
        if ( _strBirthCountry != null )
        {
            must.add( new MatchContainer( getMatch( _strBirthCountry ) ) );
        }
        if ( _strPhone != null )
        {
            must.add( new MatchContainer( getMatch( _strPhone ) ) );
        }
        if ( CollectionUtils.isNotEmpty( _listExtraAttributeValues ) )
        {
            _listExtraAttributeValues.forEach( searchAttribute -> must.add( new MatchContainer( getMatch( searchAttribute ) ) ) );
        }
        bool.setMust( must );
        query.setBool( bool );
        body.setQuery( query );
        return body;
    }

    private Match getMatch( final SearchAttribute attribute )
    {
        final Match match = new Match( );
        match.setName( "attributes." + attribute.getKey( ) + ".value" );
        match.setQuery( attribute.getValue( ) );
        if ( !attribute.isStrict( ) )
        {
            match.setFuzziness( "1" );
        }
        return match;
    }

    private MatchPhrase getMatchPhrase( final SearchAttribute attribute )
    {
        final MatchPhrase match = new MatchPhrase( );
        match.setName( "attributes." + attribute.getKey( ) + ".value" );
        match.setQuery( attribute.getValue( ) );
        return match;
    }

    public SearchAttribute getEmail( )
    {
        return _strEmail;
    }

    public void setEmail( SearchAttribute _strEmail )
    {
        this._strEmail = _strEmail;
    }

    public SearchAttribute getGender( )
    {
        return _strGender;
    }

    public void setGender( SearchAttribute _strGender )
    {
        this._strGender = _strGender;
    }

    public SearchAttribute getFamilyName( )
    {
        return _strFamilyName;
    }

    public void setFamilyName( SearchAttribute _strFamilyName )
    {
        this._strFamilyName = _strFamilyName;
    }

    public SearchAttribute getFirstName( )
    {
        return _strFirstName;
    }

    public void setFirstName( SearchAttribute _strFirstName )
    {
        this._strFirstName = _strFirstName;
    }

    public SearchAttribute getPreferredUserName( )
    {
        return _strPreferredUserName;
    }

    public void setPreferredUserName( SearchAttribute _strPreferredUserName )
    {
        this._strPreferredUserName = _strPreferredUserName;
    }

    public SearchAttribute getDateBirthDate( )
    {
        return _dateBirthDate;
    }

    public void setDateBirthDate( SearchAttribute _dateBirthDate )
    {
        this._dateBirthDate = _dateBirthDate;
    }

    public SearchAttribute getBirthPlace( )
    {
        return _strBirthPlace;
    }

    public void setBirthPlace( SearchAttribute _strBirthPlace )
    {
        this._strBirthPlace = _strBirthPlace;
    }

    public SearchAttribute getBirthCountry( )
    {
        return _strBirthCountry;
    }

    public void setBirthCountry( SearchAttribute _strBirthCountry )
    {
        this._strBirthCountry = _strBirthCountry;
    }

    public SearchAttribute getPhone( )
    {
        return _strPhone;
    }

    public void setPhone( SearchAttribute _strPhone )
    {
        this._strPhone = _strPhone;
    }

    public List<SearchAttribute> getExtraAttributeValues( )
    {
        return _listExtraAttributeValues;
    }
}

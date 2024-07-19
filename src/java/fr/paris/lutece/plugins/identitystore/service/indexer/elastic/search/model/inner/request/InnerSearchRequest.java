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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonInclude( JsonInclude.Include.NON_EMPTY )
public class InnerSearchRequest
{
    protected Integer from;

    protected Integer size;

    @JsonProperty( "query" )
    protected Query query;

    @JsonProperty( "_source" )
    protected List<String> sourceFilters = new ArrayList<>( );

    public InnerSearchRequest( )
    {
        final Query query = new Query( );
        this.setQuery( query );
        final Bool bool = new Bool( );
        query.setBool( bool );
    }

    @JsonIgnore
    protected Map<String, String> metadata = new HashMap<>( );

    public Integer getFrom( )
    {
        return from;
    }

    public void setFrom( Integer from )
    {
        this.from = from;
    }

    public Integer getSize( )
    {
        return size;
    }

    public void setSize( Integer size )
    {
        this.size = size;
    }

    public Query getQuery( )
    {
        return query;
    }

    public void setQuery( Query query )
    {
        this.query = query;
    }

    public Map<String, String> getMetadata( )
    {
        return metadata;
    }

    public void setMetadata( Map<String, String> metadata )
    {
        this.metadata = metadata;
    }

    public List<String> getSourceFilters( )
    {
        return sourceFilters;
    }

    public void setSourceFilters( List<String> sourceFilters )
    {
        this.sourceFilters = sourceFilters;
    }

    public void addMatch( final SearchAttribute attribute, final boolean must )
    {
        final Match match = new Match( );
        final String attributeKey = attribute.getOutputKeys( ).get( 0 );
        match.setName( "attributes." + attributeKey + ".value" );
        match.setQuery( attribute.getValue( ) );
        if ( AttributeTreatmentType.APPROXIMATED.equals( attribute.getTreatmentType( ) ) )
        {
            match.setFuzziness( "1" );
        }
        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attributeKey ) );
        this.addContainer( new MatchContainer( match ), must );
    }

    public void addMultiMatch( final SearchAttribute attribute, final boolean must )
    {
        final MultiMatch match = new MultiMatch( );
        match.setFields( attribute.getOutputKeys( ).stream( ).map( outputKey -> "attributes." + outputKey + ".value" ).collect( Collectors.toList( ) ) );
        match.setQuery( attribute.getValue( ) );
        if ( AttributeTreatmentType.APPROXIMATED.equals( attribute.getTreatmentType( ) ) )
        {
            match.setFuzziness( "1" );
        }
        this.addMetadata( attribute.getTreatmentType( ).name( ), attribute.getOutputKeys( ) );
        this.addContainer( new MultiMatchContainer( match ), must );
    }

    public void addMatchPhrase( final SearchAttribute attribute, final boolean must, final boolean raw )
    {
        final MatchPhrase match = new MatchPhrase( );
        final String attributeKey = attribute.getOutputKeys( ).get( 0 );
        match.setName( "attributes." + attributeKey + ".value" + (raw ? ".raw" : "") );
        match.setQuery( attribute.getValue( ) );
        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attributeKey ) );
        this.addContainer( new MatchPhraseContainer( match ), must );
    }

    public void addSpanNear( final SearchAttribute attribute, final boolean must )
    {
        final SpanNear spanNear = new SpanNear( );
        final String [ ] splitSearchValue = attribute.getValue( ).split( " " );
        spanNear.setSlop( splitSearchValue.length - 1 );
        Arrays.stream( splitSearchValue ).forEach( word -> spanNear.getClauses( )
                .add( new SpanMultiContainer( new SpanMulti( new SpanMultiFuzzyMatchContainer( this.createSpanMultiFuzzyMatch( attribute, word ) ) ) ) ) );
        spanNear.setInOrder( true );
        spanNear.setBoost( 1 );
        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attribute.getKey( ) ) );
        this.addContainer( new SpanNearContainer( spanNear ), must );
    }

    private SpanMultiFuzzyMatch createSpanMultiFuzzyMatch( SearchAttribute attribute, String value )
    {
        final SpanMultiFuzzyMatch multiMatch = new SpanMultiFuzzyMatch( );
        multiMatch.setName( "attributes." + attribute.getKey( ) + ".value" );
        multiMatch.setFuzziness( "1" );
        multiMatch.setValue( value );
        return multiMatch;
    }

    /**
     * Creates a particular match for approximated search on multi token fields (as first name), following the given rules:
     * <ul>
     *     <li>There is at least one searched value in the targeted field (can be fuzzy)</li>
     *     <li>There is only one fuzzy value at a time in the targeted field among the searched values</li>
     * </ul>
     * @param attribute the given attribute search
     */
    public void addApproximatedBoolQuery( final SearchAttribute attribute )
    {
        final String attributeKey = attribute.getOutputKeys( ).get( 0 );
        final String [ ] splitSearchValue = attribute.getValue( ).split( " " );

        /* Create a bool container to be added in must clause, to hold the should clause that will contain all combinations of approximated rules */
        final BoolContainer shouldContainer = this.createApproximatedShouldContainer( splitSearchValue, attributeKey );
        this.query.getBool( ).getMust( ).add( shouldContainer );

        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attribute.getKey( ) ) );
    }

    /**
     * Creates a particular match for different search on multi token fields (as first name), following the given rules:
     * <ul>
     *     <li>There is no match</li>
     *     <li>There is at least one searched value not matching any token</li>
     *     <li>There is more than one fuzzy match</li>
     * </ul>
     * If one of these rules is matching, this is a difference.
     * @param attribute the given attribute search
     */
    public void addDifferentBoolQuery( final SearchAttribute attribute )
    {
        final String attributeKey = attribute.getOutputKeys( ).get( 0 );
        final String [ ] splitSearchValue = attribute.getValue( ).split( " " );

        /* Create a bool container to be added in must clause, to hold the should clause that will contain all combinations of approximated rules */
        final BoolContainer shouldContainer = this.createApproximatedShouldContainer( splitSearchValue, attributeKey );
        this.query.getBool( ).getMustNot( ).add(shouldContainer);

        /* Remove strict matches */
        final BoolContainer strictMatchContainer = new BoolContainer( new Bool( ) );
        shouldContainer.getBool( ).getShould( ).add( strictMatchContainer );
        final Match strictMatch = new Match( );
        strictMatchContainer.getBool( ).getMust( ).add( new MatchContainer( strictMatch ) );
        strictMatch.setName( "attributes." + attributeKey + ".value.raw" );
        strictMatch.setQuery( attribute.getValue( ) );

        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attribute.getKey( ) ) );
    }

    private BoolContainer createApproximatedShouldContainer( final String [ ] splitSearchValue, final String attributeKey )
    {
        final BoolContainer shouldContainer = new BoolContainer( new Bool( ) );
        for ( final String currentValue : splitSearchValue )
        {
            /* Create combinations of fuzziness on a single token, and strict on others */
            final BoolContainer combinations = new BoolContainer( new Bool( ) );
            shouldContainer.getBool().getShould().add(combinations);
            final List<MatchContainer> must = Arrays.stream( splitSearchValue ).map( value -> {
                final Match match = new Match( );
                match.setName( "attributes." + attributeKey + ".value" );
                match.setQuery( value );
                if ( Objects.equals( value, currentValue ) )
                {
                    match.setFuzziness( "1" );
                }
                return new MatchContainer( match );
            } ).collect( Collectors.toList( ) );
            combinations.getBool( ).getMust( ).addAll( must );

            /* Create raw fuzziness match on current token  */
            if( splitSearchValue.length > 1 )
            {
                final BoolContainer single = new BoolContainer( new Bool( ) );
                shouldContainer.getBool( ).getShould( ).add( single );
                final Match rawMatch = new Match( );
                single.getBool( ).getMust( ).add( new MatchContainer( rawMatch ) );
                rawMatch.setName( "attributes." + attributeKey + ".value.raw" );
                rawMatch.setFuzziness( "1" );
                rawMatch.setQuery( currentValue );
            }
        }

        return shouldContainer;
    }

    public void addExists( final SearchAttribute attribute, final boolean must )
    {
        final Exists exists = new Exists( );
        exists.setField( "attributes." + attribute.getKey( ) + ".value" );
        this.addMetadata( attribute.getTreatmentType( ).name( ), Collections.singletonList( attribute.getKey( ) ) );
        this.addContainer( new ExistsContainer( exists ), must );
    }

    private void addContainer( final AbstractContainer container, final boolean must )
    {
        if ( must )
        {
            query.getBool( ).getMust( ).add( container );
        }
        else
        {
            query.getBool( ).getMustNot( ).add( container );
        }
    }

    private void addMetadata( final String key, final List<String> newValues )
    {
        final String value = metadata.get( key );
        if ( StringUtils.isEmpty( value ) )
        {
            metadata.put( key, String.join( ",", newValues ) );
        }
        else
        {
            final List<String> existingValues = Arrays.asList( value.split( "," ) );
            metadata.put( key, Stream.concat( existingValues.stream( ), newValues.stream( ) ).distinct( ).collect( Collectors.joining( "," ) ) );
        }
    }
}

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

    /**
     * Create a match container
     * @param treatmentType the attribute treatment type
     * @param attributeKey the attribute key to be searched
     * @param value the value to be searched
     * @return a {@link MatchContainer}
     */
    public MatchContainer createMatch( final AttributeTreatmentType treatmentType, final String value, final String attributeKey )
    {
        final Match match = new Match( );
        match.setName( "attributes." + attributeKey + ".value" );
        match.setQuery( value );
        if ( AttributeTreatmentType.APPROXIMATED.equals( treatmentType ) )
        {
            match.setFuzziness( "1" );
        }
        this.addMetadata( treatmentType.name( ), Collections.singletonList( attributeKey ) );
        return new MatchContainer( match );
    }

    /**
     * Create a multi match query
     * @param attribute the searched attribute
     * @return a {@link MultiMatchContainer}
     */
    public MultiMatchContainer createMultiMatch( final SearchAttribute attribute )
    {
        final MultiMatch match = new MultiMatch( );
        match.setFields( attribute.getOutputKeys( ).stream( ).map( outputKey -> "attributes." + outputKey + ".value" ).collect( Collectors.toList( ) ) );
        match.setQuery( attribute.getValue( ) );
        if ( AttributeTreatmentType.APPROXIMATED.equals( attribute.getTreatmentType( ) ) )
        {
            match.setFuzziness( "1" );
        }
        this.addMetadata( attribute.getTreatmentType( ).name( ), attribute.getOutputKeys( ) );
        return new MultiMatchContainer( match );
    }

    /**
     * Create a match phrase query
     * @param treatmentType the attribute treatment type
     * @param attributeKey the attribute key
     * @param value the value to be searched
     * @param raw if true the search is performed on the raw value instead of the tokenized value
     */
    public AbstractContainer createMatchPhrase( final AttributeTreatmentType treatmentType, final String attributeKey, final String value, final boolean raw )
    {
        final MatchPhrase match = new MatchPhrase( );
        match.setName( "attributes." + attributeKey + ".value" + (raw ? ".raw" : "") );
        match.setQuery( value );
        this.addMetadata( treatmentType.name( ), Collections.singletonList( attributeKey ) );
        return new MatchPhraseContainer( match );
    }

    /**
     * Creates a particular match for approximated search on multi token fields (as first name), following the given rules:
     * <ul>
     *     <li>There is at least one searched value in the targeted field (can be fuzzy)</li>
     *     <li>There is only one fuzzy value at a time in the targeted field among the searched values</li>
     * </ul>
     *
     * @param attributeTreatment the attribute treatment type
     * @param attributeKey the attribute key
     * @param values the list of values to be searched
     * @return a {@link BoolContainer}
     */
    public BoolContainer createMultiTokenApproximatedBoolQuery( final AttributeTreatmentType attributeTreatment, final String attributeKey, final String [ ] values)
    {
        this.addMetadata( attributeTreatment.name( ), Collections.singletonList( attributeKey ) );

        /* Create a bool container to be added in a clause, to hold the should clause that will contain all combinations of approximated rules */
        return this.createMutliTokenApproximatedShouldContainer(values, attributeKey );
    }

    /**
     * Creates a particular match for different search on multi token fields (as first name), following the given rules:
     * <ul>
     *     <li>There is no match</li>
     *     <li>There is at least one searched value not matching any token</li>
     *     <li>There is more than one fuzzy match</li>
     * </ul>
     * If one of these rules is matching, this is a difference.
     *
     * @param attributeTreatment the attribute treatment type
     * @return a {@link BoolContainer}
     */
    public BoolContainer createMultiTokenDifferentBoolQuery( final AttributeTreatmentType attributeTreatment, final String value, final String attributeKey, final String [ ] splitSearchValue )
    {
        /* Create a bool container to be added in must clause, to hold the should clause that will contain all combinations of approximated rules */
        final BoolContainer shouldContainer = this.createMutliTokenApproximatedShouldContainer( splitSearchValue, attributeKey );

        /* Remove strict matches */
        final BoolContainer strictMatchContainer = new BoolContainer( new Bool( ) );
        shouldContainer.getBool( ).getShould( ).add( strictMatchContainer );
        final Match strictMatch = new Match( );
        strictMatchContainer.getBool( ).getMust( ).add( new MatchContainer( strictMatch ) );
        strictMatch.setName( "attributes." + attributeKey + ".value.raw" );
        strictMatch.setQuery( value );

        this.addMetadata( attributeTreatment.name( ), Collections.singletonList( attributeKey ) );

        return shouldContainer;
    }

    /**
     * Creates all combination of approximated searches on multi tokenized attribute
     * @param splitSearchValue the list of searched values
     * @param attributeKey the searched attribute key
     * @return a {@link BoolContainer}
     */
    private BoolContainer createMutliTokenApproximatedShouldContainer(final String [ ] splitSearchValue, final String attributeKey )
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

    /**
     * Create an exists query
     * @param attributeTreatment the attribute treatment
     * @param attributeKey the attribute key to be checked
     * @return an {@link ExistsContainer}
     */
    public ExistsContainer createExists( final AttributeTreatmentType attributeTreatment, final String attributeKey )
    {
        final Exists exists = new Exists( );
        exists.setField( "attributes." + attributeKey + ".value" );
        this.addMetadata( attributeTreatment.name( ), Collections.singletonList( attributeKey ) );
        return new ExistsContainer( exists );
    }

    /**
     * Add a metadata describing the given key, such as the type of match in the request for each attribute
     * @param key the subject
     * @param newValues the description of the subject
     */
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

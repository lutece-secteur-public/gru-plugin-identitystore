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
package fr.paris.lutece.plugins.identitystore;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.paris.lutece.plugins.identitystore.data.TestAttribute;
import fr.paris.lutece.plugins.identitystore.data.TestDefinition;
import fr.paris.lutece.plugins.identitystore.data.TestIdentity;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public abstract class IdentityStoreJsonDataTestCase extends IdentityStoreBDDAndESTestCase
{
    protected final Map<String, Pair<Boolean, String>> results = new HashMap<>( );
    protected final Set<File> testDefinitions = new HashSet<>( );

    @Override
    protected void preInitApplication( ) throws IOException
    {
        final Path inputsPath = Paths.get( basePath, this.getTestDataPath( ), "definition" );
        final Path configPath = Paths.get( this.getTestDataPath( ), "config/identitystore.properties" );
        this.propertiesTemplatePath = configPath.toString( );

        final PathMatcher jsonMatcher = FileSystems.getDefault( ).getPathMatcher( "glob:**/*.json" );
        Files.walkFileTree( inputsPath, new SimpleFileVisitor<Path>( )
        {
            @Override
            public FileVisitResult visitFile( Path path, BasicFileAttributes attrs )
            {
                if ( !Files.isDirectory( path ) && jsonMatcher.matches( path ) )
                {
                    testDefinitions.add( path.toFile( ) );
                }
                return FileVisitResult.CONTINUE;
            }
        } );
    }

    protected abstract String getTestDataPath( );

    protected void execute( )
    {

        if ( testDefinitions != null )
        {
            final ObjectMapper mapper = new ObjectMapper( );
            mapper.enable( SerializationFeature.INDENT_OUTPUT );
            mapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
            mapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
            mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

            testDefinitions.stream( ).sorted( Comparator.comparing( File::getName ) ).forEach( file -> {
                try
                {
                    final TestDefinition testDefinition = mapper.readValue( file, TestDefinition.class );
                    if ( testDefinition != null )
                    {
                        this.runDefinition( testDefinition );
                        this.clearData( );
                    }
                    else
                    {
                        throw new RuntimeException( "ERROR " + file.getName( ) + " : JSON is empty" );
                    }
                }
                catch( Exception e )
                {
                    throw new RuntimeException( e );
                }
            } );
            System.out.println( "----- Global test Results -----" );
            results.keySet( ).stream( ).sorted( String::compareTo )
                    .forEach( s -> System.out.println( s + " :: " + ( results.get( s ).getLeft( ) ? "OK" : "KO" ) + "\n" + results.get( s ).getRight( ) ) );
            assertTrue( results.values( ).stream( ).allMatch( Pair::getLeft ) );
        }
    }

    protected Pair<Boolean, String> getTestResult( final List<TestIdentity> result, final TestDefinition testDefinition )
    {
        // Get result names from inputs and sort attributes by key name
        testDefinition.getInputs( ).forEach( testIdentity -> testIdentity.getAttributes( ).sort( Comparator.comparing( TestAttribute::getKey ) ) );
        result.forEach( testIdentity -> {
            testIdentity.getAttributes( ).sort( Comparator.comparing( TestAttribute::getKey ) );
            testDefinition.getInputs( ).stream( ).filter( input -> input.equals( testIdentity ) ).forEach( input -> testIdentity.setName( input.getName( ) ) );
        } );
        testDefinition.getExpected( ).forEach( testIdentity -> testIdentity.getAttributes( ).sort( Comparator.comparing( TestAttribute::getKey ) ) );
        String message = "Liste des inputs : "
                + String.join( ", ", testDefinition.getInputs( ).stream( ).map( TestIdentity::getName ).collect( Collectors.toList( ) ) );
        message += "\nListe des expected : "
                + String.join( ", ", testDefinition.getExpected( ).stream( ).map( TestIdentity::getName ).collect( Collectors.toList( ) ) );
        message += "\nListe des résultats : " + String.join( ", ", result.stream( ).map( TestIdentity::getName ).collect( Collectors.toList( ) ) );
        if ( result.size( ) > testDefinition.getExpected( ).size( ) )
        {
            result.removeAll( testDefinition.getExpected( ) );
            message += "\nLe résultat de la recherche contient " + result.size( ) + " identité(s) de plus que l'expected.";
            return new MutablePair<>( false, message );
        }
        else
            if ( result.size( ) < testDefinition.getExpected( ).size( ) )
            {
                testDefinition.getExpected( ).removeAll( result );
                message += "\nL'expected contient " + testDefinition.getExpected( ).size( ) + " identité(s) qui n'ont pas été retournée(s) par la recherche.";
                return new MutablePair<>( false, message );
            }
            else
            { // if equals
                final List<TestIdentity> expectedCopy = new ArrayList<>( testDefinition.getExpected( ) );
                final List<TestIdentity> resultCopy = new ArrayList<>( result );
                testDefinition.getExpected( ).removeAll( result );
                if ( testDefinition.getExpected( ).isEmpty( ) )
                {
                    message += "\nLe résultat de la recherche match parfaitement l'expected.";
                    return new MutablePair<>( true, message );
                }
                else
                {
                    message += "\nL'expected contient " + testDefinition.getExpected( ).size( )
                            + " identité(s) qui n'ont pas été retournée(s) par la recherche.";
                    resultCopy.removeAll( expectedCopy );
                    if ( !resultCopy.isEmpty( ) )
                    {
                        message += "\nLe résultat de la recherche contient " + resultCopy.size( ) + " identité(s) non définie(s) dans l'expected.";
                    }
                    return new MutablePair<>( false, message );
                }
            }
    }

    protected abstract void runDefinition( TestDefinition testDefinition ) throws Exception;

    protected abstract void clearData( ) throws Exception;
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.paris.lutece.plugins.identitystore.IdentityStoreJsonDataTestCase;
import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.data.TestAttribute;
import fr.paris.lutece.plugins.identitystore.data.TestDefinition;
import fr.paris.lutece.plugins.identitystore.data.TestIdentity;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IdentityIndexer;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IdentitySearcherTest extends IdentityStoreJsonDataTestCase
{

    @Override
    protected String getTestDataPath( )
    {
        return "data/search";
    }

    public void testResearch( )
    {
        super.execute( );
    }

    @Override
    protected void runDefinition( final TestDefinition testDefinition ) throws Exception
    {
        System.out.println( "----- Running test definition: " + testDefinition.getName( ) + " -----" );
        System.out.println( "----- Init test data -----" );
        testDefinition.getInputs( ).stream( )
                .map( testIdentity -> new ImmutablePair<>( testIdentity.getName( ), this.toIdentityChangeRequest( testIdentity ) ) ).forEach( pair -> {
                    try
                    {
                        final IdentityChangeResponse response = new IdentityChangeResponse( );
                        IdentityService.instance( ).create( pair.getRight( ), IdentityStoreTestContext.SAMPLE_APPCODE, response );
                        if ( response.getStatus( ) != IdentityChangeStatus.CREATE_SUCCESS )
                        {
                            System.out.println( "Erreur lors de la création de " + pair.getLeft( ) + " :: Status " + response.getStatus( ) + " :: Message "
                                    + response.getMessage( ) );
                        }
                    }
                    catch( IdentityStoreException e )
                    {
                        throw new RuntimeException( e );
                    }
                } );
        System.out.println( "----- Execute search request -----" );
        Thread.sleep( 1000 );
        final IdentitySearchResponse identitySearchResponse = new IdentitySearchResponse( );
        try
        {
            IdentityService.instance( ).search( this.toIdentitySearchResponse( testDefinition.getTest( ) ), identitySearchResponse,
                    IdentityStoreTestContext.SAMPLE_APPCODE );
        }
        catch( ServiceContractNotFoundException e )
        {
            throw new RuntimeException( e );
        }
        final List<TestIdentity> result = identitySearchResponse.getIdentities( ).stream( ).map( this::toTestIdentity ).collect( Collectors.toList( ) );
        results.put( testDefinition.getName( ), this.getTestResult( result, testDefinition ) );
    }

    protected void clearData( ) throws Exception
    {
        System.out.println( "----- Clear test data -----" );
        /* Clean BDD tables */
        System.out.println( "----- Truncate BDD tables -----" );
        final DataSource ds = getDataSource( postgreSQLContainer );
        final Statement statement = ds.getConnection( ).createStatement( );
        statement.execute( "truncate table " + "    identitystore_identity," + "    identitystore_identity_attribute,"
                + "    identitystore_attribute_certificate," + "    identitystore_history_identity_attribute," + "    identitystore_index_action;" );

        /* Clean ES index */
        System.out.println( "----- Delete ES Index -----" );
        final IdentityIndexer identityIndexer = new IdentityIndexer( "http://" + elasticsearchContainer.getHttpHostAddress( ) );
        final String indexBehindAlias = identityIndexer.getIndexBehindAlias( IIdentityIndexer.CURRENT_INDEX_ALIAS );
        identityIndexer.deleteIndex( indexBehindAlias );
    }

    protected DataSource getDataSource( JdbcDatabaseContainer<?> container )
    {
        final HikariConfig hikariConfig = new HikariConfig( );
        hikariConfig.setJdbcUrl( container.getJdbcUrl( ) );
        hikariConfig.setUsername( container.getUsername( ) );
        hikariConfig.setPassword( container.getPassword( ) );
        hikariConfig.setDriverClassName( container.getDriverClassName( ) );
        return new HikariDataSource( hikariConfig );
    }

    private TestIdentity toTestIdentity( final QualifiedIdentity qualifiedIdentity )
    {
        final TestIdentity testIdentity = new TestIdentity( );
        testIdentity.setConnectionId( qualifiedIdentity.getConnectionId( ) );
        testIdentity.setCustomerId( qualifiedIdentity.getCustomerId( ) );
        qualifiedIdentity.getAttributes( ).forEach( certifiedAttribute -> {
            TestAttribute testAttribute = new TestAttribute( );
            testIdentity.getAttributes( ).add( testAttribute );
            testAttribute.setCertificationDate( certifiedAttribute.getCertificationDate( ) );
            testAttribute.setCertifier( certifiedAttribute.getCertifier( ) );
            testAttribute.setType( certifiedAttribute.getType( ) );
            testAttribute.setKey( certifiedAttribute.getKey( ) );
            testAttribute.setValue( certifiedAttribute.getValue( ) );
            testAttribute.setCertificationLevel( certifiedAttribute.getCertificationLevel( ) );

        } );
        return testIdentity;
    }

    private IdentityChangeRequest toIdentityChangeRequest( final TestIdentity testIdentity )
    {
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        final RequestAuthor origin = new RequestAuthor( );
        origin.setType( AuthorType.application );
        origin.setName( "Test unitaires" );
        identityChangeRequest.setOrigin( origin );
        final Identity identity = new Identity( );
        identityChangeRequest.setIdentity( identity );
        identity.setConnectionId( testIdentity.getConnectionId( ) );
        identity.setCustomerId( testIdentity.getCustomerId( ) );
        testIdentity.getAttributes( ).forEach( testAttribute -> {
            final CertifiedAttribute certifiedAttribute = new CertifiedAttribute( );
            identity.getAttributes( ).add( certifiedAttribute );
            certifiedAttribute.setValue( testAttribute.getValue( ) );
            certifiedAttribute.setKey( testAttribute.getKey( ) );
            certifiedAttribute.setCertificationDate( testAttribute.getCertificationDate( ) );
            certifiedAttribute.setCertificationProcess( testAttribute.getCertifier( ) );
        } );
        return identityChangeRequest;
    }

    private IdentitySearchRequest toIdentitySearchResponse( final TestIdentity testIdentity )
    {
        final IdentitySearchRequest identitySearchRequest = new IdentitySearchRequest( );
        final SearchDto search = new SearchDto( );
        search.setAttributes( new ArrayList<>( ) );
        identitySearchRequest.setSearch( search );
        testIdentity.getAttributes( ).forEach( testAttribute -> {
            final SearchAttributeDto searchAttributeDto = new SearchAttributeDto( );
            search.getAttributes( ).add( searchAttributeDto );
            searchAttributeDto.setKey( testAttribute.getKey( ) );
            searchAttributeDto.setValue( testAttribute.getValue( ) );
            searchAttributeDto.setStrict( !StringUtils.equalsAny( testAttribute.getKey( ), "first_name", "family_name", "preferred_username" ) );
        } );
        return identitySearchRequest;
    }
}

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

import org.testcontainers.containers.PostgreSQLContainer;

public abstract class IdentityStoreBDDTestCase extends AbstractIdentityStoreTestCase
{

    public static PostgreSQLContainer postgreSQLContainer;

    @Override
    protected void preInitApplication( )
    {
        // nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startContainers( )
    {
        System.out.println( "----- Set UP -----" );
        postgreSQLContainer = (PostgreSQLContainer) new PostgreSQLContainer( "postgres:".concat( IdentityStoreTestContext.POSTGRES_VERSION ) )
                .withDatabaseName( "idstore" ).withUsername( "idstore" ).withPassword( "idstore" ).withInitScript( "db/init.sql" ).withReuse( false );
        postgreSQLContainer.start( );
        this.dbUrl = postgreSQLContainer.getJdbcUrl( );
    }

    @Override
    protected void shutDownContainers( )
    {
        if ( postgreSQLContainer != null && postgreSQLContainer.isRunning( ) )
        {
            postgreSQLContainer.stop( );
            postgreSQLContainer = null;
        }
    }
}

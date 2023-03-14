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

import fr.paris.lutece.plugins.identitystore.util.IdentitystoreTestUtils;
import fr.paris.lutece.portal.service.init.AppInit;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.test.LuteceTestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractIdentityStoreTestCase extends LuteceTestCase
{

    protected String basePath = Paths.get( "src", "test", "resources" ).toFile( ).getAbsolutePath( );;
    protected String esUrl = "http://localhost:9200";
    protected String dbUrl = "http://localhost:5432/idstore";
    private final static String DB_PROPERTIES_PATH = "WEB-INF/conf/db.properties";
    protected String dbTemplatePath = "data/db.template";
    protected String propertiesTemplatePath = "data/identitystore.properties";
    private final static String PROPERTIES_PATH = "WEB-INF/conf/plugins/identitystore.properties";
    private final static String CONTEXT_XML_PATH = "WEB-INF/conf/plugins/identitystore_context.xml";
    protected String contextTemplatePath = "data/context.template";

    @Override
    protected void setUp( ) throws Exception
    {
        System.out.println( "----- Set UP -----" );
        this.startContainers( );
        this.preInitApplication( );
        this.initApplication( );
        System.out.println( this.getName( ) );
    }

    @Override
    protected void tearDown( ) throws Exception
    {
        System.out.println( "----- Tear Down -----" );
        super.tearDown( );

        this.shutDownContainers( );
        this.cleanApplication( );
    }

    protected abstract void startContainers( );

    protected abstract void shutDownContainers( );

    protected abstract void preInitApplication( ) throws IOException;

    protected void initApplication( ) throws Exception
    {
        /* Properties file */
        IdentitystoreTestUtils.generateFileFromTemplate( basePath, propertiesTemplatePath, new HashMap<>( ), PROPERTIES_PATH );

        /* Inject db url to db.properties template and save it to App destination */
        final Map<String, String> dbParams = new HashMap<>( );
        dbParams.put( "db_url", dbUrl );
        IdentitystoreTestUtils.generateFileFromTemplate( basePath, dbTemplatePath, dbParams, DB_PROPERTIES_PATH );

        /* Inject ES url to identitystore_context.xml template and save it to App destination */
        final Map<String, String> contextParams = new HashMap<>( );

        contextParams.put( "es_url", esUrl.contains( "http" ) ? esUrl : "http://" + esUrl );
        IdentitystoreTestUtils.generateFileFromTemplate( basePath, contextTemplatePath, contextParams, CONTEXT_XML_PATH );

        System.out.println( "-------------resourcesDir------------" + basePath );
        AppPathService.init( basePath );
        AppInit.initServices( "/WEB-INF/conf/" );

        _bInit = true;
        System.out.println( "Lutece services initialized" );
    };

    protected void cleanApplication( ) throws IOException
    {
        /* Clean context files */
        Files.delete( Paths.get( basePath, DB_PROPERTIES_PATH ) );
        Files.delete( Paths.get( basePath, CONTEXT_XML_PATH ) );
        Files.delete( Paths.get( basePath, PROPERTIES_PATH ) );

        _bInit = false;
    }
}

/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.testcontainers.DockerClientFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks the availability of the external environments some tests depend on
 */
public final class TestEnvironmentUtils
{
    private static final String PROPERTY_ELASTICSEARCH_URL = "elasticsearch.url";
    private static final int CONNECTION_TIMEOUT_MS = 2000;

    private TestEnvironmentUtils( )
    {
    }

    /**
     * Tells if a Docker environment is available
     *
     * @return true if a Docker environment is available
     */
    public static boolean isDockerAvailable( )
    {
        return DockerClientFactory.instance( ).isDockerAvailable( );
    }

    /**
     * Tells if the Elasticsearch server defined by the elasticsearch.url property responds
     *
     * @return true if the Elasticsearch server responds
     */
    public static boolean isElasticsearchAvailable( )
    {
        final String strUrl = AppPropertiesService.getProperty( PROPERTY_ELASTICSEARCH_URL );
        if ( strUrl == null )
        {
            return false;
        }
        try
        {
            final HttpURLConnection connection = (HttpURLConnection) new URL( strUrl ).openConnection( );
            connection.setConnectTimeout( CONNECTION_TIMEOUT_MS );
            connection.setReadTimeout( CONNECTION_TIMEOUT_MS );
            final int nResponseCode = connection.getResponseCode( );
            connection.disconnect( );
            return nResponseCode < HttpURLConnection.HTTP_INTERNAL_ERROR;
        }
        catch( final IOException e )
        {
            return false;
        }
    }

    /**
     * Prints that a test is skipped because an environment is missing
     *
     * @param testClass
     *            the test class
     * @param strTestName
     *            the test name
     * @param strMissingEnvironment
     *            the missing environment
     */
    public static void printSkipped( final Class<?> testClass, final String strTestName, final String strMissingEnvironment )
    {
        System.out.println( "No " + strMissingEnvironment + " available : " + testClass.getSimpleName( ) + "." + strTestName + " skipped" );
    }
}

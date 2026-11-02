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

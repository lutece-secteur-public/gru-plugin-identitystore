package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Applies the properties overrides needed by the tests to the exploded test webapp
 */
public final class TestPropertiesOverrideUtils
{
    private static final String OVERRIDE_RESOURCE = "/data/hsql/identitystore-test.properties";
    private static final String OVERRIDE_DIRECTORY = "WEB-INF/conf/override/plugins";
    private static final String OVERRIDE_FILE_NAME = "identitystore-test.properties";
    private static final String CONF_PATH = "/WEB-INF/conf/";

    private TestPropertiesOverrideUtils( )
    {
    }

    /**
     * Copies the test overrides in the override directory of the webapp and reloads the properties, if not already done
     *
     * @throws IOException
     *             if the overrides file cannot be copied
     */
    public static void applyTestOverrides( ) throws IOException
    {
        final Path overrideFile = Paths.get( AppPathService.getWebAppPath( ), OVERRIDE_DIRECTORY, OVERRIDE_FILE_NAME );
        if ( Files.exists( overrideFile ) )
        {
            return;
        }
        Files.createDirectories( overrideFile.getParent( ) );
        try ( final InputStream overrides = TestPropertiesOverrideUtils.class.getResourceAsStream( OVERRIDE_RESOURCE ) )
        {
            Files.copy( overrides, overrideFile );
        }
        AppPropertiesService.init( CONF_PATH );
    }
}

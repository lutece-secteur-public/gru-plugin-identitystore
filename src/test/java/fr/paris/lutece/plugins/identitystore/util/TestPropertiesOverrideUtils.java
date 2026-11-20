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

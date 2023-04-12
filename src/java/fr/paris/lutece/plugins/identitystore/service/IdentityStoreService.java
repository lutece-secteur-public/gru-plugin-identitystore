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
package fr.paris.lutece.plugins.identitystore.service;

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityConstants;
import fr.paris.lutece.plugins.identitystore.business.security.SecureMode;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.jwt.service.JWTUtil;
import org.apache.commons.lang3.StringUtils;

/*
 * Copyright (c) 2002-2016, Mairie de Paris
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

/**
 * IdentityStoreService
 */
public final class IdentityStoreService
{
    /**
     * private constructor
     */
    private IdentityStoreService( )
    {
    }

    /**
     * Get the application code to use
     * 
     * @param strHeaderClientAppCode
     *            The application code in HTTP request header
     * @param strParamAppCode
     *            The application code provided by the client
     * @return The application code to use
     */
    public static String getTrustedApplicationCode( String strHeaderClientAppCode, String strParamAppCode )
    {
        // Secure mode
        switch( getSecureMode( ) )
        {
            case JWT:
            {
                if ( !StringUtils.isEmpty( strHeaderClientAppCode ) )
                {
                    String strJwtClaimAppCode = AppPropertiesService.getProperty( IdentityConstants.PROPERTY_JWT_CLAIM_APP_CODE );
                    return JWTUtil.getPayloadValue( strHeaderClientAppCode, strJwtClaimAppCode );
                }
                break;
            }
            case NONE:
            {
                if ( !StringUtils.isEmpty( strHeaderClientAppCode ) )
                {
                    return strHeaderClientAppCode;
                }
                else
                {
                    if ( !StringUtils.isEmpty( strParamAppCode ) )
                    {
                        return strParamAppCode;
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get the secure Mode of the identitystore
     * 
     * @return the secure mode
     */
    public static SecureMode getSecureMode( )
    {
        switch( AppPropertiesService.getProperty( IdentityConstants.PROPERTY_SECURE_MODE, StringUtils.EMPTY ) )
        {
            case "jwt":
                return SecureMode.JWT;
        }
        return SecureMode.NONE;

    }
}

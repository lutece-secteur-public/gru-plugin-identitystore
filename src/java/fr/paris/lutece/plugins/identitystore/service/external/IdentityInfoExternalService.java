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
package fr.paris.lutece.plugins.identitystore.service.external;

import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.IdentityDto;
import fr.paris.lutece.portal.service.spring.SpringContextService;


/**
 * This class provides a service to retrieve identity information from external source.<br/>
 * Designed as a singleton
 */
public final class IdentityInfoExternalService
{
    private static final String BEAN_IDENTITY_INFO_EXTERNAL_PROVIDER = "identitystore.identityInfoExternalProvider";
    private static IIdentityInfoExternalProvider _identityInfoExternalProvider;
    private static IdentityInfoExternalService _singleton;

    /**
     *  Private constructor.
     */
    private IdentityInfoExternalService(  )
    {
    }

    /**
     * Return the unique instance.
     *
     * @return The instance
     */
    public static IdentityInfoExternalService instance(  )
    {
        if ( _singleton == null )
        {
            _singleton = new IdentityInfoExternalService(  );
            _identityInfoExternalProvider = SpringContextService.getBean( BEAN_IDENTITY_INFO_EXTERNAL_PROVIDER );
        }

        return _singleton;
    }

    /**
     * Gives identity information from external source.
     *
     * @param strConnectionId the connection id
     * @return the identity
     * @throws IdentityNotFoundException if no identity can be retrieve from external source
     */
    public IdentityDto getIdentityInfo( String strConnectionId )
        throws IdentityNotFoundException
    {
        return (IdentityDto) _identityInfoExternalProvider.getIdentityInfo( strConnectionId );
    }
}

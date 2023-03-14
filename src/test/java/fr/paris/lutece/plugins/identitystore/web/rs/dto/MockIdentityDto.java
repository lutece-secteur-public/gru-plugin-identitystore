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
package fr.paris.lutece.plugins.identitystore.web.rs.dto;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MockIdentityDto
{
    public static IdentityDto create( )
    {
        IdentityDto identityDto = new IdentityDto( );
        Map<String, AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( UUID.randomUUID( ).toString( ) );
        identityDto.setConnectionId( UUID.randomUUID( ).toString( ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }

    public static IdentityDto create( Identity identity )
    {
        IdentityDto identityDto = new IdentityDto( );
        Map<String, AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( identity.getCustomerId( ) );
        identityDto.setConnectionId( identity.getConnectionId( ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }

    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto createV1( )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto identityDto = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto( );
        Map<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( UUID.randomUUID( ).toString( ) );
        identityDto.setConnectionId( UUID.randomUUID( ).toString( ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }

    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto createIdentityV1( Identity identity )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto identityDto = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto( );
        Map<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> mapAttributes = new HashMap<>( );

        identityDto.setCustomerId( identity.getCustomerId( ) );
        identityDto.setConnectionId( identity.getConnectionId( ) );
        identityDto.setAttributes( mapAttributes );

        return identityDto;
    }
}

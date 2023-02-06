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
package fr.paris.lutece.plugins.identitystore.v1.service;

import static fr.paris.lutece.plugins.identitystore.v1.business.IdentityUtil.createIdentityInDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v1.web.request.IdentityStoreUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreUpdateRequestTest extends LuteceTestCase
{
    private static final String APPLICATION_CODE = "MyApplication";
    private ObjectMapper _objectMapper;
    private static String STRING = "string";

    // PARAMETERS
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_FAMILY_NAME = "family_name";

    // VALUES
    private static final String VALUE_FIRST_NAME_UPDATED = "First Name Updated";
    private static final String VALUE_FAMILY_NAME_UPDATED = "Family Name Updated";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        _objectMapper = new ObjectMapper( );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
        _objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    }

    public void testUpdateIdentity( ) throws JsonParseException, JsonMappingException, AppException, IOException, IdentityStoreException
    {
        Identity identityReference = createIdentityInDatabase( );
        fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto identityDto = MockIdentityDto.create( identityReference );
        fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto identityChangeDto = MockIdentityChangeDto
                .createIdentityChangeDtoFor( identityDto );
        IdentityChangeDto identityChangeDtoOldVersion = DtoConverter.convertToIdentityChangeDtoOldVersion( identityChangeDto );
        AuthorDto authorDto = new AuthorDto( );
        authorDto.setApplicationCode( APPLICATION_CODE );
        authorDto.setType( 2 );
        identityChangeDtoOldVersion.setAuthor( authorDto );
        Map<String, AttributeDto> mapAttributes = new HashMap<String, AttributeDto>( );
        AttributeDto attribute1 = new AttributeDto( );
        attribute1.setKey( PARAMETER_FAMILY_NAME );
        attribute1.setType( STRING );
        attribute1.setValue( VALUE_FAMILY_NAME_UPDATED );
        attribute1.setCertificate( null );
        attribute1.setCertified( false );
        mapAttributes.put( PARAMETER_FAMILY_NAME, attribute1 );
        AttributeDto attribute2 = new AttributeDto( );
        attribute2.setKey( PARAMETER_FIRST_NAME );
        attribute2.setType( STRING );
        attribute2.setValue( VALUE_FIRST_NAME_UPDATED );
        attribute2.setCertificate( null );
        attribute2.setCertified( false );
        mapAttributes.put( PARAMETER_FIRST_NAME, attribute2 );
        identityChangeDtoOldVersion.getIdentity( ).setAttributes( mapAttributes );

        IdentityStoreUpdateRequest identityUpdate = new IdentityStoreUpdateRequest( identityChangeDtoOldVersion, new HashMap<String, File>( ), _objectMapper );

        IdentityDto identityDtoUpdated = _objectMapper.readValue( identityUpdate.doRequest( ), IdentityDto.class );
        assertNotNull( identityDtoUpdated );
        // check if parameters are updated
        Map<String, AttributeDto> mapAttributesUpdated = identityDtoUpdated.getAttributes( );
        assertNotNull( mapAttributesUpdated );
        assertEquals( VALUE_FIRST_NAME_UPDATED, mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getValue( ) );
        assertEquals( VALUE_FAMILY_NAME_UPDATED, mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getValue( ) );
        assertFalse( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertified( ) );
        assertFalse( mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getCertified( ) );
        assertNull( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertificate( ) );
        assertNull( mapAttributesUpdated.get( PARAMETER_FAMILY_NAME ).getCertificate( ) );
    }
}

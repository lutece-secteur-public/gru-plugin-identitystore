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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.SimpleCertifier;
import fr.paris.lutece.plugins.identitystore.v1.web.request.IdentityStoreCertifyRequest;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockCertificateDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityStoreCertifyRequestTest extends LuteceTestCase
{

    private final String STRING = "string";
    private final String VALUE_PREFERED_NAME_CERITIFIED = "Prefered Name Certified";
    private final String PARAMETER_FIRST_NAME = "first_name";
    private final String VALUE_FIRST_NAME_UPDATED = "First Name Updated";

    private ObjectMapper _objectMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        IdentityStoreTestContext.initContext( );
        _objectMapper = new ObjectMapper( );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
        _objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    }

    public void testIdentityStoreCertify( ) throws AppException, IOException, IdentityStoreException
    {
        fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.CertificateDto certificateDto = MockCertificateDto
                .create( IdentityStoreTestContext.CERTIFIER1_CODE );
        CertificateDto certificateDtoOldVersion = DtoConverter.convertToCertificateDtoOldVersion( certificateDto );
        AbstractCertifier certif1 = new SimpleCertifier( IdentityStoreTestContext.CERTIFIER1_CODE );
        certif1.setCertificateLevel( IdentityStoreTestContext.CERTIFIER1_LEVEL );
        certif1.setExpirationDelay( IdentityStoreTestContext.CERTIFIER1_EXPIRATIONDELAY );
        certif1.setName( IdentityStoreTestContext.CERTIFIER1_NAME );
        certif1.setCertifiableAttributesList( Arrays.asList( new String [ ] {
                IdentityStoreTestContext.ATTRKEY_1, IdentityStoreTestContext.ATTRKEY_3
        } ) );
        Identity identityReference = createIdentityInDatabase( );
        fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto identityDto = MockIdentityDto.create( identityReference );
        fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto identityChangeDto = MockIdentityChangeDto
                .createIdentityChangeDtoFor( identityDto );
        IdentityChangeDto identityChangeDtoOldVersion = DtoConverter.convertToIdentityChangeDtoOldVersion( identityChangeDto );
        AuthorDto authorDto = new AuthorDto( );
        authorDto.setApplicationCode( IdentityStoreTestContext.SAMPLE_APPCODE );
        authorDto.setType( 2 );
        identityChangeDtoOldVersion.setAuthor( authorDto );
        Map<String, AttributeDto> mapAttributes = new HashMap<String, AttributeDto>( );
        AttributeDto attribute1 = new AttributeDto( );
        attribute1.setKey( IdentityStoreTestContext.ATTRKEY_3 );
        attribute1.setType( STRING );
        attribute1.setValue( VALUE_PREFERED_NAME_CERITIFIED );
        attribute1.setCertificate( null );
        attribute1.setCertified( true );
        mapAttributes.put( IdentityStoreTestContext.ATTRKEY_3, attribute1 );
        AttributeDto attribute2 = new AttributeDto( );
        attribute2.setKey( PARAMETER_FIRST_NAME );
        attribute2.setType( STRING );
        attribute2.setValue( VALUE_FIRST_NAME_UPDATED );
        attribute2.setCertificate( certificateDtoOldVersion );
        attribute2.setCertified( true );
        mapAttributes.put( PARAMETER_FIRST_NAME, attribute2 );
        identityChangeDtoOldVersion.getIdentity( ).setAttributes( mapAttributes );

        IdentityStoreCertifyRequest identityStoreRequest = new IdentityStoreCertifyRequest(
                DtoConverter.convertToIdentityChangeDtoNewVersionWithCertificate( identityChangeDtoOldVersion, certif1 ), _objectMapper );

        IdentityDto identityDtoCertified = _objectMapper.readValue( identityStoreRequest.doRequest( ), IdentityDto.class );
        assertNotNull( identityDtoCertified );
        // check if parameters are updated
        Map<String, AttributeDto> mapAttributesUpdated = identityDtoCertified.getAttributes( );
        assertNotNull( mapAttributesUpdated );
        assertEquals( VALUE_FIRST_NAME_UPDATED, mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getValue( ) );
        assertEquals( VALUE_PREFERED_NAME_CERITIFIED, mapAttributesUpdated.get( IdentityStoreTestContext.ATTRKEY_3 ).getValue( ) );
        assertTrue( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertified( ) );
        assertTrue( mapAttributesUpdated.get( IdentityStoreTestContext.ATTRKEY_3 ).getCertified( ) );
        assertNotNull( mapAttributesUpdated.get( PARAMETER_FIRST_NAME ).getCertificate( ) );
        assertNotNull( mapAttributesUpdated.get( IdentityStoreTestContext.ATTRKEY_3 ).getCertificate( ) );

    }
}

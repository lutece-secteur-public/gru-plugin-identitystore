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
package fr.paris.lutece.plugins.identitystore.v2.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.AuthorType;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 * @author levy
 */
public class IdentityStoreServiceTest extends LuteceTestCase
{
    private final String TEST_KOID = "KO_ID";

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
    }

    public void testGetAttribute( )
    {
        IdentityAttribute attributeDB = IdentityStoreService.getAttribute( IdentityStoreTestContext.SAMPLE_CONNECTIONID, IdentityStoreTestContext.ATTRKEY_1,
                IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( attributeDB );
        assertEquals( IdentityStoreTestContext.ATTRVAL_1, attributeDB.getValue( ) );
        attributeDB = IdentityStoreService.getAttribute( IdentityStoreTestContext.SAMPLE_CONNECTIONID, TEST_KOID, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( attributeDB );
    }

    public void testBuildIdentityChange( )
    {
        // getIdentityChangeForCreation
        // TEST_CONNECTIONID1 has already been tested to give null identity in previous lines
        IdentityChangeDto identityChangeDto = IdentityStoreService.buildIdentityChange( IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNotNull( identityChangeDto );
        assertNotNull( identityChangeDto.getIdentity( ) );
        assertNotNull( identityChangeDto.getAuthor( ) );
        assertNull( identityChangeDto.getIdentity( ).getConnectionId( ) );
        assertNull( identityChangeDto.getIdentity( ).getCustomerId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_APPCODE, identityChangeDto.getAuthor( ).getApplicationCode( ) );
        assertEquals( AuthorType.TYPE_APPLICATION.getTypeValue( ), identityChangeDto.getAuthor( ).getType( ) );
    }

    // removeIdentity untestable due to empty identitystore.application.code.delete.authorized.list
}

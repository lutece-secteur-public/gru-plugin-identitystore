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

import static fr.paris.lutece.plugins.identitystore.v2.business.AttributeCertificateUtil.createAttributeCertificateInDatabase;
import static fr.paris.lutece.plugins.identitystore.v2.business.AttributeCertificateUtil.createExpiredAttributeCertificateInDatabase;
import static fr.paris.lutece.plugins.identitystore.v2.business.IdentityAttributeUtil.createIdentityAttributeInDatabase;
import static fr.paris.lutece.plugins.identitystore.v2.business.IdentityUtil.createIdentityInDatabase;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityDeletedException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;

import static fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto.createIdentityChangeDtoFor;

import java.util.HashMap;

import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 * @author levy
 */
public class IdentityStoreServiceGetIdentityTest extends LuteceTestCase
{
    private final String TEST_CONNECTIONID1 = "12345";
    private final String TEST_KOID = "KO_ID";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        IdentityStoreTestContext.initContext( );
    }

    public void testGetIdentityByConnectionId( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        AttributeKey attributeKey2 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_2 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey2 );

        Identity identityDB = IdentityStoreService.getIdentityByConnectionId( identityReference.getConnectionId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identityDB );
        assertEquals( identityReference.getCustomerId( ), identityDB.getCustomerId( ) );
        assertEquals( 2, identityDB.getAttributes( ).size( ) );
    }

    private AttributeKey findAttributeKey( String strAttributeKeyName )
    {
        return AttributeKeyHome.findByKey( strAttributeKeyName );
    }

    public void testGetIdentityByConnectionIdWithUnknownConnectionId( )
    {
        Identity identityDB = IdentityStoreService.getIdentityByConnectionId( TEST_CONNECTIONID1, IdentityStoreTestContext.SAMPLE_APPCODE );
        assertNull( identityDB );
    }

    public void testGetIdentityByCustomerId( )
    {
        Identity identityDB = IdentityStoreService.getIdentityByCustomerId( IdentityStoreTestContext.SAMPLE_CUSTOMERID,
                IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identityDB );
        assertEquals( IdentityStoreTestContext.SAMPLE_CONNECTIONID, identityDB.getConnectionId( ) );
        assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, identityDB.getAttributes( ).size( ) );

        identityDB = IdentityStoreService.getIdentityByCustomerId( TEST_KOID, IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNull( identityDB );
    }

    public void testGetIdentityByCustomerIdWithNoCertificateAttribut( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1,
                IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithNoCertificateAttribut" );

        Identity identity = IdentityStoreService.getIdentityByCustomerId( identityReference.getCustomerId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identity );
        assertNotNull( identity.getAttributes( ) );
        assertEquals( 1, identity.getAttributes( ).size( ) );
        IdentityAttribute attr1 = identity.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1 );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithNoCertificateAttribut", attr1.getValue( ) );
        AttributeCertificate attr1Certificate = attr1.getCertificate( );
        assertNull( attr1Certificate );
    }

    public void testGetIdentityByCustomerIdWithCertificateAttributWithLimitedExpirationDate( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeCertificate attributeCertificate = createAttributeCertificateInDatabase( IdentityStoreTestContext.CERTIFIER1_CODE );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1,
                IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithCertificateAttributWithLimitedExpirationDate", attributeCertificate );

        Identity identity = IdentityStoreService.getIdentityByCustomerId( identityReference.getCustomerId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identity );
        assertNotNull( identity.getAttributes( ) );
        assertEquals( 1, identity.getAttributes( ).size( ) );
        IdentityAttribute attr1 = identity.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1 );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithCertificateAttributWithLimitedExpirationDate", attr1.getValue( ) );
        AttributeCertificate attr1Certificate = attr1.getCertificate( );
        assertNotNull( attr1Certificate );
        assertNotNull( attr1Certificate.getCertifierCode( ) );
        assertNotNull( attr1Certificate.getCertifierName( ) );
        assertTrue( attr1Certificate.getCertificateLevel( ) > 0 );
        assertNotNull( attr1Certificate.getExpirationDate( ) );
    }

    public void testGetIdentityByCustomerIdWithCertificateAttributWithUnlimitedExpirationDate( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeCertificate attributeCertificate = createAttributeCertificateInDatabase( IdentityStoreTestContext.CERTIFIER4_CODE );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1,
                IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithCertificateAttributWithUnlimitedExpirationDate", attributeCertificate );

        Identity identity = IdentityStoreService.getIdentityByCustomerId( identityReference.getCustomerId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identity );
        assertNotNull( identity.getAttributes( ) );
        assertEquals( 1, identity.getAttributes( ).size( ) );
        IdentityAttribute attr1 = identity.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1 );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithCertificateAttributWithUnlimitedExpirationDate", attr1.getValue( ) );
        AttributeCertificate attr1Certificate = attr1.getCertificate( );
        assertNotNull( attr1Certificate );
        assertNotNull( attr1Certificate.getCertifierCode( ) );
        assertNotNull( attr1Certificate.getCertifierName( ) );
        assertTrue( attr1Certificate.getCertificateLevel( ) > 0 );
        assertNull( attr1Certificate.getExpirationDate( ) );
    }

    public void testGetIdentityByCustomerIdWithExpiredCertificateAttribut( )
    {
        Identity identityReference = createIdentityInDatabase( );
        AttributeCertificate attributeCertificate = createExpiredAttributeCertificateInDatabase( IdentityStoreTestContext.CERTIFIER1_CODE );
        AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
        createIdentityAttributeInDatabase( identityReference, attributeKey1,
                IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithExpiredCertificateAttribut", attributeCertificate );

        Identity identity = IdentityStoreService.getIdentityByCustomerId( identityReference.getCustomerId( ), IdentityStoreTestContext.SAMPLE_APPCODE );

        assertNotNull( identity );
        assertNotNull( identity.getAttributes( ) );
        assertEquals( 1, identity.getAttributes( ).size( ) );
        IdentityAttribute attr1 = identity.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
        assertNotNull( attr1 );
        assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testGetIdentityByCustomerIdWithExpiredCertificateAttribut", attr1.getValue( ) );
        AttributeCertificate attr1Certificate = attr1.getCertificate( );
        assertNull( attr1Certificate );
    }

    public void testGetIdentityDeleted( )
    {
        Identity identityReference = createIdentityInDatabase( );
        IdentityHome.softRemove( identityReference.getId( ) );
        IdentityDto identityDto = MockIdentityDto.create( identityReference );
        IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );

        try
        {
            IdentityStoreService.getOrCreateIdentity( identityChangeDto, new HashMap<>( ) );
            fail( "Expected an IdentityDeletedException to be thrown" );
        }
        catch( IdentityDeletedException e )
        {
            // Correct behavior
        }
        catch( IdentityStoreException e )
        {
            throw new RuntimeException( e );
        }
    }

    // getOrCreateIdentity untestable due to lack of attributes in MockIdentityInfoExternalProvider
}

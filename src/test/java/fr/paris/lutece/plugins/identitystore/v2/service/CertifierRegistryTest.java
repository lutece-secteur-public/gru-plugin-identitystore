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

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 */
public class CertifierRegistryTest extends LuteceTestCase
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );

        IdentityStoreTestContext.initContext( );
    }

    public void testBusiness( ) throws CertifierNotFoundException
    {
        // after initContext, there is 2 certifier with different conf
        assertEquals( IdentityStoreTestContext.getNbCertifiers( ), CertifierRegistry.instance( ).getCertifiersList( ).size( ) );

        // control certifier 1
        AbstractCertifier certifier1 = CertifierRegistry.instance( ).getCertifier( IdentityStoreTestContext.CERTIFIER1_CODE );
        assertNotNull( certifier1 );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, certifier1.getCode( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_NAME, certifier1.getName( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_LEVEL, certifier1.getCertificateLevel( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_EXPIRATIONDELAY, certifier1.getExpirationDelay( ) );
        assertEquals( 2, certifier1.getCertifiableAttributesList( ).size( ) );

        // control certifier 2
        AbstractCertifier certifier2 = CertifierRegistry.instance( ).getCertifier( IdentityStoreTestContext.CERTIFIER2_CODE );
        assertNotNull( certifier2 );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, certifier2.getCode( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_NAME, certifier2.getName( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_LEVEL, certifier2.getCertificateLevel( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER2_EXPIRATIONDELAY, certifier2.getExpirationDelay( ) );
        assertEquals( 2, certifier2.getCertifiableAttributesList( ).size( ) );

        // control certifier 3
        AbstractCertifier certifier3 = CertifierRegistry.instance( ).getCertifier( IdentityStoreTestContext.CERTIFIER3_CODE );
        assertNotNull( certifier3 );
        assertEquals( IdentityStoreTestContext.CERTIFIER3_CODE, certifier3.getCode( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER3_NAME, certifier3.getName( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER3_LEVEL, certifier3.getCertificateLevel( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER3_EXPIRATIONDELAY, certifier3.getExpirationDelay( ) );
        assertEquals( 3, certifier3.getCertifiableAttributesList( ).size( ) );

        // control certificate for certifier 1
        AttributeCertificate certificate = certifier1.generateCertificate( );
        assertNotNull( certificate );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, certificate.getCertifierCode( ) );
        assertEquals( IdentityStoreTestContext.CERTIFIER1_LEVEL, certificate.getCertificateLevel( ) );
        assertNotNull( certificate.getExpirationDate( ) );
    }
}

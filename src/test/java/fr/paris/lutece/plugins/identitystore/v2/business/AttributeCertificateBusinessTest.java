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
package fr.paris.lutece.plugins.identitystore.v2.business;

import java.sql.Timestamp;

import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.test.LuteceTestCase;

public class AttributeCertificateBusinessTest extends LuteceTestCase
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
        // Initialize an object
        AttributeCertificate attributeCertificate = CertifierRegistry.instance( ).getCertifier( IdentityStoreTestContext.CERTIFIER1_CODE )
                .generateCertificate( );
        // MYSQL doesn't managed nanosecond
        Timestamp cert1Date = attributeCertificate.getCertificateDate( );
        cert1Date.setNanos( 0 );
        Timestamp cert1ExpDate = attributeCertificate.getExpirationDate( );
        cert1ExpDate.setNanos( 0 );

        // Create test
        AttributeCertificateHome.create( attributeCertificate );

        AttributeCertificate attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate.getId( ) );
        assertEquals( attributeCertificate.getCertifierCode( ), attributeCertificateStored.getCertifierCode( ) );
        assertEquals( cert1Date.getTime( ), attributeCertificateStored.getCertificateDate( ).getTime( ) );
        assertEquals( attributeCertificate.getCertificateLevel( ), attributeCertificateStored.getCertificateLevel( ) );
        assertEquals( cert1ExpDate.getTime( ), attributeCertificateStored.getExpirationDate( ).getTime( ) );

        // Update test
        AttributeCertificate attributeCertificate2 = CertifierRegistry.instance( ).getCertifier( IdentityStoreTestContext.CERTIFIER2_CODE )
                .generateCertificate( );
        // MYSQL doesn't managed nanosecond
        Timestamp cert2Date = attributeCertificate2.getCertificateDate( );
        cert2Date.setNanos( 0 );
        Timestamp cert2ExpDate = attributeCertificate2.getExpirationDate( );
        cert2ExpDate.setNanos( 0 );

        attributeCertificate2.setId( attributeCertificate.getId( ) );
        AttributeCertificateHome.update( attributeCertificate2 );

        attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate2.getId( ) );
        assertEquals( attributeCertificate2.getCertifierCode( ), attributeCertificateStored.getCertifierCode( ) );
        assertEquals( cert2Date.getTime( ), attributeCertificateStored.getCertificateDate( ).getTime( ) );
        assertEquals( attributeCertificate2.getCertificateLevel( ), attributeCertificateStored.getCertificateLevel( ) );
        assertEquals( cert2ExpDate.getTime( ), attributeCertificateStored.getExpirationDate( ).getTime( ) );

        // List test
        AttributeCertificateHome.getAttributeCertificatesList( );

        // Delete test
        AttributeCertificateHome.remove( attributeCertificate.getId( ) );
        attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate.getId( ) );
        assertNull( attributeCertificateStored );
    }
}

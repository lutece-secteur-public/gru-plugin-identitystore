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
package fr.paris.lutece.plugins.identitystore.v1.business;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.test.LuteceTestCase;

import java.sql.Timestamp;

public class AttributeCertificateBusinessTest extends LuteceTestCase
{
    private final static String CERTIFIERCODE1 = "certifiercode1";
    private final static String CERTIFIERCODE2 = "certifiercode2";
    private final static Timestamp CERTIFICATEDATE1 = new Timestamp( 1000000L );
    private final static Timestamp CERTIFICATEDATE2 = new Timestamp( 2000000L );
    private final static int CERTIFICATELEVEL1 = 1;
    private final static int CERTIFICATELEVEL2 = 2;
    private final static Timestamp EXPIRATIONDATE1 = new Timestamp( 1000000L );
    private final static Timestamp EXPIRATIONDATE2 = new Timestamp( 2000000L );

    public void testBusiness( )
    {
        // Initialize an object
        AttributeCertificate attributeCertificate = new AttributeCertificate( );
        attributeCertificate.setCertifierCode( CERTIFIERCODE1 );
        attributeCertificate.setCertificateDate( CERTIFICATEDATE1 );
        attributeCertificate.setCertificateLevel( CERTIFICATELEVEL1 );
        attributeCertificate.setExpirationDate( EXPIRATIONDATE1 );

        // Create test
        AttributeCertificateHome.create( attributeCertificate );

        AttributeCertificate attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate.getId( ) );
        assertEquals( attributeCertificateStored.getCertifierCode( ), attributeCertificate.getCertifierCode( ) );
        assertEquals( attributeCertificateStored.getCertificateDate( ), attributeCertificate.getCertificateDate( ) );
        assertEquals( attributeCertificateStored.getCertificateLevel( ), attributeCertificate.getCertificateLevel( ) );
        assertEquals( attributeCertificateStored.getExpirationDate( ), attributeCertificate.getExpirationDate( ) );

        // Update test
        attributeCertificate.setCertifierCode( CERTIFIERCODE2 );
        attributeCertificate.setCertificateDate( CERTIFICATEDATE2 );
        attributeCertificate.setCertificateLevel( CERTIFICATELEVEL2 );
        attributeCertificate.setExpirationDate( EXPIRATIONDATE2 );
        AttributeCertificateHome.update( attributeCertificate );
        attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate.getId( ) );
        assertEquals( attributeCertificateStored.getCertifierCode( ), attributeCertificate.getCertifierCode( ) );
        assertEquals( attributeCertificateStored.getCertificateDate( ), attributeCertificate.getCertificateDate( ) );
        assertEquals( attributeCertificateStored.getCertificateLevel( ), attributeCertificate.getCertificateLevel( ) );
        assertEquals( attributeCertificateStored.getExpirationDate( ), attributeCertificate.getExpirationDate( ) );

        // List test
        AttributeCertificateHome.getAttributeCertificatesList( );

        // Delete test
        AttributeCertificateHome.remove( attributeCertificate.getId( ) );
        attributeCertificateStored = AttributeCertificateHome.findByPrimaryKey( attributeCertificate.getId( ) );
        assertNull( attributeCertificateStored );
    }
}

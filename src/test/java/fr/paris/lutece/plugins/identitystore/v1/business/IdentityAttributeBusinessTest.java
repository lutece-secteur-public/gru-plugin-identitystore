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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.test.LuteceTestCase;

public class IdentityAttributeBusinessTest extends LuteceTestCase
{
    private static final String APPLICATION_CODE = "MyApplication";

    private final static int IDIDENTITY1 = 1;
    private final static int IDIDENTITY2 = 2;
    private final static int IDATTRIBUTE1 = 1;
    private final static int IDATTRIBUTE2 = 2;
    private final static String ATTRIBUTEVALUE1 = "AttributeValue1";
    private final static String ATTRIBUTEVALUE2 = "AttributeValue2";
    private final static int IDCERTIFICATION1 = 1;
    private final static int IDCERTIFICATION2 = 2;

    public void testBusiness( )
    {
        // Initialize an object
        AttributeKey attributeKey = new AttributeKey( );
        attributeKey.setId( IDATTRIBUTE1 );
        IdentityAttribute identityAttribute = new IdentityAttribute( );
        identityAttribute.setIdIdentity( IDIDENTITY1 );
        identityAttribute.setAttributeKey( attributeKey );
        identityAttribute.setValue( ATTRIBUTEVALUE1 );
        identityAttribute.setIdCertificate( IDCERTIFICATION1 );

        // Create test
        IdentityAttributeHome.create( identityAttribute );

        IdentityAttribute identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ),
                identityAttribute.getAttributeKey( ).getId( ) );
        assertEquals( identityAttributeStored.getIdIdentity( ), identityAttribute.getIdIdentity( ) );
        assertEquals( identityAttributeStored.getAttributeKey( ).getId( ), identityAttribute.getAttributeKey( ).getId( ) );
        assertEquals( identityAttributeStored.getValue( ), identityAttribute.getValue( ) );
        assertEquals( identityAttributeStored.getIdCertificate( ), identityAttribute.getIdCertificate( ) );

        // Update test
        attributeKey.setId( IDATTRIBUTE2 );
        identityAttribute.setIdIdentity( IDIDENTITY2 );
        identityAttribute.setValue( ATTRIBUTEVALUE2 );
        identityAttribute.setIdCertificate( IDCERTIFICATION2 );
        IdentityAttributeHome.update( identityAttribute );
        identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
        assertEquals( identityAttributeStored.getIdIdentity( ), identityAttribute.getIdIdentity( ) );
        assertEquals( identityAttributeStored.getAttributeKey( ).getId( ), identityAttribute.getAttributeKey( ).getId( ) );
        assertEquals( identityAttributeStored.getValue( ), identityAttribute.getValue( ) );
        assertEquals( identityAttributeStored.getIdCertificate( ), identityAttribute.getIdCertificate( ) );

        // List test
        IdentityAttributeHome.getAttributes( 1, APPLICATION_CODE );

        // Delete test
        IdentityAttributeHome.remove( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
        identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
        assertNull( identityAttributeStored );
    }
}

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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.test.LuteceTestCase;

public class AttributeKeyBusinessTest extends LuteceTestCase
{
    private final static String KEYNAME1 = "KeyName1";
    private final static String KEYNAME2 = "KeyName2";
    private final static String NAME1 = "Name1";
    private final static String NAME2 = "Name2";
    private final static String KEYDESCRIPTION1 = "KeyDescription1";
    private final static String KEYDESCRIPTION2 = "KeyDescription2";

    public void testBusiness( )
    {
        // Initialize an object
        AttributeKey attributeKey = new AttributeKey( );
        attributeKey.setKeyName( KEYNAME1 );
        attributeKey.setName( NAME1 );
        attributeKey.setDescription( KEYDESCRIPTION1 );
        attributeKey.setKeyType( KeyType.STRING );

        // Create test
        AttributeKeyHome.create( attributeKey );

        AttributeKey attributeKeyStored = AttributeKeyHome.findByPrimaryKey( attributeKey.getId( ) );
        assertEquals( attributeKey.getKeyName( ), attributeKeyStored.getKeyName( ) );
        assertEquals( attributeKey.getDescription( ), attributeKeyStored.getDescription( ) );
        assertEquals( attributeKey.getKeyType( ), attributeKeyStored.getKeyType( ) );

        // Update test
        attributeKey.setKeyName( KEYNAME2 );
        attributeKey.setName( NAME2 );
        attributeKey.setDescription( KEYDESCRIPTION2 );
        attributeKey.setKeyType( KeyType.STRING );
        AttributeKeyHome.update( attributeKey );
        attributeKeyStored = AttributeKeyHome.findByPrimaryKey( attributeKey.getId( ) );
        assertEquals( attributeKey.getKeyName( ), attributeKeyStored.getKeyName( ) );
        assertEquals( attributeKey.getDescription( ), attributeKeyStored.getDescription( ) );
        assertEquals( attributeKey.getKeyType( ), attributeKeyStored.getKeyType( ) );

        // List test
        AttributeKeyHome.getAttributeKeysList( );

        // Delete test
        AttributeKeyHome.remove( attributeKey.getId( ) );
        attributeKeyStored = AttributeKeyHome.findByPrimaryKey( attributeKey.getId( ) );
        assertNull( attributeKeyStored );
    }
}

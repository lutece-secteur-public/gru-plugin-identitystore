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
/// *
// * Copyright (c) 2002-2023, City of Paris
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions
// * are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice
// * and the following disclaimer.
// *
// * 2. Redistributions in binary form must reproduce the above copyright notice
// * and the following disclaimer in the documentation and/or other materials
// * provided with the distribution.
// *
// * 3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
// * contributors may be used to endorse or promote products derived from
// * this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// *
// * License 1.0
// */
// package fr.paris.lutece.plugins.identitystore.v3.business;
//
// import fr.paris.lutece.plugins.identitystore.IdentityStoreBDDTestCase;
// import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
// import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
// import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
// import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
// import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
// import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
// import fr.paris.lutece.plugins.identitystore.util.IdentitystoreTestUtils;
//
// public class IdentityAttributeBusinessTest extends IdentityStoreBDDTestCase
// {
// private final static int IDATTRIBUTE1 = 1; // gender
// private final static String ATTRIBUTEVALUE1 = "AttributeValue1";
// private final static String ATTRIBUTEVALUE2 = "AttributeValue2";
// private final static int IDCERTIFICATION1 = 101;
// private final static int IDCERTIFICATION2 = 102;
//
// public void testBusiness( )
// {
// Identity identityReference = createIdentityInDatabase( );
//
// // Initialize an object
// AttributeKey attributeKey = new AttributeKey( );
// attributeKey.setId( IDATTRIBUTE1 );
// IdentityAttribute identityAttribute = new IdentityAttribute( );
// identityAttribute.setIdIdentity( identityReference.getId( ) );
// identityAttribute.setAttributeKey( attributeKey );
// identityAttribute.setValue( ATTRIBUTEVALUE1 );
// identityAttribute.setIdCertificate( IDCERTIFICATION1 );
//
// // Create test
// IdentityAttributeHome.create( identityAttribute );
//
// IdentityAttribute identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ),
// identityAttribute.getAttributeKey( ).getId( ) );
// assertEquals( identityAttribute.getIdIdentity( ), identityAttributeStored.getIdIdentity( ) );
// assertEquals( identityAttribute.getAttributeKey( ).getId( ), identityAttributeStored.getAttributeKey( ).getId( ) );
// assertEquals( identityAttribute.getValue( ), identityAttributeStored.getValue( ) );
// assertEquals( identityAttribute.getIdCertificate( ), identityAttributeStored.getIdCertificate( ) );
//
// // Update test
// identityAttribute.setValue( ATTRIBUTEVALUE2 );
// identityAttribute.setIdCertificate( IDCERTIFICATION2 );
// IdentityAttributeHome.update( identityAttribute );
// identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
// assertEquals( identityAttribute.getValue( ), identityAttributeStored.getValue( ) );
// assertEquals( identityAttribute.getIdCertificate( ), identityAttributeStored.getIdCertificate( ) );
//
// // List test
// IdentityAttributeHome.getAttributes( 1, IdentityStoreTestContext.SAMPLE_APPCODE );
//
// // Delete test
// IdentityAttributeHome.remove( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
// identityAttributeStored = IdentityAttributeHome.findByPrimaryKey( identityAttribute.getIdIdentity( ), identityAttribute.getAttributeKey( ).getId( ) );
// assertNull( identityAttributeStored );
// }
//
// private Identity createIdentityInDatabase( )
// {
// Identity identity = IdentitystoreTestUtils.createIdentity( );
// identity = IdentityHome.create( identity );
//
// return identity;
// }
// }

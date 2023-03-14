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
// package fr.paris.lutece.plugins.identitystore.v3.service;
//
// import fr.paris.lutece.plugins.identitystore.IdentityStoreBDDTestCase;
// import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
// import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
// import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
// import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
// import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
// import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
// import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
// import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
// import fr.paris.lutece.plugins.identitystore.util.IdentitystoreTestUtils;
// import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.*;
// import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
// import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockAttributeDto;
// import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockCertificateDto;
// import fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityDto;
//
// import java.util.HashMap;
//
// import static fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto.createIdentityChangeDtoFor;
//
/// **
// *
// * @author levy
// */
// public class IdentityStoreServiceUpdateIdentityTest extends IdentityStoreBDDTestCase
// {
//
// public void testUpdateIdentityWithApplicationNotAllowedToRemoveValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithApplicationNotAllowedToRemoveValue" );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( null );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithApplicationNotAllowedToRemoveValue", attr1After.getValue( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_DELETE_NOT_ALLOW_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithNoAttributeInDatabaseAndNoCertificateInInput( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// MockAttributeDto.create( identityDto, IdentityStoreTestContext.ATTRKEY_1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndNoCertificateInInput" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndNoCertificateInInput", attr1After.getValue( ) );
// assertNull( attr1After.getCertificate( ) );
// assertFalse( attr1After.isCertified( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndNoCertificateInInput",
// attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInput( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, IdentityStoreTestContext.ATTRKEY_1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInput" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER1_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInput", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInput",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1StatusAfter.getNewCertifier( ), attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInputWithNoExpirationDate( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, IdentityStoreTestContext.ATTRKEY_1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInputWithNoExpirationDate" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER4_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInputWithNoExpirationDate",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoAttributeInDatabaseAndCertificateInInputWithNoExpirationDate",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1StatusAfter.getNewCertifier( ), attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithCertificateInDatabaseButNotInInputAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER1_CODE,
/// IdentityStoreTestContext.CERTIFIER1_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificateInDatabaseButNotInInputAndSameValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// attr1.setCertificate( null );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificateInDatabaseButNotInInputAndSameValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithCertificateInDatabaseButNotInInputAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER1_CODE,
/// IdentityStoreTestContext.CERTIFIER1_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificateInDatabaseButNotInInputAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// attr1.setCertificate( null );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificateInDatabaseButNotInInputAndNewValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithLowerLevelCertificateAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithLowerLevelCertificateAndSameValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER1_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithLowerLevelCertificateAndSameValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithLowerLevelCertificateAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithLowerLevelCertificateAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithLowerLevelCertificateAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER1_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithLowerLevelCertificateAndNewValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithHigherLevelCertificateAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER1_CODE,
/// IdentityStoreTestContext.CERTIFIER1_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndSameValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndSameValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithHigherLevelCertificateAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER1_CODE,
/// IdentityStoreTestContext.CERTIFIER1_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndNewValue_newValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndSameValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER3_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndSameValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_LONGER_CERTIFIER_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER3_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateSoonerAndNewValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_LONGER_CERTIFIER_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER3_CODE,
/// IdentityStoreTestContext.CERTIFIER3_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndSameValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndSameValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER3_CODE,
/// IdentityStoreTestContext.CERTIFIER3_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndNewValue_newValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndExpirationDateLaterAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER2_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER4_CODE,
/// IdentityStoreTestContext.CERTIFIER4_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndSameValue",
// attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndSameValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER4_CODE,
/// IdentityStoreTestContext.CERTIFIER4_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndNewValue",
// attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER2_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndNewValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_VALUE_CERTIFIED_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndSameValue",
// attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER4_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndSameValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER2_CODE,
/// IdentityStoreTestContext.CERTIFIER2_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithHigherLevelCertificateAndNewValue", attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER4_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndNewValue_newValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInRequestAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER4_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndInRequestAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER4_CODE,
/// IdentityStoreTestContext.CERTIFIER4_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndSameValue",
// attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER5_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndSameValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER5_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNotNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndInRequestAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeCertificate attributeCertificate = IdentitystoreTestUtils.createAttributeCertificateInDatabase( 100, IdentityStoreTestContext.CERTIFIER4_CODE,
/// IdentityStoreTestContext.CERTIFIER4_CODE, 200 );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndNewValue",
// attributeCertificate );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1
// + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndInRequestAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER5_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals(
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndInRequestAndNewValue_newValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER5_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals(
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameLevelCertificateAndNoExpirationDateInDatabaseAndInRequestAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER5_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndSameValue" );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndSameValue", attr1After.getValue( ) );
// assertNull( attr1After.getCertificate( ) );
// assertFalse( attr1After.isCertified( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.INFO_NO_CHANGE_REQUEST_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithSameValueAndNoCertificateInRequestNorDatabase" );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndNewValue_newValue",
// attr1After.getValue( ) );
// assertNull( attr1After.getCertificate( ) );
// assertFalse( attr1After.isCertified( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithNoCertificateInDatabaseNorInInputAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertNull( attr1StatusAfter.getNewCertifier( ) );
// assertNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndSameValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndSameValue" );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER1_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndSameValue", attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertNull( attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndNewValue( ) throws IdentityStoreException
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// AttributeKey attributeKey1 = findAttributeKey( IdentityStoreTestContext.ATTRKEY_1 );
// IdentityAttribute identityAttribute1 = IdentitystoreTestUtils.createIdentityAttributeInDatabase( identityReference, attributeKey1,
// IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndNewValue" );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// AttributeDto attr1 = MockAttributeDto.create( identityDto, identityAttribute1 );
// attr1.setValue( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndNewValue_newValue" );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// CertificateDto certificateDto = MockCertificateDto.create( IdentityStoreTestContext.CERTIFIER1_CODE );
// attr1.setCertificate( certificateDto );
//
// IdentityDto identityAfterDto = IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
//
// assertNotNull( identityAfterDto );
// assertNotNull( identityAfterDto.getAttributes( ) );
// assertEquals( 1, identityAfterDto.getAttributes( ).size( ) );
// AttributeDto attr1After = identityAfterDto.getAttributes( ).get( IdentityStoreTestContext.ATTRKEY_1 );
// assertNotNull( attr1After );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndNewValue_newValue",
// attr1After.getValue( ) );
// assertNotNull( attr1After.getCertificate( ) );
// assertTrue( attr1After.isCertified( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1After.getCertificate( ).getCertifierCode( ) );
// AttributeStatusDto attr1StatusAfter = attr1After.getStatus( );
// assertNotNull( attr1StatusAfter );
// assertEquals( AttributeStatusDto.OK_CODE, attr1StatusAfter.getStatusCode( ) );
// assertEquals( IdentityStoreTestContext.ATTRKEY_1 + "testUpdateIdentityWithCertificatetNotInDatabaseButInInputAndNewValue_newValue",
// attr1StatusAfter.getNewValue( ) );
// assertEquals( IdentityStoreTestContext.CERTIFIER1_CODE, attr1StatusAfter.getNewCertifier( ) );
// assertNotNull( attr1StatusAfter.getNewCertificateExpirationDate( ) );
// }
//
// public void testUpdateIdentityDeletedWithConnectedId( )
// {
//
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// IdentityHome.softRemove( identityReference.getId( ) );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// try
// {
// IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
// fail( "Expected an IdentityNotFoundException to be thrown" );
// }
// catch( IdentityStoreException e )
// {
// // Correct behavior
// }
// }
//
// public void testUpdateIdentityDeletedByCustomerId( )
// {
// Identity identityReference = IdentitystoreTestUtils.createIdentityInDatabase( );
// IdentityHome.softRemove( identityReference.getId( ) );
// IdentityDto identityDto = MockIdentityDto.create( identityReference );
// IdentityChangeDto identityChangeDto = createIdentityChangeDtoFor( identityDto );
// identityChangeDto.getIdentity( ).setConnectionId( null );
//
// try
// {
// IdentityStoreService.updateIdentity( identityChangeDto, new HashMap<>( ) );
// fail( "Expected an IdentityDeletedException to be thrown" );
// }
// catch( IdentityStoreException e )
// {
// // Correct behavior
// }
//
// }
//
// // ###########################
// // ##### Utility methods #####
// // ###########################
//
// private AttributeKey findAttributeKey( String strAttributeKeyName )
// {
// return AttributeKeyHome.findByKey( strAttributeKeyName );
// }
// }

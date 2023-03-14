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
package fr.paris.lutece.plugins.identitystore.v3.service;

import fr.paris.lutece.plugins.identitystore.IdentityStoreBDDAndESTestCase;
import fr.paris.lutece.plugins.identitystore.IdentityStoreTestContext;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.ArrayList;
import java.util.Date;

public class IdentityStoreResearchTest extends IdentityStoreBDDAndESTestCase
{

    public void testCase1( ) throws IdentityStoreException
    {
        System.out.println( "Test first case!" );
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        final RequestAuthor origin = new RequestAuthor( );
        origin.setName( "TEST UNITAIRES" );
        origin.setType( AuthorType.application );
        identityChangeRequest.setOrigin( origin );
        final Identity identity = new Identity( );
        final ArrayList<CertifiedAttribute> attributes = new ArrayList<>( );
        final CertifiedAttribute firstName = new CertifiedAttribute( );
        firstName.setCertificationProcess( "fc" );
        firstName.setValue( "Toto" );
        firstName.setKey( "first_name" );
        firstName.setCertificationDate( new Date( ) );
        attributes.add( firstName );
        final CertifiedAttribute familyName = new CertifiedAttribute( );
        familyName.setCertificationProcess( "fc" );
        familyName.setValue( "Durand" );
        familyName.setKey( "family_name" );
        familyName.setCertificationDate( new Date( ) );
        attributes.add( familyName );
        final CertifiedAttribute birthdate = new CertifiedAttribute( );
        birthdate.setCertificationProcess( "fc" );
        birthdate.setValue( "11/11/1965" );
        birthdate.setKey( "birthdate" );
        birthdate.setCertificationDate( new Date( ) );
        attributes.add( birthdate );
        identity.setAttributes( attributes );
        identityChangeRequest.setIdentity( identity );

        final IdentityChangeResponse response = new IdentityChangeResponse( );
        fr.paris.lutece.plugins.identitystore.business.identity.Identity createdIdentity = IdentityService.instance( ).create( identityChangeRequest,
                IdentityStoreTestContext.SAMPLE_APPCODE, response );
        System.out.println( response.getStatus( ).getLabel( ) );
        assertNotNull( createdIdentity );
        System.out.println( "End of test 1!" );
    }
}

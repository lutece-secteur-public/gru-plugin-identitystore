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
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AppRightDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.ApplicationRightsDto;
import fr.paris.lutece.test.LuteceTestCase;

/**
 *
 * @author levy
 */
public class IdentityStoreServiceApplicationRightsTest extends LuteceTestCase
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

    public void testGetApplicationRights( ) // TODO update with service contract
    {
        // ApplicationRightsDto apprights = IdentityStoreService.getApplicationRights( IdentityStoreTestContext.SAMPLE_APPCODE );
        // assertEquals( IdentityStoreTestContext.SAMPLE_APPCODE, apprights.getApplicationCode( ) );
        // assertEquals( IdentityStoreTestContext.SAMPLE_NB_ATTR, apprights.getAppRights( ).size( ) ); // cf init_db_identitystore_sample.sql
        // for ( AppRightDto appright : apprights.getAppRights( ) )
        // {
        // int nSizeCertifier = 0;
        // if ( IdentityStoreTestContext.ATTRKEY_1.equals( appright.getAttributeKey( ) ) )
        // {
        // nSizeCertifier = 5;
        // }
        // if ( IdentityStoreTestContext.ATTRKEY_3.equals( appright.getAttributeKey( ) ) )
        // {
        // nSizeCertifier = 4;
        // }
        // assertEquals( appright.getAttributeKey( ) + " READABLE", true, appright.isReadable( ) );
        // assertEquals( appright.getAttributeKey( ) + " WRITABLE", true, appright.isWritable( ) );
        // if ( nSizeCertifier == 0 )
        // {
        // assertTrue( appright.getCertifiers( ) == null || appright.getCertifiers( ).size( ) == 0 );
        // }
        // else
        // {
        // assertEquals( appright.getAttributeKey( ) + " CERTIFIER", nSizeCertifier, appright.getCertifiers( ).size( ) );
        // }
        // }
    }
}

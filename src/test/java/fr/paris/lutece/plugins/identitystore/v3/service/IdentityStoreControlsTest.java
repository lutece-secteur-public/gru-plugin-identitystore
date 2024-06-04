/*
 * Copyright (c) 2002-2024, City of Paris
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

import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityAttributeValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.test.LuteceTestCase;

import static fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils.DEC;
import static fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils.NUM1;
import static fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils.ORIG1;
import static fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils.getMockIdentityDto;
// import static fr.paris.lutece.plugins.identitystore.web.rs.dto.MockIdentityChangeDto.createIdentityChangeDtoFor;

/**
 * Identity controls tests
 * 
 * mvn clean lutece:exploded antrun:run -Dlutece-test-hsql test -Dtest=IdentityStoreControlsTest
 * 
 * @author SLE
 */
public class IdentityStoreControlsTest extends LuteceTestCase
{
    String _strClientCode = "TEST";
    private final String EOL = System.lineSeparator( );

    /**
     * Test attributes integrity CREATION Ok cases
     */
    public void testControlAttributesIntegrityCreateOK( )
    {
        IdentityDto newIdentity = getMockIdentityDto("1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, "1.1. Create Identity with all attributes at level 400" );

        newIdentity = getMockIdentityDto( "1", DEC, "Dupoon", DEC, "Marcel", DEC, null, 0, null, 0, null, 0 );
        tryPivotAttributesIntegrity( null, newIdentity, "1.2. Create Identity with 3 attributes at level 100" );

        newIdentity = getMockIdentityDto( "1", DEC, "Dupoon", DEC, "Marcel", DEC, "01/01/2000", DEC, "75112", DEC, "99100", DEC );
        tryPivotAttributesIntegrity( null, newIdentity, "1.3. Create Identity with all attributes at level 100" );

        newIdentity = getMockIdentityDto( "1", DEC, "Dupoon", DEC, "Marcel", DEC, null, 0, "75112", DEC, "99100", DEC );
        tryPivotAttributesIntegrity( null, newIdentity, "1.4. Create Identity with all attributes at level 400, but with empty date" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, null, 0, "99109", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, "1.5. Create Identity with 5 attributes at level 400 and foreign birthcountry" );
    }

    /**
     * Test attributes integrity UPDATE Ok cases
     */
    public void testControlAttributesIntegrityUpdateOK( )
    {
        IdentityDto existingIdentity_DEC = getMockIdentityDto( "1", DEC, "Dupoon", DEC, "Marcel", DEC, "01/01/2000", DEC, "75112", DEC, "99100", DEC );
        IdentityDto existingIncompleteIdentity_NUM1 = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, null, 0, null, 0, null, 0 );
        IdentityDto existingIdentity_NUM1 = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );

        IdentityDto udpatedIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75111", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( existingIdentity_DEC, udpatedIdentity,
                "2.1. Update one attribute of existing Identity (level 100)  with  attributes at level 400" );

        udpatedIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75111", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( existingIdentity_NUM1, udpatedIdentity,
                "2.2. Update one attribute of existing Identity with all attributes at level 400" );

        // This test should return OK, because all the updates will be refused, so the integrity of the pivot attributes is not broken
        // But if some non pivot attribute is sent in the request, it could be updated, so the resulting status could be INCOMPLETE_SUCCESS
        udpatedIdentity = getMockIdentityDto( "1", DEC, "Dupoon", DEC, "Marcel", DEC, "01/01/2000", DEC, "75111", DEC, "99100", DEC );
        tryPivotAttributesIntegrity( existingIdentity_NUM1, udpatedIdentity, "2.3. Update  existing Identity (level 400)  with 6 attributes at level 100" );

        udpatedIdentity = getMockIdentityDto( null, 0, null, 0, null, 0, null, 0, "", NUM1, "99109", NUM1 );
        tryPivotAttributesIntegrity( existingIdentity_NUM1, udpatedIdentity, "2.4. Update birthcountry of existing Identity with all attributes at level 400" );

        udpatedIdentity = getMockIdentityDto( null, 0, null, 0, null, 0, "01/01/2000", NUM1, "", NUM1, "99109", NUM1 );
        tryPivotAttributesIntegrity( existingIncompleteIdentity_NUM1, udpatedIdentity,
                "2.5. Update birthcountry of existing incomplete Identity with all attributes at level 400" );

        udpatedIdentity = getMockIdentityDto( null, 0, null, 0, null, 0, null, 0, "", DEC, null, 0 );
        tryPivotAttributesIntegrity( existingIdentity_NUM1, udpatedIdentity,
                "2.6. Update birthplace code country of existing Identity at level 400 and a foreign birthcountry (this update should be ignored)" );

    }

    /**
     * Test attributes integrity CREATE KO cases
     */
    public void testControlAttributesIntegrityCreateKO( )
    {
        IdentityDto newIdentity = getMockIdentityDto( null, 0, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class, "3.1. Create Identity with all attributes at level 400, without gender" );

        newIdentity = getMockIdentityDto( "1", NUM1, null, 0, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class, "3.2. Create Identity with all attributes at level 400, without name" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, null, 0, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class,
                "3.3. Create Identity with all attributes at level 400, without firstname" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, null, 0, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class,
                "3.4. Create Identity with all attributes at level 400, without birthdate" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, null, 0, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class,
                "3.5. Create Identity with all attributes at level 400, without birthplace" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, null, 0 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class,
                "3.6. Create Identity with all attributes at level 400, without birthcountry" );

        newIdentity = getMockIdentityDto( "1", NUM1, "Dupoon", ORIG1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );
        tryPivotAttributesIntegrity( null, newIdentity, RequestFormatException.class, "3.7. Create Identity with 5 attributes at level 400, and 1 at level 500" );
    }

    /**
     * Test attributes integrity UPDATE KO cases
     */
    public void testControlAttributesIntegrityUpdateKO( )
    {
        IdentityDto existingIncompleteIdentity_NUM1 = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, null, 0, null, 0, null, 0 );
        IdentityDto existingIdentity_NUM1 = getMockIdentityDto( "1", NUM1, "Dupoon", NUM1, "Marcel", NUM1, "01/01/2000", NUM1, "75112", NUM1, "99100", NUM1 );

        IdentityDto udpatedIdentity = getMockIdentityDto( null, 0, "Dupoon", ORIG1, null, 0, null, 0, null, 0, null, 0 );
        tryPivotAttributesIntegrity( existingIdentity_NUM1, udpatedIdentity, RequestFormatException.class,
                "4.1. Update  existing Identity (level 400)  with 1 attribute at level 500" );

        udpatedIdentity = getMockIdentityDto( null, 0, "Dupoon", ORIG1, null, 0, null, 0, null, 0, null, 0 );
        tryPivotAttributesIntegrity( existingIncompleteIdentity_NUM1, udpatedIdentity, RequestFormatException.class,
                "4.2. Update  existing incomplete Identity (level 100)  with 1 attribute at level 500" );

    }

    /**
     * Tests that should pass
     * 
     * @param cuid
     * @param identity
     */
    private void tryPivotAttributesIntegrity( IdentityDto existingIdentity, IdentityDto requestIdentity, String strTestCase )
    {
        tryPivotAttributesIntegrity( existingIdentity, requestIdentity, null, strTestCase );
    }

    /**
     * Pivot attributes integrity test
     */
    private void tryPivotAttributesIntegrity(final IdentityDto existingIdentity, final IdentityDto requestIdentity,
                                             final Class<? extends IdentityStoreException> expectedException, final String strTestCase)
    {
        try
        {
            // test
            IdentityAttributeValidator.instance( ).validatePivotAttributesIntegrity( existingIdentity, requestIdentity, false );

            // if no exception, the validation has passed successfully
            assertNull(strTestCase + " : expected to fail pivot attributes integrity validation, but validation passed successfully.",
                       expectedException);
        }
        catch( final Exception e )
        {
            assertEquals(strTestCase + " : " + e.getMessage( ),
                         expectedException,
                         e.getClass());
        }
    }

    /**
     * id to str
     * 
     * @param identity
     * @return string
     */
    private String getIdentityToString( IdentityDto identity )
    {
        if ( identity == null )
            return "none" + EOL;

        StringBuilder str = new StringBuilder( "| " );

        for ( AttributeDto attr : identity.getAttributes( ) )
        {
            str.append( attr.getKey( ) ).append( " : " ).append( attr.getValue( ) ).append( " (" ).append( attr.getCertifier( ) ).append( " " )
                    .append( attr.getCertificationLevel( ) ).append( " ) | " );
        }
        str.append( EOL );

        return str.toString( );
    }

    /**
     * getFullResponseStatusAsString
     * 
     * @param response
     * @return string
     */
    private String getFullResponseStatusAsString( IdentityChangeResponse response )
    {
        StringBuilder str = new StringBuilder( );
        str.append( "Resulting status [" ).append( response.getStatus( ).getType( ) ).append( "] " ).append( response.getStatus( ).getMessage( ) );
        for ( AttributeStatus status : response.getStatus( ).getAttributeStatuses( ) )
        {
            str.append( EOL ).append( " * " ).append( status.getKey( ) ).append( " : " ).append( status.getMessage( ) );
        }
        str.append( EOL );

        return str.toString( );
    }

}

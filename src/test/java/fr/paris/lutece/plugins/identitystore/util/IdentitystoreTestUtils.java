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
package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;

public class IdentitystoreTestUtils
{
    public static final int NO_CERTIFICATE_EXPIRATION_DELAY = -1;

    public static void generateFileFromTemplate( final String basePath, final String templatePath, final Map<String, String> params, final String filePath )
            throws Exception
    {
        final List<String> templateLines = Files.readAllLines( Paths.get( basePath, templatePath ) );
        final String template = String.join( "\n", templateLines );
        final Path destPath = Paths.get( basePath, filePath );
        if ( params != null && !params.isEmpty( ) )
        {
            final String content = StrSubstitutor.replace( template, params, "${", "}" );
            Files.write( destPath, content.getBytes( StandardCharsets.UTF_8 ) );
        }
        else
        {
            Files.write( destPath, template.getBytes( StandardCharsets.UTF_8 ) );
        }
    }

    public static Identity createIdentityInDatabase( )
    {
        Identity identity = createIdentity( );
        identity = IdentityHome.create( identity );

        return identity;
    }

    public static Identity createIdentity( )
    {
        Identity identity = new Identity( );
        Map<String, IdentityAttribute> mapAttributes = new HashMap<>( );

        identity.setCustomerId( UUID.randomUUID( ).toString( ) );
        identity.setConnectionId( UUID.randomUUID( ).toString( ) );
        identity.setAttributes( mapAttributes );

        return identity;
    }

    public static IdentityAttribute createIdentityAttributeInDatabase( Identity identity, AttributeKey attributeKey )
    {
        IdentityAttribute identityAttribute = createIdentityAttribute( identity, attributeKey );
        identityAttribute = IdentityAttributeHome.create( identityAttribute );

        identity.getAttributes( ).put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );

        return identityAttribute;
    }

    public static IdentityAttribute createIdentityAttributeInDatabase( Identity identity, AttributeKey attributeKey, String strValue )
    {
        IdentityAttribute identityAttribute = createIdentityAttribute( identity, attributeKey );
        identityAttribute.setValue( strValue );
        identityAttribute = IdentityAttributeHome.create( identityAttribute );

        identity.getAttributes( ).put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );

        return identityAttribute;
    }

    public static IdentityAttribute createIdentityAttributeInDatabase( Identity identity, AttributeKey attributeKey, String strValue,
            AttributeCertificate attributeCertificate )
    {
        IdentityAttribute identityAttribute = createIdentityAttribute( identity, attributeKey );
        identityAttribute.setValue( strValue );
        identityAttribute.setIdCertificate( attributeCertificate.getId( ) );
        identityAttribute = IdentityAttributeHome.create( identityAttribute );

        identity.getAttributes( ).put( identityAttribute.getAttributeKey( ).getKeyName( ), identityAttribute );

        return identityAttribute;
    }

    public static AttributeCertificate createAttributeCertificate( int _nCertificateLevel, String _strName, String _strCode, int _nExpirationDelay )
    {
        final AttributeCertificate certificate = new AttributeCertificate( );
        certificate.setCertificateDate( new Timestamp( new Date( ).getTime( ) ) );
        certificate.setCertificateLevel( _nCertificateLevel );
        certificate.setCertifierName( _strName );
        certificate.setCertifierCode( _strCode );

        if ( _nExpirationDelay != NO_CERTIFICATE_EXPIRATION_DELAY )
        {
            Calendar c = Calendar.getInstance( );
            c.setTime( new Date( ) );
            c.add( Calendar.DATE, _nExpirationDelay );
            certificate.setExpirationDate( new Timestamp( c.getTime( ).getTime( ) ) );
        }
        return certificate;
    }

    public static IdentityAttribute createIdentityAttribute( Identity identity, AttributeKey attributeKey )
    {
        IdentityAttribute attribute = new IdentityAttribute( );
        attribute.setAttributeKey( attributeKey );
        attribute.setIdIdentity( identity.getId( ) );

        return attribute;
    }

    public static AttributeCertificate createAttributeCertificateInDatabase( int _nCertificateLevel, String _strName, String _strCode, int _nExpirationDelay )
    {
        AttributeCertificate attributeCertificate = createAttributeCertificate( _nCertificateLevel, _strName, _strCode, _nExpirationDelay );
        AttributeCertificateHome.create( attributeCertificate );

        return attributeCertificate;
    }

    public static AttributeCertificate createExpiredAttributeCertificateInDatabase( int _nCertificateLevel, String _strName, String _strCode )
    {
        AttributeCertificate attributeCertificate = createAttributeCertificate( _nCertificateLevel, _strName, _strCode, -1 );
        attributeCertificate.setExpirationDate( createExpiredTimestamp( ) );
        AttributeCertificateHome.create( attributeCertificate );

        return attributeCertificate;
    }

    private static Timestamp createExpiredTimestamp( )
    {
        Calendar c = Calendar.getInstance( );
        c.add( Calendar.DATE, -1 );

        return new Timestamp( c.getTime( ).getTime( ) );
    }
}

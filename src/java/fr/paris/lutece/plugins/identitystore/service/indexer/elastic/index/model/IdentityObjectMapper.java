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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model;

import com.google.common.util.concurrent.AtomicDouble;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Hit;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentityObjectMapper
{
    private static final Integer QUALITY_BASE = Integer.valueOf( AppPropertiesService.getProperty( "identitystore.identity.quality.base" ) );

    // public static final String FIRST_NAMES_DELIMITER = " ";

    public static IdentityObject map( final Identity identity )
    {
        // final String firstNameKey = AppPropertiesService.getProperty( IdentityConstants.PROPERTY_ATTRIBUTE_USER_NAME_GIVEN );
        final Map<String, AttributeObject> attributeObjects = new HashMap<>( );
        for ( final IdentityAttribute attribute : identity.getAttributes( ).values( ) )
        {
            // Object value;
            // if ( StringUtils.equals( firstNameKey, attribute.getAttributeKey( ).getKeyName( ) ) )
            // {
            // value = attribute.getValue( ).split(FIRST_NAMES_DELIMITER);
            // }
            // else
            // {
            // value = attribute.getValue( );
            // }

            final AttributeObject attributeObject = new AttributeObject( attribute.getAttributeKey( ).getName( ), attribute.getAttributeKey( ).getKeyName( ),
                    attribute.getAttributeKey( ).getKeyType( ).getCode( ), attribute.getAttributeKey( ).getKeyWeight( ), attribute.getValue( ),
                    attribute.getAttributeKey( ).getDescription( ), attribute.getAttributeKey( ).getPivot( ),
                    attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierCode( ) : null,
                    attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierName( ) : null,
                    attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateDate( ) : null,
                    attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateLevel( ) : null,
                    attribute.getCertificate( ) != null ? attribute.getCertificate( ).getExpirationDate( ) : null, attribute.getLastUpdateApplicationCode( ) );
            if ( attributeObjects.put( attributeObject.getKey( ), attributeObject ) != null )
            {
                throw new IllegalStateException( "Duplicate key" );
            }
        }
        return new IdentityObject( identity.getConnectionId( ), identity.getCustomerId( ), identity.getCreationDate( ), identity.getLastUpdateDate( ),
                attributeObjects );
    }

    public static QualifiedIdentity toQualifiedIdentity( final Hit hit, final List<SearchAttribute> searchAttributes )
    {
        final IdentityObject identityObject = hit.getSource( );
        final QualifiedIdentity identity = new QualifiedIdentity( );
        identity.setConnectionId( identityObject.getConnectionId( ) );
        identity.setCustomerId( identityObject.getCustomerId( ) );
        identity.setCreationDate( identity.getCreationDate( ) );
        identity.setLastUpdateDate( identity.getLastUpdateDate( ) );
        identityObject.getAttributes( ).forEach( ( s, attributeObject ) -> {
            final CertifiedAttribute attribute = new CertifiedAttribute( );
            attribute.setKey( s );
            // if ( attributeObject.getValue( ) instanceof String [ ] )
            // {
            // attribute.setValue( String.join(FIRST_NAMES_DELIMITER, (String [ ]) attributeObject.getValue( ) ) );
            // }
            // else
            // if ( attributeObject.getValue( ) instanceof String )
            // {
            // attribute.setValue( (String) attributeObject.getValue( ) );
            // }
            attribute.setValue( attributeObject.getValue( ) );
            attribute.setType( attributeObject.getType( ) );
            attribute.setCertifier( attributeObject.getCertifierCode( ) );
            attribute.setCertificationDate( attributeObject.getCertificateDate( ) );
            attribute.setCertificationLevel( attribute.getCertificationLevel( ) );
            identity.getAttributes( ).add( attribute );
        } );
        // TODO gérer la qualité
        identity.setScoring( computeMatchScore( identityObject.getAttributes( ), searchAttributes ) );
        identity.setQuality( computeQuality( identityObject.getAttributes( ) ) );
        return identity;
    }

    private static Double computeQuality( final Map<String, AttributeObject> attributes )
    {
        final AtomicInteger levels = new AtomicInteger( );
        attributes.forEach( ( s, attributeObject ) -> {
            if ( attributeObject.getWeight( ) > 0 )
            {
                final Integer certificateLevel = attributeObject.getCertificateLevel( ) != null ? attributeObject.getCertificateLevel( ) : 0;
                levels.addAndGet( attributeObject.getWeight( ) * certificateLevel );
            }
        } );
        return levels.doubleValue( ) / QUALITY_BASE;
    }

    private static Double computeMatchScore( final Map<String, AttributeObject> attributes, final List<SearchAttribute> searchAttributes )
    {
        final AtomicDouble levels = new AtomicDouble( );
        final AtomicDouble base = new AtomicDouble( );
        searchAttributes.forEach( searchAttribute -> {
            final AttributeObject attributeObject = attributes.get( searchAttribute.getKey( ) );
            base.addAndGet( attributeObject.getWeight( ) );
            if ( attributeObject.getValue( ).equals( searchAttribute.getValue( ) ) )
            {
                levels.addAndGet( attributeObject.getWeight( ) );
            }
            else
            {
                levels.addAndGet( attributeObject.getWeight( ) - ( attributeObject.getWeight( ) * 0.1 ) );
            }
        } );

        return levels.doubleValue( ) / base.doubleValue( );
    }
}

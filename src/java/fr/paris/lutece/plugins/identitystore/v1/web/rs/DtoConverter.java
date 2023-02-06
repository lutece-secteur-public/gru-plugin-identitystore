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
package fr.paris.lutece.plugins.identitystore.v1.web.rs;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.AuthorType;
import fr.paris.lutece.portal.service.util.AppException;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * class to help managing rest feature
 *
 */
public final class DtoConverter
{
    private final static String UNDEFINED_FROM_OLD_VERSION = "UNDEFRINED_FROM_V1";

    /**
     * private constructor
     */
    private DtoConverter( )
    {
    }

    /**
     * returns a identityDto initialized from provided identity
     *
     * @param identity
     *            business identity to convert
     * @param nServiceContractId
     *            service contract id
     * @return identityDto initialized from provided identity
     */
    public static IdentityDto convertToDto( Identity identity, int nServiceContractId )
    {
        IdentityDto identityDto = new IdentityDto( );
        identityDto.setConnectionId( identity.getConnectionId( ) );
        identityDto.setCustomerId( identity.getCustomerId( ) );

        if ( identity.getAttributes( ) != null )
        {
            Map<String, AttributeDto> mapAttributeDto = new HashMap<String, AttributeDto>( );
            List<AttributeRight> lstRights = ServiceContractHome.findByPrimaryKey( nServiceContractId ).map( ServiceContractHome::selectApplicationRights )
                    .orElseThrow( ( ) -> new AppException( "Service Contract with the id " + nServiceContractId + " doesn't exist" ) );

            for ( IdentityAttribute attribute : identity.getAttributes( ).values( ) )
            {
                AttributeKey attributeKey = attribute.getAttributeKey( );

                AttributeDto attrDto = new AttributeDto( );
                attrDto.setKey( attributeKey.getKeyName( ) );
                attrDto.setValue( attribute.getValue( ) );
                attrDto.setType( attributeKey.getKeyType( ).getCode( ) );

                for ( AttributeRight attRight : lstRights )
                {
                    if ( attRight.getAttributeKey( ).getKeyName( ).equals( attributeKey.getKeyName( ) ) )
                    {
                        attrDto.setCertified( attribute.getCertificate( ) != null );

                        break;
                    }
                }

                if ( attribute.getCertificate( ) != null )
                {
                    CertificateDto certifDto = new CertificateDto( );
                    try
                    {
                        AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( attribute.getCertificate( ).getCertifierCode( ) );

                        certifDto.setCertificateExpirationDate( attribute.getCertificate( ).getExpirationDate( ) );
                        certifDto.setCertifierCode( attribute.getCertificate( ).getCertifierCode( ) );
                        certifDto.setCertifierName( certifier.getName( ) );
                        certifDto.setCertifierLevel( attribute.getCertificate( ).getCertificateLevel( ) );
                    }
                    catch( CertifierNotFoundException e )
                    {
                        // Identity contrains attribute certified with a certifier not found;
                        // We dont populate the attrDto with an empty certificate
                    }
                    finally
                    {
                        attrDto.setCertificate( certifDto );
                    }

                }

                mapAttributeDto.put( attrDto.getKey( ), attrDto );
            }

            identityDto.setAttributes( mapAttributeDto );
        }

        return identityDto;
    }

    /**
     * returns ChangeAuthor read from authorDto
     *
     * @param authorDto
     *            authorDto (mandatory)
     * @return changeAuthor initialized from Dto datas
     * @throws AppException
     *             if provided dto is null
     */
    public static ChangeAuthor getAuthor( AuthorDto authorDto )
    {
        ChangeAuthor author = null;

        if ( authorDto != null )
        {
            author = new ChangeAuthor( );
            author.setApplicationCode( authorDto.getApplicationCode( ) );

            if ( ( authorDto.getType( ) != AuthorType.TYPE_APPLICATION.getTypeValue( ) )
                    && ( authorDto.getType( ) != AuthorType.TYPE_USER_ADMINISTRATOR.getTypeValue( ) )
                    && ( authorDto.getType( ) != AuthorType.TYPE_USER_OWNER.getTypeValue( ) ) )
            {
                throw new AppException( "type provided is unknown type=" + authorDto.getType( ) );
            }

            if ( ( authorDto.getType( ) == AuthorType.TYPE_USER_ADMINISTRATOR.getTypeValue( ) ) && StringUtils.isEmpty( authorDto.getId( ) ) )
            {
                throw new AppException( "id field is missing" );
            }

            author.setType( authorDto.getType( ) );
        }
        else
        {
            throw new AppException( "no author provided" );
        }

        return author;
    }

    /**
     * returns certificate from Dto
     *
     * @param certificateDto
     *            certificate dto (can be null)
     * @return certificate initialized from Dto datas, null if provided dto is null
     * @throws AppException
     *             if expiration date is already expired
     * @throws CertifierNotFoundException
     *             if certifier not found for given code
     *
     */
    public static AttributeCertificate getCertificate( CertificateDto certificateDto ) throws AppException, CertifierNotFoundException
    {
        AttributeCertificate attributeCertificate = null;

        if ( certificateDto != null )
        {
            attributeCertificate = new AttributeCertificate( );
            attributeCertificate.setCertificateLevel( certificateDto.getCertifierLevel( ) );
            attributeCertificate.setCertifierCode( certificateDto.getCertifierCode( ) );
            attributeCertificate.setCertifierName( certificateDto.getCertifierName( ) );

            // check existence of certifier, with given certifier code; throws CertifierNotFoundException
            CertifierRegistry.instance( ).getCertifier( attributeCertificate.getCertifierCode( ) );

            attributeCertificate.setCertificateDate( new Timestamp( ( new Date( ) ).getTime( ) ) );
            if ( ( certificateDto.getCertificateExpirationDate( ) != null ) && certificateDto.getCertificateExpirationDate( ).before( new Date( ) ) )
            {
                throw new AppException( "Certificate expiration date is expired =" + certificateDto.getCertificateExpirationDate( ) );
            }

            if ( certificateDto.getCertificateExpirationDate( ) != null )
            {
                attributeCertificate.setExpirationDate( new Timestamp( ( certificateDto.getCertificateExpirationDate( ) ).getTime( ) ) );
            }

            return attributeCertificate;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an identityDto from V1 version to V2 version
     * 
     * @param identityDtoOldVersion
     * @return identityDto from package v2
     */
    public static IdentityDto convertToIdentityDtoNewVersion( fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto identityDtoOldVersion )
    {
        IdentityDto identityDto = new IdentityDto( );

        if ( identityDtoOldVersion != null )
        {
            identityDto.setConnectionId( identityDtoOldVersion.getConnectionId( ) );
            identityDto.setCustomerId( ( identityDtoOldVersion.getCustomerId( ) ) );
            Map<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> mapAttributes = identityDtoOldVersion.getAttributes( );

            Map<String, fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto> newMapAttributeOldVersion = new HashMap<String, fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AttributeDto>( );

            for ( Map.Entry<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> attributeEntry : mapAttributes.entrySet( ) )
            {
                newMapAttributeOldVersion.put( attributeEntry.getKey( ), convertToAttributeDtoNewVersion( attributeEntry.getValue( ) ) );
            }

            identityDto.setAttributes( newMapAttributeOldVersion );

            return identityDto;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an AttributeDto from V1 version to V2 version
     * 
     * @param attributeDtoOldVersion
     * @return attributeDto from package v2
     */
    protected static AttributeDto convertToAttributeDtoNewVersion( fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto attributeDtoOldVersion )
    {

        AttributeDto attributeDto = new AttributeDto( );

        if ( attributeDtoOldVersion != null )
        {
            attributeDto.setLastUpdateApplicationCode( null );
            attributeDto.setCertificate( convertToCertificateDtoNewVersion( attributeDtoOldVersion.getCertificate( ) ) );
            attributeDto.setCertified( attributeDtoOldVersion.getCertified( ) );
            attributeDto.setKey( attributeDtoOldVersion.getKey( ) );
            attributeDto.setType( attributeDtoOldVersion.getType( ) );
            attributeDto.setValue( attributeDtoOldVersion.getValue( ) );

            return attributeDto;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an CertificateDto from V1 version to V2 version
     * 
     * @param certificateDtoOldVersion
     * @return certificateDto from package v2
     */
    public static CertificateDto convertToCertificateDtoNewVersion(
            fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.CertificateDto certificateDtoOldVersion )
    {

        CertificateDto certificateDto = new CertificateDto( );

        if ( certificateDtoOldVersion != null )
        {
            certificateDto.setCertificateExpirationDate( certificateDtoOldVersion.getCertificateExpirationDate( ) );
            certificateDto.setCertifierCode( certificateDtoOldVersion.getCertifierCode( ) );
            certificateDto.setCertifierLevel( certificateDtoOldVersion.getCertifierLevel( ) );
            certificateDto.setCertifierName( certificateDtoOldVersion.getCertifierName( ) );

            return certificateDto;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an certificateDto from V2 version to V1 version
     * 
     * @param certificateDto
     * @return certificateDtoOldVersion from package v1
     */
    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.CertificateDto convertToCertificateDtoOldVersion( CertificateDto certificateDto )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.CertificateDto certificateDtoOldVersion = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.CertificateDto( );

        if ( certificateDto != null )
        {
            certificateDtoOldVersion.setCertificateExpirationDate( certificateDto.getCertificateExpirationDate( ) );
            certificateDtoOldVersion.setCertifierCode( certificateDto.getCertifierCode( ) );
            certificateDtoOldVersion.setCertifierLevel( certificateDto.getCertifierLevel( ) );
            certificateDtoOldVersion.setCertifierName( certificateDto.getCertifierName( ) );

            return certificateDtoOldVersion;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an authorDto from V1 version to V2 version
     * 
     * @param authorDtoOldVersion
     * @return authorDto from package v2
     */
    public static AuthorDto convertToAuthorDtoNewVersion( fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto authorDtoOldVersion )
    {
        AuthorDto authorDto = new AuthorDto( );

        if ( authorDtoOldVersion != null )
        {
            authorDto.setId( UNDEFINED_FROM_OLD_VERSION );
            authorDto.setType( authorDtoOldVersion.getType( ) );
            authorDto.setApplicationCode( authorDtoOldVersion.getApplicationCode( ) );

            return authorDto;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an authorDto from V2 version to V1 version
     * 
     * @param authorDto
     * @return authorDtoOldVersion from package v1
     */
    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto convertToAuthorDtoOldVersion( AuthorDto authorDto )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto authorDtoOldVersion = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AuthorDto( );

        if ( authorDto != null )
        {
            authorDtoOldVersion.setType( authorDto.getType( ) );
            authorDtoOldVersion.setApplicationCode( authorDto.getId( ) );

            return authorDtoOldVersion;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an identityDto from V2 version to V1 version
     * 
     * @param identityDto
     * @return identityDtoOldVersion from package v1
     */
    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto convertToIdentityDtoOldVersion( IdentityDto identityDto )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto identityDtoOldVersion = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto( );

        if ( identityDto != null )
        {
            identityDtoOldVersion.setConnectionId( identityDto.getConnectionId( ) );
            identityDtoOldVersion.setCustomerId( ( identityDto.getCustomerId( ) ) );
            Map<String, AttributeDto> mapAttributes = identityDto.getAttributes( );

            Map<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> newMapAttributeOldVersion = new HashMap<String, fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto>( );

            for ( Map.Entry<String, AttributeDto> attributeEntry : mapAttributes.entrySet( ) )
            {
                newMapAttributeOldVersion.put( attributeEntry.getKey( ), convertToAttributeDtoOldVersion( attributeEntry.getValue( ) ) );
            }

            identityDtoOldVersion.setAttributes( newMapAttributeOldVersion );

            return identityDtoOldVersion;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert a list of AttributesDto from V2 version to V1 version
     * 
     * @param List
     *            of AttributeDto
     * @return List of AttributeDto from package v1
     */
    public static List<fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> convertToListAttributeDtoOldVersion(
            List<AttributeDto> listAttributesDto )
    {
        List<fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto> listAttributesDtoV1 = new ArrayList<>( );

        for ( AttributeDto attributeDto : listAttributesDto )
        {
            listAttributesDtoV1.add( convertToAttributeDtoOldVersion( attributeDto ) );
        }

        return listAttributesDtoV1;
    }

    /**
     * Convert an AttributesDto from V2 version to V1 version
     * 
     * @param attributeDto
     * @return attributeDto from package v1
     */
    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto convertToAttributeDtoOldVersion( AttributeDto attributeDto )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto attributeDtoOldVersion = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto( );

        if ( attributeDto != null )
        {
            attributeDtoOldVersion.setCertificate( convertToCertificateDtoOldVersion( attributeDto.getCertificate( ) ) );
            attributeDtoOldVersion.setCertified( attributeDto.getCertified( ) );
            attributeDtoOldVersion.setKey( attributeDto.getKey( ) );
            attributeDtoOldVersion.setType( attributeDto.getType( ) );
            attributeDtoOldVersion.setValue( attributeDto.getValue( ) );

            return attributeDtoOldVersion;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an IdentityChangeDto from V1 version to V2 version
     * 
     * @param identityChangeDtoOldVersion
     * @return identityChangeDto from package v2
     */
    public static IdentityChangeDto convertToIdentityChangeDtoNewVersion(
            fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto identityChangeDtoOldVersion )
    {
        IdentityChangeDto identityChangeDto = new IdentityChangeDto( );

        if ( identityChangeDtoOldVersion != null )
        {
            identityChangeDto.setAuthor( convertToAuthorDtoNewVersion( identityChangeDtoOldVersion.getAuthor( ) ) );
            identityChangeDto.setIdentity( convertToIdentityDtoNewVersion( identityChangeDtoOldVersion.getIdentity( ) ) );

            return identityChangeDto;
        }
        else
        {
            return null;
        }
    }

    /**
     * Convert an IdentityChangeDto from V1 version to V2 version
     * 
     * @param identityChangeDtoOldVersion
     * @param certifier
     *            the certifier
     * @return identityChangeDto from package v2
     */
    public static IdentityChangeDto convertToIdentityChangeDtoNewVersionWithCertificate(
            fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto identityChangeDtoOldVersion, AbstractCertifier certifier )
    {
        IdentityChangeDto identityChangeDto = convertToIdentityChangeDtoNewVersion( identityChangeDtoOldVersion );

        CertificateDto certificateDto = new CertificateDto( );

        if ( certifier.getExpirationDelay( ) != AbstractCertifier.NO_CERTIFICATE_EXPIRATION_DELAY )
        {
            Calendar calendar = Calendar.getInstance( );
            calendar.setTime( new Date( ) );
            calendar.add( Calendar.DATE, certifier.getExpirationDelay( ) );
            certificateDto.setCertificateExpirationDate( new Timestamp( calendar.getTime( ).getTime( ) ) );
        }

        certificateDto.setCertifierCode( certifier.getCode( ) );
        certificateDto.setCertifierLevel( certifier.getCertificateLevel( ) );
        certificateDto.setCertifierName( certifier.getName( ) );

        if ( identityChangeDto != null )
        {
            identityChangeDto.getIdentity( ).getAttributes( ).entrySet( ).forEach( attribute -> attribute.getValue( ).setCertificate( certificateDto ) );
        }
        return identityChangeDto;
    }

    /**
     * Convert an IdentityChangeDto from V2 version to V1 version
     * 
     * @param identityChangeDto
     * @return identityChangeDtoOldVersion from package v1
     */
    public static fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto convertToIdentityChangeDtoOldVersion(
            IdentityChangeDto identityChangeDto )
    {
        fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto identityChangeDtoOldVersion = new fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto( );

        if ( identityChangeDto != null )
        {
            identityChangeDtoOldVersion.setAuthor( convertToAuthorDtoOldVersion( identityChangeDto.getAuthor( ) ) );
            identityChangeDtoOldVersion.setIdentity( convertToIdentityDtoOldVersion( identityChangeDto.getIdentity( ) ) );

            return identityChangeDtoOldVersion;
        }
        else
        {
            return null;
        }
    }
}

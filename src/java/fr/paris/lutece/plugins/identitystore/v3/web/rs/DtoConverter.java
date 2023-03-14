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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.ChangeAuthor;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.AuthorType;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.AuthorDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.CertificateDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.portal.service.util.AppException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * class to help managing rest feature
 *
 */
public final class DtoConverter
{
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
     * @return identityDto initialized from provided identity
     */
    public static QualifiedIdentity convertIdentityToDto( final Identity identity )
    {
        final QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( );
        qualifiedIdentity.setConnectionId( identity.getConnectionId( ) );
        qualifiedIdentity.setCustomerId( identity.getCustomerId( ) );
        qualifiedIdentity.setMerged( identity.isMerged( ) );

        if ( MapUtils.isNotEmpty( identity.getAttributes( ) ) )
        {
            for ( final IdentityAttribute attribute : identity.getAttributes( ).values( ) )
            {
                final AttributeKey attributeKey = attribute.getAttributeKey( );

                final CertifiedAttribute certifiedAttribute = new CertifiedAttribute( );
                certifiedAttribute.setKey( attributeKey.getKeyName( ) );
                certifiedAttribute.setValue( attribute.getValue( ) );
                certifiedAttribute.setType( attributeKey.getKeyType( ).getCode( ) );

                if ( attribute.getCertificate( ) != null )
                {
                    certifiedAttribute.setCertifier( attribute.getCertificate( ).getCertifierCode( ) );
                    certifiedAttribute.setCertificationLevel( attribute.getCertificate( ).getCertificateLevel( ) );
                    certifiedAttribute.setCertificationDate( attribute.getCertificate( ).getCertificateDate( ) );

                }
                qualifiedIdentity.getAttributes( ).add( certifiedAttribute );
            }
        }

        return qualifiedIdentity;
    }

    /**
     * returns a serviceContractDto initialized from provided serviceContract
     *
     * @param serviceContract
     *            business service contract to convert
     * @return serviceContractDto initialized from provided serviceContract
     */
    public static ServiceContractDto convertContractToDto( final ServiceContract serviceContract )
    {
        final ServiceContractDto serviceContractDto = new ServiceContractDto( );
        serviceContractDto.setName( serviceContract.getName( ) );
        serviceContractDto.setServiceType( serviceContract.getServiceType( ) );
        serviceContractDto.setContactName( serviceContract.getContactName( ) );
        serviceContractDto.setOrganizationalEntity( serviceContract.getOrganizationalEntity( ) );
        serviceContractDto.setResponsibleName( serviceContract.getResponsibleName( ) );
        serviceContractDto.setStartingDate( serviceContract.getStartingDate( ) );
        serviceContractDto.setEndingDate( serviceContract.getEndingDate( ) );
        serviceContractDto.setAuthorizedAccountUpdate( serviceContract.getAuthorizedAccountUpdate( ) );
        serviceContractDto.setAuthorizedDeletion( serviceContract.getAuthorizedDeletion( ) );
        serviceContractDto.setAuthorizedExport( serviceContract.getAuthorizedExport( ) );
        serviceContractDto.setAuthorizedImport( serviceContract.getAuthorizedImport( ) );
        serviceContractDto.setAuthorizedMerge( serviceContract.getAuthorizedMerge( ) );
        serviceContractDto.setIsAuthorizedDeleteCertificate( serviceContract.getAuthorizedDeleteCertificate( ) );
        serviceContractDto.setIsAuthorizedDeleteValue( serviceContract.getAuthorizedDeleteValue( ) );

        final List<AttributeDefinitionDto> attributeDefinitions = new ArrayList<>( );

        for ( final AttributeRight attributeRight : serviceContract.getAttributeRights( ) )
        {
            final AttributeDefinitionDto attributeDefinitionDto = new AttributeDefinitionDto( );
            attributeDefinitionDto.setName( attributeRight.getAttributeKey( ).getName( ) );
            attributeDefinitionDto.setKeyName( attributeRight.getAttributeKey( ).getKeyName( ) );
            attributeDefinitionDto.setDescription( attributeRight.getAttributeKey( ).getDescription( ) );
            attributeDefinitionDto.setType( AttributeType.valueOf( attributeRight.getAttributeKey( ).getKeyType( ).name( ) ) );
            attributeDefinitionDto.setCertifiable( attributeRight.getAttributeKey( ).getCertifiable( ) );
            attributeDefinitionDto.setPivot( attributeRight.getAttributeKey( ).getPivot( ) );
            attributeDefinitionDto.setKeyWeight( attributeRight.getAttributeKey( ).getKeyWeight( ) );
            attributeDefinitionDto.setAttributeRight( new fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRight( ) );
            attributeDefinitionDto.getAttributeRight( ).setReadable( attributeRight.isReadable( ) );
            attributeDefinitionDto.getAttributeRight( ).setSearchable( attributeRight.isSearchable( ) );
            attributeDefinitionDto.getAttributeRight( ).setWritable( attributeRight.isWritable( ) );
            attributeDefinitions.add( attributeDefinitionDto );
        }

        for ( final AttributeCertification attributeCertification : serviceContract.getAttributeCertifications( ) )
        {
            AttributeDefinitionDto current = attributeDefinitions.stream( )
                    .filter( attributeDefinitionDto -> attributeDefinitionDto.getKeyName( ).equals( attributeCertification.getAttributeKey( ).getKeyName( ) ) )
                    .findFirst( ).orElse( null );
            if ( current == null )
            {
                current = new AttributeDefinitionDto( );
                current.setName( attributeCertification.getAttributeKey( ).getName( ) );
                current.setKeyName( attributeCertification.getAttributeKey( ).getKeyName( ) );
                current.setDescription( attributeCertification.getAttributeKey( ).getDescription( ) );
                current.setType( AttributeType.valueOf( attributeCertification.getAttributeKey( ).getKeyType( ).name( ) ) );
                current.setCertifiable( attributeCertification.getAttributeKey( ).getCertifiable( ) );
                current.setPivot( attributeCertification.getAttributeKey( ).getPivot( ) );
                current.setKeyWeight( attributeCertification.getAttributeKey( ).getKeyWeight( ) );
                attributeDefinitions.add( current );
            }

            final List<CertificationProcessus> certificationProcessuses = attributeCertification.getRefAttributeCertificationProcessus( ).stream( )
                    .map( ref -> {
                        final CertificationProcessus certificationProcessus = new CertificationProcessus( );
                        certificationProcessus.setCode( ref.getCode( ) );
                        certificationProcessus.setLabel( ref.getLabel( ) );
                        certificationProcessus.setLevel( ref.getLevel( ).getRefCertificationLevel( ).getLevel( ) );
                        return certificationProcessus;
                    } ).collect( Collectors.toList( ) );
            current.getAttributeCertifications( ).addAll( certificationProcessuses );
        }

        // TODO améliorer car la remontée n'est pas optimale pour ce UC
        final List<AttributeRequirement> attributeRequirements = serviceContract.getAttributeRequirements( ).stream( )
                .filter( attributeRequirement -> attributeRequirement.getRefCertificationLevel( ).getLevel( ) != null ).collect( Collectors.toList( ) );
        for ( final AttributeRequirement attributeRequirement : attributeRequirements )
        {
            AttributeDefinitionDto current = attributeDefinitions.stream( )
                    .filter( attributeDefinitionDto -> attributeDefinitionDto.getKeyName( ).equals( attributeRequirement.getAttributeKey( ).getKeyName( ) ) )
                    .findFirst( ).orElse( null );
            if ( current == null )
            {
                current = new AttributeDefinitionDto( );
                current.setName( attributeRequirement.getAttributeKey( ).getName( ) );
                current.setKeyName( attributeRequirement.getAttributeKey( ).getKeyName( ) );
                current.setDescription( attributeRequirement.getAttributeKey( ).getDescription( ) );
                current.setType( AttributeType.valueOf( attributeRequirement.getAttributeKey( ).getKeyType( ).name( ) ) );
                current.setCertifiable( attributeRequirement.getAttributeKey( ).getCertifiable( ) );
                current.setPivot( attributeRequirement.getAttributeKey( ).getPivot( ) );
                current.setKeyWeight( attributeRequirement.getAttributeKey( ).getKeyWeight( ) );
                attributeDefinitions.add( current );
            }
            fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirement requirement = new fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirement( );
            requirement.setLevel( attributeRequirement.getRefCertificationLevel( ).getLevel( ) );
            requirement.setName( attributeRequirement.getRefCertificationLevel( ).getName( ) );
            requirement.setDescription( attributeRequirement.getRefCertificationLevel( ).getDescription( ) );
            current.setAttributeRequirement( requirement );
        }

        serviceContractDto.getAttributeDefinitions( ).addAll( attributeDefinitions );

        return serviceContractDto;
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

            author.setAuthorId( authorDto.getId( ) );
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
        }

        return attributeCertificate;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.attribute;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.cache.AttributeKeyCache;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.sql.TransactionManager;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class IdentityAttributeService
{
    private static final String UNCERTIFY_PROCESSUS = "identitystore.identity.uncertify.processus";

    private final AttributeKeyCache _attributeKeyCache = SpringContextService.getBean( "identitystore.attributeKeyCache" );
    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );

    private static IdentityAttributeService _instance;

    public static IdentityAttributeService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityAttributeService( );
            _instance._attributeKeyCache.refresh( );
        }
        return _instance;
    }

    /**
     * Get all attributes
     */
    public List<AttributeKey> getAllAtributeKeys( )
    {
        if(_attributeKeyCache.isCacheEnable())
        {
            List<AttributeKey> attributeKeys = _attributeKeyCache.getAll();
            if( attributeKeys != null )
            {
                return attributeKeys;
            }
        }

        List<AttributeKey> attributeKeys =  AttributeKeyHome.getAttributeKeysList(true);
        if( attributeKeys != null && _attributeKeyCache.isCacheEnable() )
        {
            attributeKeys.forEach(
                    attributeKey -> _attributeKeyCache.put( attributeKey.getKeyName(), attributeKey ));

        }

        return attributeKeys;
    }

    /**
     * Get {@link AttributeKey} from its key name.
     * 
     * @param keyName
     *            the key name
     * @return {@link AttributeKey}
     * @throws ResourceNotFoundException
     *             if no {@link AttributeKey} exists for the provided key name.
     */
    public AttributeKey getAttributeKey( final String keyName ) throws ResourceNotFoundException
    {
        if(_attributeKeyCache.isCacheEnable())
        {
            AttributeKey attribute = _attributeKeyCache.get( keyName );
            if( attribute != null )
            {
                return attribute;
            }
        }

        AttributeKey attribute =  AttributeKeyHome.findByKey( keyName, true);
        if( attribute != null && _attributeKeyCache.isCacheEnable() )
        {
            _attributeKeyCache.put( attribute.getKeyName(), attribute );
        }

        return attribute;
    }

    /**
     * Get {@link AttributeKey} from its key name.
     * 
     * @param keyName
     *            the key name
     * @return {@link AttributeKey}, or <code>null</code> if no {@link AttributeKey} exists for the provided key name.
     */
    public AttributeKey getAttributeKeySafe( final String keyName )
    {
        if(_attributeKeyCache.isCacheEnable())
        {
            try
            {
                return _attributeKeyCache.get(keyName);
            } catch (final Exception e)
            {
                return null;
            }
        }
        return AttributeKeyHome.findByKey( keyName, true);
    }

    /**
     * Get all {@link AttributeKey} that are flaged as PIVOT.
     * 
     * @return {@link List<AttributeKey>}
     */
    public List<AttributeKey> getPivotAttributeKeys( )
    {
        if(_attributeKeyCache.isCacheEnable())
        {
            List<AttributeKey> attributeKeys = _attributeKeyCache.getAll().stream( ).filter( AttributeKey::getPivot ).collect( Collectors.toList( ) );
            if( attributeKeys != null )
            {
                return attributeKeys;
            }
        }

        List<AttributeKey> attributeKeys =  AttributeKeyHome.getMandatoryForCreationAttributeKeyList();
        if( attributeKeys != null && _attributeKeyCache.isCacheEnable() )
        {
            attributeKeys.forEach(
                    attributeKey -> _attributeKeyCache.put( attributeKey.getKeyName(), attributeKey ));

        }

        return attributeKeys;
    }

    public List<AttributeKey> getCommonAttributeKeys( final String keyName )
    {
        if(_attributeKeyCache.isCacheEnable())
        {
            if (_attributeKeyCache.getKeys().isEmpty())
            {
                _attributeKeyCache.refresh();
            }
            return _attributeKeyCache.getKeys().stream().map(this::getAttributeKeySafe).filter(Objects::nonNull)
                    .filter(attributeKey -> attributeKey.getCommonSearchKeyName() != null && Objects.equals(attributeKey.getCommonSearchKeyName(), keyName))
                    .collect(Collectors.toList());
        }

        List<String> attributeKeysnames =  AttributeKeyHome.getAttributeKeysNamesList();
        return attributeKeysnames.stream().map(this::getAttributeKeySafe).filter(Objects::nonNull)
                .filter(attributeKey -> attributeKey.getCommonSearchKeyName() != null && Objects.equals(attributeKey.getCommonSearchKeyName(), keyName))
                .collect(Collectors.toList());

    }

    public void createAttributeKey( final AttributeKey attributeKey ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            AttributeKeyHome.create( attributeKey );
            if(_attributeKeyCache.isCacheEnable())
            {
                _attributeKeyCache.put(attributeKey.getKeyName(), attributeKey);
            }
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    public void updateAttributeKey( final AttributeKey attributeKey ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            AttributeKeyHome.update( attributeKey );
            if(_attributeKeyCache.isCacheEnable())
            {
                _attributeKeyCache.put(attributeKey.getKeyName(), attributeKey);
            }
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    public void deleteAttributeKey( final AttributeKey attributeKey ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            AttributeKeyHome.remove( attributeKey.getId( ) );
            if(_attributeKeyCache.isCacheEnable())
            {
                _attributeKeyCache.removeKey(attributeKey.getKeyName());
            }
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    /**
     * Private method used to create an attribute for an identity.
     *
     * @param attributeToCreate
     * @param identity
     * @param clientCode
     * @return AttributeStatus
     * @throws IdentityStoreException
     */
    public AttributeStatus createAttribute( final AttributeDto attributeToCreate, final Identity identity, final String clientCode )
            throws IdentityStoreException
    {
        if ( StringUtils.isBlank( attributeToCreate.getValue( ) ) )
        {
            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToCreate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.NOT_CREATED );
            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_CREATED );
            return attributeStatus;
        }

        final IdentityAttribute attribute = new IdentityAttribute( );
        attribute.setIdIdentity( identity.getId( ) );
        attribute.setAttributeKey( getAttributeKey( attributeToCreate.getKey( ) ) );
        attribute.setValue( attributeToCreate.getValue( ) );
        attribute.setLastUpdateClientCode( clientCode );

        if ( attributeToCreate.getCertifier( ) != null )
        {
            final AttributeCertificate certificate = new AttributeCertificate( );
            certificate.setCertificateDate( new Timestamp( attributeToCreate.getCertificationDate( ).getTime( ) ) );
            certificate.setCertifierCode( attributeToCreate.getCertifier( ) );
            certificate.setCertifierName( attributeToCreate.getCertifier( ) ); // ?
            attribute.setCertificate( AttributeCertificateHome.create( certificate ) );
            attribute.setIdCertificate( attribute.getCertificate( ).getId( ) );
        }

        IdentityAttributeHome.create( attribute );
        identity.getAttributes( ).put( attribute.getAttributeKey( ).getKeyName( ), attribute );

        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attribute.getAttributeKey( ).getKeyName( ) );
        attributeStatus.setStatus( AttributeChangeStatus.CREATED );
        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_CREATED );

        return attributeStatus;
    }

    /**
     * private method used to update an attribute of an identity
     *
     * @param attributeToUpdate
     * @param identity
     * @param clientCode
     * @return AttributeStatus
     * @throws IdentityStoreException
     */
    public AttributeStatus updateAttribute( final AttributeDto attributeToUpdate, final Identity identity, final String clientCode )
            throws IdentityStoreException
    {
        final IdentityAttribute existingAttribute = identity.getAttributes( ).get( attributeToUpdate.getKey( ) );

        int attributeToUpdateLevelInt = _attributeCertificationDefinitionService.getLevelAsInteger( attributeToUpdate.getCertifier( ),
                attributeToUpdate.getKey( ) );
        int existingAttributeLevelInt = _attributeCertificationDefinitionService.getLevelAsInteger( existingAttribute.getCertificate( ).getCertifierCode( ),
                existingAttribute.getAttributeKey( ).getKeyName( ) );
        if ( attributeToUpdateLevelInt == existingAttributeLevelInt && StringUtils.equals( attributeToUpdate.getValue( ), existingAttribute.getValue( ) )
                && ( attributeToUpdate.getCertificationDate( ).equals( existingAttribute.getCertificate( ).getCertificateDate( ) )
                        || attributeToUpdate.getCertificationDate( ).before( existingAttribute.getCertificate( ).getCertificateDate( ) ) ) )
        {
            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToUpdate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.NOT_UPDATED );
            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_UPDATED );
            return attributeStatus;
        }
        if ( attributeToUpdateLevelInt >= existingAttributeLevelInt )
        {
            if ( StringUtils.isBlank( attributeToUpdate.getValue( ) ) )
            {
                // #232 : remove attribute if :
                // - attribute is not mandatory
                // - new value is null or blank
                // - sent certification level is >= to existing one
                final Optional<AttributeRight> right = _serviceContractService.getActiveServiceContract( clientCode ).getAttributeRights( ).stream( )
                        .filter( ar -> ar.getAttributeKey( ).getKeyName( ).equals( attributeToUpdate.getKey( ) ) ).findAny( );
                if ( right.isPresent( ) && right.get( ).isMandatory( ) )
                {
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( attributeToUpdate.getKey( ) );
                    attributeStatus.setStatus( AttributeChangeStatus.NOT_REMOVED );
                    attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_NOT_REMOVED );
                    return attributeStatus;
                }
                IdentityAttributeHome.remove( identity.getId( ), existingAttribute.getAttributeKey( ).getId( ) );
                identity.getAttributes( ).remove( existingAttribute.getAttributeKey( ).getKeyName( ) );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( attributeToUpdate.getKey( ) );
                attributeStatus.setStatus( AttributeChangeStatus.REMOVED );
                attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_REMOVED );
                return attributeStatus;
            }

            existingAttribute.setValue( attributeToUpdate.getValue( ) );
            existingAttribute.setLastUpdateClientCode( clientCode );

            if ( attributeToUpdate.getCertifier( ) != null )
            {
                final AttributeCertificate certificate = new AttributeCertificate( );
                certificate.setCertificateDate( new Timestamp( attributeToUpdate.getCertificationDate( ).getTime( ) ) );
                certificate.setCertifierCode( attributeToUpdate.getCertifier( ) );
                certificate.setCertifierName( attributeToUpdate.getCertifier( ) );

                existingAttribute.setCertificate( AttributeCertificateHome.create( certificate ) ); // TODO supprime-t-on l'ancien certificat ?
                existingAttribute.setIdCertificate( existingAttribute.getCertificate( ).getId( ) );
            }

            IdentityAttributeHome.update( existingAttribute );
            identity.getAttributes( ).put( existingAttribute.getAttributeKey( ).getKeyName( ), existingAttribute );

            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( attributeToUpdate.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.UPDATED );
            attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_UPDATED );
            return attributeStatus;
        }

        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attributeToUpdate.getKey( ) );
        attributeStatus.setStatus( AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL );
        attributeStatus.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_INSUFFICIENT_CERTIFICATION_LEVEL );
        return attributeStatus;
    }

    /**
     * Dé-certification d'un attribut.<br/>
     * Une identité ne pouvant pas posséder d'attributs non-certifiés, une dé-certification implique la certification de l'attribut avec le processus défini par
     * la property : <code>identitystore.identity.uncertify.processus</code> (par défaut : "dec", qui correspond au niveau le plus faible de certification
     * (auto-déclaratif))
     *
     * @param attribute
     *            l'attribut à dé-certifier
     * @return le status
     */
    public AttributeStatus uncertifyAttribute( final IdentityAttribute attribute ) throws IdentityStoreException
    {
        final AttributeStatus status = new AttributeStatus( );
        status.setKey( attribute.getAttributeKey( ).getKeyName( ) );

        final String processus = AppPropertiesService.getProperty( UNCERTIFY_PROCESSUS, "dec" );
        if ( StringUtils.isBlank( processus ) )
        {
            throw new IdentityStoreException( "No uncertification processus specified in property : " + UNCERTIFY_PROCESSUS );
        }

        final RefAttributeCertificationLevel certif = _attributeCertificationDefinitionService.get( processus, attribute.getAttributeKey( ).getKeyName( ) );
        if ( certif == null )
        {
            throw new IdentityStoreException( "No uncertification processus found in database for [code=" + processus + "][attributeKey="
                    + attribute.getAttributeKey( ).getKeyName( ) + "]" );
        }

        final AttributeCertificate certificate = new AttributeCertificate( );
        certificate.setCertificateDate( Timestamp.from( ZonedDateTime.now( ZoneId.systemDefault( ) ).toInstant( ) ) );
        certificate.setCertifierCode( certif.getRefAttributeCertificationProcessus( ).getCode( ) );

        attribute.setCertificate( AttributeCertificateHome.create( certificate ) ); // TODO supprime-t-on l'ancien certificat ?
        attribute.setIdCertificate( attribute.getCertificate( ).getId( ) );

        IdentityAttributeHome.update( attribute );

        status.setStatus( AttributeChangeStatus.UNCERTIFIED );
        status.setMessageKey( Constants.PROPERTY_ATTRIBUTE_STATUS_UNCERTIFIED );
        return status;
    }
}

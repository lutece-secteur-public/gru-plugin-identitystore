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
package fr.paris.lutece.plugins.identitystore.service.identity;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttributeHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.cache.IdentityAttributeCache;
import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.FullIndexTask;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.service.IIdentitySearcher;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityMapper;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.*;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IdentityService
{
    private final AttributeCertificationDefinitionService _attributeCertificationDefinitionService = AttributeCertificationDefinitionService.instance( );
    private final IdentityStoreNotifyListenerService _identityStoreNotifyListenerService = IdentityStoreNotifyListenerService.instance( );
    private final ServiceContractService _serviceContractService = ServiceContractService.instance( );
    private final IDuplicateService _duplicateServiceCreation = SpringContextService.getBean( "identitystore.duplicateService.creation" );
    private final IDuplicateService _duplicateServiceUpdate = SpringContextService.getBean( "identitystore.duplicateService.update" );
    private final IDuplicateService _duplicateServiceImportCertitude = SpringContextService.getBean( "identitystore.duplicateService.import.certitude" );
    private final IDuplicateService _duplicateServiceImportSuspicion = SpringContextService.getBean( "identitystore.duplicateService.import.suspicion" );
    private final IdentityAttributeCache _cache = SpringContextService.getBean( "identitystore.identityAttributeCache" );
    private final IIdentitySearcher _identitySearcher = SpringContextService.getBean( IIdentitySearcher.NAME );

    private static IdentityService _instance;

    public static IdentityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    /**
     * Creates a new {@link Identity} according to the given {@link IdentityChangeRequest}
     * 
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param applicationCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the created {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity create( final IdentityChangeRequest identityChangeRequest, final String applicationCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );
        final DuplicateDto duplicates = _duplicateServiceCreation.findDuplicates( attributes );
        if ( duplicates != null )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setMessage( duplicates.getMessage( ) );
            // response.setDuplicates( duplicates ); //TODO voir si on renvoie le CUID
            return null;
        }

        final Identity identity = new Identity( );
        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) ) )
        {
            identity.setConnectionId( identityChangeRequest.getIdentity( ).getConnectionId( ) );
        }
        IdentityHome.create( identity );
        for ( final CertifiedAttribute certifiedAttribute : identityChangeRequest.getIdentity( ).getAttributes( ) )
        {
            final IdentityAttribute attribute = new IdentityAttribute( );
            attribute.setIdIdentity( identity.getId( ) );
            attribute.setAttributeKey( getAttributeKey( certifiedAttribute.getKey( ) ) );
            attribute.setValue( certifiedAttribute.getValue( ) );
            attribute.setLastUpdateApplicationCode( applicationCode );

            if ( certifiedAttribute.getCertificationProcess( ) != null )
            {
                final AttributeCertificate certificate = new AttributeCertificate( );
                certificate.setCertificateDate( new Timestamp( certifiedAttribute.getCertificationDate( ).getTime( ) ) );
                certificate.setCertifierCode( certifiedAttribute.getCertificationProcess( ) );
                certificate.setCertifierName( certifiedAttribute.getCertificationProcess( ) );
                final RefAttributeCertificationLevel refAttributeCertificationLevel = _attributeCertificationDefinitionService
                        .get( certifiedAttribute.getCertificationProcess( ), certifiedAttribute.getKey( ) );
                if ( refAttributeCertificationLevel == null )
                {
                    throw new IdentityStoreException( "Aucun level n'est défini pour le processus " + certifiedAttribute.getCertificationProcess( ) );
                }
                final String level = refAttributeCertificationLevel.getRefCertificationLevel( ).getLevel( );
                certificate.setCertificateLevel( Integer.parseInt( level ) );
                attribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                attribute.setIdCertificate( attribute.getCertificate( ).getId( ) );
            }

            IdentityAttributeHome.create( attribute );
            identity.getAttributes( ).put( attribute.getAttributeKey( ).getKeyName( ), attribute );

            final AttributeStatus attributeStatus = new AttributeStatus( );
            attributeStatus.setKey( certifiedAttribute.getKey( ) );
            attributeStatus.setStatus( AttributeChangeStatus.CREATED );
            response.getAttributeStatuses( ).add( attributeStatus );
        }

        response.setCustomerId( identity.getCustomerId( ) );
        response.setCreationDate( identity.getCreationDate( ) );
        response.setStatus( IdentityChangeStatus.CREATE_SUCCESS );

        /* Historique des modifications */
        response.getAttributeStatuses( ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.CREATE, identity, attributeStatus,
                    identityChangeRequest.getOrigin( ), applicationCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Indexation */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( identity, IdentityChangeType.CREATE ) );

        return identity;
    }

    /**
     * Updates an existing {@link Identity} according to the given {@link IdentityChangeRequest} and following the given rules: <br>
     * <ul>
     * <li>The {@link Identity} must exist in te database. If not, NOT_FOUND status is returned in the execution response</li>
     * <li>The {@link Identity} must not be merged or deleted. In case of merged/deleted identity, the update is not performed and the customer ID of the
     * primary identity is returned in the execution response with a CONFLICT status</li>
     * <li>If the {@link Identity} can be updated, its {@link IdentityAttribute} list is updated following the given rule:
     * <ul>
     * <li>If the {@link IdentityAttribute} exists, it is updated if the value is different, and if the process level given in the request is higher than the
     * existing one. If the value cannot be updated, the NOT_UPDATED status, associated with the attribute key, is returned in the execution response.</li>
     * <li>If the {@link IdentityAttribute} does not exist, it is created. The CREATED status, associated with the attribute key, is returned in the execution
     * response.</li>
     * <li>CUID and GUID attributes cannot be modified.</li>
     * </ul>
     * </li>
     * </ul>
     * 
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param applicationCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the updated {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity update( final IdentityChangeRequest identityChangeRequest, final String applicationCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );
        final DuplicateDto duplicates = _duplicateServiceUpdate.findDuplicates( attributes );
        if ( duplicates != null )
        {
            duplicates.getIdentities( ).removeIf(
                    qualifiedIdentity -> StringUtils.equals( qualifiedIdentity.getCustomerId( ), identityChangeRequest.getIdentity( ).getCustomerId( ) ) );// TODO
                                                                                                                                                           // à
                                                                                                                                                           // confirmer
        }
        if ( duplicates != null && CollectionUtils.isNotEmpty( duplicates.getIdentities( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            response.setMessage( duplicates.getMessage( ) );
            // response.setDuplicates( duplicates ); //TODO voir si on renvoie le CUID
            return null;
        }

        final Identity identity = IdentityHome.findByCustomerId( identityChangeRequest.getIdentity( ).getCustomerId( ) );

        if ( identity == null )
        {
            response.setStatus( IdentityChangeStatus.NOT_FOUND );
        }
        else
            if ( identity.isMerged( ) )
            {
                final Identity masterIdentity = IdentityHome.findMasterIdentityByCustomerId( identityChangeRequest.getIdentity( ).getCustomerId( ) );
                response.setStatus( IdentityChangeStatus.CONFLICT );
                response.setCustomerId( masterIdentity.getCustomerId( ) );
                response.setMessage( "Cannot update a merged Identity. Master identity customerId is provided in the response." );
            }
            else
                if ( identity.isDeleted( ) )
                {
                    response.setStatus( IdentityChangeStatus.CONFLICT );
                    response.setCustomerId( identity.getCustomerId( ) );
                    response.setMessage( "Cannot update a deleted Identity." );
                }
                else
                    if ( !_serviceContractService.canModifyIdentity( StringUtils.isNotEmpty( identity.getConnectionId( ) ), applicationCode ) )
                    {
                        response.setStatus( IdentityChangeStatus.CONFLICT );
                        response.setCustomerId( identity.getCustomerId( ) );
                        response.setMessage( "The client application is not authorized to update a connected identity." );
                    }
                    else
                    {
                        /* Récupération des attributs déja existants ou non */
                        final Map<Boolean, List<CertifiedAttribute>> sortedAttributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                                .collect( Collectors.partitioningBy( a -> identity.getAttributes( ).containsKey( a.getKey( ) ) ) );
                        final List<CertifiedAttribute> existingWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( true ) )
                                ? sortedAttributes.get( true )
                                : new ArrayList<>( );
                        final List<CertifiedAttribute> newWritableAttributes = CollectionUtils.isNotEmpty( sortedAttributes.get( false ) )
                                ? sortedAttributes.get( false )
                                : new ArrayList<>( );

                        /* Create new attributes */
                        for ( final CertifiedAttribute attributeToWrite : newWritableAttributes )
                        {

                            final IdentityAttribute attribute = new IdentityAttribute( );
                            attribute.setIdIdentity( identity.getId( ) );
                            attribute.setAttributeKey( getAttributeKey( attributeToWrite.getKey( ) ) );
                            attribute.setValue( attributeToWrite.getValue( ) );
                            attribute.setLastUpdateApplicationCode( applicationCode );

                            if ( attributeToWrite.getCertificationProcess( ) != null )
                            {
                                final AttributeCertificate certificate = new AttributeCertificate( );
                                certificate.setCertificateDate( new Timestamp( attributeToWrite.getCertificationDate( ).getTime( ) ) );
                                certificate.setCertifierCode( attributeToWrite.getCertificationProcess( ) );
                                certificate.setCertifierName( attributeToWrite.getCertificationProcess( ) );
                                final RefAttributeCertificationLevel refAttributeCertificationLevel = _attributeCertificationDefinitionService
                                        .get( attributeToWrite.getCertificationProcess( ), attributeToWrite.getKey( ) );
                                if ( refAttributeCertificationLevel == null )
                                {
                                    throw new IdentityStoreException(
                                            "No Level defined for processus with code " + attributeToWrite.getCertificationProcess( ) );
                                }
                                final String level = refAttributeCertificationLevel.getRefCertificationLevel( ).getLevel( );
                                certificate.setCertificateLevel( Integer.parseInt( level ) );
                                attribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                                attribute.setIdCertificate( attribute.getCertificate( ).getId( ) );
                            }

                            IdentityAttributeHome.create( attribute );
                            identity.getAttributes( ).put( attribute.getAttributeKey( ).getKeyName( ), attribute );

                            final AttributeStatus attributeStatus = new AttributeStatus( );
                            attributeStatus.setKey( attributeToWrite.getKey( ) );
                            attributeStatus.setStatus( AttributeChangeStatus.CREATED );
                            response.getAttributeStatuses( ).add( attributeStatus );
                        }

                        /* Update existing attributes */
                        for ( final CertifiedAttribute attributeToUpdate : existingWritableAttributes )
                        {
                            final IdentityAttribute existingAttribute = identity.getAttributes( ).get( attributeToUpdate.getKey( ) );
                            RefAttributeCertificationLevel refAttributeCertificationLevel = null;
                            if ( StringUtils.isNotEmpty( attributeToUpdate.getCertificationProcess( ) ) )
                            {
                                refAttributeCertificationLevel = _attributeCertificationDefinitionService.get( attributeToUpdate.getCertificationProcess( ),
                                        attributeToUpdate.getKey( ) );
                                if ( refAttributeCertificationLevel == null )
                                {
                                    throw new IdentityStoreException(
                                            "No Level defined for processus with code " + attributeToUpdate.getCertificationProcess( ) );
                                }
                            }
                            final String attributeToUpdateLevel = refAttributeCertificationLevel != null
                                    ? refAttributeCertificationLevel.getRefCertificationLevel( ).getLevel( )
                                    : "0";
                            int attributeToUpdateLevelInt = Integer.parseInt( attributeToUpdateLevel );
                            int existingAttributeLevelInt = existingAttribute.getCertificate( ) != null
                                    ? existingAttribute.getCertificate( ).getCertificateLevel( )
                                    : 0;
                            if ( attributeToUpdateLevelInt == existingAttributeLevelInt
                                    && StringUtils.equals( attributeToUpdate.getValue( ), existingAttribute.getValue( ) ) )
                            {
                                final AttributeStatus attributeStatus = new AttributeStatus( );
                                attributeStatus.setKey( attributeToUpdate.getKey( ) );
                                attributeStatus.setStatus( AttributeChangeStatus.NOT_UPDATED );
                                response.getAttributeStatuses( ).add( attributeStatus );
                            }
                            else
                                if ( attributeToUpdateLevelInt >= existingAttributeLevelInt )
                                {
                                    existingAttribute.setValue( attributeToUpdate.getValue( ) );
                                    existingAttribute.setLastUpdateApplicationCode( applicationCode );

                                    if ( attributeToUpdate.getCertificationProcess( ) != null )
                                    {
                                        final AttributeCertificate certificate = new AttributeCertificate( );
                                        certificate.setCertificateDate( new Timestamp( attributeToUpdate.getCertificationDate( ).getTime( ) ) );
                                        certificate.setCertifierCode( attributeToUpdate.getCertificationProcess( ) );
                                        certificate.setCertifierName( attributeToUpdate.getCertificationProcess( ) );

                                        certificate.setCertificateLevel( attributeToUpdateLevelInt );
                                        existingAttribute.setCertificate( AttributeCertificateHome.create( certificate ) ); // TODO supprime-t-on l'ancien
                                                                                                                            // certificat ?
                                        existingAttribute.setIdCertificate( existingAttribute.getCertificate( ).getId( ) );
                                    }

                                    IdentityAttributeHome.update( existingAttribute );
                                    identity.getAttributes( ).put( existingAttribute.getAttributeKey( ).getKeyName( ), existingAttribute );

                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( attributeToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.UPDATED );
                                    response.getAttributeStatuses( ).add( attributeStatus );
                                }
                                else
                                {
                                    final AttributeStatus attributeStatus = new AttributeStatus( );
                                    attributeStatus.setKey( attributeToUpdate.getKey( ) );
                                    attributeStatus.setStatus( AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL );
                                    response.getAttributeStatuses( ).add( attributeStatus );
                                }
                        }

                        IdentityHome.update( identity );

                        response.setCustomerId( identity.getCustomerId( ) );
                        response.setConnectionId( identity.getConnectionId( ) );
                        response.setCreationDate( identity.getCreationDate( ) );
                        response.setLastUpdateDate( identity.getLastUpdateDate( ) );
                        boolean notAllAttributesCreatedOrUpdated = response.getAttributeStatuses( ).stream( )
                                .anyMatch( attributeStatus -> AttributeChangeStatus.INSUFFICIENT_CERTIFICATION_LEVEL.equals( attributeStatus.getStatus( ) )
                                        || AttributeChangeStatus.NOT_UPDATED.equals( attributeStatus.getStatus( ) )
                                        || AttributeChangeStatus.INSUFFICIENT_RIGHTS.equals( attributeStatus.getStatus( ) )
                                        || AttributeChangeStatus.UNAUTHORIZED.equals( attributeStatus.getStatus( ) ) );
                        response.setStatus(
                                notAllAttributesCreatedOrUpdated ? IdentityChangeStatus.UPDATE_INCOMPLETE_SUCCESS : IdentityChangeStatus.UPDATE_SUCCESS );
                        if ( notAllAttributesCreatedOrUpdated )
                        {
                            response.setIdentity( IdentityMapper.toJsonIdentity( identity ) );
                        }

                        /* Historique des modifications */
                        response.getAttributeStatuses( ).forEach( attributeStatus -> {
                            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.UPDATE, identity,
                                    attributeStatus, identityChangeRequest.getOrigin( ), applicationCode );
                            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
                        } );

                        /* Indexation */
                        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( identity, IdentityChangeType.UPDATE ) );
                    }

        return identity;
    }

    /**
     * Merges two existing {@link Identity} specified in the given {@link IdentityMergeRequest} and following the given rules: <br>
     * <ul>
     * <li>Both {@link Identity} must exist and not be merged or deleted in te database. If not, FAILURE status is returned in the execution response</li>
     * <li>Each {@link IdentityAttribute} of the secondary {@link Identity} is read.
     * <ul>
     * <li>If the {@link IdentityAttribute} is not present in the primary {@link Identity}, it is created.</li>
     * <li>If the {@link IdentityAttribute} is present in the primary {@link Identity}, it is updated if the certification process is higher and the value is
     * different.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param identityMergeRequest
     *            the {@link IdentityMergeRequest} holding the parameters of the request
     * @param applicationCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityMergeResponse} holding the status of the execution of the request
     * @return the merged {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity merge( final IdentityMergeRequest identityMergeRequest, final String applicationCode, final IdentityMergeResponse response )
            throws IdentityStoreException
    {
        final Identity primaryIdentity = IdentityHome.findByCustomerId( identityMergeRequest.getIdentities( ).getPrimaryCuid( ) );
        if ( primaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.getMessage( ).add( "No identity could be found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) );
        }
        else
            if ( primaryIdentity.isDeleted( ) )
            {
                response.setStatus( IdentityMergeStatus.FAILURE );
                response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) + " is deleted" );
            }
            else
                if ( primaryIdentity.isMerged( ) )
                {
                    response.setStatus( IdentityMergeStatus.FAILURE );
                    response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getPrimaryCuid( ) + " is merged" );
                }

        final Identity secondaryIdentity = IdentityHome.findByCustomerId( identityMergeRequest.getIdentities( ).getSecondaryCuid( ) );
        if ( secondaryIdentity == null )
        {
            response.setStatus( IdentityMergeStatus.FAILURE );
            response.getMessage( ).add( "No identity could be found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) );
        }
        else
            if ( secondaryIdentity.isDeleted( ) )
            {
                response.setStatus( IdentityMergeStatus.FAILURE );
                response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) + " is deleted" );
            }
            else
                if ( secondaryIdentity.isMerged( ) )
                {
                    response.setStatus( IdentityMergeStatus.FAILURE );
                    response.getMessage( ).add( "Identity found with customer_id " + identityMergeRequest.getIdentities( ).getSecondaryCuid( ) + " is merged" );
                }

        /* Return a FAIL status in case of problem */
        if ( IdentityMergeStatus.FAILURE.equals( response.getStatus( ) ) )
        {
            return null;
        }

        secondaryIdentity.getAttributes( ).forEach( ( key, secondaryAttribute ) -> {
            IdentityAttribute primaryAttribute = primaryIdentity.getAttributes( ).get( key );
            if ( primaryAttribute == null )
            { // l'attribut n'existe pas, on le crée
                primaryAttribute = new IdentityAttribute( );
                primaryAttribute.setIdIdentity( primaryIdentity.getId( ) );
                primaryAttribute.setAttributeKey( secondaryAttribute.getAttributeKey( ) );
                primaryAttribute.setValue( secondaryAttribute.getValue( ) );
                primaryAttribute.setLastUpdateApplicationCode( applicationCode );

                if ( secondaryAttribute.getCertificate( ) != null )
                {
                    final AttributeCertificate certificate = new AttributeCertificate( );
                    certificate.setCertificateDate( new Timestamp( secondaryAttribute.getCertificate( ).getCertificateDate( ).getTime( ) ) );
                    certificate.setCertifierCode( secondaryAttribute.getCertificate( ).getCertifierCode( ) );
                    certificate.setCertifierName( secondaryAttribute.getCertificate( ).getCertifierName( ) );
                    certificate.setCertificateLevel( secondaryAttribute.getCertificate( ).getCertificateLevel( ) );
                    primaryAttribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                    primaryAttribute.setIdCertificate( primaryAttribute.getCertificate( ).getId( ) );
                }
                IdentityAttributeHome.create( primaryAttribute );
                primaryIdentity.getAttributes( ).put( primaryAttribute.getAttributeKey( ).getKeyName( ), primaryAttribute );

                final AttributeStatus attributeStatus = new AttributeStatus( );
                attributeStatus.setKey( primaryAttribute.getAttributeKey( ).getKeyName( ) );
                attributeStatus.setStatus( AttributeChangeStatus.CREATED );
                response.getAttributeStatuses( ).add( attributeStatus );
            }
            else
            { // l'attribut existe, on le met à jour si le niveau de certification est plus élevé
                final int primaryLevel = primaryAttribute.getCertificate( ) != null ? primaryAttribute.getCertificate( ).getCertificateLevel( ) : 0;
                final int secondaryLevel = secondaryAttribute.getCertificate( ) != null ? secondaryAttribute.getCertificate( ).getCertificateLevel( ) : 0;
                if ( secondaryLevel > primaryLevel )
                {
                    primaryAttribute.setValue( secondaryAttribute.getValue( ) );
                    primaryAttribute.setLastUpdateApplicationCode( applicationCode );
                    if ( secondaryAttribute.getCertificate( ) != null )
                    {
                        final AttributeCertificate certificate = new AttributeCertificate( );
                        certificate.setCertificateDate( new Timestamp( secondaryAttribute.getCertificate( ).getCertificateDate( ).getTime( ) ) );
                        certificate.setCertifierCode( secondaryAttribute.getCertificate( ).getCertifierCode( ) );
                        certificate.setCertifierName( secondaryAttribute.getCertificate( ).getCertifierName( ) );
                        certificate.setCertificateLevel( secondaryAttribute.getCertificate( ).getCertificateLevel( ) );
                        primaryAttribute.setCertificate( AttributeCertificateHome.create( certificate ) );
                        primaryAttribute.setIdCertificate( primaryAttribute.getCertificate( ).getId( ) );
                    }
                    IdentityAttributeHome.update( primaryAttribute );
                    primaryIdentity.getAttributes( ).put( primaryAttribute.getAttributeKey( ).getKeyName( ), primaryAttribute );

                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( key );
                    attributeStatus.setStatus( AttributeChangeStatus.UPDATED );
                    response.getAttributeStatuses( ).add( attributeStatus );
                }
            }
        } );

        /* Tag de l'identité secondaire */
        secondaryIdentity.setMerged( true );
        secondaryIdentity.setMasterIdentityId( primaryIdentity.getId( ) );
        IdentityHome.merge( secondaryIdentity );

        response.setStatus( IdentityMergeStatus.SUCCESS );
        response.setCustomerId( primaryIdentity.getCustomerId( ) );

        /* Historique des modifications */
        response.getAttributeStatuses( ).forEach( attributeStatus -> {
            AttributeChange attributeChange = IdentityStoreNotifyListenerService.buildAttributeChange( AttributeChangeType.MERGE, primaryIdentity,
                    attributeStatus, identityMergeRequest.getOrigin( ), applicationCode );
            _identityStoreNotifyListenerService.notifyListenersAttributeChange( attributeChange );
        } );

        /* Indexation */
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( secondaryIdentity, IdentityChangeType.DELETE ) );
        _identityStoreNotifyListenerService.notifyListenersIdentityChange( new IdentityChange( primaryIdentity, IdentityChangeType.UPDATE ) );

        return primaryIdentity;
    }

    /**
     * Imports an {@link Identity} according to the given {@link IdentityChangeRequest}
     *
     * @param identityChangeRequest
     *            the {@link IdentityChangeRequest} holding the parameters of the identity change request
     * @param applicationCode
     *            code of the {@link ClientApplication} requesting the change
     * @param response
     *            the {@link IdentityChangeResponse} holding the status of the execution of the request
     * @return the imported {@link Identity}
     * @throws IdentityStoreException
     *             in case of error
     */
    public Identity importIdentity( final IdentityChangeRequest identityChangeRequest, final String applicationCode, final IdentityChangeResponse response )
            throws IdentityStoreException
    {
        final Map<String, String> attributes = identityChangeRequest.getIdentity( ).getAttributes( ).stream( )
                .collect( Collectors.toMap( CertifiedAttribute::getKey, CertifiedAttribute::getValue ) );

        final DuplicateDto certitudeDuplicates = _duplicateServiceImportCertitude.findDuplicates( attributes );
        if ( certitudeDuplicates != null && CollectionUtils.isNotEmpty( certitudeDuplicates.getIdentities( ) ) )
        {
            if ( certitudeDuplicates.getIdentities( ).size( ) == 1 )
            {
                return this.update( identityChangeRequest, applicationCode, response );
            }
        }

        final DuplicateDto suspicionDuplicates = _duplicateServiceImportSuspicion.findDuplicates( attributes );
        if ( suspicionDuplicates != null && CollectionUtils.isNotEmpty( suspicionDuplicates.getIdentities( ) ) )
        {
            response.setStatus( IdentityChangeStatus.CONFLICT );
            // response.setDuplicates( suspicionDuplicates );
            response.setMessage( "Found duplicates" );
        }
        else
        {
            return this.create( identityChangeRequest, applicationCode, response );
        }

        return null;
    }

    /**
     * Filters a list of identities, including its attributes, over {@link ServiceContract} definition and set the {@link IdentitySearchResponse} with the
     * filtered list.
     * 
     * @param response
     *            the {@link IdentitySearchResponse} holding the status of the execution of the request
     * @param applicationCode
     *            code of the {@link ClientApplication} requesting the change
     * @param qualifiedIdentities
     *            the {@link QualifiedIdentity} list to filter
     */
    public void getQualifiedIdentitiesFilteredForReading( final IdentitySearchResponse response, final String applicationCode,
            final List<QualifiedIdentity> qualifiedIdentities ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = _serviceContractService.getActiveServiceContract( applicationCode );
        final List<QualifiedIdentity> filteredIdentities = qualifiedIdentities.stream( ).peek( qualifiedIdentity -> {
            final List<fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute> filteredAttributeValues = qualifiedIdentity
                    .getAttributes( ).stream( ).filter( certifiedAttribute -> {
                        final boolean canReadAttribute = serviceContract.getAttributeRights( ).stream( )
                                .anyMatch( attributeRight -> StringUtils.equals( attributeRight.getAttributeKey( ).getKeyName( ), certifiedAttribute.getKey( ) )
                                        && attributeRight.isReadable( ) );

                        // TODO voir si on met des alert directement dans l'identité
                        if ( !canReadAttribute )
                        {
                            final IdentitySearchMessage alert = new IdentitySearchMessage( );
                            alert.setAttributeName( certifiedAttribute.getKey( ) );
                            alert.setAttributeValues( certifiedAttribute.getValue( ) );
                            alert.setMessage(
                                    "Identity " + qualifiedIdentity.getCustomerId( ) + " This attribute is not readable in service contract definition" );
                            response.getAlerts( ).add( alert );
                        }
                        return canReadAttribute;
                    } ).collect( Collectors.toList( ) );
            qualifiedIdentity.getAttributes( ).clear( );
            qualifiedIdentity.getAttributes( ).addAll( filteredAttributeValues );
        } ).filter( qualifiedIdentity -> CollectionUtils.isNotEmpty( qualifiedIdentity.getAttributes( ) ) && !qualifiedIdentity.isMerged( ) )
                .collect( Collectors.toList( ) );
        response.setIdentities( filteredIdentities );
    }

    public void fullIndexing( )
    {
        new FullIndexTask( ).run( );
    }

    public AttributeKey getAttributeKey( final String keyName ) throws IdentityAttributeNotFoundException
    {
        return _cache.get( keyName );
    }

    public void createAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.create( attributeKey );
        _cache.put( attributeKey.getKeyName( ), attributeKey );
    }

    public void updateAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.update( attributeKey );
        _cache.put( attributeKey.getKeyName( ), attributeKey );
    }

    public void deleteAttributeKey( final AttributeKey attributeKey )
    {
        AttributeKeyHome.remove( attributeKey.getId( ) );
        _cache.removeKey( attributeKey.getKeyName( ) );
    }

    public void search( final IdentitySearchRequest identitySearchRequest, final IdentitySearchResponse response, final String applicationCode )
            throws ServiceContractNotFoundException
    {
        final List<SearchAttribute> searchAttributes = identitySearchRequest.getSearch( ).getAttributes( ).stream( )
                .map( dto -> new SearchAttribute( dto.getKey( ), dto.getValue( ), dto.isStrict( ) ) ).collect( Collectors.toList( ) );
        final Response search = _identitySearcher.search( searchAttributes );
        if ( search != null )
        {
            final List<QualifiedIdentity> identities = new ArrayList<>( );
            search.getResult( ).getHits( ).forEach( hit -> identities.add( IdentityObjectMapper.toQualifiedIdentity( hit, searchAttributes ) ) );
            this.getQualifiedIdentitiesFilteredForReading( response, applicationCode, identities );
        }
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.contract;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.cache.ActiveServiceContractCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityAttributeNotFoundException;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.sql.TransactionManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServiceContractService
{

    private final ActiveServiceContractCache _cache = SpringContextService.getBean( "identitystore.activeServiceContractCache" );
    private static ServiceContractService _instance;

    public static ServiceContractService instance( )
    {
        if ( _instance == null )
        {
            _instance = new ServiceContractService( );
            _instance._cache.refresh( );
        }
        return _instance;
    }

    private ServiceContractService( )
    {
    }

    /**
     * Get the active {@link ServiceContract} associated to the given {@link ClientApplication}
     *
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @return the active {@link ServiceContract}
     * @throws ServiceContractNotFoundException
     */
    public ServiceContract getActiveServiceContract( final String clientCode ) throws ServiceContractNotFoundException
    {
        return _cache.get( clientCode );
    }

    /**
     * Validates the {@link IdentityChangeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link IdentityChangeResponse} with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The attribute must be writable</li>
     * <li>The certification processus, if given in the request, must be in the list of authorized processus</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            {@link IdentityChangeRequest} with list of attributes
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @return {@link IdentityChangeResponse} containing the execution status
     * @throws ServiceContractNotFoundException
     * @throws IdentityAttributeNotFoundException
     */
    public IdentityChangeResponse validateIdentityChange( final IdentityChangeRequest identityChangeRequest, final String clientCode )
            throws ServiceContractNotFoundException, IdentityAttributeNotFoundException
    {
        final IdentityChangeResponse response = new IdentityChangeResponse( );
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        for ( final AttributeDto attributeDto : identityChangeRequest.getIdentity( ).getAttributes( ) )
        {
            boolean canWriteAttribute = IdentityAttributeService.instance( ).getAttributeKey( attributeDto.getKey( ) ) != null;
            if ( !canWriteAttribute )
            {
                attrStatusList.add( this.buildAttributeStatus( attributeDto, AttributeChangeStatus.NOT_FOUND ) );
                continue;
            }

            canWriteAttribute = serviceContract.getAttributeRights( ).stream( )
                    .anyMatch( attributeRight -> StringUtils.equals( attributeRight.getAttributeKey( ).getKeyName( ), attributeDto.getKey( ) )
                            && attributeRight.isWritable( ) );
            if ( !canWriteAttribute )
            {
                attrStatusList.add( this.buildAttributeStatus( attributeDto, AttributeChangeStatus.UNAUTHORIZED ) );
                continue;
            }

            if ( attributeDto.getCertifier( ) != null )
            {
                canWriteAttribute = serviceContract.getAttributeCertifications( ).stream( ).anyMatch(
                        attributeCertification -> StringUtils.equals( attributeCertification.getAttributeKey( ).getKeyName( ), attributeDto.getKey( ) )
                                && attributeCertification.getRefAttributeCertificationProcessus( ).stream( )
                                        .anyMatch( processus -> StringUtils.equals( processus.getCode( ), attributeDto.getCertifier( ) ) ) );
                if ( !canWriteAttribute )
                {
                    attrStatusList.add( this.buildAttributeStatus( attributeDto, AttributeChangeStatus.INSUFFICIENT_RIGHTS ) );
                }
            }
        }

        if ( !attrStatusList.isEmpty( ) )
        {
            response.setStatus(
                    ResponseStatusFactory.failure( ).setAttributeStatuses( attrStatusList ).setMessage( "The request violates service contract definition" )
                            .setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ) );
        }

        return response;
    }

    /**
     * Validates the {@link IdentityMergeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link IdentityMergeResponse} with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The {@link ClientApplication} must be authorized to perform the merge</li>
     * </ul>
     *
     * @param identityMergeRequest
     *            {@link IdentityMergeRequest} with list of attributes
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @return {@link IdentityMergeResponse} containing the execution status
     * @throws ServiceContractNotFoundException
     */
    public IdentityMergeResponse validateIdentityMerge( final IdentityMergeRequest identityMergeRequest, final String clientCode )
            throws ServiceContractNotFoundException
    {
        final IdentityMergeResponse response = new IdentityMergeResponse( );
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        if ( !serviceContract.getAuthorizedMerge( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to merge identities" )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_MERGE_UNAUTHORIZED ) );
        }

        return response;
    }

    /**
     * Validates the {@link IdentityChangeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link IdentityChangeResponse} with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The {@link ClientApplication} must be authorized to perform the import</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            {@link IdentityMergeRequest} with list of attributes
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the change
     * @return {@link IdentityMergeResponse} containing the execution status
     * @throws ServiceContractNotFoundException
     */
    public IdentityChangeResponse validateIdentityImport( final IdentityChangeRequest identityChangeRequest, final String clientCode )
            throws ServiceContractNotFoundException, IdentityAttributeNotFoundException
    {
        final IdentityChangeResponse response = new IdentityChangeResponse( );
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        if ( !serviceContract.getAuthorizedImport( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to import identities " )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_IMPORT_UNAUTHORIZED ) );
        }
        else
        {
            return this.validateIdentityChange( identityChangeRequest, clientCode );
        }

        return response;
    }

    /**
     * Validates the {@link IdentityExportRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the export. Each
     * violation is listed in the response with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The attribute must be readable</li>
     * </ul>
     *
     * @param identityExportRequest
     *            {@link IdentityExportRequest} with list of attributes
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the search
     * @throws ServiceContractNotFoundException
     */
    public IdentityExportResponse validateIdentityExport(final IdentityExportRequest identityExportRequest, final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        final IdentityExportResponse response = new IdentityExportResponse( );
        if ( !serviceContract.getAuthorizedExport( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to export identities." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_EXPORT_UNAUTHORIZED ) );
            return response;
        }

        if ( identityExportRequest.getAttributeKeyList( ) != null && !identityExportRequest.getAttributeKeyList( ).isEmpty( ) )
        {

            for ( final String searchAttributeKey : identityExportRequest.getAttributeKeyList( ) )
            {
                final Optional<AttributeRight> attributeRight = serviceContract.getAttributeRights( ).stream( )
                        .filter( a -> StringUtils.equals( a.getAttributeKey( ).getKeyName( ), searchAttributeKey ) ).findFirst( );
                if ( attributeRight.isPresent( ) )
                {
                    boolean canReadAttribute = attributeRight.get( ).isReadable( );

                    if ( !canReadAttribute )
                    {
                        response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ).setMessage( searchAttributeKey + " key is not readable in service contract definition." ) );
                    }
                }
                else
                {
                    response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ).setMessage( searchAttributeKey + " key does not exist in service contract definition." ) );
                }
            }
        }

        return response;
    }

    /**
     * Validates the {@link IdentitySearchRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the search. Each
     * violation is listed in the response with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The attribute must be searchable</li>
     * </ul>
     *
     * @param identitySearchRequest
     *            {@link IdentitySearchRequest} with list of attributes
     * @param clientCode
     *            code of the {@link ClientApplication} requesting the search
     * @throws ServiceContractNotFoundException
     */
    public IdentitySearchResponse validateIdentitySearch( final IdentitySearchRequest identitySearchRequest, final String clientCode,
            final boolean checkContract ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        final IdentitySearchResponse response = new IdentitySearchResponse( );
        if ( checkContract && !serviceContract.getAuthorizedSearch( ) )
        {
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "The client application is not authorized to search an identity." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_SEARCH_UNAUTHORIZED ) );
            final IdentitySearchMessage message = new IdentitySearchMessage( );
            message.setMessage( "The client application is not authorized to search an identity." ); // TODO améliorer le modèle
            response.getAlerts( ).add( message );
            return response;
        }

        if ( identitySearchRequest.getSearch( ) != null )
        {

            for ( final SearchAttribute searchAttribute : identitySearchRequest.getSearch( ).getAttributes( ) )
            {
                final Optional<AttributeRight> attributeRight = serviceContract.getAttributeRights( ).stream( )
                        .filter( a -> StringUtils.equals( a.getAttributeKey( ).getKeyName( ), searchAttribute.getKey( ) ) ).findFirst( );
                if ( attributeRight.isPresent( ) )
                {
                    boolean canSearchAttribute = attributeRight.get( ).isSearchable( );

                    if ( !canSearchAttribute )
                    {
                        final IdentitySearchMessage alert = new IdentitySearchMessage( );
                        alert.setAttributeName( searchAttribute.getKey( ) );
                        alert.setMessage( "This attribute is not searchable in service contract definition." );
                        response.getAlerts( ).add( alert );
                        response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ) );
                    }
                }
                else
                { // if key does not exist, it can be a common key for search
                    final List<AttributeRight> commonAttributes = serviceContract.getAttributeRights( ).stream( )
                            .filter( a -> StringUtils.equals( a.getAttributeKey( ).getCommonSearchKeyName( ), searchAttribute.getKey( ) ) )
                            .collect( Collectors.toList( ) );
                    if ( CollectionUtils.isNotEmpty( commonAttributes ) )
                    {
                        boolean canSearchAttribute = commonAttributes.stream( ).allMatch( a -> a.isSearchable( ) );
                        if ( !canSearchAttribute )
                        {
                            final IdentitySearchMessage alert = new IdentitySearchMessage( );
                            alert.setAttributeName( searchAttribute.getKey( ) );
                            alert.setMessage( "This attribute group is not searchable in service contract definition." );
                            response.getAlerts( ).add( alert );
                            response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ) );
                        }
                    }
                    else
                    {
                        final IdentitySearchMessage alert = new IdentitySearchMessage( );
                        alert.setAttributeName( searchAttribute.getKey( ) );
                        alert.setMessage( "This attribute does not exist in service contract definition." );
                        response.getAlerts( ).add( alert );
                        response.setStatus( ResponseStatusFactory.failure( ).setMessageKey( Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION ) );
                    }
                }
            }
        }

        return response;
    }

    public List<String> getMandatoryAttributes( final String clientCode, final List<AttributeKey> sharedMandatoryAttributeList )
            throws ServiceContractNotFoundException
    {
        final List<AttributeRight> rights = this.getActiveServiceContract( clientCode ).getAttributeRights( );
        return Stream
                .concat( sharedMandatoryAttributeList.stream( ).map( AttributeKey::getKeyName ),
                        rights.stream( ).filter( AttributeRight::isMandatory ).map( ar -> ar.getAttributeKey( ).getKeyName( ) ) )
                .distinct( ).collect( Collectors.toList( ) );

    }

    public boolean canModifyConnectedIdentity( final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getAuthorizedAccountUpdate( );
    }

    public boolean canCreateIdentity( final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getAuthorizedCreation( );
    }

    public boolean canUpdateIdentity( final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getAuthorizedUpdate( );
    }

    public boolean canUncertifyIdentity( final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getAuthorizedDecertification( );
    }

    public boolean canDeleteIdentity( String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getAuthorizedDeletion( );
    }

    public int getDataRetentionPeriodInMonths( final String clientCode ) throws ServiceContractNotFoundException
    {
        final ServiceContract serviceContract = this.getActiveServiceContract( clientCode );
        return serviceContract.getDataRetentionPeriodInMonths( );
    }

    private AttributeStatus buildAttributeStatus( final AttributeDto attributeDto, final AttributeChangeStatus status )
    {
        final AttributeStatus attributeStatus = new AttributeStatus( );
        attributeStatus.setKey( attributeDto.getKey( ) );
        attributeStatus.setStatus( status );
        return attributeStatus;
    }

    /**
     * Creates a new {@link ServiceContract} (if possible) and adds it to cache if active. The contract can be created if:
     * <ul>
     * <li>The start date of the contract is not in the range [start date; end date] of an existing contract</li>
     * <li>The end date of the contract is not in the range [start date; end date] of an existing contract</li>
     * </ul>
     *
     * @param serviceContract
     * @param applicationId
     * @return
     */
    public ServiceContract create( final ServiceContract serviceContract, final Integer applicationId ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            final ClientApplication clientApplication = ClientApplicationHome.findByPrimaryKey( applicationId );
            this.validateContractDefinition( serviceContract, clientApplication );
            ServiceContractHome.create( serviceContract, applicationId );
            if ( CollectionUtils.isNotEmpty( serviceContract.getAttributeRights( ) ) )
            {
                ServiceContractHome.addAttributeRights( serviceContract.getAttributeRights( ), serviceContract );
            }
            if ( CollectionUtils.isNotEmpty( serviceContract.getAttributeRequirements( ) ) )
            {
                ServiceContractHome.addAttributeRequirements( serviceContract.getAttributeRequirements( ), serviceContract );
            }
            if ( CollectionUtils.isNotEmpty( serviceContract.getAttributeCertifications( ) ) )
            {
                ServiceContractHome.addAttributeCertifications( serviceContract.getAttributeCertifications( ), serviceContract );
            }
            serviceContract.setClientCode( clientApplication.getClientCode( ) );

            if ( serviceContract.isActive( ) )
            {
                this._cache.put( clientApplication.getClientCode( ), serviceContract );
            }
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }

        return serviceContract;
    }

    /**
     * Update an existing {@link ServiceContract} (if possible) and adds it to cache if active. The contract can be updated if:
     * <ul>
     * <li>The start date of the contract is not in the range [start date; end date] of an existing contract</li>
     * <li>The end date of the contract is not in the range [start date; end date] of an existing contract</li>
     * </ul>
     *
     * @param serviceContract
     * @param applicationId
     * @return
     */
    public ServiceContract update( final ServiceContract serviceContract, final Integer applicationId ) throws IdentityStoreException
    {

        TransactionManager.beginTransaction( null );
        try
        {
            final ClientApplication clientApplication = ClientApplicationHome.findByPrimaryKey( applicationId );
            this.validateContractDefinition( serviceContract, clientApplication );
            ServiceContractHome.update( serviceContract, applicationId );
            ServiceContractHome.removeAttributeRights( serviceContract );
            ServiceContractHome.addAttributeRights( serviceContract.getAttributeRights( ), serviceContract );
            ServiceContractHome.removeAttributeRequirements( serviceContract );
            ServiceContractHome.addAttributeRequirements( serviceContract.getAttributeRequirements( ), serviceContract );
            ServiceContractHome.removeAttributeCertifications( serviceContract );
            ServiceContractHome.addAttributeCertifications( serviceContract.getAttributeCertifications( ), serviceContract );
            serviceContract.setClientCode( clientApplication.getClientCode( ) );

            if ( serviceContract.isActive( ) )
            {
                this._cache.deleteById( serviceContract.getId( ) );
                this._cache.put( clientApplication.getClientCode( ), serviceContract );
            }
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
        return serviceContract;
    }

    /**
     * Closes an existing {@link ServiceContract} (if possible) and adds it to cache if active.
     *
     * @param serviceContract
     * @return
     */
    public ServiceContract close( final ServiceContract serviceContract ) throws IdentityStoreException
    {

        TransactionManager.beginTransaction( null );
        try
        {
            ServiceContractHome.close( serviceContract );
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
        return serviceContract;
    }

    /**
     * Deletes a {@link ServiceContract} by its id in the database
     *
     * @param id
     */
    public void delete( final Integer id )
    {
        ServiceContractHome.remove( id );
        _cache.deleteById( id );
    }

    /**
     * Deletes a {@link ClientApplication} and all the related {@link ServiceContract}
     *
     * @param clientApplication
     */
    public void deleteApplication( final ClientApplication clientApplication ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            ClientApplicationHome.removeContracts( clientApplication );
            ClientApplicationHome.remove( clientApplication );
            _cache.removeKey( clientApplication.getClientCode( ) );
            TransactionManager.commitTransaction( null );
        }
        catch( Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), e );
        }
    }

    /**
     * Checks if the definition of a given {@link ServiceContract} is Valid. Start date and End date of the given contract shall not be in the range of Start
     * date and End date of an existing contract.
     *
     * @param serviceContract
     * @param clientApplication
     */
    private void validateContractDefinition( final ServiceContract serviceContract, final ClientApplication clientApplication )
            throws ServiceContractDefinitionException
    {
        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectServiceContracts( clientApplication );
        if ( serviceContracts == null || serviceContracts.isEmpty( ) )
        {
            return;
        }
        if ( serviceContract.getStartingDate( ) == null )
        {
            throw new ServiceContractDefinitionException( "The start date of the contract shall be set" );
        }
        // filter current contract in case of update
        final List<ServiceContract> filteredServiceContracts = serviceContracts.stream( ).filter( c -> !Objects.equals( c.getId( ), serviceContract.getId( ) ) )
                .collect( Collectors.toList( ) );
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> isInRange( serviceContract.getStartingDate( ), contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            throw new ServiceContractDefinitionException( "The start date of the contract is in the range of an existing service contract" );
        }
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> isInRange( serviceContract.getEndingDate( ), contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            throw new ServiceContractDefinitionException( "The end date of the contract is in the range of an existing service contract" );
        }
        // TODO traiter le cas où il existe un contrat sans date de fin => soit on interdit soit on ferme le contrat automatiquement
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> contract.getEndingDate( ) == null && contract.getStartingDate( ).before( serviceContract.getStartingDate( ) ) ) )
        {
            throw new ServiceContractDefinitionException( "A contract exists with an infinite end date" );
        }

        // https://dev.lutece.paris.fr/gitlab/bild/gestion-identite/identity-management/-/issues/224
        // Les processus sélectionnés pour l'écriture d'un attribut doivent avoir un level >= Niveau de certification minimum exigé s'il est présent
        final List<AttributeRequirement> filledRequirements = serviceContract.getAttributeRequirements( ).stream( )
                .filter( requirement -> requirement.getRefCertificationLevel( ) != null && requirement.getRefCertificationLevel( ).getLevel( ) != null )
                .collect( Collectors.toList( ) );
        final StringBuilder message = new StringBuilder( );
        boolean hasLevelError = false;
        for ( final AttributeRequirement requirement : filledRequirements )
        {
            final Optional<AttributeCertification> result = serviceContract.getAttributeCertifications( ).stream( )
                    .filter( certification -> certification.getAttributeKey( ).getId( ) == requirement.getAttributeKey( ).getId( ) ).findFirst( );
            if ( result.isPresent( ) )
            {
                final AttributeCertification attributeCertification = result.get( );
                final int minLevel = Integer.parseInt( requirement.getRefCertificationLevel( ).getLevel( ) );
                for ( final RefAttributeCertificationProcessus processus : attributeCertification.getRefAttributeCertificationProcessus( ) )
                {
                    final int processLevel = AttributeCertificationDefinitionService.instance( ).getLevelAsInteger( processus.getCode( ),
                            attributeCertification.getAttributeKey( ).getKeyName( ) );
                    if ( processLevel < minLevel )
                    {
                        hasLevelError = true;
                        final String [ ] params = {
                                processus.getLabel( ), requirement.getAttributeKey( ).getName( ), String.valueOf( processLevel ), String.valueOf( minLevel )
                        };
                        message.append(
                                I18nService.getLocalizedString( "identitystore.message.error.servicecontract.processus.level", params, Locale.getDefault( ) ) );
                        message.append( "<br>" );
                    }
                }
            }
        }
        if ( hasLevelError )
        {
            throw new ServiceContractDefinitionException( message.toString( ) );
        }
    }

    public boolean isInRange( final Date testedDate, final Date min, final Date max )
    {
        if ( testedDate != null && min != null && max != null )
        {
            return testedDate.getTime( ) >= min.getTime( ) && testedDate.getTime( ) <= max.getTime( );
        }
        return false;
    }

    /**
     * get Client Code list From AppCode
     * 
     * @param appCode
     *            the app code
     * @return list of corresponding client code
     */
    public List<String> getClientCodesFromAppCode( String appCode )
    {
        final List<ClientApplication> clientApplicationList = ClientApplicationHome.findByApplicationCode( appCode );
        return clientApplicationList.stream( ).map( ClientApplication::getClientCode ).collect( Collectors.toList( ) );
    }

}

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
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchMessage;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
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
     * @throws ResourceNotFoundException
     */
    public ServiceContract getActiveServiceContract( final String clientCode ) throws ClientAuthorizationException {
        return _cache.get( clientCode );
    }

    public List<String> getMandatoryAttributes( final ServiceContract serviceContract, final List<AttributeKey> sharedMandatoryAttributeList )
    {
        final List<AttributeRight> rights = serviceContract.getAttributeRights( );
        return Stream
                .concat( sharedMandatoryAttributeList.stream( ).map( AttributeKey::getKeyName ),
                        rights.stream( ).filter( AttributeRight::isMandatory ).map( ar -> ar.getAttributeKey( ).getKeyName( ) ) )
                .distinct( ).collect( Collectors.toList( ) );
    }

    public int getDataRetentionPeriodInMonths( final String clientCode ) throws ClientAuthorizationException
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
     * Creates a new {@link ServiceContract} (if possible) and adds it to cache if active.
     *
     * @param serviceContract
     *            the service contract to create
     * @param clientApplication
     *            the client application
     */
    public ServiceContract create( final ServiceContract serviceContract, final ClientApplication clientApplication ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            ServiceContractHome.create( serviceContract, clientApplication.getId( ) );
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
        catch( final Exception e )
        {
            TransactionManager.rollBack( null );
            throw new IdentityStoreException( e.getMessage( ), Constants.PROPERTY_REST_ERROR_DURING_TREATMENT );
        }

        return serviceContract;
    }

    /**
     * Update an existing {@link ServiceContract} (if possible) and adds it to cache if active.
     *
     * @param serviceContract
     * @param clientApplication
     * @return
     */
    public ServiceContract update( final ServiceContract serviceContract, final ClientApplication clientApplication ) throws IdentityStoreException
    {
        TransactionManager.beginTransaction( null );
        try
        {
            ServiceContractHome.update( serviceContract, clientApplication.getId( ) );
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
     * @param clientApplicationId
     */
    public void validateContractDefinition( final ServiceContract serviceContract, final int clientApplicationId ) throws RequestFormatException
    {
        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectServiceContracts( clientApplicationId );
        if ( serviceContracts == null || serviceContracts.isEmpty( ) )
        {
            return;
        }
        if ( serviceContract.getStartingDate( ) == null )
        {
            throw new RequestFormatException( "Provided Service Contract must specify a starting date",
                    Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_WITHOUT_START_DATE );
        }
        // filter current contract in case of update
        final List<ServiceContract> filteredServiceContracts = serviceContracts.stream( ).filter( c -> !Objects.equals( c.getId( ), serviceContract.getId( ) ) )
                .collect( Collectors.toList( ) );
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> isInRange( serviceContract.getStartingDate( ), contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            throw new RequestFormatException( "The start date of the contract is in the range of an existing service contract",
                    Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_START_DATE_CONFLICT );
        }
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> isInRange( serviceContract.getEndingDate( ), contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            throw new RequestFormatException( "The end date of the contract is in the range of an existing service contract",
                    Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_END_DATE_CONFLICT );
        }
        // TODO traiter le cas où il existe un contrat sans date de fin => soit on interdit soit on ferme le contrat automatiquement
        if ( filteredServiceContracts.stream( )
                .anyMatch( contract -> contract.getEndingDate( ) == null && contract.getStartingDate( ).before( serviceContract.getStartingDate( ) ) ) )
        {
            throw new RequestFormatException( "A contract exists with an infinite end date",
                    Constants.PROPERTY_REST_ERROR_NEVERENDING_SERVICE_CONTRACT_EXISTING );
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
            throw new RequestFormatException( message.toString( ), Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_INSUFICIENT_PROCESSUS_LEVEL );
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

    // ====================================//

    /**
     * Validates that the {@link ServiceContract} associated to the {@link ClientApplication} requesting the search, is authorizing searching.
     *
     * @param serviceContract
     *            the service contract
     * @throws ClientAuthorizationException
     */
    public void validateGetAuthorization( final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
	List<AttributeRight> attrList = serviceContract.getAttributeRights ( );
	
	// at least one attribute should be readable
	for ( AttributeRight right : attrList )
	{
	    if ( right.isReadable ( ) )
	    {
		return ;
	    }
	}
	
	// otherwise
        throw new ClientAuthorizationException( "The service contract of the sent client code doesn't contain any readable attribute.",
                    Constants.PROPERTY_REST_ERROR_CLIENT_AUTHORIZATION_SEARCH );
        
    }

    /**
     * Validates the {@link IdentitySearchRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the search. Each
     * violation is listed in the {@link ClientAuthorizationException}'s response with a status by attribute key. The following rules are verified:
     * <ul>
     * <li>The service contract must exist and allow to search identities</li>
     * <li>The requested attributes must be searchable</li>
     * </ul>
     *
     * @param identitySearchRequest
     *            {@link IdentitySearchRequest} with list of attributes
     * @param serviceContract
     *            the service contract
     * @throws ClientAuthorizationException
     */
    public void validateSearchAuthorization( final IdentitySearchRequest identitySearchRequest, final ServiceContract serviceContract )
            throws ClientAuthorizationException
    {
        this.validateGetAuthorization( serviceContract );
        if ( identitySearchRequest.getSearch( ) != null )
        {
            final List<IdentitySearchMessage> alerts = new ArrayList<>( );
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
                        alerts.add( alert );
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
                            alerts.add( alert );
                        }
                    }
                    else
                    {
                        final IdentitySearchMessage alert = new IdentitySearchMessage( );
                        alert.setAttributeName( searchAttribute.getKey( ) );
                        alert.setMessage( "This attribute does not exist in service contract definition." );
                        alerts.add( alert );
                    }
                }
            }
            if ( CollectionUtils.isNotEmpty( alerts ) )
            {
                throw new ClientAuthorizationException( "The request violates service contract definition",
                        Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION, alerts );
            }
        }
    }

    /**
     * Validates the {@link IdentityChangeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link ClientAuthorizationException}'s response with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The service contract must exist and allow writing identities</li>
     * <li>The attribute must be writable</li>
     * <li>The certification processus, if given in the request, must be in the list of authorized processus</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            {@link IdentityChangeRequest} with list of attributes
     * @param serviceContract
     *            the service contract of the client requesting the change
     * @throws ClientAuthorizationException
     */
    public void validateCreateAuthorization( final IdentityChangeRequest identityChangeRequest, final ServiceContract serviceContract )
            throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedCreation( ) )
        {
            throw new ClientAuthorizationException( "The service contract of the sent client code doesn't allow creating identities",
                    Constants.PROPERTY_REST_ERROR_CLIENT_AUTHORIZATION_CREATE );
        }

        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) ) && !serviceContract.getAuthorizedAccountUpdate( ) )
        {
            throw new ClientAuthorizationException( "You cannot specify a GUID when requesting for a creation",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_CREATE_WITH_GUID );
        }

        if ( identityChangeRequest.getIdentity( ).getMonParisActive( ) != null && !serviceContract.getAuthorizedAccountUpdate( ) )
        {
            throw new ClientAuthorizationException( "You cannot set the 'mon_paris_active' flag when requesting for a creation",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_CREATE_WITH_MON_PARIS_FLAG );
        }

        this.validateWritableAndCertifiableAttributes( identityChangeRequest.getIdentity( ).getAttributes( ), serviceContract );
    }

    /**
     * Validates the {@link IdentityChangeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link ClientAuthorizationException}'s response with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The service contract must exist and allow writing identities</li>
     * <li>The attribute must be writable</li>
     * <li>The certification processus, if given in the request, must be in the list of authorized processus</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            {@link IdentityChangeRequest} with list of attributes
     * @param serviceContract
     *            service contract of the client requesting the change
     * @throws ClientAuthorizationException
     */
    public void validateUpdateAuthorization( final IdentityChangeRequest identityChangeRequest, final IdentityDto existingIdentityToUpdate,
            final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedUpdate( ) )
        {
            throw new ClientAuthorizationException( "The service contract of the sent client code doesn't allow updating identities",
                    Constants.PROPERTY_REST_ERROR_CLIENT_AUTHORIZATION_UPDATE );
        }
        if ( identityChangeRequest.getIdentity( ).getMonParisActive( ) != null && !serviceContract.getAuthorizedAccountUpdate( ) )
        {
            throw new ClientAuthorizationException( "The client application is not authorized to update the 'mon_paris_active' flag",
                    Constants.PROPERTY_REST_ERROR_FORBIDDEN_MON_PARIS_ACTIVE_UPDATE );
        }
        if ( StringUtils.isNotEmpty( identityChangeRequest.getIdentity( ).getConnectionId( ) )
                && !Objects.equals( identityChangeRequest.getIdentity( ).getConnectionId( ), existingIdentityToUpdate.getConnectionId( ) )
                && !serviceContract.getAuthorizedAccountUpdate( ) )
        {
            throw new ClientAuthorizationException( "The client application is not authorized to update the connection_id",
                    Constants.PROPERTY_REST_ERROR_CLIENT_AUTHORIZATION_UPDATE_GUID );
        }

        this.validateWritableAndCertifiableAttributes( identityChangeRequest.getIdentity( ).getAttributes( ), serviceContract );
    }

    /**
     * Validates the {@link IdentityMergeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link IdentityMergeResponse} with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The {@link ClientApplication} must be authorized to perform the merge</li>
     * </ul>
     *
     * @param request
     *            {@link IdentityMergeRequest} with list of attributes
     * @param serviceContract
     *            service contract of the client requesting the change
     * @throws ClientAuthorizationException
     */
    public void validateMergeAuthorization( final IdentityMergeRequest request, final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedMerge( ) )
        {
            throw new ClientAuthorizationException( "The client application is not authorized to merge identities",
                    Constants.PROPERTY_REST_ERROR_MERGE_UNAUTHORIZED );
        }
        if ( request.getIdentity( ) != null )
        {
            if ( !serviceContract.getAuthorizedUpdate( ) )
            {
                throw new ClientAuthorizationException( "The service contract of the sent client code doesn't allow updating identities",
                        Constants.PROPERTY_REST_ERROR_CLIENT_AUTHORIZATION_UPDATE );
            }
            this.validateWritableAndCertifiableAttributes( request.getIdentity( ).getAttributes( ), serviceContract );
        }
    }

    /**
     * checks if the service contract grants the right to delete identities
     *
     * @param serviceContract
     *            the service contract
     * @throws ClientAuthorizationException
     *             if the service contract does not grant the delete authorization
     */
    public void validateDeleteAuthorization( final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedDeletion( ) )
        {
            throw new ClientAuthorizationException( "The client application is not authorized to request the deletion of an identity.",
                    Constants.PROPERTY_REST_ERROR_DELETE_UNAUTHORIZED );

        }
    }

    /**
     * Validates the {@link IdentityChangeRequest} against the {@link ServiceContract} associated to the {@link ClientApplication} requesting the change. Each
     * violation is listed in the {@link IdentityChangeResponse} with a status by attribute key. The following rules are verified: <br>
     * <ul>
     * <li>The {@link ClientApplication} must be authorized to perform the import</li>
     * <li>The attribute must be writable</li>
     * <li>The certification processus, if given in the request, must be in the list of authorized processus</li>
     * </ul>
     *
     * @param request
     *            {@link IdentityChangeRequest} with list of attributes
     * @param serviceContract
     *            the service contract of the client requesting the change
     * @throws ClientAuthorizationException
     *             in case of error
     */
    public void validateImportAuthorization( final IdentityChangeRequest request, final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedImport( ) )
        {
            throw new ClientAuthorizationException( "The client application is not authorized to import identities ",
                    Constants.PROPERTY_REST_ERROR_IMPORT_UNAUTHORIZED );
        }
        this.validateWritableAndCertifiableAttributes( request.getIdentity( ).getAttributes( ), serviceContract );
    }

    /**
     * Checks if the service contract grants the right to uncertify an identity
     *
     * @param serviceContract
     *            the service contract
     * @throws ClientAuthorizationException
     */
    public void validateUncertifyAuthorization( final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedDecertification( ) )
        {
            throw new ClientAuthorizationException( "Unauthorized operation", Constants.PROPERTY_REST_ERROR_UNAUTHORIZED_OPERATION );
        }
    }

    /**
     * Checks if the service contract grants the right to export identities
     *
     * @param serviceContract
     *            the service contract
     * @throws ClientAuthorizationException
     */
    public void validateExportAuthorization( final ServiceContract serviceContract ) throws ClientAuthorizationException
    {
        if ( !serviceContract.getAuthorizedExport( ) )
        {
            throw new ClientAuthorizationException( "Unauthorized operation", Constants.PROPERTY_REST_ERROR_UNAUTHORIZED_OPERATION );
        }
    }

    private void validateWritableAndCertifiableAttributes( final List<AttributeDto> attributes, final ServiceContract serviceContract )
            throws ClientAuthorizationException
    {
        final List<AttributeStatus> attrStatusList = new ArrayList<>( );
        for ( final AttributeDto attributeDto : attributes )
        {
            boolean canWriteAttribute = serviceContract.getAttributeRights( ).stream( )
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
            final ClientAuthorizationException exception = new ClientAuthorizationException( "The request violates service contract definition",
                    Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_VIOLATION );
            exception.getResponse( ).getStatus( ).setAttributeStatuses( attrStatusList );
            throw exception;
        }
    }

    /**
     * Exports all service contracts
     *
     * @throws IdentityStoreException
     *             in case of error
     */
    public List<ServiceContractDto> exportAllServiceContracts( ) throws IdentityStoreException
    {
        return this.exportAllServiceContracts( null );
    }

    /**
     * Exports all service contracts associated with the provided client code
     *
     * @param clientCode
     *            the client code
     * @throws IdentityStoreException
     *             in case of error
     */
    public List<ServiceContractDto> exportAllServiceContracts( final String clientCode ) throws IdentityStoreException
    {
        final List<ServiceContractDto> result = new ArrayList<>( );
        final List<ServiceContract> serviceContracts = clientCode == null ? ServiceContractHome.getAllServiceContractsList( )
                : ClientApplicationHome.selectServiceContracts( ClientApplicationHome.findByCode( clientCode ).getId( ) );

        if ( CollectionUtils.isEmpty( serviceContracts ) )
        {
            throw new ResourceNotFoundException( "No service contract found", Constants.PROPERTY_REST_ERROR_NO_SERVICE_CONTRACT_FOUND );
        }
        serviceContracts.forEach( serviceContract -> result.add( this.enrichAndConvertToDto( serviceContract ) ) );
        return result;
    }

    public ServiceContractDto exportServiceContract( final int serviceContractId ) throws IdentityStoreException
    {
        final Optional<ServiceContract> result = ServiceContractHome.findByPrimaryKey( serviceContractId );
        if ( !result.isPresent( ) )
        {
            throw new ResourceNotFoundException( "Service contract not found", Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_NOT_FOUND );
        }
        return this.enrichAndConvertToDto( result.get( ) );
    }

    private ServiceContractDto enrichAndConvertToDto( final ServiceContract serviceContract )
    {
        serviceContract.setAttributeRights( ServiceContractHome.selectApplicationRights( serviceContract ) );
        serviceContract.setAttributeCertifications( ServiceContractHome.selectAttributeCertifications( serviceContract ) );
        serviceContract.setAttributeRequirements( ServiceContractHome.selectAttributeRequirements( serviceContract ) );
        // TODO amélioration générale à mener sur ce point
        for ( final AttributeCertification certification : serviceContract.getAttributeCertifications( ) )
        {
            for ( final RefAttributeCertificationProcessus processus : certification.getRefAttributeCertificationProcessus( ) )
            {
                processus.setLevel(
                        AttributeCertificationDefinitionService.instance( ).get( processus.getCode( ), certification.getAttributeKey( ).getKeyName( ) ) );
            }
        }
        return DtoConverter.convertContractToDto( serviceContract );
    }
}

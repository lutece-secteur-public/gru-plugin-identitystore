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
package fr.paris.lutece.plugins.identitystore.business.contract;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.referentiel.IRefAttributeCertificationLevelDAO;
import fr.paris.lutece.plugins.identitystore.business.referentiel.IRefAttributeCertificationProcessusDAO;
import fr.paris.lutece.plugins.identitystore.business.referentiel.IRefCertificationLevelDAO;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.plugins.identitystore.web.ServiceContractAttributeDefinitionDto;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides instances management methods (create, find, ...) for ServiceContract objects
 */
public final class ServiceContractHome
{
    // Static variable pointed at the DAO instance
    private static final IServiceContractDAO _serviceContractDAO = SpringContextService.getBean( IServiceContractDAO.BEAN_NAME );
    private static final IRefCertificationLevelDAO _refCertificationLevelDAO = SpringContextService.getBean( "identitystore.refCertificationLevelDAO" );
    private static final IRefAttributeCertificationProcessusDAO _refAttributeCertificationProcessusDAO = SpringContextService
            .getBean( "identitystore.refAttributeCertificationProcessusDAO" );
    private static final IRefAttributeCertificationLevelDAO _refAttributeCertificationLevelDAO = SpringContextService
            .getBean( "identitystore.refAttributeCertificationLevelDAO" );
    private static final IAttributeRequirementDAO _attributeRequirementDAO = SpringContextService.getBean( "identitystore.attributeRequirementDAO" );
    private static final IAttributeCertificationDAO _attributeCertificationDAO = SpringContextService.getBean( "identitystore.attributeCertificationDAO" );
    private static final IAttributeRightDAO _attributeRightDAO = SpringContextService.getBean( IAttributeRightDAO.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private ServiceContractHome( )
    {
    }

    /**
     * Create an instance of the serviceContract class
     * 
     * @param serviceContract
     *            The instance of the ServiceContract which contains the information to store
     * @return The instance of serviceContract which has been created with its primary key.
     */
    public static ServiceContract create( ServiceContract serviceContract, int clientApplicationId )
    {
        _serviceContractDAO.insert( serviceContract, clientApplicationId, _plugin );

        return serviceContract;
    }

    /**
     * Update of the serviceContract which is specified in parameter
     * 
     * @param serviceContract
     *            The instance of the ServiceContract which contains the data to store
     * @return The instance of the serviceContract which has been updated
     */
    public static ServiceContract update( ServiceContract serviceContract, int clientApplicationId )
    {
        _serviceContractDAO.store( serviceContract, clientApplicationId, _plugin );

        return serviceContract;
    }

    /**
     * Closes the serviceContract by setting an and date
     *
     * @param serviceContract
     *            The instance of the ServiceContract which contains the data to store
     * @return The instance of the serviceContract which has been updated
     */
    public static ServiceContract close( ServiceContract serviceContract )
    {
        _serviceContractDAO.close( serviceContract, _plugin );

        return serviceContract;
    }

    /**
     * Remove the serviceContract whose identifier is specified in parameter
     * 
     * @param nKey
     *            The serviceContract Id
     */
    public static void remove( int nKey )
    {
        ServiceContract serviceContract = findByPrimaryKey( nKey ).get( );
        removeAttributeRights( serviceContract );
        removeAttributeRequirements( serviceContract );
        removeAttributeCertifications( serviceContract );
        _serviceContractDAO.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a serviceContract whose identifier is specified in parameter
     * 
     * @param nKey
     *            The serviceContract primary key
     * @return an instance of ServiceContract
     */
    public static Optional<ServiceContract> findByPrimaryKey( int nKey )
    {
        return _serviceContractDAO.load( nKey, _plugin );
    }

    /**
     * Load the id of all the serviceContract objects and returns them as a list
     * 
     * @return the list which contains the id of all the serviceContract objects
     */
    public static List<Integer> getIdServiceContractsList( )
    {
        return _serviceContractDAO.selectIdServiceContractsList( _plugin );
    }

    /**
     * Load the id of all the serviceContract objects and returns them as a list
     *
     * @return the list which contains the id of all the serviceContract objects
     */
    public static List<Integer> getFilteredIdServiceContractsList( final Map<String, String> params )
    {
        return _serviceContractDAO.selectFilterdIdServiceContractsList( params, _plugin );
    }

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param listIds
     *            liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<ImmutablePair<ServiceContract, String>> getServiceContractsListByIds( List<Integer> listIds )
    {
        return _serviceContractDAO.selectServiceContractsListByIds( _plugin, listIds );
    }

    /**
     * Load the data of all the service contracts between two date and returns them as a list
     * 
     * @param startingDate
     *            starting date of the contract
     * @param endingDate
     *            ending date of the contract
     * @return The list which contains the data of all the avant objects
     */
    public static List<ServiceContract> getServiceContractsListBetweenDates( Date startingDate, Date endingDate )
    {
        return _serviceContractDAO.selectServiceContractBetweenDate( _plugin, startingDate, endingDate );
    }

    /**
     * Load the data of all the service contracts with a min end date as specified
     * @param minEndDate - the min end date
     * @return List
     */
    public static List<ServiceContract> getServiceContractsListWithMinEndDate(final Date minEndDate)
    {
        return _serviceContractDAO.getServiceContractsListWithMinEndDate( _plugin, minEndDate );
    }

    /**
     * Load the data of all the service contracts and returns them as a list
     * 
     * @return The list which contains the data of all the ServiceContract objects
     */
    public static List<ServiceContract> getAllServiceContractsList( )
    {
        return _serviceContractDAO.selectAllServiceContractsList( _plugin );
    }

    /**
     * returns rights of provided application
     *
     * @param serviceContract
     *            service contract
     * @return list of rights
     */
    public static List<AttributeRight> selectApplicationRights( ServiceContract serviceContract )
    {
        return _attributeRightDAO.selectAttributeRights( serviceContract, _plugin );
    }

    /**
     * add rights to the application
     *
     * @param lstAttributeRights
     *            attribute rights to add
     */
    public static void addAttributeRights( List<AttributeRight> lstAttributeRights, ServiceContract serviceContract )
    {
        for ( AttributeRight attributeRight : lstAttributeRights )
        {
            _attributeRightDAO.insert( attributeRight, serviceContract.getId( ), _plugin );
        }
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static void addAttributeCertifications( List<AttributeCertification> attributeCertifications, ServiceContract serviceContract )
    {
        for ( AttributeCertification attributeCertification : attributeCertifications )
        {
            _attributeCertificationDAO.insert( attributeCertification, serviceContract.getId( ), _plugin );
        }
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static void addAttributeRequirements( List<AttributeRequirement> attributeRequirements, ServiceContract serviceContract )
    {
        for ( AttributeRequirement attributeRequirement : attributeRequirements )
        {
            _attributeRequirementDAO.insert( attributeRequirement, serviceContract.getId( ), _plugin );
        }
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static List<ServiceContractAttributeDefinitionDto> getDto( ServiceContract serviceContract )
    {
        List<ServiceContractAttributeDefinitionDto> dtos = new ArrayList<>( );

        List<AttributeRequirement> attributeRequirements = selectAttributeRequirements( serviceContract );
        List<AttributeCertification> attributeCertifications = selectAttributeCertifications( serviceContract );
        List<AttributeRight> attributeRights = selectApplicationRights( serviceContract );

        attributeRights.forEach( attributeRight -> dtos.add( new ServiceContractAttributeDefinitionDto( ).setAttributeRight( attributeRight )
                .setAttributeKey( attributeRight.getAttributeKey( ) ).setServiceContract( serviceContract ) ) );

        attributeCertifications.forEach( attributeCertification -> {
            dtos.stream( ).filter( dto -> dto.getAttributeKey( ).getId( ) == attributeCertification.getAttributeKey( ).getId( ) ).findFirst( )
                    .ifPresent( temp -> temp.setRefAttributeCertificationProcessus( attributeCertification.getRefAttributeCertificationProcessus( ) ) );
        } );

        attributeRequirements.forEach( attributeCertification -> {
            dtos.stream( ).filter( dto -> dto.getAttributeKey( ).getId( ) == attributeCertification.getAttributeKey( ).getId( ) ).findFirst( )
                    .ifPresent( temp -> temp.setRefCertificationLevel( attributeCertification.getRefCertificationLevel( ) ) );
        } );

        dtos.forEach( dto -> {
            List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = selectRefAttributeCertificationLevels( dto.getAttributeKey( ) );
            List<RefAttributeCertificationProcessus> compatibleProcessus = refAttributeCertificationLevelList.stream( )
                    .map( RefAttributeCertificationLevel::getRefAttributeCertificationProcessus ).collect( Collectors.toList( ) );
            dto.getCompatibleProcessus( ).addAll( compatibleProcessus );
        } );

        return dtos;
    }

    public static List<RefCertificationLevel> selectCertificationLevels( )
    {
        return _refCertificationLevelDAO.selectRefCertificationLevelsList( _plugin );
    }

    public static List<RefAttributeCertificationLevel> selectRefAttributeCertificationLevels( AttributeKey attributeKey )
    {
        return _refAttributeCertificationLevelDAO.selectRefAttributeLevelByAttribute( _plugin, attributeKey );
    }

    public static List<RefAttributeCertificationProcessus> selectCertificationProcessus( )
    {
        return _refAttributeCertificationProcessusDAO.selectRefAttributeCertificationProcessussList( _plugin );
    }

    public static List<AttributeRequirement> selectAttributeRequirements( ServiceContract servicecontract )
    {
        return _attributeRequirementDAO.selectAttributeRequirementsListByServiceContract( _plugin, servicecontract );
    }

    public static List<AttributeCertification> selectAttributeCertifications( ServiceContract servicecontract )
    {
        return _attributeCertificationDAO.selectAttributeCertificationListByServiceContract( _plugin, servicecontract );
    }

    public static void removeAttributeRights( ServiceContract serviceContract )
    {
        _attributeRightDAO.removeAttributeRights( serviceContract, _plugin );
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static void removeAttributeCertifications( ServiceContract serviceContract )
    {
        _attributeCertificationDAO.deleteFromServiceContract( serviceContract.getId( ), _plugin );
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static void removeAttributeRequirements( ServiceContract serviceContract )
    {
        _attributeRequirementDAO.deleteFromServiceContract( serviceContract.getId( ), _plugin );
        updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
    }

    public static void updateServiceContractLastUpdateAndAuthor(int idServiceContract, String authorName)
    {
        _serviceContractDAO.updateLastUpdateAuthor( idServiceContract, authorName, _plugin );
    }
}

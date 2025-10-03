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

import fr.paris.lutece.portal.service.plugin.Plugin;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * IServiceContractDAO Interface
 */
public interface IServiceContractDAO
{
    String BEAN_NAME = "identitystore.serviceContractDAO";

    /**
     * Insert a new record in the table.
     * 
     * @param serviceContract
     *            instance of the ServiceContract object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( ServiceContract serviceContract, int clientApplicationId, Plugin plugin );

    void deleteFromClientApp( int nKey, Plugin plugin );

    /**
     * Update the record in the table
     * 
     * @param serviceContract
     *            the reference of the ServiceContract
     * @param plugin
     *            the Plugin
     */
    void store( ServiceContract serviceContract, int clientApplicationId, Plugin plugin );

    List<ServiceContract> loadFromClientApplication( int nKey, Plugin plugin );

    /**
     * Delete a record from the table
     * 
     * @param nKey
     *            The identifier of the ServiceContract to delete
     * @param plugin
     *            the Plugin
     */
    void delete( int nKey, Plugin plugin );

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data from the table
     * 
     * @param nKey
     *            The identifier of the serviceContract
     * @param plugin
     *            the Plugin
     * @return The instance of the serviceContract
     */
    Optional<ServiceContract> load( int nKey, Plugin plugin );

    /**
     * Load the id of all the serviceContract objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the id of all the serviceContract objects
     */
    List<Integer> selectIdServiceContractsList( Plugin plugin );

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @param listIds
     *            liste of ids
     * @return The list which contains the data of all the avant objects
     */
    List<ImmutablePair<ServiceContract, String>> selectServiceContractsListByIds( Plugin plugin, List<Integer> listIds );

    /**
     * Load the data of all the service contracts between two date and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @param startingDate
     *            starting date of the contract
     * @param endingDate
     *            ending date of the contract
     * @return The list which contains the data of all the avant objects
     */
    List<ServiceContract> selectServiceContractBetweenDate( Plugin plugin, Date startingDate, Date endingDate );

    /**
     * Load the data of all the service contracts with a min end date as specified
     * @param plugin - the plugin
     * @param minEndDate - the min end date
     * @return List
     */
    List<ServiceContract> getServiceContractsListWithMinEndDate(final Plugin plugin, final Date minEndDate);

    /**
     * Load the data of all the service contracts and returns them as a list
     * 
     * @param plugin
     *            the plugin
     * @return The list which contains the data of all ServiceContract objects
     */
    List<ServiceContract> selectAllServiceContractsList( Plugin plugin );

    List<ServiceContract> selectActiveServiceContract( String clientCode, Plugin plugin );

    void close( ServiceContract serviceContract, Plugin plugin );

    void updateLastUpdateAuthor(int idService, String authorName, Plugin plugin );

    List<Integer> selectFilterdIdServiceContractsList( final Map<String, String> params, Plugin plugin );
}

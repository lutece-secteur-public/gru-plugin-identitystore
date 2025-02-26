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
package fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Optional;

/**
 * ISuspiciousIdentityDAO Interface
 */
public interface ISuspiciousIdentityDAO
{
    /**
     * Insert a new record in the table.
     * 
     * @param suspiciousIdentity
     *            instance of the SuspiciousIdentity object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( SuspiciousIdentity suspiciousIdentity, Plugin plugin );

    /**
     * Update the record in the table
     * 
     * @param suspiciousIdentity
     *            the reference of the SuspiciousIdentity
     * @param plugin
     *            the Plugin
     */
    void store( SuspiciousIdentity suspiciousIdentity, Plugin plugin );

    /**
     * Delete a record from the table
     * 
     * @param nId
     *            The identifier of the SuspiciousIdentity to delete
     * @param plugin
     *            the Plugin
     */
    void delete( int nId, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param customerId
     *            The identifier of the SuspiciousIdentity to delete
     * @param plugin
     *            the Plugin
     */
    void delete( String customerId, Plugin plugin );

    ///////////////////////////////////////////////////////////////////////////
    // Finders

    void insertExcluded( String firstCuid, String secondCuid, String authorType, String authorName, Plugin plugin );

    /**
     * Verify if suspicious identities are already marked as excluded
     * 
     * @param firstCuid
     *            cuid of the firstCuid identity
     * @param secondCuid
     *            cuid of the secondCuid identity
     * @param plugin
     */
    boolean checkIfExcluded( String firstCuid, String secondCuid, Plugin plugin );

    /**
     * Verify if suspicious identities are already marked as excluded
     *
     * @param firstCuid
     *            cuid of the firstCuid identity
     * @param cuids
     *            cuids of the potential secondCuid identity
     * @param plugin
     */
    boolean checkIfExcluded( String firstCuid, List<String> cuids, Plugin plugin );

    /**
     * Verify if at least one customer ID within a list is identified as suspicious
     *
     * @param customerIds
     *            list of cuids
     * @param plugin
     */
    boolean checkIfContainsSuspicious( List<String> customerIds, Plugin plugin );

    /**
     * Load the data from the table
     * 
     * @param nKey
     *            The identifier of the suspiciousIdentity
     * @param plugin
     *            the Plugin
     * @return The instance of the suspiciousIdentity
     */
    Optional<SuspiciousIdentity> load( int nKey, Plugin plugin );

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a list
     *
     * @param max
     *            maximum number of {@link SuspiciousIdentity} to return
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the suspiciousIdentity objects
     */
    List<SuspiciousIdentity> selectSuspiciousIdentitysList( final String ruleCode, final int max, final Integer priority, Plugin plugin )
            throws IdentityStoreException;

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a list
     *
     * @param attributes
     *            attribute keys and values to filter on
     * @param max
     *            maximum number of {@link SuspiciousIdentity} to return
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the suspiciousIdentity objects
     */
    List<SuspiciousIdentity> selectSuspiciousIdentitysList( final String ruleCode, final List<SearchAttribute> attributes, final Integer max,
            final Integer priority, Plugin plugin ) throws IdentityStoreException;

    /**
     * Load the data of all the excluded identities objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the suspiciousIdentity objects
     */
    List<ExcludedIdentities> selectExcludedIdentitiesList( Plugin plugin );

    /**
     * Load the id of all the suspiciousIdentity objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the id of all the suspiciousIdentity objects
     */
    List<Integer> selectIdSuspiciousIdentitysList( Plugin plugin );

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a referenceList
     * 
     * @param plugin
     *            the Plugin
     * @return The referenceList which contains the data of all the suspiciousIdentity objects
     */
    ReferenceList selectSuspiciousIdentitysReferenceList( Plugin plugin );

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @param listIds
     *            liste of ids
     * @return The list which contains the data of all the avant objects
     */
    List<SuspiciousIdentity> selectSuspiciousIdentitysListByIds( Plugin plugin, List<Integer> listIds );

    /**
     * Load the data of suspiciousIdentity with customerID matching argument
     *
     * @param plugin
     *            the Plugin
     * @param customerId
     *            id of the customer
     * @return The SuspiciousIdentity
     */
    SuspiciousIdentity selectByCustomerID( String customerId, Plugin plugin );

    /**
     * Get the {@link SuspiciousIdentity} list that has been marked for this list of CUIDs, if any
     * @param customerIds the list of CUIDs to search for
     * @param plugin the plugin
     * @return the {@link SuspiciousIdentity} list that has been marked for this list of CUIDs, if any
     */
    List <SuspiciousIdentity> selectByCustomerIDs(List<String> customerIds, Plugin plugin);

    /**
     * Truncates duplicate suspicions
     * 
     * @param plugin
     */
    void purge( Plugin plugin );

    /**
     * Return the number of pending suspicious identities
     *
     * @return the count
     */
    int countSuspiciousIdentities( final Plugin plugin );

    /**
     * Return the number of pending suspicious identities for a given rule.
     *
     * @param ruleId
     *            the rule ID
     * @return the count
     */
    int countSuspiciousIdentities( final int ruleId, final Plugin plugin );

    boolean manageLock( String customerId, boolean lock, String authorType, String authorName, Plugin plugin );

    void purgeLocks( Plugin plugin );

    List <SuspiciousIdentity> getAllLocks( Plugin plugin );

    void removeLock( String customerId, Plugin plugin );

    List<String> selectSuspiciousIdentityCuidsList( String ruleCode, Plugin plugin );

    void removeExcludedIdentities( String firstCuid, String secondCuid, Plugin plugin );

    void removeExcludedIdentities( String cuid, Plugin plugin );

    List<ExcludedIdentities> selectExcludedIdentitiesList( String customerId, Plugin plugin );

    void purgeByRuleId(Integer ruleId, Plugin plugin);
}

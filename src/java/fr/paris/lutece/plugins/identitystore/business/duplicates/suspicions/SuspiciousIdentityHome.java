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

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This class provides instances management methods (create, find, ...) for SuspiciousIdentity objects
 */
public final class SuspiciousIdentityHome
{
    // Static variable pointed at the DAO instance
    private static final ISuspiciousIdentityDAO _dao = SpringContextService.getBean( "identitystore-quality.suspiciousIdentityDAO" );
    private static final Plugin _plugin = PluginService.getPlugin( "identitystore-quality" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private SuspiciousIdentityHome( )
    {
    }

    /**
     * Create an instance of the suspiciousIdentity class
     * 
     * @param suspiciousIdentity
     *            The instance of the SuspiciousIdentity which contains the informations to store
     * @return The instance of suspiciousIdentity which has been created with its primary key.
     */
    public static SuspiciousIdentity create( SuspiciousIdentity suspiciousIdentity )
    {
        _dao.insert( suspiciousIdentity, _plugin );

        return suspiciousIdentity;
    }

    /**
     * Update of the suspiciousIdentity which is specified in parameter
     * 
     * @param suspiciousIdentity
     *            The instance of the SuspiciousIdentity which contains the data to store
     * @return The instance of the suspiciousIdentity which has been updated
     */
    public static SuspiciousIdentity update( SuspiciousIdentity suspiciousIdentity )
    {
        _dao.store( suspiciousIdentity, _plugin );

        return suspiciousIdentity;
    }

    /**
     * Update of the suspiciousIdentity which is specified in parameter
     *
     * @param firstCuid
     *            The CUID of the SuspiciousIdentity which contains the data to store
     * @param secondCuid
     *            The CUID of the SuspiciousIdentity which contains the data to store
     * @return The instance of the suspiciousIdentity which has been updated
     */
    public static void exclude( String firstCuid, String secondCuid, String authorType, String authorName )
    {
        if ( !excluded( firstCuid, secondCuid ) ) // TODO handle response with author if already excluded ?
        {
            _dao.insertExcluded( firstCuid, secondCuid, authorType, authorName, _plugin );
        }
    }

    /**
     * Check if a couple of suspicious identities are marked as excluded
     *
     * @param firstCuid
     *            The CUID of the SuspiciousIdentity which contains the data to store
     * @param secondCuid
     *            The CUID of the SuspiciousIdentity which contains the data to store
     * @return true if excluded
     */
    public static boolean excluded( String firstCuid, String secondCuid )
    {
        return _dao.checkIfExcluded( firstCuid, secondCuid, _plugin );
    }

    /**
     * Check if a couple of suspicious identities are marked as excluded
     *
     * @param firstCuid
     *            The CUID of the SuspiciousIdentity which contains the data to store
     * @param cuids
     *            The CUIDs of the SuspiciousIdentities which contains the data to store
     * @return true if excluded
     */
    public static boolean excluded( String firstCuid, List<String> cuids )
    {
        return _dao.checkIfExcluded( firstCuid, cuids, _plugin );
    }

    /**
     * Verify if at least one customer ID within a list is identified as suspicious
     *
     * @param customerIds
     *            The list of CUID
     * @return true if excluded
     */
    public static boolean hasSuspicious( final List<String> customerIds )
    {
        return CollectionUtils.isNotEmpty( customerIds ) && _dao.checkIfContainsSuspicious( customerIds, _plugin );
    }

    /**
     * Remove the suspiciousIdentity whose identifier is specified in parameter
     * 
     * @param nId
     *            The suspiciousIdentity customer Id
     */
    public static void remove( int nId )
    {
        _dao.delete( nId, _plugin );
    }

    /**
     * Remove the suspiciousIdentity whose identifier is specified in parameter
     *
     * @param customerId
     *            The suspiciousIdentity customer Id
     */
    public static void remove( String customerId )
    {
        _dao.delete( customerId, _plugin );
    }

    /**
     * Returns an instance of a suspiciousIdentity whose identifier is specified in parameter
     * 
     * @param nKey
     *            The suspiciousIdentity primary key
     * @return an instance of SuspiciousIdentity
     */
    public static Optional<SuspiciousIdentity> findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a list
     *
     * @param max
     *            max number of suspicious identities to return
     * @return the list which contains the data of all the suspiciousIdentity objects
     */
    public static List<SuspiciousIdentity> getSuspiciousIdentitysList( final String ruleCode, final int max, final Integer priority )
            throws IdentityStoreException
    {
        return _dao.selectSuspiciousIdentitysList( ruleCode, max, priority, _plugin );
    }

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a list
     *
     * @param attributes
     *            attributes to filter the results on
     * @param max
     *            max number of suspicious identities to return
     * @return the list which contains the data of all the suspiciousIdentity objects
     */
    public static List<SuspiciousIdentity> getSuspiciousIdentitysList( final String ruleCode, final List<SearchAttribute> attributes, final Integer max,
            final Integer priority ) throws IdentityStoreException
    {
        return _dao.selectSuspiciousIdentitysList( ruleCode, attributes, max, priority, _plugin );
    }

    /**
     * Load the data of all the excluded identities objects and returns them as a list
     *
     * @return the list which contains the data of all the suspiciousIdentity objects
     */
    public static List<ExcludedIdentities> getExcludedIdentitiesList( )
    {
        return _dao.selectExcludedIdentitiesList( _plugin );
    }

    /**
     * Load the data of all the excluded identities objects and returns them as a list
     *
     * @return the list which contains the data of all the suspiciousIdentity objects
     */
    public static List<ExcludedIdentities> getExcludedIdentitiesList( final String customerId )
    {
        return _dao.selectExcludedIdentitiesList( customerId, _plugin );
    }

    public static void removeExcludedIdentities( final String firstCuid, final String secondCuid )
    {
        _dao.removeExcludedIdentities( firstCuid, secondCuid, _plugin );
    }

    public static void removeExcludedIdentities( final String cuid )
    {
        _dao.removeExcludedIdentities( cuid, _plugin );
    }

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a list
     *
     * @param ruleCode
     *            code of the duplicate rule
     * @return the list which contains the data of all the suspiciousIdentity objects
     */
    public static List<String> getSuspiciousIdentityCuidsList( final String ruleCode )
    {
        return _dao.selectSuspiciousIdentityCuidsList( ruleCode, _plugin );
    }

    /**
     * Load the id of all the suspiciousIdentity objects and returns them as a list
     * 
     * @return the list which contains the id of all the suspiciousIdentity objects
     */
    public static List<Integer> getIdSuspiciousIdentitysList( )
    {
        return _dao.selectIdSuspiciousIdentitysList( _plugin );
    }

    /**
     * Load the data of all the suspiciousIdentity objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the suspiciousIdentity objects
     */
    public static ReferenceList getSuspiciousIdentitysReferenceList( )
    {
        return _dao.selectSuspiciousIdentitysReferenceList( _plugin );
    }

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param listIds
     *            liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<SuspiciousIdentity> getSuspiciousIdentitysListByIds( List<Integer> listIds )
    {
        return _dao.selectSuspiciousIdentitysListByIds( _plugin, listIds );
    }

    public static SuspiciousIdentity selectByCustomerID( String customerId )
    {
        return _dao.selectByCustomerID( customerId, _plugin );
    }

    public static List<SuspiciousIdentity> selectByCustomerIDs( List<String> customerIds )
    {
        return _dao.selectByCustomerIDs( customerIds, _plugin );
    }

    public static void purge( )
    {
        _dao.purge( _plugin );
    }

    public static int countSuspiciousIdentity( final int ruleId )
    {
        return _dao.countSuspiciousIdentities( ruleId, _plugin );
    }

    public static int countSuspiciousIdentity( )
    {
        return _dao.countSuspiciousIdentities( _plugin );
    }

    public static boolean manageLock( String customerId, String authorName, String authorType, boolean lock ) throws IdentityStoreException
    {
        final Identity identity = IdentityHome.findByCustomerId( customerId );
        if ( identity == null )
        {
            throw new IdentityNotFoundException( "Could not find identity with customerId " + customerId );
        }

        final SuspiciousIdentity suspiciousIdentity = SuspiciousIdentityHome.selectByCustomerID( customerId );
        if ( suspiciousIdentity == null )
        {
            throw new IdentityNotFoundException( "Could not find suspicious identity with customerId " + customerId );
        }

        final boolean isAlreadyLocked = suspiciousIdentity.getLock( ).isLocked( );
        final boolean sameAuthorName = Objects.equals( suspiciousIdentity.getLock( ).getAuthorName( ), authorName );
        final boolean sameAuthorType = Objects.equals( suspiciousIdentity.getLock( ).getAuthorType( ), authorType );
        final boolean sameAuthor = sameAuthorName && sameAuthorType;
        if ( lock && isAlreadyLocked && !sameAuthor )
        {
            throw new SuspiciousIdentityLockedException(
                    "Suspicious identity with customerId " + customerId + " is locked by " + suspiciousIdentity.getLock( ).getAuthorName( ) + "." );
        }

        if ( !lock && !isAlreadyLocked )
        {
            throw new SuspiciousIdentityLockedException( "Suspicious identity with customerId " + customerId + " is already unlocked." );
        }

        if ( !lock && isAlreadyLocked && !sameAuthor )
        {
            throw new SuspiciousIdentityLockedException( "Suspicious identity with customerId " + customerId + " is locked by "
                    + suspiciousIdentity.getLock( ).getAuthorName( ) + ". User" + authorName + " is not allowed to unlock." );
        }

        if ( lock && isAlreadyLocked && sameAuthor )
        {
            // the request user has already locked the resource, do nothing.
            return true;
        }

        return _dao.manageLock( customerId, lock, authorType, authorName, _plugin );
    }

    public static void purgeLocks( )
    {
        _dao.purgeLocks( _plugin );
    }
}

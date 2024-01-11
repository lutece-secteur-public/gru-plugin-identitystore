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
package fr.paris.lutece.plugins.identitystore.business.identity;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.UpdatedIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchUpdatedAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class provides instances management methods (create, find, ...) for Identity objects
 */
public final class IdentityHome
{
    private static final String PROPERTY_MAX_NB_IDENTITY_RETURNED = "identitystore.search.maxNbIdentityReturned";

    // Static variable pointed at the DAO instance
    private static final IIdentityDAO _dao = SpringContextService.getBean( IIdentityDAO.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private IdentityHome( )
    {
    }

    /**
     * Create an instance of the identity class
     *
     * @param identity
     *            The instance of the Identity which contains the informations to store
     * @return The instance of identity which has been created with its primary key.
     */
    public static Identity create( final Identity identity, final int dataRetentionPeriodInMonth )
    {
        _dao.insert( identity, dataRetentionPeriodInMonth, _plugin );

        return identity;
    }

    /**
     * Update of the identity which is specified in parameter
     *
     * @param identity
     *            The instance of the Identity which contains the data to store
     * @return The instance of the identity which has been updated
     */
    public static Identity update( Identity identity )
    {
        _dao.store( identity, _plugin );

        return identity;
    }

    /**
     * Archive identities which have been merge as secondary
     *
     * @param identity
     * @return
     */
    public static Identity merge( Identity identity )
    {
        _dao.merge( identity, _plugin );

        return identity;
    }

    public static void cancelMerge( Identity identity )
    {
        _dao.cancelMerge( identity, _plugin );
    }

    /**
     * Remove the identity whose identifier is specified in parameter
     *
     * @param nIdentityId
     *            The identity Id
     */
    public static void hardRemove( int nIdentityId )
    {
        IdentityAttributeHome.removeAllAttributes( nIdentityId );
        _dao.hardDelete( nIdentityId, _plugin );
    }

    public static void softRemove( String strCuid )
    {
        _dao.softDelete( strCuid, _plugin );
    }

    /**
     * Find an identity ID from the specified customer ID
     *
     * @param strCustomerId
     *            the customer ID
     * @return the identity ID
     */
    public static int findIdByCustomerId( String strCustomerId )
    {
        return _dao.selectIdByCustomerId( strCustomerId, _plugin );
    }

    /**
     * Returns an instance of a identity whose identifier is specified in parameter
     *
     * @param nKey
     *            The identity primary key
     * @return an instance of Identity
     */
    public static Identity findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Select All {@link Identity}
     *
     * @return The Identity
     */
    public static List<Identity> findAll( )
    {
        final List<Identity> identities = _dao.selectAll( _plugin );
        identities.forEach( identity -> identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) ) );
        return identities;
    }

    /**
     * Find by connection ID
     *
     * @param strConnectionId
     *            The connection ID
     * @return The Identity
     */
    public static Identity findByConnectionId( String strConnectionId )
    {
        return _dao.selectByConnectionId( strConnectionId, _plugin );
    }

    /**
     * Find history by customer ID
     *
     * @param strCustomerId
     *            The customer ID
     * @return The Identity
     */
    public static List<IdentityChange> findHistoryByCustomerId( String strCustomerId ) throws IdentityStoreException
    {
        return _dao.selectIdentityHistoryByCustomerId( strCustomerId, _plugin );
    }

    /**
     * Find history by customer search parameters
     *
     * @param strCustomerId
     *            The customer ID
     * @return The Identity
     */
    public static List<IdentityChange> findHistoryBySearchParameters( String strCustomerId, String clientCode, String authorName, IdentityChangeType changeType,
            Map<String, String> metadata, Integer nbDaysFrom ) throws IdentityStoreException
    {
        return _dao.selectIdentityHistoryBySearchParameters( strCustomerId, clientCode, authorName, changeType, metadata, nbDaysFrom, _plugin );
    }

    /**
     * Find by customer ID
     *
     * @param strCustomerId
     *            The customer ID
     * @return The Identity
     */
    public static Identity findByCustomerId( String strCustomerId )
    {
        Identity identity = _dao.selectByCustomerId( strCustomerId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) );
        }

        return identity;
    }

    /**
     * Find by customer ID
     *
     * @param strCustomerId
     *            The customer ID
     * @return The Identity
     */
    public static Identity findMasterIdentityByCustomerId( String strCustomerId )
    {
        Identity identity = _dao.selectNotMergedByCustomerId( strCustomerId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) );
        }

        return identity;
    }

    /**
     * Get the master identity last update date coresponding to the given customer ID
     * 
     * @param customerId
     *            the customer ID
     * @return the last update date or null if the identity doesn't exist for the provided CUID
     */
    public static Timestamp getMasterIdentityLastUpdateDate( final String customerId )
    {
        final Identity identity = _dao.selectNotMergedByCustomerId( customerId, _plugin );
        return identity != null ? identity.getLastUpdateDate( ) : null;
    }

    /**
     * Find by connection ID
     *
     * @param strConnectionId
     *            The customer ID
     * @return The Identity
     */
    public static Identity findMasterIdentityByConnectionId( String strConnectionId )
    {
        Identity identity = _dao.selectNotMergedByConnectionId( strConnectionId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ) ) );
        }

        return identity;
    }

    /**
     * Get the master identity without attributes coresponding to the given connection ID
     * 
     * @param connectionId
     *            the connection ID
     * @return the identity or null if the identity doesn't exist for the provided connection ID
     */
    public static Identity getMasterIdentityNoAttributesByConnectionId( final String connectionId )
    {
        return _dao.selectNotMergedByConnectionId( connectionId, _plugin );
    }

    /**
     * Find all identities matching one of the values defined on each of the selected Attributes. One value must be found for all selected attributes. Always
     * performs an exact search.
     *
     * @param mapAttributes
     *            A map that associates the id of each attributes selected with the list of values
     * @return list of Identity
     */
    public static List<Identity> findByAttributesValueForApiSearch( Map<String, List<String>> mapAttributes, final int max )
    {
        int nMaxNbIdentityReturned = ( max > 0 ) ? max : AppPropertiesService.getPropertyInt( PROPERTY_MAX_NB_IDENTITY_RETURNED, 100 );
        return _dao.selectByAttributesValueForApiSearch( mapAttributes, nMaxNbIdentityReturned, _plugin );
    }

    /**
     * Find all identities that have all attributes specified in the list in parameters.<br/>
     * Identities <b>MUST</b> have all those attributes in order to be returned.
     *
     * @param idAttributeList
     *            the attributes id
     * @param notMerged
     *            if the returned identities have to be not merged
     * @param notSuspicious
     *            if the returned identities have to not be suspicious
     * @param nbFilledAttributes
     *            minimum number of filled attributes over idAttributeList
     * @return A list of matching customer IDs
     */
    public static List<String> findByAttributeExisting( final List<Integer> idAttributeList, final int nbFilledAttributes, final boolean notMerged,
            final boolean notSuspicious )
    {
        return _dao.selectByAttributeExisting( idAttributeList, nbFilledAttributes, notMerged, notSuspicious, _plugin );
    }

    /**
     * add an identity change event in history table
     *
     * @param identityChange
     *            identity change event
     */
    public static void addIdentityChangeHistory( IdentityChange identityChange ) throws IdentityStoreException
    {
        if ( Objects.equals( identityChange.getChangeType( ), IdentityChangeType.READ ) )
        {
            _dao.addOrUpdateChangeHistory( identityChange, _plugin );
        }
        else
        {
            _dao.addChangeHistory( identityChange, _plugin );
        }
    }

    /**
     * get identities that have been updated during the previous `days`.
     * 
     * @param days
     *            max number of days since the last update
     * @param identityChangeTypes
     *            filters on specific change types
     * @param updatedAttributes
     *            filters on specific updated attributes
     * @return the list of identities
     */
    public static List<UpdatedIdentityDto> findUpdatedIdentities( final Integer days, final List<IdentityChangeType> identityChangeTypes,
            final List<SearchUpdatedAttribute> updatedAttributes, final Integer max )
    {
        return _dao.selectUpdated( days, identityChangeTypes, updatedAttributes, max, _plugin );
    }

    /**
     * Search for identities that are not connected, and have a past expirationDate.
     * 
     * @param limit
     *            the max number of returned results
     * @return a list of expired {@link Identity}
     */
    public static List<Identity> findExpiredNotMergedAndNotConnectedIdentities( final int limit )
    {
        return _dao.selectExpiredNotMergedAndNotConnectedIdentities( limit, _plugin );
    }

    /**
     * Search for identities that are merged to the provided identity ID.
     * 
     * @param identityId
     *            the 'master' identity ID
     * @return a list of {@link Identity}
     */
    public static List<Identity> findMergedIdentities( final int identityId )
    {
        return _dao.selectMergedIdentities( identityId, _plugin );
    }

    /**
     * Delete all attribute history of the identity's provided id.
     * 
     * @param identityId
     *            the identity id
     */
    public static void deleteAttributeHistory( final int identityId )
    {
        _dao.deleteAttributeHistory( identityId, _plugin );
    }
}

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
package fr.paris.lutece.plugins.identitystore.business.identity;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceList;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * This class provides instances management methods (create, find, ...) for Identity objects
 */
public final class IdentityHome
{
    private static final String PROPERTY_MAX_NB_IDENTITY_RETURNED = "identitystore.search.maxNbIdentityReturned";

    // Static variable pointed at the DAO instance
    private static IIdentityDAO _dao = SpringContextService.getBean( IIdentityDAO.BEAN_NAME );
    private static Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

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
    public static Identity create( Identity identity )
    {
        _dao.insert( identity, _plugin );

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

    /**
     * Removes the identity whose identifier is specified in parameter
     *
     * @param strConnectionId
     *            the connection id
     * @return the id of the deleted identity
     */
    public static int removeByConnectionId( String strConnectionId )
    {
        int nIdentityId = findIdByConnectionId( strConnectionId );

        if ( nIdentityId >= 0 )
        {
            softRemove( nIdentityId );
        }

        return nIdentityId;
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

    public static void softRemove( int nIdentityId )
    {
        IdentityAttributeHome.removeAllAttributes( nIdentityId );
        _dao.softDelete( nIdentityId, _plugin );
    }

    /**
     * Find an identity ID from the specified connection ID
     *
     * @param strConnectionId
     *            the connection ID
     * @return the identity ID
     */
    public static int findIdByConnectionId( String strConnectionId )
    {
        return _dao.selectIdByConnectionId( strConnectionId, _plugin );
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
     * Find by connection ID
     *
     * @param strConnectionId
     *            The connection ID
     * @param strClientAppCode
     *            code of application client which requires infos
     * @return The Identity
     */
    public static Identity findByConnectionId( String strConnectionId, String strClientAppCode )
    {
        Identity identity = _dao.selectByConnectionId( strConnectionId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ), strClientAppCode ) );
        }

        return identity;
    }

    /**
     * Find all identities by connection ID. If the provided query string contains a wildcard, it performs a LIKE search. Otherwise performs an exact search.
     *
     * @param strConnectionId
     *            The connection ID
     * @return A list of Identity
     */
    public static List<Identity> findAllByConnectionId( String strConnectionId )
    {
        List<Identity> listIdentity = _dao.selectAllByConnectionId( strConnectionId, _plugin );

        return listIdentity;
    }

    /**
     * Find by customer ID
     *
     * @param strCustomerId
     *            The customer ID
     * @param strClientAppCode
     *            code of application client which requires infos
     * @return The Identity
     */
    public static Identity findByCustomerId( String strCustomerId, String strClientAppCode )
    {
        Identity identity = _dao.selectByCustomerId( strCustomerId, _plugin );

        if ( identity != null )
        {
            identity.setAttributes( IdentityAttributeHome.getAttributes( identity.getId( ), strClientAppCode ) );
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
     * Find by customer ID
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

    public static Identity findMasterIdentity( String strCustomerId, String strConnectionId )
    {
        Identity identity = null;
        if ( StringUtils.isNotEmpty( strCustomerId ) )
        {
            identity = IdentityHome.findMasterIdentityByCustomerId( strCustomerId );
        }
        else
            if ( StringUtils.isNotEmpty( strConnectionId ) )
            {
                identity = IdentityHome.findMasterIdentityByConnectionId( strConnectionId );
            }
        return identity;
    }

    /**
     * Find all identities by customer ID. If the provided query string contains a wildcard, it performs a LIKE search. Otherwise performs an exact search.
     *
     * @param strCustomerId
     *            The connection ID
     * @return A list of Identity
     */
    public static List<Identity> findAllByCustomerId( String strCustomerId )
    {
        List<Identity> listIdentity = _dao.selectAllByCustomerId( strCustomerId, _plugin );

        return listIdentity;
    }

    /**
     * Find by attribute value
     *
     * @param strAttributeId
     *            The attribute identifier
     * @param strAttributeValue
     *            The attribute value
     * @return The Identity
     */
    public static List<Identity> findByAttributeValue( String strAttributeId, String strAttributeValue )
    {
        return _dao.selectByAttributeValue( strAttributeId, strAttributeValue, _plugin );
    }

    /**
     * Find all identities matching the query on all Attributes, connection_id and customer_id fields.. If the query contains a wildcard, it performs a LIKE
     * search. Otherwise performs an exact search.
     * 
     * @param strAttributeValue
     *            The attribute value
     * @return list of Identity
     */
    public static List<Identity> findByAllAttributesValue( String strAttributeValue )
    {
        return _dao.selectByAllAttributesValue( strAttributeValue, _plugin );
    }

    /**
     * Find all identities matching one of the values defined on each of the selected Attributes. One value must be found for all selected attributes. Always
     * performs an exact search.
     * 
     * @param mapAttributes
     *            A map that associates the id of each attributes selected with the list of values
     * @return list of Identity
     */
    public static List<Identity> findByAttributesValueForApiSearch( Map<String, List<String>> mapAttributes )
    {
        int nMaxNbIdentityReturned = AppPropertiesService.getPropertyInt( PROPERTY_MAX_NB_IDENTITY_RETURNED, 100 );
        return _dao.selectByAttributesValueForApiSearch( mapAttributes, nMaxNbIdentityReturned, _plugin );
    }

    /**
     * Load the data of nLimit customerIds from the nStart identity and returns them as a list If nLimit is set to -1, no limit is used
     * 
     * @param nStart
     *            the count of customerId from where started
     * @param nLimit
     *            the max count of customerId to retrieve
     *
     * @return the list which contains the data of all the identity objects
     */
    public static List<String> getCustomerIdList( int nStart, int nLimit )
    {
        return _dao.selectCustomerIdsList( nStart, nLimit, _plugin );
    }

    /**
     * Load the customer id of all the identity objects and returns them as a list
     *
     * @return the list which contains the customer id of all the identity objects
     */
    public static List<String> getCustomerIdsList( )
    {
        return _dao.selectCustomerIdsList( _plugin );
    }

    /**
     * Load the data of all the identity objects and returns them as a referenceList
     *
     * @return the referenceList which contains the data of all the identity objects
     */
    public static ReferenceList getIdentitysReferenceList( )
    {
        return _dao.selectIdentitysReferenceList( _plugin );
    }

}

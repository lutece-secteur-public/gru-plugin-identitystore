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

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Map;

/**
 * IIdentityDAO Interface
 */
public interface IIdentityDAO
{
    String BEAN_NAME = "identitystore.identityDAO";

    /**
     * Insert a new record in the table.
     *
     * @param identity
     *            instance of the Identity object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( Identity identity, Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param identity
     *            the reference of the Identity
     * @param plugin
     *            the Plugin
     */
    void store( Identity identity, Plugin plugin );

    /**
     * Merge identity
     * 
     * @param identity
     * @param plugin
     */
    void merge( Identity identity, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nKey
     *            The identifier of the Identity to delete
     * @param plugin
     *            the Plugin
     */
    void hardDelete( int nKey, Plugin plugin );

    /**
     * Modify the columuns is_deleted and date_delete and connection_id to null
     * 
     * @param nKey
     *            The id of the Identity to delete
     * @param plugin
     *            the Plugin
     */
    void softDelete( int nKey, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data from the table
     *
     * @param nKey
     *            The identifier of the identity
     * @param plugin
     *            the Plugin
     * @return The instance of the identity
     */
    Identity load( int nKey, Plugin plugin );

    /**
     * Load the customer id of all the identity objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the customer id of all the identity objects
     */
    List<String> selectCustomerIdsList( Plugin plugin );

    /**
     * Load the data of nLimit customerIds from the nStart identity and returns them as a list
     * 
     * @param nStart
     *            the count of customerId from where started
     * @param nLimit
     *            the max count of customerId to retrieve
     * @param plugin
     *            the Plugin
     *
     * @return the list which contains nLimit customerId
     */
    List<String> selectCustomerIdsList( int nStart, int nLimit, Plugin plugin );

    /**
     * Load the data of all the identity objects and returns them as a referenceList
     *
     * @param plugin
     *            the Plugin
     * @return The referenceList which contains the data of all the identity objects
     */
    ReferenceList selectIdentitysReferenceList( Plugin plugin );

    List<Identity> selectAll( Plugin plugin );

    /**
     * Find by connection ID
     *
     * @param strConnectionId
     *            The connection ID
     * @param plugin
     *            The plugin
     * @return The identity
     */
    Identity selectByConnectionId( String strConnectionId, Plugin plugin );

    /**
     * Find by customer ID
     *
     * @param strCustomerId
     *            The customerID
     * @param plugin
     *            The plugin
     * @return The identity
     */
    Identity selectByCustomerId( String strCustomerId, Plugin plugin );

    Identity selectNotMergedByCustomerId( String strCustomerId, Plugin plugin );

    Identity selectNotMergedByConnectionId( String strCustomerId, Plugin plugin );

    /**
     * Find an identity ID from the specified connection ID
     *
     * @param strConnectionId
     *            the connection ID
     * @param plugin
     *            the plugin
     * @return The identity ID
     */
    int selectIdByConnectionId( String strConnectionId, Plugin plugin );

    /**
     * Find by a given Attribute value
     * 
     * @param strAttributeId
     *            The value to match
     * @param strAttributeValue
     *            The value to match
     * @param plugin
     *            The plugin
     * @return The identity
     */
    List<Identity> selectByAttributeValue( String strAttributeId, String strAttributeValue, Plugin plugin );

    /**
     * Find by a combination of Attribute values. Search for identities that match the conditions defined for each of the selected attributes, that is on each
     * of these attributes the exact value is in a list of expected values (no wildcards).
     * 
     * @param mapAttributes
     *            A map that associates the id of each attributes selected with the list of values
     * @param maxNbIdentityReturned
     *            The maximum number of Identity returned in the list
     * @param plugin
     *            The plugin
     * @return The identity
     */
    List<Identity> selectByAttributesValueForApiSearch( Map<String, List<String>> mapAttributes, int maxNbIdentityReturned, Plugin plugin );

    /**
     * Find all identities matching the query on all Attributes, connection_id and customer_id fields.. If the query contains a wildcard, it performs a LIKE
     * search. Otherwise performs an exact search.
     * 
     * @param strAttributeValue
     *            The value to match
     * @param plugin
     *            The plugin
     * @return The identity
     */
    List<Identity> selectByAllAttributesValue( String strAttributeValue, Plugin plugin );

    /**
     * Find all identities by customer ID. If the provided query string contains a wildcard, it performs a LIKE search. Otherwise performs an exact search.
     *
     * @param strCustomerId
     *            The customer ID
     * @param plugin
     *            The plugin
     * @return A list of Identity
     */
    List<Identity> selectAllByCustomerId( String strCustomerId, Plugin plugin );

    /**
     * Find all identities by connection ID. If the provided query string contains a wildcard, it performs a LIKE search. Otherwise performs an exact search.
     *
     * @param strConnectionId
     *            The connection ID
     * @param plugin
     *            The plugin
     * @return A list of Identity
     */
    List<Identity> selectAllByConnectionId( String strConnectionId, Plugin plugin );

}

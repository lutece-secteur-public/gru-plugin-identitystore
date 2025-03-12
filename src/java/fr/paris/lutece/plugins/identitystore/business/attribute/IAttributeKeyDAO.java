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
package fr.paris.lutece.plugins.identitystore.business.attribute;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * IAttributeKeyDAO Interface
 */
public interface IAttributeKeyDAO
{
    String BEAN_NAME = "identitystore.attributeKeyDAO";

    /**
     * Insert a new record in the table.
     *
     * @param attributeKey
     *            instance of the AttributeKey object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( AttributeKey attributeKey, Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param attributeKey
     *            the reference of the AttributeKey
     * @param plugin
     *            the Plugin
     */
    void store( AttributeKey attributeKey, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nKey
     *            The identifier of the AttributeKey to delete
     * @param plugin
     *            the Plugin
     */
    void delete( int nKey, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * Load the data from the table
     *
     * @param nKey
     *            The identifier of the attributeKey
     * @param plugin
     *            the Plugin
     * @return The instance of the attributeKey
     */
    AttributeKey load( int nKey, Plugin plugin );

    /**
     * Load the data of all the attributeKey objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the attributeKey objects
     */
    List<AttributeKey> selectAttributeKeysList( Plugin plugin );

    /**
     * Load the name of all the attributeKey objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the attributeKey objects
     */
    List<String> selectAttributeKeysNamesList( Plugin plugin );

    /**
     * Load the data of all the attributeKey objects and returns them as a referenceList
     *
     * @param plugin
     *            the Plugin
     * @return The referenceList which contains the data of all the attributeKey objects
     */
    ReferenceList selectAttributeKeysReferenceList( Plugin plugin );

    /**
     * Find the attribute by its key
     *
     * @param strKey
     *            The key The key
     * @param plugin
     *            The plugin
     * @return the attributeKey matching key, null if no attributes matches
     */
    AttributeKey selectByKey( String strKey, Plugin plugin );

    /**
     * check if id attribute is used in other tables
     * 
     * @param nAttributeId
     *            The Attribute ID
     * @param plugin
     *            plugin
     * @return true if id is used in other tables
     */
    boolean checkAttributeId( int nAttributeId, Plugin plugin );

    /**
     * Find the quality base factor which is computed following the rule: attributes => SUM(attribute.weight * attribute.maxCertifLevel)
     * 
     * @param plugin
     *            plugin
     * @return the base
     */
    Integer selectQualityBaseFactor( Plugin plugin );

    /**
     * Load the data of all the attributeKey objects that are mandatory for creating an identity and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the mandatory attributeKey objects
     */
    List<AttributeKey> selectMandatoryForCreationAttributeKeyList( Plugin plugin );

    List<AttributeValue> loadAttributeValues( int nKey, Plugin plugin );

    void insertAttributeValues( int nKey, AttributeValue attributeValue, Plugin plugin );

    void removeAttributeValues( int nKey, Plugin plugin );
}

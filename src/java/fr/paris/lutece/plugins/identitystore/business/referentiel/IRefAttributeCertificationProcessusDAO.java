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
package fr.paris.lutece.plugins.identitystore.business.referentiel;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * IRefAttributeCertificationProcessusDAO Interface
 */
public interface IRefAttributeCertificationProcessusDAO
{
    /**
     * Insert a new record in the table.
     * 
     * @param refAttributeCertificationProcessus
     *            instance of the RefAttributeCertificationProcessus object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( RefAttributeCertificationProcessus refAttributeCertificationProcessus, Plugin plugin );

    /**
     * Update the record in the table
     * 
     * @param refAttributeCertificationProcessus
     *            the reference of the RefAttributeCertificationProcessus
     * @param plugin
     *            the Plugin
     */
    void store( RefAttributeCertificationProcessus refAttributeCertificationProcessus, Plugin plugin );

    /**
     * Delete a record from the table
     * 
     * @param nKey
     *            The identifier of the RefAttributeCertificationProcessus to delete
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
     *            The identifier of the refAttributeCertificationProcessus
     * @param plugin
     *            the Plugin
     * @return The instance of the refAttributeCertificationProcessus
     */
    RefAttributeCertificationProcessus load( int nKey, Plugin plugin );

    /**
     * Load the data of all the refAttributeCertificationProcessus objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the refAttributeCertificationProcessus objects
     */
    List<RefAttributeCertificationProcessus> selectRefAttributeCertificationProcessussList( Plugin plugin );

    /**
     * Load the id of all the refAttributeCertificationProcessus objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @return The list which contains the id of all the refAttributeCertificationProcessus objects
     */
    List<Integer> selectIdRefAttributeCertificationProcessussList( Plugin plugin );

    /**
     * Load the data of all the refAttributeCertificationProcessus objects and returns them as a referenceList
     * 
     * @param plugin
     *            the Plugin
     * @return The referenceList which contains the data of all the refAttributeCertificationProcessus objects
     */
    ReferenceList selectRefAttributeCertificationProcessussReferenceList( Plugin plugin );

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param plugin
     *            the Plugin
     * @param listIds
     *            liste of ids
     * @return The list which contains the data of all the avant objects
     */
    List<RefAttributeCertificationProcessus> selectRefAttributeCertificationProcessussListByIds( Plugin _plugin, List<Integer> listIds );

    /**
     * Gets the {@link RefAttributeCertificationProcessus} by its code.
     * 
     * @param plugin
     *            the plugin
     * @param code
     *            the code
     * @return {@link RefAttributeCertificationProcessus}
     */
    RefAttributeCertificationProcessus getRefAttributeCertificationProcessusByCode( Plugin plugin, String code );
}

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
package fr.paris.lutece.plugins.identitystore.business.application;

import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * IClientApplicationDAO Interface
 */
public interface IClientApplicationDAO
{
    String BEAN_NAME = "identitystore.clientApplicationDAO";

    /**
     * Insert a new record in the table.
     *
     * @param clientApp
     *            instance of the ClientApplication object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( ClientApplication clientApp, Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param clientApp
     *            the reference of the ClientApplication
     * @param plugin
     *            the Plugin
     */
    void store( ClientApplication clientApp, Plugin plugin );

    ClientApplication selectByContractId( int nKey, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nKey
     *            The identifier of the ClientApplication to delete
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
     *            The identifier of the ClientApplication
     * @param plugin
     *            the Plugin
     * @return The instance of the ClientApplication
     */
    ClientApplication load( int nKey, Plugin plugin );

    /**
     * Load the data of all the ClientApplication objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the data of all the ClientApplication objects
     */
    List<ClientApplication> selectClientApplicationList( Plugin plugin );

    /**
     * Load the id of all the ClientApplication objects and returns them as a list
     *
     * @param plugin
     *            the Plugin
     * @return The list which contains the id of all the ClientApplication objects
     */
    List<Integer> selectIdClientApplicationList( Plugin plugin );

    /**
     * Load the data of all the ClientApplication objects and returns them as a referenceList
     *
     * @param plugin
     *            the Plugin
     * @return The referenceList which contains the data of all the ClientApplication objects
     */
    ReferenceList selectClientApplicationReferenceList( Plugin plugin );

    /**
     * Select a ClientApplication by its code
     *
     * @param strCode
     *            The code
     * @param plugin
     *            The plugin
     * @return The ClientApplication
     */
    ClientApplication selectByCode( String strCode, Plugin plugin );

    /*
     * methods for link certifier and ClientApplication
     */
    /**
     * Retrieve certifiers allowed for a given ClientApplication
     *
     * @param nKey
     *            The identifier of the ClientApplication
     * @param plugin
     *            The plugin
     * @return list of allowed certifiers
     */
    List<AbstractCertifier> getCertifiers( int nKey, Plugin plugin );

    /**
     * Retrieve ClientApplication allowed for a given certifier
     *
     * @param strCertifier
     *            The identifier of the certifier
     * @param plugin
     *            The plugin
     * @return list of allowed ClientApplication
     */
    List<ClientApplication> getClientApplications( String strCertifier, Plugin plugin );

    /**
     * Add a certifier to a ClientApplication
     *
     * @param nKey
     *            The identifier of the ClientApplication
     * @param strCertifier
     *            The certifier code
     * @param plugin
     *            The plugin
     */
    void addCertifier( int nKey, String strCertifier, Plugin plugin );

    /**
     * Delete a certifier to a ClientApplication
     *
     * @param nKey
     *            The identifier of the ClientApplication
     * @param strCertifier
     *            The certifier code
     * @param plugin
     *            The plugin
     */
    void deleteCertifier( int nKey, String strCertifier, Plugin plugin );

    /**
     * Delete all certifiers to a ClientApplication
     *
     * @param nKey
     *            The identifier of the ClientApplication
     * @param plugin
     *            The plugin
     */
    void cleanCertifiers( int nKey, Plugin plugin );
}

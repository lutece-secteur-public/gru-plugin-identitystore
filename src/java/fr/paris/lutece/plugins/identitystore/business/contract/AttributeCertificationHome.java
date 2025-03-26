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
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;
import java.util.Optional;

/**
 * This class provides instances management methods (create, find, ...) for AttributeCertification objects
 */
public final class AttributeCertificationHome
{
    // Static variable pointed at the DAO instance
    private static final IAttributeCertificationDAO _dao = SpringContextService.getBean( "identitystore.attributeCertificationDAO" );
    private static final Plugin _plugin = PluginService.getPlugin( "identitystore" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private AttributeCertificationHome( )
    {
    }

    /**
     * Create an instance of the attributeCertification class
     * 
     * @param attributeCertification
     *            The instance of the AttributeCertification which contains the informations to store
     * @return The instance of attributeCertification which has been created with its primary key.
     */
    public static AttributeCertification create( AttributeCertification attributeCertification, ServiceContract serviceContract )
    {
        _dao.insert( attributeCertification, serviceContract.getId( ), _plugin );
        ServiceContractHome.updateServiceContractLastUpdateAndAuthor(serviceContract.getId(), serviceContract.getAuthorName());
        return attributeCertification;
    }

    /**
     * Update of the attributeCertification which is specified in parameter
     * 
     * @param attributeCertification
     *            The instance of the AttributeCertification which contains the data to store
     * @return The instance of the attributeCertification which has been updated
     */
    public static AttributeCertification update( AttributeCertification attributeCertification )
    {
        _dao.store( attributeCertification, _plugin );

        return attributeCertification;
    }

    /**
     * Remove the attributeCertification whose identifier is specified in parameter
     * 
     * @param nKey
     *            The attributeCertification Id
     */
    public static void remove( int nKey, String author )
    {
        _dao.delete( nKey, _plugin );
        ServiceContractHome.updateServiceContractLastUpdateAndAuthor(nKey, author);
    }

    /**
     * Returns an instance of a attributeCertification whose identifier is specified in parameter
     * 
     * @param nKey
     *            The attributeCertification primary key
     * @return an instance of AttributeCertification
     */
    public static Optional<AttributeCertification> findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the attributeCertification objects and returns them as a list
     * 
     * @return the list which contains the data of all the attributeCertification objects
     */
    public static List<AttributeCertification> getAttributeCertificationsList( )
    {
        return _dao.selectAttributeCertificationsList( _plugin );
    }

    /**
     * Load the id of all the attributeCertification objects and returns them as a list
     * 
     * @return the list which contains the id of all the attributeCertification objects
     */
    public static List<Integer> getIdAttributeCertificationsList( )
    {
        return _dao.selectIdAttributeCertificationsList( _plugin );
    }

    /**
     * Load the data of all the attributeCertification objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the attributeCertification objects
     */
    public static ReferenceList getAttributeCertificationsReferenceList( )
    {
        return _dao.selectAttributeCertificationsReferenceList( _plugin );
    }

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param listIds
     *            liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<AttributeCertification> getAttributeCertificationsListByIds( List<Integer> listIds )
    {
        return _dao.selectAttributeCertificationsListByIds( _plugin, listIds );
    }

}

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
package fr.paris.lutece.plugins.identitystore.business.referentiel;

import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for RefAttributeCertificationProcessus objects
 */
public final class RefAttributeCertificationProcessusHome
{
    // Static variable pointed at the DAO instance
    private static IRefAttributeCertificationProcessusDAO _dao = SpringContextService.getBean( "identitystore.refAttributeCertificationProcessusDAO" );
    private static IRefAttributeCertificationLevelDAO iRefAttributeCertificationLevelDAO = SpringContextService
            .getBean( "identitystore.refAttributeCertificationLevelDAO" );
    private static IRefCertificationLevelDAO iRefCertificationLevelDAO = SpringContextService.getBean( "identitystore.refCertificationLevelDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "identitystore" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private RefAttributeCertificationProcessusHome( )
    {
    }

    /**
     * Create an instance of the refAttributeCertificationProcessus class
     * 
     * @param refAttributeCertificationProcessus
     *            The instance of the RefAttributeCertificationProcessus which contains the informations to store
     * @return The instance of refAttributeCertificationProcessus which has been created with its primary key.
     */
    public static RefAttributeCertificationProcessus create( RefAttributeCertificationProcessus refAttributeCertificationProcessus )
    {
        _dao.insert( refAttributeCertificationProcessus, _plugin );

        return refAttributeCertificationProcessus;
    }

    /**
     * Update of the refAttributeCertificationProcessus which is specified in parameter
     * 
     * @param refAttributeCertificationProcessus
     *            The instance of the RefAttributeCertificationProcessus which contains the data to store
     * @return The instance of the refAttributeCertificationProcessus which has been updated
     */
    public static RefAttributeCertificationProcessus update( RefAttributeCertificationProcessus refAttributeCertificationProcessus )
    {
        _dao.store( refAttributeCertificationProcessus, _plugin );

        return refAttributeCertificationProcessus;
    }

    /**
     * Remove the refAttributeCertificationProcessus whose identifier is specified in parameter
     * 
     * @param nKey
     *            The refAttributeCertificationProcessus Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a refAttributeCertificationProcessus whose identifier is specified in parameter
     * 
     * @param nKey
     *            The refAttributeCertificationProcessus primary key
     * @return an instance of RefAttributeCertificationProcessus
     */
    public static RefAttributeCertificationProcessus findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the refAttributeCertificationProcessus objects and returns them as a list
     * 
     * @return the list which contains the data of all the refAttributeCertificationProcessus objects
     */
    public static List<RefAttributeCertificationProcessus> getRefAttributeCertificationProcessussList( )
    {
        return _dao.selectRefAttributeCertificationProcessussList( _plugin );
    }

    /**
     * Load the id of all the refAttributeCertificationProcessus objects and returns them as a list
     * 
     * @return the list which contains the id of all the refAttributeCertificationProcessus objects
     */
    public static List<Integer> getIdRefAttributeCertificationProcessussList( )
    {
        return _dao.selectIdRefAttributeCertificationProcessussList( _plugin );
    }

    /**
     * Load the data of all the refAttributeCertificationProcessus objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the refAttributeCertificationProcessus objects
     */
    public static ReferenceList getRefAttributeCertificationProcessussReferenceList( )
    {
        return _dao.selectRefAttributeCertificationProcessussReferenceList( _plugin );
    }

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param listIds
     *            liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<RefAttributeCertificationProcessus> getRefAttributeCertificationProcessussListByIds( List<Integer> listIds )
    {
        return _dao.selectRefAttributeCertificationProcessussListByIds( _plugin, listIds );
    }

    public static List<RefAttributeCertificationLevel> selectAttributeLevels( RefAttributeCertificationProcessus refattributecertificationprocessus )
    {
        return iRefAttributeCertificationLevelDAO.selectRefAttributeLevelByProcessus( _plugin, refattributecertificationprocessus );
    }

    public static Object selectCertificationLevels( )
    {
        return iRefCertificationLevelDAO.selectRefCertificationLevelsList( _plugin );
    }

    public static void addRefAttributeCertificationLevels( final List<RefAttributeCertificationLevel> refAttributeCertificationLevelList )
    {
        for ( RefAttributeCertificationLevel refAttributeCertificationLevel : refAttributeCertificationLevelList )
        {
            iRefAttributeCertificationLevelDAO.insert( refAttributeCertificationLevel, _plugin );
        }
    }

    public static void removeProcessusLevels( final int processusId )
    {
        iRefAttributeCertificationLevelDAO.deleteFromProcessus( processusId, _plugin );
    }
}

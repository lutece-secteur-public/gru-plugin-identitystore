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
 * This class provides instances management methods (create, find, ...) for RefCertificationLevel objects
 */
public final class RefCertificationLevelHome
{
    // Static variable pointed at the DAO instance
    private static IRefCertificationLevelDAO _dao = SpringContextService.getBean( "identitystore.refCertificationLevelDAO" );
    private static Plugin _plugin = PluginService.getPlugin( "identitystore" );

    /**
     * Private constructor - this class need not be instantiated
     */
    private RefCertificationLevelHome( )
    {
    }

    /**
     * Create an instance of the refCertificationLevel class
     * 
     * @param refCertificationLevel
     *            The instance of the RefCertificationLevel which contains the informations to store
     * @return The instance of refCertificationLevel which has been created with its primary key.
     */
    public static RefCertificationLevel create( RefCertificationLevel refCertificationLevel )
    {
        _dao.insert( refCertificationLevel, _plugin );

        return refCertificationLevel;
    }

    /**
     * Update of the refCertificationLevel which is specified in parameter
     * 
     * @param refCertificationLevel
     *            The instance of the RefCertificationLevel which contains the data to store
     * @return The instance of the refCertificationLevel which has been updated
     */
    public static RefCertificationLevel update( RefCertificationLevel refCertificationLevel )
    {
        _dao.store( refCertificationLevel, _plugin );

        return refCertificationLevel;
    }

    /**
     * Remove the refCertificationLevel whose identifier is specified in parameter
     * 
     * @param nKey
     *            The refCertificationLevel Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a refCertificationLevel whose identifier is specified in parameter
     * 
     * @param nKey
     *            The refCertificationLevel primary key
     * @return an instance of RefCertificationLevel
     */
    public static RefCertificationLevel findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Load the data of all the refCertificationLevel objects and returns them as a list
     * 
     * @return the list which contains the data of all the refCertificationLevel objects
     */
    public static List<RefCertificationLevel> getRefCertificationLevelsList( )
    {
        return _dao.selectRefCertificationLevelsList( _plugin );
    }

    /**
     * Load the id of all the refCertificationLevel objects and returns them as a list
     * 
     * @return the list which contains the id of all the refCertificationLevel objects
     */
    public static List<Integer> getIdRefCertificationLevelsList( )
    {
        return _dao.selectIdRefCertificationLevelsList( _plugin );
    }

    /**
     * Load the data of all the refCertificationLevel objects and returns them as a referenceList
     * 
     * @return the referenceList which contains the data of all the refCertificationLevel objects
     */
    public static ReferenceList getRefCertificationLevelsReferenceList( )
    {
        return _dao.selectRefCertificationLevelsReferenceList( _plugin );
    }

    /**
     * Load the data of all the avant objects and returns them as a list
     * 
     * @param listIds
     *            liste of ids
     * @return the list which contains the data of all the avant objects
     */
    public static List<RefCertificationLevel> getRefCertificationLevelsListByIds( List<Integer> listIds )
    {
        return _dao.selectRefCertificationLevelsListByIds( _plugin, listIds );
    }

}

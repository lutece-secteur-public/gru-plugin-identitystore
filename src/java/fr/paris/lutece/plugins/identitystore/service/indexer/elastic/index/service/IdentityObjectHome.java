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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IIdentityObjectDAO;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;

public class IdentityObjectHome
{

    private static final IIdentityObjectDAO _dao = SpringContextService.getBean( IIdentityObjectDAO.BEAN_NAME );
    private static final Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Get the list of identity ids that are eligible to indexing process
     * 
     * @return the list of identity ids
     */
    public static List<Integer> getEligibleIdListForIndex( )
    {
        return _dao.getEligibleIdListForIndex( _plugin );
    }

    /**
     * Get an identity by its customer ID
     *
     * @param customerId
     *            The identifier of the identity
     * @return The instance of the identity
     */
    public static IdentityObject findByCustomerId( final String customerId )
    {
        return _dao.loadFull( customerId, _plugin );
    }

    /**
     * Load the identity objects that are eligible to indexing process and returns them as a list
     * 
     * @param limit
     *            the limit size of the stream
     * @param offset
     *            the offset of the stream
     * @return The list which contains the identity objects
     */
    public static List<IdentityObject> loadEligibleIdentitiesForIndex( final List<Integer> idList )
    {
        return _dao.loadEligibleIdentitiesForIndex( idList, _plugin );
    }
}

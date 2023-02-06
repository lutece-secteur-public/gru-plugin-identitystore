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
package fr.paris.lutece.plugins.identitystore.business.attribute;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.util.List;

/**
 * This class provides instances management methods (create, find, ...) for AttributeKey objects
 */
public final class AttributeKeyHome
{
    // Static variable pointed at the DAO instance
    private static IAttributeKeyDAO _dao = SpringContextService.getBean( IAttributeKeyDAO.BEAN_NAME );
    private static Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private AttributeKeyHome( )
    {
    }

    /**
     * Create an instance of the attributeKey class
     *
     * @param attributeKey
     *            The instance of the AttributeKey which contains the informations to store
     * @return The instance of attributeKey which has been created with its primary key.
     */
    public static AttributeKey create( AttributeKey attributeKey )
    {
        _dao.insert( attributeKey, _plugin );

        return attributeKey;
    }

    /**
     * Update of the attributeKey which is specified in parameter
     *
     * @param attributeKey
     *            The instance of the AttributeKey which contains the data to store
     * @return The instance of the attributeKey which has been updated
     */
    public static AttributeKey update( AttributeKey attributeKey )
    {
        _dao.store( attributeKey, _plugin );

        return attributeKey;
    }

    /**
     * Remove the attributeKey whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeKey Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a attributeKey whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeKey primary key
     * @return an instance of AttributeKey
     */
    public static AttributeKey findByPrimaryKey( int nKey )
    {
        return _dao.load( nKey, _plugin );
    }

    /**
     * Find the attribute by its key
     *
     * @param strKey
     *            The key The key
     * @return the AttributeKey
     */
    public static AttributeKey findByKey( String strKey )
    {
        return _dao.selectByKey( strKey, _plugin );
    }

    /**
     * Load the data of all the attributeKey objects and returns them as a list
     *
     * @return the list which contains the data of all the attributeKey objects
     */
    public static List<AttributeKey> getAttributeKeysList( )
    {
        return _dao.selectAttributeKeysList( _plugin );
    }

    /**
     * Load the data of all the attributeKey objects and returns them as a referenceList
     *
     * @return the referenceList which contains the data of all the attributeKey objects
     */
    public static ReferenceList getAttributeKeysReferenceList( )
    {
        return _dao.selectAttributeKeysReferenceList( _plugin );
    }

    /**
     * Check reference in other tables of the attributeKey whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeKey Id
     * 
     * @return true if reference exists
     */
    public static boolean checkAttributeId( int nKey )
    {
        return _dao.checkAttributeId( nKey, _plugin );
    }
}

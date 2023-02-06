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

import java.util.List;

import fr.paris.lutece.plugins.identitystore.service.IdentityStorePlugin;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;
import java.util.ArrayList;

/**
 * This class provides instances management methods (create, find, ...) for AttributeCertificate objects
 */
public final class AttributeCertificateHome
{
    // Static variable pointed at the DAO instance
    private static IAttributeCertificateDAO _dao = SpringContextService.getBean( IAttributeCertificateDAO.BEAN_NAME );
    private static Plugin _plugin = PluginService.getPlugin( IdentityStorePlugin.PLUGIN_NAME );

    /**
     * Private constructor - this class need not be instantiated
     */
    private AttributeCertificateHome( )
    {
    }

    /**
     * Create an instance of the attributeCertificate class
     *
     * @param attributeCertificate
     *            The instance of the AttributeCertificate which contains the informations to store
     * @return The instance of attributeCertificate which has been created with its primary key.
     */
    public static AttributeCertificate create( AttributeCertificate attributeCertificate )
    {
        _dao.insert( attributeCertificate, _plugin );

        return attributeCertificate;
    }

    /**
     * Update of the attributeCertificate which is specified in parameter
     *
     * @param attributeCertificate
     *            The instance of the AttributeCertificate which contains the data to store
     * @return The instance of the attributeCertificate which has been updated
     */
    public static AttributeCertificate update( AttributeCertificate attributeCertificate )
    {
        _dao.store( attributeCertificate, _plugin );

        return attributeCertificate;
    }

    /**
     * Remove the attributeCertificate whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeCertificate Id
     */
    public static void remove( int nKey )
    {
        _dao.delete( nKey, _plugin );
    }

    /**
     * Returns an instance of a attributeCertificate whose identifier is specified in parameter
     *
     * @param nKey
     *            The attributeCertificate primary key
     * @return an instance of AttributeCertificate
     */
    public static AttributeCertificate findByPrimaryKey( int nKey )
    {
        AttributeCertificate certificate = _dao.load( nKey, _plugin );

        if ( certificate != null )
        {
            // try
            // {
            // TODO voir ce qu'on fait avec ce registry certificate.setCertifierName( CertifierRegistry.instance( ).getCertifier( certificate.getCertifierCode(
            // ) ).getName( ) );
            certificate.setCertifierName( certificate.getCertifierCode( ) );
            // }
            // catch( CertifierNotFoundException e )
            // {
            // // Certifier not found, maybe deleted
            // return certificate;
            // }
        }

        return certificate;
    }

    /**
     * Load the data of all the attributeCertificate objects and returns them as a list
     *
     * @return the list which contains the data of all the attributeCertificate objects
     */
    public static List<AttributeCertificate> getAttributeCertificatesList( )
    {
        List<AttributeCertificate> listAttributeCertificate = _dao.selectAttributeCertificatesList( _plugin );
        List<AttributeCertificate> returnListAttributeCertificate = new ArrayList<AttributeCertificate>( );

        for ( AttributeCertificate certificate : listAttributeCertificate )
        {
            try
            {
                certificate.setCertifierName( CertifierRegistry.instance( ).getCertifier( certificate.getCertifierCode( ) ).getName( ) );
                returnListAttributeCertificate.add( certificate );
            }
            catch( CertifierNotFoundException e )
            {
                // No certifier found for this certificate
            }

        }
        return returnListAttributeCertificate;
    }

    /**
     * Load the id of all the attributeCertificate objects and returns them as a list
     *
     * @return the list which contains the id of all the attributeCertificate objects
     */
    public static List<Integer> getIdAttributeCertificatesList( )
    {
        return _dao.selectIdAttributeCertificatesList( _plugin );
    }

    /**
     * Load the data of all the attributeCertificate objects and returns them as a referenceList
     *
     * @return the referenceList which contains the data of all the attributeCertificate objects
     */
    public static ReferenceList getAttributeCertificatesReferenceList( )
    {
        return _dao.selectAttributeCertificatesReferenceList( _plugin );
    }
}

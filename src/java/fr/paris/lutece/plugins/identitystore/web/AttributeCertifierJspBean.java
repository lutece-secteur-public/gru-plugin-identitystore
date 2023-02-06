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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * This class provides the user interface to manage AttributeCertifier features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageAttributeCertifiers.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class AttributeCertifierJspBean extends AdminIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_ATTRIBUTECERTIFIERS = "/admin/plugins/identitystore/manage_certifiers.html";
    private static final String TEMPLATE_VIEW_CERTIFIER_ATTRIBUTES_CERTIFIABLE = "/admin/plugins/identitystore/view_attributes_certifiable.html";
    private static final String TEMPLATE_VIEW_CERTIFIER_APPLICATION_CLIENTE = "/admin/plugins/identitystore/view_certifier_appcliente.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS = "identitystore.manage_certifiers.pageTitle";

    // Markers
    private static final String MARK_ATTRIBUTECERTIFIER_LIST = "attributecertifier_list";
    private static final String MARK_CERTIFIER_ATTRIBUTES_CERTIFIABLE = "certifier_attributes_certifiable";
    private static final String MARK_CERTIFIER = "certifier";
    private static final String MARK_CERTIFIER_APPCLIENTE_MAP = "certifier_appcliente_map";

    // Views
    private static final String VIEW_MANAGE_ATTRIBUTECERTIFIERS = "manageAttributeCertifiers";
    private static final String VIEW_CERTIFIER_ATTRIBUTES_CERTIFIABLE = "certifierAttributesCertifiable";
    private static final String VIEW_CERTIFIER_APPLICATION_CLIENTE = "certifierApplicationCliente";

    // Parameters
    private static final String PARAMETER_CERTIFIER_CODE = "certifier_code";

    // Undefined attribut
    private static final int UNDEFINED_ATTRIBUT_ID = -1;

    /**
     * Build the default Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ATTRIBUTECERTIFIERS, defaultView = true )
    public String getManageAttributeCertifiers( HttpServletRequest request )
    {
        Collection<AbstractCertifier> listAttributeCertifiers = CertifierRegistry.instance( ).getCertifiersList( );
        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTECERTIFIER_LIST, listAttributeCertifiers );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS, TEMPLATE_MANAGE_ATTRIBUTECERTIFIERS, model );
    }

    /**
     * Build the attributs of a certifier View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_CERTIFIER_ATTRIBUTES_CERTIFIABLE )
    public String getCertifierAttributesCertifiable( HttpServletRequest request )
    {
        String strCertifierCode = request.getParameter( PARAMETER_CERTIFIER_CODE );

        Map<String, Object> model = getModel( );

        if ( strCertifierCode != null )
        {
            try
            {
                AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( strCertifierCode );
                List<String> listAttributeCertifiable = certifier.getCertifiableAttributesList( );
                List<AttributeKey> listAttributeKeys = new ArrayList<AttributeKey>( );
                for ( String key : listAttributeCertifiable )
                {
                    AttributeKey attributeKey = AttributeKeyHome.findByKey( key );
                    if ( attributeKey == null )
                    {
                        attributeKey = new AttributeKey( );
                        attributeKey.setId( UNDEFINED_ATTRIBUT_ID );
                        attributeKey.setKeyName( key );
                        attributeKey.setName( StringUtils.EMPTY );

                    }
                    listAttributeKeys.add( attributeKey );
                }
                model.put( MARK_CERTIFIER, certifier );
                model.put( MARK_CERTIFIER_ATTRIBUTES_CERTIFIABLE, listAttributeKeys );
            }
            catch( CertifierNotFoundException e )
            {
                // Unable to find certifier
            }
        }

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS, TEMPLATE_VIEW_CERTIFIER_ATTRIBUTES_CERTIFIABLE, model );
    }

    /**
     * Build application cliente by certifier View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_CERTIFIER_APPLICATION_CLIENTE )
    public String getCertifierApplicationCliente( HttpServletRequest request )
    {
        Map<String, Object> model = getModel( );
        Collection<AbstractCertifier> listAttributeCertifiers = CertifierRegistry.instance( ).getCertifiersList( );
        model.put( MARK_ATTRIBUTECERTIFIER_LIST, listAttributeCertifiers );

        Map<String, List<ClientApplication>> mapCertifierAppcliente = new TreeMap<String, List<ClientApplication>>( );
        for ( AbstractCertifier certifier : listAttributeCertifiers )
        {
            mapCertifierAppcliente.put( certifier.getCode( ), ClientApplicationHome.getClientApplications( certifier ) );
        }
        model.put( MARK_CERTIFIER_APPCLIENTE_MAP, mapCertifierAppcliente );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFIERS, TEMPLATE_VIEW_CERTIFIER_APPLICATION_CLIENTE, model );
    }

}

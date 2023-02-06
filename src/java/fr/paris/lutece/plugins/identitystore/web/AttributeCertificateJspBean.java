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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificateHome;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage AttributeCertificate features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageAttributeCertificates.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class AttributeCertificateJspBean extends AdminIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_ATTRIBUTECERTIFICATES = "/admin/plugins/identitystore/manage_attributecertificates.html";
    private static final String TEMPLATE_CREATE_ATTRIBUTECERTIFICATE = "/admin/plugins/identitystore/create_attributecertificate.html";
    private static final String TEMPLATE_MODIFY_ATTRIBUTECERTIFICATE = "/admin/plugins/identitystore/modify_attributecertificate.html";

    // Parameters
    private static final String PARAMETER_ID_ATTRIBUTECERTIFICATE = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFICATES = "identitystore.manage_attributecertificates.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTECERTIFICATE = "identitystore.modify_attributecertificate.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTECERTIFICATE = "identitystore.create_attributecertificate.pageTitle";

    // Markers
    private static final String MARK_ATTRIBUTECERTIFICATE_LIST = "attributecertificate_list";
    private static final String MARK_ATTRIBUTECERTIFICATE = "attributecertificate";
    private static final String JSP_MANAGE_ATTRIBUTECERTIFICATES = "jsp/admin/plugins/identitystore/ManageAttributeCertificates.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_ATTRIBUTECERTIFICATE = "identitystore.message.confirmRemoveAttributeCertificate";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.attributecertificate.attribute.";

    // Views
    private static final String VIEW_MANAGE_ATTRIBUTECERTIFICATES = "manageAttributeCertificates";
    private static final String VIEW_CREATE_ATTRIBUTECERTIFICATE = "createAttributeCertificate";
    private static final String VIEW_MODIFY_ATTRIBUTECERTIFICATE = "modifyAttributeCertificate";

    // Actions
    private static final String ACTION_CREATE_ATTRIBUTECERTIFICATE = "createAttributeCertificate";
    private static final String ACTION_MODIFY_ATTRIBUTECERTIFICATE = "modifyAttributeCertificate";
    private static final String ACTION_REMOVE_ATTRIBUTECERTIFICATE = "removeAttributeCertificate";
    private static final String ACTION_CONFIRM_REMOVE_ATTRIBUTECERTIFICATE = "confirmRemoveAttributeCertificate";

    // Infos
    private static final String INFO_ATTRIBUTECERTIFICATE_CREATED = "identitystore.info.attributecertificate.created";
    private static final String INFO_ATTRIBUTECERTIFICATE_UPDATED = "identitystore.info.attributecertificate.updated";
    private static final String INFO_ATTRIBUTECERTIFICATE_REMOVED = "identitystore.info.attributecertificate.removed";

    // Session variable to store working values
    private AttributeCertificate _attributecertificate;

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ATTRIBUTECERTIFICATES, defaultView = true )
    public String getManageAttributeCertificates( HttpServletRequest request )
    {
        _attributecertificate = null;

        List<AttributeCertificate> listAttributeCertificates = AttributeCertificateHome.getAttributeCertificatesList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_ATTRIBUTECERTIFICATE_LIST, listAttributeCertificates,
                JSP_MANAGE_ATTRIBUTECERTIFICATES );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTECERTIFICATES, TEMPLATE_MANAGE_ATTRIBUTECERTIFICATES, model );
    }

    /**
     * Returns the form to create a attributecertificate
     *
     * @param request
     *            The Http request
     * @return the html code of the attributecertificate form
     */
    @View( VIEW_CREATE_ATTRIBUTECERTIFICATE )
    public String getCreateAttributeCertificate( HttpServletRequest request )
    {
        _attributecertificate = ( _attributecertificate != null ) ? _attributecertificate : new AttributeCertificate( );

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTECERTIFICATE, _attributecertificate );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTECERTIFICATE, TEMPLATE_CREATE_ATTRIBUTECERTIFICATE, model );
    }

    /**
     * Process the data capture form of a new attributecertificate
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_ATTRIBUTECERTIFICATE )
    public String doCreateAttributeCertificate( HttpServletRequest request )
    {
        populate( _attributecertificate, request );

        // Check constraints
        if ( !validateBean( _attributecertificate, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_ATTRIBUTECERTIFICATE );
        }

        AttributeCertificateHome.create( _attributecertificate );
        addInfo( INFO_ATTRIBUTECERTIFICATE_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFICATES );
    }

    /**
     * Manages the removal form of a attributecertificate whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_ATTRIBUTECERTIFICATE )
    public String getConfirmRemoveAttributeCertificate( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFICATE ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_ATTRIBUTECERTIFICATE ) );
        url.addParameter( PARAMETER_ID_ATTRIBUTECERTIFICATE, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ATTRIBUTECERTIFICATE, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a attributecertificate
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage attributecertificates
     */
    @Action( ACTION_REMOVE_ATTRIBUTECERTIFICATE )
    public String doRemoveAttributeCertificate( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFICATE ) );
        AttributeCertificateHome.remove( nId );
        addInfo( INFO_ATTRIBUTECERTIFICATE_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFICATES );
    }

    /**
     * Returns the form to update info about a attributecertificate
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_ATTRIBUTECERTIFICATE )
    public String getModifyAttributeCertificate( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTECERTIFICATE ) );

        if ( ( _attributecertificate == null ) || ( _attributecertificate.getId( ) != nId ) )
        {
            _attributecertificate = AttributeCertificateHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTECERTIFICATE, _attributecertificate );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTECERTIFICATE, TEMPLATE_MODIFY_ATTRIBUTECERTIFICATE, model );
    }

    /**
     * Process the change form of a attributecertificate
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_ATTRIBUTECERTIFICATE )
    public String doModifyAttributeCertificate( HttpServletRequest request )
    {
        populate( _attributecertificate, request );

        // Check constraints
        if ( !validateBean( _attributecertificate, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_ATTRIBUTECERTIFICATE, PARAMETER_ID_ATTRIBUTECERTIFICATE, _attributecertificate.getId( ) );
        }

        AttributeCertificateHome.update( _attributecertificate );
        addInfo( INFO_ATTRIBUTECERTIFICATE_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTECERTIFICATES );
    }
}

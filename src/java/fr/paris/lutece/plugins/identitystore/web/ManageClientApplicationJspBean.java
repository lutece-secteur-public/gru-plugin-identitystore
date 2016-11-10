/*
 * Copyright (c) 2002-2016, Mairie de Paris
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

import fr.paris.lutece.plugins.identitystore.business.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.ClientApplicationHome;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage ClientApplication management features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageClientApplications.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class ManageClientApplicationJspBean extends ManageIdentitiesJspBean
{
    private static final long serialVersionUID = 1L;

    // Templates
    private static final String TEMPLATE_MANAGE_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/manage_clientapplications.html";
    private static final String TEMPLATE_CREATE_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/create_clientapplication.html";
    private static final String TEMPLATE_MODIFY_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/modify_clientapplication.html";

    // Parameters
    private static final String PARAMETER_ID_CLIENTAPPLICATION = "id";
    private static final String PARAMETER_RIGHT_WRITABLE = "writable";
    private static final String PARAMETER_RIGHT_READABLE = "readable";
    private static final String PARAMETER_RIGHT_CERTIFIABLE = "certifiable";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_CLIENTAPPLICATIONS = "identitystore.manage_clientapplications.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_CLIENTAPPLICATION = "identitystore.modify_clientapplication.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_CLIENTAPPLICATION = "identitystore.create_clientapplication.pageTitle";

    // Markers
    private static final String MARK_CLIENTAPPLICATION_LIST = "clientapplication_list";
    private static final String MARK_CLIENTAPPLICATION = "clientapplication";
    private static final String MARK_CLIENTAPPLICATION_RIGHT_LIST = "clientapplication_rights_list";
    private static final String JSP_MANAGE_CLIENTAPPLICATIONS = "jsp/admin/plugins/identitystore/ManageClientApplications.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_CLIENTAPPLICATION = "identitystore.message.confirmRemoveClientApplication";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.clientapplication.attribute.";

    // Views
    private static final String VIEW_MANAGE_CLIENTAPPLICATIONS = "manageClientApplications";
    private static final String VIEW_CREATE_CLIENTAPPLICATION = "createClientApplication";
    private static final String VIEW_MODIFY_CLIENTAPPLICATION = "modifyClientApplication";

    // Actions
    private static final String ACTION_CREATE_CLIENTAPPLICATION = "createClientApplication";
    private static final String ACTION_MODIFY_CLIENTAPPLICATION = "modifyClientApplication";
    private static final String ACTION_REMOVE_CLIENTAPPLICATION = "removeClientApplication";
    private static final String ACTION_CONFIRM_REMOVE_CLIENTAPPLICATION = "confirmRemoveClientApplication";

    // Infos
    private static final String INFO_CLIENTAPPLICATION_CREATED = "identitystore.info.clientapplication.created";
    private static final String INFO_CLIENTAPPLICATION_UPDATED = "identitystore.info.clientapplication.updated";
    private static final String INFO_CLIENTAPPLICATION_REMOVED = "identitystore.info.clientapplication.removed";

    // Session variable to store working values
    private ClientApplication _clientApplication;

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_CLIENTAPPLICATIONS, defaultView = true )
    public String getManageClientApplications( HttpServletRequest request )
    {
        _clientApplication = null;

        List<ClientApplication> listClientApplications = ClientApplicationHome.selectApplicationList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_CLIENTAPPLICATION_LIST, listClientApplications, JSP_MANAGE_CLIENTAPPLICATIONS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_CLIENTAPPLICATIONS, TEMPLATE_MANAGE_CLIENTAPPLICATION, model );
    }

    /**
     * Returns the form to create a clientapplication
     *
     * @param request
     *            The Http request
     * @return the html code of the clientapplication form
     */
    @View( VIEW_CREATE_CLIENTAPPLICATION )
    public String getCreateClientApplication( HttpServletRequest request )
    {
        _clientApplication = ( _clientApplication != null ) ? _clientApplication : new ClientApplication( );

        Map<String, Object> model = getModel( );
        model.put( MARK_CLIENTAPPLICATION, _clientApplication );
        model.put( MARK_CLIENTAPPLICATION_RIGHT_LIST, ClientApplicationHome.selectApplicationRights( _clientApplication ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_CLIENTAPPLICATION, TEMPLATE_CREATE_CLIENTAPPLICATION, model );
    }

    /**
     * Process the data capture form of a new clientapplication
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_CLIENTAPPLICATION )
    public String doCreateClientApplication( HttpServletRequest request )
    {
        populate( _clientApplication, request );

        // Check constraints
        if ( !validateBean( _clientApplication, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_CLIENTAPPLICATION );
        }

        ClientApplicationHome.create( _clientApplication );

        List<AttributeRight> lstAttributeRights = new ArrayList<AttributeRight>( getAttributesRightsFromRequest( request ).values( ) );
        ClientApplicationHome.addAttributeRights( lstAttributeRights );

        addInfo( INFO_CLIENTAPPLICATION_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_CLIENTAPPLICATIONS );
    }

    /**
     * Manages the removal form of a clientapplication whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_CLIENTAPPLICATION )
    public String getConfirmRemoveClientApplication( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_CLIENTAPPLICATION ) );
        url.addParameter( PARAMETER_ID_CLIENTAPPLICATION, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_CLIENTAPPLICATION, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a clientapplication
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage clientapplications
     */
    @Action( ACTION_REMOVE_CLIENTAPPLICATION )
    public String doRemoveClientApplication( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );
        ClientApplicationHome.remove( ClientApplicationHome.findByPrimaryKey( nId ) );
        addInfo( INFO_CLIENTAPPLICATION_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_CLIENTAPPLICATIONS );
    }

    /**
     * Returns the form to update info about a clientapplication
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_CLIENTAPPLICATION )
    public String getModifyClientApplication( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );

        if ( ( _clientApplication == null ) || ( _clientApplication.getId( ) != nId ) )
        {
            _clientApplication = ClientApplicationHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_CLIENTAPPLICATION, _clientApplication );
        model.put( MARK_CLIENTAPPLICATION_RIGHT_LIST, ClientApplicationHome.selectApplicationRights( _clientApplication ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_CLIENTAPPLICATION, TEMPLATE_MODIFY_CLIENTAPPLICATION, model );
    }

    /**
     * Process the change form of a clientapplication
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_CLIENTAPPLICATION )
    public String doModifyClientApplication( HttpServletRequest request )
    {
        populate( _clientApplication, request );

        // Check constraints
        if ( !validateBean( _clientApplication, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_CLIENTAPPLICATION, PARAMETER_ID_CLIENTAPPLICATION, _clientApplication.getId( ) );
        }

        // remove all formers rights
        ClientApplicationHome.removeApplicationRights( _clientApplication );

        // add new ones
        List<AttributeRight> lstAttributeRights = new ArrayList<AttributeRight>( getAttributesRightsFromRequest( request ).values( ) );
        ClientApplicationHome.addAttributeRights( lstAttributeRights );

        ClientApplicationHome.update( _clientApplication );
        addInfo( INFO_CLIENTAPPLICATION_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_CLIENTAPPLICATIONS );
    }

    /**
     * get AttributeRights to set from httprequest
     *
     * @param request
     *            http request
     * @return AttributeRights to set from httprequest
     */
    private Map<String, AttributeRight> getAttributesRightsFromRequest( HttpServletRequest request )
    {
        Map<String, AttributeRight> mapAttributesRights = new HashMap<String, AttributeRight>( );
        String [ ] tabIdReadbles = request.getParameterValues( PARAMETER_RIGHT_READABLE );
        String [ ] tabIdWritables = request.getParameterValues( PARAMETER_RIGHT_WRITABLE );
        String [ ] tabIdCertifiables = request.getParameterValues( PARAMETER_RIGHT_CERTIFIABLE );

        for ( int nCpt = 0; ( tabIdReadbles != null ) && ( nCpt < tabIdReadbles.length ); nCpt++ )
        {
            String strIdAttribute = tabIdReadbles [nCpt];
            AttributeRight attributeRight = new AttributeRight( );
            attributeRight.setReadable( true );
            attributeRight.setClientApplication( _clientApplication );
            attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ) ) );
            mapAttributesRights.put( strIdAttribute, attributeRight );
        }

        for ( int nCpt = 0; ( tabIdWritables != null ) && ( nCpt < tabIdWritables.length ); nCpt++ )
        {
            String strIdAttribute = tabIdWritables [nCpt];

            if ( mapAttributesRights.get( strIdAttribute ) != null )
            {
                mapAttributesRights.get( strIdAttribute ).setWritable( true );
            }
            else
            {
                AttributeRight attributeRight = new AttributeRight( );
                attributeRight.setClientApplication( _clientApplication );
                attributeRight.setWritable( true );
                attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ) ) );
                mapAttributesRights.put( strIdAttribute, attributeRight );
            }
        }

        for ( int nCpt = 0; ( tabIdCertifiables != null ) && ( nCpt < tabIdCertifiables.length ); nCpt++ )
        {
            String strIdAttribute = tabIdCertifiables [nCpt];

            if ( mapAttributesRights.get( strIdAttribute ) != null )
            {
                mapAttributesRights.get( strIdAttribute ).setCertifiable( true );
            }
            else
            {
                AttributeRight attributeRight = new AttributeRight( );
                attributeRight.setClientApplication( _clientApplication );
                attributeRight.setCertifiable( true );
                attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ) ) );
                mapAttributesRights.put( strIdAttribute, attributeRight );
            }
        }

        return mapAttributesRights;
    }
}

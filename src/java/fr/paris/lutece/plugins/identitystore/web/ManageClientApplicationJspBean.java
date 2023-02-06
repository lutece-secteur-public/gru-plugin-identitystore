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

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides the user interface to manage ClientApplication management features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageClientApplications.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class ManageClientApplicationJspBean extends ManageIdentitiesJspBean
{
    private static final long serialVersionUID = 1L;

    // Templates
    private static final String TEMPLATE_MANAGE_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/manage_clientapplications.html";
    private static final String TEMPLATE_DISPLAY_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/view_clientapplication.html";
    private static final String TEMPLATE_CREATE_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/create_clientapplication.html";
    private static final String TEMPLATE_MODIFY_CLIENTAPPLICATION = "/admin/plugins/identitystore/clientapplication/modify_clientapplication.html";
    private static final String TEMPLATE_MODIFY_CLIENTAPPLICATION_CERTIFICATOR = "/admin/plugins/identitystore/clientapplication/modify_clientapplication_certificator.html";

    // Parameters
    private static final String PARAMETER_ID_CLIENTAPPLICATION = "id";

    private static final String PARAMETER_CERTIFIERS_AUTH = "certif_auth";
    private static final String PARAMETER_CERTIFIER_CODE = "certif_code";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_CLIENTAPPLICATIONS = "identitystore.manage_clientapplications.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_CLIENTAPPLICATION = "identitystore.modify_clientapplication.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_CLIENTAPPLICATION = "identitystore.create_clientapplication.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_CLIENTAPPLICATION_CERTIFICATOR = "identitystore.modify_clientapplication_certificator.pageTitle";

    // Markers
    private static final String MARK_CLIENTAPPLICATION_LIST = "clientapplication_list";
    private static final String MARK_CLIENTAPPLICATION = "clientapplication";
    private static final String MARK_SERVICECONTRACTS = "servicecontract_list";
    private static final String MARK_ACTIVECONTRACT = "servicecontract_active";
    private static final String MARK_CLIENTAPPLICATION_CERTIF_LIST = "clientapplication_certifs";
    private static final String MARK_CLIENTAPPLICATION_CERTIF_CODE_MAP = "map_clientapplication_certifs";
    private static final String MARK_CERTIFIERS = "certifiers";
    private static final String MARK_CLIENTAPPLICATION_RIGHT_LIST = "clientapplication_rights_list";
    private static final String JSP_MANAGE_CLIENTAPPLICATIONS = "jsp/admin/plugins/identitystore/ManageClientApplications.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_CLIENTAPPLICATION = "identitystore.message.confirmRemoveClientApplication";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.clientapplication.attribute.";

    // Views
    private static final String VIEW_MANAGE_CLIENTAPPLICATIONS = "manageClientApplications";
    private static final String VIEW_DISPLAY_CLIENTAPPLICATIONS = "displayClientApplication";
    private static final String VIEW_CREATE_CLIENTAPPLICATION = "createClientApplication";
    private static final String VIEW_MODIFY_CLIENTAPPLICATION = "modifyClientApplication";
    private static final String VIEW_MANAGE_CLIENTAPPLICATION_CERTIFICATOR = "manageClientApplicationCertificate";

    // Actions
    private static final String ACTION_CREATE_CLIENTAPPLICATION = "createClientApplication";
    private static final String ACTION_MODIFY_CLIENTAPPLICATION = "modifyClientApplication";
    private static final String ACTION_MANAGE_CLIENTAPPLICATION_CERTIFICATOR = "manageClientApplicationCertificate";
    private static final String ACTION_REMOVE_CLIENTAPPLICATION = "removeClientApplication";
    private static final String ACTION_CONFIRM_REMOVE_CLIENTAPPLICATION = "confirmRemoveClientApplication";
    private static final String ACTION_REMOVE_CLIENTAPPLICATION_CERTIFICATOR = "removeClientApplicationCertificate";

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
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_DISPLAY_CLIENTAPPLICATIONS, defaultView = false )
    public String getDisplayClientApplications( HttpServletRequest request )
    {
        _clientApplication = null;

        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );

        Map<String, Object> model = getModel( );

        _clientApplication = ClientApplicationHome.findByPrimaryKey( nId );
        List<ServiceContract> serviceContracts = ClientApplicationHome.selectServiceContracts( _clientApplication );
        _clientApplication.setServiceContracts( serviceContracts );

        model.put( MARK_CLIENTAPPLICATION, _clientApplication );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_CLIENTAPPLICATIONS, TEMPLATE_DISPLAY_CLIENTAPPLICATION, model );
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
        _clientApplication = new ClientApplication( );

        Map<String, Object> model = getModel( );
        model.put( MARK_CLIENTAPPLICATION, _clientApplication );

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

        ClientApplication clientApplication = ClientApplicationHome.findByPrimaryKey( nId );
        ServiceContractService.instance( ).deleteApplication( clientApplication );
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
        // TODO attribute rights view on service contract now
        // model.put( MARK_CLIENTAPPLICATION_RIGHT_LIST, ClientApplicationHome.selectApplicationRights( _clientApplication ) );
        model.put( MARK_CLIENTAPPLICATION_CERTIF_LIST, ClientApplicationHome.getCertifiers( _clientApplication ) );

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

        ClientApplicationHome.update( _clientApplication );
        addInfo( INFO_CLIENTAPPLICATION_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_CLIENTAPPLICATIONS );
    }

    /**
     * manage ClientApplication certificates
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MANAGE_CLIENTAPPLICATION_CERTIFICATOR )
    public String getManageClientApplicationCertificators( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );

        if ( ( _clientApplication == null ) || ( _clientApplication.getId( ) != nId ) )
        {
            _clientApplication = ClientApplicationHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_CLIENTAPPLICATION, _clientApplication );
        model.put( MARK_CERTIFIERS, CertifierRegistry.instance( ).getCertifiersList( ) );
        // here we use a map as freemarker version doesn't support seq_contains
        Map<String, Boolean> mapCertifierClientApp = new HashMap<String, Boolean>( );
        for ( AbstractCertifier certifier : ClientApplicationHome.getCertifiers( _clientApplication ) )
        {
            mapCertifierClientApp.put( certifier.getCode( ), Boolean.TRUE );
        }
        model.put( MARK_CLIENTAPPLICATION_CERTIF_CODE_MAP, mapCertifierClientApp );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_CLIENTAPPLICATION_CERTIFICATOR, TEMPLATE_MODIFY_CLIENTAPPLICATION_CERTIFICATOR, model );
    }

    /**
     * change ClientApplication certificates
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage clientapplications
     */
    @Action( ACTION_MANAGE_CLIENTAPPLICATION_CERTIFICATOR )
    public String doManageClientApplicationCertificators( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );
        // WARNING getParameterValues give null if nothing is selected
        String [ ] tCertifiers = request.getParameterValues( PARAMETER_CERTIFIERS_AUTH );

        if ( ( _clientApplication == null ) || ( _clientApplication.getId( ) != nId ) )
        {
            return redirect( request, VIEW_MANAGE_CLIENTAPPLICATION_CERTIFICATOR, PARAMETER_ID_CLIENTAPPLICATION, nId );
        }
        // we have to remove deselected certifier and add new selected certifier
        ClientApplicationHome.cleanCertifiers( _clientApplication );
        if ( tCertifiers != null )
        {
            for ( String strCertifierCode : tCertifiers )
            {
                try
                {
                    AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( strCertifierCode );
                    ClientApplicationHome.addCertifier( _clientApplication, certifier );
                }
                catch( CertifierNotFoundException e )
                {
                    AppLogService.debug( e );
                }
            }
        }

        return redirect( request, VIEW_MODIFY_CLIENTAPPLICATION, PARAMETER_ID_CLIENTAPPLICATION, nId );
    }

    /**
     * Handles the removal of a clientapplication certificator
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage clientapplications
     */
    @Action( ACTION_REMOVE_CLIENTAPPLICATION_CERTIFICATOR )
    public String doClientApplicationCertificator( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_CLIENTAPPLICATION ) );
        if ( ( _clientApplication == null ) || ( _clientApplication.getId( ) != nId ) )
        {
            return redirect( request, VIEW_MODIFY_CLIENTAPPLICATION, PARAMETER_ID_CLIENTAPPLICATION, nId );
        }
        String strCertifierCode = request.getParameter( PARAMETER_CERTIFIER_CODE );

        try
        {
            AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( strCertifierCode );
            ClientApplicationHome.deleteCertifier( _clientApplication, certifier );
        }
        catch( CertifierNotFoundException e )
        {
            AppLogService.debug( e );
        }

        return redirect( request, VIEW_MODIFY_CLIENTAPPLICATION, PARAMETER_ID_CLIENTAPPLICATION, nId );
    }
}

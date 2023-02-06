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

import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevelHome;

/**
 * This class provides the user interface to manage RefCertificationLevel features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageRefCertificationLevels.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_REF_MANAGEMENT" )
public class RefCertificationLevelJspBean extends AbstractManageProcessusRefJspBean<Integer, RefCertificationLevel>
{
    // Templates
    private static final String TEMPLATE_MANAGE_REFCERTIFICATIONLEVELS = "/admin/plugins/identitystore/manage_refcertificationlevels.html";
    private static final String TEMPLATE_CREATE_REFCERTIFICATIONLEVEL = "/admin/plugins/identitystore/create_refcertificationlevel.html";
    private static final String TEMPLATE_MODIFY_REFCERTIFICATIONLEVEL = "/admin/plugins/identitystore/modify_refcertificationlevel.html";

    // Parameters
    private static final String PARAMETER_ID_REFCERTIFICATIONLEVEL = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_REFCERTIFICATIONLEVELS = "identitystore.manage_refcertificationlevels.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_REFCERTIFICATIONLEVEL = "identitystore.modify_refcertificationlevel.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_REFCERTIFICATIONLEVEL = "identitystore.create_refcertificationlevel.pageTitle";

    // Markers
    private static final String MARK_REFCERTIFICATIONLEVEL_LIST = "refcertificationlevel_list";
    private static final String MARK_REFCERTIFICATIONLEVEL = "refcertificationlevel";

    private static final String JSP_MANAGE_REFCERTIFICATIONLEVELS = "jsp/admin/plugins/identitystore/ManageRefCertificationLevels.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_REFCERTIFICATIONLEVEL = "identitystore.message.confirmRemoveRefCertificationLevel";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.refcertificationlevel.attribute.";

    // Views
    private static final String VIEW_MANAGE_REFCERTIFICATIONLEVELS = "manageRefCertificationLevels";
    private static final String VIEW_CREATE_REFCERTIFICATIONLEVEL = "createRefCertificationLevel";
    private static final String VIEW_MODIFY_REFCERTIFICATIONLEVEL = "modifyRefCertificationLevel";

    // Actions
    private static final String ACTION_CREATE_REFCERTIFICATIONLEVEL = "createRefCertificationLevel";
    private static final String ACTION_MODIFY_REFCERTIFICATIONLEVEL = "modifyRefCertificationLevel";
    private static final String ACTION_REMOVE_REFCERTIFICATIONLEVEL = "removeRefCertificationLevel";
    private static final String ACTION_CONFIRM_REMOVE_REFCERTIFICATIONLEVEL = "confirmRemoveRefCertificationLevel";

    // Infos
    private static final String INFO_REFCERTIFICATIONLEVEL_CREATED = "identitystore.info.refcertificationlevel.created";
    private static final String INFO_REFCERTIFICATIONLEVEL_UPDATED = "identitystore.info.refcertificationlevel.updated";
    private static final String INFO_REFCERTIFICATIONLEVEL_REMOVED = "identitystore.info.refcertificationlevel.removed";
    private static final String INFO_REFCERTIFICATIONLEVEL_NOT_REMOVED = "identitystore.info.refcertificationlevel.notremoved";

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    // Session variable to store working values
    private RefCertificationLevel _refcertificationlevel;
    private List<Integer> _listIdRefCertificationLevels;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_REFCERTIFICATIONLEVELS, defaultView = true )
    public String getManageRefCertificationLevels( HttpServletRequest request )
    {
        List<RefCertificationLevel> refCertificationLevelsList = RefCertificationLevelHome.getRefCertificationLevelsList( );

        Map<String, Object> model = getPaginatedListModel( request, MARK_REFCERTIFICATIONLEVEL_LIST, refCertificationLevelsList,
                JSP_MANAGE_REFCERTIFICATIONLEVELS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_REFCERTIFICATIONLEVELS, TEMPLATE_MANAGE_REFCERTIFICATIONLEVELS, model );
    }

    /**
     * Get Items from Ids list
     * 
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
    @Override
    List<RefCertificationLevel> getItemsFromIds( List<Integer> listIds )
    {
        List<RefCertificationLevel> listRefCertificationLevel = RefCertificationLevelHome.getRefCertificationLevelsListByIds( listIds );

        // keep original order
        return listRefCertificationLevel.stream( ).sorted( Comparator.comparingInt( notif -> listIds.indexOf( notif.getId( ) ) ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * reset the _listIdRefCertificationLevels list
     */
    public void resetListId( )
    {
        _listIdRefCertificationLevels = new ArrayList<>( );
    }

    /**
     * Returns the form to create a refcertificationlevel
     *
     * @param request
     *            The Http request
     * @return the html code of the refcertificationlevel form
     */
    @View( VIEW_CREATE_REFCERTIFICATIONLEVEL )
    public String getCreateRefCertificationLevel( HttpServletRequest request )
    {
        _refcertificationlevel = new RefCertificationLevel( );

        Map<String, Object> model = getModel( );
        model.put( MARK_REFCERTIFICATIONLEVEL, _refcertificationlevel );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_REFCERTIFICATIONLEVEL ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_REFCERTIFICATIONLEVEL, TEMPLATE_CREATE_REFCERTIFICATIONLEVEL, model );
    }

    /**
     * Process the data capture form of a new refcertificationlevel
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_REFCERTIFICATIONLEVEL )
    public String doCreateRefCertificationLevel( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refcertificationlevel, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_REFCERTIFICATIONLEVEL ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refcertificationlevel, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_REFCERTIFICATIONLEVEL );
        }

        RefCertificationLevelHome.create( _refcertificationlevel );
        addInfo( INFO_REFCERTIFICATIONLEVEL_CREATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFCERTIFICATIONLEVELS );
    }

    /**
     * Manages the removal form of a refcertificationlevel whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_REFCERTIFICATIONLEVEL )
    public String getConfirmRemoveRefCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFCERTIFICATIONLEVEL ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_REFCERTIFICATIONLEVEL ) );
        url.addParameter( PARAMETER_ID_REFCERTIFICATIONLEVEL, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_REFCERTIFICATIONLEVEL, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a refcertificationlevel
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage refcertificationlevels
     */
    @Action( ACTION_REMOVE_REFCERTIFICATIONLEVEL )
    public String doRemoveRefCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFCERTIFICATIONLEVEL ) );

        try
        {
            RefCertificationLevelHome.remove( nId );
        }
        catch( Exception e )
        {
            addInfo( "Le level ne peut être supprimé car il est utilisé dans la définition d'un processus" );
            return redirectView( request, VIEW_MANAGE_REFCERTIFICATIONLEVELS );
        }
        addInfo( INFO_REFCERTIFICATIONLEVEL_REMOVED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFCERTIFICATIONLEVELS );
    }

    /**
     * Returns the form to update info about a refcertificationlevel
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_REFCERTIFICATIONLEVEL )
    public String getModifyRefCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFCERTIFICATIONLEVEL ) );

        if ( _refcertificationlevel == null || ( _refcertificationlevel.getId( ) != nId ) )
        {
            _refcertificationlevel = RefCertificationLevelHome.findByPrimaryKey( nId );
            if ( _refcertificationlevel == null )
            {
                throw new AppException( ERROR_RESOURCE_NOT_FOUND );
            }
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_REFCERTIFICATIONLEVEL, _refcertificationlevel );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_REFCERTIFICATIONLEVEL ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_REFCERTIFICATIONLEVEL, TEMPLATE_MODIFY_REFCERTIFICATIONLEVEL, model );
    }

    /**
     * Process the change form of a refcertificationlevel
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_REFCERTIFICATIONLEVEL )
    public String doModifyRefCertificationLevel( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refcertificationlevel, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_REFCERTIFICATIONLEVEL ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refcertificationlevel, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_REFCERTIFICATIONLEVEL, PARAMETER_ID_REFCERTIFICATIONLEVEL, _refcertificationlevel.getId( ) );
        }

        RefCertificationLevelHome.update( _refcertificationlevel );
        addInfo( INFO_REFCERTIFICATIONLEVEL_UPDATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFCERTIFICATIONLEVELS );
    }
}

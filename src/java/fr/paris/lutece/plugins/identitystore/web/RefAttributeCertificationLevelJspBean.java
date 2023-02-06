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
import fr.paris.lutece.util.html.AbstractPaginator;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevelHome;

/**
 * This class provides the user interface to manage RefAttributeCertificationLevel features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageRefAttributeCertificationLevels.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_REF_MANAGEMENT" )
public class RefAttributeCertificationLevelJspBean extends AbstractManageProcessusRefJspBean<Integer, RefAttributeCertificationLevel>
{
    // Templates
    private static final String TEMPLATE_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS = "/admin/plugins/identitystore/manage_refattributecertificationlevels.html";
    private static final String TEMPLATE_CREATE_REFATTRIBUTECERTIFICATIONLEVEL = "/admin/plugins/identitystore/create_refattributecertificationlevel.html";
    private static final String TEMPLATE_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL = "/admin/plugins/identitystore/modify_refattributecertificationlevel.html";

    // Parameters
    private static final String PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL = "id";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS = "identitystore.manage_refattributecertificationlevels.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL = "identitystore.modify_refattributecertificationlevel.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_REFATTRIBUTECERTIFICATIONLEVEL = "identitystore.create_refattributecertificationlevel.pageTitle";

    // Markers
    private static final String MARK_REFATTRIBUTECERTIFICATIONLEVEL_LIST = "refattributecertificationlevel_list";
    private static final String MARK_REFATTRIBUTECERTIFICATIONLEVEL = "refattributecertificationlevel";

    private static final String JSP_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS = "jsp/admin/plugins/identitystore/ManageRefAttributeCertificationLevels.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL = "identitystore.message.confirmRemoveRefAttributeCertificationLevel";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.refattributecertificationlevel.attribute.";

    // Views
    private static final String VIEW_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS = "manageRefAttributeCertificationLevels";
    private static final String VIEW_CREATE_REFATTRIBUTECERTIFICATIONLEVEL = "createRefAttributeCertificationLevel";
    private static final String VIEW_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL = "modifyRefAttributeCertificationLevel";

    // Actions
    private static final String ACTION_CREATE_REFATTRIBUTECERTIFICATIONLEVEL = "createRefAttributeCertificationLevel";
    private static final String ACTION_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL = "modifyRefAttributeCertificationLevel";
    private static final String ACTION_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL = "removeRefAttributeCertificationLevel";
    private static final String ACTION_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL = "confirmRemoveRefAttributeCertificationLevel";

    // Infos
    private static final String INFO_REFATTRIBUTECERTIFICATIONLEVEL_CREATED = "identitystore.info.refattributecertificationlevel.created";
    private static final String INFO_REFATTRIBUTECERTIFICATIONLEVEL_UPDATED = "identitystore.info.refattributecertificationlevel.updated";
    private static final String INFO_REFATTRIBUTECERTIFICATIONLEVEL_REMOVED = "identitystore.info.refattributecertificationlevel.removed";

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    // Session variable to store working values
    private RefAttributeCertificationLevel _refattributecertificationlevel;
    private List<Integer> _listIdRefAttributeCertificationLevels;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS, defaultView = true )
    public String getManageRefAttributeCertificationLevels( HttpServletRequest request )
    {
        _refattributecertificationlevel = null;

        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX ) == null || _listIdRefAttributeCertificationLevels.isEmpty( ) )
        {
            _listIdRefAttributeCertificationLevels = RefAttributeCertificationLevelHome.getIdRefAttributeCertificationLevelsList( );
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_REFATTRIBUTECERTIFICATIONLEVEL_LIST, _listIdRefAttributeCertificationLevels,
                JSP_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS, TEMPLATE_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS, model );
    }

    /**
     * Get Items from Ids list
     * 
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
    @Override
    List<RefAttributeCertificationLevel> getItemsFromIds( List<Integer> listIds )
    {
        List<RefAttributeCertificationLevel> listRefAttributeCertificationLevel = RefAttributeCertificationLevelHome
                .getRefAttributeCertificationLevelsListByIds( listIds );

        // keep original order
        return listRefAttributeCertificationLevel.stream( ).sorted( Comparator.comparingInt( notif -> listIds.indexOf( notif.getId( ) ) ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * reset the _listIdRefAttributeCertificationLevels list
     */
    public void resetListId( )
    {
        _listIdRefAttributeCertificationLevels = new ArrayList<>( );
    }

    /**
     * Returns the form to create a refattributecertificationlevel
     *
     * @param request
     *            The Http request
     * @return the html code of the refattributecertificationlevel form
     */
    @View( VIEW_CREATE_REFATTRIBUTECERTIFICATIONLEVEL )
    public String getCreateRefAttributeCertificationLevel( HttpServletRequest request )
    {
        _refattributecertificationlevel = new RefAttributeCertificationLevel( );

        Map<String, Object> model = getModel( );
        model.put( MARK_REFATTRIBUTECERTIFICATIONLEVEL, _refattributecertificationlevel );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_REFATTRIBUTECERTIFICATIONLEVEL ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_REFATTRIBUTECERTIFICATIONLEVEL, TEMPLATE_CREATE_REFATTRIBUTECERTIFICATIONLEVEL, model );
    }

    /**
     * Process the data capture form of a new refattributecertificationlevel
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_REFATTRIBUTECERTIFICATIONLEVEL )
    public String doCreateRefAttributeCertificationLevel( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refattributecertificationlevel, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_REFATTRIBUTECERTIFICATIONLEVEL ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refattributecertificationlevel, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_REFATTRIBUTECERTIFICATIONLEVEL );
        }

        RefAttributeCertificationLevelHome.create( _refattributecertificationlevel );
        addInfo( INFO_REFATTRIBUTECERTIFICATIONLEVEL_CREATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS );
    }

    /**
     * Manages the removal form of a refattributecertificationlevel whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL )
    public String getConfirmRemoveRefAttributeCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL ) );
        url.addParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a refattributecertificationlevel
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage refattributecertificationlevels
     */
    @Action( ACTION_REMOVE_REFATTRIBUTECERTIFICATIONLEVEL )
    public String doRemoveRefAttributeCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL ) );

        RefAttributeCertificationLevelHome.remove( nId );
        addInfo( INFO_REFATTRIBUTECERTIFICATIONLEVEL_REMOVED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS );
    }

    /**
     * Returns the form to update info about a refattributecertificationlevel
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL )
    public String getModifyRefAttributeCertificationLevel( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL ) );

        if ( _refattributecertificationlevel == null || ( _refattributecertificationlevel.getId( ) != nId ) )
        {
            Optional<RefAttributeCertificationLevel> optRefAttributeCertificationLevel = RefAttributeCertificationLevelHome.findByPrimaryKey( nId );
            _refattributecertificationlevel = optRefAttributeCertificationLevel.orElseThrow( ( ) -> new AppException( ERROR_RESOURCE_NOT_FOUND ) );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_REFATTRIBUTECERTIFICATIONLEVEL, _refattributecertificationlevel );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL, TEMPLATE_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL, model );
    }

    /**
     * Process the change form of a refattributecertificationlevel
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL )
    public String doModifyRefAttributeCertificationLevel( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refattributecertificationlevel, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refattributecertificationlevel, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_REFATTRIBUTECERTIFICATIONLEVEL, PARAMETER_ID_REFATTRIBUTECERTIFICATIONLEVEL,
                    _refattributecertificationlevel.getId( ) );
        }

        RefAttributeCertificationLevelHome.update( _refattributecertificationlevel );
        addInfo( INFO_REFATTRIBUTECERTIFICATIONLEVEL_UPDATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONLEVELS );
    }
}

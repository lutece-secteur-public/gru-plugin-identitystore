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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessusHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.service.contract.AttributeCertificationDefinitionService;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage RefAttributeCertificationProcessus features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageRefAttributeCertificationProcessuss.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_REF_MANAGEMENT" )
public class RefAttributeCertificationProcessusJspBean extends AbstractManageProcessusRefJspBean<Integer, RefAttributeCertificationProcessus>
{
    // Templates
    private static final String TEMPLATE_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS = "/admin/plugins/identitystore/manage_refattributecertificationprocessuss.html";
    private static final String TEMPLATE_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS = "/admin/plugins/identitystore/create_refattributecertificationprocessus.html";
    private static final String TEMPLATE_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS = "/admin/plugins/identitystore/modify_refattributecertificationprocessus.html";

    // Parameters
    private static final String PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS = "id";

    private static final String PARAMETER_CERTICATION_LEVEL = "certification_level";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS = "identitystore.manage_refattributecertificationprocessuss.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS = "identitystore.modify_refattributecertificationprocessus.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS = "identitystore.create_refattributecertificationprocessus.pageTitle";

    // Markers
    private static final String MARK_REFATTRIBUTECERTIFICATIONPROCESSUS_LIST = "refattributecertificationprocessus_list";
    private static final String MARK_REFATTRIBUTECERTIFICATIONPROCESSUS = "refattributecertificationprocessus";

    private static final String MARK_ATTRIBUTE_LEVELS_LIST = "refattributecertificationlevels_attribute_list";
    private static final String MARK_AVAILAIBLE_LEVELS_LIST = "availaible_certification_levels_list";

    private static final String JSP_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS = "jsp/admin/plugins/identitystore/ManageRefAttributeCertificationProcessuss.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS = "identitystore.message.confirmRemoveRefAttributeCertificationProcessus";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.refattributecertificationprocessus.attribute.";

    // Views
    private static final String VIEW_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS = "manageRefAttributeCertificationProcessuss";
    private static final String VIEW_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS = "createRefAttributeCertificationProcessus";
    private static final String VIEW_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS = "modifyRefAttributeCertificationProcessus";

    // Actions
    private static final String ACTION_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS = "createRefAttributeCertificationProcessus";
    private static final String ACTION_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS = "modifyRefAttributeCertificationProcessus";
    private static final String ACTION_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS = "removeRefAttributeCertificationProcessus";
    private static final String ACTION_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS = "confirmRemoveRefAttributeCertificationProcessus";

    // Infos
    private static final String INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_CREATED = "identitystore.info.refattributecertificationprocessus.created";
    private static final String INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_UPDATED = "identitystore.info.refattributecertificationprocessus.updated";
    private static final String INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_REMOVED = "identitystore.info.refattributecertificationprocessus.removed";

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    // Session variable to store working values
    private RefAttributeCertificationProcessus _refattributecertificationprocessus;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS, defaultView = true )
    public String getManageRefAttributeCertificationProcessuss( HttpServletRequest request )
    {
        List<RefAttributeCertificationProcessus> refAttributeCertificationProcessussList = RefAttributeCertificationProcessusHome
                .getRefAttributeCertificationProcessussList( );
        refAttributeCertificationProcessussList.sort( Comparator.comparing( RefAttributeCertificationProcessus::getCode ) );

        Map<String, Object> model = getPaginatedListModel( request, MARK_REFATTRIBUTECERTIFICATIONPROCESSUS_LIST, refAttributeCertificationProcessussList,
                JSP_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS, TEMPLATE_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS, model );
    }

    /**
     * Get Items from Ids list
     * 
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
    @Override
    List<RefAttributeCertificationProcessus> getItemsFromIds( List<Integer> listIds )
    {
        List<RefAttributeCertificationProcessus> listRefAttributeCertificationProcessus = RefAttributeCertificationProcessusHome
                .getRefAttributeCertificationProcessussListByIds( listIds );

        // keep original order
        return listRefAttributeCertificationProcessus.stream( ).sorted( Comparator.comparingInt( notif -> listIds.indexOf( notif.getId( ) ) ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * Returns the form to create a refattributecertificationprocessus
     *
     * @param request
     *            The Http request
     * @return the html code of the refattributecertificationprocessus form
     */
    @View( VIEW_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String getCreateRefAttributeCertificationProcessus( HttpServletRequest request )
    {
        _refattributecertificationprocessus = new RefAttributeCertificationProcessus( );

        Map<String, Object> model = getModel( );
        model.put( MARK_REFATTRIBUTECERTIFICATIONPROCESSUS, _refattributecertificationprocessus );
        model.put( MARK_ATTRIBUTE_LEVELS_LIST, RefAttributeCertificationProcessusHome.selectAttributeLevels( _refattributecertificationprocessus ) );
        model.put( MARK_AVAILAIBLE_LEVELS_LIST, RefAttributeCertificationProcessusHome.selectCertificationLevels( ).stream( )
                .sorted( Comparator.comparing( RefCertificationLevel::getLevel ) ).collect( Collectors.toList( ) ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS, TEMPLATE_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS, model );
    }

    /**
     * Process the data capture form of a new refattributecertificationprocessus
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String doCreateRefAttributeCertificationProcessus( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refattributecertificationprocessus, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refattributecertificationprocessus, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_REFATTRIBUTECERTIFICATIONPROCESSUS );
        }

        RefAttributeCertificationProcessusHome.create( _refattributecertificationprocessus );

        // Manage children
        List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>(
                getRefAttributeCertificationLevelsFromRequest( request ).values( ) );
        AttributeCertificationDefinitionService.instance( ).addRefAttributeCertificationLevels(
                refAttributeCertificationLevelList.stream( ).filter( certif -> certif.getRefCertificationLevel( ) != null ).collect( Collectors.toList( ) ) );

        addInfo( INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS );
    }

    /**
     * Manages the removal form of a refattributecertificationprocessus whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String getConfirmRemoveRefAttributeCertificationProcessus( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS ) );
        url.addParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a refattributecertificationprocessus
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage refattributecertificationprocessuss
     */
    @Action( ACTION_REMOVE_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String doRemoveRefAttributeCertificationProcessus( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS ) );

        final RefAttributeCertificationProcessus processus = RefAttributeCertificationProcessusHome.findByPrimaryKey( nId );
        AttributeCertificationDefinitionService.instance( ).removeRefAttributeCertificationLevels( processus );
        RefAttributeCertificationProcessusHome.remove( nId );
        addInfo( INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS );
    }

    /**
     * Returns the form to update info about a refattributecertificationprocessus
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String getModifyRefAttributeCertificationProcessus( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS ) );

        if ( _refattributecertificationprocessus == null || ( _refattributecertificationprocessus.getId( ) != nId ) )
        {
            _refattributecertificationprocessus = RefAttributeCertificationProcessusHome.findByPrimaryKey( nId );
            if ( _refattributecertificationprocessus == null )
            {
                throw new AppException( ERROR_RESOURCE_NOT_FOUND );
            }

        }

        Map<String, Object> model = getModel( );
        model.put( MARK_REFATTRIBUTECERTIFICATIONPROCESSUS, _refattributecertificationprocessus );
        model.put( MARK_ATTRIBUTE_LEVELS_LIST, RefAttributeCertificationProcessusHome.selectAttributeLevels( _refattributecertificationprocessus ) );
        model.put( MARK_AVAILAIBLE_LEVELS_LIST, RefAttributeCertificationProcessusHome.selectCertificationLevels( ).stream( )
                .sorted( Comparator.comparing( RefCertificationLevel::getLevel ) ).collect( Collectors.toList( ) ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS, TEMPLATE_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS, model );
    }

    /**
     * Process the change form of a refattributecertificationprocessus
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS )
    public String doModifyRefAttributeCertificationProcessus( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _refattributecertificationprocessus, request, getLocale( ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _refattributecertificationprocessus, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_REFATTRIBUTECERTIFICATIONPROCESSUS, PARAMETER_ID_REFATTRIBUTECERTIFICATIONPROCESSUS,
                    _refattributecertificationprocessus.getId( ) );
        }

        RefAttributeCertificationProcessusHome.update( _refattributecertificationprocessus );

        // Manage children
        AttributeCertificationDefinitionService.instance( ).removeRefAttributeCertificationLevels( _refattributecertificationprocessus );
        List<RefAttributeCertificationLevel> refAttributeCertificationLevelList = new ArrayList<>(
                getRefAttributeCertificationLevelsFromRequest( request ).values( ) );
        AttributeCertificationDefinitionService.instance( ).addRefAttributeCertificationLevels(
                refAttributeCertificationLevelList.stream( ).filter( certif -> certif.getRefCertificationLevel( ) != null ).collect( Collectors.toList( ) ) );

        addInfo( INFO_REFATTRIBUTECERTIFICATIONPROCESSUS_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_REFATTRIBUTECERTIFICATIONPROCESSUSS );
    }

    /**
     * get AttributeRights to set from httprequest
     *
     * @param request
     *            http request
     * @return AttributeRights to set from httprequest
     */
    private Map<String, RefAttributeCertificationLevel> getRefAttributeCertificationLevelsFromRequest( HttpServletRequest request )
    {
        Map<String, RefAttributeCertificationLevel> mapAttributesRights = new HashMap<String, RefAttributeCertificationLevel>( );
        String [ ] tabIdCertificationLevels = request.getParameterValues( PARAMETER_CERTICATION_LEVEL );

        for ( int nCpt = 0; ( tabIdCertificationLevels != null ) && ( nCpt < tabIdCertificationLevels.length ); nCpt++ )
        {
            String strIdAttribute = tabIdCertificationLevels [nCpt];
            RefAttributeCertificationLevel refAttributeCertificationLevel = new RefAttributeCertificationLevel( );
            refAttributeCertificationLevel.setRefAttributeCertificationProcessus( _refattributecertificationprocessus );
            if ( strIdAttribute != null && strIdAttribute.contains( "," ) )
            {
                String [ ] keyAndLevel = strIdAttribute.split( "," );
                refAttributeCertificationLevel.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( keyAndLevel [0] ), false ) );
                refAttributeCertificationLevel.setRefCertificationLevel( RefCertificationLevelHome.findByPrimaryKey( Integer.parseInt( keyAndLevel [1] ) ) );
            }
            mapAttributesRights.put( strIdAttribute, refAttributeCertificationLevel );
        }

        return mapAttributesRights;
    }
}

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
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * This class provides the user interface to manage AttributeKey features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageAttributeKeys.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class AttributeKeyJspBean extends AdminIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_ATTRIBUTEKEYS = "/admin/plugins/identitystore/manage_attributekeys.html";
    private static final String TEMPLATE_CREATE_ATTRIBUTEKEY = "/admin/plugins/identitystore/create_attributekey.html";
    private static final String TEMPLATE_MODIFY_ATTRIBUTEKEY = "/admin/plugins/identitystore/modify_attributekey.html";
    private static final String TEMPLATE_APP_RIGHT_ATTRIBUTES = "/admin/plugins/identitystore/view_apprightattributes.html";

    // Parameters
    private static final String PARAMETER_ID_ATTRIBUTEKEY = "id";
    private static final String PARAMETER_ID_KEY_TYPE = "id_keytype";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTEKEYS = "identitystore.manage_attributekeys.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTEKEY = "identitystore.modify_attributekey.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTEKEY = "identitystore.create_attributekey.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_APP_RIGHT_ATTRIBUTES = "identitystore.view_appRightAttributes.pageTitle";
    private static final String PROPERTY_MANAGE_ATTRIBUTEKEYS_DUPLICATE_ERROR_MESSAGE = "identitystore.validation.attributekey.KeyName.duplicate";

    // Markers
    private static final String MARK_ATTRIBUTEKEY_LIST = "attributekey_list";
    private static final String MARK_ATTRIBUTE_APPS_RIGHT_MAP = "attribute_apps_right_map";
    private static final String MARK_ATTRIBUTEKEY = "attributekey";
    private static final String MARK_KEYTYPE_LIST = "keytype_list";
    private static final String MARK_WEBAPP_URL = "webapp_url";
    private static final String MARK_LOCALE = "locale";
    private static final String JSP_MANAGE_ATTRIBUTEKEYS = "jsp/admin/plugins/identitystore/ManageAttributeKeys.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_ATTRIBUTEKEY = "identitystore.message.confirmRemoveAttributeKey";
    private static final String MESSAGE_CANNOT_REMOVE_REFERENCE_ATTRIBUTE_EXISTS = "identitystore.message.cannotRemoveReferenceAttributeExists";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.attributekey.attribute.";

    // Views
    private static final String VIEW_MANAGE_ATTRIBUTEKEYS = "manageAttributeKeys";
    private static final String VIEW_CREATE_ATTRIBUTEKEY = "createAttributeKey";
    private static final String VIEW_MODIFY_ATTRIBUTEKEY = "modifyAttributeKey";
    private static final String VIEW_APP_RIGHT_ATTRIBUTES = "appRightAttributes";

    // Actions
    private static final String ACTION_CREATE_ATTRIBUTEKEY = "createAttributeKey";
    private static final String ACTION_MODIFY_ATTRIBUTEKEY = "modifyAttributeKey";
    private static final String ACTION_REMOVE_ATTRIBUTEKEY = "removeAttributeKey";
    private static final String ACTION_CONFIRM_REMOVE_ATTRIBUTEKEY = "confirmRemoveAttributeKey";

    // Infos
    private static final String INFO_ATTRIBUTEKEY_CREATED = "identitystore.info.attributekey.created";
    private static final String INFO_ATTRIBUTEKEY_UPDATED = "identitystore.info.attributekey.updated";
    private static final String INFO_ATTRIBUTEKEY_REMOVED = "identitystore.info.attributekey.removed";

    // Session variable to store working values
    private AttributeKey _attributekey;

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_ATTRIBUTEKEYS, defaultView = true )
    public String getManageAttributeKeys( HttpServletRequest request )
    {
        _attributekey = null;

        List<AttributeKey> listAttributeKeys = AttributeKeyHome.getAttributeKeysList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_ATTRIBUTEKEY_LIST, listAttributeKeys, JSP_MANAGE_ATTRIBUTEKEYS );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_ATTRIBUTEKEYS, TEMPLATE_MANAGE_ATTRIBUTEKEYS, model );
    }

    /**
     * Returns the form to create a attributekey
     *
     * @param request
     *            The Http request
     * @return the html code of the attributekey form
     */
    @View( VIEW_CREATE_ATTRIBUTEKEY )
    public String getCreateAttributeKey( HttpServletRequest request )
    {
        _attributekey = ( _attributekey != null ) ? _attributekey : new AttributeKey( );

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTEKEY, _attributekey );
        model.put( MARK_KEYTYPE_LIST, KeyType.getReferenceList( request.getLocale( ) ) );
        storeRichText( request, model );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_ATTRIBUTEKEY, TEMPLATE_CREATE_ATTRIBUTEKEY, model );
    }

    /**
     * Process the data capture form of a new attributekey
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_ATTRIBUTEKEY )
    public String doCreateAttributeKey( HttpServletRequest request )
    {
        populate( _attributekey, request );

        // Check constraints
        if ( !validateBean( _attributekey, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirectView( request, VIEW_CREATE_ATTRIBUTEKEY );
        }

        String strIdKeyType = request.getParameter( PARAMETER_ID_KEY_TYPE );
        int nKeyType = Integer.parseInt( strIdKeyType );
        KeyType keyType = KeyType.valueOf( nKeyType );
        _attributekey.setKeyType( keyType );

        if ( AttributeKeyHome.findByKey( _attributekey.getKeyName( ) ) != null )
        {
            addError( PROPERTY_MANAGE_ATTRIBUTEKEYS_DUPLICATE_ERROR_MESSAGE, getLocale( ) );

            return redirectView( request, VIEW_CREATE_ATTRIBUTEKEY );
        }

        IdentityService.instance( ).createAttributeKey( _attributekey );
        addInfo( INFO_ATTRIBUTEKEY_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTEKEYS );
    }

    /**
     * Manages the removal form of a attributekey whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_ATTRIBUTEKEY )
    public String getConfirmRemoveAttributeKey( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTEKEY ) );

        if ( AttributeKeyHome.checkAttributeId( nId ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CANNOT_REMOVE_REFERENCE_ATTRIBUTE_EXISTS, AdminMessage.TYPE_ERROR ) );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_ATTRIBUTEKEY ) );
        url.addParameter( PARAMETER_ID_ATTRIBUTEKEY, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_ATTRIBUTEKEY, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a attributekey
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage attributekeys
     */
    @Action( ACTION_REMOVE_ATTRIBUTEKEY )
    public String doRemoveAttributeKey( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTEKEY ) );
        final AttributeKey attributeKey = AttributeKeyHome.findByPrimaryKey( nId );
        if ( attributeKey == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CANNOT_REMOVE_REFERENCE_ATTRIBUTE_EXISTS, AdminMessage.TYPE_ERROR ) );
        }
        IdentityService.instance( ).deleteAttributeKey( attributeKey );
        addInfo( INFO_ATTRIBUTEKEY_REMOVED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTEKEYS );
    }

    /**
     * Returns the form to update info about a attributekey
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_ATTRIBUTEKEY )
    public String getModifyAttributeKey( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_ATTRIBUTEKEY ) );

        if ( ( _attributekey == null ) || ( _attributekey.getId( ) != nId ) )
        {
            _attributekey = AttributeKeyHome.findByPrimaryKey( nId );
        }

        Map<String, Object> model = getModel( );
        model.put( MARK_ATTRIBUTEKEY, _attributekey );
        model.put( MARK_KEYTYPE_LIST, KeyType.getReferenceList( request.getLocale( ) ) );
        storeRichText( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_ATTRIBUTEKEY, TEMPLATE_MODIFY_ATTRIBUTEKEY, model );
    }

    /**
     * Process the change form of a attributekey
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_ATTRIBUTEKEY )
    public String doModifyAttributeKey( HttpServletRequest request )
    {
        populate( _attributekey, request );

        // Check constraints
        if ( !validateBean( _attributekey, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_ATTRIBUTEKEY, PARAMETER_ID_ATTRIBUTEKEY, _attributekey.getId( ) );
        }

        IdentityService.instance( ).updateAttributeKey( _attributekey );
        addInfo( INFO_ATTRIBUTEKEY_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_ATTRIBUTEKEYS );
    }

    /**
     * fill model for richText
     * 
     * @param request
     *            the request
     * @param model
     *            the model
     */
    private static void storeRichText( HttpServletRequest request, Map<String, Object> model )
    {
        model.put( MARK_WEBAPP_URL, AppPathService.getBaseUrl( request ) );
        model.put( MARK_LOCALE, AdminUserService.getLocale( request ).getLanguage( ) );
    }

    /**
     * Returns the display of attribute with application rights
     *
     * @param request
     *            The Http request
     * @return The HTML info
     */
    @View( VIEW_APP_RIGHT_ATTRIBUTES )
    public String getApplicationRight( HttpServletRequest request )
    {
        List<AttributeKey> listAttributeKeys = AttributeKeyHome.getAttributeKeysList( );
        Map<String, Object> model = getPaginatedListModel( request, MARK_ATTRIBUTEKEY_LIST, listAttributeKeys,
                JSP_MANAGE_ATTRIBUTEKEYS + "?view=" + VIEW_APP_RIGHT_ATTRIBUTES );

        // TODO
        // Map<String, AttributeApplicationsRight> mapAttributeApplicationsRight = ClientApplicationHome.getAttributeApplicationsRight( );
        // model.put( MARK_ATTRIBUTE_APPS_RIGHT_MAP, mapAttributeApplicationsRight );

        return getPage( PROPERTY_PAGE_TITLE_APP_RIGHT_ATTRIBUTES, TEMPLATE_APP_RIGHT_ATTRIBUTES, model );
    }
}

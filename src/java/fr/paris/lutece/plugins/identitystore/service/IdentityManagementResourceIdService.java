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
package fr.paris.lutece.plugins.identitystore.service;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

/**
 *
 */
public class IdentityManagementResourceIdService extends ResourceIdService
{
    /** Permission for viewing a ticket domain */
    public static final String PERMISSION_CREATE_IDENTITY = "CREATE_IDENTITY";
    public static final String PERMISSION_MODIFY_IDENTITY = "MODIFY_IDENTITY";
    public static final String PERMISSION_DELETE_IDENTITY = "DELETE_IDENTITY";
    public static final String PERMISSION_VIEW_IDENTITY = "VIEW_IDENTITY";
    public static final String PERMISSION_ATTRIBUTS_HISTO = "ATTRIBUTS_HISTO";
    public static final String RESOURCE_TYPE = "IDENTITY";
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "identitystore.identity.resourceType";
    private static final String PROPERTY_LABEL_CREATE_IDENTITY = "identitystore.identity.permission.label.create";
    private static final String PROPERTY_LABEL_MODIFY_IDENTITY = "identitystore.identity.permission.label.modify";
    private static final String PROPERTY_LABEL_DELETE_IDENTITY = "identitystore.identity.permission.label.delete";
    private static final String PROPERTY_LABEL_VIEW_IDENTITY = "identitystore.identity.permission.label.view";
    private static final String PROPERTY_LABEL_ATTRIBUTS_HISTO = "identitystore.identity.permission.label.attributs_histo";

    /**
     * Constructor
     */
    public IdentityManagementResourceIdService( )
    {
        setPluginName( IdentityStorePlugin.PLUGIN_NAME );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register( )
    {
        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( IdentityManagementResourceIdService.class.getName( ) );
        rt.setPluginName( IdentityStorePlugin.PLUGIN_NAME );
        rt.setResourceTypeKey( RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission p = new Permission( );
        p.setPermissionKey( PERMISSION_CREATE_IDENTITY );
        p.setPermissionTitleKey( PROPERTY_LABEL_CREATE_IDENTITY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_MODIFY_IDENTITY );
        p.setPermissionTitleKey( PROPERTY_LABEL_MODIFY_IDENTITY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_DELETE_IDENTITY );
        p.setPermissionTitleKey( PROPERTY_LABEL_DELETE_IDENTITY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_VIEW_IDENTITY );
        p.setPermissionTitleKey( PROPERTY_LABEL_VIEW_IDENTITY );
        rt.registerPermission( p );

        p = new Permission( );
        p.setPermissionKey( PERMISSION_ATTRIBUTS_HISTO );
        p.setPermissionTitleKey( PROPERTY_LABEL_ATTRIBUTS_HISTO );
        rt.registerPermission( p );

        ResourceTypeManager.registerResourceType( rt );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( String strId, Locale locale )
    {
        return StringUtils.EMPTY;
    }

    /**
     * Check if a user has a permission
     * 
     * @param strPermission
     *            the permission to check
     * @param adminUser
     *            the user to check
     * @return true if user is allowed
     */
    public static boolean isAuthorized( String strPermission, AdminUser adminUser )
    {
        return RBACService.isAuthorized( RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, strPermission, adminUser );
    }
}

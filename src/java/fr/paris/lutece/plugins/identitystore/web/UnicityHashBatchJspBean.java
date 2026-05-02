/*
 * Copyright (c) 2002-2026, City of Paris
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

import fr.paris.lutece.plugins.identitystore.service.identity.batch.UnicityHashCodeBatchTask;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Admin JspBean to batch-initialize the unicity_hash_code column
 * for all identities in the identitystore_identity table.
 * <br>
 * This feature is restricted to admin users of level 0.
 */
@Controller( controllerJsp = "ManageUnicityHashBatch.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_UNICITY_HASH_BATCH" )
public class UnicityHashBatchJspBean extends MVCAdminJspBean
{
    private static final long serialVersionUID = 1L;

    // Templates
    private static final String TEMPLATE_BATCH = "admin/plugins/identitystore/manage_unicity_hash_batch.html";

    // Properties
    private static final String PROPERTY_PAGE_TITLE = "identitystore.unicityHashBatch.pageTitle";

    // Markers
    private static final String MARK_LAST_LOGS = "batch_last_logs";

    // Views
    private static final String VIEW_BATCH = "viewBatch";

    // Actions
    private static final String ACTION_START_BATCH = "startBatch";

    private final UnicityHashCodeBatchTask _task = SpringContextService.getBean( "identitystore.unicityHashCodeBatchTask" );

    @View( value = VIEW_BATCH, defaultView = true )
    public String getViewBatch( final HttpServletRequest request ) throws AccessDeniedException
    {
        this.checkAdminLevel0( );

        String lastLogs = "";
        if ( _task.getStatus( ) != null )
        {
            lastLogs = _task.getStatus( ).getLogs( );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_LAST_LOGS, lastLogs );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_START_BATCH ) );

        return getPage( PROPERTY_PAGE_TITLE, TEMPLATE_BATCH, model );
    }

    @Action( ACTION_START_BATCH )
    public String doStartBatch( final HttpServletRequest request ) throws AccessDeniedException
    {
        this.checkAdminLevel0( );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_START_BATCH ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        if ( _task.getStatus( ) == null || !_task.getStatus( ).isRunning( ) )
        {
            new Thread( _task::doJob, "UnicityHashBatch-Worker" ).start( );
        }

        return redirectView( request, VIEW_BATCH );
    }

    private void checkAdminLevel0( ) throws AccessDeniedException
    {
        if ( this.getUser( ) == null || this.getUser( ).getUserLevel( ) != 0 )
        {
            throw new AccessDeniedException( "Access denied: admin level 0 required" );
        }
    }
}
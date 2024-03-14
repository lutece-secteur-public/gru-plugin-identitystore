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

import fr.paris.lutece.plugins.identitystore.service.IdentityManagementResourceIdService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.FullIndexTask;
import fr.paris.lutece.portal.business.progressmanager.ProgressFeed;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.progressmanager.ProgressManagerService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This class provides the user interface to index Identity features
 */
@Controller( controllerJsp = "IndexIdentities.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IndexIdentityJspBean extends ManageIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_INDEX_IDENTITIES = "/admin/plugins/identitystore/index_identities.html";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitystore.index_identities.pageTitle";

    // Markers
    private static final String MARK_INDEX_LAST_LOGS = "index_last_logs";
    private static final String MARK_HAS_INDEX_ROLE = "indexIdentityRole";

    // Views
    private static final String VIEW_INDEX_IDENTITIES = "indexIdentities";

    // Actions
    private static final String ACTION_INDEX_IDENTITIES = "indexIdentities";

    // Feed
    private static final String MARK_FEED_TOKEN = "feed_token";
    private static final String FEED_NAME = "fullIndexFeed";
    private String _feedToken;
    private final ProgressManagerService progressManagerService = ProgressManagerService.getInstance( );
    private final FullIndexTask _fullIndexTask = SpringContextService.getBean( "identitystore.fullIndexer" );
    private String _strLastLogs;

    @View( value = VIEW_INDEX_IDENTITIES, defaultView = true )
    public String getInterface( HttpServletRequest request )
    {
        if ( _fullIndexTask.getStatus( ) != null )
        {
            _strLastLogs = _fullIndexTask.getStatus( ).getLogs( );
        }
        this.registerFeed( );
        final Map<String, Object> model = getModel( );
        model.put( MARK_HAS_INDEX_ROLE,
                IdentityManagementResourceIdService.isAuthorized( IdentityManagementResourceIdService.PERMISSION_INDEX_IDENTITY, getUser( ) ) );
        model.put( MARK_INDEX_LAST_LOGS, _strLastLogs );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_INDEX_IDENTITIES ) );
        model.put( MARK_FEED_TOKEN, _feedToken );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_INDEX_IDENTITIES, model );
    }

    /**
     * Process the change form of a servicecontract
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_INDEX_IDENTITIES )
    public String doIndexIdentities( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_INDEX_IDENTITIES ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }
        if ( _fullIndexTask.getStatus( ) == null || !_fullIndexTask.getStatus( ).isRunning( ) )
        {
            _fullIndexTask.doJob( _feedToken );
        }
        this.unregisterFeed( );
        return redirectView( request, VIEW_INDEX_IDENTITIES );
    }

    private void unregisterFeed( )
    {
        progressManagerService.unRegisterFeed( _feedToken );
        _feedToken = null;
    }

    private void registerFeed( )
    {
        final Optional<ProgressFeed> progressFeed = progressManagerService.getProgressFeeds().values().stream()
                .filter(feed -> Objects.equals(FEED_NAME, feed.getId())).findFirst();
        if(progressFeed.isPresent()) {
            _feedToken = progressFeed.get().getToken();
        } else {
            _feedToken = progressManagerService.registerFeed( FEED_NAME, 0 );
        }
    }
}

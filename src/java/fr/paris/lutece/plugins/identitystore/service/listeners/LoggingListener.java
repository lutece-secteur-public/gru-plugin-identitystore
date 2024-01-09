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
package fr.paris.lutece.plugins.identitystore.service.listeners;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.AttributeChangeListener;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 * Logging Listener
 */
public class LoggingListener implements AttributeChangeListener
{
    private static final String SERVICE_NAME = "Logging AttributeChangeListener";

    /**
     * {@inheritDoc }
     */
    @Override
    public void processAttributeChange( AttributeChangeType changeType, Identity identity, AttributeStatus attributeStatus, RequestAuthor author,
            String clientCode )
    {
        final AttributeChange change = this.buildAttributeChange( changeType, identity, attributeStatus, author, clientCode );
        final String sbLog = "Change for identity '" + "' [ID:" + change.getIdIdentity( ) + "] " + " by " + " [ID:" + change.getAuthorName( ) + "] "
                + " via application : '" + change.getClientCode( ) + "' on " + change.getModificationDate( ) + " Key changed : '" + change.getAttributeKey( )
                + "' New value : '" + change.getAttributeValue( ) + "' With status : '" + change.getChangeSatus( ) + " - " + change.getChangeSatus( )
                + "' With MetaData : '" + change.getMetadata( ) + " - " + change.getChangeMessage( ) + "'";

        AppLogService.debug( sbLog );
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

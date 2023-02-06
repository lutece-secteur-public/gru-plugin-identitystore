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
package fr.paris.lutece.plugins.identitystore.service.listeners;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.service.AttributeChangeListener;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.log4j.Logger;

/**
 * Logging Listener
 */
public class LoggingListener implements AttributeChangeListener
{
    private static final String SERVICE_NAME = "Logging AttributeChangeListener";
    private static final String PROPERTY_LOGGER_NAME = "identitystore.changelistener.logging.loggerName";
    private static final String DEFAULT_LOGGER_NAME = "lutece.identitystore";
    private static final String LOGGER_NAME = AppPropertiesService.getProperty( PROPERTY_LOGGER_NAME, DEFAULT_LOGGER_NAME );
    private static Logger _logger = Logger.getLogger( LOGGER_NAME );

    /**
     * {@inheritDoc }
     */
    @Override
    public void processAttributeChange( AttributeChange change )
    {
        StringBuilder sbLog = new StringBuilder( );
        sbLog.append( "Change for identity '" ).append( "' [ID:" ).append( change.getIdIdentity( ) ).append( "] " ).append( " by " ).append( " [ID:" )
                .append( change.getAuthorName( ) ).append( "] " ).append( " via application : '" ).append( change.getClientCode( ) ).append( "' on " )
                .append( change.getModificationDate( ) ).append( " Key changed : '" ).append( change.getAttributeKey( ) ).append( "' New value : '" )
                .append( change.getAttributeValue( ) ).append( "' With status : '" ).append( change.getChangeSatus( ) ).append( " - " )
                .append( change.getChangeMessage( ) ).append( "'" );

        _logger.info( sbLog.toString( ) );
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}

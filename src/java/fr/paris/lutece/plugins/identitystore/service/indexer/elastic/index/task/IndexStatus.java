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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task;

import fr.paris.lutece.portal.service.progressmanager.ProgressManagerService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

public class IndexStatus implements Serializable
{
    protected final ProgressManagerService progressManagerService = ProgressManagerService.getInstance( );
    protected String _strFeedToken;
    protected StringBuilder _sbLogs;
    protected final AtomicBoolean _bIsRunning = new AtomicBoolean( );

    public String getLogs( )
    {
        return _sbLogs != null ? _sbLogs.toString( ) : "";
    }

    public void resetLogs( )
    {
        this._sbLogs = new StringBuilder( );
    }

    public boolean isRunning( )
    {
        return this._bIsRunning.get( );
    }

    public void setRunning( final boolean running )
    {
        this._bIsRunning.set( running );
    }

    public String getFeedToken( )
    {
        return _strFeedToken;
    }

    public void setFeedToken( String _strFeedToken )
    {
        this._strFeedToken = _strFeedToken;
    }

    protected void log( final String message )
    {
        if ( this._sbLogs == null )
        {
            this._sbLogs = new StringBuilder( );
        }
        this._sbLogs.append( message ).append( "\n" );
        if ( this._strFeedToken != null )
        {
            this.progressManagerService.addReport( _strFeedToken, message );
        }
        AppLogService.debug( message );
    }

    public void initFeed( int size )
    {
        if ( this.getFeedToken( ) != null )
        {
            this.progressManagerService.initFeed( this.getFeedToken( ), size );
        }
    }

    public void incrementCurrentNbIndexedIdentities( int size )
    {
        if ( this.getFeedToken( ) != null )
        {
            this.progressManagerService.incrementSuccess( this.getFeedToken( ), size );
        }
    }
}

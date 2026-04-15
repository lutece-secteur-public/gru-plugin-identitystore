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
package fr.paris.lutece.plugins.identitystore.service.daemon;

import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.ExcludedIdentities;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentity;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.PurgeIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Arrays;
import java.util.List;

/**
 * This daemon is used to purge expired {@link Identity}.<br/>
 * Those identities are the ones that reached or exceeded their expiration date, and that are also <strong>NOT</strong> :
 * <ul>
 * <li>Associated with a <i>MonParis</i> account</li>
 * <li>Merged to a primary {@link Identity}</li>
 * </ul>
 * For each of those expired identities, their GRU notifications are fetched :
 * <ul>
 * <li>If there are notifications, a new expiration date for the identity is calculated starting from the date of the last notification.</li>
 * <li>If the new expiration date is after the current date, the treatment for this identity is stopped.</li>
 * <li>If the new expiration date is still before the current date, the identity is purged.</li>
 * </ul>
 * The purge consist of, for the identity and all of its potential merged identities :
 * <ul>
 * <li>Delete the {@link Identity} object</li>
 * <li>Delete the {@link IdentityAttribute} objetcs</li>
 * <li>Delete the IdentityAttributeHistory objects</li>
 * <li>Delete the {@link SuspiciousIdentity} objects</li>
 * <li>Delete the {@link ExcludedIdentities} objects</li>
 * </ul>
 * The identity's history is kept.
 */
public class PurgeIdentityDaemon extends LoggingDaemon
{
    private final String authorName = AppPropertiesService.getProperty( "daemon.purgeIdentityDaemon.author.name" );
    private final String clientCode = AppPropertiesService.getProperty( "daemon.purgeIdentityDaemon.client.code" );
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void doTask( )
    {
        final StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        this.info( PurgeIdentityService.getInstance( ).purge( buildAuthor( stopWatch.getTime( ) ), clientCode ) );
        stopWatch.stop( );
        final String execTime = "Execution time " + DurationFormatUtils.formatDurationWords( stopWatch.getTime( ), true, true );
        this.info( execTime );
    }

    private RequestAuthor buildAuthor( final long time )
    {
        final RequestAuthor author = new RequestAuthor( );
        author.setType( AuthorType.application );
        author.setName( authorName + DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format( time ) );
        return author;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service.network;

import fr.paris.lutece.plugins.identitystore.utils.LoggingTask;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * This generic class is a retry pattern aimed to call method that could fail or need to be slow down.<br>
 * Properties:
 * <ul>
 *     <li>Default max retries is set to 500 but can be overridden by the user through <i>identitystore.network.retry.max</i> property.</li>
 *     <li>Default delay is set to 10ms but can be overridden by the user through <i>identitystore.network.delay</i> property. This delay is always used in retries</li>
 *     <li>Default delay activated is set to false but can be overridden by the user through <i>identitystore.network.delay.activated</i> property. When set to true, the delay is applied after each call.</li>
 * </ul>
 * @param <T> the return type of the response to be handled
 */
public class DelayedNetworkService<T>
{
    protected final int NETWORK_MAX_RETRY = AppPropertiesService.getPropertyInt( "identitystore.network.retry.max", 500 );
    protected final int NETWORK_DELAY = AppPropertiesService.getPropertyInt( "identitystore.network.delay", 10 );
    protected final boolean NETWORK_DELAY_ACTIVATED = AppPropertiesService.getPropertyBoolean( "identitystore.network.delay.activate", false );
    protected int nbRetry;

    public T call( final NetworkSupplier<T> method, final String serviceName ) throws IdentityStoreException
    {
        return this.call( method, serviceName, null );
    }

    public T call( final NetworkSupplier<T> method, final String serviceName, final LoggingTask logger) throws IdentityStoreException
    {
        try
        {
            if( NETWORK_DELAY_ACTIVATED )
            {
                //this.log( LogLevel.debug,serviceName + " - Delayed network service activated with value of " + NETWORK_DELAY + " ms", logger);
                try
                {
                    Thread.sleep( NETWORK_DELAY );
                }
                catch( final InterruptedException e )
                {
                    this.log( LogLevel.error, "Could thread sleep.. + " + e.getMessage( ), logger);
                }
            }
            return method.apply();
        }
        catch ( final Exception e )
        {
            return this.retry( method, serviceName, logger);
        }
    }

    private T retry( final NetworkSupplier<T> method, final String serviceName, final LoggingTask logger ) throws RuntimeException, IdentityStoreException
    {
        this.log( LogLevel.error,serviceName + " failed, will be retried " + NETWORK_MAX_RETRY + " times.", logger );
        nbRetry = 0;
        while ( nbRetry < NETWORK_MAX_RETRY )
        {
            try
            {
                Thread.sleep( NETWORK_DELAY );
                return method.apply();
            }
            catch ( final Exception e )
            {
                nbRetry++;
                //this.log( LogLevel.error, "Retry nb " + nbRetry + "/" + NETWORK_MAX_RETRY + " caused by: " + e.getMessage(), logger );
                if ( nbRetry >= NETWORK_MAX_RETRY )
                {
                    this.log( LogLevel.error, "The number of retries exceeds the configured value of " + NETWORK_MAX_RETRY + ", interrupting..", logger );
                    break;
                }
            }
        }
        throw new IdentityStoreException( "Could not process " + serviceName + " The number of retries exceeds the configured value of " + NETWORK_MAX_RETRY );
    }

    private void log( final LogLevel level, final String message, final LoggingTask logger )
    {
        if( logger != null )
        {
            switch ( level )
            {
                case error:
                    logger.error( message );
                    break;
                case info:
                    logger.info( message );
                    break;
                case debug:
                    logger.debug( message );
                    break;
                default:
                    break;
            }
        }
        else
        {
            switch ( level )
            {
                case error:
                    AppLogService.error( message );
                    break;
                case info:
                    AppLogService.info( message );
                    break;
                case debug:
                    AppLogService.debug( message );
                    break;
                default:
                    break;
            }
        }
    }

    public int getNbRetry()
    {
        return nbRetry;
    }

    private enum LogLevel
    {
        error, info, debug;
    }
}

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
package fr.paris.lutece.plugins.identitystore.service;

import static fr.paris.lutece.plugins.identitystore.service.identity.IdentityService.UPDATE_IDENTITY_EVENT_CODE;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.grubusiness.business.demand.DemandType;
import fr.paris.lutece.plugins.grubusiness.business.web.rs.DemandDisplay;
import fr.paris.lutece.plugins.grubusiness.business.web.rs.DemandResult;
import fr.paris.lutece.plugins.grubusiness.business.web.rs.EnumGenericStatus;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.service.listeners.IdentityStoreNotifyListenerService;
import fr.paris.lutece.plugins.identitystore.service.user.InternalUserService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.notificationstore.v1.web.service.NotificationStoreService;
import fr.paris.lutece.portal.service.security.AccessLogService;
import fr.paris.lutece.portal.service.security.AccessLoggerConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public final class PurgeIdentityService
{
    private static PurgeIdentityService _instance;

    private final NotificationStoreService _notificationStoreService = SpringContextService.getBean( "notificationStore.notificationStoreService" );
   
    public static PurgeIdentityService getInstance( )
    {
        if ( _instance == null )
        {
            _instance = new PurgeIdentityService( );
        }
        return _instance;
    }

    /**
     * purge identities
     *
     * @return log {@link StringBuilder}
     */
    public String purge( final RequestAuthor daemonAuthor, final String daemonClientCode, final List<String> excludedAppCodes, final int batchLimit )
    {
        final StringBuilder msg = new StringBuilder( );

        // search identities with a passed peremption date, not merged to a primary identity, and not associated to a MonParis account
        final List<Identity> expiredIdentities = IdentityHome.findExpiredNotMergedAndNotConnectedIdentities( 
        	batchLimit, AppPropertiesService.getPropertyBoolean ( "daemon.purgeIdentityDaemon.withGuidOnly", true) );
        final Timestamp now = Timestamp.from( Instant.now( ) );

        msg.append( expiredIdentities.size( ) ).append( " expired identities found" ).append( "\n" );

        for ( final Identity expiredIdentity : expiredIdentities )
        {
            try
            {
                final List<Identity> mergedIdentities = IdentityHome.findMergedIdentities( expiredIdentity.getId( ) );
                // - check if exists recent Demands associated to each identity or its merged ones
                // >> if true, calculate the new expiration date (date of demand last update + CGUs term)
                DemandResult demandResult = _notificationStoreService.getListDemand( expiredIdentity.getCustomerId( ), null, null, null, null );
                final List<DemandDisplay> demandDisplayList = demandResult.getListDemandDisplay() == null ? new ArrayList<>() : new ArrayList<>(demandResult.getListDemandDisplay());
                for ( final Identity mergedIdentity : mergedIdentities )
                {
                    demandDisplayList
                            .addAll( _notificationStoreService.getListDemand( mergedIdentity.getCustomerId( ), null, null, null, null ).getListDemandDisplay( ) );
                }

                Timestamp demandExpirationDateMAX = expiredIdentity.getExpirationDate( );
                for ( final DemandDisplay demand : demandDisplayList )
                {
                    if (demand.getDemand().getStatusId() == EnumGenericStatus.CANCELED.getStatusId()) {
                        continue;
                    }
                    final String appCode = getAppCodeFromDemandTypeId( demand.getDemand( ).getTypeId( ) );
                    if ( !excludedAppCodes.contains( appCode ) )
                    {
                        final List<String> clientCodeList = ServiceContractService.instance( ).getClientCodesFromAppCode( appCode );

                        int nbMonthsCGUsMAX = 0;
                        for ( final String clientCode : clientCodeList )
                        {
                            // if there is more than one client code for the app_code, keep the max value of cgus
                            int nbMonthsCGUs = ServiceContractService.instance( ).getDataRetentionPeriodInMonths( clientCode );
                            if ( nbMonthsCGUs > nbMonthsCGUsMAX )
                            {
                                nbMonthsCGUsMAX = nbMonthsCGUs;
                            }
                        }

                        final ZonedDateTime demandDate = ZonedDateTime.ofInstant( Instant.ofEpochMilli( demand.getDemand( ).getModifyDate( ) ),
                                ZoneId.systemDefault( ) );
                        final Timestamp expirationDateFromDemand = Timestamp.from( demandDate.plusMonths( nbMonthsCGUsMAX ).toInstant( ) );

                        // keep the max expiration date
                        if ( demandExpirationDateMAX.before( expirationDateFromDemand ) )
                        {
                            demandExpirationDateMAX = expirationDateFromDemand;
                        }
                    }
                }

                // check if expiredIdentity should be preserved or can be deleted
                if ( demandExpirationDateMAX.after( now ) )
                {
                    // expiration date calculated from most recent demand is later than today
                    // => update the expiration date of expiredIdentity : it will be deleted later
                    expiredIdentity.setExpirationDate( demandExpirationDateMAX );
                    IdentityHome.update( expiredIdentity );

                    // re-index and add history
                    IdentityStoreNotifyListenerService.instance( ).notifyListenersIdentityChange( IdentityChangeType.UPDATE, expiredIdentity,
                            "EXPIRATION_POSTPONED", StringUtils.EMPTY, daemonAuthor, daemonClientCode, Collections.emptyMap( ) );
                    AccessLogService.getInstance( ).info( AccessLoggerConstants.EVENT_TYPE_MODIFY, UPDATE_IDENTITY_EVENT_CODE,
                            InternalUserService.getInstance( ).getApiUser( daemonAuthor, daemonClientCode ), null, "PURGE_DAEMON" );

                    msg.append( "Identity expiration date updated : [" ).append( expiredIdentity.getCustomerId( ) ).append( "]" ).append( "\n" );
                }
                else
                {
                    // if the peremption date still passed, delete the identity (and children as merged identities,
                    // suspicious, attributes and attributes history, etc ...) EXCEPT the identity history
                    IdentityService.instance( ).delete( expiredIdentity.getCustomerId( ) );
                    msg.append( "Identity deleted for [" ).append( expiredIdentity.getCustomerId( ) ).append( "]" ).append( "\n" );

                    // delete notifications
                    _notificationStoreService.deleteNotificationByCuid( expiredIdentity.getCustomerId( ) );
                    msg.append( "Notifications deleted for main identity [" ).append( expiredIdentity.getCustomerId( ) ).append( "]" ).append( "\n" );
                    for ( final Identity mergedIdentity : mergedIdentities )
                    {
                        _notificationStoreService.deleteNotificationByCuid( mergedIdentity.getCustomerId( ) );
                        msg.append( "Notifications deleted for merged identity [" ).append( mergedIdentity.getCustomerId( ) ).append( "]" ).append( "\n" );
                    }
                }
            }
            catch( final Exception e )
            {
                msg.append( "Daemon execution error for identity : " ).append( expiredIdentity.getCustomerId( ) ).append( " :: " ).append( e.getMessage( ) )
                        .append( "\n" );
                return msg.toString( );
            }
        }

        // return message for daemons
        return msg.toString( );
    }

    /**
     * get app code
     *
     * @param strTypeId
     *            the type id
     * @return the app code
     */
    private String getAppCodeFromDemandTypeId( final String strTypeId )
    {
        DemandType demandType = _notificationStoreService.getDemandType( strTypeId );

        if ( demandType != null )
        {
            return demandType.getAppCode( );
        }
        
        return null;
    }

}

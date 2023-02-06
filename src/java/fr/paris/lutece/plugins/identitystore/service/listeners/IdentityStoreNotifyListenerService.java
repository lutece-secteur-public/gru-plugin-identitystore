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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeCertificate;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.AttributeChangeListener;
import fr.paris.lutece.plugins.identitystore.service.IdentityChange;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeListener;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChangeType;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 *
 */
public final class IdentityStoreNotifyListenerService
{
    // Beans
    private static final String BEAN_ATTRIBUTE_CHANGE_LISTENERS_LIST = "identitystore.attributes.changelisteners.list";
    private static final String BEAN_IDENTITY_CHANGE_LISTENERS_LIST = "identitystore.identity.changelisteners.list";

    // singleton
    private static IdentityStoreNotifyListenerService _singleton;

    // List
    private static List<AttributeChangeListener> _attributeChangelistListeners;
    private List<IdentityChangeListener> _identityChangeListListeners;

    /**
     * private constructor
     */
    private IdentityStoreNotifyListenerService( )
    {
        // init attributeChangelistListeners
        _attributeChangelistListeners = SpringContextService.getBean( BEAN_ATTRIBUTE_CHANGE_LISTENERS_LIST );

        StringBuilder sbLog = new StringBuilder( );
        sbLog.append( "IdentityStore - loading listeners  : " );

        for ( AttributeChangeListener listener : _attributeChangelistListeners )
        {
            sbLog.append( "\n\t\t\t\t - " ).append( listener.getName( ) );
        }

        AppLogService.info( sbLog.toString( ) );

        // init identityChangeListListeners
        if ( _identityChangeListListeners == null )
        {
            _identityChangeListListeners = SpringContextService.getBean( BEAN_IDENTITY_CHANGE_LISTENERS_LIST );

            sbLog = new StringBuilder( );
            sbLog.append( "IdentityStore - loading listeners  : " );

            for ( IdentityChangeListener listener : _identityChangeListListeners )
            {
                sbLog.append( "\n\t\t\t\t - " ).append( listener.getName( ) );
            }

            AppLogService.info( sbLog.toString( ) );
        }
    }

    /**
     * Returns the unique instance
     * 
     * @return The instance
     */
    public static IdentityStoreNotifyListenerService instance( )
    {
        if ( _singleton == null )
        {
            _singleton = new IdentityStoreNotifyListenerService( );
        }
        return _singleton;
    }

    /**
     * Notify an attribute change to all registered listeners
     *
     * @param change
     *            The change
     */
    public void notifyListenersAttributeChange( AttributeChange change )
    {
        for ( AttributeChangeListener listener : _attributeChangelistListeners )
        {
            listener.processAttributeChange( change );
        }
    }

    /**
     * Notify an identityChange to all registered listeners
     *
     * @param identityChange
     *            The identityChange
     */
    public void notifyListenersIdentityChange( IdentityChange identityChange )
    {
        for ( IdentityChangeListener listener : _identityChangeListListeners )
        {
            listener.processIdentityChange( identityChange );
        }
    }

    /**
     * create and return an AttributeChange from input params
     * 
     * @param changeType
     * @param identity
     * @param attributeStatus
     * @param author
     * @param clientApplicationCode
     * @return AttributeChange from input params
     */
    public static AttributeChange buildAttributeChange( AttributeChangeType changeType, Identity identity, AttributeStatus attributeStatus,
            RequestAuthor author, String clientApplicationCode )
    {
        final AttributeChange attributeChange = new AttributeChange( );

        attributeChange.setChangeType( changeType );
        attributeChange.setChangeSatus( attributeStatus.getStatus( ).getCode( ) );
        attributeChange.setChangeMessage( attributeStatus.getStatus( ).getMessage( ) );
        attributeChange.setAuthorType( author.getType( ) );
        attributeChange.setAuthorName( author.getName( ) );
        attributeChange.setIdIdentity( identity.getId( ) );
        attributeChange.setAttributeKey( attributeStatus.getKey( ) );
        final IdentityAttribute identityAttribute = identity.getAttributes( ).get( attributeStatus.getKey( ) );
        if ( identityAttribute != null )
        {
            attributeChange.setAttributeValue( identityAttribute.getValue( ) );
            final AttributeCertificate certificate = identityAttribute.getCertificate( );
            if ( certificate != null )
            {
                attributeChange.setCertificationProcessus( certificate.getCertifierCode( ) );
                attributeChange.setCertificationDate( certificate.getCertificateDate( ) );
            }
        }
        attributeChange.setClientCode( clientApplicationCode );
        attributeChange.setModificationDate( new Timestamp( new Date( ).getTime( ) ) );

        return attributeChange;
    }
}

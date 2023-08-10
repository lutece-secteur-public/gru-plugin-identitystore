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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.listener;

import fr.paris.lutece.plugins.identitystore.service.IdentityChangeListener;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.stream.Collectors;

public class IdentityIndexListener implements IdentityChangeListener
{

    private static final String SERVICE_NAME = "Elastic Search identity change listener";
    private static final String PROPERTY_LOGGER_NAME = "identitystore.changelistener.logging.loggerName";
    private static final String DEFAULT_LOGGER_NAME = "lutece.identitystore";
    private static final String LOGGER_NAME = AppPropertiesService.getProperty( PROPERTY_LOGGER_NAME, DEFAULT_LOGGER_NAME );
    private static final Logger _logger = Logger.getLogger( LOGGER_NAME );

    private final IIdentityIndexer _identityIndexer;

    public IdentityIndexListener( IIdentityIndexer _identityIndexer )
    {
        this._identityIndexer = _identityIndexer;
    }

    @Override
    public void processIdentityChange( IdentityChange identityChange )
    {
        if ( identityChange instanceof IndexIdentityChange )
        {
            final IndexIdentityChange localIdentityChange = (IndexIdentityChange) identityChange;
            if ( localIdentityChange.getCustomerId( ) != null && localIdentityChange.getChangeType( ) != null && localIdentityChange.getIdentity( ) != null )
            {

                _logger.info( "Indexing identity change (" + localIdentityChange.getChangeType( ).name( ) + ") with customerId = "
                        + localIdentityChange.getCustomerId( ) );
                final Map<String, AttributeObject> attributeObjects = this.mapToIndexObject( localIdentityChange );
                final IdentityObject identityObject = new IdentityObject( localIdentityChange.getConnectionId( ), localIdentityChange.getCustomerId( ),
                        localIdentityChange.getCreationDate( ), localIdentityChange.getLastUpdateDate( ), localIdentityChange.isMonParisActive( ),
                        attributeObjects );

                switch( localIdentityChange.getChangeType( ) )
                {
                    case CREATE:
                    case MERGE_CANCELLED:
                        this._identityIndexer.create( identityObject, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                        break;
                    case UPDATE:
                    case CONSOLIDATED:
                        this._identityIndexer.update( identityObject, IIdentityIndexer.CURRENT_INDEX_ALIAS );
                        break;
                    case DELETE:
                    case MERGED:
                        this._identityIndexer.delete( identityObject.getCustomerId( ), IIdentityIndexer.CURRENT_INDEX_ALIAS );
                        break;
                    default:
                        break;
                }
            }
            else
            {
                _logger.error( "An error occurred during Identity change indexation" );
            }
        }
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }

    private Map<String, AttributeObject> mapToIndexObject( final IndexIdentityChange identity )
    {
        return identity.getIdentity( ).getAttributes( ).values( ).stream( )
                .map( attribute -> new AttributeObject( attribute.getAttributeKey( ).getName( ), attribute.getAttributeKey( ).getKeyName( ),
                        attribute.getAttributeKey( ).getKeyType( ).getCode( ), attribute.getValue( ), attribute.getAttributeKey( ).getDescription( ),
                        attribute.getAttributeKey( ).getPivot( ), attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierCode( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierName( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateDate( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getExpirationDate( ) : null, attribute.getLastUpdateClientCode( ) ) )
                .collect( Collectors.toMap( AttributeObject::getKey, o -> o ) );
    }
}

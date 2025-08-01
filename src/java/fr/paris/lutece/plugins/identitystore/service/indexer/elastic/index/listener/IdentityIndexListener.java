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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.listener;

import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspicionAction;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspicionActionHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.IdentityChangeListener;
import fr.paris.lutece.plugins.identitystore.service.duplicate.IDuplicateService;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Map;
import java.util.stream.Collectors;

public class IdentityIndexListener implements IdentityChangeListener
{
    private static final String CURRENT_INDEX_ALIAS = AppPropertiesService.getProperty( "identitystore.elastic.client.identities.alias", "identities-alias" );
    private static final String SERVICE_NAME = "Elastic Search identity change listener";

    private final IIdentityIndexer _identityIndexer;
    private final IDuplicateService _duplicateServiceElasticSearch;

    public IdentityIndexListener( final IIdentityIndexer _identityIndexer, final IDuplicateService _duplicateServiceElasticSearch )
    {
        this._identityIndexer = _identityIndexer;
        this._duplicateServiceElasticSearch = _duplicateServiceElasticSearch;
    }

    @Override
    public void processIdentityChange( IdentityChangeType identityChangeType, Identity identity, String statusCode, String statusMessage, RequestAuthor author,
            String clientCode, Map<String, String> metadata )
    {
        final Map<String, AttributeObject> attributeObjects = this.mapToIndexObject( identity );
        final IdentityObject identityObject = new IdentityObject( identity.getConnectionId( ), identity.getCustomerId( ), identity.getCreationDate( ),
                identity.getLastUpdateDate( ), identity.getExpirationDate(), identity.isMonParisActive( ), attributeObjects );

        switch( identityChangeType )
        {
            case CREATE:
                this.createSuspicionAction( identityChangeType, identity.getCustomerId() );
            case MERGE_CANCELLED:
                AppLogService.debug( "Indexing identity change (" + identityChangeType.name( ) + ") with customerId = " + identity.getCustomerId( ) );
                this._identityIndexer.create( identityObject, CURRENT_INDEX_ALIAS );
                break;
            case UPDATE:
                this.createSuspicionAction( identityChangeType, identity.getCustomerId() );
            case CONSOLIDATED:
                AppLogService.debug( "Indexing identity change (" + identityChangeType.name( ) + ") with customerId = " + identity.getCustomerId( ) );
                this._identityIndexer.update( identityObject, CURRENT_INDEX_ALIAS );
                break;
            case DELETE:
                this.createSuspicionAction( identityChangeType, identity.getCustomerId() );
            case MERGED:
                AppLogService.debug( "Indexing identity change (" + identityChangeType.name( ) + ") with customerId = " + identity.getCustomerId( ) );
                this._identityIndexer.delete( identityObject.getCustomerId( ), CURRENT_INDEX_ALIAS );
                break;
            case READ:
            case EXCLUDED:
            case EXCLUSION_CANCELLED:
            case MARKED_SUSPICIOUS:
            case CONSOLIDATION_CANCELLED:
            default:
                break;
        }
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }

    private Map<String, AttributeObject> mapToIndexObject( final Identity identity )
    {
        return identity.getAttributes( ).values( ).stream( )
                .map( attribute -> new AttributeObject( attribute.getAttributeKey( ).getName( ), attribute.getAttributeKey( ).getKeyName( ),
                        attribute.getAttributeKey( ).getKeyType( ).getCode( ), attribute.getValue( ), attribute.getAttributeKey( ).getDescription( ),
                        attribute.getAttributeKey( ).getPivot( ), attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierCode( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierName( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateDate( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getExpirationDate( ) : null, attribute.getLastUpdateClientCode( ) ) )
                .collect( Collectors.toMap( AttributeObject::getKey, o -> o ) );
    }

    private void createSuspicionAction( final IdentityChangeType identityChangeType, final String customerId )
    {
        final SuspicionAction action = new SuspicionAction();
        action.setCustomerId( customerId );
        action.setActionType( identityChangeType.name( ) );
        SuspicionActionHome.create( action );
    }
}

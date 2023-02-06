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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.daemon;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.AttributeObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.IdentityObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IndexDaemon extends Daemon
{
    private IIdentityIndexer _identityIndexer = SpringContextService.getBean( IIdentityIndexer.NAME );

    @Override
    public void run( )
    {
        if ( _identityIndexer.isAlive( ) )
        {
            final StringBuilder logs = new StringBuilder( "ES available :: indexing" ).append( "\n" );
            logs.append( "\n" );
            final List<IndexAction> indexActions = IndexActionHome.selectWithLimit( 100 );// TODO à mettre en conf ?
            for ( final IndexAction indexAction : indexActions )
            {
                switch( indexAction.getActionType( ) )
                {
                    case CREATE:
                        final Identity creation = IdentityHome.findByCustomerId( indexAction.getCustomerId( ) );
                        _identityIndexer.create( new IdentityObject( creation.getConnectionId( ), creation.getCustomerId( ), creation.getCreationDate( ),
                                creation.getLastUpdateDate( ), this.mapToIndexObject( creation ) ) );
                        logs.append( "Created document for customer ID " + indexAction.getCustomerId( ) + "\n" );
                        break;
                    case UPDATE:
                        final Identity update = IdentityHome.findByCustomerId( indexAction.getCustomerId( ) );
                        _identityIndexer.update( new IdentityObject( update.getConnectionId( ), update.getCustomerId( ), update.getCreationDate( ),
                                update.getLastUpdateDate( ), this.mapToIndexObject( update ) ) );
                        logs.append( "Updated document for customer ID " + indexAction.getCustomerId( ) + "\n" );
                        break;
                    case DELETE:
                        _identityIndexer.delete( indexAction.getCustomerId( ) );
                        logs.append( "Deleted document for customer ID " + indexAction.getCustomerId( ) + "\n" );
                        break;
                    default:
                        break;
                }
                IndexActionHome.delete( indexAction );
                this.setLastRunLogs( logs.toString( ) );
            }
        }
        else
        {
            this.setLastRunLogs( "[ERROR] ES not available" );
        }
    }

    private Map<String, AttributeObject> mapToIndexObject( final Identity identity )
    {
        return identity.getAttributes( ).values( ).stream( )
                .map( attribute -> new AttributeObject( attribute.getAttributeKey( ).getName( ), attribute.getAttributeKey( ).getKeyName( ),
                        attribute.getAttributeKey( ).getKeyType( ).getCode( ), attribute.getValue( ), attribute.getAttributeKey( ).getDescription( ),
                        attribute.getAttributeKey( ).getPivot( ), attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierCode( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertifierName( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateDate( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getCertificateLevel( ) : null,
                        attribute.getCertificate( ) != null ? attribute.getCertificate( ).getExpirationDate( ) : null,
                        attribute.getLastUpdateApplicationCode( ) ) )
                .collect( Collectors.toMap( AttributeObject::getKey, o -> o ) );
    }
}

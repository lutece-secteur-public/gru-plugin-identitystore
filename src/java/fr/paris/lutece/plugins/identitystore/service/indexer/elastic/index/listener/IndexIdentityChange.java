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

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;

public class IndexIdentityChange extends IdentityChange
{
    protected Identity identity;

    public IndexIdentityChange( final IdentityChange identityChange, final Identity identity )
    {
        this.setId( identityChange.getId( ) );
        this.setChangeMessage( identityChange.getChangeMessage( ) );
        this.setChangeType( identityChange.getChangeType( ) );
        this.setChangeStatus( identityChange.getChangeStatus( ) );
        this.setAuthor( identityChange.getAuthor( ) );
        this.setClientCode( identityChange.getClientCode( ) );
        this.setCustomerId( identityChange.getCustomerId( ) );
        this.setConnectionId( identity.getConnectionId( ) );
        this.setCreationDate( identityChange.getCreationDate( ) );
        this.setLastUpdateDate( identityChange.getLastUpdateDate( ) );
        this.setModificationDate( identityChange.getModificationDate( ) );
        this.setMonParisActive( identityChange.isMonParisActive( ) );
        this.setDeleted( identityChange.isDeleted( ) );
        this.setExpirationDate( identityChange.getExpirationDate( ) );
        this.setMasterCustomerId( identityChange.getMasterCustomerId( ) );
        this.setMerged( identityChange.isMerged( ) );
        this.setMergeDate( identityChange.getMergeDate( ) );
        this.getMetadata( ).putAll( identityChange.getMetadata( ) );
        this.setIdentity( identity );
    }

    public Identity getIdentity( )
    {
        return identity;
    }

    public void setIdentity( Identity identity )
    {
        this.identity = identity;
    }
}

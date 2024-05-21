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
package fr.paris.lutece.plugins.identitystore.v3.web.request.identity;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.UncertifyIdentityRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import java.util.ArrayList;
import java.util.List;

public class IdentityStoreUncertifyRequest extends AbstractIdentityStoreRequest
{

    private final String _strCustomerId;
    private final UncertifyIdentityRequest request;

    public IdentityStoreUncertifyRequest(final UncertifyIdentityRequest request, final String strClientCode, final String strCustomerId, String strAuthorName, String strAuthorType)
            throws IdentityStoreException
    {
        super( strClientCode, strAuthorName, strAuthorType );
        this._strCustomerId = strCustomerId;
        this.request = request;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkCustomerId( _strCustomerId );
    }

    @Override
    protected IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        if ( !ServiceContractService.instance( ).canUncertifyIdentity( _strClientCode ) )
        {
            final IdentityChangeResponse response = new IdentityChangeResponse( );
            response.setStatus( ResponseStatusFactory.failure( ).setMessage( "Unauthorized operation." )
                    .setMessageKey( Constants.PROPERTY_REST_ERROR_UNAUTHORIZED_OPERATION ) );
            return response;
        }
        final List<AttributeKey> attributeKeys = new ArrayList<>( );
        if(request != null && !request.getAttributeKeyList().isEmpty()) {
            final List<AttributeStatus> statusList = new ArrayList<>();
            for( final String key : request.getAttributeKeyList( ) ) {
                final AttributeKey attributeKey = IdentityAttributeService.instance().getAttributeKey(key);
                if (attributeKey == null) {
                    final AttributeStatus attributeStatus = new AttributeStatus( );
                    attributeStatus.setKey( key );
                    attributeStatus.setStatus( AttributeChangeStatus.NOT_FOUND );
                    statusList.add( attributeStatus );
                }else{
                    attributeKeys.add( attributeKey );
                }
            }
            if (!statusList.isEmpty()) {
                final IdentityChangeResponse response = new IdentityChangeResponse();
                response.setStatus(ResponseStatusFactory.notFound().setMessage("Unknown attribute key.")
                                                        .setMessageKey(Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY)
                                                        .setAttributeStatuses(statusList));
                return response;
            }
        }

        return IdentityService.instance( ).uncertifyIdentity( _strCustomerId, attributeKeys, _strClientCode, _author );
    }
}

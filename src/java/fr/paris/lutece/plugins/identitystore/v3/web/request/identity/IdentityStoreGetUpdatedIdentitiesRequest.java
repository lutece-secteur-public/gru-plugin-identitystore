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

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.Page;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.UpdatedIdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.UpdatedIdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.UpdatedIdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.List;

/**
 * This class represents a get request for IdentityStoreRestServive
 *
 */
public class IdentityStoreGetUpdatedIdentitiesRequest extends AbstractIdentityStoreRequest
{
    private final UpdatedIdentitySearchRequest _request;

    /**
     * Constructor of IdentityStoreGetRequest
     *
     * @param updatedIdentitySearchRequest
     *            the request
     * @param strClientCode
     *            the client application code
     * @param strAuthorType
     * @param strAuthorName
     */
    public IdentityStoreGetUpdatedIdentitiesRequest( final UpdatedIdentitySearchRequest updatedIdentitySearchRequest, final String strClientCode,
            final String strAuthorName, final String strAuthorType ) throws IdentityStoreException
    {
        super( strClientCode, strAuthorName, strAuthorType );
        this._request = updatedIdentitySearchRequest;
    }

    @Override
    protected void validateSpecificRequest( ) throws IdentityStoreException
    {
        IdentityRequestValidator.instance( ).checkUpdatedIdentitySearchRequest( this._request );
    }

    /**
     * get the identity
     * 
     * @throws AppException
     *             if there is an exception during the treatment
     */
    @Override
    public UpdatedIdentitySearchResponse doSpecificRequest( )
    {
        final UpdatedIdentitySearchResponse response = new UpdatedIdentitySearchResponse( );
        final int maxFromProperty = AppPropertiesService.getPropertyInt( "identitystore.identity.updated.size.limit", 500 );
        final int maxResult = _request.getMax( ) == null ? maxFromProperty : Math.min( _request.getMax( ), maxFromProperty );
        final List<UpdatedIdentityDto> updatedIdentities;
        if ( _request.getPage( ) != null && _request.getSize( ) != null )
        {
            // Si pagination
            if ( _request.getPage( ) < 1 )
            {
                response.setStatus( ResponseStatusFactory.badRequest( ).setMessage( "Pagination should start at index 1" )
                        .setMessageKey( Constants.PROPERTY_REST_PAGINATION_START_ERROR ) );
                return response;
            }
            if ( _request.getSize( ) < 1 )
            {
                response.setStatus( ResponseStatusFactory.badRequest( ).setMessage( "Page size should be of at least 1" )
                        .setMessageKey( Constants.PROPERTY_REST_PAGE_SIZE_ERROR ) );
                return response;
            }

            // première requête qui ne ramène que les ID (avec un LIMIT ${maxResult} ), triés par date de derniere modification
            final List<Integer> allUpdatedIdentityIds = IdentityHome.findUpdatedIdentityIds( _request.getDays( ), _request.getIdentityChangeTypes( ),
                    _request.getUpdatedAttributes( ), maxResult );

            final int totalRecords = allUpdatedIdentityIds.size( );
            if ( totalRecords == 0 )
            {
                response.setStatus( ResponseStatusFactory.noResult( ).setMessage( "No updated identity found with the provided criterias." )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_NO_UPDATED_IDENTITY_FOUND ) );
                return response;
            }
            final int totalPages = (int) Math.ceil( (double) totalRecords / _request.getSize( ) );
            if ( _request.getPage( ) > totalPages )
            {
                response.setStatus( ResponseStatusFactory.badRequest( ).setMessage( "Pagination index should not exceed total number of pages." )
                        .setMessageKey( Constants.PROPERTY_REST_PAGINATION_END_ERROR ) );
                return response;
            }

            final Page pagination = new Page( );
            pagination.setTotalPages( totalPages );
            pagination.setTotalRecords( totalRecords );
            pagination.setCurrentPage( _request.getPage( ) );
            pagination.setNextPage( _request.getPage( ) == totalPages ? null : _request.getPage( ) + 1 );
            pagination.setPreviousPage( _request.getPage( ) > 1 ? _request.getPage( ) - 1 : null );
            response.setPagination( pagination );

            // deuxième requête qui prend les IDs correspondant à la page demandée (sublist), et qui va chercher les datas
            final int start = ( _request.getPage( ) - 1 ) * _request.getSize( );
            final int end = Math.min( start + _request.getSize( ), totalRecords );
            updatedIdentities = IdentityHome.getUpdatedIdentitiesFromIds( allUpdatedIdentityIds.subList( start, end ) );
        }
        else
        {
            // Pas de pagination demandée, une seule requête qui ramène directement les datas (avec un LIMIT ${maxResult} )
            updatedIdentities = IdentityHome.findUpdatedIdentities( _request.getDays( ), _request.getIdentityChangeTypes( ), _request.getUpdatedAttributes( ),
                    maxResult );
            if ( updatedIdentities.isEmpty( ) )
            {
                response.setStatus( ResponseStatusFactory.noResult( ).setMessage( "No updated identity found with the provided criterias." )
                        .setMessageKey( Constants.PROPERTY_REST_ERROR_NO_UPDATED_IDENTITY_FOUND ) );
                return response;
            }
        }

        response.setStatus( ResponseStatusFactory.ok( ).setMessageKey( Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION ) );
        response.getUpdatedIdentityList( ).addAll( updatedIdentities );

        return response;
    }

}

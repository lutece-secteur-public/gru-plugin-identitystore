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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.IDENTITY_PATH )
public final class IdentityStoreRestService
{
    private static final String ERROR_NO_IDENTITY_FOUND = "No identity found";
    private static final String ERROR_DURING_TREATMENT = "An error occured during the treatment.";

    /**
     * private constructor
     */
    public IdentityStoreRestService( )
    {
    }

    /**
     * Gives Identity from a connectionId or customerID either connectionId or customerId must be provided if connectionId AND customerId are provided, they
     * must be consistent otherwise an AppException is thrown
     *
     * @param strConnectionId
     *            connection ID
     * @param strCustomerId
     *            customerID
     * @param strHeaderClientAppCode
     *            client code
     * @param strQueryClientAppCode
     *            client code, will be removed, use Header parameter instead
     * @return the identity
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getIdentity( @QueryParam( Constants.PARAM_ID_CONNECTION ) String strConnectionId,
            @QueryParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode,
            @QueryParam( Constants.PARAM_CLIENT_CODE ) String strQueryClientAppCode )
    {
        String strClientAppCode = IdentityStoreService.getTrustedApplicationCode( strHeaderClientAppCode, strQueryClientAppCode );
        try
        {
            final IdentityStoreGetRequest identityStoreRequest = new IdentityStoreGetRequest( strConnectionId, strCustomerId, strClientAppCode );
            final IdentitySearchResponse entity = (IdentitySearchResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Searches Identities from a list of values for a series of attributes
     *
     * @param strHeaderClientAppCode
     *            client code
     * @return the identities
     */
    @POST
    @Path( Constants.SEARCH_IDENTITIES_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response searchIdentities( IdentitySearchRequest identitySearchRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode )
    {
        try
        {
            final String strClientAppCode = IdentityStoreService.getTrustedApplicationCode( strHeaderClientAppCode, StringUtils.EMPTY );
            final IdentityStoreSearchRequest identityStoreRequest = new IdentityStoreSearchRequest( identitySearchRequest, strClientAppCode );
            final IdentitySearchResponse entity = (IdentitySearchResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Searches Identities from a list of values for a series of attributes
     *
     * @param strHeaderClientAppCode
     *            client code
     * @return the identities
     */
    @POST
    @Path( "index" )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response fullIndex( @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode )
    {
        try
        {
            IdentityService.instance( ).fullIndexing( );
            return Response.status( Response.Status.OK ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Creates an identity.<br/>
     * The identity is created from the provided attributes in {@link IdentityChangeRequest}.<br/>
     * <br/>
     * The attributes are created if their characteristics match the definition given into the active service contract associated to the client application.
     * <ul>
     * <li>Attribute must be writable</li>
     * <li>Attribute certification processus must be declared</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            the identity creation request
     * @param applicationCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( Constants.CREATE_IDENTITY_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response createIdentity( IdentityChangeRequest identityChangeRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String applicationCode )
    {
        try
        {
            final String trustedApplicationCode = IdentityStoreService.getTrustedApplicationCode( applicationCode, StringUtils.EMPTY );
            final IdentityStoreCreateRequest identityStoreRequest = new IdentityStoreCreateRequest( identityChangeRequest, trustedApplicationCode );
            final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * update identity method
     *
     * @param identityChangeRequest
     *            the identity update request
     * @param applicationCode
     *            the header client app code
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Path( Constants.UPDATE_IDENTITY_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response updateIdentity( IdentityChangeRequest identityChangeRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String applicationCode )
    {
        try
        {
            final String trustedApplicationCode = IdentityStoreService.getTrustedApplicationCode( applicationCode, StringUtils.EMPTY );
            final IdentityStoreUpdateRequest identityStoreRequest = new IdentityStoreUpdateRequest( identityChangeRequest, trustedApplicationCode );
            final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Merge two identities.<br/>
     *
     * @param identityMergeRequest
     *            the identity merge request
     * @param applicationCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( "/merge" )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response mergeIdentities( IdentityMergeRequest identityMergeRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String applicationCode )
    {
        try
        {
            final String trustedApplicationCode = IdentityStoreService.getTrustedApplicationCode( applicationCode, StringUtils.EMPTY );
            final IdentityStoreMergeRequest identityStoreRequest = new IdentityStoreMergeRequest( identityMergeRequest, trustedApplicationCode );
            final IdentityMergeResponse entity = (IdentityMergeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Deletes an identity from the specified connectionId
     *
     * @param strConnectionId
     *            the connection ID
     * @param strHeaderClientAppCode
     *            the client code from header
     * @param strQueryClientAppCode
     *            the client code from query
     * @return a OK message if the deletion has been performed, a KO message otherwise
     */
    @DELETE
    @Produces( MediaType.APPLICATION_JSON )
    public Response deleteIdentity( @QueryParam( Constants.PARAM_ID_CONNECTION ) String strConnectionId,
            @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode, @QueryParam( Constants.PARAM_CLIENT_CODE ) String strQueryClientAppCode )
    {
        String strClientAppCode = IdentityStoreService.getTrustedApplicationCode( strHeaderClientAppCode, strQueryClientAppCode );
        try
        {
            final IdentityStoreDeleteRequest identityStoreRequest = new IdentityStoreDeleteRequest( strConnectionId, strClientAppCode );
            return buildResponse( (String) identityStoreRequest.doRequest( ), Response.Status.OK );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Import an identity from mediation website.<br/>
     * The identity is created from the provided attributes in {@link IdentityChangeRequest}.<br/>
     * <br/>
     * The attributes are created if their characteristics match the definition given into the active service contract associated to the client application.
     * <ul>
     * <li>Attribute must be writable</li>
     * <li>Attribute certification processus must be declared</li>
     * </ul>
     *
     * @param identityChangeRequest
     *            the identity creation request
     * @param applicationCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( "/import" )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response importMediationIdentity( IdentityChangeRequest identityChangeRequest, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String applicationCode )
    {
        try
        {
            final String trustedApplicationCode = IdentityStoreService.getTrustedApplicationCode( applicationCode, StringUtils.EMPTY );
            final IdentityStoreImportRequest identityStoreRequest = new IdentityStoreImportRequest( identityChangeRequest, trustedApplicationCode );
            final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
            return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * build error response from exception
     *
     * @param exception
     *            the exception
     * @return ResponseDto from exception
     */
    private Response getErrorResponse( Exception exception )
    {
        // For security purpose, send a generic message
        String strMessage;
        Response.StatusType status;

        AppLogService.error( "IdentityStoreRestService getErrorResponse : " + exception, exception );

        if ( exception instanceof IdentityNotFoundException )
        {
            strMessage = ERROR_NO_IDENTITY_FOUND;
            status = Response.Status.NOT_FOUND;
        }
        else
        {
            strMessage = ERROR_DURING_TREATMENT + " : " + exception.getMessage( );
            status = Response.Status.BAD_REQUEST;
        }

        return buildResponse( strMessage, status );
    }

    /**
     * Builds a {@code Response} object from the specified message and status
     * 
     * @param strMessage
     *            the message
     * @param status
     *            the status
     * @return the {@code Response} object
     */
    private Response buildResponse( String strMessage, Response.StatusType status )
    {
        final ResponseDto response = new ResponseDto( );
        response.setStatus( status.toString( ) );
        response.setMessage( strMessage );
        return Response.status( status ).type( MediaType.APPLICATION_JSON ).entity( response ).build( );
    }
}

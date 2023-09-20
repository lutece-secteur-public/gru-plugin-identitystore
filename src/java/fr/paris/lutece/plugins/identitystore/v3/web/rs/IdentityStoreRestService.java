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
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreCancelMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreGetUpdatedIdentitiesRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreImportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreSearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreUncertifyRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.UpdatedIdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.swagger.SwaggerConstants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static fr.paris.lutece.plugins.identitystore.v3.web.rs.error.UncaughtIdentityNotFoundExceptionMapper.ERROR_NO_IDENTITY_FOUND;
import static fr.paris.lutece.plugins.rest.service.mapper.GenericUncaughtExceptionMapper.ERROR_DURING_TREATMENT;

/**
 * REST service for channel resource
 *
 */

@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.IDENTITY_PATH )
@Api( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.IDENTITY_PATH )
public final class IdentityStoreRestService
{
    /**
     * private constructor
     */
    public IdentityStoreRestService( )
    {
    }

    /**
     * Gives Identity from a customerID
     *
     * @param strCustomerId
     *            customerID
     * @param strHeaderClientAppCode
     *            client code
     * @return the identity
     */
    @GET
    @Path( "{" + Constants.PARAM_ID_CUSTOMER + "}" )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Get an identity by its customer ID (CUID)", response = IdentitySearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND )
    } )
    public Response getIdentity(
            @ApiParam( name = Constants.PARAM_ID_CUSTOMER, value = "Customer ID of the requested identity" ) @PathParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String strHeaderAuthorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String strHeaderAuthorType )
            throws IdentityStoreException
    {
        final String strClientAppCode = IdentityStoreService.getTrustedClientCode( strHeaderClientAppCode, StringUtils.EMPTY );
        final IdentityStoreGetRequest identityStoreRequest = new IdentityStoreGetRequest( strCustomerId, strClientAppCode, strHeaderAuthorType,
                strHeaderAuthorName );
        final IdentitySearchResponse entity = (IdentitySearchResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
    @ApiOperation( value = "Search an identity by a set of attributes", notes = "", response = IdentitySearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND )
    } )
    public Response searchIdentities(
            @ApiParam( name = "Request body", value = "Identity Search Request", type = "IdentitySearchRequest" ) IdentitySearchRequest identitySearchRequest,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String strClientAppCode = IdentityStoreService.getTrustedClientCode( strHeaderClientAppCode, StringUtils.EMPTY );
        final IdentityStoreSearchRequest identityStoreRequest = new IdentityStoreSearchRequest( identitySearchRequest, strClientAppCode, authorName,
                authorType );
        final IdentitySearchResponse entity = (IdentitySearchResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
     * @param clientCode
     *            the application code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Create a new Identity", notes = "The creation is conditioned by the service contract definition associated to the client application code.", response = IdentityChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ),
            @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response createIdentity( @ApiParam( name = "Request body", value = "An Identity Change Request" ) IdentityChangeRequest identityChangeRequest,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreCreateRequest identityStoreRequest = new IdentityStoreCreateRequest( identityChangeRequest, trustedClientCode, authorName,
                authorType );
        final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * update identity method
     *
     * @param identityChangeRequest
     *            the identity update request
     * @param clientCode
     *            the header client app code
     * @return http 200 if update is ok with ResponseDto
     */
    @PUT
    @Path( "{" + Constants.PARAM_ID_CUSTOMER + "}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Updates an existing Identity", notes = "The update is conditioned by the service contract definition associated to the client application code.", response = IdentityChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ),
            @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response updateIdentity( @ApiParam( name = "Request body", value = "An Identity Change Request" ) IdentityChangeRequest identityChangeRequest,
            @ApiParam( name = Constants.PARAM_ID_CUSTOMER, value = "Customer ID of the updated identity" ) @PathParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreUpdateRequest identityStoreRequest = new IdentityStoreUpdateRequest( strCustomerId, identityChangeRequest, trustedClientCode,
                authorName, authorType );
        final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Merge two identities.<br/>
     *
     * @param identityMergeRequest
     *            the identity merge request
     * @param clientCode
     *            the client code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( Constants.MERGE_IDENTITIES_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Merges two existing Identities", notes = "The merge is conditioned by the service contract definition associated to the client application code.", response = IdentityMergeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND )
    } )
    public Response mergeIdentities( @ApiParam( name = "Request body", value = "An Identity Merge Request" ) IdentityMergeRequest identityMergeRequest,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreMergeRequest identityStoreRequest = new IdentityStoreMergeRequest( identityMergeRequest, trustedClientCode, authorName, authorType );
        final IdentityMergeResponse entity = (IdentityMergeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Cancel the merge of two identities.<br/>
     *
     * @param identityMergeRequest
     *            the identity merge request
     * @param clientCode
     *            the client code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( Constants.CANCEL_MERGE_IDENTITIES_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Merges two existing Identities", notes = "The merge is conditioned by the service contract definition associated to the client application code.", response = IdentityMergeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND )
    } )
    public Response unmergeIdentities( @ApiParam( name = "Request body", value = "An Identity Merge Request" ) IdentityMergeRequest identityMergeRequest,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreCancelMergeRequest identityStoreRequest = new IdentityStoreCancelMergeRequest( identityMergeRequest, trustedClientCode, authorName,
                authorType );
        final IdentityMergeResponse entity = (IdentityMergeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Delete request for an identity from the specified CustomerId The identity will be marked as expired, for the deletion Deamon.
     *
     * @param strCustomerId
     * @param strClientCode
     * @return a OK message if the deletion has been performed, a KO message otherwise
     */
    @DELETE
    @Path( "{" + Constants.PARAM_ID_CUSTOMER + "}" )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Delete request for an existing Identity", notes = "The delete is conditioned by the service contract definition associated to the client application code.", response = IdentityChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ),
            @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response deleteIdentity(
            @ApiParam( name = "Request body", value = "An Identity Change Request to specify the author" ) IdentityChangeRequest identityChangeRequest,
            @ApiParam( name = Constants.PARAM_ID_CUSTOMER, value = "Customer ID of the updated identity" ) @PathParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strClientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final IdentityStoreDeleteRequest identityStoreRequest = new IdentityStoreDeleteRequest( strCustomerId, strClientCode, authorName, authorType );
        final IdentityChangeResponse response = (IdentityChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( response.getStatus( ).getHttpCode( ) ).entity( response ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
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
     * @param clientCode
     *            the client application code in the HTTP header
     * @return http 200 if creation is ok with {@link IdentityChangeResponse}
     */
    @POST
    @Path( "/import" )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Import an Identity", notes = "The import is conditioned by the service contract definition associated to the client application code.", response = IdentityMergeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ),
            @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response importMediationIdentity(
            @ApiParam( name = "Request body", value = "An Identity Change Request" ) IdentityChangeRequest identityChangeRequest,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreImportRequest identityStoreRequest = new IdentityStoreImportRequest( identityChangeRequest, trustedClientCode, authorName,
                authorType );
        final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Gives all modified identity CUIDs within the last given number of days
     *
     * @param strDays
     *            max number of days since the last update
     * @param strHeaderClientAppCode
     *            client code
     * @return the identity list
     */
    @GET
    @Path( Constants.UPDATED_IDENTITIES_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "Gets all modified identity CUIDs within the last given number of days", response = UpdatedIdentitySearchResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 200, message = "Identity Found" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND )
    } )
    public Response getUpdatedIdentities(
            @ApiParam( name = Constants.PARAM_DAYS, value = "max number of days since the last update" ) @QueryParam( Constants.PARAM_DAYS ) String strDays,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String strClientAppCode = IdentityStoreService.getTrustedClientCode( strHeaderClientAppCode, StringUtils.EMPTY );
        final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest( strDays, strClientAppCode, authorName,
                authorType );
        final UpdatedIdentitySearchResponse entity = request.doSpecificRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * uncertify identity method
     *
     * @param clientCode
     *            the header client app code
     * @return http 200 if update is ok with ResponseDto
     */
    @PUT
    @Path( Constants.UNCERTIFY_ATTRIBUTES_PATH + "/" + "{" + Constants.PARAM_ID_CUSTOMER + "}" )
    @Consumes( MediaType.APPLICATION_JSON )
    @ApiOperation( value = "uncertifies all attributes of an existing Identity", notes = "The uncertify process is NOT conditioned by the service contract definition associated to the client application code. All attributes of the identity will be uncertified, regardless of the service contract.", response = IdentityChangeResponse.class )
    @ApiResponses( value = {
            @ApiResponse( code = 201, message = "Success" ), @ApiResponse( code = 400, message = ERROR_DURING_TREATMENT + " with explanation message" ),
            @ApiResponse( code = 403, message = "Failure" ), @ApiResponse( code = 404, message = ERROR_NO_IDENTITY_FOUND ),
            @ApiResponse( code = 409, message = "Conflict" )
    } )
    public Response uncertifyIdentity(
            @ApiParam( name = Constants.PARAM_ID_CUSTOMER, value = "Customer ID of the identity" ) @PathParam( Constants.PARAM_ID_CUSTOMER ) String strCustomerId,
            @ApiParam( name = Constants.PARAM_CLIENT_CODE, value = SwaggerConstants.PARAM_CLIENT_CODE_DESCRIPTION ) @HeaderParam( Constants.PARAM_CLIENT_CODE ) String clientCode,
            @ApiParam( name = Constants.PARAM_AUTHOR_NAME, value = SwaggerConstants.PARAM_AUTHOR_NAME_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_NAME ) String authorName,
            @ApiParam( name = Constants.PARAM_AUTHOR_TYPE, value = SwaggerConstants.PARAM_AUTHOR_TYPE_DESCRIPTION ) @HeaderParam( Constants.PARAM_AUTHOR_TYPE ) String authorType )
            throws IdentityStoreException
    {
        final String trustedClientCode = IdentityStoreService.getTrustedClientCode( clientCode, StringUtils.EMPTY );
        final IdentityStoreUncertifyRequest identityStoreUncertifyRequest = new IdentityStoreUncertifyRequest( trustedClientCode, strCustomerId, authorName,
                authorType );
        final IdentityChangeResponse entity = (IdentityChangeResponse) identityStoreUncertifyRequest.doRequest( );
        return Response.status( entity.getStatus( ).getHttpCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }
}

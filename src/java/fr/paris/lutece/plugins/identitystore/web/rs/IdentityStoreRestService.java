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
package fr.paris.lutece.plugins.identitystore.web.rs;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.v2.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.IDENTITY_PATH )
public final class IdentityStoreRestService
{
    public static final String PARAM_BIRTH_NAME = "birth_name";
    public static final String PARAM_USAGE_NAME = "usage_name";
    public static final String PARAM_FIRST_NAMES = "first_names";
    public static final String PARAM_GENDER = "gender";
    public static final String PARAM_BIRTH_DATE = "birth_date";
    public static final String PARAM_BIRTH_PLACE_LABEL = "birth_place_label";
    public static final String PARAM_BIRTH_PLACE_INSEE_CODE = "birth_place_insee_code";
    public static final String PARAM_BIRTH_ZIP_CODE = "birth_zip_code";
    public static final String PARAM_BIRTH_COUNTRY_LABEL = "birth_coutry_label";
    public static final String PARAM_BIRTH_COUNTRY_INSEE_CODE = "birth_country_insee_code";
    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_MOBILE = "mobile";
    private static Logger _logger = Logger.getLogger( IdentityStoreRestService.class );

    private static final String SLASH = "/";
    private final ObjectMapper _objectMapper;
    private final String _version;
    @Inject
    private fr.paris.lutece.plugins.identitystore.v1.web.rs.IdentityStoreRestService _identityStoreRestServiceV1;
    @Inject
    private fr.paris.lutece.plugins.identitystore.v2.web.rs.IdentityStoreRestService _identityStoreRestServiceV2;
    @Inject
    private fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityStoreRestService _identityStoreRestServiceV3;

    /**
     * private constructor
     */
    public IdentityStoreRestService( )
    {
        _objectMapper = new ObjectMapper( );
        _objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
        _objectMapper.enable( SerializationFeature.WRAP_ROOT_VALUE );
        _objectMapper.enable( DeserializationFeature.UNWRAP_ROOT_VALUE );
        _objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
        _version = SLASH + AppPropertiesService.getProperty( Constants.PROPERTY_APPLICATION_VERSION );
    }

    /**
     * Gives Identity from a connectionId or customerID either connectionId or customerId must be provided if connectionId AND customerId are provided, they
     * must be consistent otherwise an AppException is thrown
     * 
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

        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.getIdentity( strConnectionId, strCustomerId, strHeaderClientAppCode, strQueryClientAppCode );

        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.getIdentity( strConnectionId, strCustomerId, strHeaderClientAppCode, strQueryClientAppCode );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.getIdentity : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * update identity method
     *
     * @param strHeaderApplicationCode
     *            The client app code in header
     * @param formParams
     *            form params, bodypars used for files upload
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Path( Constants.UPDATE_IDENTITY_PATH )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response updateIdentity( @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderApplicationCode, FormDataMultiPart formParams )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.updateIdentity( formParams, strHeaderApplicationCode );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.updateIdentity( formParams, strHeaderApplicationCode );
            }

        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.updateIdentity : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * certify identity attributes
     * 
     * @param strHeaderApplicationCode
     * @param formParams
     * @return 200 ok : 204 no content
     */
    @POST
    @Path( Constants.CERTIFY_ATTRIBUTES_PATH )
    public Response certifyIdentityAttributes( @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderApplicationCode, FormDataMultiPart formParams )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.certifyIdentityAttributes( formParams, strHeaderApplicationCode );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.updateIdentity( formParams, strHeaderApplicationCode );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.certifyIdentityAttributes : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * Creates an identity <b>only if the identity does not already exist</b>.<br/>
     * The identity is created from the provided attributes.<br/>
     * <br/>
     * The order to test if the identity exists:
     * <ul>
     * <li>by using the provided customer id if present</li>
     * <li>by using the provided connection id if present</li>
     * </ul>
     *
     * @param strHeaderApplicationCode
     * @param formParams
     *            form params, bodypars used for files upload
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Path( Constants.CREATE_IDENTITY_PATH )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response createIdentity( @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderApplicationCode, FormDataMultiPart formParams )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.createIdentity( formParams, strHeaderApplicationCode );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.createIdentity( formParams, strHeaderApplicationCode );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.createIdentity : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * Deletes an identity from the specified connectionId
     *
     * @param strConnectionId
     *            the connection ID
     * @param strHeaderClientAppCode
     *            the client code
     * @param strQueryClientAppCode
     *            the client code in query
     * @return a OK message if the deletion has been performed, a KO message otherwise
     */
    @DELETE
    @Produces( MediaType.APPLICATION_JSON )
    public Response deleteIdentity( @QueryParam( Constants.PARAM_ID_CONNECTION ) String strConnectionId,
            @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode, @QueryParam( Constants.PARAM_CLIENT_CODE ) String strQueryClientAppCode )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.deleteIdentity( strConnectionId, strHeaderClientAppCode, strQueryClientAppCode );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.deleteIdentity( strConnectionId, strHeaderClientAppCode, strQueryClientAppCode );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.deleteIdentity : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * returns requested file matching attributeKey / connectionId if application is authorized
     *
     * @param strConnectionId
     *            connectionId (must not be empty)
     * @param strHeaderClientAppCode
     *            client application code (must not be empty)
     * @param strQueryClientAppCode
     *            client application code in query
     * @param strAttributeKey
     *            attribute key containing file (must not be empty)
     * @return http 200 Response containing requested file, http 400 otherwise
     */
    @GET
    @Path( Constants.DOWNLOAD_FILE_PATH )
    public Response downloadFileAttribute( @QueryParam( Constants.PARAM_ID_CONNECTION ) String strConnectionId,
            @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode, @QueryParam( Constants.PARAM_CLIENT_CODE ) String strQueryClientAppCode,
            @QueryParam( Constants.PARAM_ATTRIBUTE_KEY ) String strAttributeKey )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return _identityStoreRestServiceV1.downloadFileAttribute( strConnectionId, strHeaderClientAppCode, strQueryClientAppCode, strAttributeKey );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.downloadFileAttribute( strConnectionId, strHeaderClientAppCode, strQueryClientAppCode, strAttributeKey );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.downloadFileAttribute : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * Gives list of application rights according to its application code, it miss must be consistent otherwise an AppException is thrown
     *
     * @param strClientAppCode
     *            client code from header
     * @return the applications rights
     */
    @GET
    @Path( Constants.APPLICATION_RIGHTS_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getApplicationRights( @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strClientAppCode )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V1 ) )
        {
            return Response.noContent( ).build( );
        }
        else
            if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
            {
                return _identityStoreRestServiceV2.getApplicationRights( strClientAppCode );
            }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.getApplicationRights : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }

    /**
     * Searches Identities from a list of values for a series of attributes
     *
     * @param strHeaderClientAppCode
     *            client code
     * @param strQueryClientAppCode
     *            client code, will be removed, use Header parameter instead
     * @return the identities
     */
    @POST
    @Path( Constants.SEARCH_IDENTITIES_PATH )
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    public Response searchIdentities( String strJsonAttributeValues, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderClientAppCode,
            @QueryParam( Constants.PARAM_CLIENT_CODE ) String strQueryClientAppCode )
    {
        if ( _version.equalsIgnoreCase( Constants.VERSION_PATH_V2 ) )
        {
            return _identityStoreRestServiceV2.searchIdentities( strJsonAttributeValues, strHeaderClientAppCode, strQueryClientAppCode );
        }
        String strError = "IdentityStoreRestService - Error IdentityStoreRestService.searchIdentities : No default version found, please check configuration ";
        _logger.error( strError );
        return Response.noContent( ).build( );
    }
}

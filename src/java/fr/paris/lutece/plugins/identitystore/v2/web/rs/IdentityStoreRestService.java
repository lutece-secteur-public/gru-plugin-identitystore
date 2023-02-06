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
package fr.paris.lutece.plugins.identitystore.v2.web.rs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityNotFoundException;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreAppRightsRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreCreateRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreGetRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreSearchRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.request.IdentityStoreUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.SearchDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFileHome;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import java.util.List;

/**
 * REST service for channel resource
 *
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V2 + Constants.IDENTITY_PATH )
public final class IdentityStoreRestService
{
    private static final String ERROR_NO_IDENTITY_FOUND = "No identity found";
    private static final String ERROR_NO_IDENTITY_TO_UPDATE = "no identity to update";
    private static final String ERROR_DURING_TREATMENT = "An error occured during the treatment.";
    private ObjectMapper _objectMapper;

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
            IdentityStoreGetRequest identityStoreRequest = new IdentityStoreGetRequest( strConnectionId, strCustomerId, strClientAppCode, _objectMapper );

            return Response.ok( identityStoreRequest.doRequest( ) ).build( );
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
        String strClientAppCode = IdentityStoreService.getTrustedApplicationCode( strHeaderClientAppCode, strQueryClientAppCode );
        try
        {
            final ObjectMapper objectMapper = new ObjectMapper( );
            final SearchDto searchDto = objectMapper.readValue( strJsonAttributeValues, SearchDto.class );
            final Map<String, List<String>> mapAttributeValues = searchDto.getMapAttributeValues( );
            final List<String> listAttributeKeyNames = searchDto.getListAttributeKeyNames( );

            // TODO change to just pass service contract id
            IdentityStoreSearchRequest identityStoreRequest = new IdentityStoreSearchRequest( mapAttributeValues, listAttributeKeyNames, 0, strClientAppCode,
                    objectMapper );

            return Response.ok( identityStoreRequest.doRequest( ) ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * update identity method
     *
     * @param formParams
     *            form params, bodypars used for files upload
     * @param strHeaderApplicationCode
     *            the header client app code
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Path( Constants.UPDATE_IDENTITY_PATH )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response updateIdentity( FormDataMultiPart formParams, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderApplicationCode )
    {
        try
        {
            IdentityChangeDto identityChangeDto = fetchIdentityChange( formParams );
            Map<String, File> mapAttachedFiles = fetchAttachedFiles( formParams );

            // Use the trusted application code for identitystore update request processing
            String strApplicationCode = IdentityStoreService.getTrustedApplicationCode( strHeaderApplicationCode, identityChangeDto );
            identityChangeDto.getAuthor( ).setApplicationCode( strApplicationCode );

            IdentityStoreUpdateRequest identityStoreRequest = new IdentityStoreUpdateRequest( identityChangeDto, mapAttachedFiles, _objectMapper );

            return Response.ok( identityStoreRequest.doRequest( ), MediaType.APPLICATION_JSON ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
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
     * @param formParams
     *            form params, bodypars used for files upload
     * @param strHeaderApplicationCode
     *            the application code in the HTTP header
     * @return http 200 if update is ok with ResponseDto
     */
    @POST
    @Path( Constants.CREATE_IDENTITY_PATH )
    @Consumes( MediaType.MULTIPART_FORM_DATA )
    public Response createIdentity( FormDataMultiPart formParams, @HeaderParam( Constants.PARAM_CLIENT_CODE ) String strHeaderApplicationCode )
    {
        try
        {
            IdentityChangeDto identityChangeDto = fetchIdentityChange( formParams );
            Map<String, File> mapAttachedFiles = fetchAttachedFiles( formParams );

            // Use the trusted application code for identitystore update request processing
            String strApplicationCode = IdentityStoreService.getTrustedApplicationCode( strHeaderApplicationCode, identityChangeDto );
            identityChangeDto.getAuthor( ).setApplicationCode( strApplicationCode );

            IdentityStoreCreateRequest identityStoreRequest = new IdentityStoreCreateRequest( identityChangeDto, mapAttachedFiles, _objectMapper );

            return Response.ok( identityStoreRequest.doRequest( ), MediaType.APPLICATION_JSON ).build( );
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
            IdentityStoreDeleteRequest identityStoreRequest = new IdentityStoreDeleteRequest( strConnectionId, strClientAppCode );
            return buildResponse( identityStoreRequest.doRequest( ), Response.Status.OK );
        }
        catch( Exception e )
        {
            return getErrorResponse( e );
        }
    }

    /**
     * returns requested file matching attributeKey / connectionId if application is authorized
     *
     * @param strConnectionId
     *            connectionId (must not be empty)
     * @param strHeaderClientAppCode
     *            the client code from header
     * @param strQueryClientAppCode
     *            the client code from query
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
        String strClientAppCode = IdentityStoreService.getTrustedApplicationCode( strHeaderClientAppCode, strQueryClientAppCode );
        File file = null;

        try
        {
            IdentityRequestValidator.instance( ).checkDownloadFileAttributeParams( strConnectionId, strClientAppCode, strAttributeKey );
            file = getFileAttribute( strConnectionId, strClientAppCode, strAttributeKey );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }

        ResponseBuilder response = Response.ok( (Object) PhysicalFileHome.findByPrimaryKey( file.getPhysicalFile( ).getIdPhysicalFile( ) ).getValue( ) );
        response.header( "Content-Disposition", "attachment; filename=" + file.getTitle( ) );
        response.header( "Content-Type", file.getMimeType( ) );

        return response.build( );
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
        try
        {
            IdentityStoreAppRightsRequest identityStoreAppRightsRequest = new IdentityStoreAppRightsRequest( strClientAppCode, _objectMapper );

            return Response.ok( identityStoreAppRightsRequest.doRequest( ) ).build( );
        }
        catch( Exception exception )
        {
            return getErrorResponse( exception );
        }
    }

    /**
     * Fetches the object {@link IdentityChangeDto} from multi-part data
     *
     * @param formParams
     *            the mutli-part data
     * @return the IdentityChangeDto
     * @throws IOException
     *             if an error occurs during the treatment
     */
    private IdentityChangeDto fetchIdentityChange( FormDataMultiPart formParams ) throws IOException
    {
        IdentityChangeDto identityChangeDto = null;
        String strBody;

        for ( BodyPart part : formParams.getBodyParts( ) )
        {
            InputStream inputStream = part.getEntityAs( InputStream.class );
            ContentDisposition contentDispo = part.getContentDisposition( );

            if ( StringUtils.isBlank( contentDispo.getFileName( ) ) && part.getMediaType( ).isCompatible( MediaType.TEXT_PLAIN_TYPE )
                    && Constants.PARAM_IDENTITY_CHANGE.equals( contentDispo.getParameters( ).get( Constants.PARAMETER_NAME ) ) )
            {
                // content-body of request
                strBody = IOUtils.toString( inputStream, StandardCharsets.UTF_8.toString( ) );

                try
                {
                    identityChangeDto = getIdentityChangeFromJson( strBody );
                }
                catch( IOException e )
                {
                    throw new AppException( "Error parsing json request " + strBody );
                }
            }
        }

        return identityChangeDto;
    }

    /**
     * Fetches the attached files from the specified multi-part data
     *
     * @param formParams
     *            the multi-part data
     * @return the attached files
     * @throws IOException
     *             if an error occurs during the treatment
     */
    private Map<String, File> fetchAttachedFiles( FormDataMultiPart formParams ) throws IOException
    {
        Map<String, File> mapAttachedFiles = new HashMap<String, File>( );

        for ( BodyPart part : formParams.getBodyParts( ) )
        {
            InputStream inputStream = part.getEntityAs( InputStream.class );
            ContentDisposition contentDispo = part.getContentDisposition( );

            if ( StringUtils.isNotBlank( contentDispo.getFileName( ) ) )
            {
                // attachment file
                PhysicalFile physicalFile = new PhysicalFile( );
                physicalFile.setValue( IOUtils.toByteArray( inputStream ) );

                File file = new File( );
                file.setPhysicalFile( physicalFile );
                file.setMimeType( part.getMediaType( ).getType( ) + "/" + part.getMediaType( ).getSubtype( ) );
                file.setSize( physicalFile.getValue( ).length );
                file.setTitle( contentDispo.getFileName( ) );
                mapAttachedFiles.put( contentDispo.getFileName( ), file );
            }
        }

        return mapAttachedFiles;
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
        String strMessage = null;
        Response.StatusType status = null;

        AppLogService.error( "IdentityStoreRestService getErrorResponse : " + exception, exception );

        if ( exception instanceof IdentityNotFoundException )
        {
            strMessage = ERROR_NO_IDENTITY_FOUND;
            status = Response.Status.NOT_FOUND;
        }
        else
        {
            strMessage = ERROR_DURING_TREATMENT;
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
        try
        {
            ResponseDto response = new ResponseDto( );
            response.setStatus( status.toString( ) );
            response.setMessage( strMessage );

            return Response.status( status ).type( MediaType.APPLICATION_JSON ).entity( _objectMapper.writeValueAsString( response ) ).build( );
        }
        catch( JsonProcessingException jpe )
        {
            return Response.status( status ).type( MediaType.TEXT_PLAIN ).entity( strMessage ).build( );
        }
    }

    /**
     * @param strConnectionId
     *            connectionId (must not be empty)
     * @param strClientCode
     *            client application code (must not be empty)
     * @param strAttributeKey
     *            attribute key containing file (must not be empty)
     * @return File matching connectionId/attribute key if readable for client application code
     * @throws AppException
     *             thrown if input parameters are invalid or no file is found
     */
    private File getFileAttribute( String strConnectionId, String strClientCode, String strAttributeKey ) throws AppException
    {
        IdentityAttribute attribute = IdentityStoreService.getAttribute( strConnectionId, strAttributeKey, strClientCode );

        if ( ( attribute == null ) || ( attribute.getFile( ) == null ) )
        {
            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + strAttributeKey + " attribute not found for " + Constants.PARAM_ID_CONNECTION + "="
                    + strConnectionId + "  " + Constants.PARAM_CLIENT_CODE + "=" + strClientCode );
        }

        return attribute.getFile( );
    }

    /**
     * returns IdentityChangeDto from jsonContent
     *
     * @param strJsonContent
     *            json content of request
     * @return IdentityChangeDto parsed identityDto
     * @throws AppException
     *             if request is not correct
     * @throws IOException
     *             if error occurs while parsing json
     * @throws JsonMappingException
     *             if error occurs while parsing json
     * @throws JsonParseException
     *             if error occurs while parsing json
     *
     */
    private IdentityChangeDto getIdentityChangeFromJson( String strJsonContent ) throws AppException, JsonParseException, JsonMappingException, IOException
    {
        IdentityChangeDto identityChangeDto = _objectMapper.readValue( strJsonContent, IdentityChangeDto.class );

        if ( ( identityChangeDto == null ) || ( identityChangeDto.getIdentity( ) == null ) )
        {
            throw new AppException( ERROR_NO_IDENTITY_TO_UPDATE );
        }

        return identityChangeDto;
    }
}

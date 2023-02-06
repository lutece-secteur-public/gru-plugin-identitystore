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

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.swagger.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * ServiceContractRest
 */
@Path( RestConstants.BASE_PATH + Constants.API_PATH + Constants.VERSION_PATH + Constants.SERVICECONTRACT_PATH )
public class ServiceContractRestService
{
    private static final int VERSION_1 = 1;

    /**
     * Get ServiceContract List
     * 
     * @param nVersion
     *            the API version
     * @return the ServiceContract List
     */
    @GET
    @Path( StringUtils.EMPTY )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getServiceContractList( @PathParam( Constants.VERSION ) Integer nVersion )
    {
        if ( nVersion == VERSION_1 )
        {
            return getServiceContractListV1( );
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Get ServiceContract List V1
     * 
     * @return the ServiceContract List for the version 1
     */
    private Response getServiceContractListV1( )
    {
        List<ServiceContract> listServiceContracts = ServiceContractHome.getServiceContractsList( );

        if ( listServiceContracts.isEmpty( ) )
        {
            return Response.status( Response.Status.NO_CONTENT ).entity( JsonUtil.buildJsonResponse( new JsonResponse( Constants.EMPTY_OBJECT ) ) ).build( );
        }
        return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( listServiceContracts ) ) ).build( );
    }

    /**
     * Create ServiceContract
     * 
     * @param nVersion
     *            the API version
     * @param name
     *            the name
     * @param application_code
     *            the application_code
     * @param organizational_entity
     *            the organizational_entity
     * @param responsible_name
     *            the responsible_name
     * @param contact_name
     *            the contact_name
     * @param service_type
     *            the service_type
     * @param authorized_read
     *            the authorized_read
     * @param authorized_deletion
     *            the authorized_deletion
     * @param authorized_search
     *            the authorized_search
     * @param authorized_import
     *            the authorized_import
     * @param authorized_export
     *            the authorized_export
     * @return the ServiceContract if created
     */
    @POST
    @Path( StringUtils.EMPTY )
    @Produces( MediaType.APPLICATION_JSON )
    public Response createServiceContract( @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_NAME ) String name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_APPLICATION_CODE ) String application_code,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_ORGANIZATIONAL_ENTITY ) String organizational_entity,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_RESPONSIBLE_NAME ) String responsible_name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_CONTACT_NAME ) String contact_name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_SERVICE_TYPE ) String service_type,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_READ ) String authorized_read,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_DELETION ) String authorized_deletion,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_SEARCH ) String authorized_search,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_IMPORT ) String authorized_import,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_EXPORT ) String authorized_export, @PathParam( Constants.VERSION ) Integer nVersion )
    {
        if ( nVersion == VERSION_1 )
        {
            return createServiceContractV1( name, application_code, organizational_entity, responsible_name, contact_name, service_type, authorized_read,
                    authorized_deletion, authorized_search, authorized_import, authorized_export );
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Create ServiceContract V1
     * 
     * @param name
     *            the name
     * @param application_code
     *            the application_code
     * @param organizational_entity
     *            the organizational_entity
     * @param responsible_name
     *            the responsible_name
     * @param contact_name
     *            the contact_name
     * @param service_type
     *            the service_type
     * @param authorized_read
     *            the authorized_read
     * @param authorized_deletion
     *            the authorized_deletion
     * @param authorized_search
     *            the authorized_search
     * @param authorized_import
     *            the authorized_import
     * @param authorized_export
     *            the authorized_export
     * @return the ServiceContract if created for the version 1
     */
    private Response createServiceContractV1( String name, String application_code, String organizational_entity, String responsible_name, String contact_name,
            String service_type, String authorized_read, String authorized_deletion, String authorized_search, String authorized_import,
            String authorized_export )
    {
        if ( StringUtils.isEmpty( name ) || StringUtils.isEmpty( application_code ) || StringUtils.isEmpty( organizational_entity )
                || StringUtils.isEmpty( responsible_name ) || StringUtils.isEmpty( contact_name ) || StringUtils.isEmpty( service_type )
                || StringUtils.isEmpty( authorized_read ) || StringUtils.isEmpty( authorized_deletion ) || StringUtils.isEmpty( authorized_search )
                || StringUtils.isEmpty( authorized_import ) || StringUtils.isEmpty( authorized_export ) )
        {
            AppLogService.error( Constants.ERROR_BAD_REQUEST_EMPTY_PARAMETER );
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity( JsonUtil
                            .buildJsonResponse( new ErrorJsonResponse( Response.Status.BAD_REQUEST.name( ), Constants.ERROR_BAD_REQUEST_EMPTY_PARAMETER ) ) )
                    .build( );
        }

        ServiceContract servicecontract = new ServiceContract( );
        servicecontract.setName( name );
        // TODO call client application service to set in the service contract servicecontract.setApplicationCode( application_code );
        servicecontract.setOrganizationalEntity( organizational_entity );
        servicecontract.setResponsibleName( responsible_name );
        servicecontract.setContactName( contact_name );
        servicecontract.setServiceType( service_type );
        servicecontract.setAuthorizedDeletion( Boolean.parseBoolean( authorized_deletion ) );
        servicecontract.setAuthorizedImport( Boolean.parseBoolean( authorized_import ) );
        servicecontract.setAuthorizedExport( Boolean.parseBoolean( authorized_export ) );
        // TODO ServiceContractHome.create( servicecontract );

        return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( servicecontract ) ) ).build( );
    }

    /**
     * Modify ServiceContract
     * 
     * @param nVersion
     *            the API version
     * @param id
     *            the id
     * @param name
     *            the name
     * @param application_code
     *            the application_code
     * @param organizational_entity
     *            the organizational_entity
     * @param responsible_name
     *            the responsible_name
     * @param contact_name
     *            the contact_name
     * @param service_type
     *            the service_type
     * @param authorized_read
     *            the authorized_read
     * @param authorized_deletion
     *            the authorized_deletion
     * @param authorized_search
     *            the authorized_search
     * @param authorized_import
     *            the authorized_import
     * @param authorized_export
     *            the authorized_export
     * @return the ServiceContract if modified
     */
    @PUT
    @Path( Constants.ID_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response modifyServiceContract( @PathParam( Constants.ID ) Integer id, @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_NAME ) String name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_APPLICATION_CODE ) String application_code,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_ORGANIZATIONAL_ENTITY ) String organizational_entity,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_RESPONSIBLE_NAME ) String responsible_name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_CONTACT_NAME ) String contact_name,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_SERVICE_TYPE ) String service_type,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_READ ) String authorized_read,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_DELETION ) String authorized_deletion,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_SEARCH ) String authorized_search,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_IMPORT ) String authorized_import,
            @FormParam( Constants.SERVICECONTRACT_ATTRIBUTE_AUTHORIZED_EXPORT ) String authorized_export, @PathParam( Constants.VERSION ) Integer nVersion )
    {
        if ( nVersion == VERSION_1 )
        {
            return modifyServiceContractV1( id, name, application_code, organizational_entity, responsible_name, contact_name, service_type, authorized_read,
                    authorized_deletion, authorized_search, authorized_import, authorized_export );
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Modify ServiceContract V1
     * 
     * @param id
     *            the id
     * @param name
     *            the name
     * @param application_code
     *            the application_code
     * @param organizational_entity
     *            the organizational_entity
     * @param responsible_name
     *            the responsible_name
     * @param contact_name
     *            the contact_name
     * @param service_type
     *            the service_type
     * @param authorized_read
     *            the authorized_read
     * @param authorized_deletion
     *            the authorized_deletion
     * @param authorized_search
     *            the authorized_search
     * @param authorized_import
     *            the authorized_import
     * @param authorized_export
     *            the authorized_export
     * @return the ServiceContract if modified for the version 1
     */
    private Response modifyServiceContractV1( Integer id, String name, String application_code, String organizational_entity, String responsible_name,
            String contact_name, String service_type, String authorized_read, String authorized_deletion, String authorized_search, String authorized_import,
            String authorized_export )
    {
        if ( StringUtils.isEmpty( name ) || StringUtils.isEmpty( application_code ) || StringUtils.isEmpty( organizational_entity )
                || StringUtils.isEmpty( responsible_name ) || StringUtils.isEmpty( contact_name ) || StringUtils.isEmpty( service_type )
                || StringUtils.isEmpty( authorized_read ) || StringUtils.isEmpty( authorized_deletion ) || StringUtils.isEmpty( authorized_search )
                || StringUtils.isEmpty( authorized_import ) || StringUtils.isEmpty( authorized_export ) )
        {
            AppLogService.error( Constants.ERROR_BAD_REQUEST_EMPTY_PARAMETER );
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity( JsonUtil
                            .buildJsonResponse( new ErrorJsonResponse( Response.Status.BAD_REQUEST.name( ), Constants.ERROR_BAD_REQUEST_EMPTY_PARAMETER ) ) )
                    .build( );
        }

        Optional<ServiceContract> optServiceContract = ServiceContractHome.findByPrimaryKey( id );
        if ( !optServiceContract.isPresent( ) )
        {
            AppLogService.error( Constants.ERROR_NOT_FOUND_RESOURCE );
            return Response.status( Response.Status.NOT_FOUND )
                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_RESOURCE ) ) )
                    .build( );
        }
        else
        {
            ServiceContract servicecontract = optServiceContract.get( );
            servicecontract.setName( name );
            // TODO servicecontract.setApplicationCode( application_code );
            servicecontract.setOrganizationalEntity( organizational_entity );
            servicecontract.setResponsibleName( responsible_name );
            servicecontract.setContactName( contact_name );
            servicecontract.setServiceType( service_type );
            servicecontract.setAuthorizedDeletion( Boolean.parseBoolean( authorized_deletion ) );
            servicecontract.setAuthorizedImport( Boolean.parseBoolean( authorized_import ) );
            servicecontract.setAuthorizedExport( Boolean.parseBoolean( authorized_export ) );
            // TODO ServiceContractHome.update( servicecontract );

            return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( servicecontract ) ) ).build( );
        }
    }

    /**
     * Delete ServiceContract
     * 
     * @param nVersion
     *            the API version
     * @param id
     *            the id
     * @return the ServiceContract List if deleted
     */
    @DELETE
    @Path( Constants.ID_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response deleteServiceContract( @PathParam( Constants.VERSION ) Integer nVersion, @PathParam( Constants.ID ) Integer id )
    {
        if ( nVersion == VERSION_1 )
        {
            return deleteServiceContractV1( id );
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Delete ServiceContract V1
     * 
     * @param id
     *            the id
     * @return the ServiceContract List if deleted for the version 1
     */
    private Response deleteServiceContractV1( Integer id )
    {
        Optional<ServiceContract> optServiceContract = ServiceContractHome.findByPrimaryKey( id );
        if ( !optServiceContract.isPresent( ) )
        {
            AppLogService.error( Constants.ERROR_NOT_FOUND_RESOURCE );
            return Response.status( Response.Status.NOT_FOUND )
                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_RESOURCE ) ) )
                    .build( );
        }

        ServiceContractHome.remove( id );

        return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( Constants.EMPTY_OBJECT ) ) ).build( );
    }

    /**
     * Get ServiceContract
     * 
     * @param nVersion
     *            the API version
     * @param id
     *            the id
     * @return the ServiceContract
     */
    @GET
    @Path( Constants.ID_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getServiceContract( @PathParam( Constants.VERSION ) Integer nVersion, @PathParam( Constants.ID ) Integer id )
    {
        if ( nVersion == VERSION_1 )
        {
            return getServiceContractV1( id );
        }
        AppLogService.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Get ServiceContract V1
     * 
     * @param id
     *            the id
     * @return the ServiceContract for the version 1
     */
    private Response getServiceContractV1( Integer id )
    {
        Optional<ServiceContract> optServiceContract = ServiceContractHome.findByPrimaryKey( id );
        if ( !optServiceContract.isPresent( ) )
        {
            AppLogService.error( Constants.ERROR_NOT_FOUND_RESOURCE );
            return Response.status( Response.Status.NOT_FOUND )
                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_RESOURCE ) ) )
                    .build( );
        }

        return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( optServiceContract.get( ) ) ) ).build( );
    }
}

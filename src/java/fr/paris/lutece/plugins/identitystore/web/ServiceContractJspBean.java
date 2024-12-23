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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessusHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.v3.csv.CsvIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.html.AbstractPaginator;
import fr.paris.lutece.util.url.UrlItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter.convertContractToDto;

/**
 * This class provides the user interface to manage ServiceContract features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageServiceContracts.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_ADMIN_MANAGEMENT" )
public class ServiceContractJspBean extends ManageServiceContractJspBean<Integer, ServiceContract>
{
    // Templates
    private static final String TEMPLATE_MANAGE_SERVICECONTRACTS = "/admin/plugins/identitystore/manage_servicecontracts.html";
    private static final String TEMPLATE_DISPLAY_SERVICECONTRACTS = "/admin/plugins/identitystore/view_servicecontract.html";
    private static final String TEMPLATE_CREATE_SERVICECONTRACT = "/admin/plugins/identitystore/create_servicecontract.html";
    private static final String TEMPLATE_MODIFY_SERVICECONTRACT = "/admin/plugins/identitystore/create_servicecontract.html";

    // Parameters
    private static final String PARAMETER_ID_SERVICECONTRACT = "id";
    private static final String PARAMETER_RIGHT_MANDATORY = "mandatory";
    private static final String PARAMETER_RIGHT_SEARCHABLE = "searchable";
    private static final String PARAMETER_RIGHT_READABLE = "readable";
    private static final String PARAMETER_RIGHT_WRITABLE = "writable";
    private static final String PARAMETER_ID_CLIENTAPPLICATION = "id_client_app";
    private static final String PARAMETER_CODE_CLIENTAPPLICATION = "code_client_app";
    private static final String PARAMETER_ID_PARENTCLIENTAPPLICATION = "parent_application_id";
    private static final String PARAMETER_CERTICATION_LEVEL = "certification_level";
    private static final String PARAMETER_CERTICATION_PROCESSUS = "certification_processus";
    private static final String PARAMETER_BACK_URL = "back_url";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SERVICECONTRACTS = "identitystore.manage_servicecontracts.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_SERVICECONTRACT = "identitystore.modify_servicecontract.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_SERVICECONTRACT = "identitystore.create_servicecontract.pageTitle";

    // Markers
    private static final String MARK_SERVICECONTRACT_LIST = "servicecontract_list";
    private static final String MARK_SERVICECONTRACT = "servicecontract";
    private static final String MARK_ATTRIBUTE_REQUIREMENTS_LIST = "servicecontract_attribute_list";
    private static final String MARK_AVAILAIBLE_LEVELS_LIST = "availaible_certification_levels_list";
    private static final String MARK_AVAILAIBLE_CLIENT_APPLICATIONS_LIST = "availaible_client_applications_list";
    private static final String MARK_EDIT_ACTION = "edit_action";
    private static final String MARK_BACK_URL = "back_url";
    private static final String MARK_MANDATORY_ATTRIBUTE_KEYS_LIST = "mandatory_attribute_keys_list";

    private static final String JSP_MANAGE_SERVICECONTRACTS = "jsp/admin/plugins/identitystore/ManageServiceContracts.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_SERVICECONTRACT = "contractservice.message.confirmRemoveServiceContract";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "contractservice.model.entity.servicecontract.attribute.";

    // Views
    private static final String VIEW_MANAGE_SERVICECONTRACTS = "manageServiceContracts";
    private static final String VIEW_DISPLAY_SERVICECONTRACTS = "displayServiceContract";
    private static final String VIEW_CREATE_SERVICECONTRACT = "createServiceContract";
    private static final String VIEW_MODIFY_SERVICECONTRACT = "modifyServiceContract";

    // Actions
    private static final String ACTION_CREATE_SERVICECONTRACT = "createServiceContract";
    private static final String ACTION_MODIFY_SERVICECONTRACT = "modifyServiceContract";
    private static final String ACTION_REMOVE_SERVICECONTRACT = "removeServiceContract";
    private static final String ACTION_CONFIRM_REMOVE_SERVICECONTRACT = "confirmRemoveServiceContract";
    private static final String ACTION_EXPORT_SERVICECONTRACT = "exportServiceContract";

    // Infos
    private static final String INFO_SERVICECONTRACT_CREATED = "identitystore.info.servicecontract.created";
    private static final String INFO_SERVICECONTRACT_UPDATED = "identitystore.info.servicecontract.updated";
    private static final String INFO_SERVICECONTRACT_REMOVED = "identitystore.info.servicecontract.removed";
    private static final int BATCH_PARTITION_SIZE = AppPropertiesService.getPropertyInt( "identitystore.export.batch.size", 100 );

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    private static final String ERROR_SERVICECONTRACT_NO_DATE = "identitystore.error.servicecontract.nostartingdate";
    private static final String ERROR_SERVICECONTRACT_INVALID_DATE = "identitystore.error.servicecontract.validatedate";
    private static final String ERROR_SERVICECONTRACT_INVALID_MANDATORY_ATTRIBUTE = "identitystore.error.servicecontract.validate.mandatory.attribute";

    // Session variable to store working values
    private ServiceContract _servicecontract;
    private List<Integer> _listIdServiceContracts;

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_SERVICECONTRACTS, defaultView = true )
    public String getManageServiceContracts( HttpServletRequest request )
    {
        _servicecontract = null;
        final Map<String, String> queryParameters = this.getQueryParameters( request );

        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX ) == null || _listIdServiceContracts.isEmpty( ) )
        {
            _listIdServiceContracts = ServiceContractHome.getFilteredIdServiceContractsList( queryParameters );
        }

        final Map<String, Object> model = getPaginatedListModel( request, MARK_SERVICECONTRACT_LIST, _listIdServiceContracts, JSP_MANAGE_SERVICECONTRACTS );
        model.put( QUERY_PARAM_ACTIVE, queryParameters.get(QUERY_PARAM_ACTIVE) );
        model.put( QUERY_PARAM_CONTRACT_NAME, request.getParameter( QUERY_PARAM_CONTRACT_NAME ) );
        model.put( QUERY_PARAM_CLIENT_CODE, request.getParameter( QUERY_PARAM_CLIENT_CODE ) );
        model.put( QUERY_PARAM_START_DATE, request.getParameter( QUERY_PARAM_START_DATE ) );
        model.put( QUERY_PARAM_END_DATE, request.getParameter( QUERY_PARAM_END_DATE ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_SERVICECONTRACTS, TEMPLATE_MANAGE_SERVICECONTRACTS, model );
    }

    /**
     * Build the Manage View
     *
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_DISPLAY_SERVICECONTRACTS )
    public String getDisplayServiceContracts( HttpServletRequest request )
    {
        final int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SERVICECONTRACT ) );
        final String backUrl = request.getParameter( PARAMETER_BACK_URL )
                .concat( ( StringUtils.isNotBlank( request.getParameter( QUERY_PARAM_ACTIVE ) ) ? "&" + QUERY_PARAM_ACTIVE + "=" + request.getParameter( QUERY_PARAM_ACTIVE ) : "" ) )
                .concat( ( StringUtils.isNotBlank( request.getParameter( QUERY_PARAM_CONTRACT_NAME ) ) ? "&" + QUERY_PARAM_CONTRACT_NAME + "=" + request.getParameter( QUERY_PARAM_CONTRACT_NAME ) : "" ) )
                .concat( ( StringUtils.isNotBlank( request.getParameter( QUERY_PARAM_CLIENT_CODE ) ) ? "&" + QUERY_PARAM_CLIENT_CODE + "=" + request.getParameter( QUERY_PARAM_CLIENT_CODE ) : "" ) )
                .concat( ( StringUtils.isNotBlank( request.getParameter( QUERY_PARAM_START_DATE ) ) ? "&" + QUERY_PARAM_START_DATE + "=" + request.getParameter( QUERY_PARAM_START_DATE ) : "" ) )
                .concat( ( StringUtils.isNotBlank( request.getParameter( QUERY_PARAM_END_DATE ) ) ? "&" + QUERY_PARAM_END_DATE + "=" + request.getParameter( QUERY_PARAM_END_DATE ) : "" ) );
        _servicecontract = null;

        final Optional<ServiceContract> optServiceContract = ServiceContractHome.findByPrimaryKey( nId );
        _servicecontract = optServiceContract.orElseThrow( ( ) -> new AppException( ERROR_RESOURCE_NOT_FOUND ) );

        final Map<String, Object> model = getModel( );
        final ClientApplication parentApplication = ClientApplicationHome.getParentApplication( _servicecontract );
        if ( parentApplication != null )
        {
            model.put( PARAMETER_ID_CLIENTAPPLICATION, parentApplication.getId( ) );
            model.put( PARAMETER_CODE_CLIENTAPPLICATION, parentApplication.getClientCode( ) );
        }
        else
        {
            throw new AppException( ERROR_RESOURCE_NOT_FOUND );
        }

        final List<ServiceContractAttributeDefinitionDto> attributeRequirementList = ServiceContractHome.getDto( _servicecontract );
        sortAttributeRequirementList( attributeRequirementList );

        model.put( MARK_BACK_URL, backUrl );
        model.put( MARK_SERVICECONTRACT, _servicecontract );
        model.put( MARK_ATTRIBUTE_REQUIREMENTS_LIST, attributeRequirementList );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_SERVICECONTRACTS, TEMPLATE_DISPLAY_SERVICECONTRACTS, model );
    }

    /**
     * Get Items from Ids list
     *
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
    @Override
    List<ImmutablePair<ServiceContract, String>> getItemsFromIds( final List<Integer> listIds )
    {
        final List<ImmutablePair<ServiceContract, String>> listServiceContract = ServiceContractHome.getServiceContractsListByIds( listIds );

        // #134 - sort by ascending application code and descending start date
        return listServiceContract.stream( ).sorted( Comparator.comparing( ( ImmutablePair<ServiceContract, String> pair ) -> pair.getValue( ) )
                .thenComparing( pair -> pair.getKey( ).getStartingDate( ), Comparator.reverseOrder( ) ) ).collect( Collectors.toList( ) );
    }

    /**
     * reset the _listIdServiceContracts list
     */
    public void resetListId( )
    {
        _listIdServiceContracts = new ArrayList<>( );
    }

    /**
     * Returns the form to create a servicecontract
     *
     * @param request
     *            The Http request
     * @return the html code of the servicecontract form
     */
    @View( VIEW_CREATE_SERVICECONTRACT )
    public String getCreateServiceContract( HttpServletRequest request )
    {
        final Map<String, Object> model = getModel( );

        _servicecontract = new ServiceContract( );
        final String idClientApp = request.getParameter( PARAMETER_ID_CLIENTAPPLICATION );
        if ( StringUtils.isNotEmpty( idClientApp ) )
        {
            final int nIdClientApp = Integer.parseInt( idClientApp );
            model.put( PARAMETER_ID_CLIENTAPPLICATION, nIdClientApp );
        }

        final List<ServiceContractAttributeDefinitionDto> attributeRequirementList = ServiceContractHome.getDto( _servicecontract );
        sortAttributeRequirementList( attributeRequirementList );

        final List<String> mandatoryAttrKeyList = AttributeKeyHome.getMandatoryForCreationAttributeKeyList( ).stream( ).map( AttributeKey::getKeyName )
                .collect( Collectors.toList( ) );

        model.put( MARK_MANDATORY_ATTRIBUTE_KEYS_LIST, mandatoryAttrKeyList );
        model.put( MARK_SERVICECONTRACT, _servicecontract );
        model.put( MARK_EDIT_ACTION, "action_createServiceContract" );
        model.put( MARK_ATTRIBUTE_REQUIREMENTS_LIST, attributeRequirementList );
        model.put( MARK_AVAILAIBLE_LEVELS_LIST, ServiceContractHome.selectCertificationLevels( ).stream( )
                .sorted( Comparator.comparing( RefCertificationLevel::getLevel ) ).collect( Collectors.toList( ) ) );
        model.put( MARK_AVAILAIBLE_CLIENT_APPLICATIONS_LIST, ClientApplicationHome.selectApplicationList( ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_SERVICECONTRACT ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_SERVICECONTRACT, TEMPLATE_CREATE_SERVICECONTRACT, model );
    }

    /**
     * Process the data capture form of a new servicecontract
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_SERVICECONTRACT )
    public String doCreateServiceContract( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _servicecontract, request, getLocale( ) );
        final String [ ] selectedClienAppId = request.getParameterValues( PARAMETER_ID_PARENTCLIENTAPPLICATION );
        final String strSelectedClientAppId = selectedClienAppId [0];
        int selectedClientAppId = Integer.parseInt( strSelectedClientAppId );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_SERVICECONTRACT ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _servicecontract, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_CREATE_SERVICECONTRACT, PARAMETER_ID_CLIENTAPPLICATION, selectedClientAppId );
        }

        if ( _servicecontract.getStartingDate( ).equals( null ) )
        {
            addError( ERROR_SERVICECONTRACT_NO_DATE, getLocale( ) );
            return redirect( request, VIEW_CREATE_SERVICECONTRACT, PARAMETER_ID_CLIENTAPPLICATION, selectedClientAppId );
        }
        else
            if ( !checkServiceContractsActivationDate( _servicecontract, selectedClientAppId ) )
            {
                addError( ERROR_SERVICECONTRACT_INVALID_DATE, getLocale( ) );
                return redirect( request, VIEW_CREATE_SERVICECONTRACT, PARAMETER_ID_CLIENTAPPLICATION, selectedClientAppId );
            }

        final List<AttributeRight> lstAttributeRights = new ArrayList<>( getAttributesRightsFromRequest( request ).values( ) );
        _servicecontract.setAttributeRights( lstAttributeRights );
        final List<AttributeRequirement> attributeRequirements = new ArrayList<>( getAttributesRequirementsFromRequest( request ).values( ) );
        _servicecontract.getAttributeRequirements( )
                .addAll( attributeRequirements.stream( ).filter( attributeRequirement -> attributeRequirement.getRefCertificationLevel( ) != null
                        && attributeRequirement.getRefCertificationLevel( ).getLevel( ) != null ).collect( Collectors.toList( ) ) );
        final List<AttributeCertification> attributeCertifications = new ArrayList<>( getAttributesCertificationsFromRequest( request ) );
        _servicecontract.setAttributeCertifications( attributeCertifications.stream( ).filter( certif -> {
            AttributeRight attributeRight = lstAttributeRights.stream( ).filter( atr -> atr.getAttributeKey( ).getId( ) == certif.getAttributeKey( ).getId( ) )
                    .findFirst( ).orElse( null );
            return attributeRight != null && attributeRight.isWritable( );
        } ).collect( Collectors.toList( ) ) );

        final boolean validMandatoryConfig = _servicecontract.getAttributeRights( ).stream( ).filter( AttributeRight::isMandatory )
                .allMatch( attributeRight -> _servicecontract.getAttributeRequirements( ).stream( )
                        .anyMatch( req -> Objects.equals( req.getAttributeKey( ).getKeyName( ), attributeRight.getAttributeKey( ).getKeyName( ) )
                                && req.getRefCertificationLevel( ) != null && req.getRefCertificationLevel( ).getLevel( ) != null ) );
        if ( !validMandatoryConfig )
        {
            this.addError( ERROR_SERVICECONTRACT_INVALID_MANDATORY_ATTRIBUTE, getLocale( ) );
            return redirect( request, VIEW_CREATE_SERVICECONTRACT, PARAMETER_ID_CLIENTAPPLICATION, selectedClientAppId );
        }

        try
        {
            ServiceContractService.instance( ).create( _servicecontract, selectedClientAppId );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            return redirect( request, VIEW_CREATE_SERVICECONTRACT, PARAMETER_ID_CLIENTAPPLICATION, selectedClientAppId );
        }

        addInfo( INFO_SERVICECONTRACT_CREATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_SERVICECONTRACTS );
    }

    /**
     * Manages the removal form of a servicecontract whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_SERVICECONTRACT )
    public String getConfirmRemoveServiceContract( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SERVICECONTRACT ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_SERVICECONTRACT ) );
        url.addParameter( PARAMETER_ID_SERVICECONTRACT, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_SERVICECONTRACT, url.getUrl( ),
                AdminMessage.TYPE_CONFIRMATION );

        return redirect( request, strMessageUrl );
    }

    /**
     * Handles the removal form of a servicecontract
     *
     * @param request
     *            The Http request
     * @return the jsp URL to display the form to manage servicecontracts
     */
    @Action( ACTION_REMOVE_SERVICECONTRACT )
    public String doRemoveServiceContract( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SERVICECONTRACT ) );
        ServiceContractService.instance( ).delete( nId );
        addInfo( INFO_SERVICECONTRACT_REMOVED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_SERVICECONTRACTS );
    }

    /**
     * Returns the form to update info about a servicecontract
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_SERVICECONTRACT )
    public String getModifyServiceContract( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_SERVICECONTRACT ) );

        if ( _servicecontract == null || ( _servicecontract.getId( ) != nId ) )
        {
            Optional<ServiceContract> optServiceContract = ServiceContractHome.findByPrimaryKey( nId );
            _servicecontract = optServiceContract.orElseThrow( ( ) -> new AppException( ERROR_RESOURCE_NOT_FOUND ) );
        }

        final Map<String, Object> model = getModel( );
        final ClientApplication parentApplication = ClientApplicationHome.getParentApplication( _servicecontract );
        if ( parentApplication != null )
        {
            model.put( PARAMETER_ID_CLIENTAPPLICATION, parentApplication.getId( ) );
        }
        else
        {
            throw new AppException( ERROR_RESOURCE_NOT_FOUND );
        }

        final List<ServiceContractAttributeDefinitionDto> attributeRequirementList = ServiceContractHome.getDto( _servicecontract );
        sortAttributeRequirementList( attributeRequirementList );

        final List<String> mandatoryAttrKeyList = AttributeKeyHome.getMandatoryForCreationAttributeKeyList( ).stream( ).map( AttributeKey::getKeyName )
                .collect( Collectors.toList( ) );

        model.put( MARK_MANDATORY_ATTRIBUTE_KEYS_LIST, mandatoryAttrKeyList );
        model.put( QUERY_PARAM_ACTIVE, request.getParameter(QUERY_PARAM_ACTIVE) );
        model.put( QUERY_PARAM_CONTRACT_NAME, request.getParameter( QUERY_PARAM_CONTRACT_NAME ) );
        model.put( QUERY_PARAM_CLIENT_CODE, request.getParameter( QUERY_PARAM_CLIENT_CODE ) );
        model.put( QUERY_PARAM_START_DATE, request.getParameter( QUERY_PARAM_START_DATE ) );
        model.put( QUERY_PARAM_END_DATE, request.getParameter( QUERY_PARAM_END_DATE ) );
        model.put( MARK_SERVICECONTRACT, _servicecontract );
        model.put( MARK_EDIT_ACTION, "action_modifyServiceContract" );
        model.put( MARK_ATTRIBUTE_REQUIREMENTS_LIST, attributeRequirementList );
        model.put( MARK_AVAILAIBLE_LEVELS_LIST, ServiceContractHome.selectCertificationLevels( ).stream( )
                .sorted( Comparator.comparing( RefCertificationLevel::getLevel ) ).collect( Collectors.toList( ) ) );
        model.put( MARK_AVAILAIBLE_CLIENT_APPLICATIONS_LIST, ClientApplicationHome.selectApplicationList( ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_SERVICECONTRACT ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_SERVICECONTRACT, TEMPLATE_MODIFY_SERVICECONTRACT, model );
    }

    /**
     * Process the change form of a servicecontract
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_SERVICECONTRACT )
    public String doModifyServiceContract( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _servicecontract, request, getLocale( ) );
        final String [ ] selectedClienAppId = request.getParameterValues( PARAMETER_ID_PARENTCLIENTAPPLICATION );
        final String strSelectedClientAppId = selectedClienAppId [0];
        int selectedClientAppId = Integer.parseInt( strSelectedClientAppId );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_SERVICECONTRACT ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _servicecontract, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_SERVICECONTRACT, PARAMETER_ID_SERVICECONTRACT, _servicecontract.getId( ) );
        }

        if ( _servicecontract.getStartingDate( ).equals( null ) )
        {
            addError( ERROR_SERVICECONTRACT_NO_DATE, getLocale( ) );
            return redirect( request, VIEW_MODIFY_SERVICECONTRACT, PARAMETER_ID_SERVICECONTRACT, _servicecontract.getId( ) );
        }
        else
            if ( !checkServiceContractsActivationDate( _servicecontract, selectedClientAppId ) )
            {
                addError( ERROR_SERVICECONTRACT_INVALID_DATE, getLocale( ) );
                return redirect( request, VIEW_MODIFY_SERVICECONTRACT, PARAMETER_ID_SERVICECONTRACT, _servicecontract.getId( ) );
            }

        final String [ ] parameterValues = request.getParameterValues( PARAMETER_ID_PARENTCLIENTAPPLICATION );
        final List<AttributeRight> lstAttributeRights = new ArrayList<>( getAttributesRightsFromRequest( request ).values( ) );
        _servicecontract.getAttributeRights( ).clear( );
        _servicecontract.getAttributeRights( ).addAll( lstAttributeRights );
        final List<AttributeRequirement> attributeRequirements = new ArrayList<>( getAttributesRequirementsFromRequest( request ).values( ) );
        _servicecontract.getAttributeRequirements( ).clear( );
        _servicecontract.getAttributeRequirements( )
                .addAll( attributeRequirements.stream( ).filter( attributeRequirement -> attributeRequirement.getRefCertificationLevel( ) != null
                        && attributeRequirement.getRefCertificationLevel( ).getLevel( ) != null ).collect( Collectors.toList( ) ) );
        final List<AttributeCertification> attributeCertifications = new ArrayList<>( getAttributesCertificationsFromRequest( request ) );
        _servicecontract.getAttributeCertifications( ).clear( );
        _servicecontract.getAttributeCertifications( ).addAll( attributeCertifications.stream( ).filter( certif -> {
            AttributeRight attributeRight = lstAttributeRights.stream( ).filter( atr -> atr.getAttributeKey( ).getId( ) == certif.getAttributeKey( ).getId( ) )
                    .findFirst( ).orElse( null );
            return attributeRight != null && attributeRight.isWritable( );
        } ).collect( Collectors.toList( ) ) );

        final boolean validMandatoryConfig = _servicecontract.getAttributeRights( ).stream( ).filter( AttributeRight::isMandatory )
                .allMatch( attributeRight -> _servicecontract.getAttributeRequirements( ).stream( )
                        .anyMatch( req -> Objects.equals( req.getAttributeKey( ).getKeyName( ), attributeRight.getAttributeKey( ).getKeyName( ) )
                                && req.getRefCertificationLevel( ) != null && req.getRefCertificationLevel( ).getLevel( ) != null ) );
        if ( !validMandatoryConfig )
        {
            this.addError( ERROR_SERVICECONTRACT_INVALID_MANDATORY_ATTRIBUTE, getLocale( ) );
            return redirect( request, VIEW_MODIFY_SERVICECONTRACT, PARAMETER_ID_SERVICECONTRACT, _servicecontract.getId( ) );
        }

        try
        {
            ServiceContractService.instance( ).update( _servicecontract, Integer.parseInt( parameterValues [0] ) );
        }
        catch( IdentityStoreException e )
        {
            addError( e.getMessage( ) );
            return redirect( request, VIEW_MODIFY_SERVICECONTRACT, PARAMETER_ID_SERVICECONTRACT, _servicecontract.getId( ) );
        }

        addInfo( INFO_SERVICECONTRACT_UPDATED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_SERVICECONTRACTS );
    }

    @Action( ACTION_EXPORT_SERVICECONTRACT )
    public void doExportIdentities( HttpServletRequest request )
    {
        try
        {
            List<ServiceContractDto> listServiceContracts = new ArrayList<>();
                    for(  ImmutablePair<ServiceContract, String> serviceContract : getItemsFromIds(_listIdServiceContracts))
                    {
                        listServiceContracts.add(convertContractToDto(serviceContract.left));
                    }
            final Batch<ServiceContractDto> batches = Batch.ofSize( listServiceContracts, BATCH_PARTITION_SIZE );

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            final ZipOutputStream zipOut = new ZipOutputStream( outputStream );

            int i = 0;
            for ( final List<ServiceContractDto> batch : batches )
            {
                final byte [ ] bytes = CsvIdentityService.instance( ).writeContract( batch );
                final ZipEntry zipEntry = new ZipEntry( "service-contracts-" + ++i + ".csv" );
                zipEntry.setSize( bytes.length );
                zipOut.putNextEntry( zipEntry );
                zipOut.write( bytes );
            }
            zipOut.closeEntry( );
            zipOut.close( );
            this.download( outputStream.toByteArray( ), "contractServices.zip", "application/zip" );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            redirectView( request, VIEW_MANAGE_SERVICECONTRACTS );
        }
    }

    /**
     * get AttributeRights to set from httprequest
     *
     * @param request
     *            http request
     * @return AttributeRights to set from httprequest
     */
    private Map<String, AttributeRight> getAttributesRightsFromRequest( HttpServletRequest request )
    {
        Map<String, AttributeRight> mapAttributesRights = new HashMap<String, AttributeRight>( );
        String [ ] tabIdMandatories = request.getParameterValues( PARAMETER_RIGHT_MANDATORY );
        String [ ] tabIdSearchables = request.getParameterValues( PARAMETER_RIGHT_SEARCHABLE );
        String [ ] tabIdReadables = request.getParameterValues( PARAMETER_RIGHT_READABLE );
        String [ ] tabIdWritables = request.getParameterValues( PARAMETER_RIGHT_WRITABLE );

        for ( int nCpt = 0; ( tabIdSearchables != null ) && ( nCpt < tabIdSearchables.length ); nCpt++ )
        {
            final String strIdAttribute = tabIdSearchables [nCpt];
            final AttributeRight attributeRight = new AttributeRight( );
            attributeRight.setSearchable( true );
            attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ), false ) );
            mapAttributesRights.put( strIdAttribute, attributeRight );
        }

        for ( int nCpt = 0; ( tabIdMandatories != null ) && ( nCpt < tabIdMandatories.length ); nCpt++ )
        {
            String strIdAttribute = tabIdMandatories [nCpt];

            if ( mapAttributesRights.get( strIdAttribute ) != null )
            {
                mapAttributesRights.get( strIdAttribute ).setMandatory( true );
            }
            else
            {
                final AttributeRight attributeRight = new AttributeRight( );
                attributeRight.setMandatory( true );
                attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ), false ) );
                mapAttributesRights.put( strIdAttribute, attributeRight );
            }
        }

        for ( int nCpt = 0; ( tabIdReadables != null ) && ( nCpt < tabIdReadables.length ); nCpt++ )
        {
            String strIdAttribute = tabIdReadables [nCpt];

            if ( mapAttributesRights.get( strIdAttribute ) != null )
            {
                mapAttributesRights.get( strIdAttribute ).setReadable( true );
            }
            else
            {
                AttributeRight attributeRight = new AttributeRight( );
                attributeRight.setReadable( true );
                attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ), false ) );
                mapAttributesRights.put( strIdAttribute, attributeRight );
            }
        }

        for ( int nCpt = 0; ( tabIdWritables != null ) && ( nCpt < tabIdWritables.length ); nCpt++ )
        {
            String strIdAttribute = tabIdWritables [nCpt];

            if ( mapAttributesRights.get( strIdAttribute ) != null )
            {
                mapAttributesRights.get( strIdAttribute ).setWritable( true );
            }
            else
            {
                AttributeRight attributeRight = new AttributeRight( );
                attributeRight.setWritable( true );
                attributeRight.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( strIdAttribute ), false ) );
                mapAttributesRights.put( strIdAttribute, attributeRight );
            }
        }

        return mapAttributesRights;
    }

    private List<AttributeCertification> getAttributesCertificationsFromRequest( HttpServletRequest request )
    {
        final List<AttributeCertification> attributeCertifications = new ArrayList<>( );
        final String [ ] tabIdCertificationLevels = request.getParameterValues( PARAMETER_CERTICATION_PROCESSUS );
        if ( tabIdCertificationLevels != null )
        {
            Arrays.stream( tabIdCertificationLevels ).map( s -> s.split( "," ) )
                    .collect( Collectors.groupingBy( s -> s [0], Collectors.mapping( s -> s [1], Collectors.toList( ) ) ) )
                    .forEach( ( attributeId, processusIds ) -> {
                        final AttributeCertification attributeCertification = new AttributeCertification( );
                        attributeCertification.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( attributeId ), false ) );
                        processusIds.forEach( processusId -> attributeCertification.getRefAttributeCertificationProcessus( )
                                .add( RefAttributeCertificationProcessusHome.findByPrimaryKey( Integer.parseInt( processusId ) ) ) );
                        attributeCertifications.add( attributeCertification );
                    } );
        }

        return attributeCertifications;
    }

    private Map<String, AttributeRequirement> getAttributesRequirementsFromRequest( HttpServletRequest request )
    {
        Map<String, AttributeRequirement> mapAttributesRights = new HashMap<>( );
        String [ ] tabIdCertificationLevels = request.getParameterValues( PARAMETER_CERTICATION_LEVEL );

        for ( int nCpt = 0; ( tabIdCertificationLevels != null ) && ( nCpt < tabIdCertificationLevels.length ); nCpt++ )
        {
            String strIdAttribute = tabIdCertificationLevels [nCpt];
            AttributeRequirement attributeRequirement = new AttributeRequirement( );
            if ( strIdAttribute != null && strIdAttribute.contains( "," ) )
            {
                String [ ] keyAndLevel = strIdAttribute.split( "," );
                attributeRequirement.setAttributeKey( AttributeKeyHome.findByPrimaryKey( Integer.parseInt( keyAndLevel [0] ), false ) );
                attributeRequirement.setRefCertificationLevel( RefCertificationLevelHome.findByPrimaryKey( Integer.parseInt( keyAndLevel [1] ) ) );
            }
            mapAttributesRights.put( strIdAttribute, attributeRequirement );
        }

        return mapAttributesRights;
    }

    private boolean checkServiceContractsActivationDate( final ServiceContract serviceContract, final int clientAppId )
    {
        final ClientApplication clientApplication = ClientApplicationHome.findByPrimaryKey( clientAppId );
        final List<ServiceContract> serviceContracts = ClientApplicationHome.selectServiceContracts( clientApplication );
        final List<ServiceContract> filteredServiceContracts = serviceContracts.stream( ).filter( c -> !Objects.equals( c.getId( ), serviceContract.getId( ) ) )
                .collect( Collectors.toList( ) );
        if ( filteredServiceContracts == null || filteredServiceContracts.isEmpty( ) )
        {
            return true;
        }

        if ( filteredServiceContracts.stream( ).anyMatch( contract -> ServiceContractService.instance( ).isInRange( serviceContract.getStartingDate( ),
                contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            return false;
        }
        if ( filteredServiceContracts.stream( ).anyMatch( contract -> ServiceContractService.instance( ).isInRange( serviceContract.getEndingDate( ),
                contract.getStartingDate( ), contract.getEndingDate( ) ) ) )
        {
            return false;
        }
        // TODO traiter le cas où il existe un contrat sans date de fin => soit on interdit soit on ferme le contrat automatiquement
        return filteredServiceContracts.stream( )
                .noneMatch( contract -> contract.getEndingDate( ) == null && contract.getStartingDate( ).before( serviceContract.getStartingDate( ) ) );
    }

    private void sortAttributeRequirementList( final List<ServiceContractAttributeDefinitionDto> attributeRequirementList )
    {
        if ( attributeRequirementList != null )
        {
            attributeRequirementList.sort( ( o1, o2 ) -> {
                final boolean p1 = o1.getAttributeKey( ).getPivot( );
                final boolean p2 = o2.getAttributeKey( ).getPivot( );
                if ( p1 != p2 )
                {
                    return Boolean.compare( p2, p1 );
                }
                return Integer.compare( o1.getAttributeKey( ).getId( ), o2.getAttributeKey( ).getId( ) );
            } );
            attributeRequirementList.forEach( a -> {
                a.getCompatibleProcessus( ).sort( Comparator.comparing( p -> p.getLevel( ).getRefCertificationLevel( ).getLevel( ) ) );
                a.getRefAttributeCertificationProcessus( ).sort( Comparator.comparingInt( p -> a.getCompatibleProcessus( ).indexOf( p ) ) );
            } );
        }
    }
}

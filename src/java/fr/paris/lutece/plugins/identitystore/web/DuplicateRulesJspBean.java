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

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleAttributeTreatment;
import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRuleHome;
import fr.paris.lutece.plugins.identitystore.service.duplicate.DuplicateRuleService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.v3.csv.CsvDuplicateRule;
import fr.paris.lutece.plugins.identitystore.v3.csv.CsvIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
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

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class provides the user interface to manage DuplicateRule features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageDuplicateRules.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_DUPLICATE_RULES_MANAGEMENT" )
public class DuplicateRulesJspBean extends ManageIdentitiesJspBean
{
    // Templates
    private static final String TEMPLATE_MANAGE_DUPLICATERULES = "/admin/plugins/identitystore/manage_duplicaterules.html";
    private static final String TEMPLATE_DISPLAY_DUPLICATERULES = "/admin/plugins/identitystore/view_duplicaterule.html";
    private static final String TEMPLATE_CREATE_DUPLICATERULES = "/admin/plugins/identitystore/create_duplicaterule.html";
    private static final String TEMPLATE_MODIFY_DUPLICATERULES = "/admin/plugins/identitystore/create_duplicaterule.html";

    // Parameters
    private static final String PARAMETER_ID_DUPLICATERULES = "id";
    private static final String PARAMETER_SELECTED_CHECKED_ATTRIBUTES = "selected_checked_attributes";
    private static final String PARAMETER_SELECTED_ATTRIBUTE_TREATMENTS_KEYS = "selected_attribute_treatment_attributes_";
    private static final String PARAMETER_SELECTED_ATTRIBUTE_TREATMENTS_TYPE = "selected_attribute_treatment_type_";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_DUPLICATERULES = "identitystore.manage_duplicaterules.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_DUPLICATERULES = "identitystore.modify_duplicaterules.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_DUPLICATERULES = "identitystore.create_duplicaterules.pageTitle";

    // Markers
    private static final String MARK_DUPLICATERULES_LIST = "duplicaterule_list";
    private static final String MARK_DUPLICATERULE = "duplicaterule";
    private static final String MARK_ACTION = "action";
    private static final String MARK_AVAILABLE_ATTRIBUTES = "available_attributes";
    private static final String MARK_AVAILABLE_ATTRIBUTE_TREATMENT_TYPES = "available_rule_types";
    private static final String MARK_SELECTED_CHECKED_ATTRIBUTES = "rule_checked_attributes";

    private static final String JSP_MANAGE_DUPLICATERULES = "jsp/admin/plugins/identitystore/ManageDuplicateRules.jsp";

    // Properties
    private static final String MESSAGE_CONFIRM_REMOVE_DUPLICATERULES = "identitystore.message.confirmRemoveDuplicateRule";

    // Validations
    private static final String VALIDATION_ATTRIBUTES_PREFIX = "identitystore.model.entity.duplicaterules.attribute.";

    // Views
    private static final String VIEW_MANAGE_DUPLICATERULES = "manageDuplicateRules";
    private static final String VIEW_DISPLAY_DUPLICATERULES = "displayDuplicateRule";
    private static final String VIEW_CREATE_DUPLICATERULES = "createDuplicateRule";
    private static final String VIEW_MODIFY_DUPLICATERULES = "modifyDuplicateRule";

    // Actions
    private static final String ACTION_CREATE_DUPLICATERULES = "createDuplicateRule";
    private static final String ACTION_MODIFY_DUPLICATERULES = "modifyDuplicateRule";
    private static final String ACTION_REMOVE_DUPLICATERULES = "removeDuplicateRule";
    private static final String ACTION_CONFIRM_REMOVE_DUPLICATERULES = "confirmRemoveDuplicateRule";
    private static final String ACTION_EXPORT_DUPLICATERULES = "exportDuplicaterules";

    // Infos
    private static final String INFO_DUPLICATERULES_CREATED = "identitystore.info.duplicaterule.created";
    private static final String INFO_DUPLICATERULES_UPDATED = "identitystore.info.duplicaterule.updated";
    private static final String INFO_DUPLICATERULES_REMOVED = "identitystore.info.duplicaterule.removed";
    private static final int BATCH_PARTITION_SIZE = AppPropertiesService.getPropertyInt( "identitystore.export.batch.size", 100 );

    // Errors
    private static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";

    // Session variable to store working values
    private DuplicateRule _duplicateRule;
    private List<DuplicateRule> _listDuplicateRules;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_MANAGE_DUPLICATERULES, defaultView = true )
    public String getManageDuplicateRules( HttpServletRequest request )
    {
        _duplicateRule = null;

        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX ) == null || _listDuplicateRules.isEmpty( ) )
        {
            _listDuplicateRules = DuplicateRuleHome.findAll( );
            if ( _listDuplicateRules != null && !_listDuplicateRules.isEmpty( ) )
            {
                _listDuplicateRules.sort( Comparator.comparing( DuplicateRule::getCode ) );
            }
        }

        final Map<String, Object> model = getPaginatedListModel( request, MARK_DUPLICATERULES_LIST, _listDuplicateRules, JSP_MANAGE_DUPLICATERULES );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_DUPLICATERULES, TEMPLATE_MANAGE_DUPLICATERULES, model );
    }

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_DISPLAY_DUPLICATERULES )
    public String getDisplayDuplicateRules( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_DUPLICATERULES ) );
        _duplicateRule = DuplicateRuleHome.find( nId );
        if ( _duplicateRule == null )
        {
            throw new AppException( ERROR_RESOURCE_NOT_FOUND );
        }

        final Map<String, Object> model = getModel( );
        model.put( MARK_DUPLICATERULE, _duplicateRule );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_DUPLICATERULES, TEMPLATE_DISPLAY_DUPLICATERULES, model );
    }

    /**
     * reset the _listIdDuplicateRules list
     */
    public void resetListId( )
    {
        _listDuplicateRules = new ArrayList<>( );
    }

    /**
     * Returns the form to create a servicecontract
     *
     * @param request
     *            The Http request
     * @return the html code of the servicecontract form
     */
    @View( VIEW_CREATE_DUPLICATERULES )
    public String getCreateDuplicateRule( HttpServletRequest request )
    {
        final Map<String, Object> model = getModel( );

        if ( _duplicateRule == null )
        {
            _duplicateRule = new DuplicateRule( );
        }

        model.put( MARK_DUPLICATERULE, _duplicateRule );
        model.put( MARK_SELECTED_CHECKED_ATTRIBUTES, new ArrayList<>( ) );
        model.put( MARK_AVAILABLE_ATTRIBUTE_TREATMENT_TYPES, AttributeTreatmentType.valuesForRules( ) );
        model.put( MARK_ACTION, "action_createDuplicateRule" );
        model.put( MARK_AVAILABLE_ATTRIBUTES, AttributeKeyHome.getAttributeKeysList( false ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_DUPLICATERULES ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_DUPLICATERULES, TEMPLATE_CREATE_DUPLICATERULES, model );
    }

    /**
     * Process the data capture form of a new servicecontract
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_CREATE_DUPLICATERULES )
    public String doCreateDuplicateRule( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _duplicateRule, request, getLocale( ) );
        _duplicateRule.setAuthorName(this.getUser().getEmail());
        _duplicateRule.getCheckedAttributes( ).addAll( this.extractAttributeKeys( request ) );
        _duplicateRule.getAttributeTreatments( ).addAll( this.extractAttributeTreatments( request ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_CREATE_DUPLICATERULES ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _duplicateRule, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_CREATE_DUPLICATERULES );
        }

        try
        {
            DuplicateRuleService.instance( ).create( _duplicateRule );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            return redirectView( request, VIEW_CREATE_DUPLICATERULES );
        }

        addInfo( INFO_DUPLICATERULES_CREATED, getLocale( ) );
        resetListId( );
        _duplicateRule = null;

        return redirectView( request, VIEW_MANAGE_DUPLICATERULES );
    }

    /**
     * Manages the removal form of a servicecontract whose identifier is in the http request
     *
     * @param request
     *            The Http request
     * @return the html code to confirm
     */
    @Action( ACTION_CONFIRM_REMOVE_DUPLICATERULES )
    public String getConfirmRemoveDuplicateRule( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_DUPLICATERULES ) );
        UrlItem url = new UrlItem( getActionUrl( ACTION_REMOVE_DUPLICATERULES ) );
        url.addParameter( PARAMETER_ID_DUPLICATERULES, nId );

        String strMessageUrl = AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_DUPLICATERULES, url.getUrl( ),
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
    @Action( ACTION_REMOVE_DUPLICATERULES )
    public String doRemoveDuplicateRule( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_DUPLICATERULES ) );
        DuplicateRuleService.instance( ).delete( nId );
        addInfo( INFO_DUPLICATERULES_REMOVED, getLocale( ) );
        resetListId( );

        return redirectView( request, VIEW_MANAGE_DUPLICATERULES );
    }

    /**
     * Returns the form to update info about a servicecontract
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_DUPLICATERULES )
    public String getModifyDuplicateRule( HttpServletRequest request )
    {
        int nId = Integer.parseInt( request.getParameter( PARAMETER_ID_DUPLICATERULES ) );

        if ( _duplicateRule == null || ( _duplicateRule.getId( ) != nId ) )
        {
            _duplicateRule = DuplicateRuleHome.find( nId );
            if ( _duplicateRule == null )
            {
                throw new AppException( ERROR_RESOURCE_NOT_FOUND );
            }
        }

        final Map<String, Object> model = getModel( );

        model.put( MARK_DUPLICATERULE, _duplicateRule );
        model.put( MARK_SELECTED_CHECKED_ATTRIBUTES,
                _duplicateRule.getCheckedAttributes( ).stream( ).map( AttributeKey::getKeyName ).collect( Collectors.toList( ) ) );
        model.put( MARK_AVAILABLE_ATTRIBUTE_TREATMENT_TYPES, AttributeTreatmentType.valuesForRules( ) );
        model.put( MARK_ACTION, "action_modifyDuplicateRule" );
        model.put( MARK_AVAILABLE_ATTRIBUTES, AttributeKeyHome.getAttributeKeysList( false ) );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_MODIFY_DUPLICATERULES ) );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_DUPLICATERULES, TEMPLATE_MODIFY_DUPLICATERULES, model );
    }

    /**
     * Process the change form of a servicecontract
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_MODIFY_DUPLICATERULES )
    public String doModifyDuplicateRule( HttpServletRequest request ) throws AccessDeniedException
    {
        populate( _duplicateRule, request, getLocale( ) );
        _duplicateRule.setAuthorName(this.getUser().getEmail());
        _duplicateRule.getCheckedAttributes( ).clear( );
        _duplicateRule.getCheckedAttributes( ).addAll( this.extractAttributeKeys( request ) );
        _duplicateRule.getAttributeTreatments( ).clear( );
        _duplicateRule.getAttributeTreatments( ).addAll( this.extractAttributeTreatments( request ) );

        if ( !SecurityTokenService.getInstance( ).validate( request, ACTION_MODIFY_DUPLICATERULES ) )
        {
            throw new AccessDeniedException( "Invalid security token" );
        }

        // Check constraints
        if ( !validateBean( _duplicateRule, VALIDATION_ATTRIBUTES_PREFIX ) )
        {
            return redirect( request, VIEW_MODIFY_DUPLICATERULES, PARAMETER_ID_DUPLICATERULES, _duplicateRule.getId( ) );
        }

        try
        {
            DuplicateRuleService.instance( ).update( _duplicateRule );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            return redirect( request, VIEW_MODIFY_DUPLICATERULES, PARAMETER_ID_DUPLICATERULES, _duplicateRule.getId( ) );
        }

        addInfo( INFO_DUPLICATERULES_UPDATED, getLocale( ) );
        resetListId( );
        _duplicateRule = null;

        return redirectView( request, VIEW_MANAGE_DUPLICATERULES );
    }

    @Action( ACTION_EXPORT_DUPLICATERULES )
    public void doExportDuplicatesRules( HttpServletRequest request )
    {
        try{
            final Batch<CsvDuplicateRule> batches = Batch.ofSize( convertToCsvDuplicateRule(_listDuplicateRules), BATCH_PARTITION_SIZE );

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            final ZipOutputStream zipOut = new ZipOutputStream( outputStream );

            int i = 0;
            for ( final List<CsvDuplicateRule> batch : batches )
            {
                final byte [ ] bytes = CsvIdentityService.instance( ).writeDuplicateRules( batch );
                final ZipEntry zipEntry = new ZipEntry( "duplicateRules-" + ++i + ".csv" );
                zipEntry.setSize( bytes.length );
                zipOut.putNextEntry( zipEntry );
                zipOut.write( bytes );
            }
            zipOut.closeEntry( );
            zipOut.close( );
            this.download( outputStream.toByteArray( ), "duplicateRules.zip", "application/zip" );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            redirectView( request, VIEW_MANAGE_DUPLICATERULES );
        }
    }

    private List<AttributeKey> extractAttributeKeys(final HttpServletRequest request)
    {
        final String[] selectedAttributes = request.getParameterValues(PARAMETER_SELECTED_CHECKED_ATTRIBUTES);
        if (selectedAttributes != null)
        {
            return Arrays.stream(selectedAttributes).map(key -> AttributeKeyHome.findByKey(key, false)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private List<DuplicateRuleAttributeTreatment> extractAttributeTreatments( final HttpServletRequest request )
    {
        final List<DuplicateRuleAttributeTreatment> treatments = new ArrayList<>( );
        request.getParameterMap( ).entrySet( ).stream( )
                .filter( stringEntry -> stringEntry.getKey( ).startsWith( PARAMETER_SELECTED_ATTRIBUTE_TREATMENTS_KEYS ) ).forEach( stringEntry -> {
                    final List<AttributeKey> attributeKeys = Arrays.stream( stringEntry.getValue( ) ).map( key -> AttributeKeyHome.findByKey( key, false ) )
                            .collect( Collectors.toList( ) );
                    final String index = stringEntry.getKey( ).replace( PARAMETER_SELECTED_ATTRIBUTE_TREATMENTS_KEYS, "" );
                    final String [ ] type = request.getParameterValues( PARAMETER_SELECTED_ATTRIBUTE_TREATMENTS_TYPE + index );
                    final DuplicateRuleAttributeTreatment treatment = new DuplicateRuleAttributeTreatment( );
                    treatment.getAttributes( ).addAll( attributeKeys );
                    treatment.setType( AttributeTreatmentType.valueOf( type [0] ) );
                    treatments.add( treatment );
                } );

        return treatments;
    }

    private List<CsvDuplicateRule> convertToCsvDuplicateRule( List<DuplicateRule> duplicateRules )
    {
        List<CsvDuplicateRule> csvDuplicateRules = new ArrayList<>( );

        for( DuplicateRule duplicateRule : duplicateRules )
        {
            CsvDuplicateRule csvDuplicateRule = new CsvDuplicateRule( );

            csvDuplicateRule.setCode( duplicateRule.getCode( ) );
            csvDuplicateRule.setName( duplicateRule.getName( ) );
            csvDuplicateRule.setDescription( duplicateRule.getDescription( ) );
            csvDuplicateRule.setPriority( duplicateRule.getPriority( ) );
            csvDuplicateRule.setDetectionLimit( duplicateRule.getDetectionLimit( ) );
            csvDuplicateRule.setActive( duplicateRule.isActive( ) );
            csvDuplicateRule.setDaemon( duplicateRule.isDaemon( ) );
            csvDuplicateRule.setImpactedAttributes(duplicateRule.getCheckedAttributes().size());
            csvDuplicateRule.setNbFilledAttributes( duplicateRule.getNbFilledAttributes( ) );
            csvDuplicateRule.setNbEqualAttributes( duplicateRule.getNbEqualAttributes( ) );
            csvDuplicateRule.setNbMissingAttributes( duplicateRule.getNbMissingAttributes( ) );
            String treatmentType = "";
            for(DuplicateRuleAttributeTreatment treatment : duplicateRule.getAttributeTreatments())
            {
                treatmentType = treatmentType.concat(" ").concat(treatment.getType().getLabel()).concat("/");
            }
            csvDuplicateRule.settreatmentType(!treatmentType.isEmpty() ? treatmentType.substring( 0, treatmentType.length( ) - 1 ) : treatmentType );
            csvDuplicateRule.setCreationDate( duplicateRule.getCreationDate( ) );
            csvDuplicateRule.setLastUpdatedDate( duplicateRule.getLastUpdateDate( ) );
            csvDuplicateRule.setAuthorName( duplicateRule.getAuthorName( ) );

            csvDuplicateRules.add( csvDuplicateRule );
        }
        csvDuplicateRules.sort(Comparator.comparing(CsvDuplicateRule::getPriority));
        return csvDuplicateRules;
    }
}

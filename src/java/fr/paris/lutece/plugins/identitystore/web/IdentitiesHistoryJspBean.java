package fr.paris.lutece.plugins.identitystore.web;


import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.history.IdentityHistoryService;
import fr.paris.lutece.plugins.identitystore.utils.Batch;
import fr.paris.lutece.plugins.identitystore.v3.csv.CsvIdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller( controllerJsp = "IdentitiesHistory.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IdentitiesHistoryJspBean extends ManageIdentitiesJspBean
{
    //Templates
    private static final String TEMPLATE_IDENTITIES_HISTORY = "/admin/plugins/identitystore/identities_history.html";
    //Parameters
    private static final Integer DAYS_FROM_HYSTORY = 0;
    //Properties
    private static final String PROPERTY_PAGE_TITLE_IDENTITIES_HISTORY = "";
    private static final String PROPERTY_FEATURE_HISTORY_SEARCH = "historySearch";
    //Markers
    private static final String MARK_IDENTITY_CHANGE_LIST = "identity_change_list";
    private static final String JSP_IDENTITIES_HISTORY = "jsp/admin/plugins/identitystore/IdentitiesHistory.jsp";

    //Views
    private static final String VIEW_IDENTITIES_HISTORY = "viewIdentitiesHistory";

    //Actions
    private static final String ACTION_EXPORT_IDENTITIES = "exportIdentities";

    //Infos
    private static final int BATCH_PARTITION_SIZE = AppPropertiesService.getPropertyInt( "identitystore.export.batch.size", 100 );

    // Session variable to store working values
    private final List<IdentityDto> _identities = new ArrayList<>( );
    List<IdentityChange> _historyList = new ArrayList<>();
    private List<String> _listQuery = new ArrayList<>( );

    @View( value = VIEW_IDENTITIES_HISTORY, defaultView = true )
    public String getIdentitiesHistory( HttpServletRequest request )
    {
        _identities.clear( );
        _historyList.clear();
        final Map<String, String> queryParameters = this.getQueryParameters( request );
        final String cuid = queryParameters.get( QUERY_PARAM_CUID ) != null ?
                queryParameters.get( QUERY_PARAM_CUID ) : "";
        final String type = queryParameters.get( QUERY_PARAM_TYPE ) != null ?
                StringUtils.replace(queryParameters.get( QUERY_PARAM_TYPE ).toUpperCase(), " ", "_") : "";
        final String status = queryParameters.get( QUERY_PARAM_STATUS ) != null ?
                queryParameters.get( QUERY_PARAM_STATUS ) : "";
        final String author_type = queryParameters.get( QUERY_PARAM_AUTHOR_TYPE ) != null ?
                queryParameters.get( QUERY_PARAM_AUTHOR_TYPE ) : "";
        final String author_name = queryParameters.get( QUERY_PARAM_AUTHOR_NAME ) != null ?
                queryParameters.get( QUERY_PARAM_AUTHOR_NAME ) : "";
        final String client_code = queryParameters.get( QUERY_PARAM_CLIENT_CODE ) != null ?
                queryParameters.get( QUERY_PARAM_CLIENT_CODE ) : "";
        final String date = queryParameters.get( QUERY_PARAM_DATE ) != null ?
                queryParameters.get( QUERY_PARAM_DATE ) : "";
        Date modificationDate = null;
        if(StringUtils.isNotBlank(date))
        {
            try
            {
                List<String> listDate = Arrays.asList(date.replaceAll("-","/").split("/"));
                String dateFormated = listDate.get(2) + "/" + listDate.get(1) + "/" + listDate.get(0);
                modificationDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateFormated);
            } catch (ParseException e)
            {
                addError(e.getMessage());
                return redirectView(request, VIEW_IDENTITIES_HISTORY);
            }
        }

        List<String> typeList = new ArrayList<>();
        List<String> statusList = IdentityHistoryService.instance().getStatusList();
        try
        {
            if( StringUtils.isNotBlank( cuid ) || StringUtils.isNotBlank( type ) ||
                    StringUtils.isNotBlank( date ) || StringUtils.isNotBlank( author_type ) || StringUtils.isNotBlank( author_name ) ||
                    StringUtils.isNotBlank( client_code ) || StringUtils.isNotBlank(status))
            {
                if(StringUtils.isNotBlank( type ))
                {
                    _historyList.addAll(IdentityHome.findHistoryBySearchParameters(cuid, client_code, author_name,
                            IdentityChangeType.valueOf(type), status, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null, 0));
                }
                else
                {
                    _historyList.addAll(IdentityHome.findHistoryBySearchParameters(cuid, client_code, author_name,
                            null, status, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null, 0));
                }
            }
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            this.clearParameters( request );
            return redirectView( request, VIEW_IDENTITIES_HISTORY );
        }

        for( IdentityChangeType value : IdentityChangeType.values())
        {
            typeList.add(value.toString());
        }

        Map<String, Object> model = getPaginatedListModel(request, MARK_IDENTITY_CHANGE_LIST, _historyList, JSP_IDENTITIES_HISTORY);

        model.put(QUERY_PARAM_CUID, cuid);
        model.put(QUERY_PARAM_TYPE, type);
        model.put(QUERY_PARAM_STATUS, status);
        model.put(QUERY_PARAM_DATE, date);
        model.put(QUERY_PARAM_AUTHOR_TYPE, author_type);
        model.put(QUERY_PARAM_AUTHOR_NAME, author_name);
        model.put(QUERY_PARAM_CLIENT_CODE, client_code);
        model.put(QUERY_PARAM_TYPE_LIST, typeList);
        model.put(QUERY_PARAM_STATUS_LIST, statusList);

        return getPage( PROPERTY_PAGE_TITLE_IDENTITIES_HISTORY, TEMPLATE_IDENTITIES_HISTORY, model );
    }

    /**
     * Process the data capture form of a new suspiciousidentity
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_EXPORT_IDENTITIES )
    public void doExportIdentities( HttpServletRequest request )
    {
        try
        {
            final Batch<IdentityChange> batches = Batch.ofSize( _historyList, BATCH_PARTITION_SIZE );

            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            final ZipOutputStream zipOut = new ZipOutputStream( outputStream );

            int i = 0;
            for ( final List<IdentityChange> batch : batches )
            {
                final byte [ ] bytes = CsvIdentityService.instance( ).writeChange( batch );
                final ZipEntry zipEntry = new ZipEntry( "identities-" + ++i + ".csv" );
                zipEntry.setSize( bytes.length );
                zipOut.putNextEntry( zipEntry );
                zipOut.write( bytes );
            }
            zipOut.closeEntry( );
            zipOut.close( );
            this.download( outputStream.toByteArray( ), "identitiesHistory.zip", "application/zip" );
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            redirectView( request, VIEW_IDENTITIES_HISTORY );
        }
    }

    private Map<String, String> getHisotryQueryParameters(HttpServletRequest request )
    {
        Map<String, String> queryParameters = new HashMap<>();

        _listQuery.forEach(queryParameter -> {
            final String param = request.getParameter( queryParameter );
            if ( param != null )
            {
                queryParameters.put( queryParameter, param );
            }
        });

        return queryParameters;
    }


}

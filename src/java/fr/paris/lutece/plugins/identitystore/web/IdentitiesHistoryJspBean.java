package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller( controllerJsp = "IdentitiesHistory.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IdentitiesHistoryJspBean extends MVCAdminJspBean
{
    //Templates
    private static final String TEMPLATE_IDENTITIES_HISTORY = "/admin/plugins/identitystore/identities_history.html";
    //Parameters
    private static final Integer DAYS_FROM_HYSTORY = 0;
    //Properties
    private static final String PROPERTY_PAGE_TITLE_IDENTITIES_HISTORY = "";
    //Markers
    private static final String MARK_IDENTITY_CHANGE_LIST = "identity_change_list";

    //Views
    private static final String VIEW_IDENTITIES_HISTORY = "viewIdentitiesHistory";

    //Actions

    //Infos
    private static final String QUERY_PARAM_CUID = "customerId";
    private static final String QUERY_PARAM_TYPE = "type";
    private static final String QUERY_PARAM_STATUS = "status";
    private static final String QUERY_PARAM_DATE = "date";
    private static final String QUERY_PARAM_AUTHOR_TYPE = "author_type";
    private static final String QUERY_PARAM_AUTHOR_NAME = "author_name";
    private static final String QUERY_PARAM_CLIENT_CODE = "client_code";

    // Session variable to store working values
    private Identity _identity;
    private final List<IdentityDto> _identities = new ArrayList<>( );
    private List<String> _listQuery = new ArrayList<>( );

    @View( value = VIEW_IDENTITIES_HISTORY, defaultView = true )
    public String getIdentitiesHistory( HttpServletRequest request )
    {
        _identity = null;
        _identities.clear( );
        initListQuery();
        final Map<String, String> queryParameters = this.getQueryParameters( request );
        final String customerId = queryParameters.get( QUERY_PARAM_CUID ) != null ?
                queryParameters.get( QUERY_PARAM_CUID ) : "";
        final String type = queryParameters.get( QUERY_PARAM_TYPE ) != null ?
                StringUtils.replace(queryParameters.get( QUERY_PARAM_TYPE ).toUpperCase(), " ", "_") : "";
        final String status = queryParameters.get( QUERY_PARAM_STATUS ) != null ?
                StringUtils.replace(queryParameters.get( QUERY_PARAM_STATUS ).toUpperCase(), " ", "_") : "";
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

        List<IdentityChange> historyList = new ArrayList<>();
        try
        {
            if( StringUtils.isNotBlank( customerId ) || StringUtils.isNotBlank( type ) || StringUtils.isNotBlank( status ) ||
                    StringUtils.isNotBlank( date ) || StringUtils.isNotBlank( author_type ) || StringUtils.isNotBlank( author_name ) ||
                    StringUtils.isNotBlank( client_code ))
            {
                if(StringUtils.isNotBlank( type ))
                {
                    historyList.addAll(IdentityHome.findHistoryBySearchParameters(customerId, client_code, author_name,
                            IdentityChangeType.valueOf(type), status, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null));
                }
                else
                {
                    historyList.addAll(IdentityHome.findHistoryBySearchParameters(customerId, client_code, author_name,
                            null, status, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null));
                }
            }
        }
        catch( Exception e )
        {
            addError( e.getMessage( ) );
            return redirectView( request, VIEW_IDENTITIES_HISTORY );
        }
        Map<String, Object> model = new HashMap<>();

        model.put(MARK_IDENTITY_CHANGE_LIST,historyList);
        model.put(QUERY_PARAM_CUID, customerId);
        model.put(QUERY_PARAM_TYPE, type);
        model.put(QUERY_PARAM_STATUS, status);
        model.put(QUERY_PARAM_DATE, date);
        model.put(QUERY_PARAM_AUTHOR_TYPE, author_type);
        model.put(QUERY_PARAM_AUTHOR_NAME, author_name);
        model.put(QUERY_PARAM_CLIENT_CODE, client_code);

        return getPage( PROPERTY_PAGE_TITLE_IDENTITIES_HISTORY, TEMPLATE_IDENTITIES_HISTORY, model );
    }

    private Map<String, String> getQueryParameters( HttpServletRequest request )
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

    private void initListQuery( )
    {
        _listQuery = new ArrayList<>();
        _listQuery.add(QUERY_PARAM_CUID);
        _listQuery.add(QUERY_PARAM_TYPE);
        _listQuery.add(QUERY_PARAM_STATUS);
        _listQuery.add(QUERY_PARAM_DATE);
        _listQuery.add(QUERY_PARAM_AUTHOR_TYPE);
        _listQuery.add(QUERY_PARAM_AUTHOR_NAME);
        _listQuery.add(QUERY_PARAM_CLIENT_CODE);
    }


}

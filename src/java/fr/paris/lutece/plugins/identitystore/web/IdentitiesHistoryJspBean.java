package fr.paris.lutece.plugins.identitystore.web;


import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChange;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
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

    //Infos

    // Session variable to store working values
    private final List<IdentityDto> _identities = new ArrayList<>( );
    private List<String> _listQuery = new ArrayList<>( );

    @View( value = VIEW_IDENTITIES_HISTORY, defaultView = true )
    public String getIdentitiesHistory( HttpServletRequest request )
    {
        _identities.clear( );
        final Map<String, String> queryParameters = this.getQueryParameters( request );
        final String cuid = queryParameters.get( QUERY_PARAM_CUID ) != null ?
                queryParameters.get( QUERY_PARAM_CUID ) : "";
        final String type = queryParameters.get( QUERY_PARAM_TYPE ) != null ?
                StringUtils.replace(queryParameters.get( QUERY_PARAM_TYPE ).toUpperCase(), " ", "_") : "";
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
        List<String> typeList = new ArrayList<>();
        try
        {
            if( StringUtils.isNotBlank( cuid ) || StringUtils.isNotBlank( type ) ||
                    StringUtils.isNotBlank( date ) || StringUtils.isNotBlank( author_type ) || StringUtils.isNotBlank( author_name ) ||
                    StringUtils.isNotBlank( client_code ))
            {
                if(StringUtils.isNotBlank( type ))
                {
                    historyList.addAll(IdentityHome.findHistoryBySearchParameters(cuid, client_code, author_name,
                            IdentityChangeType.valueOf(type), null, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null, 0));
                }
                else
                {
                    historyList.addAll(IdentityHome.findHistoryBySearchParameters(cuid, client_code, author_name,
                            null, null, author_type, modificationDate, null, DAYS_FROM_HYSTORY, null, 0));
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

        Map<String, Object> model = getPaginatedListModel(request, MARK_IDENTITY_CHANGE_LIST, historyList, JSP_IDENTITIES_HISTORY);

        model.put(QUERY_PARAM_CUID, cuid);
        model.put(QUERY_PARAM_TYPE, type);
        model.put(QUERY_PARAM_DATE, date);
        model.put(QUERY_PARAM_AUTHOR_TYPE, author_type);
        model.put(QUERY_PARAM_AUTHOR_NAME, author_name);
        model.put(QUERY_PARAM_CLIENT_CODE, client_code);
        model.put(QUERY_PARAM_TYPE_LIST, typeList);

        return getPage( PROPERTY_PAGE_TITLE_IDENTITIES_HISTORY, TEMPLATE_IDENTITIES_HISTORY, model );
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

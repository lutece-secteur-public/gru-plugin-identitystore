package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IndicatorsActionsType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.UsingElasticConnection;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller( controllerJsp = "Indicators.jsp", controllerPath = "jsp/admin/plugins/identitystore/", right = "IDENTITYSTORE_MANAGEMENT" )
public class IndicatorsJspBean extends MVCAdminJspBean implements UsingElasticConnection
{
    //Templates
    private static final String TEMPLATE_INDICATORS = "/admin/plugins/identitystore/indicators.html";

    //Parameters

    //Properties
    private static final String PROPERTY_PAGE_TITLE_INDICATORS = "";
    private final String PROPERTY_CURRENT_INDEX_ALIAS = AppPropertiesService.getProperty( "identitystore.elastic.client.identities.alias", "identities-alias" );

    //Markers
    private static final String MARKER_COUNT_IDENTITIES = "count_identities";
    private static final String MARKER_COUNT_DELETED_IDENTITIES = "count_deleted_identities";
    private static final String MARKER_COUNT_MERGED_IDENTITIES = "count_merged_identities";
    private static final String MARKER_COUNT_MONPARIS_IDENTITIES = "count_monparis_identities";
    private static final String MARKER_COUNT_UNMERGED_NO_ATTRIBUTES_IDENTITIES = "count_unmerged_no_attributes_identities";
    private static final String MARKER_LIST_ATTRIBUTES_IDENTITIES = "list_attributes_identities";
    private static final String MARKER_COUNT_INDEXED_IDENTITIES = "count_indexed_identities";
    //Views
    private static final String VIEW_INDICATORS = "viewIndicators";

    //Actions
    private static final String ACTION_ATTRIBUTES_BY_IDENTITIES = "attributes_by_identities";
    private static final String ACTION_COUNT_UNMERGED_NO_ATTRIBUTES = "count_unmerged_no_attributes";
    private static final String ACTION_COUNT_ACTIONS_BY_TIME = "count_actions_by_time";

    //Infos

    // Session variable to store working values

    @View( value = VIEW_INDICATORS, defaultView = true )
    public String getViewIndicators( HttpServletRequest request ) throws ElasticClientException
    {
        Integer countIdentities = IdentityHome.getCountIdentities();
        Integer countDeletedIdentities = IdentityHome.getCountDeletedIdentities(true);
        Integer countMergedIdentities = IdentityHome.getCountMergedIdentities(true);
        Integer countMonparisIdentities = IdentityHome.getCountActiveMonParisdentities(true);
        final IIdentityIndexer identityIndexer = this.createIdentityIndexer( );
        String countIndexedIdentities = "";
        if(identityIndexer.isAlive())
        {
            countIndexedIdentities = identityIndexer.getIndexedIdentitiesNumber(PROPERTY_CURRENT_INDEX_ALIAS);
        }

        Map<String, Object> model = new HashMap<>();
        model.put( MARKER_COUNT_IDENTITIES, countIdentities);
        model.put( MARKER_COUNT_DELETED_IDENTITIES, countDeletedIdentities);
        model.put( MARKER_COUNT_MERGED_IDENTITIES, countMergedIdentities);
        model.put( MARKER_COUNT_MONPARIS_IDENTITIES, countMonparisIdentities);
        model.put( MARKER_COUNT_INDEXED_IDENTITIES, countIndexedIdentities);

        return getPage( PROPERTY_PAGE_TITLE_INDICATORS, TEMPLATE_INDICATORS, model );
    }

    @Action( ACTION_ATTRIBUTES_BY_IDENTITIES )
    public String getAttributesByIdentities( HttpServletRequest request )
    {
        Map<Integer, Integer> attributesByIdentities = IdentityHome.getCountAttributesByIdentities( );
        String result = "";
        for( Map.Entry<Integer, Integer> entry : attributesByIdentities.entrySet( ) ){
            result += "<tr><td><span>"
                    + entry.getKey( )
                    + "</span></td><td><span>"
                    + entry.getValue( )
                    + "</span></td></tr>";
        }

        final HashMap<String, String> additionalParameters = new HashMap<>( );
        additionalParameters.put( "response", result );
        return redirect(request, null, additionalParameters);
    }

    @Action( ACTION_COUNT_UNMERGED_NO_ATTRIBUTES )
    public String countUnmergedNoAttributes( HttpServletRequest request )
    {
        final HashMap<String, String> additionalParameters = new HashMap<>( );
        additionalParameters.put( "response", IdentityHome.getCountUnmergedIdentitiesWithoutAttributes().toString() );
        return redirect(request, null, additionalParameters);
    }

    @Action( ACTION_COUNT_ACTIONS_BY_TIME )
    public String countActionsByTime( HttpServletRequest request )
    {
        int data = Integer.parseInt(request.getParameter("duration"));
        String result = "";
        List<IndicatorsActionsType> listIndicators = IdentityHome.getActionsTypesDuringInterval(data);
        if(!listIndicators.isEmpty())
        {
            for (IndicatorsActionsType action : listIndicators)
            {
                result += "<tr><td><span>"
                        + IdentityChangeType.valueOf( action.getChangeType() )
                        + "</span></td><td><span>"
                        + action.getChangeStatus()
                        + "</span></td><td><span>"
                        + action.getAuthorType()
                        + "</span></td><td><span>"
                        + action.getClientCode()
                        + "</span></td><td><span>"
                        + action.getCountActions()
                        + "</span></td></tr>";
            }
        }
        else{
            result += "<tr><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td></tr>";
        }

        final HashMap<String, String> additionalParameters = new HashMap<>( );
        additionalParameters.put( "response", result );
        return redirect(request, null, additionalParameters);
    }
}

package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service.IIdentityIndexer;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.UsingElasticConnection;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
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

    //Infos

    // Session variable to store working values

    @View( value = VIEW_INDICATORS, defaultView = true )
    public String getViewIndicators( HttpServletRequest request ) throws ElasticClientException
    {
        Integer countIdentities = IdentityHome.getCountIdentities();
        Integer countDeletedIdentities = IdentityHome.getCountDeletedIdentities(true);
        Integer countMergedIdentities = IdentityHome.getCountMergedIdentities(true);
        Integer countMonparisIdentities = IdentityHome.getCountActiveMonParisdentities(true);
        //Integer countUnmergedNoAttrIdentities = IdentityHome.getCountUnmergedIdentitiesWithoutAttributes();
        //Map<Integer, Integer> attributesByIdentities = IdentityHome.getCountAttributesByIdentities();
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
        //model.put( MARKER_COUNT_UNMERGED_NO_ATTRIBUTES_IDENTITIES, countUnmergedNoAttrIdentities);
        //model.put( MARKER_LIST_ATTRIBUTES_IDENTITIES, attributesByIdentities);
        model.put( MARKER_COUNT_INDEXED_IDENTITIES, countIndexedIdentities);

        return getPage( PROPERTY_PAGE_TITLE_INDICATORS, TEMPLATE_INDICATORS, model );
    }
}

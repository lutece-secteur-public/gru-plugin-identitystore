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

import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.web.util.LocalizedPaginator;
import fr.paris.lutece.util.html.Paginator;
import fr.paris.lutece.util.url.UrlItem;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ManageIdentities JSP Bean abstract class for JSP Bean
 */
public abstract class ManageIdentitiesJspBean extends MVCAdminJspBean
{
    // Rights
    public static final String RIGHT_MANAGEIDENTITIES = "IDENTITYSTORE_MANAGEMENT";

    // Properties
    private static final String PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE = "identitystore.listItems.itemsPerPage";

    // Parameters
    private static final String PARAMETER_PAGE_INDEX = "page_index";

    // Markers
    private static final String MARK_PAGINATOR = "paginator";
    private static final String MARK_NB_ITEMS_PER_PAGE = "nb_items_per_page";

    // Infos
    public static final String QUERY_PARAM_CUID = "cuid";
    public static final String QUERY_PARAM_GUID = "guid";
    public static final String QUERY_PARAM_INSEE_CITY = "insee_city";
    public static final String QUERY_PARAM_INSEE_COUNTRY = "insee_country";
    public static final String QUERY_PARAM_EMAIL = "email";
    public static final String QUERY_PARAM_GENDER = "gender";
    public static final String QUERY_PARAM_FAMILY_NAME = "family_name";
    public static final String QUERY_PARAM_PREFERRED_USERNAME = "preferred_username";
    public static final String QUERY_PARAM_FIRST_NAME = "first_name";
    public static final String QUERY_PARAM_BIRTHDATE = "birthdate";
    public static final String QUERY_PARAM_INSEE_BIRTHPLACE_LABEL = "insee_birthplace_label";
    public static final String QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL = "insee_birthcountry_label";
    public static final String QUERY_PARAM_PHONE = "phone";
    public static final String QUERY_PARAM_DATASOURCE = "datasource";

    // Variables
    private final int _nDefaultItemsPerPage = AppPropertiesService.getPropertyInt( PROPERTY_DEFAULT_LIST_ITEM_PER_PAGE, 50 );
    private String _strCurrentPageIndex;
    private int _nItemsPerPage;

    /**
     * Return a model that contains the list and paginator infos
     *
     * @param request
     *            The HTTP request
     * @param strBookmark
     *            The bookmark
     * @param list
     *            The list of item
     * @param strManageJsp
     *            The JSP
     * @return The model
     */
    protected Map<String, Object> getPaginatedListModel( HttpServletRequest request, String strBookmark, List list, String strManageJsp )
    {
        _strCurrentPageIndex = Paginator.getPageIndex( request, Paginator.PARAMETER_PAGE_INDEX, _strCurrentPageIndex );
        _nItemsPerPage = Paginator.getItemsPerPage( request, Paginator.PARAMETER_ITEMS_PER_PAGE, _nItemsPerPage, _nDefaultItemsPerPage );

        final UrlItem url = new UrlItem( strManageJsp );
        final Map<String, String> queryParameters = this.getQueryParameters( request );
        queryParameters.forEach( url::addParameter );
        final String strUrl = url.getUrl( );

        // PAGINATOR
        final LocalizedPaginator paginator = new LocalizedPaginator( list, _nItemsPerPage, strUrl, PARAMETER_PAGE_INDEX, _strCurrentPageIndex, getLocale( ) );

        final Map<String, Object> model = getModel( );

        model.put( MARK_NB_ITEMS_PER_PAGE, "" + _nItemsPerPage );
        model.put( MARK_PAGINATOR, paginator );
        model.put( strBookmark, paginator.getPageItems( ) );

        return model;
    }

    protected Map<String, String> getQueryParameters( final HttpServletRequest request )
    {
        final Map<String, String> parameters = new HashMap<>( );
        final String cuid = request.getParameter( QUERY_PARAM_CUID );
        if ( cuid != null )
        {
            parameters.put( QUERY_PARAM_CUID, cuid );
        }
        final String guid = request.getParameter( QUERY_PARAM_GUID );
        if ( guid != null )
        {
            parameters.put( QUERY_PARAM_GUID, guid );
        }
        final String email = request.getParameter( QUERY_PARAM_EMAIL );
        if ( email != null )
        {
            parameters.put( QUERY_PARAM_EMAIL, email );
        }
        final String gender = request.getParameter( QUERY_PARAM_GENDER );
        if ( gender != null )
        {
            parameters.put( QUERY_PARAM_GENDER, gender );
        }
        final String family_name = request.getParameter( QUERY_PARAM_FAMILY_NAME );
        if ( family_name != null )
        {
            parameters.put( QUERY_PARAM_FAMILY_NAME, family_name );
        }
        final String preferred_username = request.getParameter( QUERY_PARAM_PREFERRED_USERNAME );
        if ( preferred_username != null )
        {
            parameters.put( QUERY_PARAM_PREFERRED_USERNAME, preferred_username );
        }
        final String first_name = request.getParameter( QUERY_PARAM_FIRST_NAME );
        if ( first_name != null )
        {
            parameters.put( QUERY_PARAM_FIRST_NAME, first_name );
        }
        final String birthdate = request.getParameter( QUERY_PARAM_BIRTHDATE );
        if ( birthdate != null )
        {
            parameters.put( QUERY_PARAM_BIRTHDATE, birthdate );
        }
        final String insee_birthplace_label = request.getParameter( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL );
        if ( insee_birthplace_label != null )
        {
            parameters.put( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL, insee_birthplace_label );
        }
        final String insee_birthcountry_label = request.getParameter( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL );
        if ( insee_birthcountry_label != null )
        {
            parameters.put( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL, insee_birthcountry_label );
        }
        final String phone = request.getParameter( QUERY_PARAM_PHONE );
        if ( phone != null )
        {
            parameters.put( QUERY_PARAM_PHONE, phone );
        }
        final String datasource = request.getParameter( QUERY_PARAM_DATASOURCE );
        if ( datasource != null )
        {
            parameters.put( QUERY_PARAM_DATASOURCE, datasource );
        }
        final String insee_city = request.getParameter( QUERY_PARAM_INSEE_CITY );
        if ( insee_city != null )
        {
            parameters.put( QUERY_PARAM_INSEE_CITY, insee_city );
        }
        final String insee_country = request.getParameter( QUERY_PARAM_INSEE_COUNTRY );
        if ( insee_country != null )
        {
            parameters.put( QUERY_PARAM_INSEE_COUNTRY, insee_country );
        }
        return parameters;
    }

    protected void clearParameters( final HttpServletRequest request )
    {
        final String cuid = request.getParameter( QUERY_PARAM_CUID );
        if ( cuid != null )
        {
            request.removeAttribute( QUERY_PARAM_CUID );
        }
        final String guid = request.getParameter( QUERY_PARAM_GUID );
        if ( guid != null )
        {
            request.removeAttribute( QUERY_PARAM_GUID );
        }
        final String email = request.getParameter( QUERY_PARAM_EMAIL );
        if ( email != null )
        {
            request.removeAttribute( QUERY_PARAM_EMAIL );
        }
        final String gender = request.getParameter( QUERY_PARAM_GENDER );
        if ( gender != null )
        {
            request.removeAttribute( QUERY_PARAM_GENDER );
        }
        final String family_name = request.getParameter( QUERY_PARAM_FAMILY_NAME );
        if ( family_name != null )
        {
            request.removeAttribute( QUERY_PARAM_FAMILY_NAME );
        }
        final String preferred_username = request.getParameter( QUERY_PARAM_PREFERRED_USERNAME );
        if ( preferred_username != null )
        {
            request.removeAttribute( QUERY_PARAM_PREFERRED_USERNAME );
        }
        final String first_name = request.getParameter( QUERY_PARAM_FIRST_NAME );
        if ( first_name != null )
        {
            request.removeAttribute( QUERY_PARAM_FIRST_NAME );
        }
        final String birthdate = request.getParameter( QUERY_PARAM_BIRTHDATE );
        if ( birthdate != null )
        {
            request.removeAttribute( QUERY_PARAM_BIRTHDATE );
        }
        final String insee_birthplace_label = request.getParameter( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL );
        if ( insee_birthplace_label != null )
        {
            request.removeAttribute( QUERY_PARAM_INSEE_BIRTHPLACE_LABEL );
        }
        final String insee_birthcountry_label = request.getParameter( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL );
        if ( insee_birthcountry_label != null )
        {
            request.removeAttribute( QUERY_PARAM_INSEE_BIRTHCOUNTRY_LABEL );
        }
        final String phone = request.getParameter( QUERY_PARAM_PHONE );
        if ( phone != null )
        {
            request.removeAttribute( QUERY_PARAM_PHONE );
        }
        final String datasource = request.getParameter( QUERY_PARAM_DATASOURCE );
        if ( datasource != null )
        {
            request.removeAttribute( QUERY_PARAM_DATASOURCE );
        }
        final String insee_city = request.getParameter( QUERY_PARAM_INSEE_CITY );
        if ( insee_city != null )
        {
            request.removeAttribute( QUERY_PARAM_INSEE_CITY );
        }
        final String insee_country = request.getParameter( QUERY_PARAM_INSEE_COUNTRY );
        if ( insee_country != null )
        {
            request.removeAttribute( QUERY_PARAM_INSEE_COUNTRY );
        }
    }
}

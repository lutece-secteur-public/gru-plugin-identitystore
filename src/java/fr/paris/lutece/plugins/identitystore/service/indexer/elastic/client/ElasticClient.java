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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.Constants;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.httpaccess.InvalidResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;

import java.util.List;

public class ElasticClient
{
    private static ObjectMapper _mapper = new ObjectMapper( );
    private ElasticConnexion _connexion;
    private String _strServerUrl;

    /**
     * Constructor
     *
     * @param strServerUrl
     *            The Elastic server URL
     */
    public ElasticClient( final String strServerUrl )
    {
        _strServerUrl = strServerUrl;
        _connexion = new ElasticConnexion( );
    }

    /**
     * Basic Authentification constructor
     *
     * @param strServerUrl
     *            The Elastic server URL
     * @param strServerLogin
     *            Login
     * @param strServerPwd
     *            Password
     */
    public ElasticClient( final String strServerUrl, final String strServerLogin, final String strServerPwd )
    {
        _strServerUrl = strServerUrl;
        _connexion = new ElasticConnexion( strServerLogin, strServerPwd );
    }

    /**
     * Create a document of given type into a given index
     *
     * @param strIndex
     *            The index
     * @param object
     *            The document
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String create( final String strIndex, final Object object ) throws ElasticClientException
    {
        return create( strIndex, StringUtils.EMPTY, object );
    }

    /**
     * Create a document of given type into a given index at the given id
     *
     * @param strIndex
     *            The index
     * @param strId
     *            The document id
     * @param object
     *            The document
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String create( final String strIndex, final String strId, final Object object ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            String strJSON;

            if ( object instanceof String )
            {
                strJSON = (String) object;
            }
            else
            {
                strJSON = _mapper.writeValueAsString( object );
            }

            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            strResponse = _connexion.POST( strURI, strJSON );
        }
        catch( final JsonProcessingException | HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating object : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * perform a bulk indexing of documents : this is used for indexing thousand doc with one HTTP call
     *
     * @param strIndex
     *            the elk index name
     * @param bulkActions
     *            the actions
     * @return the reponse of Elk server
     * @throws ElasticClientException
     */
    public String indexByBulk( final String strIndex, final List<BulkAction> bulkActions ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_BULK;
            final StringBuilder requestBuilder = new StringBuilder( );
            for ( final BulkAction action : bulkActions )
            {
                requestBuilder.append(
                        "{ \"" + action.getType( ).getCode( ) + "\" : { \"_index\" : \"" + strIndex + "\", \"_id\" : \"" + action.getKey( ) + "\" } }" );
                requestBuilder.append( "\n" );
                if ( action.getType( ).hasDocument( ) && action.getDocument( ) != null )
                {
                    final Object object = action.getDocument( );
                    String strJSON;
                    if ( object instanceof String )
                    {
                        strJSON = (String) object;
                    }
                    else
                    {
                        strJSON = _mapper.writeValueAsString( object );
                    }
                    requestBuilder.append( strJSON );
                    requestBuilder.append( "\n" );
                }
            }
            strResponse = _connexion.POST( strURI, requestBuilder.toString( ) );
        }
        catch( final JsonProcessingException | HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error processing bulking request : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Delete a given index
     *
     * @param strIndex
     *            The index
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String deleteIndex( final String strIndex ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex );
            strResponse = _connexion.DELETE( strURI );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting index : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Delete a document based on its id in the index
     *
     * @param strIndex
     *            The index
     * @param strId
     *            The id
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String deleteDocument( final String strIndex, final String strId ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            strResponse = _connexion.DELETE( strURI );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting document : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Delete a documents by Query
     *
     * @param strIndex
     *            The index
     * @param strQuery
     *            The Query
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String deleteByQuery( final String strIndex, final String strQuery ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_DELETE_BY_QUERY;
            strResponse = _connexion.POST( strURI, strQuery );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting by query : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Partial Updates to Documents
     *
     * @param strIndex
     *            The index
     * @param strId
     *            The document id
     * @param object
     *            The document
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */

    public String update( final String strIndex, final String strId, final Object object ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            String strJSON;
            if ( object instanceof String )
            {
                strJSON = (String) object;
            }
            else
            {
                strJSON = _mapper.writeValueAsString( object );
            }

            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            strResponse = _connexion.POST( strURI, strJSON );
        }
        catch( final JsonProcessingException | HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error updating: " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Search
     *
     * @param strIndex
     *            The index
     * @param strId
     *            the id of the requested document
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String get( final String strIndex, final String strId ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            strResponse = _connexion.GET( strURI );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error searching object : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Check if a given index exists
     *
     * @param strIndex
     *            The index
     * @return if th index exists
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public boolean isExists( final String strIndex ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex );
            _connexion.GET( strURI );
        }
        catch( final InvalidResponseStatus ex )
        {
            if ( ex.getResponseStatus( ) == HttpStatus.SC_NOT_FOUND )
            {
                return false;
            }
            throw new ElasticClientException( "ElasticLibrary : Error getting index : " + ex.getMessage( ), ex );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error getting index : " + ex.getMessage( ), ex );
        }
        return true;
    }

    /**
     * Search a document of given type into a given index
     *
     * @param strIndex
     *            The index
     * @param search
     *            search request
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String search( final String strIndex, final Object search ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strJSON = _mapper.writeValueAsString( search );
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_SEARCH;
            strResponse = _connexion.POST( strURI, strJSON );
        }
        catch( final JsonProcessingException | HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error searching object : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * Search
     *
     * @param strIndex
     *            The index
     * @param searchRequest
     *            search request
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String search( final String strIndex, final String searchRequest ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_SEARCH;
            strResponse = _connexion.POST( strURI, searchRequest );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error searching object : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * suggest a list of document of given type into a given index The suggest is done with a _search request with size set to 0 to avoid fetch in 'hits' so be
     * careful with the JSON result
     *
     * @param strIndex
     *            The index
     * @param suggest
     *            suggest request
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    // public String suggest( String strIndex, AbstractSuggestRequest suggest ) throws ElasticClientException
    // {
    // String strResponse = StringUtils.EMPTY;
    // try
    // {
    // SearchRequest search = new SearchRequest( );
    // search.setSize( 0 );
    // search.setSearchQuery( suggest );
    // String strJSON = _mapper.writeValueAsString( search.mapToNode( ) );
    // String strURI = getURI( strIndex ) + Constants.PATH_QUERY_SEARCH;
    // strResponse = _connexion.POST( strURI, strJSON );
    // }
    // catch( JsonProcessingException | HttpAccessException ex )
    // {
    // throw new ElasticClientException( "ElasticLibrary : Error suggesting object : " + ex.getMessage( ), ex );
    // }
    // return strResponse;
    // }

    /**
     * suggest a list of document of given type into a given index The suggest is done with a _search request with size set to 0 to avoid fetch in 'hits' so be
     * careful with the JSON result
     *
     * @param strIndex
     *            The index
     * @param strJSON
     *            suggest request
     * @return The JSON response from Elastic
     * @throws ElasticClientException
     *             If a problem occurs connecting Elastic
     */
    public String suggest( final String strIndex, final String strJSON ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_SEARCH;
            strResponse = _connexion.POST( strURI, strJSON );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error suggesting object : " + ex.getMessage( ), ex );
        }
        return strResponse;
    }

    /**
     * @param strIndex
     * @param strJsonMappings
     * @return
     * @throws ElasticClientException
     */
    public String createMappings( final String strIndex, final String strJsonMappings ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex );
            strResponse = _connexion.PUT( strURI, strJsonMappings );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating mappings : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * @param strIndex
     * @param strJsonSettings
     * @return
     * @throws ElasticClientException
     */
    public String updateSettings( final String strIndex, final String strJsonSettings ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + "_settings";
            strResponse = _connexion.PUT( strURI, strJsonSettings );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error updating settings : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * @param strIndex
     * @param alias
     * @return
     * @throws ElasticClientException
     */
    public String createAlias( final String strIndex, final String alias ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + "_alias" + Constants.URL_PATH_SEPARATOR + alias;
            strResponse = _connexion.POST( strURI, "" );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating alias : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * @param alias
     * @return
     * @throws ElasticClientException
     */
    public String getAlias( final String alias ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( "" ) + "_alias" + Constants.URL_PATH_SEPARATOR + alias;
            strResponse = _connexion.GET( strURI );
        }
        catch( final Exception ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating alias : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * @param strIndex
     * @param alias
     * @return
     * @throws ElasticClientException
     */
    public String deleteAlias( final String strIndex, final String alias ) throws ElasticClientException
    {
        String strResponse;
        try
        {
            final String strURI = getURI( strIndex ) + "_alias" + Constants.URL_PATH_SEPARATOR + alias;
            strResponse = _connexion.DELETE( strURI );
        }
        catch( final HttpAccessException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting alias : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * Build the URI of a given index
     *
     * @param strIndex
     *            The index name
     * @return The URI
     */
    private String getURI( String strIndex )
    {
        String strURI = _strServerUrl;
        strURI = ( strURI.endsWith( Constants.URL_PATH_SEPARATOR ) ) ? strURI : strURI + Constants.URL_PATH_SEPARATOR;
        if ( StringUtils.isNotEmpty( strIndex ) )
        {
            strURI = ( ( strIndex.endsWith( Constants.URL_PATH_SEPARATOR ) ) ? strURI + strIndex : strURI + strIndex + Constants.URL_PATH_SEPARATOR );
        }

        return strURI;
    }

    /**
     * Build Json to partial update
     *
     * @param strJson
     *            The json
     * @return json
     */
    private String buildJsonToPartialUpdate( String strJson )
    {

        StringBuilder sbuilder = new StringBuilder( );
        sbuilder.append( "{ \"doc\" : " );
        sbuilder.append( strJson );
        sbuilder.append( "}" );

        return sbuilder.toString( );
    }

    public boolean isAlive( )
    {
        try
        {
            final String strURI = getURI( "" ) + "_cat" + Constants.URL_PATH_SEPARATOR + "health";
            _connexion.GET( strURI );
        }
        catch( Exception ex )
        {
            return false;
        }
        return true;

    }
}

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
package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.Constants;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.BulkAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias.AliasActions;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.request.MultiSearchAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Response;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.search.model.inner.response.Responses;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

public class ElasticClient
{
    private static final ObjectMapper _mapper = new ObjectMapper( ).disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    private static final String SETTINGS_PATH = "_settings";
    private final ElasticConnexion _connexion;
    private final String _strServerUrl;

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
    public void create( final String strIndex, final String strId, final Object object ) throws ElasticClientException
    {
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
            _connexion.POST( strURI, strJSON );
        }
        catch( final ElasticConnexionException | JsonProcessingException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating object : " + ex.getMessage( ), ex );
        }
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
    public void indexByBulk( final String strIndex, final List<BulkAction> bulkActions ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_BULK;
            final StringBuilder requestBuilder = new StringBuilder( );
            for ( final BulkAction action : bulkActions )
            {
                requestBuilder.append( "{ \"" ).append( action.getType( ).getCode( ) ).append( "\" : { \"_index\" : \"" ).append( strIndex )
                        .append( "\", \"_id\" : \"" ).append( action.getKey( ) ).append( "\" } }" );
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
            this._connexion.POST( strURI, requestBuilder.toString( ) );
        }
        catch( final ElasticConnexionException | JsonProcessingException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error processing bulking request : " + ex.getMessage( ), ex );
        }
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
    public void deleteIndex( final String strIndex ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex );
            this._connexion.DELETE( strURI );
        }
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting index : " + ex.getMessage( ), ex );
        }
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
    public void deleteDocument( final String strIndex, final String strId ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            this._connexion.DELETE( strURI );
        }
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting document : " + ex.getMessage( ), ex );
        }
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

    public void update( final String strIndex, final String strId, final Object object ) throws ElasticClientException
    {
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
            this._connexion.POST( strURI, strJSON );
        }
        catch( final ElasticConnexionException | JsonProcessingException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error updating: " + ex.getMessage( ), ex );
        }
    }

    /**
     * @param strIndex
     *            the index of the document
     * @param strId
     *            the id of the document
     * @return the document if any
     * @throws ElasticClientException
     */
    public String get( final String strIndex, final String strId ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + "_doc" + Constants.URL_PATH_SEPARATOR + strId;
            return _connexion.GET( strURI );
        }
        catch( final Exception ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating alias : " + ex.getMessage( ), ex );
        }

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
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error getting index : " + ex.getMessage( ), ex );
        }
        return true;
    }

    /**
     * Check if a given index is not in read only mode
     *
     * @param strIndex
     *            The index
     * @return true if the index exists and is writable
     */
    public boolean isWriteable( final String strIndex )
    {
        try
        {
            final String strURI = getURI( strIndex ) + SETTINGS_PATH;
            final String response = _connexion.GET( strURI );
            final JsonNode rootNode = _mapper.readTree( response );

            if ( rootNode != null )
            {
                final JsonNode readOnlyNode = rootNode.path( "settings" ).path( "index" ).path( "blocks" ).path( "read_only" );
                if ( readOnlyNode != null && readOnlyNode.asBoolean( ) )
                {
                    return false;
                }
            }
        }
        catch( final ElasticConnexionException | JsonProcessingException ex )
        {
            AppLogService.error( "ElasticLibrary : Error trying to determine if index " + strIndex + " is writeable : " + ex.getMessage( ), ex );
            return false;
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
    public Response search( final String strIndex, final Object search ) throws ElasticClientException
    {
        try
        {
            final String strJSON = _mapper.writeValueAsString( search );
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_SEARCH;
            return _connexion.SEARCH( strURI, strJSON );
        }
        catch( final IOException | ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error searching object : " + ex.getMessage( ), ex );
        }
    }

    /**
     * perform a multi search of documents : used to perform several query with a single http call
     *
     * @param strIndex
     *            the elk index name
     * @param searchActions
     *            the actions
     * @return the reponse of Elk server
     * @throws ElasticClientException
     */
    public Responses multiSearch( final String strIndex, final List<MultiSearchAction> searchActions ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + Constants.PATH_QUERY_MULTI_SEARCH;
            final StringBuilder requestBuilder = new StringBuilder( );
            for ( final MultiSearchAction action : searchActions )
            {
                switch( action.getType( ) )
                {
                    case INDEX:
                        requestBuilder.append( "{ \"" ).append( action.getType( ).getCode( ) ).append( "\" : " ).append( action.getIndex( ) ).append( "\" }" );
                        requestBuilder.append( "\n" );
                        break;
                    case QUERY:
                        final String strJSON = _mapper.writeValueAsString( action.getQuery( ) );
                        requestBuilder.append( "{}" ).append( "\n" ).append( strJSON ).append( "\n" );
                        break;
                    default:
                        break;
                }
            }
            return this._connexion.MSEARCH( strURI, requestBuilder.toString( ) );
        }
        catch( final IOException | ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error processing multi search request : " + ex.getMessage( ), ex );
        }
    }

    /**
     * @param strIndex
     * @param strJsonMappings
     * @throws ElasticClientException
     */
    public void createMappings( final String strIndex, final String strJsonMappings ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex );
            _connexion.PUT( strURI, strJsonMappings );
        }
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating mappings : " + ex.getMessage( ), ex );
        }

    }

    /**
     * @param strIndex
     * @param strJsonSettings
     * @return
     * @throws ElasticClientException
     */
    public void updateSettings( final String strIndex, final String strJsonSettings ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + SETTINGS_PATH;
            this._connexion.PUT( strURI, strJsonSettings );
        }
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error updating settings : " + ex.getMessage( ), ex );
        }

    }

    /**
     * @param strIndex
     * @param alias
     * @return
     * @throws ElasticClientException
     */
    public void addAliasOnIndex( final AliasActions aliasActions ) throws ElasticClientException
    {
        try
        {
            final String strJSON = _mapper.writeValueAsString( aliasActions );
            final String strURI = getURI( "" ) + "_aliases";
            this._connexion.POST( strURI, strJSON );
        }
        catch( final ElasticConnexionException | JsonProcessingException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error creating alias : " + ex.getMessage( ), ex );
        }

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
            throw new ElasticClientException( "ElasticLibrary : Error getting alias : " + ex.getMessage( ), ex );
        }
        return strResponse;

    }

    /**
     * Check if a given alias exists
     *
     * @param strAlias
     *            The indexThe alias
     * @return if th index exists
     */
    public boolean aliasExists( final String strAlias )
    {
        try
        {
            final String strURI = getURI( "" ) + "_alias" + Constants.URL_PATH_SEPARATOR + strAlias;
            _connexion.HEAD( strURI );
        }
        catch( final ElasticConnexionException ex )
        {
            return false;
        }
        return true;
    }

    /**
     * @param strIndex
     * @param alias
     * @return
     * @throws ElasticClientException
     */
    public void deleteAlias( final String strIndex, final String alias ) throws ElasticClientException
    {
        try
        {
            final String strURI = getURI( strIndex ) + "_alias" + Constants.URL_PATH_SEPARATOR + alias;
            this._connexion.DELETE( strURI );
        }
        catch( final ElasticConnexionException ex )
        {
            throw new ElasticClientException( "ElasticLibrary : Error deleting alias : " + ex.getMessage( ), ex );
        }

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

    public boolean isAlive( )
    {
        try
        {
            final String strURI = getURI( "" ) + "_cat" + Constants.URL_PATH_SEPARATOR + "health";
            _connexion.GET( strURI );
        }
        catch( Exception ex )
        {
            AppLogService.error( "ElasticClient : Error checking if ElasticSearch is alive", ex );
            return false;
        }
        return true;

    }
}

package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClient;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionHome;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.business.IndexActionType;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.FailRequestObject;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias.AliasAction;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.internal.alias.AliasActions;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Iterator;
import java.util.UUID;

public class FailRequestIndexer implements IFailRequestIndexer
{
    private final String CURRENT_INDEX_ALIAS = AppPropertiesService.getProperty( "identitystore.elastic.client.identities.alias", "identities-alias" );
    private final String PROPERTY_COUNT = "count";

    private final static ObjectMapper _mapper = new ObjectMapper( ).disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    private final ElasticClient _elasticClient;

    public FailRequestIndexer (final String strServerUrl, final String strLogin, final String strPassword )
    {
        this._elasticClient = new ElasticClient( strServerUrl, strLogin, strPassword );
    }

    public FailRequestIndexer(final String strServerUrl )
    {
        this._elasticClient = new ElasticClient( strServerUrl );
    }


    @Override
    public void create(FailRequestObject request)
    {
        try
        {
            if ( !this._elasticClient.isExists( CURRENT_INDEX_ALIAS ) )
            {
                final String newIndex = "identities-" + UUID.randomUUID( );
                this.initIndex( newIndex );
                this.addAliasOnIndex( newIndex, CURRENT_INDEX_ALIAS );
            }

            this._elasticClient.create( CURRENT_INDEX_ALIAS, request.getConcernedRequest(), request );
            AppLogService.debug( "Indexed document: " + request.getConcernedRequest( ) );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( request.getConcernedRequest( ), IndexActionType.CREATE );
            AppLogService.error( "Failed to index (creation) request " + request.getConcernedRequest( ), e );
        }
    }

    @Override
    public void delete(String documentId)
    {
        try
        {
            this._elasticClient.deleteDocument( CURRENT_INDEX_ALIAS, documentId );
            AppLogService.debug( "Removed request : " + documentId );
        }
        catch( final ElasticClientException e )
        {
            this.handleError( documentId, IndexActionType.DELETE );
            AppLogService.error( "Failed to remove request " + documentId, e );
        }
    }

    @Override
    public String getIndexedIdentitiesNumber( String index ) throws ElasticClientException
    {
        try
        {
            String countIndexed =  this._elasticClient.getIndexedIdentitiesNumber(index);
            JsonObject jsonObject = JsonParser.parseString(countIndexed).getAsJsonObject();

            return jsonObject.get(PROPERTY_COUNT).getAsString();
        }
        catch(ElasticClientException e )
        {
            AppLogService.error( "Unexpected error occurred while managing ES alias.", e );
            throw new AppException( "Unexpected error occurred while managing ES alias.", e );
        }
    }

    @Override
    public void addAliasOnIndex(String newIndex, String alias)
    {
        try
        {
            final AliasActions actions = new AliasActions( );
            final AliasAction remove = new AliasAction( );
            remove.setName( "remove" );
            remove.setAlias( alias );
            remove.setIndex( "*" );
            actions.addAction( remove );

            final AliasAction add = new AliasAction( );
            add.setName( "add" );
            add.setAlias( alias );
            add.setIndex( newIndex );
            actions.addAction( add );

            this._elasticClient.addAliasOnIndex( actions );
        }
        catch( final ElasticClientException e )
        {
            AppLogService.error( "Unexpected error occurred while managing ES alias.", e );
            throw new AppException( "Unexpected error occurred while managing ES alias.", e );
        }
    }

    @Override
    public String getIndexBehindAlias(String alias)
    {
        try
        {
            final String response = this._elasticClient.getAlias( alias );
            final JsonNode node = _mapper.readTree( response );

            if ( node != null )
            {
                final Iterator<String> iterator = node.fieldNames( );
                if ( iterator != null && iterator.hasNext( ) )
                {
                    return iterator.next( );
                }
            }
        }
        catch(ElasticClientException | JsonProcessingException e )
        {
            AppLogService.error( "Failed to get index behind alias", e );
            return null;
        }
        return null;
    }

    @Override
    public boolean isAlive()
    {
        return _elasticClient.isAlive( );
    }

    @Override
    public boolean indexExists(String index)
    {
        try
        {
            return _elasticClient.isExists( index );
        }
        catch( final ElasticClientException e )
        {
            AppLogService.error( "Failed to check index existence", e );
            return false;
        }
    }

    @Override
    public boolean isIndexWriteable(String index)
    {
        return _elasticClient.isWriteable( index );
    }

    @Override
    public boolean aliasExists(String index)
    {
        return false;
    }

    @Override
    public void initIndex(String index) throws ElasticClientException
    {

    }

    @Override
    public void deleteIndex(String index) throws ElasticClientException
    {

    }

    @Override
    public void makeIndexReadOnly(String index) throws ElasticClientException
    {

    }

    @Override
    public void removeIndexReadOnly(String index) throws ElasticClientException
    {

    }

    private void handleError( final String documentId, final IndexActionType actionType )
    {
        final IndexAction indexAction = new IndexAction( actionType, documentId );
        IndexActionHome.create( indexAction );
    }
}

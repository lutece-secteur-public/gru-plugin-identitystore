package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.client.ElasticClientException;
import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.FailRequestObject;

public interface IFailRequestIndexer
{
    /* Documents API */
    void create(final FailRequestObject request );

    void delete( final String documentId );

    String getIndexedIdentitiesNumber( String index ) throws ElasticClientException;

    void addAliasOnIndex( String newIndex, String alias );

    String getIndexBehindAlias( String alias );

    /* Cluster API */
    boolean isAlive( );

    /* Index API */
    boolean indexExists( final String index );

    boolean isIndexWriteable( final String index );

    boolean aliasExists( final String index );

    void initIndex( final String index ) throws ElasticClientException;

    void deleteIndex( String index ) throws ElasticClientException;

    void makeIndexReadOnly( final String index ) throws ElasticClientException;

    void removeIndexReadOnly( final String index ) throws ElasticClientException;
}

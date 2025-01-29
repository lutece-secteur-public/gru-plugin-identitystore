package fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.service;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.model.FailRequestObject;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ChangeResponse;

import java.sql.Timestamp;

public class FailRequestService
{
    IFailRequestIndexer _failRequestIndexer;

    public FailRequestService(IFailRequestIndexer _failRequestIndexer)
    {
        this._failRequestIndexer = _failRequestIndexer;
    }

    public void failIdentityRequest ( String requestType, ChangeResponse response )
    {
        FailRequestObject failRequestObject = new FailRequestObject( );

        failRequestObject.setConcernedResponse( response.getClass( ).getCanonicalName( ) );
        failRequestObject.setErrorType( response.getStatus( ).getType( ).name( ) );
        failRequestObject.setErrorMessage( response.getStatus( ).getMessage( ) );
        failRequestObject.setErrorCode( response.getStatus( ).getHttpCode( ) );
        failRequestObject.setCreationDate( new Timestamp( System.currentTimeMillis( ) ) );

        failRequestObject.setConcernedRequest( requestType );

        _failRequestIndexer.create(failRequestObject);
    }

}

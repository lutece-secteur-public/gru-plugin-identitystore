package fr.paris.lutece.plugins.identitystore.v3.web.rs.error;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.error.ErrorResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.rest.service.mapper.GenericUncaughtExceptionMapper;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class UncaughtResourceConsistencyExceptionMapper extends GenericUncaughtExceptionMapper<ResourceConsistencyException, ResponseDto> {

    public static final String ERROR_REQUEST_RESOURCE_CONFLICT = "The sent request is conflicting with existing resources";

    @Override
    protected Response.Status getStatus(final ResourceConsistencyException e) {
        if ( e.getResponse( ) != null )
        {
            return Response.Status.fromStatusCode( e.getResponse( ).getStatus( ).getHttpCode( ) );
        }
        return Response.Status.CONFLICT;
    }

    @Override
    protected ResponseDto getBody(final ResourceConsistencyException e) {
        if ( e.getResponse( ) != null )
        {
            return e.getResponse( );
        }
        final ErrorResponse response = new ErrorResponse( );
        response.setStatus(ResponseStatusFactory.conflict().setMessage(ERROR_REQUEST_RESOURCE_CONFLICT + " :: " + e.getMessage())
                                                .setMessageKey(Constants.PROPERTY_REST_ERROR_UPDATE_CONFLICT));
        return response;
    }

    @Override
    protected String getType() {
        return MediaType.APPLICATION_JSON;
    }
}

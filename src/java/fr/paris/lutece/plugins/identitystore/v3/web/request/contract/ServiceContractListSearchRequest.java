package fr.paris.lutece.plugins.identitystore.v3.web.request.contract;

import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.ServiceContractRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.sql.Date;
import java.util.List;

public class ServiceContractListSearchRequest extends AbstractIdentityStoreAppCodeRequest {

    private final String strLoadDetails;
    private final String strMinEndDate;

    private boolean loadDetails;
    private Date minEndDate;

    public ServiceContractListSearchRequest(final String strLoadDetails, final String strMinEndDate, final String strClientCode, final String strAppCode, final String authorName, final String authorType) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this.strLoadDetails = strLoadDetails;
        this.strMinEndDate = strMinEndDate;
    }

    @Override
    protected void fetchResources() throws ResourceNotFoundException, ClientAuthorizationException {
        // do nothing
    }

    @Override
    protected void validateRequestFormat() throws RequestFormatException {
        loadDetails = ServiceContractRequestValidator.getInstance().checkAndParseLoadDetails( strLoadDetails );
        minEndDate = ServiceContractRequestValidator.getInstance().checkAndParseMinEndDate( strMinEndDate );
    }

    @Override
    protected void validateClientAuthorization() throws ClientAuthorizationException {
        // TODO no authorization in service contract for that
    }

    @Override
    protected void validateResourcesConsistency() throws ResourceConsistencyException {
        // do nothing
    }

    @Override
    protected void formatRequestContent() throws RequestContentFormattingException {
        // do nothing
    }

    @Override
    protected void checkDuplicatesConsistency() throws DuplicatesConsistencyException {
        // do nothing
    }

    @Override
    protected ResponseDto doSpecificRequest() throws IdentityStoreException {
        final ServiceContractsSearchResponse response = new ServiceContractsSearchResponse( );

        final List<ServiceContractDto> result = ServiceContractService.instance().searchServiceContracts(loadDetails, minEndDate);

        response.setStatus(ResponseStatusFactory.ok().setMessageKey(Constants.PROPERTY_REST_INFO_SUCCESSFUL_OPERATION));
        response.getServiceContracts( ).addAll( result );

        return response;
    }
}

package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractSearchResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class ServiceContractGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get service contract by ID";
        final ServiceContract serviceContract = ServiceContractService.instance().getActiveServiceContract(H_CLIENT_CODE);
        final int targetContractId = serviceContract.getId();
        try {
            final ServiceContractGetRequest request = new ServiceContractGetRequest(targetContractId, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractSearchResponse response = (ServiceContractSearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : service contract in response is null", response.getServiceContract());
            assertEquals(strTestCase + " : service contract in response doesn't have the expected ID", targetContractId, response.getServiceContract().getId( ) );
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get service contract by ID without ID";
        try {
            final ServiceContractGetRequest request = new ServiceContractGetRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get service contract by ID with unknown ID";
        try {
            final ServiceContractGetRequest request = new ServiceContractGetRequest(-99, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }
}

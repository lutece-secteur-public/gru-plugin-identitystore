package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractListGetAllRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

public class ServiceContractListGetAllRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get all service contracts";
        try {
            final ServiceContractListGetAllRequest request = new ServiceContractListGetAllRequest(H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractsSearchResponse response = (ServiceContractsSearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : service contract list in response is null", response.getServiceContracts());
            assertFalse(strTestCase + " : service contract list in response is empty", response.getServiceContracts().isEmpty( ) );
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        // no-op
    }
}

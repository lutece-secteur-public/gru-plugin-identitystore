package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractsSearchResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class ServiceContractListGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get all service contract list by client code";
        final String targetClientCode = H_CLIENT_CODE;
        try {
            final ServiceContractListGetRequest request = new ServiceContractListGetRequest(targetClientCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractsSearchResponse response = (ServiceContractsSearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : service contract list in response is null", response.getServiceContracts());
            assertFalse(strTestCase + " : service contract list in response is empty", response.getServiceContracts().isEmpty( ) );
            assertTrue(strTestCase + " : service contracts in response don't all have the expected client code", response.getServiceContracts().stream().allMatch(c -> c.getClientCode().equals(targetClientCode)));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get all service contract list without client code";
        String targetClientCode = null;
        try{
            final ServiceContractListGetRequest request = new ServiceContractListGetRequest(targetClientCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get all service contract list with empty client code";
        targetClientCode = "";
        try{
            final ServiceContractListGetRequest request = new ServiceContractListGetRequest(targetClientCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get all service contract list with unknown client code";
        targetClientCode = "UnknownClientCode";
        try{
            final ServiceContractListGetRequest request = new ServiceContractListGetRequest(targetClientCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }
}

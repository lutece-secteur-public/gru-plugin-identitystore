package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.util.concurrent.TimeUnit;

public class ServiceContractUpdateRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final ServiceContractDto mockContract = createMockContract();

        final String strTestCase = "1.1. Update service contract";
        final String newName = "ServiceContractUpdateRequestTestNewName";
        mockContract.setName(newName);
        try{
            final ServiceContractUpdateRequest request = new ServiceContractUpdateRequest(mockContract, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractChangeResponse response = (ServiceContractChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + " : service contract in response is null", response.getServiceContract());
            assertEquals(strTestCase + " : service contract in response doesn't have the expected name", newName, response.getServiceContract().getName( ));

            TimeUnit.SECONDS.sleep(1);
            assertEquals(strTestCase + " : service contract in database doen't have the expected name", newName, ServiceContractHome.findByPrimaryKey(mockContract.getId()).get().getName());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            ServiceContractHome.remove(mockContract.getId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final ServiceContractDto mockContract = createMockContract();
        try {
            String strTestCase = "2.1. Update service contract without ID and contract";
            try {
                final ServiceContractUpdateRequest request =
                        new ServiceContractUpdateRequest(null, null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_PROVIDED_SERVICE_CONTRACT_NULL);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Update service contract without ID";
            mockContract.setName("NewName");
            try {
                final ServiceContractUpdateRequest request =
                        new ServiceContractUpdateRequest(mockContract, null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_SERVICE_CONTRACT_ID);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Update service contract without contract";
            try {
                final ServiceContractUpdateRequest request =
                        new ServiceContractUpdateRequest(null, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_PROVIDED_SERVICE_CONTRACT_NULL);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Update service contract without one mandatory field";
            mockContract.setMoaContactName(null);
            try {
                final ServiceContractUpdateRequest request =
                        new ServiceContractUpdateRequest(mockContract, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_WITHOUT_MANDATORY_FIELDS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Update service contract with unknown ID";
            mockContract.setMoaContactName("moaContactName");
            try {
                final ServiceContractUpdateRequest request =
                        new ServiceContractUpdateRequest(mockContract, -99, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_NOT_FOUND);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            ServiceContractHome.remove(mockContract.getId());
        }
    }

    private ServiceContractDto createMockContract() throws Exception {
        final ServiceContractDto contract = ServiceContractCreateRequestTest.getServiceContractDtoForCreate();
        contract.setName("ServiceContractUpdateRequestTest");
        final ServiceContractCreateRequest createRequest = new ServiceContractCreateRequest(contract, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
        final ServiceContractDto mockContract = ((ServiceContractChangeResponse) createRequest.doRequest()).getServiceContract();
        TimeUnit.SECONDS.sleep(1);
        return mockContract;
    }

}

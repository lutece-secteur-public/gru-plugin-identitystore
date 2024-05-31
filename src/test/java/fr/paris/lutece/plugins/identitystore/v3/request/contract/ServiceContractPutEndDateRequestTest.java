package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractPutEndDateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class ServiceContractPutEndDateRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final ServiceContractDto mockContract = createMockContract();

        final String strTestCase = "1.1. Close service contract";
        final int targetContractId = mockContract.getId();
        final Date targetEndDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse("2030-01-01").getTime());
        final ServiceContractDto contract = new ServiceContractDto();
        contract.setEndingDate(targetEndDate);
        try {
            final ServiceContractPutEndDateRequest request = new ServiceContractPutEndDateRequest(contract, targetContractId, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractChangeResponse response = (ServiceContractChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + " : service contract in response is null", response.getServiceContract());
            assertEquals(strTestCase + " : service contract in response doesn't have the expected end date", targetEndDate, response.getServiceContract().getEndingDate( ) );

            TimeUnit.SECONDS.sleep(1);
            assertEquals(strTestCase + " : service contract in database doesn't have the expected end date", targetEndDate, ServiceContractHome.findByPrimaryKey(targetContractId).get().getEndingDate());
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
            String strTestCase = "2.1. Close service contract without ID and contract";
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(null, null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Close service contract without ID";
            ServiceContractDto contract = new ServiceContractDto();
            contract.setEndingDate(new Date(new SimpleDateFormat("yyyy-MM-dd").parse("2030-01-01").getTime()));
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(contract, null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Close service contract without contract";
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(null, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Close service contract without end date";
            contract = new ServiceContractDto();
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(contract, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Close service contract with unknown ID";
            contract = new ServiceContractDto();
            contract.setEndingDate(new Date(new SimpleDateFormat("yyyy-MM-dd").parse("2030-01-01").getTime()));
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(contract, -99, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.6. Close service contract with end date before start date";
            contract = new ServiceContractDto();
            contract.setEndingDate(new Date(new SimpleDateFormat("yyyy-MM-dd").parse("1980-01-01").getTime()));
            try {
                final ServiceContractPutEndDateRequest request =
                        new ServiceContractPutEndDateRequest(contract, mockContract.getId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceConsistencyException.class);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            ServiceContractHome.remove(mockContract.getId());
        }
    }

    private ServiceContractDto createMockContract() throws Exception {
        final ServiceContractDto contract = ServiceContractCreateRequestTest.getServiceContractDtoForCreate();
        contract.setName("ServiceContractPutEndDateRequestTest");
        final ServiceContractCreateRequest createRequest = new ServiceContractCreateRequest(contract, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
        final ServiceContractDto mockContract = ((ServiceContractChangeResponse) createRequest.doRequest()).getServiceContract();
        TimeUnit.SECONDS.sleep(1);
        return mockContract;
    }
}

package fr.paris.lutece.plugins.identitystore.v3.request.contract;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.contract.ServiceContractCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class ServiceContractCreateRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Create service contract request";
        final ServiceContractDto contract = getServiceContractDtoForCreate();
        try {
            final ServiceContractCreateRequest request = new ServiceContractCreateRequest(contract, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ServiceContractChangeResponse response = (ServiceContractChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + " : service contract in response is null", response.getServiceContract());

            TimeUnit.SECONDS.sleep(1);
            assertNotNull(strTestCase + " : service contract not found in database after creation", ServiceContractHome.findByPrimaryKey(response.getServiceContract().getId( )).orElse(null));

            ServiceContractHome.remove(response.getServiceContract().getId( ));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Create request without service contract";
        try {
            final ServiceContractCreateRequest request = new ServiceContractCreateRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_PROVIDED_SERVICE_CONTRACT_NULL);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Create request with empty service contract";
        try {
            final ServiceContractCreateRequest request = new ServiceContractCreateRequest(new ServiceContractDto(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_WITHOUT_MANDATORY_FIELDS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Create request with unknown client code";
        ServiceContractDto contract = new ServiceContractDto();
        contract.setClientCode("unknownClientCode");
        try {
            final ServiceContractCreateRequest request = new ServiceContractCreateRequest(contract, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_APPLICATION_NOT_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.4. Create request with one missing mandatory field";
        contract = getServiceContractDtoForCreate();
        contract.setMoaContactName(null);
        try {
            final ServiceContractCreateRequest request = new ServiceContractCreateRequest(contract, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_WITHOUT_MANDATORY_FIELDS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }

    public static ServiceContractDto getServiceContractDtoForCreate() throws ParseException {
        final ServiceContractDto contract = new ServiceContractDto();
        contract.setClientCode(H_CLIENT_CODE);
        contract.setStartingDate(new Date(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01").getTime()));
        contract.setName("ServiceContractCreateRequestTest");
        contract.setMoaEntityName("moaEntityName");
        contract.setMoaContactName("moaContactName");
        contract.setMoeEntityName("moeEntityName");
        contract.setMoeResponsibleName("moeResponsibleName");
        contract.setServiceType("serviceType");
        return contract;
    }
}

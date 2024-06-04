package fr.paris.lutece.plugins.identitystore.v3.request.application;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientsGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientsSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class ClientsGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        String strTestCase = "1.1. Get client applications with existing application code";
        final String targetAppCode = H_APP_CODE;
        try {
            final ClientsGetRequest request = new ClientsGetRequest(targetAppCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ClientsSearchResponse response = (ClientsSearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : client application list in response is null", response.getClientApplications());
            assertFalse(strTestCase + " : client application list in response is empty", response.getClientApplications().isEmpty());
            assertTrue(strTestCase + " : client applications in response don't all have the expected app code", response.getClientApplications().stream().allMatch(ca -> ca.getApplicationCode().equals(targetAppCode)));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "1.2. Get client applications without application code (get all)";
        try {
            final ClientsGetRequest request = new ClientsGetRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final String strTestCase = "2.1. Get client applications with non existing application code";
        try {
            final ClientsGetRequest request = new ClientsGetRequest("NonExistingAppCode", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_CLIENT_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }
}

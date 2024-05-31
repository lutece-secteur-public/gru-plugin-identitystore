package fr.paris.lutece.plugins.identitystore.v3.request.application;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientSearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class ClientGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        String strTestCase = "1.1. Get client application with existing client code";
        final String targetClientCode = H_CLIENT_CODE;
        try {
            final ClientGetRequest request = new ClientGetRequest(targetClientCode, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ClientSearchResponse response = (ClientSearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : client application in response is null", response.getClientApplication());
            assertEquals(strTestCase + " : client application in response doesn't have the expected client code", targetClientCode, response.getClientApplication().getClientCode());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get client application without client code";
        try {
            final ClientGetRequest request = new ClientGetRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get client application with non existing client code";
        try {
            final ClientGetRequest request = new ClientGetRequest("NonExistingClientCode", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }
}

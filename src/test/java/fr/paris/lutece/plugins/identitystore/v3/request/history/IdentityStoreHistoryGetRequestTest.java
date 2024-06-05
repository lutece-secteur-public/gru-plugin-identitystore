package fr.paris.lutece.plugins.identitystore.v3.request.history;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.request.identity.AbstractIdentityRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.history.IdentityStoreHistoryGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistoryGetResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class IdentityStoreHistoryGetRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get identity history from customer ID";
        final Identity mockIdentity = this.createMockIdentityInDB();
        try {
            final IdentityStoreHistoryGetRequest request = new IdentityStoreHistoryGetRequest(mockIdentity.getCustomerId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final IdentityHistoryGetResponse response = (IdentityHistoryGetResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : history in response is null", response.getHistory());
            assertEquals(strTestCase + " : history in response is not with the expected customer ID", mockIdentity.getCustomerId(), response.getHistory().getCustomerId());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get identity history without customer ID";
        try {
            final IdentityStoreHistoryGetRequest request = new IdentityStoreHistoryGetRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_CUSTOMER_ID);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get identity history with unknown customer ID";
        try {
            final IdentityStoreHistoryGetRequest request = new IdentityStoreHistoryGetRequest("unknown-customer-id", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }
}

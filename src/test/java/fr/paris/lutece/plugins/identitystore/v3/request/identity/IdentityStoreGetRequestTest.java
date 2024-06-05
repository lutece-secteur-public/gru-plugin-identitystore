package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class IdentityStoreGetRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get identity by customer ID request";
        final Identity mockIdentity = createMockIdentityInDB();
        try {
            final IdentityStoreGetRequest request = new IdentityStoreGetRequest(mockIdentity.getCustomerId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            final IdentitySearchResponse response = (IdentitySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : identity list in response is null", response.getIdentities());
            assertFalse(strTestCase + " : identity list in response is empty", response.getIdentities().isEmpty());
            assertFalse(strTestCase + " : identity list in response contains more than one identty", response.getIdentities().size() > 1);
            assertEquals(strTestCase + " : identity in response doesn't have the same customer ID as in the request", response.getIdentities().get(0).getCustomerId(), mockIdentity.getCustomerId());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }

    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get identity request without CUID";
        try {
            final IdentityStoreGetRequest request = new IdentityStoreGetRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_CUSTOMER_ID);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get identity request with unknown CUID";
        try {
            final IdentityStoreGetRequest request = new IdentityStoreGetRequest("unknowncuid", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Get identity request with unknown client code";
        final Identity mockIdentity = createMockIdentityInDB();
        try {
            final IdentityStoreGetRequest request = new IdentityStoreGetRequest(mockIdentity.getCustomerId(), "unknown-client-code", H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_SERVICE_CONTRACT_NOT_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }

    }

}

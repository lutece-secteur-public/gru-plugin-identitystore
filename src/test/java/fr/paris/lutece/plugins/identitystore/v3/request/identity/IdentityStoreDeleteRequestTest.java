package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreDeleteRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class IdentityStoreDeleteRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Delete identity request without CUID";
        try {
            final IdentityStoreDeleteRequest request = new IdentityStoreDeleteRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_CUSTOMER_ID);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Delete identity request with unknown CUID";
        try {
            final IdentityStoreDeleteRequest request = new IdentityStoreDeleteRequest("unknowncuid", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }
}

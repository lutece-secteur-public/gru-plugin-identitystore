package fr.paris.lutece.plugins.identitystore.v3.request.referentiel;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.referentiel.AttributeKeyListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeSearchResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

public class AttributeKeyListGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get all attribute keys";
        try {
            final AttributeKeyListGetRequest request = new AttributeKeyListGetRequest(H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final AttributeSearchResponse response = (AttributeSearchResponse) executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : the attribute key list in the response is null", response.getAttributeKeys());
            assertFalse(strTestCase + " : the attribute key list in the response is empty", response.getAttributeKeys().isEmpty());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        // no-op
    }
}

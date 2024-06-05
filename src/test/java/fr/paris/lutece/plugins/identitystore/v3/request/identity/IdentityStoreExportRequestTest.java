package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.exporting.IdentityExportResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class IdentityStoreExportRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Export request";
        final Identity mockIdentity = createMockIdentityInDB();
        final IdentityExportRequest req = new IdentityExportRequest();
        req.getCuidList().add(mockIdentity.getCustomerId());
        req.getAttributeKeyList().add(Constants.PARAM_GENDER);
        try {
            final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final IdentityExportResponse response = (IdentityExportResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : identity list in response is null", response.getIdentities());
            assertFalse(strTestCase + " : identity list in response is empty", response.getIdentities().isEmpty());
            assertTrue(strTestCase + " : identity list in response doesn't contain expected identity", response.getIdentities().stream().allMatch(i -> i.getCustomerId().equals(mockIdentity.getCustomerId())));
            assertTrue(strTestCase + " : identity list in response contains other attributes than expected", response.getIdentities().stream().flatMap(i -> i.getAttributes().stream()).allMatch(a -> req.getAttributeKeyList().contains(a.getKey())));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final Identity mockIdentity = createMockIdentityInDB();
        try {
            String strTestCase = "2.1. Export without request";
            IdentityExportRequest req = null;
            try {
                final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EXPORT_REQUEST_NULL_OR_EMPTY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Export with empty request";
            req = new IdentityExportRequest();
            try {
                final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EXPORT_REQUEST_NULL_OR_EMPTY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Export request with cuid number over limit";
            req = new IdentityExportRequest();
            for(int i = 0 ; i <= AppPropertiesService.getPropertyInt("identitystore.identity.export.size.limit", 500); i++) {
                req.getCuidList().add(String.valueOf(i));
            }
            try {
                final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EXPORT_LIMIT_EXCEEDED);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Export with no result";
            req = new IdentityExportRequest();
            req.getCuidList().add("unknown-cuid");
            try {
                final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Export with unknown attribute key";
            req = new IdentityExportRequest();
            req.getCuidList().add(mockIdentity.getCustomerId());
            req.getAttributeKeyList().add("unknown-attribute-key");
            try {
                final IdentityStoreExportRequest request = new IdentityStoreExportRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }
}

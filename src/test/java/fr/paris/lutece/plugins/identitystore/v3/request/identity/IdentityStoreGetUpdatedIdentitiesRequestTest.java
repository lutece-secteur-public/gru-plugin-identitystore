package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreGetUpdatedIdentitiesRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.UpdatedIdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.UpdatedIdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

public class IdentityStoreGetUpdatedIdentitiesRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get updated identities request";
        final Identity mockIdentity = createMockIdentityInDB();
        try {
            final UpdatedIdentitySearchRequest req = new UpdatedIdentitySearchRequest();
            req.getIdentityChangeTypes().add(IdentityChangeType.CREATE);
            req.setDays(1);
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            final UpdatedIdentitySearchResponse response = (UpdatedIdentitySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : updated identity list in response is null", response.getUpdatedIdentityList());
            assertFalse(strTestCase + " : updated identity list in response is empty", response.getUpdatedIdentityList().isEmpty());
            assertTrue(strTestCase + " : updated identity list in response doesn't contain the newly created identity", response.getUpdatedIdentityList().stream().anyMatch(ui -> ui.getCustomerId().equals(mockIdentity.getCustomerId())));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Get updated identities without request";
        try {
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_UPDATED_IDENTITY_SEARCH_REQUEST_NULL_OR_EMPTY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Get updated identities with empty request";
        try {
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(new UpdatedIdentitySearchRequest(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_UPDATED_IDENTITY_SEARCH_REQUEST_NULL_OR_EMPTY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Get updated identities with wrong page";
        UpdatedIdentitySearchRequest req = new UpdatedIdentitySearchRequest();
        req.setDays(1);
        req.setPage(0);
        try {
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_PAGINATION_START_ERROR);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.4. Get updated identities with wrong size";
        req = new UpdatedIdentitySearchRequest();
        req.setDays(1);
        req.setSize(0);
        try {
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_PAGE_SIZE_ERROR);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.5. Get updated identities with no result";
        req = new UpdatedIdentitySearchRequest();
        req.getIdentityChangeTypes().add(IdentityChangeType.CONSOLIDATION_CANCELLED);
        req.setDays(0);
        try {
            final IdentityStoreGetUpdatedIdentitiesRequest request = new IdentityStoreGetUpdatedIdentitiesRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE );
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_UPDATED_IDENTITY_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }
}

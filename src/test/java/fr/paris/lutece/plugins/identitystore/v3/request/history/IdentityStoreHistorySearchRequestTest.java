package fr.paris.lutece.plugins.identitystore.v3.request.history;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.request.identity.AbstractIdentityRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.history.IdentityStoreHistorySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityHistorySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IdentityStoreHistorySearchRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final Identity mockIdentity = this.createMockIdentityInDB();
        try {
            String strTestCase = "1.1. Search identity history from customer ID";
            IdentityHistorySearchRequest req = new IdentityHistorySearchRequest();
            req.setCustomerId(mockIdentity.getCustomerId());
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentityHistorySearchResponse response = (IdentityHistorySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
                assertNotNull(strTestCase + " : history list in response is null", response.getHistories());
                assertFalse(strTestCase + " : history list in response is empty", response.getHistories().isEmpty());
                assertTrue(strTestCase + " : histories in response are not all with the expected customer ID", response.getHistories().stream().allMatch(h -> h.getCustomerId().equals(mockIdentity.getCustomerId())));
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "1.2. Search identity history from change type";
            req = new IdentityHistorySearchRequest();
            req.setIdentityChangeType(IdentityChangeType.CREATE);
            req.setNbDaysFrom(1);
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentityHistorySearchResponse response = (IdentityHistorySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
                assertNotNull(strTestCase + " : history list in response is null", response.getHistories());
                assertFalse(strTestCase + " : history list in response is empty", response.getHistories().isEmpty());
                assertTrue(strTestCase + " : histories in response do not contain the expected customer ID", response.getHistories().stream().anyMatch(h -> h.getCustomerId().equals(mockIdentity.getCustomerId())));
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final Identity mockIdentity = this.createMockIdentityInDB();
        try {
            String strTestCase = "2.1. Search identity history without request";
            IdentityHistorySearchRequest req = null;
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_HISTORY_SEARCH_EMPTY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Search identity history with empty request";
            req = new IdentityHistorySearchRequest();
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_HISTORY_SEARCH_EMPTY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Search identity history with end date before start date";
            req = new IdentityHistorySearchRequest();
            req.setModificationDateIntervalStart(new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01"));
            req.setModificationDateIntervalEnd(new SimpleDateFormat("yyyy-MM-dd").parse("1990-01-01"));
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_END_DATE_BEFORE_START_DATE);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Search identity history with both days and date interval";
            req = new IdentityHistorySearchRequest();
            req.setNbDaysFrom(10);
            req.setModificationDateIntervalStart(new SimpleDateFormat("yyyy-MM-dd").parse("2020-01-01"));
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_HISTORY_SEARCH_INVALID);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Search identity history with no result";
            req = new IdentityHistorySearchRequest();
            req.setIdentityChangeType(IdentityChangeType.CONSOLIDATION_CANCELLED);
            req.setCustomerId(mockIdentity.getCustomerId());
            try {
                final IdentityStoreHistorySearchRequest request = new IdentityStoreHistorySearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_HISTORY_FOUND);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityHome.hardRemove(mockIdentity.getId());
        }
    }
}

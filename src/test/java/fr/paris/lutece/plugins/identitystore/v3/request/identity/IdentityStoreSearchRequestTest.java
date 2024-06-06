package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreSearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeTreatmentType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class IdentityStoreSearchRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final Identity mockIdentity = createMockIdentityInDB("IdentityStoreSearchRequestTest-connection-id");
        TimeUnit.SECONDS.sleep(3); // ES needs time to index the new identity
        try {
            String strTestCase = "1.1. Search identity with mandatory attributes";
            IdentitySearchRequest req = new IdentitySearchRequest();
            req.setSearch(buildSearchDto(mockIdentity.getAttributes().get(Constants.PARAM_FIRST_NAME).getValue(),
                                         mockIdentity.getAttributes().get(Constants.PARAM_FAMILY_NAME).getValue(),
                                         mockIdentity.getAttributes().get(Constants.PARAM_BIRTH_DATE).getValue()));
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentitySearchResponse response = (IdentitySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
                assertNotNull(strTestCase + " : identity list in response is null", response.getIdentities());
                assertFalse(strTestCase + " : identity list in response is empty", response.getIdentities().isEmpty());
                assertTrue(strTestCase + " : identity list in response doesn't have the identity with expected customer ID", response.getIdentities().stream().anyMatch(i -> i.getCustomerId().equals(mockIdentity.getCustomerId())));
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "1.2. Search identity with connection ID";
            req = new IdentitySearchRequest();
            req.setConnectionId(mockIdentity.getConnectionId());
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentitySearchResponse response = (IdentitySearchResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.OK);
                assertNotNull(strTestCase + " : identity list in response is null", response.getIdentities());
                assertFalse(strTestCase + " : identity list in response is empty", response.getIdentities().isEmpty());
                assertFalse(strTestCase + " : identity list in response contains more than one identty", response.getIdentities().size() > 1);
                assertEquals(strTestCase + " : identity in response doesn't have the same customer ID as in the request", mockIdentity.getCustomerId(), response.getIdentities().get(0).getCustomerId());
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final Identity mockIdentity = createMockIdentityInDB("IdentityStoreSearchRequestTest-connection-id");
        TimeUnit.SECONDS.sleep(3); // ES needs time to index the new identity
        try {
            String strTestCase = "2.1. Search identity without request";
            IdentitySearchRequest req = null;
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_SEARCH_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Search identity with empty request";
            req = new IdentitySearchRequest();
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_SEARCH_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Search identity with empty search in request";
            req = new IdentitySearchRequest();
            req.setSearch(new SearchDto());
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_SEARCH_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Search identity with both search and connection ID";
            req = new IdentitySearchRequest();
            req.setConnectionId(mockIdentity.getConnectionId());
            req.setSearch(new SearchDto());
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_SEARCH_IDENTITY_CONNECTION_ID_OR_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Search identity without all required attributes";
            req = new IdentitySearchRequest();
            req.setSearch(buildSearchDto(null,
                                         mockIdentity.getAttributes().get(Constants.PARAM_FAMILY_NAME).getValue(),
                                         mockIdentity.getAttributes().get(Constants.PARAM_BIRTH_DATE).getValue()));
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_MANDATORY_ATTRIBUTES);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.6. Search identity with unknown connection ID";
            req = new IdentitySearchRequest();
            req.setConnectionId("unknown-connection-id");
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.6. Search identity with attributes no result";
            req = new IdentitySearchRequest();
            req.setSearch(buildSearchDto("unknownfirstname", "unknownfamillyname", "30/04/1921"));
            try {
                final IdentityStoreSearchRequest request = new IdentityStoreSearchRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_IDENTITY_FOUND);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }

    }

    private SearchDto buildSearchDto( final String firstname, final String famillyName, final String birthDate ) {
        final SearchDto search = new SearchDto( );
        for (final Pair<String, String> pair : Arrays.asList(Pair.of(Constants.PARAM_FIRST_NAME, firstname),
                                                             Pair.of(Constants.PARAM_FAMILY_NAME, famillyName),
                                                             Pair.of(Constants.PARAM_BIRTH_DATE, birthDate))) {
            if (pair.getValue() != null) {
                final SearchAttribute attr = new SearchAttribute();
                attr.setKey(pair.getKey());
                attr.setValue(pair.getValue());
                attr.setTreatmentType(AttributeTreatmentType.STRICT);
                search.getAttributes().add(attr);
            }
        }
        return search;
    }
}

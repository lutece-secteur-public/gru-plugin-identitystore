package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotEquals;

public class IdentityStoreUpdateRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        Identity mockIdentity = createMockIdentityInDB();
        try {
            String strTestCase = "1.1. Update identity request : add new attribute";
            assertNull(strTestCase + " : mock identity already have a birthcountry_code. Please create it without", mockIdentity.getAttributes().get(Constants.PARAM_BIRTH_COUNTRY_CODE));
            IdentityChangeRequest req = new IdentityChangeRequest();
            IdentityDto identityDto = new IdentityDto();
            identityDto.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            final String newCountryCode = "99100";
            identityDto.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_BIRTH_COUNTRY_CODE, newCountryCode, IdentityMockUtils.DEC));
            req.setIdentity(identityDto);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentityChangeResponse response = (IdentityChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
                assertNotNull(strTestCase + " : customer ID in response is null", response.getCustomerId());
                assertEquals(strTestCase + " : customer ID in response is not equal to the sent customer ID", mockIdentity.getCustomerId(), response.getCustomerId());
                assertEquals(strTestCase + " : ", AttributeChangeStatus.CREATED, response.getStatus().getAttributeStatuses().stream().filter(s -> s.getKey().equals(Constants.PARAM_BIRTH_COUNTRY_CODE)).findFirst().get().getStatus());

                TimeUnit.SECONDS.sleep(1);
                final Identity identityFromDb = IdentityHome.findByCustomerId(mockIdentity.getCustomerId());
                final IdentityAttribute birthcountrycode = identityFromDb.getAttributes().get(Constants.PARAM_BIRTH_COUNTRY_CODE);
                assertNotNull(strTestCase + " : birthcountry_code of identity is still null in DB after update", birthcountrycode);
                assertEquals(strTestCase + " : birthcountry_code of identity in DB is not the expected one", newCountryCode, birthcountrycode.getValue());
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "1.2. Update identity request : update existing attribute";
            mockIdentity = IdentityHome.findByCustomerId(mockIdentity.getCustomerId());
            assertNotNull(strTestCase + " : mock identity does not have a gender. Please create the mock with a gender", mockIdentity.getAttributes().get(Constants.PARAM_GENDER));
            req = new IdentityChangeRequest();
            identityDto = new IdentityDto();
            identityDto.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            final String newGender = "1";
            final String oldGender = mockIdentity.getAttributes().get(Constants.PARAM_GENDER).getValue();
            identityDto.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_GENDER, newGender, IdentityMockUtils.DEC));
            req.setIdentity(identityDto);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                final IdentityChangeResponse response = (IdentityChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
                assertNotNull(strTestCase + " : customer ID in response is null", response.getCustomerId());
                assertEquals(strTestCase + " : customer ID in response is not equal to the sent customer ID", mockIdentity.getCustomerId(), response.getCustomerId());
                assertEquals(strTestCase + " : ", AttributeChangeStatus.UPDATED, response.getStatus().getAttributeStatuses().stream().filter(s -> s.getKey().equals(Constants.PARAM_GENDER)).findFirst().get().getStatus());

                TimeUnit.SECONDS.sleep(1);
                final Identity identityFromDb = IdentityHome.findByCustomerId(mockIdentity.getCustomerId());
                final IdentityAttribute gender = identityFromDb.getAttributes().get(Constants.PARAM_GENDER);
                assertNotNull(strTestCase + " : gender of identity is null in DB after update", gender);
                assertNotEquals(strTestCase + " : gender of identity in DB is not the expected one", oldGender, gender.getValue());
                assertEquals(strTestCase + " : gender of identity in DB is not the expected one", newGender, gender.getValue());
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        final Identity mockIdentity = createMockIdentityInDB("IdentityStoreUpdateRequestTest-connection-id");
        try {
            String strTestCase = "2.1. Update identity without request";
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.2. Update identity with empty request";
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), new IdentityChangeRequest(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.3. Update identity with empty identity in request";
            IdentityChangeRequest req = new IdentityChangeRequest();
            req.setIdentity(new IdentityDto());
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.4. Update identity with no last update date";
            req = new IdentityChangeRequest();
            IdentityDto identity = new IdentityDto();
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_IDENTITY_LAST_UPDATE_DATE);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.5. Update identity with one attribute key null";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(null, "test", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ATTRIBUTE_MISSING_KEY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.6. Update identity with duplicate in attributes with different values";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.com", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CHANGE_REQUEST_SAME_ATTRIUTE_DIFFERENT_VALUE);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.7. Update identity with null in attr value";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, null, IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ATTRIBUTE_MISSING_VALUE);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.8. Update identity with no cuid";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(null, req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_CUSTOMER_ID);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.9. Update identity with unknown cuid";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest("unknown-cuid", req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.10. Update identity with only connection ID";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.setConnectionId(mockIdentity.getConnectionId());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(null, req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_UPDATE_WITH_ONLY_CONNECTION_ID);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.11. Update identity with unknown attribute";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute("unknown_attr_key", "test", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.12. Update identity with one high certification pivot";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(mockIdentity.getLastUpdateDate());
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_BIRTH_COUNTRY_CODE, "99100", IdentityMockUtils.ORIG1));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }

            strTestCase = "2.13. Update identity with wrong last update date";
            req = new IdentityChangeRequest();
            identity = new IdentityDto();
            identity.setLastUpdateDate(Timestamp.from(new Date().toInstant()));
            identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_BIRTH_COUNTRY_CODE, "99100", IdentityMockUtils.DEC));
            req.setIdentity(identity);
            try {
                final IdentityStoreUpdateRequest request = new IdentityStoreUpdateRequest(mockIdentity.getCustomerId(), req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
                this.executeRequestKO(request, strTestCase, ResourceConsistencyException.class, Constants.PROPERTY_REST_ERROR_UPDATE_CONFLICT);
            } catch (final IdentityStoreException e) {
                fail(strTestCase + " : FAIL : " + e.getMessage());
            }
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }
    }
}

package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;

import java.util.concurrent.TimeUnit;

public class IdentityStoreCreateRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Create identity request";
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest();
        identityChangeRequest.setIdentity(getIdentityDtoForCreate());
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(identityChangeRequest, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final IdentityChangeResponse response = (IdentityChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + " : customer ID in response is null", response.getCustomerId());
            assertNotNull(strTestCase + " : creation date in response is null", response.getCreationDate());

            TimeUnit.SECONDS.sleep(1);
            final Identity dbIdentity = IdentityHome.findByCustomerId(response.getCustomerId());
            assertNotNull(strTestCase + " : identity not found in database after creation", dbIdentity);

            IdentityHome.hardRemove(dbIdentity.getId());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Create request without identity change request";
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Create request with empty identity change request";
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(new IdentityChangeRequest(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Create request with empty identity";
        IdentityChangeRequest req = new IdentityChangeRequest();
        req.setIdentity(new IdentityDto());
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_EMPTY_CHANGE_CRITERIAS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.4. Create request with CUID";
        IdentityDto identity = getIdentityDtoForCreate();
        identity.setCustomerId("custom CUID");
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_CREATE_WITH_CUID);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.4. Create request with an unknown attribute";
        identity = getIdentityDtoForCreate();
        identity.getAttributes().add(IdentityMockUtils.getMockAttribute("unknown_attribute_key", "test", IdentityMockUtils.DEC));
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_UNKNOWN_ATTRIBUTE_KEY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.5. Create request with duplicate in attributes with different values";
        identity = getIdentityDtoForCreate();
        identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.fr", IdentityMockUtils.DEC));
        identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.com", IdentityMockUtils.DEC));
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CHANGE_REQUEST_SAME_ATTRIUTE_DIFFERENT_VALUE);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.6. Create request with null in attribute value";
        identity = getIdentityDtoForCreate();
        identity.getAttributes().add(IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, null, IdentityMockUtils.DEC));
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ATTRIBUTE_MISSING_VALUE);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.7. Create request with null in attribute key";
        identity = getIdentityDtoForCreate();
        identity.getAttributes().add(IdentityMockUtils.getMockAttribute(null, "test", IdentityMockUtils.DEC));
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ATTRIBUTE_MISSING_KEY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.8. Create request with an attribute without certification";
        identity = getIdentityDtoForCreate();
        AttributeDto attr = IdentityMockUtils.getMockAttribute(Constants.PARAM_EMAIL, "test@test.com", IdentityMockUtils.DEC);
        attr.setCertifier(null);
        identity.getAttributes().add(attr);
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ATTRIBUTE_NOT_CERTIFIED);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.9. Create request with a single pivot attribute with high certification";
        identity = getIdentityDtoForCreate();
        attr = IdentityMockUtils.getMockAttribute(Constants.PARAM_BIRTH_COUNTRY_CODE, "99100", IdentityMockUtils.ORIG1);
        identity.getAttributes().add(attr);
        req.setIdentity(identity);
        try {
            final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(req, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_IDENTITY_ALL_PIVOT_ATTRIBUTE_SAME_CERTIFICATION);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }

}

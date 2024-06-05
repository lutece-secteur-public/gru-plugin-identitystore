package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.service.identity.IdentityService;
import fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreUncertifyRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class IdentityStoreUncertifyRequestTest extends AbstractIdentityRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Uncertify identity request";
        final Identity mockIdentity = createMockIdentityInDB(IdentityMockUtils.NUM2);
        final String decertificationProcessus = AppPropertiesService.getProperty("identitystore.identity.uncertify.processus", "dec");
        assertTrue(strTestCase + " : mock identity was created with the decertification processus for its attributes. Please choose a higher certification", mockIdentity.getAttributes().values().stream().noneMatch(a -> a.getCertificate().getCertifierCode().equals(decertificationProcessus)));
        try {
            final IdentityStoreUncertifyRequest request = new IdentityStoreUncertifyRequest(mockIdentity.getCustomerId(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final IdentityChangeResponse response = (IdentityChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + " : attribute status list in response is null", response.getStatus().getAttributeStatuses());
            assertFalse(strTestCase + " : attribute status list in response is empty", response.getStatus().getAttributeStatuses().isEmpty());
            assertTrue(strTestCase + " : attribute status list in response doesn't contain expected UNCERTIFY status", response.getStatus().getAttributeStatuses().stream().allMatch(s -> s.getStatus() == AttributeChangeStatus.UNCERTIFIED));

            final Identity mockIdentityFromDb = IdentityHome.findByCustomerId(mockIdentity.getCustomerId());
            assertTrue(strTestCase + " : identity from DB after decertification doesn't have all of its attribute certified with " + decertificationProcessus, mockIdentityFromDb.getAttributes().values().stream().allMatch(a -> a.getCertificate().getCertifierCode().equals(decertificationProcessus)));
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            IdentityService.instance().delete(mockIdentity.getCustomerId());
        }

    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Uncertify identity without customer ID";
        try {
            final IdentityStoreUncertifyRequest request = new IdentityStoreUncertifyRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_MISSING_CUSTOMER_ID);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Uncertify identity with unknown customer ID";
        try {
            final IdentityStoreUncertifyRequest request = new IdentityStoreUncertifyRequest("unknown-cuid", H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_MATCHING_IDENTITY);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }
}

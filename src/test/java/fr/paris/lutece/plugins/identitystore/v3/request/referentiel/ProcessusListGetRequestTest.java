package fr.paris.lutece.plugins.identitystore.v3.request.referentiel;

import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.referentiel.ProcessusListGetRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.ProcessusSearchResponse;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

public class ProcessusListGetRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final String strTestCase = "1.1. Get all processus";
        try {
            final ProcessusListGetRequest request = new ProcessusListGetRequest(H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ProcessusSearchResponse response = (ProcessusSearchResponse) executeRequestOK(request, strTestCase, ResponseStatusType.OK);
            assertNotNull(strTestCase + " : the level list in the response is null", response.getProcessus());
            assertFalse(strTestCase + " : the level list in the response is empty", response.getProcessus().isEmpty());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        // no-op
    }
}

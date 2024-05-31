package fr.paris.lutece.plugins.identitystore.v3.request;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.test.LuteceTestCase;

public abstract class AbstractIdentityStoreRequestTest extends LuteceTestCase {

    protected static final String H_CLIENT_CODE = "TEST";
    protected static final String H_APP_CODE = "TEST";
    protected static final String H_AUTHOR_NAME = "test";
    protected static final String H_AUTHOR_TYPE = "admin";

    public abstract void test_1_RequestOK() throws Exception;

    public abstract void test_2_RequestKO() throws Exception;

    protected void executeRequestKO(final AbstractIdentityStoreRequest request, final String strTestCase, final Class<? extends IdentityStoreException> expectedException) {
        executeRequest(request, strTestCase, null, expectedException);
    }

    protected ResponseDto executeRequestOK(final AbstractIdentityStoreRequest request, final String strTestCase, final ResponseStatusType expectedResponseStatus) {
        return executeRequest(request, strTestCase, expectedResponseStatus, null);
    }

    private ResponseDto executeRequest(final AbstractIdentityStoreRequest request, final String strTestCase, final ResponseStatusType expectedResponseStatus,
                                final Class<? extends IdentityStoreException> expectedException) {
        try {
            // Execute request
            final ResponseDto response = request.doRequest();

            // If no exception, the request was successfull.
            assertNotNull( strTestCase + " : response is null", response );
            final ResponseStatusType responseStatus = response.getStatus().getType();

            // Checking if no exception was expected
            assertNull(strTestCase + " : request was expected to fail, but it was successfull. Response status : " + responseStatus,
                       expectedException);

            // Checking if the response has the expected status
            assertEquals(strTestCase + " : response doesn't have the expected status. Response status : " + responseStatus,
                         expectedResponseStatus,
                         responseStatus);

            return response;

        } catch (final IdentityStoreException e) {
            // If exception, the request wasn't successfull.

            // Checking if no response status was expected
            assertNull(strTestCase + " : request was expected to succeed, but it failed. Exception message : " + e.getMessage(),
                       expectedResponseStatus);

            // Checking if the exception is of the expected type
            assertEquals(strTestCase + " : the exception that occured is not of the expected type. Exception message : " + e.getMessage(),
                         expectedException,
                         e.getClass());
        }
        return null;
    }

}

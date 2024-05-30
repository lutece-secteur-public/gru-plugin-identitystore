package fr.paris.lutece.plugins.identitystore.v3.request;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public interface IdentityStoreRequestTest {

    void test_1_RequestOK() throws Exception;

    void test_2_RequestKO() throws Exception;

    default void executeRequestKO(final AbstractIdentityStoreRequest request, final String strTestCase, final Class<? extends IdentityStoreException> expectedException) {
        executeRequest(request, strTestCase, null, expectedException);
    }

    default ResponseDto executeRequestOK(final AbstractIdentityStoreRequest request, final String strTestCase, final ResponseStatusType expectedResponseStatus) {
        return executeRequest(request, strTestCase, expectedResponseStatus, null);
    }

    default ResponseDto executeRequest(final AbstractIdentityStoreRequest request, final String strTestCase, final ResponseStatusType expectedResponseStatus,
                                final Class<? extends IdentityStoreException> expectedException) {
        try {
            // Execute request
            final ResponseDto response = request.doRequest();

            // If no exception, the request was successfull.
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

    default String getClientCode() {
        return "TEST";
    }
    default String getAppCode() {
        return "TEST";
    }
    default String getAuthorName() {
        return "test";
    }
    default String getAuthorType() {
        return "admin";
    }

}

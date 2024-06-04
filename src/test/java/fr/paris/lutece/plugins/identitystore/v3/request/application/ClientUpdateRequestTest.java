package fr.paris.lutece.plugins.identitystore.v3.request.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientUpdateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;

import java.util.concurrent.TimeUnit;

public class ClientUpdateRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        final ClientApplicationDto mockClientApplication = createMockClientApplication();

        String strTestCase = "1.1. Update existing client application's name";
        final String newName = "ClientUpdateRequestTestNewName";
        mockClientApplication.setName(newName);
        try {
            final ClientUpdateRequest request = new ClientUpdateRequest(mockClientApplication, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ClientChangeResponse response = (ClientChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull( strTestCase + " : client application in response is null", response.getClientApplication() );
            assertEquals(strTestCase + " : client application in response doesn't have the axpected new name", newName, response.getClientApplication().getName( ) );

            TimeUnit.SECONDS.sleep(1);
            assertEquals(strTestCase + " : client application name in database is not the expected after update", newName, ClientApplicationHome.findByPrimaryKey(response.getClientApplication().getId()).getName());
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        } finally {
            ClientApplicationHome.remove(DtoConverter.convertDtoToClient(mockClientApplication));
        }
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Update request without client application";
        try {
            final ClientUpdateRequest request = new ClientUpdateRequest(null, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CLIENT_APPLICATION_NULL);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Update request with empty client application";
        try {
            final ClientUpdateRequest request = new ClientUpdateRequest(new ClientApplicationDto(), H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CLIENT_APPLICATION_WITHOUT_CLIENT_CODE);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Update request with uknown client code";
        ClientApplicationDto clientApp = new ClientApplicationDto();
        clientApp.setClientCode("unknownClientCode");
        try {
            final ClientUpdateRequest request = new ClientUpdateRequest(clientApp, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceNotFoundException.class, Constants.PROPERTY_REST_ERROR_NO_CLIENT_FOUND);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

    }

    private ClientApplicationDto createMockClientApplication() throws Exception {
        final ClientApplicationDto clientApp = ClientCreateRequestTest.getClientApplicationDtoForCreate();
        clientApp.setName("ClientUpdateRequestTest");
        final ClientCreateRequest request = new ClientCreateRequest(clientApp, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
        final ClientApplicationDto mock = ((ClientChangeResponse) request.doRequest()).getClientApplication();
        TimeUnit.SECONDS.sleep(1);
        return mock;
    }
}

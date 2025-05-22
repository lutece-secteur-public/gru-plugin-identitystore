package fr.paris.lutece.plugins.identitystore.v3.request.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;

import java.util.concurrent.TimeUnit;

public class ClientCreateRequestTest extends AbstractIdentityStoreRequestTest {

    @Override
    public void test_1_RequestOK() throws Exception {
        String strTestCase = "1.1. Create client application with all fields";
        final ClientApplicationDto clientApplicationDto = getClientApplicationDtoForCreate();
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            final ClientChangeResponse response = (ClientChangeResponse) this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
            assertNotNull(strTestCase + ": client application in response is null", response.getClientApplication());

            TimeUnit.SECONDS.sleep(1);
            assertNotNull(strTestCase + " : could not find the new client application in database after creation", ClientApplicationHome.findByPrimaryKey(response.getClientApplication().getId()));

            
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
        
        strTestCase = "2.3. Create client application with existing client code";
        //clientApplicationDto = getClientApplicationDtoForCreate();
        clientApplicationDto.setClientCode("ClientCodeMock");
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, ResourceConsistencyException.class, Constants.PROPERTY_REST_ERROR_CLIENT_ALREADY_EXISTS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }
        
        ClientApplicationHome.remove(DtoConverter.convertDtoToClient( clientApplicationDto ));

    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Create client application without clientApplicationDto";
        ClientApplicationDto clientApplicationDto = null;
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CLIENT_APPLICATION_NULL);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Create client application without client code";
        clientApplicationDto = getClientApplicationDtoForCreate();
        clientApplicationDto.setClientCode(null);
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
            this.executeRequestKO(request, strTestCase, RequestFormatException.class, Constants.PROPERTY_REST_ERROR_CLIENT_APPLICATION_WITHOUT_CLIENT_CODE);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }



    }

    public static ClientApplicationDto getClientApplicationDtoForCreate( ) {
        final ClientApplicationDto clientApplicationDto = new ClientApplicationDto();
        clientApplicationDto.setClientCode("ClientCodeMock");
        clientApplicationDto.setApplicationCode("AppCodeMock");
        clientApplicationDto.setName("NameMock");
        return clientApplicationDto;
    }
}

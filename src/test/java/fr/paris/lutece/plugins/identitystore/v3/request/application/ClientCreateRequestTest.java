package fr.paris.lutece.plugins.identitystore.v3.request.application;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.v3.request.IdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.application.ClientCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.application.ClientApplicationDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ResponseStatusType;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.test.LuteceTestCase;

import java.util.concurrent.TimeUnit;

public class ClientCreateRequestTest extends LuteceTestCase implements IdentityStoreRequestTest {

    private static final String CLIENT_CODE_TO_CREATE = "ClientCreateRequestTest";
    private static final String APP_CODE_TO_CREATE = "ClientCreateRequestTest";
    private static final String NAME_TO_CREATE = "ClientCreateRequestTest";

    @Override
    public void test_1_RequestOK() throws Exception {
        String strTestCase = "1.1. Create client application with all fields";
        final ClientApplicationDto clientApplicationDto = new ClientApplicationDto();
        clientApplicationDto.setClientCode(CLIENT_CODE_TO_CREATE);
        clientApplicationDto.setApplicationCode(APP_CODE_TO_CREATE);
        clientApplicationDto.setName(NAME_TO_CREATE);
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, getClientCode(), getAppCode(), getAuthorName(), getAuthorType());
            this.executeRequestOK(request, strTestCase, ResponseStatusType.SUCCESS);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        TimeUnit.SECONDS.sleep(2);
    }

    @Override
    public void test_2_RequestKO() throws Exception {
        String strTestCase = "2.1. Create client application without clientApplicationDto";
        ClientApplicationDto clientApplicationDto = null;
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, getClientCode(), getAppCode(), getAuthorName(), getAuthorType());
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.2. Create client application without client code";
        clientApplicationDto = new ClientApplicationDto();
        clientApplicationDto.setApplicationCode(APP_CODE_TO_CREATE);
        clientApplicationDto.setName(NAME_TO_CREATE);
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, getClientCode(), getAppCode(), getAuthorName(), getAuthorType());
            this.executeRequestKO(request, strTestCase, RequestFormatException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        strTestCase = "2.3. Create client application with existing client code";
        clientApplicationDto = new ClientApplicationDto();
        clientApplicationDto.setClientCode(CLIENT_CODE_TO_CREATE);
        clientApplicationDto.setApplicationCode(APP_CODE_TO_CREATE);
        clientApplicationDto.setName(NAME_TO_CREATE);
        try {
            final ClientCreateRequest request = new ClientCreateRequest(clientApplicationDto, getClientCode(), getAppCode(), getAuthorName(), getAuthorType());
            this.executeRequestKO(request, strTestCase, ResourceConsistencyException.class);
        } catch (final IdentityStoreException e) {
            fail(strTestCase + " : FAIL : " + e.getMessage());
        }

        try {
            ClientApplicationHome.remove(ClientApplicationHome.findByCode(CLIENT_CODE_TO_CREATE));
        } catch (final Throwable e) {
            // no-op
        }
    }
}

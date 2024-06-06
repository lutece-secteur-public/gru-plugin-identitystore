package fr.paris.lutece.plugins.identitystore.v3.request.identity;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.util.IdentityMockUtils;
import fr.paris.lutece.plugins.identitystore.v3.request.AbstractIdentityStoreRequestTest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.identity.IdentityStoreCreateRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;

import java.util.concurrent.TimeUnit;

public abstract class AbstractIdentityRequestTest extends AbstractIdentityStoreRequestTest {

    protected IdentityDto getIdentityDtoForCreate() {
        return getIdentityDtoForCreate(IdentityMockUtils.DEC);
    }

    protected IdentityDto getIdentityDtoForCreate(final int certifLevel) {
        return IdentityMockUtils.getMockIdentityDto("0", certifLevel,
                                                    "IdentityStoreCreateRequestTestFamillyName", certifLevel,
                                                    "IdentityStoreCreateRequestTestFirstName", certifLevel,
                                                    "01/01/1999", certifLevel,
                                                    null, 0,
                                                    null, 0);
    }

    protected Identity createMockIdentityInDB() throws Exception {
        return createMockIdentityInDB(IdentityMockUtils.DEC, null);
    }

    protected Identity createMockIdentityInDB(final String connectionId) throws Exception {
        return createMockIdentityInDB(IdentityMockUtils.DEC, connectionId);
    }

    protected Identity createMockIdentityInDB(final int certifLevel) throws Exception {
        return createMockIdentityInDB(certifLevel, null);
    }

    protected Identity createMockIdentityInDB(final int certifLevel, final String connectionId) throws Exception {
        final IdentityDto mock = getIdentityDtoForCreate(certifLevel);
        if(connectionId != null) {
            mock.setConnectionId(connectionId);
        }
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest();
        identityChangeRequest.setIdentity(mock);
        final IdentityStoreCreateRequest request = new IdentityStoreCreateRequest(identityChangeRequest, H_CLIENT_CODE, H_APP_CODE, H_AUTHOR_NAME, H_AUTHOR_TYPE);
        final IdentityChangeResponse response = (IdentityChangeResponse) request.doRequest();
        TimeUnit.SECONDS.sleep(1);
        return IdentityHome.findByCustomerId(response.getCustomerId());
    }


}

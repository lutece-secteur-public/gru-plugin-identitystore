package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeCertification;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevel;
import fr.paris.lutece.plugins.identitystore.cache.ActiveServiceContractCache;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides the client application and the service contract used by the request tests
 */
public final class ServiceContractMockUtils
{
    public static final String TEST_CONTRACT_NAME = "Test service contract";

    // Must start after the contracts created by the contract tests (1990-01-01) so that they are not rejected for overlapping
    private static final Date TEST_CONTRACT_STARTING_DATE = Date.valueOf( LocalDate.of( 2000, 1, 1 ) );

    private ServiceContractMockUtils( )
    {
    }

    /**
     * Creates the client application and its service contract granting every right if they do not exist, and puts this contract back in the active
     * service contract cache, as other tests may have replaced or removed it.
     *
     * @param clientCode
     *            the client code
     * @param appCode
     *            the application code
     * @return the service contract of the client application
     */
    public static ServiceContract ensureTestServiceContract( final String clientCode, final String appCode ) throws IdentityStoreException
    {
        ClientApplication clientApplication = ClientApplicationHome.findByCode( clientCode );
        if ( clientApplication == null )
        {
            clientApplication = new ClientApplication( );
            clientApplication.setClientCode( clientCode );
            clientApplication.setApplicationCode( appCode );
            clientApplication.setName( "Test application " + clientCode );
            ClientApplicationHome.create( clientApplication );
        }

        ServiceContract serviceContract = ClientApplicationHome.selectServiceContracts( clientApplication.getId( ) ).stream( )
                .filter( contract -> TEST_CONTRACT_NAME.equals( contract.getName( ) ) ).findFirst( ).orElse( null );
        if ( serviceContract == null )
        {
            serviceContract = ServiceContractService.instance( ).create( buildFullRightsServiceContract( ), clientApplication );
        }
        else
        {
            serviceContract.setAttributeRights( ServiceContractHome.selectApplicationRights( serviceContract ) );
            serviceContract.setAttributeCertifications( ServiceContractHome.selectAttributeCertifications( serviceContract ) );
            serviceContract.setAttributeRequirements( ServiceContractHome.selectAttributeRequirements( serviceContract ) );
        }
        serviceContract.setClientCode( clientCode );

        final ActiveServiceContractCache cache = SpringContextService.getBean( "identitystore.activeServiceContractCache" );
        cache.put( clientCode, serviceContract );

        return serviceContract;
    }

    /**
     * Builds a service contract granting every authorization, every attribute right and every certification processus
     *
     * @return the service contract to create
     */
    private static ServiceContract buildFullRightsServiceContract( )
    {
        final ServiceContract serviceContract = new ServiceContract( );
        serviceContract.setName( TEST_CONTRACT_NAME );
        serviceContract.setMoaEntityName( "moaEntityName" );
        serviceContract.setMoaContactName( "moaContactName" );
        serviceContract.setMoeEntityName( "moeEntityName" );
        serviceContract.setMoeResponsibleName( "moeResponsibleName" );
        serviceContract.setServiceType( "serviceType" );
        serviceContract.setStartingDate( TEST_CONTRACT_STARTING_DATE );
        serviceContract.setDataRetentionPeriodInMonths( 12 );
        serviceContract.setAuthorizedCreation( true );
        serviceContract.setAuthorizedUpdate( true );
        serviceContract.setAuthorizedSearch( true );
        serviceContract.setAuthorizedMerge( true );
        serviceContract.setAuthorizedAccountUpdate( true );
        serviceContract.setAuthorizedDeletion( true );
        serviceContract.setAuthorizedImport( true );
        serviceContract.setAuthorizedExport( true );
        serviceContract.setAuthorizedDecertification( true );
        serviceContract.setAuthorizedAgentHistoryRead( true );
        serviceContract.setAuthorizedAttachmentCertification( true );

        final List<AttributeRight> attributeRights = new ArrayList<>( );
        final List<AttributeCertification> attributeCertifications = new ArrayList<>( );
        for ( final AttributeKey attributeKey : AttributeKeyHome.getAttributeKeysList( false ) )
        {
            final AttributeRight attributeRight = new AttributeRight( );
            attributeRight.setAttributeKey( attributeKey );
            attributeRight.setSearchable( true );
            attributeRight.setReadable( true );
            attributeRight.setWritable( true );
            attributeRights.add( attributeRight );

            final List<RefAttributeCertificationLevel> certificationLevels = ServiceContractHome.selectRefAttributeCertificationLevels( attributeKey );
            if ( !certificationLevels.isEmpty( ) )
            {
                final AttributeCertification attributeCertification = new AttributeCertification( );
                attributeCertification.setAttributeKey( attributeKey );
                attributeCertification.setRefAttributeCertificationProcessus( certificationLevels.stream( )
                        .map( RefAttributeCertificationLevel::getRefAttributeCertificationProcessus ).collect( Collectors.toList( ) ) );
                attributeCertifications.add( attributeCertification );
            }
        }
        serviceContract.setAttributeRights( attributeRights );
        serviceContract.setAttributeCertifications( attributeCertifications );

        return serviceContract;
    }
}

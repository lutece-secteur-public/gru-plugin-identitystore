package fr.paris.lutece.plugins.identitystore.service.attribute;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.test.LuteceTestCase;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class IdentityAttributeGeocodesAdjustmentServiceTest extends LuteceTestCase
{
    protected static final String BIRTHPLACE_CODE_MULTIPLE = "01053";
    protected static final String BIRTHPLACE_CODE_UNIQUE = "75201";
    protected static final String BIRTHPLACE_NAME_UNIQUE = "PARIS 1ER ARROND";
    protected static final String BIRTHPLACE_NAME_GERMAN = "BERLIN";
    protected static final String COUNTRY_CODE_FRANCE = "99100";
    protected static final String COUNTRY_CODE_GERMANY = "99109";
    protected static final String COUNTRY_NAME_FRANCE = "FRANCE";
    protected static final String COUNTRY_NAME_FRANCE_ERROR = "FRAAAANCE";
    protected static final String COUNTRY_NAME_GERMANY = "ALLEMAGNE";
    protected static final String FAMILY_NAME = "TEST";
    protected static final String BIRTH_DATE = "01/01/2000";

    protected static final String KEY_BIRTHCOUNTRY_CODE = "birthcountry_code";
    protected static final String KEY_BIRTHCOUNTRY = "birthcountry";
    protected static final String KEY_BIRTHPLACE_CODE = "birthplace_code";
    protected static final String KEY_BIRTHPLACE = "birthplace";
    protected static final String KEY_FAMILY_NAME = "family_name";
    protected static final String KEY_FIRST_NAME = "first_name";
    protected static final String KEY_BIRTH_DATE = "birthdate";

    protected static final Timestamp DATE_NOW =  new Timestamp( System.currentTimeMillis( ) );
    protected static final String CERT_PROCESS = "DEC";

    protected static final String RETURNED_STATUS_ERROR_UNKONWN_COUNTRY = "unknown_geocodes_label";
    protected static final String RETURNED_STATUS_ERROR_MULTIPLE_CITIES = "multiple_geocodes_results_for_code";
    protected static final String RETURNED_STATUS_ERROR_NOT_CREATED = "not_created";

    // Test methods
    public void testCountryNameOnly() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHCOUNTRY, COUNTRY_NAME_FRANCE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityBase();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCountryNameOnly : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    public void testFalseCountryNameOnly() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHCOUNTRY, COUNTRY_NAME_FRANCE_ERROR) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityBase();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testFalseCountryNameOnly : " + attributeStatusList.toString());

        // Assert
        assertFalse(attributeStatusList.isEmpty());
        assertEquals(RETURNED_STATUS_ERROR_UNKONWN_COUNTRY, attributeStatusList.get(0).getStatus().getCode() );
    }

    public void testCityNameUnique() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE_CODE, BIRTHPLACE_CODE_UNIQUE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCityNameUnique : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    public void testCityNameMultiple() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE_CODE, BIRTHPLACE_CODE_MULTIPLE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCityNameMultiple : " + attributeStatusList.toString());

        // Assert

        assertFalse(attributeStatusList.isEmpty());
        assertEquals(RETURNED_STATUS_ERROR_MULTIPLE_CITIES, attributeStatusList.get(0).getStatus().getCode() );
    }

    public void testCityNameNotFrench() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE, BIRTHPLACE_NAME_GERMAN) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityNotFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCityNameNotFrench : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    public void testCityCodeNotFrench() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE_CODE, BIRTHPLACE_CODE_UNIQUE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityNotFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCityCodeNotFrench : " + attributeStatusList.toString());

        // Assert
        assertFalse(attributeStatusList.isEmpty());
        assertEquals(RETURNED_STATUS_ERROR_NOT_CREATED, attributeStatusList.get(0).getStatus().getCode() );
    }

    public void testCityNameAndCode() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE, BIRTHPLACE_NAME_UNIQUE) );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE_CODE, BIRTHPLACE_CODE_UNIQUE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testCityNameAndCode : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    public void testCityCodeOnlyNoCountry() {
    // Initialize objects
    List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
    attributeDtoList.add( this.createAttribute(KEY_BIRTHPLACE_CODE, BIRTHPLACE_CODE_UNIQUE) );

    IdentityChangeRequest request = this.createRequest(attributeDtoList);
    IdentityDto existingIdentity = this.createExistingIdentityBase();

    // Create test
    List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
    System.out.println( " -=-=-=-=-=-=-=-= retour testCityCodeOnlyNoCountry : " + attributeStatusList.toString());

    // Assert
    assertTrue(attributeStatusList.isEmpty());
}

    public void testFranceToOtherCountry() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHCOUNTRY_CODE, COUNTRY_CODE_GERMANY) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testFranceToOtherCountry : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    public void testOtherCountryToFrance() {
        // Initialize objects
        List<AttributeDto> attributeDtoList = new ArrayList<AttributeDto>( );
        attributeDtoList.add( this.createAttribute(KEY_BIRTHCOUNTRY_CODE, COUNTRY_CODE_FRANCE) );

        IdentityChangeRequest request = this.createRequest(attributeDtoList);
        IdentityDto existingIdentity = this.createExistingIdentityNotFrance();

        // Create test
        List<AttributeStatus> attributeStatusList = IdentityAttributeGeocodesAdjustmentService.instance().adjustGeocodesAttributes(request, existingIdentity);
        System.out.println( " -=-=-=-=-=-=-=-= retour testOtherCountryToFrance : " + attributeStatusList.toString());

        // Assert
        assertTrue(attributeStatusList.isEmpty());
    }

    // Tool methods
    private AttributeDto createAttribute(String key, String value)
    {
        AttributeDto attributeDto = new AttributeDto( );

        attributeDto.setKey(key);
        attributeDto.setValue( value );
        attributeDto.setCertifier( CERT_PROCESS );
        attributeDto.setCertificationDate( DATE_NOW );

        return attributeDto;
    }

    private IdentityChangeRequest createRequest(List<AttributeDto> attributeDtoList )
    {
        IdentityDto identityDto = new IdentityDto( );
        identityDto.setAttributes(new ArrayList<>());
        identityDto.getAttributes().addAll(attributeDtoList);

        IdentityChangeRequest request = new IdentityChangeRequest( );
        request.setIdentity( identityDto );

        return request;
    }

    private IdentityDto createExistingIdentityBase()
    {
        IdentityDto identityDto = new IdentityDto( );
        identityDto.setAttributes( new ArrayList<>( ) );
        identityDto.getAttributes().add( this.createAttribute( KEY_FIRST_NAME, FAMILY_NAME ) );
        identityDto.getAttributes().add( this.createAttribute( KEY_FAMILY_NAME, FAMILY_NAME ) );
        identityDto.getAttributes().add( this.createAttribute( KEY_BIRTH_DATE, BIRTH_DATE ) );

        return identityDto;
    }

    private IdentityDto createExistingIdentityFrance()
    {
        IdentityDto identityDto = this.createExistingIdentityBase();
        identityDto.getAttributes().add( this.createAttribute( KEY_BIRTHCOUNTRY, COUNTRY_NAME_FRANCE ) );
        identityDto.getAttributes().add( this.createAttribute( KEY_BIRTHCOUNTRY_CODE, COUNTRY_CODE_FRANCE ) );

        return identityDto;
    }

    private IdentityDto createExistingIdentityNotFrance()
    {
        IdentityDto identityDto = this.createExistingIdentityBase();
        identityDto.getAttributes().add( this.createAttribute( KEY_BIRTHCOUNTRY, COUNTRY_NAME_FRANCE ) );
        identityDto.getAttributes().add( this.createAttribute( KEY_BIRTHCOUNTRY_CODE, COUNTRY_CODE_GERMANY ) );

        return identityDto;
    }
}

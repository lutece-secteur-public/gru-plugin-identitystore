package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IdentityMockUtils {

    public static final int DEC = 100;
    public static final int NUM2 = 330;
    public static final int NUM1 = 400;
    public static final int ORIG1 = 500;

    public static IdentityDto getMockIdentityDto(final String gender, final int genderLevel, final String lastName, final int lastNameLevel,
                                                 final String firstName, final int firstNameLevel,
                                                 final String birthDate, final int birthDateLevel, final String birthPlaceCode, final int birthPlaceCodeLevel,
                                                 final String birthCountryCode, final int birthCountryCodeLevel)
    {
        final IdentityDto identity = new IdentityDto( );
        final List<AttributeDto> attributeList = new ArrayList<>( );

        if ( gender != null )
            attributeList.add( getMockAttribute(Constants.PARAM_GENDER, gender, genderLevel));
        if ( lastName != null )
            attributeList.add( getMockAttribute( Constants.PARAM_FAMILY_NAME, lastName, lastNameLevel ) );
        if ( firstName != null )
            attributeList.add( getMockAttribute( Constants.PARAM_FIRST_NAME, firstName, firstNameLevel ) );
        if ( birthDate != null )
            attributeList.add( getMockAttribute( Constants.PARAM_BIRTH_DATE, birthDate, birthDateLevel ) );
        if ( birthPlaceCode != null )
            attributeList.add( getMockAttribute( Constants.PARAM_BIRTH_PLACE_CODE, birthPlaceCode, birthPlaceCodeLevel ) );
        if ( birthCountryCode != null )
            attributeList.add( getMockAttribute( Constants.PARAM_BIRTH_COUNTRY_CODE, birthCountryCode, birthCountryCodeLevel ) );

        identity.setAttributes( attributeList );

        return identity;
    }

    public static AttributeDto getMockAttribute( final String key, final String value, final int level )
    {
        AttributeDto attribute = new AttributeDto( );
        attribute.setKey( key );
        attribute.setCertificationLevel( level );
        attribute.setValue( value );
        attribute.setCertifier( getCertifier( level ) );
        attribute.setCertificationDate(new Date());

        return attribute;
    }

    private static String getCertifier( final int level )
    {
        switch( level )
        {
            case DEC:
                return "DEC";
            case ORIG1:
                return "ORIG1";
            case NUM1:
                return "NUM1";
            case NUM2:
                return "NUM2";
        }

        return "?";
    }

}

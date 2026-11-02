package fr.paris.lutece.plugins.identitystore.util;

import fr.paris.lutece.plugins.geocodes.business.City;
import fr.paris.lutece.plugins.geocodes.business.Country;
import fr.paris.lutece.plugins.geocodes.provider.GeoCodeProviderService;
import fr.paris.lutece.plugins.geocodes.provider.IGeoCodeProvider;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In memory geocodes provider replacing the local provider of the geocodes plugin, which is not available in the test context
 */
public class MockGeoCodeProvider implements IGeoCodeProvider
{
    public static final String PROVIDER_ID = "geocodes.geoCodeProviderLocal";
    private static final String CODE_COUNTRY_FRANCE = "99100";
    private static final Date DATE_VALIDITY_START = Date.from( LocalDate.of( 1943, 1, 1 ).atStartOfDay( ZoneId.systemDefault( ) ).toInstant( ) );

    private final List<City> _listCities = new ArrayList<>( );

    /**
     * Constructor, initializing the cities
     */
    public MockGeoCodeProvider( )
    {
        _listCities.add( createCity( 1, "34172", "MONTPELLIER" ) );
        _listCities.add( createCity( 2, "1004", "AMBERIEU-EN-BUGEY" ) );
        _listCities.add( createCity( 3, "1004", "AMBERIEU-EN-BUGEY-ANCIENNE" ) );
    }

    /**
     * Registers this provider in the geocodes provider service, if no provider with the same id is already registered
     */
    public static void register( )
    {
        final List<IGeoCodeProvider> providers = GeoCodeProviderService.getInstance( ).getGeoCodeProviders( );
        if ( providers.stream( ).noneMatch( provider -> PROVIDER_ID.equals( provider.getId( ) ) ) )
        {
            providers.add( new MockGeoCodeProvider( ) );
        }
    }

    /**
     * Creates a french city valid since 1943
     *
     * @param nId
     *            the id
     * @param strCode
     *            the code
     * @param strValue
     *            the label
     * @return the city
     */
    private static City createCity( final int nId, final String strCode, final String strValue )
    {
        final City city = new City( );
        city.setId( nId );
        city.setCode( strCode );
        city.setValue( strValue );
        city.setValueMin( strValue );
        city.setValueMinComplete( strValue );
        city.setCodeCountry( CODE_COUNTRY_FRANCE );
        city.setDateValidityStart( DATE_VALIDITY_START );
        return city;
    }

    /**
     * Tells if the city is valid at the given date
     *
     * @param city
     *            the city
     * @param date
     *            the date
     * @return true if the city is valid at the given date
     */
    private static boolean isValidAt( final City city, final Date date )
    {
        return !city.getDateValidityStart( ).after( date ) && ( city.getDateValidityEnd( ) == null || !city.getDateValidityEnd( ).before( date ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId( )
    {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<City> getCityByDateAndCode( final Date date, final String strCode )
    {
        return _listCities.stream( ).filter( city -> city.getCode( ).equals( strCode ) && isValidAt( city, date ) ).findFirst( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getCitiesListByNameAndDate( final String strName, final Date date )
    {
        return getCitiesListByName( strName ).stream( ).filter( city -> isValidAt( city, date ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Country> getCountryByCodeAndDate( final Date date, final String strCode )
    {
        return Optional.empty( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getCitiesListByNameAndDateLike( final String strName, final Date date )
    {
        return getCitiesListByNameLike( strName ).stream( ).filter( city -> isValidAt( city, date ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getCitiesListByNameLike( final String strName )
    {
        return _listCities.stream( ).filter( city -> city.getValue( ).toUpperCase( ).startsWith( strName.toUpperCase( ) ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getCitiesListByName( final String strName )
    {
        return _listCities.stream( ).filter( city -> city.getValue( ).equalsIgnoreCase( strName ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<City> getCityByCode( final String strCode )
    {
        return _listCities.stream( ).filter( city -> city.getCode( ).equals( strCode ) ).collect( Collectors.toList( ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCitiesCodesListByDate( final Date date )
    {
        return Collections.unmodifiableList( _listCities.stream( ).filter( city -> isValidAt( city, date ) ).map( City::getCode ).distinct( )
                .collect( Collectors.toList( ) ) );
    }
}

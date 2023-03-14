/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitystore.service.geocodes;

import fr.paris.lutece.plugins.identitystore.business.geocodes.City;
import fr.paris.lutece.plugins.identitystore.business.geocodes.CityHome;
import fr.paris.lutece.plugins.identitystore.business.geocodes.Country;
import fr.paris.lutece.plugins.identitystore.business.geocodes.CountryHome;

import java.util.List;
import java.util.Optional;

public class GeoCodesService
{

    /**
     * get city by code
     * 
     * @param strCodel
     * @return the city (as Optional)
     */
    public static Optional<City> getCityByCode( String strCode )
    {
        return CityHome.findByCode( strCode );
    }

    /**
     * search cities by name beginning with a string
     * 
     * @param strSearchBeginningVal
     * @return the list
     */
    public static List<City> getCitiesListByName( String strSearchBeginningVal )
    {
        return CityHome.getCitiesListByName( strSearchBeginningVal );
    }

    /**
     * get country by code
     * 
     * @param strCode
     * @return the country (as Optional)
     */
    public static Optional<Country> getCountryByCode( String strCode )
    {
        return CountryHome.findByCode( strCode );
    }

    /**
     * search countries by name
     * 
     * @param strSearchBeginningVal
     * @return the list
     */
    public static List<Country> getCountriesListByName( String strSearchBeginningVal )
    {
        return CountryHome.getCountriesListByName( strSearchBeginningVal );
    }

}

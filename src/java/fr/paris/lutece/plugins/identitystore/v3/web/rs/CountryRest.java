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

package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.business.geocodes.City;
import fr.paris.lutece.plugins.identitystore.business.geocodes.Country;
import fr.paris.lutece.plugins.identitystore.service.geocodes.GeoCodesService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.geocodes.*;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CountryRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 + Constants.COUNTRY_PATH )
public class CountryRest
{

    /**
     * Get Country List
     * 
     * @return the Country List
     */
    @GET
    @Path( StringUtils.EMPTY )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCountryList( )
    {
        final CountrySearchResponse entity = new CountrySearchResponse( );
        final List<Country> listCountries = GeoCodesService.getCountriesListByName( "%" );

        if ( listCountries.isEmpty( ) )
        {
            entity.setStatus( GeocodesSearchStatus.NO_CONTENT );
        }
        else
        {
            final List<CountryDto> countryDtos = listCountries.stream( ).map( country -> new CountryDto( country.getCode( ), country.getName( ) ) )
                    .collect( Collectors.toList( ) );
            entity.setCountries( countryDtos );
            entity.setStatus( GeocodesSearchStatus.OK );
        }
        return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Get Country
     * 
     * @param code
     *            the code
     * @return the Country
     */
    @GET
    @Path( Constants.ID_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCountry( @PathParam( Constants.ID ) String code )
    {
        final CountrySearchResponse entity = new CountrySearchResponse( );
        Optional<Country> optCountry = GeoCodesService.getCountryByCode( code );
        if ( !optCountry.isPresent( ) )
        {
            entity.setStatus( GeocodesSearchStatus.NOT_FOUND );
        }
        else
        {
            final Country country = optCountry.get( );
            final CountryDto countryDto = new CountryDto( country.getCode( ), country.getName( ) );
            entity.setCountries( Arrays.asList( countryDto ) );
            entity.setStatus( GeocodesSearchStatus.OK );
        }
        return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

    /**
     * Search Countries
     * 
     * @param strVal
     *            the searched string
     * @return the Countries
     */
    @GET
    @Path( Constants.SEARCH_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCountriesByName( @PathParam( Constants.SEARCHED_STRING ) String strVal )
    {
        final CountrySearchResponse entity = new CountrySearchResponse( );
        if ( strVal == null || strVal.length( ) < 3 )
        {
            AppLogService.error( Constants.ERROR_SEARCH_STRING );
            return Response.status( Response.Status.BAD_REQUEST )
                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.BAD_REQUEST.name( ), Constants.ERROR_SEARCH_STRING ) ) )
                    .build( );
        }

        List<Country> listCountries = GeoCodesService.getCountriesListByName( strVal );
        if ( listCountries.isEmpty( ) )
        {
            entity.setStatus( GeocodesSearchStatus.NO_CONTENT );
        }
        else
        {
            final List<CountryDto> countryDtos = listCountries.stream( ).map( country -> new CountryDto( country.getCode( ), country.getName( ) ) )
                    .collect( Collectors.toList( ) );
            entity.setCountries( countryDtos );
            entity.setStatus( GeocodesSearchStatus.OK );
        }
        return Response.status( entity.getStatus( ).getCode( ) ).entity( entity ).type( MediaType.APPLICATION_JSON_TYPE ).build( );
    }

}

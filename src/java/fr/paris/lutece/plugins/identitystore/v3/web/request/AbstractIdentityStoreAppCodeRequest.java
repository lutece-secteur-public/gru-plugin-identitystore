/*
 * Copyright (c) 2002-2024, City of Paris
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
package fr.paris.lutece.plugins.identitystore.v3.web.request;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.service.application.ClientApplicationService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.AbstractIdentityStoreRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractIdentityStoreAppCodeRequest extends AbstractIdentityStoreRequest
{
    private static final List<String> EXCEPTION_APP_CODES = Arrays
            .stream( AppPropertiesService.getProperty( "identitystore.header.application.code.verif.exception", "" ).split( "," ) )
            .collect( Collectors.toList( ) );
    private static final String APPCODE_DEFAULT_REGEXP = "^[A-Za-z][0-9]{2}";  // => "must start with one alphabetical character and two numerical caracters"
    private static final String APPCODE_REGEXP = AppPropertiesService.getProperty( "identitystore.header.application.code.verif.regexp", APPCODE_DEFAULT_REGEXP );
    
    protected final String _strAppCode;

    protected AbstractIdentityStoreAppCodeRequest( final String strClientCode, final String strAppCode, final String authorName, final String authorType )
            throws RequestFormatException
    {
        super( strClientCode, authorName, authorType );
        this._strAppCode = strAppCode;
    }

    @Override
    protected void validateRequestHeaderFormat( ) throws RequestFormatException
    {
        super.validateRequestHeaderFormat( );
        
        // check if client code is related to the AppCode
        
        // if appCode is not provided, or this is an authorized AppCode => do not check
        if ( StringUtils.isBlank( _strAppCode ) || EXCEPTION_APP_CODES.contains( _strAppCode ) )
        {
            return;
        }
        
        // check if appCode corresponds to (or starts with) a valid pattern of application code :
        final Pattern pattern = Pattern.compile( APPCODE_REGEXP );
        final Matcher matcher = pattern.matcher( _strAppCode );
        boolean isValidAppCodePattern = matcher.find( );
        
        if ( !isValidAppCodePattern )
        {
            // this code doesn't correspond to (or starts with) a valid appCode, can't check
            return;
        }
        
        String strAppCode  = matcher.group(0) ;
        final List<String> clientCodeList = ClientApplicationService.instance( ).getClientCodes( strAppCode );
        
        // Check if the client code exists and is related to the AppCode        
        if ( clientCodeList.stream( ).noneMatch( clientCode -> clientCode.equals( _strClientCode ) ) )
        {
            throw new RequestFormatException( "The provided client code and application code are not correlated.",
                    Constants.PROPERTY_REST_ERROR_CLIENT_AND_APP_CODE_NOT_MATCHING );
        }

    }

}

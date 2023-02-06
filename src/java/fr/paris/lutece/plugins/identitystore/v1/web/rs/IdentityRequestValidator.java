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
package fr.paris.lutece.plugins.identitystore.v1.web.rs;

import java.util.List;
import java.util.Map;

import fr.paris.lutece.plugins.identitystore.business.application.ClientApplication;
import fr.paris.lutece.plugins.identitystore.business.application.ClientApplicationHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKeyHome;
import fr.paris.lutece.plugins.identitystore.business.attribute.KeyType;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityAttribute;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.plugins.identitystore.service.IdentityStoreService;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityChangeDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v1.web.rs.util.Constants;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;

/**
 *
 * util class used to validate identity store request
 *
 */
public final class IdentityRequestValidator
{
    /**
     * singleton
     */
    private static IdentityRequestValidator _singleton;

    /**
     * private constructor
     */
    private IdentityRequestValidator( )
    {

    }

    /**
     * return singleton's instance
     * 
     * @return IdentityRequestValidator
     */
    public static IdentityRequestValidator instance( )
    {
        if ( _singleton == null )
        {
            try
            {
                _singleton = new IdentityRequestValidator( );
            }
            catch( Exception e )
            {
                AppLogService.error( "Error when instantiating IdentityRequestValidator instance" + e.getMessage( ), e );
            }
        }

        return _singleton;
    }

    /**
     * check whether the parameters related to the application are valid or not
     *
     * @param strClientCode
     *            client application code
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkClientApplication( String strClientCode ) throws AppException
    {
        if ( StringUtils.isBlank( strClientCode ) )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " is missing" );
        }

        ClientApplication clientApp = ClientApplicationHome.findByCode( strClientCode );

        if ( clientApp == null )
        {
            throw new AppException( Constants.PARAM_CLIENT_CODE + " : " + strClientCode + " is unknown " );
        }
    }

    /**
     * check whether the parameters related to the identity are valid or not
     *
     * @param strConnectionId
     *            the connection id
     * @param strCustomerId
     *            the customer id
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkIdentity( String strConnectionId, String strCustomerId ) throws AppException
    {
        if ( StringUtils.isBlank( strConnectionId ) && ( StringUtils.isBlank( strCustomerId ) ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " AND " + Constants.PARAM_ID_CUSTOMER + " are missing, at least one must be provided" );
        }
    }

    /**
     * check whether the parameters related to the identity change are valid or not
     *
     * @param identityChange
     *            the identity change
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkIdentityChange( IdentityChangeDto identityChange ) throws AppException
    {
        if ( ( identityChange == null ) || ( identityChange.getIdentity( ) == null ) )
        {
            throw new AppException( "Provided IdentityChange / Identity is null" );
        }

        if ( identityChange.getAuthor( ) == null )
        {
            throw new AppException( "Provided Author is null" );
        }
    }

    /**
     * @param strConnectionId
     *            connectionId (must not be empty)
     * @param strClientCode
     *            client application code (must not be empty)
     * @param strAttributeKey
     *            attribute key containing file (must not be empty)
     * @throws AppException
     *             thrown if input parameters are not valid or no file is found
     */
    public void checkDownloadFileAttributeParams( String strConnectionId, String strClientCode, String strAttributeKey ) throws AppException
    {
        if ( StringUtils.isBlank( strConnectionId ) )
        {
            throw new AppException( Constants.PARAM_ID_CONNECTION + " is null or empty" );
        }

        if ( StringUtils.isBlank( strAttributeKey ) )
        {
            throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " is null or empty" );
        }

        checkClientApplication( strClientCode );
    }

    /**
     * check attached files are present in identity Dto and that attributes to update exist and are writable (or not writable AND unchanged)
     *
     * @param identityDto
     *            identityDto with list of attributes
     * @param nServiceContractId
     *            service contract to check right
     * @param mapAttachedFiles
     *            map of attached files
     * @throws AppException
     *             thrown if provided attributes are not valid
     */
    public void checkAttributes( IdentityDto identityDto, String strClientAppCode, int nServiceContractId, Map<String, File> mapAttachedFiles )
            throws AppException
    {
        if ( ( mapAttachedFiles != null ) && !mapAttachedFiles.isEmpty( ) )
        {
            // check if input files is present in identity DTO attributes
            for ( Map.Entry<String, File> entry : mapAttachedFiles.entrySet( ) )
            {
                boolean bFound = false;

                for ( AttributeDto attributeDto : identityDto.getAttributes( ).values( ) )
                {
                    AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey( ) );

                    if ( attributeKey == null )
                    {
                        throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey( ) + " is provided but does not exist" );
                    }

                    // check that attribute is file type and that its name is matching
                    if ( attributeKey.getKeyType( ).equals( KeyType.FILE ) && StringUtils.isNotBlank( attributeDto.getValue( ) )
                            && attributeDto.getValue( ).equals( entry.getKey( ) ) )
                    {
                        bFound = true;

                        break;
                    }
                }

                if ( !bFound )
                {
                    throw new AppException( Constants.PARAM_FILE + " " + entry.getKey( ) + " is provided but its attribute is missing" );
                }
            }
        }

        if ( identityDto.getAttributes( ) != null )
        {
            List<AttributeRight> lstRights = ServiceContractHome.findByPrimaryKey( nServiceContractId ).map( ServiceContractHome::selectApplicationRights )
                    .orElseThrow( ( ) -> new AppException( "Service Contract with the id " + nServiceContractId + " doesn't exist" ) );

            // check that all file attribute type provided with filename in dto have
            // matching attachements
            for ( AttributeDto attributeDto : identityDto.getAttributes( ).values( ) )
            {
                AttributeKey attributeKey = AttributeKeyHome.findByKey( attributeDto.getKey( ) );

                if ( attributeKey == null )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeDto.getKey( ) + " is provided but does not exist" );
                }

                for ( AttributeRight attRight : lstRights )
                {
                    if ( attRight.getAttributeKey( ).getId( ) == attributeKey.getId( ) )
                    {
                        IdentityAttribute attribute = IdentityStoreService.getAttribute( identityDto.getConnectionId( ),
                                attRight.getAttributeKey( ).getKeyName( ), strClientAppCode );

                        // TODO if provided attribute is writable, or if no change => ok
                        /*
                         * if ( attRight.isWritable( ) || ( ( attribute != null ) && attributeDto.getValue( ).equals( attribute.getValue( ) ) ) ) { break; }
                         * else { throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName( ) + " is provided but is not writable"
                         * ); }
                         */
                    }
                }

                if ( attributeKey.getKeyType( ).equals( KeyType.FILE ) && StringUtils.isNotBlank( attributeDto.getValue( ) )
                        && ( ( mapAttachedFiles == null ) || ( mapAttachedFiles.get( attributeDto.getValue( ) ) == null ) ) )
                {
                    throw new AppException( Constants.PARAM_ATTRIBUTE_KEY + " " + attributeKey.getKeyName( ) + " is provided with filename="
                            + attributeDto.getValue( ) + " but no file is attached" );
                }
            }
        }
    }

    /**
     * Check certification authorization, in this order - clientApplication has to got the certifier - certifier and clientApplication must have certification
     * right on all attributs of the identitychange given
     * 
     * @param identityDto
     *            the identity change
     * @param certifier
     *            the certifier
     * @throws AppException
     *             thrown if one of this rule is not ok
     */
    public void checkCertification( IdentityDto identityDto, int nServiceContractId, AbstractCertifier certifier ) throws AppException
    {
        if ( MapUtils.isEmpty( identityDto.getAttributes( ) ) )
        {
            throw new AppException( "No attributes given for certification " );
        }
        ServiceContract serviceContract = ServiceContractHome.findByPrimaryKey( nServiceContractId )
                .orElseThrow( ( ) -> new AppException( "Service Contract with the id " + nServiceContractId + " doesn't exist" ) );
        List<AttributeRight> listRights = ServiceContractHome.selectApplicationRights( serviceContract );
        List<AbstractCertifier> listCertifier = ClientApplicationHome.getCertifiers( null ); // TODO serviceContract.getClientApplication() );
        // rule 1, client application has the certifier
        boolean bCertifierOk = false;
        for ( AbstractCertifier certifierApp : listCertifier )
        {
            if ( certifierApp.getCode( ).equals( certifier.getCode( ) ) )
            {
                bCertifierOk = true;
                break;
            }
        }
        String strClientAppCode = ""; // TODO serviceContract.getClientApplication().getCode();
        if ( !bCertifierOk )
        {
            throw new AppException( "ClientApplication [" + strClientAppCode + "] has no right to use certifier [" + certifier.getCode( ) + "]" );
        }

        // next check all attribute is certifiable
        List<String> listAttributsCertifier = certifier.getCertifiableAttributesList( );
        for ( String strAttributeKey : identityDto.getAttributes( ).keySet( ) )
        {
            if ( !listAttributsCertifier.contains( strAttributeKey ) )
            {
                throw new AppException( "Certifier [" + certifier.getCode( ) + "] has no right to certify [" + strAttributeKey + "]" );
            }
            /*
             * TODO else { for ( AttributeRight attributeRight : listRights ) { if ( attributeRight.getAttributeKey( ).getKeyName( ).equals( strAttributeKey )
             * && !attributeRight.isCertifiable( ) ) { throw new AppException( "ClientApplication [" + strClientAppCode + "] has no right to certify [" +
             * strAttributeKey + "]" ); } } }
             */
        }
    }
}

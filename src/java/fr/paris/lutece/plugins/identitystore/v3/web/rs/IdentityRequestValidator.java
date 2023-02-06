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

import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContractHome;
import fr.paris.lutece.plugins.identitystore.service.certifier.AbstractCertifier;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierNotFoundException;
import fr.paris.lutece.plugins.identitystore.service.certifier.CertifierRegistry;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.dto.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v2.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.merge.IdentityMergeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public void checkClientApplication( String strClientCode ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strClientCode ) )
        {
            throw new IdentityStoreException( Constants.PARAM_CLIENT_CODE + " is missing" );
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
    public void checkIdentity( String strConnectionId, String strCustomerId ) throws IdentityStoreException
    {
        if ( StringUtils.isBlank( strConnectionId ) && ( StringUtils.isBlank( strCustomerId ) ) )
        {
            throw new IdentityStoreException(
                    Constants.PARAM_ID_CONNECTION + " AND " + Constants.PARAM_ID_CUSTOMER + " are missing, at least one must be provided" );
        }
    }

    /**
     * check whether the parameters related to the identity are valid or not
     * 
     * @param identityChange
     * @throws AppException
     */
    public void checkIdentityChange( IdentityChangeRequest identityChange ) throws IdentityStoreException
    {
        if ( identityChange == null || identityChange.getIdentity( ) == null || CollectionUtils.isEmpty( identityChange.getIdentity( ).getAttributes( ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Change request is null or empty" );
        }

        if ( identityChange.getOrigin( ) == null )
        {
            throw new IdentityStoreException( "Provided Author is null" );
        }
    }

    public void checkIdentitySearch( IdentitySearchRequest identitySearch ) throws IdentityStoreException
    {
        if ( identitySearch.getSearch( ) == null || identitySearch.getSearch( ).getAttributes( ) == null
                || identitySearch.getSearch( ).getAttributes( ).isEmpty( ) )
        {
            throw new IdentityStoreException( "Provided Identity Search request is null or empty" );
        }
    }

    /**
     * Check whether the parameters related to the identity merge request are valid or not
     *
     * @param identityMergeRequest
     *            the identity merge request
     * @throws AppException
     *             if the parameters are not valid
     */
    public void checkMergeRequest( IdentityMergeRequest identityMergeRequest ) throws IdentityStoreException
    {
        if ( identityMergeRequest == null || identityMergeRequest.getIdentities( ) == null
                || StringUtils.isEmpty( identityMergeRequest.getIdentities( ).getPrimaryCuid( ) )
                || StringUtils.isEmpty( identityMergeRequest.getIdentities( ).getSecondaryCuid( ) ) )
        {
            throw new IdentityStoreException( "Provided Identity Merge request is null or empty" );
        }
    }

    /**
     * check attached files are present in identity Dto and that attributes to update exist and are writable (or not writable AND unchanged)
     *
     * @param mapAttributeValues
     *            map of attached files
     * @param nServiceContractId
     *            service contract to check right
     * @throws AppException
     *             thrown if provided attributes are not valid
     */
    public void checkSearchAttributes( Map<String, List<String>> mapAttributeValues, int nServiceContractId ) throws IdentityStoreException
    {
        ServiceContract serviceContract = ServiceContractHome.findByPrimaryKey( nServiceContractId )
                .orElseThrow( ( ) -> new AppException( "Service Contract with the id " + nServiceContractId + " doesn't exist" ) );
        String strClientAppCode = ""; // TODO serviceContract.getClientApplication().getCode();
        List<AttributeRight> listAttributeRight = ServiceContractHome.selectApplicationRights( serviceContract );

        if ( ( mapAttributeValues != null ) && !mapAttributeValues.isEmpty( ) )
        {
            for ( String strAttributeKeyName : mapAttributeValues.keySet( ) )
            {
                for ( AttributeRight attributeRight : listAttributeRight )
                {
                    if ( attributeRight.getAttributeKey( ).getKeyName( ).equals( strAttributeKeyName ) )
                    {
                        if ( !attributeRight.isSearchable( ) )
                        {
                            throw new IdentityStoreException(
                                    "The attribute " + strAttributeKeyName + " is provided but not searchable for " + strClientAppCode );
                        }
                    }
                }
            }
        }
    }

    /**
     * Check certification authorization, in this order - clientApplication has to got the certifier - certifier and clientApplication must have certification
     * right on all attributs of the identitychange given
     * 
     * @param identityDto
     *            the identity to check
     * @param nServiceContractId
     *            service contract to check right
     * @throws AppException
     *             thrown if one of this rule is not ok
     * @throws CertifierNotFoundException
     */
    public void checkCertification( IdentityDto identityDto, int nServiceContractId ) throws AppException
    {
        if ( MapUtils.isEmpty( identityDto.getAttributes( ) ) )
        {
            throw new AppException( "No attributes given for certification " );
        }
        ServiceContract serviceContract = ServiceContractHome.findByPrimaryKey( nServiceContractId )
                .orElseThrow( ( ) -> new AppException( "Service Contract with the id " + nServiceContractId + " doesn't exist" ) );
        String strClientAppCode = ""; // TODO serviceContract.getClientApplication().getCode();
        List<AttributeRight> listRights = ServiceContractHome.selectApplicationRights( serviceContract );
        // TODO search certifier on the service contract
        List<AbstractCertifier> listCertifier = Collections.emptyList( );
        // List<AbstractCertifier> listCertifier = ClientApplicationHome.getCertifiers( clientApp );

        // for each attribute retrieve certifier to control rights
        for ( String strAttributeKey : identityDto.getAttributes( ).keySet( ) )
        {
            if ( identityDto.getAttributes( ).get( strAttributeKey ).getCertificate( ) == null )
            {
                continue;
            }
            // rule 1, client application has the certifier and the certifier exists
            String strCertifierCode = identityDto.getAttributes( ).get( strAttributeKey ).getCertificate( ).getCertifierCode( );
            boolean bCertifierOk = false;
            List<String> listAttributsCertifier;
            try
            {
                AbstractCertifier certifier = CertifierRegistry.instance( ).getCertifier( strCertifierCode );
                listAttributsCertifier = certifier.getCertifiableAttributesList( );
            }
            catch( CertifierNotFoundException e )
            {
                throw new AppException( "Certifier [" + strCertifierCode + "] doesn't exists" );
            }
            for ( AbstractCertifier certifierApp : listCertifier )
            {
                if ( certifierApp.getCode( ).equals( strCertifierCode ) )
                {
                    bCertifierOk = true;
                    break;
                }
            }
            if ( !bCertifierOk )
            {
                throw new AppException( "ClientApplication [" + strClientAppCode + "] with the ServiceContract [" + nServiceContractId
                        + "] has no right to use certifier [" + strCertifierCode + "]" );
            }

            // rule 2 application and certifier allow on attribute
            if ( !listAttributsCertifier.contains( strAttributeKey ) )
            {
                throw new AppException( "Certifier [" + strCertifierCode + "] has no right to certify [" + strAttributeKey + "]" );
            }
            else
            {
                for ( AttributeRight attributeRight : listRights )
                {
                    // TODO change to find if certifiable on service contract
                    /*
                     * if ( attributeRight.getAttributeKey( ).getKeyName( ).equals( strAttributeKey ) && !attributeRight.isCertifiable( ) ) { throw new
                     * AppException( "ClientApplication [" + strClientAppCode + "] has no right to certify [" + strAttributeKey + "]" ); }
                     */
                }
            }
        }
    }
}

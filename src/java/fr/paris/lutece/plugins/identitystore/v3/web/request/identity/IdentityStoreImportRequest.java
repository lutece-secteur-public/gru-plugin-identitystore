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
package fr.paris.lutece.plugins.identitystore.v3.web.request.identity;

import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationLevelHome;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeFormatterService;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeGeocodesAdjustmentService;
import fr.paris.lutece.plugins.identitystore.service.contract.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.v3.web.request.AbstractIdentityStoreAppCodeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.request.validator.IdentityDuplicateValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.DtoConverter;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.IdentityRequestValidator;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ClientAuthorizationException;
import fr.paris.lutece.plugins.identitystore.web.exception.DuplicatesConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestContentFormattingException;
import fr.paris.lutece.plugins.identitystore.web.exception.RequestFormatException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class IdentityStoreImportRequest extends AbstractIdentityStoreAppCodeRequest
{
    private final IdentityChangeRequest _identityChangeRequest;
    private final List<AttributeStatus> formatStatuses = new ArrayList<>();

    private ServiceContract serviceContract;
    private String strictDuplicateCustomerId;

    /**
     * Constructor of IdentityStoreImportRequest
     *
     * @param identityChangeRequest
     *            the dto of identity's change
     */
    public IdentityStoreImportRequest( final IdentityChangeRequest identityChangeRequest, final String strClientCode, final String strAppCode,
            final String authorName, final String authorType ) throws IdentityStoreException
    {
        super( strClientCode, strAppCode, authorName, authorType );
        this._identityChangeRequest = identityChangeRequest;
    }

    @Override
    protected void fetchResources( ) throws ResourceNotFoundException, ClientAuthorizationException {
        serviceContract = ServiceContractService.instance( ).getActiveServiceContract( _strClientCode );
    }

    @Override
    protected void validateRequestFormat( ) throws RequestFormatException
    {
        IdentityRequestValidator.instance( ).checkIdentityChange( _identityChangeRequest, false );

    }

    @Override
    protected void validateClientAuthorization( ) throws ClientAuthorizationException
    {
        ServiceContractService.instance( ).validateImportAuthorization( _identityChangeRequest, serviceContract );
    }

    @Override
    protected void validateResourcesConsistency( ) throws ResourceConsistencyException
    {
        // do nothing, it will be done in subsequent request (create or update)
    }

    @Override
    protected void formatRequestContent( ) throws RequestContentFormattingException
    {
        formatStatuses.addAll( IdentityAttributeFormatterService.instance( ).formatIdentityChangeRequestAttributeValues( _identityChangeRequest ) );
        formatStatuses.addAll( IdentityAttributeGeocodesAdjustmentService.instance( ).adjustGeocodesAttributes( _identityChangeRequest ) );
    }

    @Override
    protected void checkDuplicatesConsistency( ) throws DuplicatesConsistencyException
    {
        strictDuplicateCustomerId = IdentityDuplicateValidator.instance( ).checkDuplicateExistenceForImport( _identityChangeRequest );
    }

    @Override
    protected IdentityChangeResponse doSpecificRequest( ) throws IdentityStoreException
    {
        if ( strictDuplicateCustomerId == null )
        {
            final IdentityChangeResponse identityChangeResponse = (IdentityChangeResponse) new IdentityStoreCreateRequest(_identityChangeRequest, _strClientCode, _strAppCode, _author.getName(),
                    _author.getType().name()).doRequest();
            identityChangeResponse.getStatus().getAttributeStatuses().addAll( formatStatuses );
            return identityChangeResponse;
        }
        else
        {
            IdentityDto identityDuplicate = DtoConverter.convertIdentityToDto( IdentityHome.findByCustomerId( strictDuplicateCustomerId ) );

            try
            {
                identityDuplicate.getAttributes().forEach(attributeDto -> {
                    // pour chaque attribut on va comparer entre l'original et celui de la requete
                    _identityChangeRequest.getIdentity().getAttributes().forEach(requestAttribute ->
                        {
                            // si il s'agit du bon attribut
                             if( StringUtils.equals(attributeDto.getKey(), requestAttribute.getKey()) )
                             {
                                 Integer requestCertification = RefAttributeCertificationLevelHome.findfindLevelByProcessusAndAttributeKeyName(requestAttribute.getCertifier(), requestAttribute.getKey());
                                 // si ils ne sont pas égaux alors on va regarder si le niveau de certification est inférieur ou égal
                                 boolean change = !StringUtils.equals(attributeDto.getValue(), requestAttribute.getValue())
                                         && attributeDto.isCertified() && requestAttribute.isCertified()
                                         && attributeDto.getCertificationLevel() >= requestCertification;

                                 if(change)
                                 {
                                    throw new RuntimeException("The identity has different attribute value with lower or equal certification for attribute : " + attributeDto.getKey());
                                 }

                                 //si ils ont égaux on va vérifier si le niveau de certification est inférieur
                                 boolean certif = StringUtils.equals(attributeDto.getValue(), requestAttribute.getValue())
                                         && attributeDto.isCertified() && requestAttribute.isCertified()
                                         && attributeDto.getCertificationLevel() > requestCertification;

                                 if(certif)
                                 {
                                     throw new RuntimeException("The certification is too low for attribute : " + attributeDto.getKey());
                                 }
                             }
                        }
                    );
                });
            }
            catch (RuntimeException e)
            {
                throw new DuplicatesConsistencyException(e.getMessage(), Constants.PROPERTY_REST_INFO_UNABLE_TO_UPDATE);
            }
            final IdentityChangeResponse identityChangeResponse = (IdentityChangeResponse) new IdentityStoreUpdateRequest(strictDuplicateCustomerId, _identityChangeRequest, _strClientCode, _strAppCode,
                    _author.getName(), _author.getType().name()).doRequest();
            identityChangeResponse.getStatus().getAttributeStatuses().addAll( formatStatuses );
            return identityChangeResponse;
        }
    }
}

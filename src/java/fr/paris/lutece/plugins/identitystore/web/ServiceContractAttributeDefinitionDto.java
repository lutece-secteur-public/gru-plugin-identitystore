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
package fr.paris.lutece.plugins.identitystore.web;

import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefAttributeCertificationProcessus;
import fr.paris.lutece.plugins.identitystore.business.referentiel.RefCertificationLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceContractAttributeDefinitionDto
{
    private ServiceContract serviceContract;
    private AttributeKey attributeKey;
    private AttributeRight attributeRight;
    private RefCertificationLevel refCertificationLevel;
    private List<RefAttributeCertificationProcessus> refAttributeCertificationProcessus = new ArrayList<>( );

    private List<RefAttributeCertificationProcessus> compatibleProcessus = new ArrayList<>( );

    public String getDisplayProcessus( )
    {
        return this.getRefAttributeCertificationProcessus( ).stream( ).map( RefAttributeCertificationProcessus::getLabel )
                .collect( Collectors.joining( ", " ) );
    }

    public ServiceContract getServiceContract( )
    {
        return serviceContract;
    }

    public ServiceContractAttributeDefinitionDto setServiceContract( ServiceContract serviceContract )
    {
        this.serviceContract = serviceContract;
        return this;
    }

    public AttributeKey getAttributeKey( )
    {
        return attributeKey;
    }

    public ServiceContractAttributeDefinitionDto setAttributeKey( AttributeKey attributeKey )
    {
        this.attributeKey = attributeKey;
        return this;
    }

    public RefCertificationLevel getRefCertificationLevel( )
    {
        return refCertificationLevel;
    }

    public ServiceContractAttributeDefinitionDto setRefCertificationLevel( RefCertificationLevel refCertificationLevel )
    {
        this.refCertificationLevel = refCertificationLevel;
        return this;
    }

    public List<RefAttributeCertificationProcessus> getRefAttributeCertificationProcessus( )
    {
        return refAttributeCertificationProcessus;
    }

    public void setRefAttributeCertificationProcessus( List<RefAttributeCertificationProcessus> refAttributeCertificationProcessus )
    {
        this.refAttributeCertificationProcessus = refAttributeCertificationProcessus;
    }

    public AttributeRight getAttributeRight( )
    {
        return attributeRight;
    }

    public ServiceContractAttributeDefinitionDto setAttributeRight( AttributeRight attributeRight )
    {
        this.attributeRight = attributeRight;
        return this;
    }

    public List<RefAttributeCertificationProcessus> getCompatibleProcessus( )
    {
        return compatibleProcessus;
    }
}

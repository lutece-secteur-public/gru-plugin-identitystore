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
package fr.paris.lutece.plugins.identitystore.service.identity;

import com.google.common.util.concurrent.AtomicDouble;
import fr.paris.lutece.plugins.identitystore.business.attribute.AttributeKey;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.business.contract.AttributeRight;
import fr.paris.lutece.plugins.identitystore.business.contract.ServiceContract;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.ExcludedIdentities;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentity;
import fr.paris.lutece.plugins.identitystore.business.duplicates.suspicions.SuspiciousIdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.cache.QualityBaseCache;
import fr.paris.lutece.plugins.identitystore.service.attribute.IdentityAttributeService;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.ConsolidateDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.QualityDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateDefinition;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateExclusion;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.duplicate.IdentityDuplicateSuspicion;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttribute;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceNotFoundException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IdentityQualityService
{
    private static final QualityBaseCache _qualityBaseCache = SpringContextService.getBean( "identitystore.qualityBaseCache" );

    private static IdentityQualityService _instance;

    public static IdentityQualityService instance( )
    {
        if ( _instance == null )
        {
            _instance = new IdentityQualityService( );
            _qualityBaseCache.refresh( );
        }
        return _instance;
    }

    private IdentityQualityService( )
    {
    }

    public void enrich( final List<SearchAttribute> searchAttributes, final IdentityDto identity, final ServiceContract serviceContract, final Identity bean )
    {
        this.enrich( searchAttributes, identity, serviceContract, bean, true );
    }

    public void enrich( final List<SearchAttribute> searchAttributes, final IdentityDto identity, final ServiceContract serviceContract, final Identity bean,
            final boolean computeDuplicateDefinition )
    {
        /* Compute Quality Definition */
        IdentityQualityService.instance( ).computeCoverage( identity, serviceContract );
        IdentityQualityService.instance( ).computeQuality( identity );
        IdentityQualityService.instance( ).computeMatchScore( identity, searchAttributes );

        /* Filter client readable attributes */
        final List<AttributeDto> filteredAttributeValues = identity.getAttributes( ).stream( )
                .filter( certifiedAttribute -> serviceContract.getAttributeRights( ).stream( )
                        .anyMatch( attributeRight -> StringUtils.equals( attributeRight.getAttributeKey( ).getKeyName( ), certifiedAttribute.getKey( ) )
                                && attributeRight.isReadable( ) ) )
                .collect( Collectors.toList( ) );
        identity.getAttributes( ).clear( );
        identity.getAttributes( ).addAll( filteredAttributeValues );

        if ( computeDuplicateDefinition )
        {
            /* Compute Duplicate Definition */
            final SuspiciousIdentity suspiciousIdentity = SuspiciousIdentityHome.selectByCustomerID( identity.getCustomerId( ) );
            if ( suspiciousIdentity != null )
            {
                identity.setDuplicateDefinition( new IdentityDuplicateDefinition( ) );
                final IdentityDuplicateSuspicion duplicateSuspicion = new IdentityDuplicateSuspicion( );
                identity.getDuplicateDefinition( ).setDuplicateSuspicion( duplicateSuspicion );
                duplicateSuspicion.setDuplicateRuleCode( suspiciousIdentity.getDuplicateRuleCode( ) );
                duplicateSuspicion.setCreationDate( suspiciousIdentity.getCreationDate( ) );
            }

            final List<ExcludedIdentities> excludedIdentitiesList = SuspiciousIdentityHome.getExcludedIdentitiesList( identity.getCustomerId( ) );
            if ( CollectionUtils.isNotEmpty( excludedIdentitiesList ) )
            {
                if ( identity.getDuplicateDefinition( ) == null )
                {
                    identity.setDuplicateDefinition( new IdentityDuplicateDefinition( ) );
                }
                identity.getDuplicateDefinition( ).getDuplicateExclusions( ).addAll( excludedIdentitiesList.stream( ).map( excludedIdentities -> {
                    final IdentityDuplicateExclusion exclusion = new IdentityDuplicateExclusion( );
                    exclusion.setExclusionDate( excludedIdentities.getExclusionDate( ) );
                    exclusion.setAuthorName( excludedIdentities.getAuthorName( ) );
                    exclusion.setAuthorType( excludedIdentities.getAuthorType( ) );
                    final String excludedCustomerId = Objects.equals( excludedIdentities.getFirstCustomerId( ), identity.getCustomerId( ) )
                            ? excludedIdentities.getSecondCustomerId( )
                            : excludedIdentities.getFirstCustomerId( );
                    exclusion.setExcludedCustomerId( excludedCustomerId );
                    return exclusion;
                } ).collect( Collectors.toList( ) ) );
            }
        }

        if ( bean != null )
        {
            final List<Identity> mergedIdentities = IdentityHome.findMergedIdentities( bean.getId( ) );
            if ( !mergedIdentities.isEmpty( ) )
            {
                final ConsolidateDefinition consolidateDefinition = new ConsolidateDefinition( );
                for ( final Identity mergedIdentity : mergedIdentities )
                {
                    final IdentityDto mergedDto = new IdentityDto( );
                    mergedDto.setCustomerId( mergedIdentity.getCustomerId( ) );
                    mergedDto.setConnectionId( mergedIdentity.getConnectionId( ) );
                    consolidateDefinition.getMergedIdentities( ).add( mergedDto );
                }
                identity.setConsolidate( consolidateDefinition );
            }
        }
    }

    /**
     * Compute the {@link IdentityDto} coverage of the {@link ServiceContract} requirements. <br>
     * Rule:<br>
     * The coverage is set to 1 when
     * <ul>
     * <li>All mandatory keys defined in CS must be present in identity and match the defined minimum level</li>
     * <li>Optional keys (not mandatory but with a minimum level defined in CS) can be absent in identity, but if present must match the defined minimum
     * level</li>
     * </ul>
     * Otherwise, the coverage is set to 0
     * 
     * @param identity
     *            the identity to qualify
     * @param serviceContract
     *            the base service contract
     */
    private void computeCoverage( final IdentityDto identity, final ServiceContract serviceContract )
    {
        final Set<String> mandatoryKeys = serviceContract.getAttributeRights( ).stream( ).filter( AttributeRight::isMandatory )
                .map( AttributeRight::getAttributeKey ).map( AttributeKey::getKeyName ).collect( Collectors.toSet( ) );
        final Set<String> identityKeys = identity.getAttributes( ).stream( ).map( AttributeDto::getKey ).collect( Collectors.toSet( ) );

        if ( identity.getQuality( ) == null )
        {
            identity.setQuality( new QualityDefinition( ) );
        }

        if ( !identityKeys.containsAll( mandatoryKeys ) )
        {
            // Some mandatory attributes are missing
            identity.getQuality( ).setCoverage( 0 );
        }
        else
        {
            // All mandatory attributes are present, check all present attributes match the minimum certification level if defined in CS
            boolean coverageMatches = identity.getAttributes( ).stream( ).noneMatch( certifiedAttribute -> {
                final AttributeRequirement requirement = serviceContract.getAttributeRequirements( ).stream( )
                        .filter( req -> Objects.equals( req.getAttributeKey( ).getKeyName( ), certifiedAttribute.getKey( ) ) ).findFirst( ).orElse( null );
                final int attributeLevel = certifiedAttribute.getCertificationLevel( ) != null ? certifiedAttribute.getCertificationLevel( ) : 0;
                final int minLevel = ( requirement != null && requirement.getRefCertificationLevel( ) != null
                        && requirement.getRefCertificationLevel( ).getLevel( ) != null )
                                ? Integer.parseInt( requirement.getRefCertificationLevel( ).getLevel( ) )
                                : 0;
                return minLevel > attributeLevel;
            } );
            identity.getQuality( ).setCoverage( coverageMatches ? 1 : 0 );
        }
    }

    public void computeQuality( final IdentityDto identity )
    {
        if ( identity.getQuality( ) == null )
        {
            identity.setQuality( new QualityDefinition( ) );
        }
        final AtomicInteger levels = new AtomicInteger( );
        for ( final AttributeDto attribute : identity.getAttributes( ) )
        {
            if ( attribute.getCertificationLevel( ) == null || attribute.getCertificationLevel( ) == 0 || StringUtils.isBlank( attribute.getValue( ) ) )
            {
                continue;
            }
            final AttributeKey attributeKey = IdentityAttributeService.instance( ).getAttributeKeySafe( attribute.getKey( ) );
            if ( attributeKey != null && attributeKey.getKeyWeight( ) > 0 )
            {
                levels.addAndGet( attributeKey.getKeyWeight( ) * attribute.getCertificationLevel( ) );
            }
        }
        identity.getQuality( ).setQuality( levels.doubleValue( ) / _qualityBaseCache.get( ) );
    }

    private void computeMatchScore( final IdentityDto identity, final List<SearchAttribute> searchAttributes )
    {
        if ( identity.getQuality( ) == null )
        {
            identity.setQuality( new QualityDefinition( ) );
        }

        if ( CollectionUtils.isEmpty( searchAttributes ) )
        {
            identity.getQuality( ).setScoring( 1.0 );
        }
        else
        {
            final AtomicDouble levels = new AtomicDouble( );
            final AtomicDouble base = new AtomicDouble( );
            final Map<SearchAttribute, List<AttributeKey>> attributesToProcess = new HashMap<>( );
            for ( final SearchAttribute searchAttribute : searchAttributes )
            {
                AttributeKey refKey = null;
                try
                {
                    refKey = IdentityAttributeService.instance( ).getAttributeKey( searchAttribute.getKey( ) );
                }
                catch( final ResourceNotFoundException e )
                {
                    // do nothing, we check if attribute exists
                }
                if ( refKey != null )
                {
                    attributesToProcess.put( searchAttribute, Collections.singletonList( refKey ) );
                }
                else
                {
                    // In this case we have a common search key in the request, so retrieve the attribute
                    final List<AttributeKey> commonAttributes = IdentityAttributeService.instance( ).getCommonAttributeKeys( searchAttribute.getKey( ) );
                    attributesToProcess.put( searchAttribute, commonAttributes );
                }
            }

            for ( final Map.Entry<SearchAttribute, List<AttributeKey>> entry : attributesToProcess.entrySet( ) )
            {
                for ( final AttributeKey attributeKey : entry.getValue( ) )
                {
                    final AttributeDto attributeDto = identity.getAttributes( ).stream( )
                            .filter( attribute -> Objects.equals( attribute.getKey( ), attributeKey.getKeyName( ) ) ).findFirst( ).orElse( null );
                    base.addAndGet( attributeKey.getKeyWeight( ) );
                    if ( attributeDto != null && attributeDto.getValue( ).equalsIgnoreCase( entry.getKey( ).getValue( ) ) )
                    {
                        levels.addAndGet( attributeKey.getKeyWeight( ) );
                    }
                    else
                    {
                        final double penalty = Double.parseDouble( AppPropertiesService.getProperty( "identitystore.identity.scoring.penalty", "0.3" ) );
                        levels.addAndGet( attributeKey.getKeyWeight( ) - ( attributeKey.getKeyWeight( ) * penalty ) );
                    }
                }
            }

            identity.getQuality( ).setScoring( levels.doubleValue( ) / base.doubleValue( ) );
        }

    }
}

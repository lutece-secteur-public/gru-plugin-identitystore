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
package fr.paris.lutece.plugins.identitystore.v3.web.request.validator;

import fr.paris.lutece.plugins.identitystore.business.identity.Identity;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.ResponseStatusFactory;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;

import java.sql.Timestamp;
import java.util.Objects;

public class IdentityValidator
{

    private static IdentityValidator instance;

    public static IdentityValidator instance( )
    {
        if ( instance == null )
        {
            instance = new IdentityValidator( );
        }
        return instance;
    }

    private IdentityValidator( )
    {

    }

    /**
     * Checks if the sent last update date correlates with the actual identity's last update date
     * 
     * @param existingIdentityToUpdate
     *            the identity
     * @param requestLastUpdateDate
     *            the sent last update date
     * @throws ResourceConsistencyException
     *             if the dates don't correlate
     */
    public void checkIdentityLastUpdateDate( final IdentityDto existingIdentityToUpdate, final Timestamp requestLastUpdateDate )
            throws ResourceConsistencyException
    {
        if ( !Objects.equals( existingIdentityToUpdate.getLastUpdateDate( ), requestLastUpdateDate ) )
        {
            final ResourceConsistencyException exception = new ResourceConsistencyException(
                    "This identity has been updated recently, please load the latest data before updating.", Constants.PROPERTY_REST_ERROR_UPDATE_CONFLICT,
                    IdentityChangeResponse.class );
            ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( existingIdentityToUpdate.getCustomerId( ) );
            throw exception;
        }
    }

    /**
     * Checks if the identity to update is not merged
     *
     * @param existingIdentityToUpdate
     *            the identity
     * @throws ResourceConsistencyException
     *             if the identity is already merged
     */
    public void checkIdentityMergedStatusForUpdate( final IdentityDto existingIdentityToUpdate ) throws ResourceConsistencyException
    {
        if ( existingIdentityToUpdate.isMerged( ) )
        {
            final Identity masterIdentity = IdentityHome.findMasterIdentityByCustomerId( existingIdentityToUpdate.getCustomerId( ) );
            final ResourceConsistencyException exception = new ResourceConsistencyException(
                    "Cannot update a merged Identity. Master identity customerId is provided in the response.",
                    Constants.PROPERTY_REST_ERROR_FORBIDDEN_UPDATE_ON_MERGED_IDENTITY, IdentityChangeResponse.class );
            ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( masterIdentity.getCustomerId( ) );
            throw exception;
        }
    }

    /**
     * Checks if the identity to update is not merged
     * 
     * @param identityToDelete
     *            identity to delete
     * @throws ResourceConsistencyException
     *             if identity is merged
     */
    public void checkIDentityMergeStatusForDelete( final IdentityDto identityToDelete ) throws ResourceConsistencyException
    {
        if ( identityToDelete.isMerged( ) )
        {
            final ResourceConsistencyException exception = new ResourceConsistencyException( "Identity in merged state can not be deleted.",
                    Constants.PROPERTY_REST_ERROR_FORBIDDEN_DELETE_ON_MERGED_IDENTITY, IdentityChangeResponse.class );
            ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( identityToDelete.getCustomerId( ) );
            throw exception;
        }
    }

    /**
     * Checks if the identity to update is already deleted
     *
     * @param existingIdentityToUpdate
     *            the identity
     * @throws ResourceConsistencyException
     *             if the identity is already deleted
     */
    public void checkIdentityDeletedStatusForUpdate( final IdentityDto existingIdentityToUpdate ) throws ResourceConsistencyException
    {
        if ( existingIdentityToUpdate.isDeleted( ) )
        {
            final ResourceConsistencyException exception = new ResourceConsistencyException( "Cannot update a deleted Identity",
                    Constants.PROPERTY_REST_ERROR_FORBIDDEN_UPDATE_ON_DELETED_IDENTITY, IdentityChangeResponse.class );
            ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( existingIdentityToUpdate.getCustomerId( ) );
            throw exception;
        }
    }

    /**
     * Checks if the identity to delete is already deleted
     *
     * @param identityToDelete
     *            the identity
     * @throws ResourceConsistencyException
     *             if the identity is already deleted
     */
    public void checkIdentityDeletedStatusForDelete( final IdentityDto identityToDelete ) throws ResourceConsistencyException
    {
        if ( identityToDelete.isDeleted( ) )
        {
            final ResourceConsistencyException exception = new ResourceConsistencyException( "Identity allready in deleted state.",
                    Constants.PROPERTY_REST_ERROR_IDENTITY_ALREADY_DELETED, IdentityChangeResponse.class );
            ( (IdentityChangeResponse) exception.getResponse( ) ).setCustomerId( identityToDelete.getCustomerId( ) );
            throw exception;
        }
    }

    /**
     * Checks if the secondary identity to unmerge is already merged to the primary identity
     * 
     * @param primaryIdentity
     *            the primary identity
     * @param secondaryIdentity
     *            the secondary identity
     * @throws ResourceConsistencyException
     *             if the secondary identity is not merged to the primary identity
     */
    public void checkIdentityMergedStatusForMergeCancel( final IdentityDto primaryIdentity, final IdentityDto secondaryIdentity )
            throws ResourceConsistencyException
    {
        if ( !secondaryIdentity.isMerged( ) )
        {
            throw new ResourceConsistencyException( "Secondary identity found with customer_id " + secondaryIdentity.getCustomerId( ) + " is not merged",
                    Constants.PROPERTY_REST_ERROR_SECONDARY_IDENTITY_NOT_MERGED );
        }

        if ( !Objects.equals( secondaryIdentity.getMerge( ).getMasterCustomerId( ), primaryIdentity.getCustomerId( ) ) )
        {
            throw new ResourceConsistencyException(
                    "Secondary identity found with customer_id " + secondaryIdentity.getCustomerId( )
                            + " is not merged to Primary identity found with customer ID " + primaryIdentity.getCustomerId( ),
                    Constants.PROPERTY_REST_ERROR_IDENTITIES_NOT_MERGED_TOGETHER );
        }

    }

}

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
package fr.paris.lutece.plugins.identitystore.business.identity;

/**
 *
 * Constant class for identity
 *
 */
public final class IdentityConstants
{
    public static final String PROPERTY_APPLICATION_CODE = "identitystore.application.code";
    public static final String PROPERTY_ATTRIBUTE_USER_NAME_GIVEN = "identitystore.identity.attribute.user.name.given";
    public static final String PROPERTY_ATTRIBUTE_USER_PREFERRED_NAME = "identitystore.identity.attribute.user.name.preferred-username";
    public static final String PROPERTY_ATTRIBUTE_USER_FAMILY_NAME = "identitystore.identity.attribute.user.name.family.name";
    public static final String PROPERTY_ATTRIBUTE_USER_HOMEINFO_ONLINE_EMAIL = "identitystore.identity.attribute.user.home-info.online.email";
    public static final String PROPERTY_ATTRIBUTE_USER_HOMEINFO_TELECOM_TELEPHONE_NUMBER = "identitystore.identity.attribute.user.home-info.telecom.telephone.number";
    public static final String PROPERTY_ATTRIBUTE_USER_HOMEINFO_TELECOM_MOBILE_NUMBER = "identitystore.identity.attribute.user.home-info.telecom.mobile.number";
    public static final String PROPERTY_ATTRIBUTE_USER_GENDER = "identitystore.identity.attribute.user.gender";
    public static final String PROPERTY_ATTRIBUTE_USER_BDATE = "identitystore.identity.attribute.user.bdate";

    // Security
    public static final String PROPERTY_SECURE_MODE = "identitystore.secureMode";
    public static final String PROPERTY_JWT_CLAIM_APP_CODE = "identitystore.jwt.appCode.claimName";

    public static final String ALL_ATTRIBUTES_FILTER = "all";
    public static final String GUID_FILTER = "guid";
    public static final String CID_FILTER = "cid";
    public static final String ATTRIBUTE_USER_GUID_LABEL = "identitystore.search_identities.columnConnectionId";
    public static final String ATTRIBUTE_USER_CID_LABEL = "identitystore.search_identities.columnCustomerId";
    public static final String ATTRIBUTE_ALL_LABEL = "identitystore.search_identities.allAttributes";

    /**
     * private constructor
     */
    private IdentityConstants( )
    {
    }
}

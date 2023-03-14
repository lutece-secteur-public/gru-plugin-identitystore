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
package fr.paris.lutece.plugins.identitystore;

/**
 * Init test context, such has certifiers
 */
public class IdentityStoreTestContext
{
    public static final String ELASTICSEARCH_VERSION = "8.6.2";
    public static final String POSTGRES_VERSION = "9.4.26";
    public final static String SAMPLE_APPCODE = "TEST";
    public final static String ATTRKEY_1 = "first_name";
    public final static String ATTRVAL_1 = "John";
    public final static String ATTRKEY_2 = "family_name";
    public final static String ATTRKEY_3 = "preferred_username";
    public final static String CERTIFIER1_CODE = "fc";
    public final static String CERTIFIER2_CODE = "agent";
    public final static String CERTIFIER3_CODE = "courrier";
    public final static String CERTIFIER4_CODE = "mail";
    public final static String CERTIFIER5_CODE = "sms";
    public final static String CERTIFIER6_CODE = "r2p";
    public final static String CERTIFIER7_CODE = "fc";
    public final static String CERTIFIER8_CODE = "mon_paris";
    public final static String CERTIFIER9_CODE = "pj";

    public final static String SAMPLE_CONNECTIONID = "azerty";
    public final static String SAMPLE_CUSTOMERID = "3F2504E0-4F89-11D3-9A0C-0305E82C3301";
    public final static int SAMPLE_NB_ATTR = 18;
}

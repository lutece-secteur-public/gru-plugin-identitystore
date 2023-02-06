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
package fr.paris.lutece.plugins.identitystore.business.contract;

import java.util.List;
import java.util.Map;

import fr.paris.lutece.portal.service.plugin.Plugin;

/**
 * IAttributeRightDAO Interface
 */
public interface IAttributeRightDAO
{
    String BEAN_NAME = "identitystore.attributeRightDAO";

    /**
     * Insert a new record in the table.
     *
     * @param attributeRight
     *            instance of the AttributeRight object to insert
     * @param plugin
     *            the Plugin
     */
    void insert( AttributeRight attributeRight, int serviceContractId, Plugin plugin );

    /**
     * Update the record in the table
     *
     * @param attributeRight
     *            the reference of the AttributeRight
     * @param plugin
     *            the Plugin
     */
    void store( AttributeRight attributeRight, int serviceContractId, Plugin plugin );

    // /////////////////////////////////////////////////////////////////////////
    // Finders

    /**
     * retrieve all attributes and its rights for provided application
     *
     * @param serviceContract
     *            application
     * @param plugin
     *            the Plugin
     * @return list of all attributes with rights for provided application
     */
    List<AttributeRight> selectAttributeRights( ServiceContract serviceContract, Plugin plugin );

    /**
     * remove all rights for provided application
     *
     * @param serviceContract
     *            serviceContract
     * @param plugin
     *            the Plugin
     */
    void removeAttributeRights( ServiceContract serviceContract, Plugin plugin );

    /**
     * @param plugin
     *            the Plugin
     * @return map of all attribute wich have application rights
     */
    Map<String, AttributeApplicationsRight> getAttributeApplicationsRight( Plugin plugin );
}

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
package fr.paris.lutece.plugins.identitystore.v3.web.rs;

import fr.paris.lutece.plugins.identitystore.service.indexer.elastic.index.task.FullIndexTask;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.spring.SpringContextService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * ClientRest
 */
@Path( RestConstants.BASE_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class IndexingRestService
{

    public static final String INDEX_STATUS = "indexing/status";

    private FullIndexTask fullIndexTask;

    /**
     * Get indexing process status
     * @return the status
     */
    @Path(INDEX_STATUS )
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getIndexStatus( )
    {
        final FullIndexTask service = this.getFullIndexTask();
        if ( service == null )
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR ).entity( "{}" ).type( MediaType.APPLICATION_JSON ).build( );
        }
        else
        {
            return Response.status( Response.Status.OK ).entity( service.getStatus( ) ).type( MediaType.APPLICATION_JSON ).build( );
        }
    }

    /**
     * Permet de récupérer le bean quand il est prêt dans le lifeCycle,
     * car il n'est pas possible de le fournir sous forme de param de constructeur,
     * ce n'est pas bien géré par Jersey.
     * @return le {@link FullIndexTask}
     */
    public FullIndexTask getFullIndexTask() {
        if( fullIndexTask == null )
        {
            fullIndexTask = SpringContextService.getBean( "identitystore.fullIndexer" );
        }
        return fullIndexTask;
    }

}

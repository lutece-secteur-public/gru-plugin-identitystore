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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome;
import fr.paris.lutece.plugins.identitystore.business.identity.IndicatorsActionsType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.IdentityChangeType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClientRest
 */
@Path( Constants.INTERNAL_API_PATH + Constants.PLUGIN_PATH + Constants.VERSION_PATH_V3 )
public class IndicatorsRestService
{

    public static final String INDICATORS_COUNT_ACTIONSBYTIME = "indicators/count/actionsbytime";
    public static final String INDICATORS_ATTRIBUTESBYIDENTITIES = "indicators/count/attributesbyidentities";
    public static final String INDICATORS_UNMERGED_NO_ATTRIBUTES = "indicators/count/unmergednoattributes";

    final static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get all Clients
     * @return the Client
     */
    @Path(INDICATORS_COUNT_ACTIONSBYTIME)
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getActionsCountByTime( @QueryParam("duration") final Integer duration )
    {

        boolean sendDefault = true;
        final List<Action> results = new ArrayList<>();
        if( duration != null )
        {
            final List<IndicatorsActionsType> listIndicators = IdentityHome.getActionsTypesDuringInterval(duration);
            if( !listIndicators.isEmpty() )
            {
                sendDefault = false;
                for ( final IndicatorsActionsType action : listIndicators )
                {
                    final Action result = new Action();
                    result.setAuthorType( action.getAuthorType( ) );
                    result.setChangeStatus(action.getChangeStatus( ) );
                    result.setChangeType(IdentityChangeType.valueOf(action.getChangeType()).name());
                    result.setClientCode( action.getClientCode( ) );
                    result.setCountActions( String.valueOf(action.getCountActions()) );
                    results.add( result );
                }
            }
        }

        if ( sendDefault )
        {
            final Action result = new Action();
            result.setAuthorType( "-" );
            result.setChangeStatus( "-" );
            result.setChangeType( "-" );
            result.setClientCode( "-" );
            result.setCountActions( "-" );
            results.add( result );
        }

        return Response.status( 200 ).entity( results ).type( MediaType.APPLICATION_JSON ).build( );
    }

    /**
     * Get all Clients
     * @return the Client
     */
    @Path(INDICATORS_ATTRIBUTESBYIDENTITIES)
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getAttributesByIdentities( ) throws JsonProcessingException {
        final Map<Integer, Integer> attributesByIdentities = IdentityHome.getCountAttributesByIdentities( );
        final String results = objectMapper.writeValueAsString(attributesByIdentities);
        return Response.status( 200 ).entity( results ).type( MediaType.APPLICATION_JSON ).build( );
    }

    /**
     * Get all Clients
     * @return the Client
     */
    @Path(INDICATORS_UNMERGED_NO_ATTRIBUTES)
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getCountUnmergedIdentitiesWithoutAttributes( ) {
        return Response.status( 200 ).entity( IdentityHome.getCountUnmergedIdentitiesWithoutAttributes().toString() ).type( MediaType.APPLICATION_JSON ).build( );
    }

    private static class Action
    {
        private String changeType;
        private String changeStatus;
        private String authorType;
        private String clientCode;
        private String countActions;

        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }

        public String getChangeStatus() {
            return changeStatus;
        }

        public void setChangeStatus(String changeStatus) {
            this.changeStatus = changeStatus;
        }

        public String getAuthorType() {
            return authorType;
        }

        public void setAuthorType(String authorType) {
            this.authorType = authorType;
        }

        public String getClientCode() {
            return clientCode;
        }

        public void setClientCode(String clientCode) {
            this.clientCode = clientCode;
        }

        public String getCountActions() {
            return countActions;
        }

        public void setCountActions(String countActions) {
            this.countActions = countActions;
        }
    }
}

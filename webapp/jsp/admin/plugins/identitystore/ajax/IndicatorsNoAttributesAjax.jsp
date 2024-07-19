<%@ page import="fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome" %>
<%
    Integer countUnmergedNoAttrIdentities = IdentityHome.getCountUnmergedIdentitiesWithoutAttributes();
%>
<%=countUnmergedNoAttrIdentities%>
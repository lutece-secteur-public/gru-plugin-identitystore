<%@ page import="java.util.Map" %>
<%@ page import="fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome" %>
<%
    Map<Integer, Integer> attributesByIdentities = IdentityHome.getCountAttributesByIdentities();
    String result = "";
    for( Map.Entry<Integer, Integer> entry : attributesByIdentities.entrySet()){
        result += "<tr><td><span>"
                + entry.getKey()
                + "</span></td><td><span>"
                + entry.getValue()
                + "</span></td></tr>";
    }
%>

<%=
result
%>
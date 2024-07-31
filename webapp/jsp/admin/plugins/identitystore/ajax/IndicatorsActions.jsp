
<%@ page import="fr.paris.lutece.plugins.identitystore.business.identity.IdentityHome" %>
<%@ page import="fr.paris.lutece.plugins.identitystore.business.identity.IndicatorsActionsType" %>
<%@ page import="java.util.List" %>
<%@ page import="com.mysql.cj.util.StringUtils" %>
<%
    int data = Integer.parseInt(request.getParameter("data"));
    String result = "";
    List<IndicatorsActionsType> listIndicators = IdentityHome.getActionsTypesDuringInterval(data);
    if(!listIndicators.isEmpty())
    {
        for (IndicatorsActionsType action : listIndicators)
        {
            result += "<tr><td><span>"
                    + action.getChangeType()
                    + "</span></td><td><span>"
                    + action.getChangeStatus()
                    + "</span></td><td><span>"
                    + action.getAuthorType()
                    + "</span></td><td><span>"
                    + action.getClientCode()
                    + "</span></td><td><span>"
                    + action.getCountActions()
                    + "</span></td></tr>";
        }
    }
    else{
        result += "<tr><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td><td><span>-</span></td></tr>";
    }
%>
<%=
result
%>
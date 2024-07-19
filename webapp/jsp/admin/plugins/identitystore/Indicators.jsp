<jsp:useBean id="indicators" scope="session" class="fr.paris.lutece.plugins.identitystore.web.IndicatorsJspBean" />
<% String strContent = indicators.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>

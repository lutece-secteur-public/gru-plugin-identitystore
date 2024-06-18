<jsp:useBean id="manageidentitiesHistory" scope="session" class="fr.paris.lutece.plugins.identitystore.web.IdentitiesHistoryJspBean" />
<% String strContent = manageidentitiesHistory.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>

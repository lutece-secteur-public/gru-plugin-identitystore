<jsp:useBean id="manageprocessusrefRefAttributeCertificationLevel" scope="session" class="fr.paris.lutece.plugins.identitystore.web.RefAttributeCertificationLevelJspBean" />
<% String strContent = manageprocessusrefRefAttributeCertificationLevel.processController ( request , response ); %>

<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../AdminHeader.jsp" />

<%= strContent %>

<%@ include file="../../AdminFooter.jsp" %>
